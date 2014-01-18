;;; This namespace is used for testing purpose. It use the
;;; clojurescript.test lib.
(ns cljs-tutorial.core-test
  (:require-macros [cemerick.cljs.test :as m :refer (deftest testing are)])
  (:require [cemerick.cljs.test :as t]
            [cljs-tutorial.core :as core]))

(deftest foo-test
  (testing "I don't do a lot\n"
    (testing "(foo str)"
        (are [expected actual] (= expected actual)
             true false))))
