(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form
                                                     validate-shopping-quantity
                                                     validate-shopping-price
                                                     validate-shopping-tax
                                                     validate-shopping-discount]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "1" "0" "0" "0")
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))

    (testing "/ No presence"
      (are [expected actual] (= expected actual)

           "Quantity can't be empty"
           (first (:quantity (validate-shopping-form "" "0" "0" "0")))

           "Price can't be empty"
           (first (:price (validate-shopping-form "1" "" "0" "0")))

           "Tax can't be empty"
           (first (:tax (validate-shopping-form "1" "0" "" "0")))

           "Discount can't be empty"
           (first (:discount (validate-shopping-form "1" "0" "0" "")))))

    (testing "/ Value Type"
      (are [expected actual] (= expected actual)

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "foo" "0" "0" "0")))

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "1.1" "0" "0" "0")))

           "Price has to be a number"
           (first (:price (validate-shopping-form "1" "foo" "0" "0")))

           "Tax has to be a number"
           (first (:tax (validate-shopping-form "1" "0" "foo" "0")))

           "Discount has to be a number"
           (first (:discount (validate-shopping-form "1" "0" "0" "foo")))))

    (testing "/ Value Range"
      (are [expected actual] (= expected actual)

           "Quantity can't be negative"
           (first (:quantity (validate-shopping-form "-1" "0" "0" "0")))))))

;;; test quantity validators
(deftest validate-shopping-quantity-test
  (testing "Shopping Quantity Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-quantity "1")
        nil (validate-shopping-quantity "10")
        nil (validate-shopping-quantity "100")))
    ;; present?
    (testing "/ Present?"
      (are [expected actual] (= expected actual)
        "Quantity can't be empty" (validate-shopping-quantity nil)
        "Quantity can't be empty" (validate-shopping-quantity "")))
    ;; integer?
    (testing "/ Interger String?"
      (are [expected actual] (= expected actual)
        "Quantity has to be an integer number" (validate-shopping-quantity "1.0")
        "Quantity has to be an integer number" (validate-shopping-quantity "foo")))
    ;; positive?
    (testing "/ Positive?"
      (are [expected actual] (= expected actual)
        "Quantity can't be negative" (validate-shopping-quantity "-1")
        "Quantity can't be negative" (validate-shopping-quantity "-100")))))

;;; test price validators
(deftest validate-shopping-price-test
  (testing "Shopping Price Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-price "0.00")
        nil (validate-shopping-price "1.00")
        nil (validate-shopping-price "10.00")
        nil (validate-shopping-price "100.00")))
    ;; present?
    (testing "/ Present?"
      (are [expected actual] (= expected actual)
        "Price can't be empty" (validate-shopping-price nil)
        "Price can't be empty" (validate-shopping-price "")))
    ;; number?
    (testing "/ Number?"
      (are [expected actual] (= expected actual)
        "Price has to be a number" (validate-shopping-price "foo")
        "Price has to be a number" (validate-shopping-price "(* 1 2")))))

;;; test tax validators
(deftest validate-shopping-tax-test
  (testing "Shopping Tax Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-tax "0.00")
        nil (validate-shopping-tax "1.00")
        nil (validate-shopping-tax "10.00")
        nil (validate-shopping-tax "100.00")))
    ;; present?
    (testing "/ Present?"
      (are [expected actual] (= expected actual)
        "Tax can't be empty" (validate-shopping-tax nil)
        "Tax can't be empty" (validate-shopping-tax "")))
    ;; number?
    (testing "/ Number?"
      (are [expected actual] (= expected actual)
        "Tax has to be a number" (validate-shopping-tax "foo")
        "Tax has to be a number" (validate-shopping-tax "(* 1 2")))))

;;; test discount validators
(deftest validate-shopping-discount-test
  (testing "Shopping Discount Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-discount "0.00")
        nil (validate-shopping-discount "1.00")
        nil (validate-shopping-discount "10.00")
        nil (validate-shopping-discount "199.00")))
    ;; present?
    (testing "/ Present?"
      (are [expected actual] (= expected actual)
        "Discount can't be empty" (validate-shopping-discount nil)
        "Discount can't be empty" (validate-shopping-discount "")))
    ;; number?
    (testing "/ Number?"
      (are [expected actual] (= expected actual)
        "Discount has to be a number" (validate-shopping-discount "foo")
        "Discount has to be a number" (validate-shopping-discount "(* 1 2")))))
