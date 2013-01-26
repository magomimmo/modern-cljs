(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [validate-user-credential]]))

(defn authenticate-user [email password]
  (let [{email-messages :email
         password-messages :password} (validate-user-credential email password)]
    (println email-messages)
    (println password-messages)
    (if (and (empty? email-messages)
             (empty? password-messages))
      (str email " and " password
           " passed the formal validation, but we still have to authenticate you")
      (str "Please complete the form."))))
