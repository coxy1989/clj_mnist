(ns feedforward.core
  (:require 
    [clojure.core.matrix :as clj-mtrx]))

(defn pow [base exp]
  #?(:cljs (js/Math.pow base exp)
     :clj (.pow Math base exp)))

(defn sigmoid-activation-fn [value]
  "Return a real value in the range 0 to 1.
   The reciprocal of one plus e to the negative `value`th power
   `value` - a real valued number"
  (/ 1 (+ 1 (pow 2.71828182846 (- value)))))

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

