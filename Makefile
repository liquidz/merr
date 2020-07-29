.PHONY: repl prepare clean outdated coverage
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
	rm -rf node_modules

lint:
	clj-kondo --lint src:test
	cljstyle check

outdated:
	lein with-profile +antq run -m antq.core

coverage:
	lein cloverage --codecov
