(ns merr.core-test
  (:require #?@(:clj  [[clojure.test :refer :all]
                       [merr.core :as sut]
                       testdoc.core]
                :cljs [[cljs.test :refer-macros [deftest is are testing]]
                       [merr.core :as sut :include-macros true]])))

#?(:clj
   (deftest docstring-test
     (is (testdoc #'sut/err?))
     (is (testdoc #'sut/err))
     (is (testdoc #'sut/let))))

(deftest err-test
  (are [x y] (= x y)
    (sut/->MerrError true)          (sut/err)
    (sut/->MerrError true)          (sut/err true)
    (sut/->MerrError nil)           (sut/err nil)
    (sut/->MerrError true)          (sut/err (sut/err true))
    (sut/->MerrError {:value true}) (sut/err {:value true})))

(deftest err?-test
  (are [x y] (= x (sut/err? y))
    true  (sut/err)
    true  (sut/err 1)
    true  (assoc (sut/err 1) :foo "bar")
    true  (sut/err {:cause (sut/err 1)})
    false (merge {} (sut/err))
    false true
    false false
    false nil))

(deftest deref-test
  (are [x y] (= x y)
    true  @(sut/err)
    "ERR" @(sut/err "ERR")))

(deftest let-test
  (testing "succeeded"
    (sut/let +err+ [foo 1
                    bar (inc foo)
                    baz (inc bar)]
      (is (= foo 1))
      (is (= bar 2))
      (is (= baz 3))
      (is (nil? +err+))))

  (testing "failed"
    (sut/let +err+ [foo 1
                    bar (sut/err "ERR")
                    baz (inc bar)]
      (is (= foo 1))
      (is (nil? bar))
      (is (nil? bar))
      (is (sut/err? +err+))
      (is (= @+err+ "ERR"))))

  (testing "clojure.core/let"
    (let [foo (sut/err 1)]
      (is (= foo (sut/->MerrError 1))))))
