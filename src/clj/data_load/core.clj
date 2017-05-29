(ns data-load.core
  (:require 
    [clojure.core.matrix :as clj-mtrx]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]))

(defn gen-one-hot [digit]
  (-> (into [] (take 10 (repeat 0)))
      (assoc digit 1)
      (clj-mtrx/array)))

(defn parse-label [csv-label]
  (->> (read-string csv-label)
       int
       gen-one-hot))

(defn parse-image [csv-image]
  (clj-mtrx/array (map #(float (/ (read-string %) 255)) csv-image)))

(defn training-data [reader split-idx]
  (let [records (drop 1 (csv/read-csv reader))
        label-image-pairs (->> (for [r records] (let [[label & pixels] (seq r)] [(parse-image pixels) (parse-label label)])))]
    (split-at split-idx label-image-pairs)))

