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
(require '[merr.core :as merr])
;; For clojurescript
;; (require '[merr.core :as merr :include-macros true])

(defn gen-odd-num []
  (let [n (rand-int 10)]
    (if (odd? n)
      (merr/ok n)
      (merr/err :even-number))))

(merr/if-let +err+ [n (gen-odd-num)
                    m (inc n)]
  (str "n: " n ", m: " m)
  "Failed to generate odd number")
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
