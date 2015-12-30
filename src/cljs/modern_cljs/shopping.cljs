(ns modern-cljs.shopping
  (:require [domina.core :refer [add-class!
                                 append! 
                                 by-class
                                 by-id 
                                 destroy!
                                 remove-class!
                                 set-value!
                                 set-text!
                                 text
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [domina.css :refer [sel]]
            [hiccups.runtime]
            [modern-cljs.shopping.validators :refer [validate-shopping-field
                                                     validate-shopping-form]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))

(defn calculate! []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))
        errors (validate-shopping-form quantity price tax discount)]
    (if-not errors
      (remote-callback :calculate
                       [quantity price tax discount]
                       #(set-value! (by-id "total") (.toFixed % 2))))))

(defn validate-shopping-field! [evt field text]
  (let [attr (name field)
        label (sel (str "label[for=" attr "]"))]
    (remove-class! label "help")
    (if-let [error (validate-shopping-field field (value (by-id attr)))]
      (do
        (add-class! label "help")
        (set-text! label error))
      (do 
        (set-text! label text)
        (calculate!)
        (prevent-default evt)))))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    ;; get original labels' texts
    (let [quantity-text (text (sel "label[for=quantity]"))
          price-text (text (sel "label[for=price]"))
          tax-text (text (sel "label[for=tax]"))
          discount-text (text (sel "label[for=discount]"))]
      ;; quantity validation
      (listen! (by-id "quantity")
               :input
               (fn [evt] (validate-shopping-field! evt :quantity quantity-text)))
      ;; price validation
      (listen! (by-id "price")
               :input
               (fn [evt] (validate-shopping-field! evt :price price-text)))
      ;; tax validation
      (listen! (by-id "tax")
               :input
               (fn [evt] (validate-shopping-field! evt :tax tax-text)))
      ;; discount validation
      (listen! (by-id "discount")
               :input
               (fn [evt] (validate-shopping-field! evt :discount discount-text))))
    (listen! (by-id "calc") 
             :click 
             (fn [evt] 
               (calculate!)
               (prevent-default evt)))
    (listen! (by-id "calc") 
             :mouseover 
             (fn [_]
               (append! (by-id "shoppingForm")
                        (html [:div.help "Click to calculate"]))))  ;; hiccups
    (listen! (by-id "calc") 
             :mouseout 
             (fn [_]
               (destroy! (by-class "help"))))
    (calculate!)))

