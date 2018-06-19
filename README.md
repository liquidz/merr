# merr
[![CircleCI](https://circleci.com/gh/liquidz/merr.svg?style=svg)](https://circleci.com/gh/liquidz/merr)

Minimal error handling library for Clojure/ClojureScript

This library is based on ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/).

**THIS PROJECT IS WORK IN PROGRESS**

## Concept

* Easy to imagine behavior
* Minimum to remember

## Usage

* Clojars
  * TODO

### let

```clj
(require '[merr.core :as m])
;; For clojurescript
;; (require '[merr.core :as m :include-macros true])


(defn may-fail-inc [n]
  (if (odd? (rand-int 10))
    (inc n)
    (m/err (str "failed to inc: " n))))

(m/if-let err [a 10
               b (may-fail-inc a)
               c (may-fail-inc b)]
  (str "c = " c)
  @err)
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
