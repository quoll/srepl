(defproject srepl "0.1.0-SNAPSHOT"
  :description "A repl that provides access to sources of values defined in the repl"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-beta1"]]
  :aot [srepl.core]
  :main srepl.core)
