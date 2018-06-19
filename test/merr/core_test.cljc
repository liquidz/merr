(ns merr.core-test
  (:require #?@(:clj  [[clojure.test :refer :all]
                       [merr.core :as sut]]
                :cljs [[cljs.test :refer-macros [deftest is are testing]]
                       [merr.core :as sut :include-macros true]])))

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
                    bar (inc foo)]
      (is (= foo 1))
      (is (= bar 2))
      (is (nil? +err+))))

  (testing "failed"
    (sut/let +err+ [foo (sut/err "ERR")
                    bar (inc foo)]
      (is (nil? foo))
      (is (nil? bar))
      (is (= @+err+ "ERR"))))

  (testing "clojure.core/let"
    (let [foo (sut/err 1)]
      (is (= foo (sut/->MerrError 1))))))

(deftest if-let-test
  (are [x y] (= x y)
    2               (sut/if-let err [foo 1 bar (inc foo)] bar)
    2               (sut/if-let err [foo 1 bar (inc foo)] bar err)
    (sut/err "ERR") (sut/if-let err [foo (sut/err "ERR") bar (inc foo)] bar)
    (sut/err "ERR") (sut/if-let err [foo (sut/err "ERR") bar (inc foo)] bar err)
    nil             (sut/if-let err [foo (sut/err "ERR") bar (inc foo)] bar nil)))

(deftest when-let-test
  (are [x y] (= x y)
    2               (sut/when-let [foo 1 bar (inc foo)] bar)
    (sut/err "ERR") (sut/when-let [foo (sut/err "ERR") bar (inc foo)] bar)))

(comment

  (macroexpand-1 '(sut/let +err+ [x 1] x))
  (macroexpand-1 '(sut/let +err+ [x "foo"] x))
  (macroexpand-1 '(sut/let +err+ [x (sut/ok 1)] x))
  (macroexpand-1 '(sut/let +err+ [x (sut/err 1)] x))
  (macroexpand-1 '(sut/let +err+ [x (do-something 1)] x))
  (macroexpand-1 '(sut/let +err+ [x ^:result (do-something 1)] x))
  (macroexpand-1 '(sut/let +err+ [x ^:value (do-something 1)] x))

  )
