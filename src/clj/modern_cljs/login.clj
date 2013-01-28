(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]
            [modern-cljs.login.java.validators :refer [email-domain?]]))

(defn authenticate-user [email password]
  (if (or (boolean (user-credential-errors email password)) (boolean (email-domain? email)))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
