(ns merr.core)

(def default-value true)
(defn- ok*  [x] ^::result ^::ok  [x nil])
(defn- err* [x] ^::result ^::err [nil x])

(defn result?
  "Returns true if x is a merr/result"
  [x]
  (-> x meta ::result true?))

(defn ok?
  "Returns true if x is Ok result"
  [x]
  (-> x meta ::ok true?))

(defn err?
  "Returns true if x is Error result"
  [x]
  (-> x meta ::err true?))

(defn ^::result ok
  "Returns merr/result value as Ok"
  ([] (ok default-value))
  ([x] (cond-> x (not (ok? x)) ok*)))

(defn ^::result err
  "Returns merr/result value as Error
  NOTE Error value MUST not be nil"
  ([] (err default-value))
  ([x] {:pre [(some? x)]}
   (cond-> x (not (err? x)) err*)))

(defn ^::result result
  ""
  ([x] (result x default-value))
  ([x err-value]
   (cond
     (result? x) x
     (some? x) (ok x)
     :else (err err-value))))

(defn ^::result ok-if
  ([x pred] (ok-if x pred default-value))
  ([x pred err-value]
   (if (pred x)
     (ok x)
     (err err-value))))

(defn ^::result ok-or-err
  ""
  [x y]
  (if x (ok x) (err y)))

(defn ^::result err-or-ok [x y]
  (if x (err x) (ok y)))

#?(:clj
   (defn- merr-fn? [v]
     (when (and (sequential? v)
                (symbol? (first v)))
       (-> v first resolve result?))))

(defn- categorize [v]
  (let [m (meta v)]
    (cond
      (number? v) :prim
      (string? v) :prim
      #?@(:clj [(merr-fn? v) :result])
      (:value m)  :value
      (:result m) :result
      :else       :auto)))

(defmacro err-let
  {:style/indent 2}
  [err-sym bindings & body]
  (let [bindings (->> (partition 2 bindings)
                      (mapcat (fn [[k v]]
                                (case (categorize v)
                                  :prim   [k v]
                                  :value  [k `(when (nil? ~err-sym) ~v)]
                                  :result [[k err-sym] `(if (nil? ~err-sym) ~v [nil ~err-sym])]
                                  [[k err-sym] `(if (nil? ~err-sym)
                                                  (let [v# ~v] (if (result? v#) v# [v# nil]))
                                                  [nil ~err-sym])]))))]
    `(let [~err-sym nil ~@bindings] ~@body)))

(comment

  (macroexpand-1 '(err-let +err+ [x 1] x))
  (macroexpand-1 '(err-let +err+ [x "foo"] x))
  (macroexpand-1 '(err-let +err+ [x (ok 1)] x))
  (macroexpand-1 '(err-let +err+ [x (err 1)] x))
  (macroexpand-1 '(err-let +err+ [x (do-something 1)] x))
  (macroexpand-1 '(err-let +err+ [x ^:result (do-something 1)] x))
  (macroexpand-1 '(err-let +err+ [x ^:value (do-something 1)] x))

  )
