(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]))

(deftemplate shopping "shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value (format "%.2f" (calculate 
                                             (read-string quantity)
                                             (read-string price)
                                             (read-string tax)
                                             (read-string discount)))))


