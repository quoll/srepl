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

(defn meta-repl
  [& options]
  (apply main-repl (update-kv-list options :eval meta-eval-fn)))

(alter-var-root (var clojure.main/repl) (constantly meta-repl))

(defn repl-opt
  "Duplicates standard repl entry point in Clojure, handling init functions and initial requirements"
  [[_ & args] inits]
  (when-not (some #(= #'clojure.main/eval-opt (#'clojure.main/init-dispatch (first %))) inits)
    (println "Clojure" (clojure-version)))
  (meta-repl :init (fn []
                     (#'clojure.main/initialize args inits)
                     (apply require clojure.main/repl-requires)))
  (prn)
  (System/exit 0))

(defn -main
  "Standalone entry point for the repl. Duplicates the standard entry by clojure.main to handle the various
   expected command line options, when init functions are provided and/or a repl is not being called."
  [& args]
  (try 
    (if args
      (loop [[opt arg & more :as args] args, inits [], flags nil]
        (cond
          ;; flag
          (contains? #{"--report"} opt)
          (recur more inits (merge flags {(subs opt 2) arg}))
      
          ;; init opt
          (#'clojure.main/init-dispatch opt)
          (recur more (conj inits [opt arg]) flags)
  
          :main-opt
          (try
            ((#'clojure.main/main-dispatch opt) args inits)
            (catch Throwable t
              (clojure.main/report-error t :target (get flags "report" (System/getProperty "clojure.main.report" "file")))
              (System/exit 1)))))
      (try
        (repl-opt nil nil)
        (catch Throwable t
          (clojure.main/report-error t :target "file")
          (System/exit 1))))
    (finally
      (flush))))

