# merr
[![CircleCI](https://circleci.com/gh/liquidz/merr.svg?style=svg)](https://circleci.com/gh/liquidz/merr)

Minimal and good enough error handling library for Clojure/ClojureScript

This library is based on ["Good Enough" error handling in Clojure](https://adambard.com/blog/acceptable-error-handling-in-clojure/).

## Concept

* Easy to imagine behavior
* Minimum to remember

```clj
(merr/let +err+ [foo {:some "data"}
                 bar (may-fail!!! foo)
                 baz (do-something bar)]
  (if +err+ "NG" "OK"))
```

## Usage
[![Clojars Project](https://img.shields.io/clojars/v/merr.svg)](https://clojars.org/merr)
[![cljdoc badge](https://cljdoc.xyz/badge/merr/merr)](https://cljdoc.xyz/d/merr/merr/CURRENT)


```clj
;; for Clojure
(require '[merr.core :as merr])

;; for ClojureScript
(require '[merr.core :as merr :include-macros true])
```

### error record

```clj
(defrecord MerrError [type message data cause])
```

### example

```clj
(defn may-fail-inc [n]
  (if (odd? (rand-int 10))
    (inc n)
    (merr/err {:message (str "failed to inc: " n)})))

(merr/let err [a 10
               b (may-fail-inc a)
               c (may-fail-inc b)]
  (if err
    (merr/message err)
    (str "c = " c)))
```

## License

Copyright © 2018 [Masashi Iizuka](https://twitter.com/uochan)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
