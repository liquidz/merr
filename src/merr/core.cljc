(ns merr.core
  (:refer-clojure :rename {let core-let
                           if-let core-if-let
                           when-let core-when-let}))

(defrecord MerrError [value]
  #?@(:clj [clojure.lang.IDeref (deref [_] value)]
      :cljs [cljs.core/IDeref (-deref [_] value)]))

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

  => (let error [a 1
  =>             b (inc a)]
  =>   [b error])
  [2 nil]

  => (let error [a (err \"ERR\")
  =>             b (inc a)]
  =>   [b (err? error)])
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

(defmacro if-let
  "bindings => binding-form init-expr
   If there is no Error, evaluates `then` with binding-form,
   if not, yields `else`.

  => (if-let error [a 1
  =>                b (inc a)]
  =>   b (err? error))
  2

  => (if-let error [a (err \"ERR\")
  =>                b (inc a)]
  =>   b :ng)
  :ng

  => (if-let error [a (err \"ERR\")
  =>                b (inc a)]
  =>   b)
  err?"
  {:style/indent 2}
  ([err-sym bindings then]
   `(if-let ~err-sym ~bindings ~then ~err-sym))
  ([err-sym bindings then else & oldform]
   (assert (vector? bindings) "a vector for its binding")
   (assert (nil? oldform) "1 or 2 forms after binding vector")
   (assert (even? (count bindings)) "an even number of forms in binding vector")
   `(let ~err-sym ~bindings
      (if ~err-sym ~else ~then))))

(defmacro when-let
  "bindings => binding-form init-expr
   If there is not Error, evaluates body with binding-form,
   if not, returns Error.

  => (when-let [a 1
  =>            b (inc a)]
  =>   b)
  2

  => (when-let [a (err \"ERR\")
  =>            b (inc a)]
  =>   b)
  err?"
  {:style/indent 1}
  [bindings & body]
  (core-let [err-sym (gensym)]
    `(let ~err-sym ~bindings
       (or ~err-sym (do ~@body)))))
