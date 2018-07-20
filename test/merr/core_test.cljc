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

(def ^:private _det sut/default-error-type)

(deftest err-test
  (are [x y] (= x y)
    (sut/->MerrError _det nil nil nil) (sut/err)
    (sut/->MerrError :foo nil nil nil) (sut/err {:type :foo})
    (sut/->MerrError _det "hello" nil nil) (sut/err {:message "hello"})
    (sut/->MerrError _det nil {:foo "bar"} nil) (sut/err {:data {:foo "bar"}})
    (sut/->MerrError _det nil nil (sut/err)) (sut/err {:cause (sut/err)}))

  (let [e (sut/err {:extra "hello"})]
    (is (instance? merr.core.MerrError e))
    (is (= (:type e) _det))
    (is (= (:extra e) "hello"))))

(deftest err?-test
  (are [x y] (= x (sut/err? y))
    true  (sut/err)
    true  (sut/err {:message "foo"})
    true  (assoc (sut/err {:message "foo"}) :foo "bar")
    true  (sut/err {:cause (sut/err)})
    false (merge {} (sut/err))
    false true
    false false
    false nil))

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
                    bar (sut/err {:message "ERR"})
                    baz (inc bar)]
      (is (= foo 1))
      (is (nil? bar))
      (is (nil? bar))
      (is (sut/err? +err+))
      (is (= (:message +err+) "ERR"))))

  (testing "ignore error"
    (sut/let +err+ [foo ^:merr/ignore (sut/err)]
      (is (= foo (sut/err)))
      (is (nil? +err+))))

  (testing "clojure.core/let"
    (let [foo (sut/err)]
      (is (sut/err? foo)))))
