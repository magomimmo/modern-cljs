(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr attr=]]
            [modern-cljs.remotes :refer [calculate]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(defn update-shopping-form [q p t d errors]
  (fn [node]
    nil))

(deftemplate shopping-form-template "public/shopping.html"
  [q p t d errors]
  [:#quantity] (set-attr :value q)
  [:#price] (set-attr :value p)
  [:#tax] (set-attr :value t)
  [:#discount] (set-attr :value d)
  #{[:div :label] 
    [:input#total]} (update-shopping-form q p t d e))

(defn shopping [q p t d]
  (shopping-form-template q p t d (validate-shopping-form q p t d)))
