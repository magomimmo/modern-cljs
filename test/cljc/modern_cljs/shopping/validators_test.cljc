(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form
                                                     validate-shopping-field]]
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
           (first (:quantity (validate-shopping-form "" nil nil nil)))

           "Quantity can't be empty"
           (first (:quantity (validate-shopping-form nil nil nil nil)))

           "Price can't be empty"
           (first (:price (validate-shopping-form nil "" nil nil)))

           "Price can't be empty"
           (first (:price (validate-shopping-form nil nil nil nil)))

           "Tax can't be empty"
           (first (:tax (validate-shopping-form nil nil  "" nil)))

           "Tax can't be empty"
           (first (:tax (validate-shopping-form nil nil nil nil)))

           "Discount can't be empty"
           (first (:discount (validate-shopping-form nil nil nil "")))

           "Discount can't be empty"
           (first (:discount (validate-shopping-form nil nil nil nil)))))

    (testing "/ Value Type"
      (are [expected actual] (= expected actual)

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "foo" nil nil nil)))

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "1.1" nil nil nil)))

           "Price has to be a number"
           (first (:price (validate-shopping-form nil "foo" nil nil)))

           "Tax has to be a number"
           (first (:tax (validate-shopping-form nil nil "foo" nil)))

           "Discount has to be a number"
           (first (:discount (validate-shopping-form nil nil nil "foo")))))

    (testing "/ Value Range"
      (are [expected actual] (= expected actual)

           "Quantity can't be negative"
           (first (:quantity (validate-shopping-form "-1" nil nil nil)))))))

(deftest validate-shopping-field-test
  (testing "Shopping Form Field Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-field :quantity "1")
        nil (validate-shopping-field :price "1.0")
        nil (validate-shopping-field :tax "1.0")
        nil (validate-shopping-field :discount "1.0")))
    ;; presence
    (testing "/ Presence"
      (are [expected actual] (= expected actual)
        "Quantity can't be empty" (validate-shopping-field :quantity "")
        "Quantity can't be empty" (validate-shopping-field :quantity nil)
        "Price can't be empty" (validate-shopping-field :price "")
        "Price can't be empty" (validate-shopping-field :price nil)
        "Tax can't be empty" (validate-shopping-field :tax "")
        "Tax can't be empty" (validate-shopping-field :tax nil)
        "Discount can't be empty" (validate-shopping-field :discount "")
        "Discount can't be empty" (validate-shopping-field :discount nil)))
    ;; type
    (testing "/ Type"
      (are [expected actual] (= expected actual)
        "Quantity has to be an integer number" (validate-shopping-field :quantity "1.1")
        "Quantity has to be an integer number" (validate-shopping-field :quantity "foo")
        "Price has to be a number" (validate-shopping-field :price "foo")
        "Tax has to be a number" (validate-shopping-field :tax "foo")
        "Discount has to be a number" (validate-shopping-field :discount "foo")
        "Discount can't be empty" (validate-shopping-field :discount nil)))
    ;; range
    (testing "/ Type"
      (are [expected actual] (= expected actual)
        "Quantity can't be negative" (validate-shopping-field :quantity "-1")))))
