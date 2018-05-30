(ns merr.core-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is are testing]])
            [merr.core :as sut]))

(deftest ok-test
  (is (= (sut/ok) [{} nil]))
  (is (= (sut/ok true) [true nil])))

(deftest err-test
  (is (= (sut/err) [nil {}]))
  (is (= (sut/err true) [nil true])))

(deftest result?-test
  (is (sut/result? (sut/ok)))
  (is (sut/result? (sut/err)))
  (is (not (sut/result? [true nil])))
  (is (not (sut/result? [nil true]))))

(deftest wrap-test
  (are [x y] (= x y)
    (sut/ok true) (sut/wrap true)
    (sut/ok true) (sut/wrap true "ERR")
    (sut/err) (sut/wrap nil)
    (sut/err "ERR") (sut/wrap nil "ERR")

    (sut/ok) (sut/wrap (sut/ok))
    (sut/err) (sut/wrap (sut/err))))

(deftest unwrap-test
  (are [x y] (= x (sut/unwrap y))
    "OK" (sut/ok "OK")
    nil (sut/err "ERR")))

(deftest err-let-success-test
  (sut/err-let +err+ [foo 1
                      bar (inc foo)]
    (is (= foo 1))
    (is (= bar 2))
    (is (nil? +err+)))

  (sut/err-let +err+ [foo (sut/ok 1)]
    (is (= foo (sut/ok 1)))
    (is (nil? +err+)))

  (sut/err-let +err+ [foo ^:merr (sut/ok 1)
                      bar (inc foo)]
    (is (= foo 1))
    (is (= bar 2))
    (is (nil? +err+))))

(deftest err-let-failure-test
  (sut/err-let +err+ [foo ^:merr (sut/err "ERR")]
    (is (nil? foo))
    (is (= +err+ "ERR")))

  (sut/err-let +err+ [foo ^:merr (sut/err "ERR")
                      bar ^:merr (sut/ok true)]
    (is (nil? foo))
    (is (nil? bar))
    (is (= +err+ "ERR")))

  (sut/err-let +err+ [foo ^:merr (sut/err "ERR")
                      bar true]
    (is (nil? foo))
    (is (nil? bar))
    (is (= +err+ "ERR"))))

(deftest err->-test
  (letfn [(fail [_ msg] (sut/err msg))]
    (are [x y] (= x y)
      (sut/ok 2) (sut/err-> 1 inc)
      (sut/ok 2) (sut/err-> 5 (- 3))
      (sut/err "ERR") (sut/err-> 1 (fail "ERR") inc)
      (sut/err "ERR") (sut/err-> (sut/err "ERR") inc))))

(deftest err->>-test
  (letfn [(fail [msg _] (sut/err msg))]
    (are [x y] (= x y)
      (sut/ok 2) (sut/err->> 1 inc)
      (sut/ok -2) (sut/err->> 5 (- 3))
      (sut/err "ERR") (sut/err->> 1 (fail "ERR") inc)
      (sut/err "ERR") (sut/err->> (sut/err "ERR") inc))))
