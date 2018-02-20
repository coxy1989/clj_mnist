(ns app.core
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [feedforward.core :as ff]
    [ajax.core :as ajax]
    [rum.core :as rum]
    [cljs.core.async :as async]))

;; CANVAS ---------------------------------------------------------------------------------------------------

(defn gen-pixel-vector
  "TODO: docstring"
  [state]
  (let [drawing @(:drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        data (.-data (.getImageData context 0 0 28 28))
        alpha-vec (take-nth 4 (drop 3 (.from js/Array data)))]
    (for [pix alpha-vec] (/ pix 255))))

(defn gen-draw-state
  "TODO: docstring"
  [state e d]
  (let [previous (:latest @(:drawing state))
        latest {:x (.. e -nativeEvent -pageX) :y (.. e -nativeEvent -pageY)}]
      {:previous previous 
       :latest latest}))

(defn draw!
  "TODO: docstring"
  [state]
  (let [drawing @(:drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        mv-x (- (get-in drawing [:previous :x]) (.-offsetLeft canvas))
        mv-y ( - (get-in drawing [:previous :y]) (.-offsetTop canvas))
        ln-x (- (get-in drawing [:latest :x]) (.-offsetLeft canvas))
        ln-y (- (get-in drawing [:latest :y]) (.-offsetTop canvas))]
    (.moveTo context mv-x mv-y)
    (.lineTo context ln-x ln-y)
    (.stroke context)))

(rum/defcs canvas-component
  < (rum/local {} :drawing)
  {:did-update (fn [state] (if (= true (:mouse-down @(:drawing state))) (do (draw! state) state) state))}
  "TODO: docstring"
  [state chan]
  [:div#canvas-cont
   [:canvas#canvas {:ref "canvas"
             :width 28
             :height 28
             :on-mouse-down #(swap! (:drawing state) merge {:mouse-down true} (gen-draw-state state %1 %2))
             :on-mouse-up #(swap! (:drawing state) assoc :mouse-down false)
             :on-mouse-move #(swap! (:drawing state) merge (gen-draw-state state %1 %2))}]
   [:button#go-button {:on-click #(async/put! chan (gen-pixel-vector state))} "Go"]])

(rum/defc results-component < rum/reactive
  "TODO: docstring"
  [state]
  (let [results (rum/react state)]
    (if (empty? results)
     [:div]
     [:div#results-cont
      [:table
       [:tbody
       [:tr [:th "Digit"] [:th "Rank"]]
       (for [[digit value] results] [:tr {:key (str "idx-" digit)}  [:td digit] [:td value]])]]])))

(rum/defc top-level-component
  "TODO: docstring"
  [canvas-chan results-atom]
  [:div#top-level-container
   (canvas-component canvas-chan)
   (results-component results-atom)])

;; DATA ------------------------------------------------------------------------------------------------------

(defn pixel-vector->classification
  "TODO: docstring"
  [net pixel-vector]
  (->> (ff/feedforward-n net pixel-vector [])
       (last)
       (:a)
       (map-indexed vector)
       (sort-by second)
       (reverse)))

(defn process-canvas-chan
  "TODO: docstring"
  [canvas-chan results-atom]
  (let [data-chan (async/chan)]
    (ajax/GET "net.json" :keywords? true :response-format :json :handler #(async/put! data-chan %))
    (go (let [net (<! data-chan)]
        (go (loop [] (let [pixel-vector (<! canvas-chan)]
                       (reset! results-atom (pixel-vector->classification net pixel-vector)))(recur)))))))

;; ENTRY POINT -----------------------------------------------------------------------------------------------

(defn ^:export inject
  "TODO: docstring"
  [dom-id]
  (let [injection-site (.getElementById js/document dom-id)
        canvas-chan (async/chan)
        results-atom (atom [])]
    (process-canvas-chan canvas-chan results-atom)
    (rum/mount (top-level-component canvas-chan results-atom) injection-site)))

