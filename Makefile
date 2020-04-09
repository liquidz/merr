.PHONY: repl prepare clean ancient coverage
.PHONY: test test-clj test-cljs

VERSION := 1.10.1
POM_FILE=pom.xml

$(POM_FILE):
	clj -Spom

repl:
	iced repl --with-kaocha

node_modules/ws:
	npm install
prepare: node_modules/ws

test: test-clj test-cljs

test-clj:
	lein test-clj

test-cljs: prepare
	lein test-cljs

clean:
	lein clean

lint:
	clj-kondo --lint src:test

ancient:
	clojure -A:ancient

coverage:
	lein cloverage --codecov
