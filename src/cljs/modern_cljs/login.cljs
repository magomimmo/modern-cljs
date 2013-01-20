(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! log]]
            [domina.events :refer [listen! prevent-default]]))

(def ^:dynamic *min-password-length* 8)

(def ^:dynamic *email-re*
     #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(defn validate-email [email-node]
  (destroy! (by-class "email"))
  (if (not (re-matches *email-re* (value email-node)))
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email "Wrong email"]))
      false)
    true))

(defn validate-password [password-node]
  (destroy! (by-class "password"))
  (if (< (count (value password-node)) 8)
    (do
      (append! (by-id "loginForm") (html [:div.help.password "Wrong password"]))
      false)
    true))

(defn validate-form [evt]
  (let [email-node (by-id "email")
        password-node (by-id "password")
        email-val (value email-node)
        password-val (value password-node)]
    (if (or (empty? email-val) (empty? password-val))
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") (html [:div.help "Please complete the form"])))
      (if (and (validate-email email-node)
               (validate-password password-node))
        true
        (prevent-default evt)))))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email-node (by-id "email")
          password-node (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt)))
      (listen! email-node :blur (fn [evt] (validate-email email-node)))
      (listen! password-node :blur (fn [evt] (validate-password password-node))))))
