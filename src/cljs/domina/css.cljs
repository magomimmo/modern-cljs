(ns domina.css
  (:require [domina :as domina]
            [goog.dom :as dom]
            [goog.dom.query :as query]))

(defn- root-element
  []
  (aget (dom/getElementsByTagNameAndClass "html") 0))


(defn sel
  "Returns content based on a css selector expression. Takes an optional content as a base; if none is given, uses the HTML element as a base."
  ([expr] (sel (root-element) expr))
  ([base expr] (reify domina/DomContent
                 (nodes [_] (mapcat #(domina/normalize-seq (dom/query expr %))
                                    (domina/nodes base)))
                 (single-node [_] (first (filter
                                          (complement nil?)
                                          (mapcat #(domina/normalize-seq (dom/query expr %))
                                                  (domina/nodes base))))))))