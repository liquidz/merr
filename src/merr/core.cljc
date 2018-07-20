(ns merr.core
  (:refer-clojure :rename {let core-let}))

(def ^:const default-error-type :error)
(defrecord MerrError [type message data cause])

(defn err?
  "Returns true if x is Error.

  => (err? \"foo\")
  false

  => (err? (err {:message \"foo\"}))
  true"
  [x] (instance? MerrError x))

(defn err
  "Returns value as Error.
  NOTE default error type is `:error`

  => (:type (err {:message \"hello\"}))
  :error

  => (:type (err {:type :custom-error :message \"hello\"}))
  :custom-error

  => (:data (err {:data {:foo \"bar\"}}))
  {:foo \"bar\"} "
  ([] (err {}))
  ([{:keys [type message data cause]
     :or {type default-error-type} :as m}]
   (-> m
       (assoc :type type)
       map->MerrError)))

(def ^:private ignore-checkers
  #{number? string? vector? keyword? boolean? map? set?
    #(:merr/ignore (meta %))})

(defn- compare-value [v]
  (if (some #(% v) ignore-checkers)
    [v nil]
    `(core-let [v# ~v]
       (if (err? v#) [nil v#] [v# nil]))))

(defmacro let
  "binding => binding-form init-expr
   If init-expr is not Error, binding-form bound to the value,
   if not, err-sym bound to the Error value and rest bindings are skipped.

  => (let +err+ [a 1
  =>             b (inc a)]
  =>   [a b (err? +err+)])
  [1 2 false]

  => (let +err+ [a (err {:message \"ERR\"})
  =>             b (inc a)]
  =>   [a b (err? +err+)])
  [nil nil true]"
  {:style/indent 2}
  [err-sym bindings & body]
  (assert (vector? bindings) "a vector for its binding")
  (assert (even? (count bindings)) "an even number of forms in binding vector")
  (core-let [bindings (partition 2 bindings)
             [k v] (first bindings)
             first-bind [[k err-sym] (compare-value v)]
             rest-binds (mapcat (fn [[k v]]
                                  [[k err-sym] `(if (nil? ~err-sym)
                                                  ~(compare-value v)
                                                  [nil ~err-sym])])
                                (rest bindings))]
    `(core-let [~@first-bind ~@rest-binds] ~@body)))
