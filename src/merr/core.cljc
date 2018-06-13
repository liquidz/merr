(ns merr.core
  (:refer-clojure :rename {let core-let}))

(def ^:private default-value true)
(defn- ok*  [x] ^:merr/result ^:merr/ok  [x nil])
(defn- err* [x] ^:merr/result ^:merr/err [nil x])

(defn result?
  "Returns true if x is a merr/result"
  [x]
  (-> x meta :merr/result true?))

(defn ok?
  "Returns true if x is Ok result"
  [x]
  (-> x meta :merr/ok true?))

(defn err?
  "Returns true if x is Error result"
  [x]
  (-> x meta :merr/err true?))

(defn ^:merr/result ok
  "Returns merr/result value as Ok"
  ([] (ok default-value))
  ([x] (cond-> x (not (ok? x)) ok*)))

(defn ^:merr/result err
  "Returns merr/result value as Error
  NOTE Error value MUST not be nil"
  ([] (err default-value))
  ([x] {:pre [(some? x)]}
   (cond-> x (not (err? x)) err*)))

;; (defn ^:merr/result result
;;   ""
;;   ([x] (result x default-value))
;;   ([x err-value]
;;    (cond
;;      (result? x) x
;;      (some? x) (ok x)
;;      :else (err err-value))))

;; (defn ^:merr/result ok-if
;;   ([x pred] (ok-if x pred default-value))
;;   ([x pred err-value]
;;    (if (pred x)
;;      (ok x)
;;      (err err-value))))

(defn ^:merr/result ok-or-err
  ""
  [ok-val err-val]
  (if ok-val (ok ok-val) (err err-val)))

(defn ^:merr/result err-or-ok
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
  "FIXME"
  {:style/indent 2}
  [err-sym bindings & body]
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
