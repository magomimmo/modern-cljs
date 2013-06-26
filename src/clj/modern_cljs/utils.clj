(ns modern-cljs.utils
  (:require [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))

(defn parse-integer [s]
  (if (and (string? s) (re-matches #"\s*[+-]?\d+\s*" s))
    (read-string s)))

(defn parse-double [s]
  (if (and (string? s) (re-matches #"\s*[+-]?\d+(\.\d+(M|M|N)?)?\s*" s))
    (read-string s)))

(defn parse-number [x]
  (if (and (string? x) (re-matches #"\s*[+-]?\d+(\.\d+M|M|N)?\s*" x))
    (read-string x)))

