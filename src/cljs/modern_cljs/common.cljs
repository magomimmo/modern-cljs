(ns modern-cljs.common)

;;; :export init function to protect it from renaming it by Google
;;; Closure Compiler  :simple and :advanded optimization options
(defn ^:export init [form-id onload-fn]
  ;; verity that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get the form-id by element id and set its onload property to
    ;; onload-fn function
    (let [form (.getElementById js/document form-id)]
      (set! (.-onsubmit form) onload-fn))))
