(ns modern-cljs.core 
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found files resources]]
            [modern-cljs.login :refer [authenticate-user]]))

(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (POST "/login" [email password] (authenticate-user email password))
  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))         ;; page not found
