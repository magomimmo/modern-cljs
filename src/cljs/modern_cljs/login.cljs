(ns modern-cljs.login
  (:require-macros [domina.macros :refer [defined?]])
  (:require [domina :refer [by-id value log]]
            [domina.events :refer [listen!
                                   prevent-default]]))

(defn validate-form [e]
  ;; get email and password element using (by-id id)
  (let [email (value (by-id "email"))
        password (value (by-id "password"))
        email? (not (empty? email))
        password? (not (empty? password?))]
    (if (and email? password?)
      true
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
