(ns merr.core
  (:refer-clojure :rename {let core-let
                           if-let core-if-let}))

(def ^:private default-value true)
(defn- ok*  [x] ^:merr/result ^:merr/ok  [x nil])
(defn- err* [x] ^:merr/result ^:merr/err [nil x])

(defn result?
  "Returns true if x is a merr/result."
  [x]
  (-> x meta :merr/result true?))

(defn ok?
  "Returns true if x is Ok result."
  [x]
  (-> x meta :merr/ok true?))

(defn err?
  "Returns true if x is Error result."
  [x]
  (-> x meta :merr/err true?))

(defn ^:merr/result ok
  "Returns merr/result value as Ok."
  ([] (ok default-value))
  ([x] (cond-> x (not (ok? x)) ok*)))

(defn ^:merr/result err
  "Returns merr/result value as Error.
  **NOTE Error value MUST not be nil**"
  ([] (err default-value))
  ([x] {:pre [(some? x)]}
   (cond-> x (not (err? x)) err*)))

(defn ^:merr/result ok-or-err
  "Returns Ok value if ok-val is not nil.
  Otherwise returns Err value with err-val."
  [ok-val err-val]
  (if ok-val (ok ok-val) (err err-val)))

(defn ^:merr/result err-or-ok
  "Returns Err value if err-value is not nil.
  Otherwise return Ok value with ok-val."
  [err-val ok-val]
  (if err-val (err err-val) (ok ok-val)))

#?(:clj
   (defn- merr-fn? [v]
     (when (and (sequential? v)
                (symbol? (first v)))
       (-> v first resolve result?))))

(defn- categorize [v]
  (core-let [m (meta v)]
    (cond
      (number? v) :prim
      (string? v) :prim
      #?@(:clj [(merr-fn? v) :result])
      (:value m)  :value
      (:result m) :result
      :else       :auto)))

(defmacro let
  "binding => binding-form init-expr
   If init-expr is merr's Ok, binding-form bound to the Ok value,
   if not, err-sym bound to the Err value and rest bindings are skipped."
  {:style/indent 2}
  [err-sym bindings & body]
  (assert (vector? bindings) "a vector for its binding")
  (assert (even? (count bindings)) "exactly 2 forms in binding vector")
  (core-let [bindings (->> (partition 2 bindings)
                           (mapcat (fn [[k v]]
                                     (case (categorize v)
                                       :prim   [k v]
                                       :value  [k `(when (nil? ~err-sym) ~v)]
                                       :result [[k err-sym] `(if (nil? ~err-sym) ~v [nil ~err-sym])]
                                       [[k err-sym] `(if (nil? ~err-sym)
                                                       (core-let [v# ~v] (if (result? v#) v# [v# nil]))
                                                       [nil ~err-sym])]))))]
    `(core-let [~err-sym nil ~@bindings] ~@body)))

(defmacro if-let
  "bindings => binding-form init-expr
   If there is no merr's Err, evaluates then with binding-form,
   if not, yields else"
  {:style/indent 2}
  ([err-sym bindings then]
   `(if-let ~err-sym ~bindings ~then nil))
  ([err-sym bindings then else & oldform]
   (assert (vector? bindings) "a vector for its binding")
   (assert (nil? oldform) "1 or 2 forms after binding vector")
   (assert (even? (count bindings)) "an even number of forms in binding vector")
   `(let ~err-sym ~bindings
      (if ~err-sym ~else ~then))))
