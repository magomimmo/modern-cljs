(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ; clojure source code path
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 ; compojure dependency
                 [compojure "1.1.3"]
                 ; domina dependency
                 [domina "1.0.0"]]
  :plugins [; cljsbuild plugin
            [lein-cljsbuild "0.2.9"]
            ; ring plugin
            [lein-ring "0.7.5"]]
  ; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}
  ; cljsbuild tadks configuration
  :cljsbuild {:builds
              [{; clojurescript source code path
                :source-path "src/cljs"
                ; Google Closure Compiler options
                :compiler {; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"
                           ; minimum optimization
                           :optimizations :whitespace
                           ; prettyfying emitted JS
                           :pretty-print true}}]})
