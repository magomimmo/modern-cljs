(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [modern-cljs.login :refer [validate-user]]))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to server static pages saved in resources/public directory
  (POST "/login" [email password] (validate-user email password)) 
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))

;;; site function create an handler suitable for a standard website,
;;; adding a bunch of standard ring middleware to app-route:
(def handler
  (site app-routes))
