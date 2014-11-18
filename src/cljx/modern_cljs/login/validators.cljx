(ns modern-cljs.login.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? matches email-address?
                                      #+clj valid-email-domain?]]))

(def ^:dynamic *re-password* #"^(?=.*\d).{4,8}$")

(defn user-credential-errors [email password]
  (validate {:email email :password password}
            [:email present? "Email can't be empty."]
            [:email email-address? "The provided email is invalid."]
            [:password present? "Password can't be empty."]
            [:password (matches *re-password*) "The provided password is invalid."]))

#+clj
(defn email-domain-errors [email]
  (validate {:email email}
            [:email valid-email-domain? "The domain of the email doesn't exist."]))
