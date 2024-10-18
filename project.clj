(defproject srepl "0.0.1"
  :description "POC for a repl that provides access to sources of values defined in the repl"
  :url "https://github.com/quoll/srepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.0"]]
  :aot [srepl.main srepl.core]
  :main srepl.main)
