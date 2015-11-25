(ns modern-cljs.login
  (:require [domina.core :refer [by-id value]]
            [domina.events :refer [listen! prevent-default]]))

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
