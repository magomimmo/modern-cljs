(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.2.0"
  
  :source-paths ["src/clj"]
  :test-paths ["target/test/clj"]
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [domina "1.0.2-SNAPSHOT"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]
                 [com.cemerick/clojurescript.test "0.0.4"]
                 [com.cemerick/piggieback "0.1.0"]]
  
  :plugins [[lein-ring "0.8.7"]
            [lein-cljsbuild "0.3.3"]
            [com.keminglabs/cljx "0.3.0"]]

  :hooks [leiningen.cljsbuild]
  
  :aliases {"clean-test!" ["do" "clean," "cljx," "compile," "test"]
            "clean-run!" ["do" "clean," "cljx," "compile," "ring" "server-headless"]}
            
  
  :cljx {:builds [{:source-paths ["test/cljx"]
                   :output-path "target/test/clj"
                   :rules :clj}
                  
                  {:source-paths ["test/cljx"]
                   :output-path "target/test/cljs"
                   :rules :cljs}]}
  
  :ring {:handler modern-cljs.core/app}
  
  :cljsbuild {:crossovers [valip.core
                           valip.predicates
                           modern-cljs.login.validators
                           modern-cljs.shopping.validators]
              ;; for unit testing with phantomjs
              :test-commands {"phantomjs-whitespace"
                              ["runners/phantomjs.js" "target/test/js/testable_dbg.js"]
                              
                              "phantomjs-simple"
                              ["runners/phantomjs.js" "target/test/js/testable_pre.js"]
                              
                              "phantomjs-advanced"
                              ["runners/phantomjs.js" "target/test/js/testable.js"]}
              :builds
              {:ws-unit-tests
               { ;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                
                ;; Google Closure Compiler options
                :compiler { ;; the name of emitted JS script file
                           :output-to "target/test/js/testable_dbg.js"
                           
                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               
               :simple-unit-tests
               { ;; same path as above
                :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                
                :compiler { ;; different JS output name
                           :output-to "target/test/js/testable_pre.js"
                           
                           ;; simple optimization
                           :optimizations :simple
                           
                           ;; no need prettification
                           :pretty-print false}}
               
               :advanced-unit-tests
               { ;; same path as above
                :source-paths ["src/cljs" "target/test/cljs"]
                
                :compiler { ;; different JS output name
                           :output-to "target/test/js/testable.js"
                           
                           ;; advanced optimization
                           :optimizations :advanced
                           
                           ;; no need prettification
                           :pretty-print false}}
               
               :dev
               { ;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs"]
                
                ;; Google Closure Compiler options
                :compiler { ;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"
                           
                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               { ;; same path as above
                :source-paths ["src/brepl" "src/cljs"]
                
                :compiler { ;; different JS output name
                           :output-to "resources/public/js/modern_pre.js"
                           
                           ;; simple optimization
                           :optimizations :simple
                           
                           ;; no need prettification
                           :pretty-print false}}
               :prod
               { ;; same path as above
                :source-paths ["src/cljs"]

                :compiler { ;; different JS output name
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced

                           ;; no need prettification
                           :pretty-print false}}}}
  
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :injections [(require '[cljs.repl.browser :as brepl]
                        '[cemerick.piggieback :as pb])
               (defn browser-repl []
                 (pb/cljs-repl :repl-env
                               (brepl/repl-env :port 9000)))])

