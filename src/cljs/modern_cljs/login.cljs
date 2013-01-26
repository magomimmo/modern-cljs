(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [{errors :email} (user-credential-errors (value email) nil)]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    true))

(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [{errors :password} (user-credential-errors nil (value password))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))

(defn validate-form [evt]
  (let [email (by-id "email")
        password (by-id "password")
        email-val (value email)
        password-val (value password)]
    (if (or (empty? email-val) (empty? password-val))
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") (html [:div.help "Please complete the form."])))
      (if (user-credential-errors email-val password-val)
        (prevent-default evt)
        true))))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt)))
      (listen! email :blur (fn [evt] (validate-email email)))
      (listen! password :blur (fn [evt] (validate-password password))))))
