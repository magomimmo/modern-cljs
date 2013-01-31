(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as shore-macros])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [modern-cljs.login.validators :refer [user-credential-errors]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))


(defn validate-email-domain [email]
  (remote-callback :email-domain-errors
                   [email]
                   #(if %
                      (do
                        (prepend! (by-id "loginForm")
                                  (html [:div.help.email "The email domain doesn't exist."]))
                        false)
                      true)))

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [errors (:email (user-credential-errors (value email) nil))]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    (validate-email-domain (value email))))


(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [errors (:password (user-credential-errors nil (value password)))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))

(defn validate-form [evt email password]
  (if-let [{e-errs :email p-errs :password} (user-credential-errors (value email) (value password))]
    (if (or e-errs p-errs)
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") (html [:div.help "Please complete the form."])))
      (prevent-default evt))
    true))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt email password)))
      (listen! email :blur (fn [evt] (validate-email email)))
      (listen! password :blur (fn [evt] (validate-password password))))))
