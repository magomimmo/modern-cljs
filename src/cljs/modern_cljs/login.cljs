(ns modern-cljs.login
  (:require [domina :refer [by-id value]]
            [domina.events :refer [listen!]]))

(defn validate-form []
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))

;; define the function to attach validate-form to onsubmit event of
;;the form
(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; our validate-form function
    (set! (.-onsubmit (by-id "loginForm")) validate-form)))
