(ns components.canvas.ui
  (:require
    [util.draw.core :as draw-util]
    [cljs.core.async :as async]
    [rum.core :as rum]))
  
(defn canvas-did-update [state]
  (if (draw-util/should-draw? state)
    (do (draw-util/draw! state) state)
    state))

(defn canvas-element [state]
  [:canvas {:ref "canvas"
            :width 28
            :height 28
            :style {:cursor "crosshair"
                    :background-color "blue"}
            :on-mouse-down (draw-util/on-mouse-down state)
            :on-mouse-up (draw-util/on-mouse-up state)
            :on-mouse-move (draw-util/on-mouse-move state)}])

(rum/defcs component < (rum/local {} :drawing) {:did-update canvas-did-update} [state chan]
  [:div 
   (canvas-element state)
   [:button {:on-click #(async/put! chan (draw-util/gen-pixel-vector state))} "Evaluate"]])

