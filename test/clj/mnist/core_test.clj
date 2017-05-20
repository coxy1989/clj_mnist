(ns clj.mnist.core-test
  (:require 
    [clojure.test :refer :all]
    [clojure.core.matrix :as clj-mtrx]
    [clojure.core.matrix.random :as clj-mtrx-rnm]
    [clj.mnist.core :refer :all]))

(deftest test-inner-product 
  (testing "integration test - clj-mtrx/inner-product"
    (is (= [1] (clj-mtrx/inner-product [[1]] [1])))
    (is (= [3] (clj-mtrx/inner-product [[1 1]] [1 2])))
    (is (= [6] (clj-mtrx/inner-product [[1 1 1]] [1 2 3])))
    (is (= [1 1] (clj-mtrx/inner-product [[1][1]] [1])))
    (is (= [3 3] (clj-mtrx/inner-product [[1 1][1 1]] [1 2])))
    (is (= [5 6 7 8] (clj-mtrx/inner-product [[1 1 1 2][1 1 1 3][1 1 1 4][1 1 1 5]] [1 1 1 1])))
    (is (= (clj-mtrx/inner-product [[1]] [[1]]) [[1]]))
    (is (= (clj-mtrx/inner-product [[1 1]] [[1] [2]]) [[3]]))
    (is (= (clj-mtrx/inner-product [[1 1] [1 1]] [[1] [2]]) [[3][3]]))
    (is (= (clj-mtrx/inner-product [[1 1] [1 2]] [[1] [2]]) [[3][5]]))
    (is (= (clj-mtrx/inner-product [[1 1 1] [1 1 1]] [[1 1] [2 2] [3 3]]) [[6 6][6 6]]))
    (is (= (clj-mtrx/inner-product [[1 1 1] [1 1 1]] [[1 2] [2 3] [3 4]]) [[6 9][6 9]]))
    (is (= (clj-mtrx/inner-product [[1 2 1] [0 1 0] [2 3 4]] [[2 5] [6 7] [1 8]]) [[15 27] [6 7] [26 63]]))))

(deftest test-vector-add
  (testing "integration test - clj-mtrx/+"
    (is (= [3] (clj-mtrx/add [2] [1])))
    (is (= [3 3] (clj-mtrx/add [2 2] [1 1])))
    (is (= [8 3] (clj-mtrx/add [5 3] [3 0])))
    (is (= [[2 4 6] [8 10 12]] (clj-mtrx/add [[1 2 3] [4 5 6]] [[1 2 3] [4 5 6]])))))

(deftest test-sample-normal
  (testing "integration test - clj-mtrx-rnm/sample-normal"
    (is (= [3] (clj-mtrx/shape (clj-mtrx-rnm/sample-normal 3))))
    (is (= [4 5] (clj-mtrx/shape (clj-mtrx-rnm/sample-normal [4 5]))))))

(deftest test-gen-net
  (testing "gen-net"
    (let [net-1 (gen-net [1 2])]
      (is (= 1 (count net-1)))
      (is (= [2 1] (clj-mtrx/shape (:w (first net-1)))))
      (is (= [2] (clj-mtrx/shape (:b (first net-1))))))
    (let [net-2 (gen-net [3 4])]
      (is (= 1 (count net-2)))
      (is (= [4 3] (clj-mtrx/shape (:w (first net-2)))))
      (is (= [4] (clj-mtrx/shape (:b (first net-2))))))
    (let [net-3 (gen-net [4 6 5 ])]
      (is (= 2 (count net-3)))
      (is (= [6 4] (clj-mtrx/shape (:w (first net-3)))))
      (is (= [6] (clj-mtrx/shape (:b (first net-3)))))
      (is (= [5 6] (clj-mtrx/shape (:w (second net-3)))))
      (is (= [5] (clj-mtrx/shape (:b (second net-3))))))))

(deftest test-z 
  (testing "z"
    (is (= [4 4] (z [[1 1 1][1 1 1]] [1 1 1] [1 1])))))

(deftest test-feedforward-1 
  (testing "feedforward-1"
    (let [activations [1 1 1 1]
          weights [[1 1 1 1] [1 1 1 1] [1 1 1 1]]
          biases [1 1 1]
          ff1-value (feedforward-1 weights activations biases)]
      (is (not (nil? (:a ff1-value))))
      (is (not (nil? (:z ff1-value))))
      (is (= (count (:a ff1-value)) 3))
      (is (= (count (:z ff1-value)) 3)))))

(deftest test-feedforward-n
  (testing "feedforward-n"
      (is (= 2 (count (feedforward-n (gen-net [3 4 2]) [1 1 1] []))))
      (is (= 3 (count (feedforward-n (gen-net [3 4 2 4]) [1 1 1] []))))
      (is (= 3 (count (feedforward-n (gen-net [5 4 2 4]) [1 1 1 1 1] []))))))

(deftest test-bp-L
  (testing "bp-L"
    (is (= 3 (count (bp-L [0.5 0.5 0.5] [1 0 0] [0.25 0.25 0.25]))))))

(deftest test-bp-l
  (testing "bp-l"
    (is (= 3 (count (bp-l [[1 1 1][1 1 1]] [0.02 0.01] [0.25 0.5 0.1]))))))

(deftest test-bp-l-n
  (testing "bp-l-n"
    (is (= 1 (count (bp-l-n [[[1 1 1][1 1 1]]] [0.25 0.5] [[0.5 0.75 0.25]] '()))))
    (is (= [3] (clj-mtrx/shape (first (bp-l-n [[[1 1 1][1 1 1]]] [0.25 0.5] [[0.5 0.75 0.25]] '())))))
    (let [net (gen-net [5 10 8 2])
          l+1-weights-seq (map :w net)
          l+1-δ [0.2 0.5]
          ff-n (feedforward-n net [0.1 0.2 0.3 0.4 0.5] [])
          l-weighted-inputs-seq (drop-last (map :z ff-n))
          bp-l-n-v-list (bp-l-n l+1-weights-seq l+1-δ l-weighted-inputs-seq '())
          bp-l-n-v-vector (bp-l-n l+1-weights-seq l+1-δ l-weighted-inputs-seq [])]
      (is (= 2 (count bp-l-n-v-list)))
      (is (= 10 (count (first bp-l-n-v-list))))
      (is (= 8 (count (second bp-l-n-v-list))))
      (is (= 2 (count bp-l-n-v-vector)))
      (is (= 8 (count (first bp-l-n-v-vector))))
      (is (= 10 (count (second bp-l-n-v-vector)))))))

(deftest test-bp-w 
  (testing "bp-w"
    (is (= [3 5] (clj-mtrx/shape (bp-w [0.1 0.2 0.3] [0.1 0.2 0.3 0.4 0.5]))))))

(deftest test-bp-w-n
  (testing "bp-w-n"
    (let [bp-w-n-v (bp-w-n [[1 1 1] [1 1 1 1]] [[1 1 1 1 1] [1 1 1]])]
      (is (= 2 (count bp-w-n-v)))
      (is (= [3 5] (clj-mtrx/shape (first bp-w-n-v))))
      (is (= [4 3] (clj-mtrx/shape (second bp-w-n-v)))))))
   
(deftest test-backprop-sum-n 
  (testing "backprop-sum-n"
    (let [net (gen-net [6 5 2])
          b-seq (for [{:keys [w b]} net] (clj-mtrx/zero-vector (count b)))
          w-seq (for [{:keys [w b]} net] (apply clj-mtrx/zero-matrix (clj-mtrx/shape w)))
          t-data `([[0.1 0.2 0.3 0.4 0.5 0.6] [0 1]]
                   [[0.9 0.8 0.7 0.6 0.5 0.3] [1 0]])
          bp-sum-n-v (backprop-sum-n net t-data [b-seq w-seq])
          bp-1 (backprop net (first (first t-data)) (second (first t-data)))
          bp-2 (backprop net (first (second t-data)) (second (second t-data)))]
      (is (= [5 2] (map count (first bp-sum-n-v))))
      (is (= [[5 6] [2 5]] (map clj-mtrx/shape (second bp-sum-n-v))))
      (is (= (clj-mtrx/add (first bp-1) (first bp-2)) (first bp-sum-n-v)))
      (is (= (clj-mtrx/add (second bp-1) (second bp-2)) (second bp-sum-n-v))))))

(deftest test-descent-gradient
  (testing "descend-gradient"
    (is (= [0 0] (descend-gradient [1 1] [1 1] 1)))
    (is (= [0 2] (descend-gradient [1 2] [1 0] 1)))
    (is (= [[0 0] [0 0]] (descend-gradient [[1 1] [1 1]] [[1 1][1 1]] 1)))))

(deftest test-stocastic-gradient-descent
  (testing "stocastic-gradient-descent"
    (let [net (gen-net [6 5 2])
          b-seq (for [{:keys [w b]} net] (clj-mtrx/zero-vector (count b)))
          w-seq (for [{:keys [w b]} net] (apply clj-mtrx/zero-matrix (clj-mtrx/shape w)))
          t-data `([[0.1 0.2 0.3 0.4 0.5 0.6] [0 1]]
                   [[0.9 0.8 0.7 0.6 0.5 0.3] [1 0]])
          [nab-b nab-w] (backprop-sum-n net t-data [b-seq w-seq])
          sgd (sgd net nab-b nab-w 1)]
     (is (= 2 (count sgd))) 
     (is (= [5] (clj-mtrx/shape (:b(first sgd)))))
     (is (= [5 6] (clj-mtrx/shape (:w (first sgd)))))
     (is (= [2] (clj-mtrx/shape (:b(second sgd)))))
     (is (= [2 5] (clj-mtrx/shape (:w (second sgd))))))))

(deftest test-argmax 
  (testing "argmax"
    (is (= 1 (argmax [1 9 3 5 3 5 8])))
    (is (= 4 (argmax [0.1 0.09 0.0003 0.5 0.9 0.1 0.2])))))

(deftest test-evaluate 
  (testing "evaluate"
    (let [net (gen-net [6 5 2])
          t-data `([[0.1 0.2 0.3 0.4 0.5 0.6] [0 1]]
                   [[0.9 0.8 0.7 0.6 0.5 0.3] [1 0]])
          value (evaluate net t-data)]
      (is (number? value)))))

