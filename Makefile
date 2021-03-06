.PHONY: repl prepare clean outdated coverage
.PHONY: test test-clj test-cljs

CP=$(shell clojure -A:dev -Spath)
ARTIFACT=target/merr.jar

pom.xml:
	clj -Spom

repl:
	iced repl -A:dev --with-kaocha --force-clojure-cli
repl-cljs:
	iced repl --force-shadow-cljs app

inspect:
	node --inspect target/js/compiled/index.js

node_modules/ws:
	npm install
prepare: node_modules/ws

test: test-clj test-cljs

test-clj:
	clojure -M:dev:1.9:test --focus :unit-clj
	clojure -M:dev:test --focus :unit-clj

test-cljs: prepare
	clojure -M:dev:test --focus :unit-cljs

clean:
	rm -rf node_modules target .cpcache .clj-kondo/.cache

lint:
	@clj-kondo --no-warnings --lint "$(CP)"
	clj-kondo --lint src:test
	cljstyle check

outdated:
	clojure -M:outdated --upgrade

pom:
	clojure -Spom

$(ARTIFACT): pom
	clojure -X:depstar jar :jar $@
jar: clean $(ARTIFACT)

install: clean $(ARTIFACT)
	clojure -X:deploy :installer :local :artifact $(ARTIFACT)

deploy: clean $(ARTIFACT)
	echo "Testing if CLOJARS_USERNAME environmental variable exists."
	test $(CLOJARS_USERNAME)
	clojure -X:deploy :installer :remote :artifact $(ARTIFACT)

coverage:
	clojure -M:coverage:dev --src-ns-path=src --test-ns-path=test --codecov
