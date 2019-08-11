.PHONY: test lein-test cli-test lein-test-clj lein-test-cljs cli-test-clj cli-test-cljs clean ancient

VERSION := 1.10.1
POM_FILE=pom.xml

$(POM_FILE):
	clj -Spom

repl:
	iced repl --with-kaocha with-profile $(VERSION)

test: lein-test cli-test

lein-test: lein-test-clj lein-test-cljs
lein-test-clj:
	lein test

lein-test-cljs:
	lein test-cljs

cli-test: cli-test-clj cli-test-cljs
cli-test-clj:
	clj -R:dev -A:test-clj

cli-test-cljs:
	clj -A:test-cljs

clean:
	lein clean
	\rm -f $(POM_FILE)

ancient:
	clojure -A:ancient
