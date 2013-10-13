(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "A series of tutorials on ClojureScript"
  :url "https://github.com/magomimmo/modern-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
 
  :pom-addition [:developers [:developer 
                              [:id "magomimmo"]
                              [:name "Mimmo Cosenza"]
                              [:url "https://github.com/magomimmo"]
                              [:email "mimmo.cosenza@gmail.com"]
                              [:timezone "+2"]]]

  :min-lein-version "2.1.2"

  ;; clojure source code path
  :source-paths ["src/clj"]
  ;; clojure source test path
  :test-paths ["test/clj"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1913"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [domina "1.0.2"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]]

  :plugins [[lein-cljsbuild "0.3.4"]
            [lein-ring "0.8.7"]]

  :hooks [leiningen.cljsbuild]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/app}

  ;; cljsbuild tasks configuration
  :cljsbuild {:crossovers [valip.core 
                           valip.predicates 
                           modern-cljs.login.validators
                           modern-cljs.shopping.validators]
              :builds
              {:dev
               {;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               {;; same path as above
                :source-paths ["src/brepl" "src/cljs"]

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optimization
                           :optimizations :simple

                           ;; no need prettification
                           :pretty-print false}}
               :prod
               {;; same path as above
                :source-paths ["src/cljs"]

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced

                           ;; no need prettification
                           :pretty-print false}}
               }})
