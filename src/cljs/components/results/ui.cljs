(ns components.results.ui
  (:require [rum.core :as rum]))

(defn results-template [results]
  [:div (for [[digit value] results] [:p (str digit " - " value)])])

(defn empty-results-template []
  [:div "enter a digit and click evaluate"])

(rum/defc component < rum/reactive [state]
  (let [results (rum/react state)]
    (if (empty? results) 
     (empty-results-template) 
     (results-template results))))

