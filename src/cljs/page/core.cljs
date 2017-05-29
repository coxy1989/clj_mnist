(ns page.core
  (:require-macros 
    [macros.go-loop-let.core :refer [go-loop-let-recur go-loop-let]]
    [cljs.core.async.macros :refer [go go-loop]])    
  (:require
    [page.ui :as ui]
    [ajax.core :as ajax]
    [feedforward.core :as ff]
    [rum.core :as rum]
    [cljs.core.async :as async]))
  
(defn process-pixel-vector-chan [net pixel-vector-chan]
  (go-loop-let-recur [pixel-vector (<! pixel-vector-chan)] (->> (ff/feedforward-n net pixel-vector [])
                                                                last
                                                                :a
                                                                (map-indexed vector)
                                                                (sort-by second)
                                                                reverse
                                                                (clj->js)
                                                                (.log js/console))))

(defn get-net [chan]
  (ajax/GET "/net.json" :keywords? true :response-format :json :handler (fn [net] (async/put! chan net))))

(defn ^:export inject [dom-id]  
  (let [element (.getElementById js/document dom-id)
        net-chan (async/chan)
        pixel-vector-chan (async/chan)]
    (get-net net-chan)
    (go-loop-let [net (<! net-chan)] (do (process-pixel-vector-chan net pixel-vector-chan)
                                         (rum/mount (ui/component pixel-vector-chan) element)))))

