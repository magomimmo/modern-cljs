(ns modern-cljs.shopping
  (:use [domina :only [by-id value set-value!]]))

;;; we need to :export calculate funtion to protect it from renanimg
;;; caused by Google Closure Compiler when :simple or :advanced
;;; optimization option are used.
(defn ^:export calculate []
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
