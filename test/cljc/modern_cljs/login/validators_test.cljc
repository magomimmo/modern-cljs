(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest user-credential-errors-test
  (testing "Login Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (user-credential-errors "me@me.com" "weak1")))))
