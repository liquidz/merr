# merr
[![CircleCI](https://circleci.com/gh/liquidz/merr.svg?style=svg)](https://circleci.com/gh/liquidz/merr)

Minimal and good enough error handling library for Clojure/ClojureScript

This library is based on ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/).

**THIS PROJECT IS WORK IN PROGRESS**

## Concept

* Easy to imagine behavior
* Minimum to remember

## Usage
[![Clojars Project](https://img.shields.io/clojars/v/merr.svg)](https://clojars.org/merr)

[API Documents](https://cljdoc.xyz/d/merr/merr/0.1.0-SNAPSHOT/api/merr.core)

### example

```clj
(require '[merr.core :as merr])
;; For clojurescript
;; (require '[merr.core :as merr :include-macros true])


(defn may-fail-inc [n]
  (if (odd? (rand-int 10))
    (inc n)
    (merr/err (str "failed to inc: " n))))

(merr/if-let err [a 10
                  b (may-fail-inc a)
                  c (may-fail-inc b)]
  (str "c = " c)
  @err)
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
