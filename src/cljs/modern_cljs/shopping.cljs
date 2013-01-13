(ns modern-cljs.shopping
  (:require-macros [hiccups.core :as h]
                   [shoreleave.remotes.macros :as macros])
  (:require [domina :as dom]
            [domina.events :as ev]
            [shoreleave.remotes.http-rpc :as rpc]
            [cljs.reader :refer [read-string]]))

(defn calculate []
  (let [quantity (read-string (dom/value (dom/by-id "quantity")))
        price (read-string (dom/value (dom/by-id "price")))
        tax (read-string (dom/value (dom/by-id "tax")))
        discount (read-string (dom/value (dom/by-id "discount")))]
    (rpc/remote-callback :calculate
                         [quantity price tax discount]
                         #(dom/set-value! (dom/by-id "total") (.toFixed % 2)))))

(defn add-help []
  (dom/append! (dom/by-id "shoppingForm")
               (h/html [:div.help "Click to calculate"])))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (ev/listen! (dom/by-id "calc") :click calculate)
    (ev/listen! (dom/by-id "calc") :mouseover add-help)
    (ev/listen! (dom/by-id "calc") :mouseout remove-help)))
