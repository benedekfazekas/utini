(ns utini.ns.reader
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as readers]
            [clojure.walk :as walk]))

(defn flatten-prefix-notations [block]
  (if (coll? (second block))
    (map #(cons (symbol (str (first block) "." (first %))) (rest %)) (rest block))
    [block]))

(defn build-alias-map [ns-form]
  (->> (filter list? ns-form)
       (some #(when (#{:require} (first %)) %))
       rest
       (remove symbol?)
       (mapcat flatten-prefix-notations)
       (filter #(contains? (set %) :as))
       (map (juxt #(->> (rest %)
                        (partition-by #{:as})
                        last
                        first)
                  first))
       (into {})))

(def to-expand-for-deconstruct #{'defn                   'clojure.core/fn 'fn
                                'clojure.core/let       'let
                                'clojure.core/if-let    'if-let
                                'clojure.core/when-let  'when-let
                                'clojure.core/if-some   'if-some
                                'clojure.core/when-some 'when-some
                                'clojure.core/for       'for
                                'clojure.core/doseq     'doseq
                                'clojure.core/loop      'loop})

(defn macroexpand-some-forms [syms-to-expand form]
  (walk/prewalk
   (fn [node]
     (if (syms-to-expand (and (coll? node) (first node)))
       (macroexpand node)
       node))
   form))

(defn read-ns [file-name]
  (let [cljs? (str/ends-with? file-name ".cljs")
        features (if cljs? #{:cljs} #{:clj})
        ns-form-maybe (binding [*read-eval* false]
                        (reader/read-string {:read-cond :allow
                                             :features features} (slurp file-name)))]
    (with-open [rdr (io/reader file-name)]
      (let [pbr (readers/indexing-push-back-reader (java.io.PushbackReader. rdr) 10 file-name)
            eof (Object.)]
        (binding [*read-eval* false
                  reader/*alias-map* (build-alias-map ns-form-maybe)
                  reader/*data-readers* (if cljs? {'js (constantly "js")} {})]
          (loop [form-nodes (transient [])]
            (let [form (reader/read {:eof       eof
                                     :read-cond :allow
                                     :features  features} pbr)
                  expanded-or-error (try {:expanded-form (macroexpand-some-forms to-expand-for-deconstruct form)}
                                         (catch Throwable t {:error t}))
                  form-node (merge {:form form} expanded-or-error)]
              (if (identical? form eof)
                (persistent! form-nodes)
                (recur (conj! form-nodes form-node))))))))))

(defn harvest [form-nodes pred]
  (let [acc    (transient [])
        acc-fn (fn [fm]
                 (when (pred fm)
                   (conj! acc fm))
                 fm)]
    (doseq [{:keys [form expanded-form error]} form-nodes]
      (walk/postwalk acc-fn form)
      (when-not error
        (walk/postwalk acc-fn expanded-form)))
    (persistent! acc)))
