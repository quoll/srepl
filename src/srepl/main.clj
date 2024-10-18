(ns srepl.main
  (:require [clojure.main]
            [srepl.core :as c])
  (:gen-class))

(defn repl-opt
  "Duplicates standard repl entry point in Clojure, handling init functions and initial requirements"
  [[_ & args] inits]
  (when-not (some #(= #'clojure.main/eval-opt (#'clojure.main/init-dispatch (first %))) inits)
    (println "Clojure" (clojure-version)))
  (c/src-repl :init (fn []
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

