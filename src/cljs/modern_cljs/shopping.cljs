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

(defn validate-shopping-field! [field text]
  (let [attr (name field)
        label (sel (str "label[for=" attr "]"))]
    (remove-class! label "help")
    (if-let [error (validate-shopping-field field (value (by-id attr)))]
      (do
        (add-class! label "help")
        (set-text! label error))
      (set-text! label text))))

(defn calculate! [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))
        errors (validate-shopping-form quantity price tax discount)]
    (if-not errors
      (remote-callback :calculate
                       [quantity price tax discount]
                       #(set-value! (by-id "total") (.toFixed % 2))))
    (prevent-default evt)))

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
               :blur
               (fn [_] (validate-shopping-field! :quantity quantity-text)))
      ;; price validation
      (listen! (by-id "price")
               :blur
               (fn [_] (validate-shopping-field! :price price-text)))
      ;; tax validation
      (listen! (by-id "tax")
               :blur
               (fn [_] (validate-shopping-field! :tax tax-text)))
      ;; discount validation
      (listen! (by-id "discount")
               :blur
               (fn [_] (validate-shopping-field! :discount discount-text))))
    
    ;; calculate
    (listen! (by-id "calc") 
             :click 
             (fn [evt] (calculate! evt)))
    ;; show help
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div#help.help "Click to calculate"]))))  ;; hiccups
    ;; remove help
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-id "help"))))))

