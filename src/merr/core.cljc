(ns merr.core
  (:refer-clojure :rename {let core-let}))

(defrecord MerrError [__value__]
  #?@(:clj [clojure.lang.IDeref (deref [_] __value__)]
      :cljs [cljs.core/IDeref (-deref [_] __value__)]))

#?(:clj
   (defmethod print-method MerrError
     [x writer]
     (print-method (conj {} x) writer)))

(def ^:private ^:const default-value true)
(defn- err* [x] (->MerrError x))

(defn err?
  "Returns true if x is Error.

  => (err? \"foo\")
  false

  => (err? (err \"foo\"))
  true"
  [x] (instance? MerrError x))

(defn err
  "Returns value as Error.

  => (type (err \"foo\"))
  merr.core.MerrError

  => @(err \"foo\")
  \"foo\""
  ([] (err default-value))
  ([x] (cond-> x (not (err? x)) err*)))

(defn- compare-value [k v]
  `(core-let [v# ~v]
     (if (err? v#) [nil v#] [v# nil])))

(defmacro let
  "binding => binding-form init-expr
   If init-expr is not Error, binding-form bound to the value,
   if not, err-sym bound to the Error value and rest bindings are skipped.

  => (let +err+ [a 1
  =>             b (inc a)]
  =>   [b +err+])
  [2 nil]

  => (let +err+ [a (err \"ERR\")
  =>             b (inc a)]
  =>   [b (err? +err+)])
  [nil true]"
  {:style/indent 2}
  [err-sym bindings & body]
  (assert (vector? bindings) "a vector for its binding")
  (assert (even? (count bindings)) "an even number of forms in binding vector")
  (core-let [bindings (partition 2 bindings)
             [k v] (first bindings)
             first-bind [[k err-sym] (compare-value k v)]
             rest-binds (mapcat (fn [[k v]]
                                  [[k err-sym] `(if (nil? ~err-sym)
                                                  ~(compare-value k v)
                                                  [nil ~err-sym])])
                                (rest bindings))]
    `(core-let [~@first-bind ~@rest-binds] ~@body)))
