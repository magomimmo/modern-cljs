(ns modern-cljs.shopping
  (:require [domina.core :refer [by-id value set-value!]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))
    false))

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [the-form (by-id "shoppingForm")]
      (set! (.-onsubmit the-form) calculate))))

(set! (.-onload js/window) init)
