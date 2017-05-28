(ns page.ui
  (:require
    [rum.core :as rum]))
  
(defn should-draw? [state]
  (= true (:mouse-down @(::drawing state))))

(defn draw [state]
  (let [drawing @(::drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        mv-x (- (get-in drawing [::previous ::x]) (.-offsetLeft canvas))
        mv-y ( - (get-in drawing [::previous ::y]) (.-offsetTop canvas))
        ln-x (- (get-in drawing [::latest ::x]) (.-offsetLeft canvas))
        ln-y (- (get-in drawing [::latest ::y]) (.-offsetTop canvas))]
    (.moveTo context mv-x mv-y)
    (.lineTo context ln-x ln-y)
    (.stroke context)))

(defn did-update [state]
  (if (should-draw? state)
    (do (draw state) state)
    state))

(defn state-patch [state e d]
  (let [previous (::latest @(::drawing state))
        latest {::x (.. e -nativeEvent -pageX) ::y (.. e -nativeEvent -pageY)}]
      {::previous previous 
       ::latest latest}))


(defn canvas-element [state]
  [:canvas {:ref "canvas"
            :width 28
            :height 28
            :style {:cursor "crosshair"
                    :background-color "blue"}
            :on-mouse-down #(swap! (::drawing state) merge {:mouse-down true} (state-patch state %1 %2))
            :on-mouse-up #(swap! (::drawing state) assoc :mouse-down false)
            :on-mouse-move #(swap! (::drawing state) merge (state-patch state %1 %2))}])

(defn evaluate [state]
  (let [drawing @(::drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        data (.-data (.getImageData context 0 0 28 28))
        alpha-vec (take-nth 4 (drop 3 (.from js/Array data)))
        norm-alpha-vec (for [pix alpha-vec] (/ pix 255))]
    (.log js/console (clj->js norm-alpha-vec))))

(rum/defcs component < (rum/local {} ::drawing) {:did-update did-update} [state]
  [:div 
   (canvas-element state)
   [:button {:on-click #(evaluate state)} "Evaluate"]])

