# merr

Minimal error handling library for Clojure/ClojureScript
This library is based on ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/).

**THIS PROJECT IS WORK IN PROGRESS**

## Philosophy

* explicit
* no magic

## Usage

* Clojars
  * TODO

### err-let

```clj
(require '[merr.core :as merr])

(defn gen-odd-num []
  (merr/ok-if (rand-int 10) odd?))

(defn sum-odd-num []
  (merr/err-let +err+ [x (gen-odd-num)
                       y (gen-odd-num)
                       z (+ x y)]
    (merr/err-or-ok +err+ z)))

(merr/err-let +err+ [n (sum-odd-num)
                     m (inc n)]
  (if +err+
    "Failed to generate odd number"
    (str "n: " n ", m: " m)))
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
