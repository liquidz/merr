(ns merr.core-test
  (:require
   #?@(:clj  [[clojure.test :as t]
              [clojure.java.io :as io]
              [merr.core :as sut]
              testdoc.core]
       :cljs [[cljs.test :as t :include-macros true]
              [merr.core :as sut :include-macros true]])))

#?(:clj (println "Clojure version:" (clojure-version)))

#?(:clj
   (t/deftest docstring-test
     (t/is (testdoc #'sut/err?))
     (t/is (testdoc #'sut/err))
     (t/is (testdoc #'sut/let))
     (t/is (testdoc #'sut/type))
     (t/is (testdoc #'sut/message))
     (t/is (testdoc #'sut/data))
     (t/is (testdoc #'sut/cause))
     (t/is (testdoc #'sut/->))
     (t/is (testdoc #'sut/->>))))

#?(:clj
   (t/deftest README-test
     (t/is (testdoc (slurp (io/file "README.adoc"))))))

(def ^:private _det sut/default-error-type)

(t/deftest err-test
  (t/are [x y] (= x y)
    (sut/->MerrError _det nil nil nil) (sut/err)
    (sut/->MerrError :foo nil nil nil) (sut/err {:type :foo})
    (sut/->MerrError _det "hello" nil nil) (sut/err {:message "hello"})
    (sut/->MerrError _det nil {:foo "bar"} nil) (sut/err {:data {:foo "bar"}})
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
      (t/is (nil? baz))
      (t/is (sut/err? +err+))
      (t/is (= (:message +err+) "ERR"))))

  (t/testing "ignore error"
    (sut/let +err+ [foo ^:merr/ignore (sut/err)]
      (t/is (= foo (sut/err)))
      (t/is (nil? +err+))))

  (t/testing "clojure.core/let"
    (let [foo (sut/err)]
      (t/is (sut/err? foo)))))

(t/deftest ->-test
  (let [failinc (fn [i] (sut/err {:data i}))
        throwexp (fn [& _] (throw (ex-info "must not be called" {})))]
    (t/is (= 3 (sut/-> 1 inc inc)))
    (t/is (= 1 (sut/-> 1 (+ 1) (- 1))))
    (t/is (= (sut/->MerrError _det nil 2 nil)
             (sut/-> 1 inc failinc throwexp)))))

(t/deftest ->>-test
  (let [failinc (fn [i] (sut/err {:data i}))
        throwexp (fn [& _] (throw (ex-info "must not be called" {})))]
    (t/is (= 3 (sut/->> 1 inc inc)))
    (t/is (= -1 (sut/->> 1 (+ 1) (- 1))))
    (t/is (= (sut/->MerrError _det nil 2 nil)
             (sut/->> 1 inc failinc throwexp)))))
