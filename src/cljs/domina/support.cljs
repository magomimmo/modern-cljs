(ns domina.support
  (:require [goog.dom :as dom]
            [goog.events :as event]))

(let [div (.createElement js/document "div")
      test-html "   <link/><table></table><a href='/a' style='top:1px;float:left;opacity:.55;'>a</a><input type='checkbox'/>"]
  (set! (.-innerHTML div)
        test-html)
  (def leading-whitespace?
    (= (.-nodeType (.-firstChild div))
       3))
  (def extraneous-tbody?
    (= (.-length (.getElementsByTagName div "tbody"))
       0))
  (def unscoped-html-elements?
    (= (.-length (.getElementsByTagName div "link"))
       0)))
