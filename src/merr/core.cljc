(ns merr.core)

(defn ok
  ([] (ok {}))
  ([x] ^:merr/result [x nil]))

(defn err
  ([] (err {}))
  ([x] ^:merr/result [nil x]))

(defn result? [x]
  (-> x meta :merr/result))

(defn ok? [x]
  (and (result? x) (nil? (second x))))

(defn err? [x]
  (and (result? x) (some? (second x))))

(defn wrap
  ([x] (wrap x {}))
  ([x err-value]
   (cond
     (result? x) x
     x (ok x)
     :else (err err-value))))

(def unwrap first)

(defmacro err-let
  {:style/indent 2}
  [err-sym bindings & body]
  (let [bindings (->> (partition 2 bindings)
                      (mapcat (fn [[k v]]
                                (if (:merr (meta v))
                                  [[k err-sym]
                                   `(if (nil? ~err-sym) ~v [nil ~err-sym])]
                                  [k
                                   `(when (nil? ~err-sym) ~v)]))))]
    `(let [~err-sym nil ~@bindings] ~@body)))

(defmacro ->ok? [v form]
  `(if (ok? ~v) (-> ~v unwrap ~form wrap) ~v))

(defmacro ->>ok? [form v]
  `(if (ok? ~v) (->> ~v unwrap ~form wrap) ~v))

(defmacro err-> [x & forms]
  (let [forms (for [form forms]
                `(->ok? ~form))]
    `(-> (wrap ~x) ~@forms)))

(defmacro err->> [x & forms]
  (let [forms (for [form forms]
                `(->>ok? ~form))]
    `(->> (wrap ~x) ~@forms)))
