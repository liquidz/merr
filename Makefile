.PHONY: repl prepare clean outdated coverage
.PHONY: test test-clj test-cljs

CP=$(shell clojure -A:dev -Spath)

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
	clojure -T:build pom

.PHONY: jar
jar:
	clojure -T:build jar

.PHONY: install
install: clean
	clojure -T:build install

.PHONY: deploy
deploy: clean
	clojure -T:build deploy

coverage:
	clojure -M:coverage:dev --src-ns-path=src --test-ns-path=test --codecov
