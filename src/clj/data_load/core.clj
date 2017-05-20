(ns clj.data-load.core
  (:require 
    [clojure.core.matrix :as clj-mtrx]
    [clj.data-load.t10k-labels :as t10k-labels]
    [clj.data-load.t10k-images :as t10k-images]))

(defn training-data []
  (->> [t10k-images/t10k-images t10k-labels/t10k-labels]
       (apply map (fn [image-seq label-seq] [(clj-mtrx/array image-seq) (clj-mtrx/array label-seq)]))
       (split-at 8000)))

