(ns hooks.merr
  (:require
   [clj-kondo.hooks-api :as api]))

(defn merr-let
  [{:keys [:node]}]
  (let [[err-sym bindings & body] (rest (:children node))
        bindings (api/vector-node (concat [err-sym (api/token-node nil)]
                                          (:children bindings)))
        new-node (api/list-node
                  (list*
                   (api/token-node 'let)
                   bindings
                   body))]
    {:node new-node}))
