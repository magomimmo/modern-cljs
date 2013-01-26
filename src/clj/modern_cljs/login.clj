(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn authenticate-user [email password]
  (let [{email-errors :email
         password-errors :password} (user-credential-errors email password)]
    (println email-errors)
    (println password-errors)
    (if (and (empty? email-errors)
             (empty? password-errors))
      (str email " and " password
           " passed the formal validation, but we still have to authenticate you")
      (str "Please complete the form."))))
