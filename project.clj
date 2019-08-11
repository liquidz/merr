(defproject merr "0.2.2-SNAPSHOT"
  :description "Minimal and good enough error handling library for Clojure/ClojureScript"
  :url "https://github.com/liquidz/merr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.520"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]]

  :doo
  {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}

  :profiles
  {:dev {:dependencies [[testdoc "0.1.0"]
                        [orchestra "2019.02.06-1"]
                        [org.clojure/test.check "0.10.0"]]
         :source-paths ["dev" "src"]
         :global-vars {*warn-on-reflection* true}}

   :test [:dev {:dependencies [[org.mozilla/rhino "1.7.11"]]
                :cljsbuild
                {:builds
                 {:test
                  {:source-paths ["src" "test"]
                   :compiler {:output-to "target/main.js"
                              :output-dir "target"
                              :main merr.test-runner
                              :optimizations :simple}}}}}]
   :1.9 [:test {:dependencies [[org.clojure/clojure "1.9.0"]]}]
   :1.10 [:test {:dependencies [[org.clojure/clojure "1.10.0"]]}]
   :1.10.1 [:test {:dependencies [[org.clojure/clojure "1.10.1"]]}]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :aliases
  {"test-clj"  ["with-profile" "1.9:1.10:1.10.1" "test"]
   "test-cljs" ["with-profile" "test" "doo" "rhino" "test" "once"]
   "test-all"  ["do" ["test-clj"] ["test-cljs"]]})
