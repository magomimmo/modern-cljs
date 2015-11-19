(ns modern-cljs.shopping
  (:require [domina :refer [append! 
                            by-class
                            by-id 
                            destroy! 
                            set-value! 
                            value]]
            [domina.events :refer [listen!]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))))

(defn ^:export init []
  (when (and js/document
           (.-getElementById js/document))
    (listen! (by-id "calc") 
             :click 
             calculate)
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        "<div class='help'>Click to calculate</div>")))
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))

