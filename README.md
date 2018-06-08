# merr

Minimal error handling library for Clojure/ClojureScript
This library is based on ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/).

**THIS PROJECT IS WORK IN PROGRESS**

## Concept

* Easy to imagine behavior
* Minimum to remember

## Usage

* Clojars
  * TODO

### err-let

```clj
(require '[merr.core :as m])

(defn gen-odd-num []
  (let [n (rand-int 10)]
    (if (odd? n)
      (m/ok n)
      (m/err :even-number))))

(m/err-let +err+ [n (gen-odd-num)
                  m (gen-odd-num)
                  x (+ n m)]
  (if +err+
    "Failed to generate odd number"
    (str "n: " n ", m: " m ", x: " x)))
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
