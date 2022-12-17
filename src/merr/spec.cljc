(ns merr.spec
  (:require
   [clojure.core.specs.alpha :as c.s]
   [clojure.spec.alpha :as s]
   [merr.core :as core]))

(s/def ::merr-error (partial instance? merr.core.MerrError))
(s/def ::type some?)
(s/def ::message (s/or :string string? :none nil?))
(s/def ::data any?)
(s/def ::cause any?)
(s/def ::error-map (s/keys :opt-un [::type ::message ::data ::cause]))

(s/fdef core/error?
  :args (s/cat :x any?)
  :ret boolean?)

(s/fdef core/error
  :args (s/cat :m (s/? ::error-map))
  :ret ::merr-error)

(s/fdef core/let
  :args (s/cat :err-sym simple-symbol?
               :bindings ::c.s/bindings
               :body (s/* any?))
  :ret any?)

(s/fdef core/assert
  :args (s/cat :pred any?
               :m map?)
  :ret (s/or :err ::merr-error
             :none nil?))

(s/fdef core/type :args (s/cat :e any?))
(s/fdef core/message :args (s/cat :e any?))
(s/fdef core/data :args (s/cat :e any?))
(s/fdef core/cause :args (s/cat :e any?))
