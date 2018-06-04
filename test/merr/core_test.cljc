(ns merr.core-test
  (:require #?@(:clj  [[clojure.test :refer :all]
                       [merr.core :as sut]]
                :cljs [[cljs.test :refer-macros [deftest is are testing]]
                       [merr.core :as sut :include-macros true]])))

(deftest ok-test
  (are [x y] (= x y)
    [sut/default-value nil] (sut/ok)
    [true nil]              (sut/ok true)
    [nil nil]               (sut/ok nil)
    [true nil]              (sut/ok (sut/ok true))
    [[true nil] nil]        (sut/ok [true nil])))

(deftest err-test
  (are [x y] (= x y)
    [nil sut/default-value] (sut/err)
    [nil true]              (sut/err true)
    [nil true]              (sut/err (sut/err true))
    [nil [nil true]]        (sut/err [nil true])))

(deftest err-nil-test
  (is (thrown? #?(:clj AssertionError :cljs js/Error) (sut/err nil))))

(deftest result?-test
  (are [x y] (= x (sut/result? y))
    true  (sut/ok)
    true  (sut/err)
    false (vec (sut/ok))
    false (vec (sut/err))))

(deftest ok?-test
  (are [x y] (= x (sut/ok? y))
    true  (sut/ok)
    true  (sut/ok 1)
    true  (sut/ok nil)
    false (sut/err)
    false (sut/err 1)
    false (vec (sut/ok))
    false (vec (sut/err))))

(deftest err?-test
  (are [x y] (= x (sut/err? y))
    true  (sut/err)
    true  (sut/err 1)
    false (sut/ok)
    false (sut/ok 1)
    false (sut/ok nil)
    false (vec (sut/err))
    false (vec (sut/ok))))

(deftest result-test
  (are [x y] (= x y)
    (sut/ok true)   (sut/result true)
    (sut/ok true)   (sut/result true "ERR")
    (sut/err)       (sut/result nil)
    (sut/err "ERR") (sut/result nil "ERR")
    (sut/err "ERR") (sut/result nil (sut/err "ERR"))
    (sut/ok)        (sut/result (sut/ok))
    (sut/err)       (sut/result (sut/err))))

(deftest ok-if-test
  (are [x y] (= x y)
    (sut/ok 1)      (sut/ok-if 1 odd?)
    (sut/ok 1)      (sut/ok-if (sut/ok 1) (constantly true))
    (sut/err)       (sut/ok-if (sut/ok 1) (constantly false))
    (sut/err)       (sut/ok-if 1 even?)
    (sut/err "ERR") (sut/ok-if 1 even? "ERR")
    (sut/err "ERR") (sut/ok-if 1 even? (sut/err "ERR"))))

; (deftest abort-let-success-test
;   (sut/abort-let +err+ [foo 1
;                         bar (inc foo)]
;     (is (= foo 1))
;     (is (= bar 2))
;     (is (nil? +err+)))
;
;   (sut/abort-let +err+ [foo (sut/ok 1)]
;     (is (= foo (sut/ok 1)))
;     (is (nil? +err+)))
;
;   (sut/abort-let +err+ [foo ^:result (sut/ok 1)
;                         bar (inc foo)]
;     (is (= foo 1))
;     (is (= bar 2))
;     (is (nil? +err+))))
;
; (deftest abort-let-failure-test
;   (sut/abort-let +err+ [foo ^:result (sut/err "ERR")]
;     (is (nil? foo))
;     (is (= +err+ "ERR")))
;
;   (sut/abort-let +err+ [foo ^:result (sut/err "ERR")
;                         bar ^:result (sut/ok true)]
;     (is (nil? foo))
;     (is (nil? bar))
;     (is (= +err+ "ERR")))
;
;   (sut/abort-let +err+ [foo ^:result (sut/err "ERR")
;                         bar true]
;     (is (nil? foo))
;     (is (nil? bar))
;     (is (= +err+ "ERR"))))
;
; (deftest result-let-test
;   (are [x y] (= x y)
;     (sut/ok 1)      (sut/result-let [foo 1] foo)
;     (sut/ok 1)      (sut/result-let [foo 1] (sut/ok foo))
;     (sut/err)       (sut/result-let [foo nil] foo)
;     (sut/err "ERR") (sut/result-let [foo ^:result (sut/err "ERR")] foo)
;     (sut/err "ERR") (sut/result-let [foo ^:result (sut/err "ERR") bar 1] bar)
;     (sut/err)       (sut/result-let [foo 1] (sut/err))))

(deftest err-or-ok-test
  (are [x y] (= x y)
    (sut/err "ERR") (sut/err-or-ok "ERR" "OK")
    (sut/err "ERR") (sut/err-or-ok (sut/err "ERR") "OK")
    (sut/ok "OK")   (sut/err-or-ok nil "OK")
    (sut/ok "OK")   (sut/err-or-ok nil (sut/ok "OK"))))

(deftest err-let-test
  (testing "succeeded"
    (sut/err-let +err+ [foo 1
                        bar (inc foo)]
      (is (= foo 1))
      (is (= bar 2))
      (is (nil? +err+)))

    (sut/err-let +err+ [foo (sut/ok 1)
                        bar (inc foo)]
      (is (= foo 1))
      (is (= bar 2))
      (is (nil? +err+))))

  (testing "failure"
    (sut/err-let +err+ [foo (sut/err "ERR")
                        bar (inc foo)]
      (is (nil? foo))
      (is (nil? bar))
      (is (= +err+ "ERR"))))
  )

; (macroexpand-1 '(sut/err-let +err+ [foo (sut/ok 1)] foo))
; (macroexpand-1 '(sut/err-let +err+ [foo ^:result (sut/ok 1)] foo))

;; (deftest err->-test
;;   (letfn [(fail [_ msg] (sut/err msg))]
;;     (are [x y] (= x y)
;;       (sut/ok 2) (sut/err-> 1 inc)
;;       (sut/ok 2) (sut/err-> 5 (- 3))
;;       (sut/err "ERR") (sut/err-> 1 (fail "ERR") inc)
;;       (sut/err "ERR") (sut/err-> (sut/err "ERR") inc))))

;; (deftest err->>-test
;;   (letfn [(fail [msg _] (sut/err msg))]
;;     (are [x y] (= x y)
;;       (sut/ok 2) (sut/err->> 1 inc)
;;       (sut/ok -2) (sut/err->> 5 (- 3))
;;       (sut/err "ERR") (sut/err->> 1 (fail "ERR") inc)
;;       (sut/err "ERR") (sut/err->> (sut/err "ERR") inc))))
