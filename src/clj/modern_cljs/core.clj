(ns modern-cljs.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  (GET "/" [] "<p>Hello from compojure</p>")
  ; to server static pages saved in resources/public directory
  (route/resources "/")
  ; when the stati resource does not exist
  (route/not-found "Page non found"))

(def handler
  (handler/site app-routes))
