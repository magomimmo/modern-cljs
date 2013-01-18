(ns modern-cljs.login
  (:require [domina :refer [by-id value log]]
            [domina.events :refer [listen! event-type prevent-default]]))

(defn validate-form [e]
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do
        (prevent-default e)
        (js/alert "Please, complete the form!")
        false))))

(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click (fn [e] (validate-form e)))))
