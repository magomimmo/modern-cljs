(ns modern-cljs.shopping.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present?
                                      integer-string?
                                      decimal-string?
                                      gt]]))

(defn validate-shopping-form [quantity price tax discount]
  (validate {:quantity quantity :price price :tax tax :discount discount}

            ;; validate presence

            [:quantity present? "Quantity can't be empty"]
            [:price present? "Price can't be empty"]
            [:tax present? "Tax can't be empty"]
            [:discount present? "Discount can't be empty"]

            ;; validate type

            [:quantity integer-string? "Quantity has to be an integer number"]
            [:price decimal-string? "Price has to be a number"]
            [:tax decimal-string? "Tax has to be a number"]
            [:discount decimal-string? "Discount has to be a number"]

            ;; validate range

            [:quantity (gt 0) "Quantity can't be negative"]

            ;; other specific platform validations (not at the moment)

            ))

(defn validate-shopping-field [field value]
  (condp = field
    :quantity (first (field (validate-shopping-form value "1.00" "0.0" "0.0")))
    :price (first (field (validate-shopping-form "1" value "0.0" "0.0")))
    :tax (first (field (validate-shopping-form "1" "1.00" value "0.0")))
    :discount (first (field (validate-shopping-form "1" "1.00" "0.0" value)))))
