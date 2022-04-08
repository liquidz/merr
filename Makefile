CP=$(shell clojure -A:dev -Spath)

.PHONY: repl
repl:
	iced repl -A:dev --with-kaocha --force-clojure-cli
.PHONY: repl-cljs
repl-cljs:
	iced repl --force-shadow-cljs app

.PHONY: inspect
inspect:
	node --inspect target/js/compiled/index.js

node_modules/ws:
	npm install
.PHONY: prepare
prepare: node_modules/ws

.PHONY: test
test: test-clj test-cljs

.PHONY: test-clj
test-clj:
	clojure -M:dev:1.9:test --focus :unit-clj
	clojure -M:dev:1.10:test --focus :unit-clj
	clojure -M:dev:test --focus :unit-clj

.PHONY: test-cljs
test-cljs: prepare
	clojure -M:dev:test --focus :unit-cljs

.PHONY: clean
clean:
	rm -rf node_modules target .cpcache .clj-kondo/.cache

.PHONY: lint
lint:
	@clj-kondo --no-warnings --lint "$(CP)"
	clj-kondo --lint src:test
	cljstyle check

.PHONY: outdated
outdated:
	clojure -M:outdated --upgrade

.PHONY: pom
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

.PHONY: coverage
coverage:
	clojure -M:coverage:dev --src-ns-path=src --test-ns-path=test --codecov
