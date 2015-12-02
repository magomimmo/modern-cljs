(ns modern-cljs.core 
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found files resources]]
            [modern-cljs.login :refer [authenticate-user]]
            [modern-cljs.templates.shopping :refer [shopping]]))

(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (POST "/login" [email password] (authenticate-user email password))
  (POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))
  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))         ;; page not found
