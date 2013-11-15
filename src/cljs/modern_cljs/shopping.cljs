(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!
                            add-class! remove-class! text set-text!]]
            [domina.events :refer [listen! prevent-default]]
            [domina.xpath :refer [xpath]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [modern-cljs.shopping.validators :refer [validate-field]]
            [cljs.core.async :refer [put! chan >! <! map< merge]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))))

(defn add-help! []
  (append! (by-id "shoppingForm")
               (html [:div.help "Click to calculate"])))

(defn remove-help![]
  ;;(destroy! (by-class "help")))
  (destroy! (.getElementsByClassName js/document "help")))

(defn listen [el type]
  (let [out (chan)]
    (listen! el type (partial put! out))
    out))

(defn errors-for [field]
  (map< (fn [evt]
          (or (validate-field (keyword field) (.-value (:target evt)))
              []))
        (listen (by-id field) :change)))

(defn check-field [field errs-chan]
  (let [label (xpath (str "//label[@for='" field "']"))
        title (text label)]
    (go-loop
      []
      (let [errs (<! errs-chan)]
        (if (empty? errs)
          (-> label (remove-class! "error") (set-text! title))
          (-> label (add-class! "error") (set-text! (first errs))))
        (recur)))))

(defn recalc-total [errs-chan]
  (go-loop
    [err-fields #{}]
    (let [[field errs] (<! errs-chan)]
      (when (not (empty? errs))
        (recur (conj err-fields field)))
      (let [rest-errs (disj err-fields field)]
        (when (empty? rest-errs)
          (calculate))
        (recur rest-errs)))))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [fields '("quantity" "price" "tax" "discount")
          all-field-errs (map (fn [field] (map< #(vector field %) (errors-for field))) fields)]
      (recalc-total (merge all-field-errs))
      (loop [fields fields]
        (when (not (empty? fields))
          (check-field (first fields) (errors-for (first fields)))
          (recur (rest fields)))))
    (destroy! (by-id "calc"))))
