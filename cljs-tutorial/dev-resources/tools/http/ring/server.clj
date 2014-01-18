;;; This namespace is used for development and testing purpose only.
(ns ring.server
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer  (resources)]
            [compojure.core :refer (GET defroutes)]
            [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]))

;;; We use enlive lib to add to the body of the index.html page the
;;; script tag containing the JS code which activates the bREPL
;;; connection.
(enlive/deftemplate page
  (io/resource "public/index.html")
  []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))

;;; defroutes macro defines a function that chains individual route
;;; functions together. The request map is passed to each function in
;;; turn, until a non-nil response is returned.
(defroutes site
  (resources "/")
  (GET "/*" req (page)))

;;; To run the jetty server. The server symbol is not private to
;;; allows to start and stop thejetty server from the repl.
(defn run
  "Run the ring server. It defines the server symbol with defonce."
  []
  (defonce server
    (jetty/run-jetty #'site {:port 3000 :join? false}))
  server)
