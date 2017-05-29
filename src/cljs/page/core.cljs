(ns page.core
  (:require-macros 
    [cljs.core.async.macros :refer [go go-loop]])    
  (:require
    [components.canvas.ui :as canvas-ui]
    [components.results.ui :as results-ui]    
    [feedforward.core :as ff]
    [ajax.core :as ajax]
    [rum.core :as rum]
    [cljs.core.async :as async]))
  
(rum/defc page [canvas results]
  [:div canvas results])

(defn process-pixel-vector [results-atom net pixel-vector]
  (->> (ff/feedforward-n net pixel-vector [])
       last
       :a
       (map-indexed vector)
       (sort-by second)
       reverse
       (reset! results-atom)))

(defn process-get-net-chan [net-chan dom-id]
  (go-loop [] (let [net (<! net-chan)] (let [element (.getElementById js/document dom-id)
                                             results-atom (atom [])
                                             pixel-vector-chan (async/chan)]
                                         (go-loop [] (let [pixel-vector (<! pixel-vector-chan)] (process-pixel-vector results-atom net pixel-vector)) (recur))
                                         (rum/mount (page (canvas-ui/component pixel-vector-chan) (results-ui/component results-atom)) element)))))

(defn ^:export inject [dom-id]  
  (let [net-chan (async/chan)]
    (ajax/GET "/net.json" :keywords? true :response-format :json :handler (fn [net] (async/put! net-chan net)))
    (process-get-net-chan net-chan dom-id)))

