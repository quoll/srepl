(ns srepl.core
  (:require [clojure.main]
            [clojure.repl])
  (:import [clojure.lang IObj])
  (:gen-class))

(def repl-source-fn clojure.repl/source-fn)

(defn meta-source-fn
  "Wraps the source-fn from clojure-repl to return the source
  of a given symbol, using a store form if available.

  Example: (source-fn 'filter)"
  [x]
  (if-let [form (:src (meta x))]
    form
    (when-let [v (resolve x)]
      (if-let [form (:src (meta v))]
        form
        (repl-source-fn x)))))

(alter-var-root (var clojure.repl/source-fn) (constantly meta-source-fn))

(defn add-meta [x f]
  (if (instance? clojure.lang.IObj x)
    (vary-meta x merge {:src f})
    x))

(defn meta-eval
  [form]
  (let [value (eval form)]
    (if-let [m (meta value)]
      (if (instance? clojure.lang.IObj value)
        (vary-meta value assoc :src form)
        (when (var? value)
          (alter-var-root value add-meta form)
          (.setMeta value (merge {:src form} m))
          value))
      value)))

(def main-repl clojure.main/repl)

(defn meta-repl
  [& options]
  (apply main-repl (concat options [:eval meta-eval])))

(alter-var-root (var clojure.main/repl) (constantly meta-repl))

(defn -main
  "An alternate REPL with sources"
  [& args]
  (apply clojure.main/main args))

