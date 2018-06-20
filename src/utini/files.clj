(ns utini.files
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io File]))

(defn file-ending-pred [ending file-name]
  (str/ends-with? file-name ending))

(defn all-files-under [root]
  (->> (file-seq (io/file (.getCanonicalPath (io/file root))))
       (remove #(.isDirectory ^File %))
       (map #(.getCanonicalPath ^File %))
       (remove #(re-find #"^\.?#" %))))

(defn all-clj*-file-names-under [root]
  (->> (all-files-under root)
       (filter (some-fn (partial file-ending-pred ".clj")
                        (partial file-ending-pred ".cljc")
                        (partial file-ending-pred ".cljs")))))
