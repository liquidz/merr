(defproject merr "0.3.2-SNAPSHOT"
  :description "Minimal and good enough error handling library for Clojure/ClojureScript"
  :url "https://github.com/liquidz/merr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]

  :plugins [[lein-cloverage "1.2.1"]]

  :profiles
  {:1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
   :1.10 {:dependencies [[org.clojure/clojure "1.10.0"]]}
   :1.10.1 {:dependencies [[org.clojure/clojure "1.10.1"]]}
   :provided [:1.10.1 {:dependencies [[org.clojure/clojurescript "1.10.773"]]}]
   :test {:dependencies [[lambdaisland/kaocha "1.0.700"]
                         [lambdaisland/kaocha-cljs "0.0-71"]
                         [testdoc "1.4.0"]
                         [orchestra "2020.07.12-1"]
                         [org.clojure/test.check "1.1.0"]]}
   :dev [:test :1.10.1
         {:source-paths ["dev" "src"]
          :global-vars {*warn-on-reflection* true}}]

   :antq {:dependencies [[antq/antq "RELEASE"]]}}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :aliases
  {"test-clj" ["with-profile" "test,1.9:test,1.10:test,1.10.1" "test"]
   "test-cljs" ["with-profile" "+dev" "run" "-m" "kaocha.runner"]
   "test-all" ["do" ["test-clj"] ["test-cljs"]]})
