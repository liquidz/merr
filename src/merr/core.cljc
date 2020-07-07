(ns merr.core
  (:refer-clojure :exclude [->
                            ->>
                            let
                            type]))

(def ^:const default-error-type :error)

(defrecord MerrError
  [type message data cause])

(defn err?
  "Returns `true` if x is `MerrError`.

  ```
  => (err? \"foo\")
  false

  => (err? (err {:message \"foo\"}))
  true
  ```"
  [x]
  (instance? MerrError x))

(defn err
  "Returns value as `MerrError`.

  **NOTE** Default error type is `:error`

  ```
  => (:type (err {:message \"hello\"}))
  :error

  => (:type (err {:type :custom-error :message \"hello\"}))
  :custom-error

  => (:data (err {:data {:foo \"bar\"}}))
  {:foo \"bar\"}
  ```"
  ([] (err {}))
  ([{:keys [type message data cause]
     :or {type default-error-type} :as m}]
   (clojure.core/-> m
                    (assoc :type type)
                    map->MerrError)))

(def ^:private ignore-checkers
  #{number? string? vector? keyword? boolean? map? set?
    #(:merr/ignore (meta %))})

(defn- compare-value
  [v]
  (if (some #(% v) ignore-checkers)
    [v nil]
    `(clojure.core/let [v# ~v]
       (if (err? v#) [nil v#] [v# nil]))))

(defmacro let
  "binding => binding-form init-expr

   If init-expr is not `MerrError`, binding-form bound to the value,
   if not, `err-sym` bound to the `MerrError` value and rest bindings are skipped.

  ```
  => (let +err+ [a 1
  =>             b (inc a)]
  =>   [a b (err? +err+)])
  [1 2 false]

  => (let +err+ [a (err {:message \"ERR\"})
  =>             b (inc a)]
  =>   [a b (err? +err+)])
  [nil nil true]
  ```"
  {:style/indent 2}
  [err-sym bindings & body]
  (assert (vector? bindings) "a vector for its binding")
  (assert (even? (count bindings)) "an even number of forms in binding vector")
  (clojure.core/let [bindings (partition 2 bindings)
                     [k v] (first bindings)
                     first-bind [[k err-sym] (compare-value v)]
                     rest-binds (mapcat (fn [[k v]]
                                          [[k err-sym] `(if (nil? ~err-sym)
                                                          ~(compare-value v)
                                                          [nil ~err-sym])])
                                        (rest bindings))]
    `(clojure.core/let [~@first-bind ~@rest-binds]
       ~@body)))

(defn type
  "Get error type.

  ```
  => (type (err {:type :foo :message \"bar\"}))
  :foo
  ```"
  [e]
  (when (err? e) (:type e)))

(defn message
  "Get error message.

  ```
  => (message (err {:type :foo :message \"bar\"}))
  \"bar\"
  ```"
  [e]
  (when (err? e) (:message e)))

(defn data
  "Get error custom data.

  ```
  => (data (err {:message \"bar\" :data {:hello \"world\"}}))
  {:hello \"world\"}
  ```"
  [e]
  (when (err? e) (:data e)))

(defn cause
  "Get error cause.

  ```
  => (cause (err {:message \"foo\" :cause (err {:message \"bar\"})}))
  (err {:message \"bar\"})
  ```"
  [e]
  (when (err? e) (:cause e)))

(defmacro ->
  "Threads the expr through the forms. Inserts x as the
  second item in the first form, making a list of it if it is not a
  list already. If there are more forms, inserts the first form as the
  second item in second form, etc.
  Return `MerrError` on the spot when there is on the way.

  ```
  => (-> 1 inc (- 1))
  1
  => (letfn [(failinc [_] (err))]
  =>   (-> 1 inc failinc inc))
  (err)
  ```"
  [x & forms]
  (clojure.core/let [sym (gensym)
                     bindings (map (fn [form]
                                     (if (seq? form)
                                       (with-meta `(~(first form) ~sym ~@(next form)) (meta form))
                                       (list form sym)))
                                   forms)
                     bindings (cons x bindings)]
    `(let err# [~@(interleave (repeat sym) bindings)]
          (or err# ~sym))))

(defmacro ->>
  "Threads the expr through the forms. Inserts x as the
  second item in the first form, making a list of it if it is not a
  list already. If there are more forms, inserts the first form as the
  second item in second form, etc.
  Return `MerrError` on the spot when there is on the way.

  ```
  => (->> 1 inc (- 1))
  -1
  => (letfn [(failinc [_] (err))]
  =>   (->> 1 inc failinc inc))
  (err)
  ```"
  [x & forms]
  (clojure.core/let [sym (gensym)
                     bindings (map (fn [form]
                                     (if (seq? form)
                                       (with-meta `(~@form ~sym) (meta form))
                                       (list form sym)))
                                   forms)
                     bindings (cons x bindings)]
    `(let err# [~@(interleave (repeat sym) bindings)]
          (or err# ~sym))))
