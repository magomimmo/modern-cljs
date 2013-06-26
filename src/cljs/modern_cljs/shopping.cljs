(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))

(defn calculate [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))

(defn add-help! []
  (append! (by-id "shoppingForm")
               (html [:div.help "Click to calculate"])))

(defn remove-help![]
  ;;(destroy! (by-class "help")))
  (destroy! (.getElementsByClassName js/document "help")))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
