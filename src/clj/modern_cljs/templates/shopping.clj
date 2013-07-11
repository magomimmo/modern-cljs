(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(defn update-attr [value error]
  (set-attr :value value))

(deftemplate shopping-template "public/shopping.html"
  [quantity price tax discount errors]
  [:#quantity] (update-attr quantity (first (:quantity errors)))
  [:#price] (update-attr price (first (:price errors)))
  [:#tax] (update-attr tax (first (:tax errors)))
  [:#discount] (update-attr discount (first (:discount errors)))
  [:#total] (set-attr :value
                      (format "%.2f" (calculate quantity price tax discount))))

(defn shopping [q p t d]
  (shopping-template q p t d (validate-shopping-form q p t d)))
