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

(defn add-help []
  (dom/append! (dom/by-id "shoppingForm")
               "<div class='help'>Click to calculate</div>"))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

(defn ^:export init []
  (when (and js/document
             (.-getElementById js/document))
    (ev/listen! (dom/by-id "calc") :click calculate)
    (ev/listen! (dom/by-id "calc") :mouseover add-help)
    (ev/listen! (dom/by-id "calc") :mouseout remove-help)))
