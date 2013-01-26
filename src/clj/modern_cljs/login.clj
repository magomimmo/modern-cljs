(ns modern-cljs.login
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? email-address? matches]]))

(def ^:dynamic *re-password*
  #"^(?=.*\d).{4,8}$")

(defn validate-email [email]
  (validate {:email email}
            [:email present? "Email can't be empty"]
            [:email email-address? "The provided email is invalid"]))

(defn validate-password [password]
  (validate {:password password}
            [:password present? "Password can't be empty"]
            [:password (matches *re-password*) "The provided password is invalid"]))

(defn authenticate-user [email password]
  (let [email-errors (validate-email email)
        passwd-errors (validate-password password)]
    (println email-errors passwd-errors)
    (if (and (empty? email-errors)
             (empty? passwd-errors))
      (str email " and " password
           " passed the formal validation, but we still have to authenticate you")
      (str "Please complete the form"))))
