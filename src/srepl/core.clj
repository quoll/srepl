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

(def eval-forms
  "Various forms that clojure.core/eval may appear in"
  #{'eval 'clojure.core/eval clojure.core/eval})

(defn meta-eval-fn
  [eval-fn]
  (let [efn (or eval-fn clojure.core/eval)]
    (fn 
      [form]
      (let [[value form] (if (and (seq? form) (eval-forms (first form)))
                           (let [f (efn (second form))]
                             [(efn f) f])
                           [(efn form) form])]
        (if-let [m (meta value)]
          (if (instance? clojure.lang.IObj value)
            (vary-meta value assoc :src form)
            (when (var? value)
              (alter-var-root value add-meta form)
              (.setMeta value (merge {:src form} m))
              value))
          value)))))

(def main-repl clojure.main/repl)

(defn update-kv-list
  [lst k vfn]
  (-> (apply hash-map lst)
      (update k vfn)
      seq
      flatten))

(defn src-repl
  [& options]
  (apply main-repl (update-kv-list options :eval meta-eval-fn)))

(alter-var-root (var clojure.main/repl) (constantly src-repl))
