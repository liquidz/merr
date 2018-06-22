(defproject merr "0.1.0-SNAPSHOT"
  :description "Minimal and good enough error handling library for Clojure/ClojureScript"
  :url "https://github.com/liquidz/merr"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.10"]]

  :doo
  {:paths {:rhino "lein run -m org.mozilla.javascript.tools.shell.Main"}}

  :aliases
  {"test-cljs" ["with-profile" "test" "doo" "rhino" "test" "once"]
   "test-all"  ["do" ["test"] ["test-cljs"]]}
  :profiles
  {:test {:dependencies [[org.mozilla/rhino "1.7.10"]]
          :cljsbuild
          {:builds
           {:test
            {:source-paths ["src" "test"]
             :compiler {:output-to "target/main.js"
                        :output-dir "target"
                        :main merr.test-runner
                        :optimizations :simple}}}}}})
