(ns utini.core
  (:require [clojure.set :as set]
            [utini.files :as files]
            [utini.ns.reader :as nsr]))

(defn distinct-keywords-in-dir
  "A set of keywords in dir"
  [dir & {:keys [throw-on-error] :as opts}]
  (reduce
   set/union
   #{}
   (for [clj*-file (files/all-clj*-file-names-under dir)
         :let [forms (nsr/read-ns clj*-file)]]
     (do
       (when (and throw-on-error (some :error forms))
         (throw (ex-info "Some forms could not be expanded"
                         {:file   clj*-file
                          :errors (->> (map :error forms)
                                       (filter identity))})))
       (set (nsr/harvest forms keyword?))))))
