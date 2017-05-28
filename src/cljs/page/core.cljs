(ns page.core
  (:require
    [rum.core :as rum]
    [page.ui :as ui]))
  
(defn ^:export inject [dom-id]  
  (let [element (.getElementById js/document dom-id)]
    (rum/mount (ui/component) element)))

