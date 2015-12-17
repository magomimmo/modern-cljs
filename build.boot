(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"html"}
 :target-path "target"

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.2"]
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; add bREPL
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL 
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"]
                 [hiccups "0.3.0"]
                 [compojure "1.4.0"]                   ;; for routing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojars.magomimmo/valip "0.4.0-SNAPSHOT"]
                 [enlive "1.1.6"]
                 [adzerk/boot-test "1.0.6"]
                 [crisptrutski/boot-cljs-test "0.2.1-SNAPSHOT"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-test :refer [test]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(deftask testing
  []
  (set-env! :source-paths #(conj % "test/cljc"))
  identity)

(deftask tdd 
  []
  (comp
   (serve :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (testing)
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env js-engine 
              :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})))

;;; add dev task
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :dir "target"                                
          :handler 'modern-cljs.core/app               ;; ring hanlder
          :resource-root "target"                      ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)))
