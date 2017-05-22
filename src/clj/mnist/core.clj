(ns mnist.core
  (:gen-class)
  (:require 
    [data-load.core :as data-load]
    [clojure.math.numeric-tower :as math]
    [clojure.core.matrix.random :as clj-mtrx-rnm]
    [clojure.core.matrix :as clj-mtrx]))

;; Network Representation

(defn gen-layer [m n]
  "Return a map of the form {:w [[]] :b [[]]} where:
   `w` maps to a matrix of shape (m, n)
   `b` maps to a vector of length m"
  {:w (clj-mtrx-rnm/sample-normal [m n])
   :b (clj-mtrx-rnm/sample-normal m)})

(defn gen-net [layer-size-seq]
  "Return a seq of maps of the form returned by `gen-layer`
   Considers first item in `layer-size-seq` as the net's input layer
  `layer-size-seq` - vector of integers where the first entry represents the number of inputs to the net, subsequent
                     entries represent the number of neurons in the layer at that entry's index"
 (let [t (drop-last layer-size-seq)
       b (drop 1 layer-size-seq)
       z (map vector t b)]
   (for [[n m] z] (gen-layer m n))))

;; Forward Pass

(defn sigmoid-activation-fn [value]
  "Return a real value in the range 0 to 1.
   The reciprocal of one plus e to the negative `value`th power
   `value` - a real valued number"
  (/ 1 (+ 1 (math/expt 2.71828182846 (- value)))))

(defn z [weights activations biases]
  "Return `z` = (wa+b) - vector of length p
  `weights` - matrix of shape (p, m) of the form [[w11 w12 w13..][w21 w22 w23..][w31 w32 w33..]]
  `activations` - vector of length m
  `biases` - vector of length p"
  (clj-mtrx/add (clj-mtrx/inner-product weights activations) biases))

(defn feedforward-1 [weights activations biases]
  "Return a map with keys `:z` and `:a` where:
   The value for :z is wa+b - vector of length m 
   The value for :a is σ(wa+b) - vector of length m
   `weights` - matrix of shape (m, n)
   `activations` - vector of length n
   `biases` - vector of length m" 
  (let [z-value (z weights activations biases)]
    {:z z-value
     :a (map sigmoid-activation-fn z-value)}))

(defn feedforward-n [layers-seq activations roll]
  "Return a seq of maps of the form returned by `feedforward-1`
  `layers-seq` - seq of maps of the form returned by `gen-layer`
  `activations` - vector with length equal to the second dim of the value for `:w` of the first item in layers-seq
  `roll` - expects an empty vector, recursively accumulates the result"
  (if (empty? layers-seq)
    roll
    (let [{:keys [w b]} (first layers-seq)
          {:keys [z a] :as ff} (feedforward-1 w activations b)]
      (feedforward-n (drop 1 layers-seq) a (conj roll ff)))))

;; Backward Pass

(defn sigmoid-activation-fn-derivative [value]
  "The derivative of `sigmoid-activation-fn`
   `value` - a real valued number"
  (let [sig-v (sigmoid-activation-fn value)]
    (* sig-v (- 1 sig-v))))

(defn mean-squared-error-derivative [activations desireds]
  "The derivative of the mean-squared-error
   `activations` - a vector of length m
   `desireds` - a vector of length m"
  (clj-mtrx/sub activations desireds))

(defn bp-L [output-activations desired-activations weighted-inputs]
  "Return the vector of δ values for the output layer 
  `output-activations` - vector of length m 
  `desired-activations` - vector of length m 
  `weighted-inputs` - vector of length m"
    (->> (mean-squared-error-derivative output-activations desired-activations)
         (clj-mtrx/emul (map sigmoid-activation-fn-derivative weighted-inputs))))

(defn bp-l [l+1-weights l+1-δ l-weighted-inputs]
  "Return the vector of δ values for the (non-output) layer l
  `l+1-weights` - matrix of shape (m,n) 
  `l+1-δ` - vector of length m 
  `l-weighted-inputs` - vector of length m"
  (->> (clj-mtrx/inner-product (clj-mtrx/transpose l+1-weights) l+1-δ)
       (clj-mtrx/emul (map sigmoid-activation-fn-derivative l-weighted-inputs))))

(defn bp-l-n [l+1-weights-seq l+1-δ l-weighted-inputs-seq roll]
  "Return a seq of vectors of δ values for the (non-output) layers represented by `weighted-inputs-seq`
   Expects `weighted-inputs-seq` to be in input to output order with respect to the network (wrttn)
   Return value is in input to output order (wrttn) if the value of `roll` is a list
   Return value is in output to input order (wrttn) if the value of `roll` is a vector
   `l+1-weights-seq` - seq of matrices of the form expected in the `l+1-weights` param of the `bp-l` fn
   `l+1-δ` - vector with length equal to the first dim of the value for the key :w in the last element of `l+1-weights-seq`
   `l-weighted-inputs-seq` - seq of vectors of the form expected in the `l-wighted-inputs` param of the `bp-l` fn
   `roll` - expects an empty list or vector, recursively accumulates the result"
  (if (empty? l-weighted-inputs-seq)
    roll
    (let [bp-l-v (bp-l (last l+1-weights-seq) l+1-δ (last l-weighted-inputs-seq))]
      (bp-l-n (drop-last l+1-weights-seq) bp-l-v (drop-last l-weighted-inputs-seq) (conj roll bp-l-v)))))

(defn bp-w [δ-l activations-l-1]
  "Return the matrix of ∇ values of shape (m, p) for the weights matrix in layer `l`
  `δ-l` - vector of length m 
  `activations-l-1` - vector of length p" 
  (->> (clj-mtrx/transpose (clj-mtrx/column-matrix activations-l-1))
       (clj-mtrx/inner-product (clj-mtrx/column-matrix δ-l))))

(defn bp-w-n [δ-l-seq activations-l-1-seq]
  "Return a seq of matrices of the form returned by the `bp-w` fn
   Return value is in input -> output or output -> input order (wrttn) depending on the order of the seqs passed as params
   `δ-l-seq` - seq of vectors of the form expected by the `bp-w` fn
   `activations-l-1-seq` - seq of vectors of the form expected by the `bp-w` fn"
  (for [[δ-l activations-l-1] (map vector δ-l-seq activations-l-1-seq)] (bp-w δ-l activations-l-1)))

(defn backprop [net image label]
  "Return a 2-tuple such that:
   The first item is a seq of vectors containing the layer-wise gradients of the cost function w.r.t the bias
   The second item is a seq of matrices containing the layer-wise gradients of the cost function w.r.t the weights
   Both the seqs are in input -> output order (wrttn)
   `net` - seq of the form returned by `gen-net` fn
   `image` - vector of real numbers of length equal to the first dim of the value of :w and :b of the first item of `net`
   `label` - vector of real numbers of length equal to the first dim of the value of :w and :b of the last item of `net`"
  (let [ff (feedforward-n net image [])
        bp-L-v (bp-L (:a (last ff)) label (:z (last ff)))
        bp-l-n-v (bp-l-n (map :w net) bp-L-v (drop-last (map :z ff)) '())
        bp-w-n-v (bp-w-n (concat bp-l-n-v [bp-L-v]) (concat [image] (drop-last (map :a ff))))]
    [(concat bp-l-n-v [bp-L-v]) bp-w-n-v]))

(defn backprop-sum-n [net t-data-seq [b-seq w-seq :as roll]]
  "Return a 2-tuple, such that:
   The first item is a seq of vectors containing the sums of the layer-wise gradients of the cost function w.r.t the
   bias - the vectors are of equal dim to the vectors of `b-seq`
   The second item is a seq of matrices containing the sums of the layer-wise gradients of the cost function w.r.t
   the weights - the matrices are of equal dim to the matrices of `w-seq`
   `net` - seq of maps of the from returned by the `gen-net` fn
   `t-data-seq` - seq of 2-tuples of the form [input-activation-vector desired-output-vector]
   `b-seq` - seq of vectors of dim equal to `:b` values of maps in `net` - recursively accumulates result
   `w-seq` - seq of matrices of dim equal to `:w` values of maps in `net` - recursively accumulates result"
  (if (empty? t-data-seq)
    roll
  (let [[image label] (first t-data-seq)
        [nabla-b-seq nabla-w-seq] (backprop net image label)]
    (backprop-sum-n net (drop 1 t-data-seq) [(map clj-mtrx/add b-seq nabla-b-seq) (map clj-mtrx/add w-seq nabla-w-seq)]))))

;; Stocastic Gradient Descent

(defn descend-gradient [tensor gradient learning-rate]
  "Return a matrix or vector of equal dim to `tensor`
   Performs elementwise subtraction of `tensor` by `learning-rate` multiplied by `gradient`
   `tensor` - vector or matrix
   `gradient` - vector or matrix of equal dim to `tensor`
   `learning-rate` - scalar value"
  (clj-mtrx/sub tensor (clj-mtrx/mul learning-rate gradient)))

(defn sgd [net bp-b-seq bp-w-seq learning-rate]
  "Return a seq of maps of the form returned by `gen-net`"
  (map (fn [{:keys [w b]} nabla-b nabla-w] {:b (descend-gradient b nabla-b learning-rate)
                                            :w (descend-gradient w nabla-w learning-rate)}) net bp-b-seq bp-w-seq))

;; Evaluate

(defn argmax [check-seq]
  (.indexOf check-seq (apply max check-seq)))

(defn evaluate [net test-data]
  (->> (for [[image label] test-data] [(argmax (:a (last (feedforward-n net image [])))) (argmax label)])
       (filter (fn [[res label]] (= res label)))
       (count)))

;; Run!

(defn run-epoch [net batches batch-learning-rate]
  (if (= (mod (count batches) 100) 0)
    (println (str "Running Batch: " (count batches))))
  (if (= 0 (count batches))
    net
    (let [t-data (first batches)
          b-seq (for [{:keys [w b]} net] (clj-mtrx/zero-vector (count b)))
          w-seq (for [{:keys [w b]} net] (apply clj-mtrx/zero-matrix (clj-mtrx/shape w)))
          [bp-sum-b bp-sum-w] (backprop-sum-n net t-data [b-seq w-seq])
          next-net (sgd net bp-sum-b bp-sum-w batch-learning-rate)]
     (run-epoch next-net (drop 1 batches) batch-learning-rate))))

(defn run-epochs [net epoch-number training-data test-data]
  (if (= 0 epoch-number)
    net
  (let [batch-size 10 
        learning-rate 3
        batches (partition batch-size (shuffle training-data))
        batch-learning-rate (/ learning-rate batch-size)
        next-net (run-epoch net batches batch-learning-rate)]
    (println (str "---------------"))
    (println (str "Epoch: " epoch-number))
    (println (str "Correct: " (evaluate next-net test-data) " / " (count test-data)))    
    (println (str "---------------"))    
    (run-epochs next-net (- epoch-number 1) training-data test-data))))

(defn run []
  (let [[training-data test-data] (data-load/training-data)
        epochs 30
        net (gen-net [784 30 10])]
    (println "Starting Run")
    (println (str "Length of training data: " (count training-data)))
    (println (str "Length of test data: " (count test-data)))
    (println "Evaluating Start Accuracy")
    (println (str "Correct: " (evaluate net test-data) " / " (count test-data)))    
    (println (str "---------------"))    
    (run-epochs net epochs training-data test-data)))

(defn -main
  "Entry point"
  [& args]
  (run))

