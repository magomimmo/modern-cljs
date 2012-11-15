(ns modern-cljs.login
  (:use [domina :only [by-id value]]))

;;; we need to :export validate-form funtion to protect it from
;;; renanimg caused by Google Closure Compiler when :simple or
;;; :advanced optimization option are used.
(defn ^:export validate-form []
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))
