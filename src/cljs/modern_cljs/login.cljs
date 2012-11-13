(ns modern-cljs.login
  (:use [domina :only [by-id value]]))

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

;;; export init function to let it be called inside a script tag in
;;; the corresponding login.html page
(defn ^:export init []
  ;; verity that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; validate-form function
    (let [login-form (.getElementById js/document "loginForm")]
      (set! (.-onsubmit login-form) validate-form))))

;; the following call to set the onload property of the winodw object
;; has been removed/commented as a consequence of the above exporting
;; of the init function.

;; when js/window has been loaded, set its onload property to init function
; (set! (.-onload js/window) init)
