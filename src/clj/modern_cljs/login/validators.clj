(ns modern-cljs.login.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refers [present? matches email-address?]]))

(def ^:dynamic *re-password* #"^(?=.*\d).{4,8}$")

(defn validate-user-credential [email password]
  (validate {:email email :password password}
            [:email present? "Email can't be empty."]
            [:email email-address? "The provided email is invalid."]
            [:password present? "Password can't be empty."]
            [:password (matches *re-password*) "The provided password is invalid"]))
