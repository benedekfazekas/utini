(ns utini-test.keywords)

(defn some-keywords []
  ;; :keyword-in-semicolon-comment
  (comment :keyword-in-comment-tag)
  [:keyword-foo
   :keyword-bar])

(defn clojure-core-deconstruct-test [{:keys [fooparam]}]
  (do
    (doseq [{:keys [foodoseq]} fooparam]
      (println "foodoseq" foodoseq))
    (loop [{:keys [fooloop]} fooparam]
      (println "fooloop" fooloop))
    (if-let [{:keys [fooiflet]} fooparam]
      (println "fooiflet" fooiflet)
      (println "boo"))
    (when-let [{:keys [foowhenlet]} fooparam]
      (println "foowhenlet" foowhenlet))
    (if-some [{:keys [fooifsome]} fooparam]
      [fooifsome 1])
    (when-some [{:keys [foowhensome]} fooparam]
      [foowhensome 1])
    (for [{:keys [foofor]} [fooparam]
          :let [{:keys [fooforfor]} foofor]]
      (println "foofor" foofor "fooforfor" fooforfor))))
