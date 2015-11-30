(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors
                                                  email-domain-errors]]))

(defn authenticate-user [email password]
  (if (or (boolean (email-domain-errors email))
          (boolean (user-credential-errors email password)))
    (str "Please complete the form")
    (str email " and " password
         " passed the formal validation, but you still have to be authenticated")))

