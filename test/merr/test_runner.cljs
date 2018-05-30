(ns merr.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [merr.core-test]))

(doo-tests 'merr.core-test)
