(ns domina.macros)

(defmacro defined? [x]
  (list 'js* "(typeof ~{} != 'undefined')" x))