(ns modern-cljs.shopping
  (:require [domina :as dom]
            [domina.events :as ev]))

(defn calculate []
  (let [quantity (dom/value (dom/by-id "quantity"))
        price (dom/value (dom/by-id "price"))
        tax (dom/value (dom/by-id "tax"))
        discount (dom/value (dom/by-id "discount"))]
    (dom/set-value! (dom/by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))))

(defn reset-form []
  (dom/set-value! (dom/by-id "quantity") "1")
  (dom/set-value! (dom/by-id "price") "0.00")
  (dom/set-value! (dom/by-id "tax") "0.0")
  (dom/set-value! (dom/by-id "discount") "0.00")
  (calculate))

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (let [calcButton (dom/by-id "calc")
          resetButton (dom/by-id "res")]
      (ev/listen! calcButton :click calculate)
      ;;(ev/listen! resetButton :click reset-form)
      )))
