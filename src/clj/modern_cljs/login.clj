(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn authenticate-user [email password]
  (if (boolean (user-credential-errors email password))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
