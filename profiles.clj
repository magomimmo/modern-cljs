{:dev {:hooks [leiningen.cljsbuild]

       :dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                      [com.cemerick/piggieback "0.1.0"]]
       
       :plugins [[lein-cljsbuild "0.3.2"]
                 [com.keminglabs/cljx "0.3.0"]]

       :cljx {:builds [{:source-paths ["test/cljx"]
                        :output-path "target/test/clj"
                        :rules :clj}
                       
                       {:source-paths ["test/cljx"]
                        :output-path "target/test/cljs"
                        :rules :cljs}]}
       
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
                                    (brepl/repl-env :port 9000)))]}}
