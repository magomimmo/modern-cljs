(set-env!
  :source-paths #{"src/cljs"}
  :resource-paths #{"html"}

  :dependencies '[[org.clojure/clojure "1.8.0"]
  [org.clojure/clojurescript "1.9.473"]
  [adzerk/boot-cljs "1.7.228-2"]
  [pandeiro/boot-http "0.7.6"]
  [org.clojure/tools.nrepl "0.2.12"] ;;required in order to make boot-http works
  [adzerk/boot-reload "0.5.1"]
  [adzerk/boot-cljs-repl "0.3.3"]
  [com.cemerick/piggieback "0.2.1"]
  [weasel "0.7.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
'[pandeiro.boot-http :refer [serve]]    ;; make serve task visible
'[adzerk.boot-reload :refer [reload]]
'[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])


;; define dev task as composition of subtasks
(deftask dev
  "Launch Immediate Feedback Development Environment"
  []
  (comp
    (serve :dir "target")
    (watch)
    (reload)
    (cljs-repl) ;; before cljs task
    (cljs)
    (target :dir #{"target"})))
