(ns modern-cljs.shopping
  (:use [domina :only [by-id value set-value!]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    ;; bad CLJ style
    ;; (set-value! (by-id "total")
    ;;             (.toFixed (- (* (+ 1 (/ tax 100))
    ;;                             (* quantity price))
    ;;                          discount)
    ;;                       2))

    ;; better CLJ style
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))
    false))

;;; export init function to let it be called inside a script tag in
;;; the corresponding shopping.html page
(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (let [theForm (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit theForm) calculate))))

;; the following call to set the onload property of the winodw object
;; has been removed/commented as a consequence of the above exporting
;; of the init function.

;; when js/window has been loaded, set its onload property to init function
; (set! (.-onload js/window) init)
