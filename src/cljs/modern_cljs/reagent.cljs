(ns modern-cljs.reagent
  (:require [reagent.core :as r]
            [cljsjs.marked]))

(def data [{:id 1
            :author "Pete Hunt"
            :text "This is one comment"}
           {:id 2
            :author "Jordan Walke"
            :text "This is *another* comment"}])

(defn comment-component [author comment]
  [:div 
   [:h2 author]
   [:span {:dangerouslySetInnerHTML 
           #js {:__html (js/marked comment #js {:sanitize true})}}]])

(defn comment-list [comments]
  [:div
   (for [{:keys [id author text]} comments] 
     ^{:key id} [comment-component author text])])

(defn comment-form []
  [:div "Hello, world! I'm a comment-form"])

(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list comments]
   [comment-form]])
