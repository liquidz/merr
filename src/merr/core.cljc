(ns merr.core
  (:refer-clojure :exclude [->
                            ->>
                            let
                            type]))

(def ^:const default-error-type :error)

(defrecord MerrError
  [type message data cause])

(defn error?
  "Returns `true` if x is `MerrError`.

  ```
  => (error? \"foo\")
  false

  => (error? (error {:message \"foo\"}))
  true
  ```"
  [x]
  (instance? MerrError x))

(defn error
  "Returns value as `MerrError`.

  **NOTE** Default error type is `:error`

  ```
  => (:type (error {:message \"hello\"}))
  :error

  => (:type (error {:type :custom-error :message \"hello\"}))
  :custom-error

  => (:data (error {:data {:foo \"bar\"}}))
  {:foo \"bar\"}
  ```"
  ([] (error {}))
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
       (if (error? v#) [nil v#] [v# nil]))))

(defmacro let
  "binding => binding-form init-expr

   If init-expr is not `MerrError`, binding-form bound to the value,
   if not, `err-sym` bound to the `MerrError` value and rest bindings are skipped.

  ```
  => (let +err+ [a 1
  =>             b (inc a)]
  =>   [a b (error? +err+)])
  [1 2 false]

  => (let +err+ [a (error {:message \"ERR\"})
  =>             b (inc a)]
  =>   [a b (error? +err+)])
  [nil nil true]
  ```"
  {:style/indent 2}
  [err-sym bindings & body]
  (assert (vector? bindings) "a vector for its binding")
  (assert (even? (count bindings)) "an even number of forms in binding vector")
  (assert (some #(= err-sym %) (tree-seq coll? seq body))
          (str err-sym " must be handled"))
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
  => (type (error {:type :foo :message \"bar\"}))
  :foo
  ```"
  [e]
  (when (error? e) (:type e)))

(defn message
  "Get error message.

  ```
  => (message (error {:type :foo :message \"bar\"}))
  \"bar\"
  ```"
  [e]
  (when (error? e) (:message e)))

(defn data
  "Get error custom data.

  ```
  => (data (error {:message \"bar\" :data {:hello \"world\"}}))
  {:hello \"world\"}
  ```"
  [e]
  (when (error? e) (:data e)))

(defn cause
  "Get error cause.

  ```
  => (cause (error {:message \"foo\" :cause (error {:message \"bar\"})}))
  (error {:message \"bar\"})
  ```"
  [e]
  (when (error? e) (:cause e)))

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

(defn- ex->err
  [cljs? ex & [m]]
  `(error {:type (or (:type ~m)
                     (:merr/type (ex-data ~ex))
                     default-error-type)
           :message (or (:message ~m)
                        (if ~cljs?
                          (.-message ~ex)
                          (.getMessage ~ex)))
           :data (merge (:data ~m)
                        (ex-data ~ex))
           :cause ~ex}))

#?(:clj (defmacro try
          "Returs `MerrError` when Exceptions/Errors are thrown.

          ```
          => (merr.core/try (throw (ex-info \"hello\" {})))
          error?

          => (type (merr.core/try {:type :test} (throw (ex-info \"hello\" {}))))
          :test

          => (type (merr.core/try (throw (ex-info \"hello\" {:merr/type :test}))))
          :test

          => (data (merr.core/try {:data {:bar 2}} (throw (ex-info \"hello\" {:foo 1}))))
          {:foo 1, :bar 2}
          ```"
          [& body]
          (clojure.core/let [[m & body] (if (and (> (count body) 1)
                                                 (map? (first body)))
                                          body
                                          (cons {} body))
                             ;; cf.
                             ;; https://groups.google.com/g/clojurescript/c/HsWTuhMP7yc
                             ;; https://github.com/tonsky/datascript/blob/eef79f8c21be50a63e77dbe5672a307c6980fb90/src/datascript/arrays.cljc#L22
                             cljs? (some? (:ns &env))
                             ex-class (if cljs? 'js/Error 'Throwable)
                             ex-sym (gensym "ex")]
            `(try
               ~@body
               (catch ~ex-class ~ex-sym
                 ~(ex->err cljs? ex-sym m)))))
   ;; For nbb
   :cljs (defmacro try
           [& body]
           (clojure.core/let [[m & body] (if (and (> (count body) 1)
                                                  (map? (first body)))
                                           body
                                           (cons {} body))
                              ex-sym (gensym "ex")]
             `(try
                ~@body
                (catch js/Error ~ex-sym
                  ~(ex->err true ex-sym m))))))

(def ^:deprecated err?
  "DEPRECATED"
  error?)

(def ^:deprecated err
  "DEPRECATED"
  error)
