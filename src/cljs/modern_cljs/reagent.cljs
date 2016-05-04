(ns modern-cljs.reagent
  (:require [reagent.core :as r :refer [render]]
            [domina.core :as dom :refer [by-id]]
            [clojure.string :as s :refer [trim blank?]]
            [cljsjs.marked]))

(def data (r/atom [{:id 1
                    :author "Pete Hunt"
                    :text "This is one comment"}
                   {:id 2
                    :author "Jordan Walke"
                    :text "This is *another* comment"}]))

(defn comment-component [author comment]
  [:div 
   [:h2 author]
   [:span {:dangerouslySetInnerHTML 
           #js {:__html (js/marked comment #js {:sanitize true})}}]])

(defn comment-list [comments]
  [:div
   (for [{:keys [id author text]} @comments] 
     ^{:key id} [comment-component author text])])

(defn handle-comment-on-click [comments comment]
  (let [author (trim (:author @comment))
        text (trim (:text @comment))]
    (reset! comment {:author "" :text ""})
    (when-not (or (blank? author) (blank? text))
      (swap! comments conj {:id (.getTime (js/Date.)) :author author :text text}))))

(defn comment-form [comments]
  (let [comment (r/atom {:author "" :text ""})] 
    (fn [] 
      [:form
       [:input {:type "text"
                :placeholder "Your name"
                :value (:author @comment)
                :on-change #(swap! comment assoc :author (-> %
                                                             .-target
                                                             .-value))}]
       [:input {:type "text"
                :placeholder "Say something"
                :value (:text @comment)
                :on-change #(swap! comment assoc :text (-> %
                                                           .-target
                                                           .-value))}]
       [:input {:type "button"
                :value "Post"
                :on-click #(handle-comment-on-click comments comment)}]])))

(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list comments]
   [comment-form comments]])

(defn ^export init []
  (render [comment-box data] (by-id "content")))
