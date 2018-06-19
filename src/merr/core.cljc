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
  "Returns true if x is Error result."
  [x] (instance? MerrError x))

(defn err
  "Returns merr/result value as Error."
  ([] (err default-value))
  ([x] (cond-> x (not (err? x)) err*)))

(defn- compare-value [k v]
  `(core-let [v# ~v]
     (if (err? v#) [nil v#] [v# nil])))

(defmacro let
  "binding => binding-form init-expr
   If init-expr is merr's Ok, binding-form bound to the Ok value,
   if not, err-sym bound to the Err value and rest bindings are skipped."
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
   If there is no merr's Err, evaluates then with binding-form,
   if not, yields else"
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
  "FIXME"
  {:style/indent 1}
  [bindings & body]
  (core-let [err-sym (gensym)]
    `(let ~err-sym ~bindings
       (or ~err-sym (do ~@body)))))
