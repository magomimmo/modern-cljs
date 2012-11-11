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

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [theForm (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit theForm) calculate))))

(set! (.-onload js/window) init)
