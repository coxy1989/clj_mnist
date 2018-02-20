(comment 

(deftask run-tests
  "Run the tests"
  []
  (merge-env! :source-paths #{"test/clj"})
  (test))

(deftask auto-test
  "Watch and run the tests when there is a change"
  []
  (comp
    (watch)
    (run-tests))))



(comment 

;; DRAW

(defn gen-pixel-vector
  "TODO: docstring"
  [state]
  (let [drawing @(:drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        data (.-data (.getImageData context 0 0 28 28))
        alpha-vec (take-nth 4 (drop 3 (.from js/Array data)))]
    (for [pix alpha-vec] (/ pix 255))))

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

(defn should-draw?
  "TODO: docstring"
  [state]
  (= true (:mouse-down @(:drawing state))))

(defn gen-draw-state
  "TODO: docstring"
  [state e d]
  (let [previous (:latest @(:drawing state))
        latest {:x (.. e -nativeEvent -pageX) :y (.. e -nativeEvent -pageY)}]
      {:previous previous 
       :latest latest}))

(defn on-mouse-up
  "TODO: docstring"
  [state]
  #(swap! (:drawing state) assoc :mouse-down false))

(defn on-mouse-down
  "TODO: docstring"
  [state]
  #(swap! (:drawing state) merge {:mouse-down true} (gen-draw-state state %1 %2)))

(defn on-mouse-move
  "TODO: docstring"
  [state]
  #(swap! (:drawing state) merge (gen-draw-state state %1 %2)))

;; RESULTS

(defn results-template
  "TODO: docstring"
  [results]
  [:div (for [[digit value] results] [:p (str digit " - " value)])])

(defn empty-results-template
  "TODO: docstring"
  []
  [:div "enter a digit and click evaluate"])

(rum/defc component < rum/reactive 
  "TODO: docstring"
  [state]
  (let [results (rum/react state)]
    (if (empty? results) 
     (empty-results-template) 
     (results-template results))))

;; CANVAS

(defn canvas-did-update
  "TODO: docstring"
  [state]
  (if (draw-util/should-draw? state)
    (do (draw-util/draw! state) state)
    state))

(defn canvas-element
  "TODO: docstring"
  [state]
  [:canvas {:ref "canvas"
            :width 28
            :height 28
            :style {:cursor "crosshair"
                    :background-color "blue"}
            :on-mouse-down (draw-util/on-mouse-down state)
            :on-mouse-up (draw-util/on-mouse-up state)
            :on-mouse-move (draw-util/on-mouse-move state)}])

(rum/defcs component
  < (rum/local {} :drawing)
  {:did-update canvas-did-update}
  "TODO: docstring"
  [state chan]
  [:div 
   (canvas-element state)
   [:button {:on-click #(async/put! chan (draw-util/gen-pixel-vector state))} "Evaluate"]])


;; ENTRYPOINT

(rum/defc page
  "TODO: docstring"
  [canvas results]
  [:div canvas results])

(defn process-pixel-vector
  "TODO: docstring"
  [results-atom net pixel-vector]
  (->> (ff/feedforward-n net pixel-vector [])
       last
       :a
       (map-indexed vector)
       (sort-by second)
       reverse
       (reset! results-atom)))

(defn process-get-net-chan 
  "TODO: docstring"
  [net-chan dom-id]
  (go-loop [] (let [net (<! net-chan)] (let [element (.getElementById js/document dom-id)
                                             results-atom (atom [])
                                             pixel-vector-chan (async/chan)]
                                         (go-loop [] (let [pixel-vector (<! pixel-vector-chan)] (process-pixel-vector results-atom net pixel-vector)) (recur))
                                         (rum/mount (page (canvas-ui/component pixel-vector-chan) (results-ui/component results-atom)) element)))))

(defn ^:export inject
  "TODO: docstring"
  [dom-id]
  (let [net-chan (async/chan)]
    (process-get-net-chan net-chan dom-id)
    (ajax/GET "/net.json"
              :keywords? true
              :response-format :json
              :handler (fn [net] (async/put! net-chan net))))))
