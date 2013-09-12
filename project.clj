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
                 [enlive "1.1.4"]]
  
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler modern-cljs.core/app})
