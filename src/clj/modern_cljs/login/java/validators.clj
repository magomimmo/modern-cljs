(ns modern-cljs.login.java.validators
  (:require [valip.core :refer [validate]]
            [valip.java.predicates :refer [valid-email-domain?]]))

(defn email-domain? [email]
  (validate {:email email}
            [:email valid-email-domain? "Email domain doesn't exists"]))
