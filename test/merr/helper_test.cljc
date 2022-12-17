(ns merr.helper-test
  (:require
   #?@(:clj  [[clojure.test :as t]
              [testdoc.core]]
       :cljs [[cljs.test :as t :include-macros true]])
   [merr.core :as core]
   [merr.helper :as sut]))

(def test-error (partial sut/typed-error ::test-error))
(def test-error? (partial sut/typed-error? ::test-error))

(t/deftest test-error-test
  (t/is (core/error? (test-error)))
  (t/is (= ::test-error (core/type (test-error))))
  (t/is (= "foo" (core/message (test-error {:message "foo"}))))

  (t/is (test-error? (test-error)))
  (t/is (not (test-error? (core/error))))

  (derive ::derived-error ::test-error)
  (t/is (test-error? (core/error {:type ::derived-error})))
  (t/is (not (test-error? (core/error {:type ::non-derived-error})))))

#?(:bb
   nil
   :clj
   (t/deftest docstring-test
     (t/is (testdoc #'sut/typed-error))
     (t/is (testdoc #'sut/typed-error?))))
