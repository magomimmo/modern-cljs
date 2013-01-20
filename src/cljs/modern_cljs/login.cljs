(ns modern-cljs.login
  (:require [domina :refer [by-id value log]]
            [domina.events :refer [listen! prevent-default]]))

(def ^:dynamic *min-password-length* 8)

(def ^:dynamic *email-re* 
     #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(defn validate-email [evt email]
  (if (not (re-matches *email-re* email))
    (log (str "Please type a valid email address."))
    true))

(defn validate-password [evt password]
  (if (< (count password) 8)
    (log (str "The password has to be longer."))
    true))

(defn validate-form [evt]
  (let [email (value (by-id "email"))
        password (value (by-id "password"))]
    (if (or (empty? email) (empty? password))
      (do
        (prevent-default evt)
        (js/alert "Please insert your email and "))
      (if (and (validate-email evt email)
               (validate-password evt password))
        true
        (prevent-default evt)))))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt)))
      (listen! email :blur (fn [evt] (validate-email evt (value email))))
      (listen! password :blur (fn [evt] (validate-password evt (value password)))))))
