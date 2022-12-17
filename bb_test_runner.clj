(ns bb-test-runner
  (:require
   [clojure.test :as t]
   [merr.core-test]
   [merr.helper-test]))

(t/run-tests 'merr.core-test)
(t/run-tests 'merr.helper-test)
