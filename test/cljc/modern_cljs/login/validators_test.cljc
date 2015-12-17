(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :as v :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest user-credential-errors-test
  (testing "Login Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (user-credential-errors "me@me.com" "weak1")))
    (testing "/ Email Presence"
      (are [expected actual] (= expected actual)
        
        {:email
         ["Email can't be empty."
          "The provided email is invalid."],
         :password
         ["Password can't be empty."
          "The provided password is invalid"]}
        (user-credential-errors nil nil)

        {:email
         ["Email can't be empty."
          "The provided email is invalid."],
         :password
         ["Password can't be empty."
          "The provided password is invalid"]}
        (user-credential-errors "" nil)

        {:email
         ["Email can't be empty."
          "The provided email is invalid."],
         :password
         ["Password can't be empty."
          "The provided password is invalid"]}
        (user-credential-errors "" "")

        {:email
         ["Email can't be empty." "The provided email is invalid."],
         :password 
         ["The provided password is invalid"]}
        (user-credential-errors "" "weak")

        {:email
         ["Email can't be empty." "The provided email is invalid."]}
        (user-credential-errors "" "weak1")))

    (testing "/ Email is invalid"
      (are [expected actual] (= expected actual)
        
        {:email ["The provided email is invalid."],
         :password
         ["Password can't be empty."
          "The provided password is invalid"]}
        (user-credential-errors "me" nil)

        {:email ["The provided email is invalid."],
           :password
           ["Password can't be empty."
            "The provided password is invalid"]}
        (user-credential-errors "me" "")

        {:email ["The provided email is invalid."],
           :password ["The provided password is invalid"]}
        (user-credential-errors "me" "weak")

        {:email ["The provided email is invalid."]}
        (user-credential-errors "me" "weak1")))))

#?(:clj (deftest email-domain-errors-test
          (testing "/ Happy Path" 
            (are [expected actual] (= expected actual)
              nil (v/email-domain-errors "me@me.com")))))

