(ns merr.core-test
  (:require #?@(:clj  [[clojure.test :refer :all]
                       [merr.core :as sut]]
                :cljs [[cljs.test :refer-macros [deftest is are testing]]
                       [merr.core :as sut :include-macros true]])))

(deftest ok-test
  (are [x y] (= x y)
    [true nil]       (sut/ok)
    [true nil]       (sut/ok true)
    [nil nil]        (sut/ok nil)
    [true nil]       (sut/ok (sut/ok true))
    [[true nil] nil] (sut/ok [true nil])))

(deftest err-test
  (are [x y] (= x y)
    [nil true]       (sut/err)
    [nil true]       (sut/err true)
    [nil true]       (sut/err (sut/err true))
    [nil [nil true]] (sut/err [nil true])))

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

;; (deftest result-test
;;   (are [x y] (= x y)
;;     (sut/ok true)   (sut/result true)
;;     (sut/ok true)   (sut/result true "ERR")
;;     (sut/err)       (sut/result nil)
;;     (sut/err "ERR") (sut/result nil "ERR")
;;     (sut/err "ERR") (sut/result nil (sut/err "ERR"))
;;     (sut/ok)        (sut/result (sut/ok))
;;     (sut/err)       (sut/result (sut/err))))

;; (deftest ok-if-test
;;   (are [x y] (= x y)
;;     (sut/ok 1)      (sut/ok-if 1 odd?)
;;     (sut/ok 1)      (sut/ok-if (sut/ok 1) (constantly true))
;;     (sut/err)       (sut/ok-if (sut/ok 1) (constantly false))
;;     (sut/err)       (sut/ok-if 1 even?)
;;     (sut/err "ERR") (sut/ok-if 1 even? "ERR")
;;     (sut/err "ERR") (sut/ok-if 1 even? (sut/err "ERR"))))

(deftest ok-or-err-test
  (are [x y] (= x y)
    (sut/ok "OK")   (sut/ok-or-err "OK" "ERR")
    (sut/ok "OK")   (sut/ok-or-err (sut/ok "OK") "ERR")
    (sut/err "ERR") (sut/ok-or-err nil "ERR")
    (sut/err "ERR") (sut/ok-or-err nil (sut/err "ERR"))))

(deftest err-or-ok-test
  (are [x y] (= x y)
    (sut/err "ERR") (sut/err-or-ok "ERR" "OK")
    (sut/err "ERR") (sut/err-or-ok (sut/err "ERR") "OK")
    (sut/ok "OK")   (sut/err-or-ok nil "OK")
    (sut/ok "OK")   (sut/err-or-ok nil (sut/ok "OK"))))

(deftest let-test
  (testing "succeeded"
    (sut/let +err+ [foo (sut/ok 1)
                    bar (inc foo)]
      (is (= foo 1))
      (is (= bar 2))
      (is (nil? +err+))))

  (testing "failed"
    (sut/let +err+ [foo (sut/err "ERR")
                    bar (inc foo)]
      (is (nil? foo))
      (is (nil? bar))
      (is (= +err+ "ERR"))))

  (testing "clojure.core/let"
    (let [foo (sut/ok 1)]
      (is (= foo [1 nil])))))

(comment

  (macroexpand-1 '(sut/let +err+ [x 1] x))
  (macroexpand-1 '(sut/let +err+ [x "foo"] x))
  (macroexpand-1 '(sut/let +err+ [x (sut/ok 1)] x))
  (macroexpand-1 '(sut/let +err+ [x (sut/err 1)] x))
  (macroexpand-1 '(sut/let +err+ [x (do-something 1)] x))
  (macroexpand-1 '(sut/let +err+ [x ^:result (do-something 1)] x))
  (macroexpand-1 '(sut/let +err+ [x ^:value (do-something 1)] x))

  )
