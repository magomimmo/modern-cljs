(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"

  ;; clojure source code path
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [domina "1.0.0"]]

  :plugins [[lein-cljsbuild "0.2.10"]
            [lein-ring "0.7.5"]]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              {:dev
               {;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               {;; same path as above
                :source-path "src/cljs"

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optimization
                           :optimizations :whitespace}}
               :prod
               {;; same path as above
                :source-path "src/cljs"

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced}}
               }})
