(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :as v :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

#?(:clj (deftest email-domain-errors-test
          (testing "Email domain existence"
            (are [expected actual] (= expected actual)
              "The domain of the email doesn't exist."
              (first (:email (v/email-domain-errors "me@googlenospam.com")))))))

(deftest user-credential-errors-test
  (testing "Login Form Validation"
    
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (user-credential-errors "me@me.com" "weak1")))
    
    (testing "/ Email presence"
      (are [expected actual] (= expected actual)
        "Email can't be empty."
        (first (:email (user-credential-errors "" "")))
        "Email can't be empty."
        (first (:email (user-credential-errors "" nil)))
        "Email can't be empty."
        (first (:email (user-credential-errors "" "weak1")))
        "Email can't be empty."
        (first (:email (user-credential-errors "" "weak")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil nil)))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "weak1")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "weak")))))

    (testing "/ Password presence"
      (are [expected actual] (= expected actual)
        "Password can't be empty."
        (first (:password (user-credential-errors "" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors nil "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "me@me.com" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "me" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "" nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors nil nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors "me@me.com" nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors "me" nil)))))

    (testing "/ Email validity"
      (are [expected actual] (= expected actual)
        "The provided email is invalid."
        (first (:email (user-credential-errors "me" "")))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me.me" nil)))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me@me" "weak")))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me.me@me" "weak1")))))

    (testing "/ Password validity"
      (are [expected actual] (= expected actual)
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "weak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors "" "lessweak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "lessweak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "toolongforthat")))))))


