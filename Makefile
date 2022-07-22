CP=$(shell clojure -A:dev -Spath)

.PHONY: repl
repl:
	iced repl -A:dev --with-kaocha --force-clojure-cli

.PHONY: repl-cljs
repl-cljs:
	@npx nbb --classpath "$(shell clojure -A:dev -Spath)" nrepl-server

.PHONY: inspect
inspect:
	node --inspect target/js/compiled/index.js

.PHONY: test
test: test-clj test-bb test-cljs test-nbb

.PHONY: test-clj
test-clj:
	clojure -M:dev:1.9:test-clj
	clojure -M:dev:1.10:test-clj
	clojure -M:dev:test-clj

.PHONY: test-bb
test-bb:
	@bb --classpath "$(CP)" bb_test_runner.clj

.PHONY: test-cljs
test-cljs:
	@clojure -M:dev:test-cljs

.PHONY: test-nbb
test-nbb:
	@npx nbb --classpath "$(CP)" bb_test_runner.clj

.PHONY: clean
clean:
	rm -rf node_modules target .cpcache .clj-kondo/.cache

.PHONY: lint
lint:
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
