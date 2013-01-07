(ns modern-cljs.epoch
  (:require [domina :as dom]
            [domina.events :as ev]))


(defn update-duration []
  (let [now (Date.)
        message  (str (.getTime now) " seconds since epoch. (mouseover to update)")]
    (dom/set-text! (dom/by-id "output") message)))

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (let [output (dom/by-id "output")]
      (set! (.-onmouseover output) update-duration))))
;      (ev/listen! output :mouseover update-duration))))
