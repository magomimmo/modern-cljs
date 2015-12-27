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
            [modern-cljs.shopping.validators :refer [validate-shopping-quantity
                                                     validate-shopping-price
                                                     validate-shopping-tax
                                                     validate-shopping-discount]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))

(defn validate-quantity [evt text]
  (let [label (sel "label[for=quantity]")]
    (remove-class! label "help")
    (if-let [error (validate-shopping-quantity (value (by-id "quantity")))]
      (do
        (add-class! label "help")
        (set-text! label error)
        false)
      (do
        (set-text! label text)
        true))))

(defn validate-price [evt text]
  (let [label (sel "label[for=price]")]
    (remove-class! label "help")
    (if-let [error (validate-shopping-price (value (by-id "price")))]
      (do
        (add-class! label "help")
        (set-text! label error)
        false)
      (do
        (set-text! label text)
        true))))

(defn validate-tax [evt text]
  (let [label (sel "label[for=tax]")]
    (remove-class! label "help")
    (if-let [error (validate-shopping-tax (value (by-id "tax")))]
      (do
        (add-class! label "help")
        (set-text! label error)
        false)
      (do
        (set-text! label text)
        true))))

(defn validate-discount [evt text]
  (let [label (sel "label[for=discount]")]
    (remove-class! label "help")
    (if-let [error (validate-shopping-discount (value (by-id "discount")))]
      (do
        (add-class! label "help")
        (set-text! label error)
        false)
      (do
        (set-text! label text)
        true))))

(defn calculate [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [quantity-text (text (sel "label[for=quantity]"))
          price-text (text (sel "label[for=price]"))
          tax-text (text (sel "label[for=tax]"))
          discount-text (text (sel "label[for=discount]"))]
      ;; blur quantity
      (listen! (by-id "quantity")
               :blur
               (fn [evt] 
                 (validate-quantity evt quantity-text)))
      ;; blur price
      (listen! (by-id "price")
               :blur
               (fn [evt] (validate-price evt price-text)))
      ;; blur tax
      (listen! (by-id "tax")
               :blur
               (fn [evt] (validate-tax evt tax-text)))
      ;; blur discount
      (listen! (by-id "discount")
               :blur
               (fn [evt] (validate-discount evt discount-text))))
    
    ;; click
    (listen! (by-id "calc") 
             :click 
             (fn [evt] (calculate evt)))
    ;; mouseover button
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div.help "Click to calculate"]))))  ;; hiccups
    ;; mouseout button
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))

