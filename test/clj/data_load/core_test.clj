(ns data-load.core-test
  (:require 
    [data-load.core :as data-load]
    [clojure.test :refer :all]))

(deftest test-gen-one-hot []
  (testing "gen-one-hot"
    (is (= [1 0 0 0 0 0 0 0 0 0] (into [] (data-load/gen-one-hot 0))))
    (is (= [0 1 0 0 0 0 0 0 0 0] (into [] (data-load/gen-one-hot 1))))
    (is (= [0 0 1 0 0 0 0 0 0 0] (into [] (data-load/gen-one-hot 2))))
    (is (= [0 0 0 1 0 0 0 0 0 0] (into [] (data-load/gen-one-hot 3))))
    (is (= [0 0 0 0 1 0 0 0 0 0] (into [] (data-load/gen-one-hot 4))))
    (is (= [0 0 0 0 0 1 0 0 0 0] (into [] (data-load/gen-one-hot 5))))
    (is (= [0 0 0 0 0 0 1 0 0 0] (into [] (data-load/gen-one-hot 6))))
    (is (= [0 0 0 0 0 0 0 1 0 0] (into [] (data-load/gen-one-hot 7))))
    (is (= [0 0 0 0 0 0 0 0 1 0] (into [] (data-load/gen-one-hot 8))))
    (is (= [0 0 0 0 0 0 0 0 0 1] (into [] (data-load/gen-one-hot 9))))))

(deftest test-parse-label []
  (testing "parse-label"
    (is (= [1 0 0 0 0 0 0 0 0 0] (into [] (data-load/parse-label "0"))))
    (is (= [0 0 0 0 0 0 0 0 0 1] (into [] (data-load/parse-label "9"))))))

(deftest test-parse-image []
  (testing "parse-image"
    (is (= [0.0 0.0 1.0] (into [] (data-load/parse-image ["0" "0" "255"]))))))

