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

  :min-lein-version "2.2.0"

  ;; clojure source code path
  :source-paths ["src/clj" "src/cljs" "src/brepl"]
  :test-paths ["target/test/clj" "target/test/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [compojure "1.1.6"]
                 [hiccups "0.2.0"]
                 [domina "1.0.3-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]]

  :plugins [[lein-cljsbuild "1.0.0"]
            [lein-ring "0.8.8"]
            [com.keminglabs/cljx "0.3.0"]
            [com.cemerick/clojurescript.test "0.2.1"]]

  :hooks [leiningen.cljsbuild]

  :cljx {:builds [{:source-paths ["test/cljx"]
                   :output-path "target/test/clj"
                   :rules :clj}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test/cljs"
                   :rules :cljs}]}

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/app}

  ;; cljsbuild tasks configuration
  :cljsbuild {:crossovers [valip.core
                           valip.predicates
                           modern-cljs.login.validators
                           modern-cljs.shopping.validators]
              ;; for unit testing with phantomjs
              :test-commands {"phantomjs-whitespace"
                              ["phantomjs" :runner "target/test/js/testable_dbg.js"]

                              "phantomjs-simple"
                              ["phantomjs" :runner "target/test/js/testable_pre.js"]

                              "phantomjs-advanced"
                              ["phantomjs" :runner "target/test/js/testable.js"]}
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
                           :pretty-print false}}}})
