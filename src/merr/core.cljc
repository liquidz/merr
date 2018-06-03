(ns merr.core)

(def default-value true)
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

(defn ok
  "Returns merr/result value as Ok"
  ([] (ok default-value))
  ([x] (cond-> x (not (ok? x)) ok*)))

(defn err
  "Returns merr/result value as Error
  NOTES: Error value MUST not be nil"
  ([] (err default-value))
  ([x] {:pre [(some? x)]}
   (cond-> x (not (err? x)) err*)))

(defn result
  ""
  ([x] (result x default-value))
  ([x err-value]
   (cond
     (result? x) x
     (some? x) (ok x)
     :else (err err-value))))

(defn ok-if
  ""
  ([x pred] (ok-if x pred default-value))
  ([x pred err-value]
   (if (pred x)
     (ok x)
     (err err-value))))

(defn ok-or-err [x y]
  (if x (ok x) (err y)))

(defn err-or-ok [x y]
  (if x (err x) (ok y)))

;; TODO: abort-let と result-let の名前でもっと良いのがあれば変更したい


(defmacro err-let
  {:style/indent 2}
  [err-sym bindings & body]
  (let [bindings (->> (partition 2 bindings)
                      (mapcat (fn [[k v]]
                                [[k err-sym]
                                 `(if (nil? ~err-sym)
                                    (let [x# ~v]
                                      (if (result? x#)
                                        x# [x# ~err-sym]))
                                    [nil ~err-sym]) ])))]
    `(let [~err-sym nil ~@bindings] ~@body)))

(defmacro abort-let
  {:style/indent 2}
  [err-sym bindings & body]
  (let [bindings (->> (partition 2 bindings)
                      (mapcat (fn [[k v]]
                                (if (:result (meta v))
                                  [[k err-sym]
                                   `(if (nil? ~err-sym) ~v [nil ~err-sym])]
                                  [k
                                   `(when (nil? ~err-sym) ~v)]))))]
    `(let [~err-sym nil ~@bindings] ~@body)))

(defmacro result-let
  {:style/indent 1}
  [bindings & body]
  `(abort-let err# ~bindings
     (if err# (err err#) (result (do ~@body)))))


;; (defmacro ->ok? [v form]
;;   `(if (ok? ~v) (-> ~v unwrap ~form wrap) ~v))

;; (defmacro ->>ok? [form v]
;;   `(if (ok? ~v) (->> ~v unwrap ~form wrap) ~v))

;; (defmacro err-> [x & forms]
;;   (let [forms (for [form forms]
;;                 `(->ok? ~form))]
;;     `(-> (wrap ~x) ~@forms)))

;; (defmacro err->> [x & forms]
;;   (let [forms (for [form forms]
;;                 `(->>ok? ~form))]
;;     `(->> (wrap ~x) ~@forms)))
