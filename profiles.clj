{:dev {:source-paths ["src/clj"]
       :test-paths ["target/test/clj"]

       :dependencies [[org.clojure/clojure "1.5.1"]
                      [org.clojure/clojurescript "0.0-1847"]
                      [com.cemerick/clojurescript.test "0.0.4"]
                      [com.cemerick/piggieback "0.0.5"]]
       
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

                   :test-commands {"whitespace"
                                   ["runners/phantomjs.js" 
                                    "resources/public/js/modern_dbg.js"]
                                   
                                   "simple"
                                   ["runners/phantomjs.js" 
                                    "resources/public/js/modern_pre.js"]
                                   
                                   "advanced"
                                   ["runners/phantomjs.js" 
                                    "resources/public/js/modern.js"]}
                   
                   :builds

                   {:dev
                    {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                     :compiler {:output-to "resources/public/js/modern_dbg.js"
                                :optimizations :whitespace
                                :pretty-print true}}

                    :pre-prod
                    {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                     :compiler {:output-to "resources/public/js/modern_pre.js"
                                :optimizations :simple
                                :pretty-print false}}
                    
                    :prod
                    {:source-paths ["src/cljs" "target/test/cljs"]
                     :compiler {:output-to "resources/public/js/modern.js"
                                :optimizations :advanced
                                :pretty-print false}}}}
       
       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
       
       :injections [(require '[cljs.repl.browser :as brepl]
                             '[cemerick.piggieback :as pb])
                    (defn browser-repl []
                      (pb/cljs-repl :repl-env
                                    (doto (brepl/repl-env :port 9000)
                                      cljs.repl/-setup)))]}}

