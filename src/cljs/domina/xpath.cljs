(ns domina.xpath
  (:require [domina :as domina]
            [goog.dom :as dom]))

;; This file covers the same basic functionality as goog.dom.xml.
;; However, it does so in such a way that it can fall back to xpath support
;; provided by non-native XPath implementations (e.g, http://llamalab.com/js/xpath/
;; or http://mcc.id.au/xpathjs)

(defn- select-node*
  [path node technique-1 technique-2]
  (let [doc (dom/getOwnerDocument node)]
    (cond (and (. node -selectSingleNode)
               (. doc -setProperty))
          (do
            (.setProperty doc "SelectionLanguage" "XPath")
            (technique-1 node path))
          (. doc -evaluate)
          (technique-2 nil doc node path)
          :else (throw (js/Error. "Could not find XPath support in this browser.")))))

(defn- select-node
  "Selects a single node using an XPath expression and a root node"
  [expr node]
  (select-node* expr node
                (fn [node expr]
                  (.selectSingleNode node expr))
                (fn [resolver doc node expr]
                  (let [result (.evaluate doc expr node nil
                                          XPathResult/FIRST_ORDERED_NODE_TYPE nil)]
                    (. result -singleNodeValue)))))

(defn- select-nodes
  "Selects multiple nodes using an XPath expression and a root node"
  [expr node]
  (select-node* expr node
                (fn [node expr]
                  (.selectNodes node expr))
                (fn [resolver doc node expr]
                  (let [result (.evaluate doc expr node nil
                                          XPathResult/ORDERED_NODE_SNAPSHOT_TYPE nil)
                        num-results (.-snapshotLength result)]
                    (loop [i 0 acc nil]
                      (if (< i num-results)
                        (recur (inc i) (cons (.snapshotItem result i) acc))
                        acc))))))

(defn- root-element
  []
  (aget (dom/getElementsByTagNameAndClass "html") 0))

(defn xpath
  "Returns content based on an xpath expression. Takes an optional content as a base; if none is given, uses the HTML element as a base."
  ([expr] (xpath (root-element) expr))
  ([base expr] (reify domina/DomContent
                 (nodes [_] (mapcat (partial select-nodes expr) (domina/nodes base)))
                 (single-node [_] (first (filter (complement nil?)
                                                 (map (partial select-node expr)
                                                      (domina/nodes base))))))))