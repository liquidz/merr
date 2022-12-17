(ns merr.helper
  (:require
   [merr.core :as core]))

(defn typed-error
  "Returns type specified `merr.core.MerrError`

  ```
  => (def custom-error (partial typed-error ::custom))
  var?

  => (:type (custom-error))
  ::custom

  => (:message (custom-error {:message \"hello\"}))
  \"hello\"
  ```"
  ([error-type]
   (typed-error error-type {}))
  ([error-type {:as m :keys [message data cause]}]
   (core/error (assoc m :type error-type))))

(defn typed-error?
  "Returns `true` if x is type specified `merr.core.MerrError`

  ```
  => (def custom-error? (partial typed-error? ::custom))
  var?

  => (custom-error? (merr.core/error {:type ::custom}))
  true

  => (custom-error? (merr.core/error {:type ::another}))
  false

  => (derive ::derived ::custom)
  nil

  => (custom-error? (merr.core/error {:type ::derived}))
  true
  ```
  "
  [error-type err]
  (boolean
   (some-> (core/type err)
           (isa? error-type))))
