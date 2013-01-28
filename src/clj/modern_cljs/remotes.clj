(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [modern-cljs.login.java.validators :refer [email-domain?]]
            [compojure.handler :refer [site]]
            [cemerick.shoreleave.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(defremote email-domain-remote? [email]
  (email-domain? email))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))


