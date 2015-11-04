(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[org.clojure/clojure "1.7.0"] ;; add CLJ
                 [org.clojure/clojurescript "1.7.122"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.48-6"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.1"]
                 [adzerk/boot-cljs-repl "0.2.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

;;; add dev task
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :dir "target")
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)))

