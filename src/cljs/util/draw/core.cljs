(ns util.draw.core)

(defn gen-pixel-vector [state]
  (let [drawing @(:drawing state)
        canvas (aget (:rum/react-component state) "refs" "canvas")
        context (.getContext canvas "2d")
        data (.-data (.getImageData context 0 0 28 28))
        alpha-vec (take-nth 4 (drop 3 (.from js/Array data)))]
    (for [pix alpha-vec] (/ pix 255))))

(defn draw! [state]
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

(defn should-draw? [state]
  (= true (:mouse-down @(:drawing state))))

(defn gen-draw-state [state e d]
  (let [previous (:latest @(:drawing state))
        latest {:x (.. e -nativeEvent -pageX) :y (.. e -nativeEvent -pageY)}]
      {:previous previous 
       :latest latest}))

(defn on-mouse-up [state]
  #(swap! (:drawing state) assoc :mouse-down false))

(defn on-mouse-down [state]
  #(swap! (:drawing state) merge {:mouse-down true} (gen-draw-state state %1 %2)))

(defn on-mouse-move [state]
  #(swap! (:drawing state) merge (gen-draw-state state %1 %2)))

