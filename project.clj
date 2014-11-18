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
  :source-paths ["src/clj" "src/cljs" "src/brepl" "target/src/clj" "target/src/cljs"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [compojure "1.2.1"]
                 [hiccups "0.3.0"]
                 [domina "1.0.3-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"
                  :exclusions [org.clojars.magomimmo/shoreleave-browser]]
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"
                  :exclusions [org.clojars.magomimmo/shoreleave-browser]]
                 [org.clojars.magomimmo/shoreleave-browser "0.3.2-SNAPSHOT"]
                 [org.clojars.stepugnetti/valip "0.4.0-SNAPSHOT"
                  :exclusions [com.keminglabs/cljx]]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.8"]
            [com.keminglabs/cljx "0.4.0"]]

  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/src/clj"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/src/cljs"
                   :rules :cljs}]}

  ;; enable cljsbuild tasks support
  ;; :hooks [leiningen.cljsbuild]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.remotes/app}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              [{;; build id
                :id "dev"
                :source-paths ["target/src/cljs" "src/brepl" "src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               {;; build id
                :id "pre-prod"
                :source-paths ["target/src/cljs" "src/brepl" "src/cljs"]

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optimization
                           :optimizations :simple

                           ;; no need prettification
                           :pretty-print false}}
               {;; build id
                :id "prod"
                :source-paths ["target/src/cljs" "src/cljs"]
                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced

                           ;; no need prettification
                           :pretty-print false}}]})
