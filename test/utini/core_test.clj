(ns utini.core-test
  (:require [utini.core :as sut]
            [clojure.test :as t]))

(t/deftest some-keywords-found-test
  (t/testing "keywords found"
    (let [all-testproject-keywords (sut/distinct-keywords-in-dir "testproject")]
      (t/is (some #{:keyword-foo} all-testproject-keywords) ":keyword-foo not found")
      (t/is (some #{:keyword-bar} all-testproject-keywords) ":keyword-bar not found"))))

(t/deftest keyword-in-comment-test
  (t/testing "keyword in comment"
    (let [all-testproject-keywords (sut/distinct-keywords-in-dir "testproject")]
      (t/is (not (some #{:keyword-in-semicolon-comment} all-testproject-keywords)))
      (t/is (some #{:keyword-in-comment-tag} all-testproject-keywords)))))

(t/deftest throw-on-error-test
  (t/testing "throwing on macro expansion error optionally"
    (t/is (thrown-with-msg? clojure.lang.ExceptionInfo #"forms could not be expanded" (sut/distinct-keywords-in-dir "testproject" :throw-on-error true)))))

(t/deftest deconstruct-test
  (t/testing "deconstruction"
    (let [all-testproject-keywords (sut/distinct-keywords-in-dir "testproject")]
      (t/is (some #{:fooparam} all-testproject-keywords))
      (t/is (some #{:foodoseq} all-testproject-keywords))
      (t/is (some #{:fooloop} all-testproject-keywords))
      (t/is (some #{:fooiflet} all-testproject-keywords))
      (t/is (some #{:foowhenlet} all-testproject-keywords))
      (t/is (some #{:fooifsome} all-testproject-keywords))
      (t/is (some #{:foowhensome} all-testproject-keywords))
      (t/is (some #{:foofor} all-testproject-keywords))
      (t/is (some #{:fooforfor} all-testproject-keywords)))))
