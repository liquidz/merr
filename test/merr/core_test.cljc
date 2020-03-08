(ns merr.core-test
  (:require
   #?@(:clj  [[clojure.test :as t]
              [merr.core :as sut]
              testdoc.core]
       :cljs [[cljs.test :as t :include-macros true]
              [merr.core :as sut :include-macros true]])))

#?(:clj
   (t/deftest docstring-test
     (t/is (testdoc #'sut/err?))
     (t/is (testdoc #'sut/err))
     (t/is (testdoc #'sut/let))
     (t/is (testdoc #'sut/type))
     (t/is (testdoc #'sut/message))
     (t/is (testdoc #'sut/data))
     (t/is (testdoc #'sut/cause))
     (t/is (testdoc #'sut/assert))))

(def ^:private _det sut/default-error-type)

(t/deftest err-test
  (t/are [x y] (= x y)
    (sut/->MerrError _det nil nil nil) (sut/err)
    (sut/->MerrError :foo nil nil nil) (sut/err {:type :foo})
    (sut/->MerrError _det "hello" nil nil) (sut/err {:message "hello"}) (sut/->MerrError _det nil {:foo "bar"} nil) (sut/err {:data {:foo "bar"}})
    (sut/->MerrError _det nil nil (sut/err)) (sut/err {:cause (sut/err)}))

  (let [e (sut/err {:extra "hello"})]
    (t/is (sut/err? e))
    (t/is (= (:type e) _det))
    (t/is (= (:extra e) "hello"))))

(t/deftest err?-test
  (t/are [x y] (= x (sut/err? y))
    true  (sut/err)
    true  (sut/err {:message "foo"})
    true  (assoc (sut/err {:message "foo"}) :foo "bar")
    true  (sut/err {:cause (sut/err)})
    false (merge {} (sut/err))
    false true
    false false
    false nil))

(t/deftest let-test
  (t/testing "succeeded"
    (sut/let +err+ [foo 1
                    bar (inc foo)
                    baz (inc bar)]
      (t/is (= foo 1))
      (t/is (= bar 2))
      (t/is (= baz 3))
      (t/is (nil? +err+))))

  (t/testing "failed"
    (sut/let +err+ [foo 1
                    bar (sut/err {:message "ERR"})
                    baz (inc bar)]
      (t/is (= foo 1))
      (t/is (nil? bar))
      (t/is (nil? bar))
      (t/is (sut/err? +err+))
      (t/is (= (:message +err+) "ERR"))))

  (t/testing "ignore error"
    (sut/let +err+ [foo ^:merr/ignore (sut/err)]
      (t/is (= foo (sut/err)))
      (t/is (nil? +err+))))

  (t/testing "clojure.core/let"
    (let [foo (sut/err)]
      (t/is (sut/err? foo)))))

(t/deftest assert-test
  (let [a (atom nil)
        ret (sut/assert true {:message (do (reset! a "foo") "foo")})]
    (t/is (nil? ret))
    (t/is (nil? @a)))

  (let [a (atom nil)
        ret (sut/assert false {:message (do (reset! a "foo") "foo")})]
    (t/is (= ret (sut/err {:message "foo"})))
    (t/is (not (nil? @a)))))
