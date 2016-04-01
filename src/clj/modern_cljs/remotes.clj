(ns modern-cljs.remotes
  (:require [modern-cljs.login.validators :as v]
            [shoreleave.middleware.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (-> (* (read-string quantity) (read-string price))
      (* (+ 1 (/ (read-string tax) 100)))
      (- (read-string discount))))

(defremote email-domain-errors [email]
  (v/email-domain-errors email))
