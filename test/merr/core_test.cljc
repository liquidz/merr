(ns merr.core-test
  (:require
   #?@(:clj  [[clojure.test :as t]
              [clojure.java.io :as io]
              [merr.core :as sut]
              [testdoc.core]]
       :cljs [[cljs.test :as t :include-macros true]
              [merr.core :as sut :include-macros true]])))

#?(:bb
   nil
   :clj (println "Clojure version:" (clojure-version)))

#?(:bb
   nil
   :clj
   (t/deftest docstring-test
     (t/is (testdoc #'sut/error?))
     (t/is (testdoc #'sut/error))
     (t/is (testdoc #'sut/let))
     (t/is (testdoc #'sut/type))
     (t/is (testdoc #'sut/message))
     (t/is (testdoc #'sut/data))
     (t/is (testdoc #'sut/cause))
     (t/is (testdoc #'sut/->))
     (t/is (testdoc #'sut/->>))
     (t/is (testdoc #'sut/try))))

#?(:bb
   nil
   :clj
   (t/deftest README-test
     (t/is (testdoc (slurp (io/file "README.adoc"))))))

(def ^:private _det sut/default-error-type)

(defn- err=
  [e1 e2]
  (every? #(= (% e1) (% e2))
          [sut/type sut/message sut/data sut/cause]))

(t/deftest error-test
  (t/are [x y] (err= x y)
    (sut/->MerrError _det nil nil nil) (sut/error)
    (sut/->MerrError :foo nil nil nil) (sut/error {:type :foo})
    (sut/->MerrError _det "hello" nil nil) (sut/error {:message "hello"})
    (sut/->MerrError _det nil {:foo "bar"} nil) (sut/error {:data {:foo "bar"}})
    (sut/->MerrError _det nil nil (sut/error)) (sut/error {:cause (sut/error)}))

  (let [e (sut/error {:extra "hello"})]
    (t/is (sut/error? e))
    (t/is (= (:type e) _det))
    (t/is (= (:extra e) "hello"))))

(t/deftest err?-test
  (t/are [x y] (= x (sut/error? y))
    true  (sut/error)
    true  (sut/error {:message "foo"})
    true  (assoc (sut/error {:message "foo"}) :foo "bar")
    true  (sut/error {:cause (sut/error)})
    false (merge {} (sut/error))
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
                    bar (sut/error {:message "ERR"})
                    baz (inc bar)]
      (t/is (= foo 1))
      (t/is (nil? bar))
      (t/is (nil? baz))
      (t/is (sut/error? +err+))
      (t/is (= (:message +err+) "ERR"))))

  (t/testing "ignore error"
    (sut/let +err+ [foo ^:merr/ignore (sut/error)]
      (t/is (= foo (sut/error)))
      (t/is (nil? +err+))))

  (t/testing "clojure.core/let"
    (let [foo (sut/error)]
      (t/is (sut/error? foo)))))

(t/deftest ->-test
  (let [failinc (fn [i] (sut/error {:data i}))
        throwexp (fn [& _] (throw (ex-info "must not be called" {})))]
    (t/is (= 3 (sut/-> 1 inc inc)))
    (t/is (= 1 (sut/-> 1 (+ 1) (- 1))))
    (t/is (err= (sut/->MerrError _det nil 2 nil)
                (sut/-> 1 inc failinc throwexp)))))

(t/deftest ->>-test
  (let [failinc (fn [i] (sut/error {:data i}))
        throwexp (fn [& _] (throw (ex-info "must not be called" {})))]
    (t/is (= 3 (sut/->> 1 inc inc)))
    (t/is (= -1 (sut/->> 1 (+ 1) (- 1))))
    (t/is (err= (sut/->MerrError _det nil 2 nil)
                (sut/->> 1 inc failinc throwexp)))))

(t/deftest try-test
  (let [ex (ex-info "test error" {::test 1})]
    (t/is (err= (sut/error {:message "test error"
                            :data {::test 1}
                            :cause ex})
                (sut/try (throw ex))))

    (t/is (err= (sut/error {:message "test error"
                            :data {::test 1}
                            :cause ex})
                (sut/try {} (throw ex))))

    (t/is (err= (sut/error {:type ::error
                            :message "new message"
                            :data {::test 1
                                   ::new 2}
                            :cause ex})
                (sut/try {:type ::error
                          :message "new message"
                          :data {::new 2}}
                         (throw ex)))))

  (let [ex (ex-info "test typed error" {:merr/type ::error})]
    (t/is (err= (sut/error {:type ::error
                            :message "test typed error"
                            :data {:merr/type ::error}
                            :cause ex})
                (sut/try (throw ex))))))
