.PHONY: test lein-test cli-test lein-test-clj lein-test-cljs cli-test-clj cli-test-cljs clean ancient

POM_FILE=pom.xml

$(POM_FILE):
	clj -Spom

test: lein-test cli-test

lein-test: lein-test-clj lein-test-cljs
lein-test-clj:
	lein test

lein-test-cljs:
	lein test-cljs

cli-test: cli-test-clj cli-test-cljs
cli-test-clj:
	clj -R:dev -A:test-clj -e "(require '[eftest.runner :refer :all]) (run-tests (find-tests \"test\"))"

cli-test-cljs:
	clj -A:test-cljs

clean:
	\rm -f $(POM_FILE)

ancient:
	clojure -A:ancient
