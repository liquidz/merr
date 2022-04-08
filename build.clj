(ns build
  (:require
   [clojure.tools.build.api :as b]
   [clojure.xml :as xml]
   [deps-deploy.deps-deploy :as deploy]))

(def ^:private class-dir "target/classes")
(def ^:private jar-file "target/merr.jar")
(def ^:private lib 'com.github.liquidz/merr)
(def ^:private pom-file "./pom.xml")

(defn- get-current-version
  [pom-file-path]
  (->> (xml/parse pom-file-path)
       (xml-seq)
       (some #(and (= :version (:tag %)) %))
       (:content)
       (first)))

(defn pom
  [arg]
  (let [basis (or (:basis arg) (b/create-basis {:project "deps.edn"}))
        ver' (or (:version arg) (get-current-version pom-file))]
    (b/write-pom {:basis basis
                  :class-dir class-dir
                  :lib lib
                  :version ver'
                  :src-dirs ["src"]})
    (when (:copy? arg true)
      (b/copy-file {:src (b/pom-path {:lib lib :class-dir class-dir})
                    :target pom-file}))))

(defn jar
  [arg]
  (let [basis (b/create-basis {:project "deps.edn"})
        arg (assoc arg :basis basis)]
    (pom arg)
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file jar-file})))

(defn install
  [arg]
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :local}))

(defn deploy
  [arg]
  (assert (and (System/getenv "CLOJARS_USERNAME")
               (System/getenv "CLOJARS_PASSWORD")))
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :remote
                  :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))

(defn deploy-snapshot
  [arg]
  (let [version (str (get-current-version pom-file)
                     "-SNAPSHOT")]
    (deploy (assoc arg
                   :version version
                   :copy? false))))
