CP=$(shell clojure -A:dev -Spath)

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.PHONY: repl-cljs
repl-cljs: ## Start cljs REPL via nbb
	@npx nbb --classpath "$(CP)" nrepl-server

.PHONY: test
test: test-clj test-bb test-cljs test-nbb ## Run all tests

.PHONY: test-clj
test-clj: ## Run clj tests
	clojure -M:dev:1.9:test-clj
	clojure -M:dev:1.10:test-clj
	clojure -M:dev:test-clj

.PHONY: test-bb
test-bb: ## Run babashka tests
	@bb --classpath "$(CP)" bb_test_runner.clj

.PHONY: test-cljs
test-cljs: ## Run cljs tests
	@clojure -M:dev:test-cljs

.PHONY: test-nbb
test-nbb: ## Run nbb tests
	@npx nbb --classpath "$(CP)" bb_test_runner.clj

.PHONY: clean
clean: ## Clean
	rm -rf node_modules target .cpcache .clj-kondo/.cache

.PHONY: lint
lint: ## Check lint errors and format errors
	clj-kondo --lint src:test
	cljstyle check

.PHONY: outdated
outdated: ## Show and upgrade outdated dependencies
	clojure -M:outdated --upgrade

.PHONY: install
install: clean ## Install jar to the local Maven repository
	clojure -T:build install

.PHONY: coverage
coverage: ## Show coverage
	clojure -M:coverage:dev --src-ns-path=src --test-ns-path=test --codecov
