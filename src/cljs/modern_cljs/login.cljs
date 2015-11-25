(ns modern-cljs.login
  (:require [domina.core :refer [by-id value]]
            [domina.events :refer [listen! prevent-default]]))

;;; 4 to 8, at least one numeric digit.
(def ^:dynamic *password-re* 
  #"^(?=.*\d).{4,8}$")

(def ^:dynamic *email-re* 
  #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(defn validate-form [e]
  (if (or (empty? (value (by-id "email")))
          (empty? (value (by-id "password"))))
    (do 
      (prevent-default e) 
      (js/alert "Please, complete the form!"))
    true))

(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    (listen! (by-id "submit") :click validate-form)))
