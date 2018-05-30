# merr

minimal error handling library for clojure

**THIS PROJECT IS WORK IN PROGRESS**

## Philosophy

* explicit
* no magic
* no monad

## Usage

* Clojars
  * TODO

### err-let

```clj
(require '[merr.core :as merr])

(defn gen-odd-num []
  (let [n (rand-int 10)]
    (if (odd? n)
      (merr/ok n)
      (merr/err))))

(merr/err-let +err+ [n ^:merr (gen-odd-num)
                     m (inc n)]
  (if +err+
    "Failed to generate odd number"
    (str "n: " n ", m: " m)))
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
