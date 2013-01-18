(ns modern-cljs.login
  (:require [domina :refer [by-id value]]
            [domina.events :refer [listen! prevent-default]]))

(defn validate-form [e]
  (let [email (value (by-id "email"))
        password (value (by-id "password"))
        blank-email? (empty? email)
        blank-password? (empty? password?)]
    (if (or blank-email? blank-password?)
      (do 
        (prevent-default e)
        (js/alert "Please insert your email and password"))
      false)))

(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click (fn [e] (validate-form e)))))
