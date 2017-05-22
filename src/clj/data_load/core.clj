(ns data-load.core
  (:require 
    [clojure.core.matrix :as clj-mtrx]
    [data-load.t10k-labels :as t10k-labels]
    [data-load.t10k-images :as t10k-images]))

(defn training-data []
  (->> [t10k-images/t10k-images t10k-labels/t10k-labels]
       (apply map (fn [image-seq label-seq] [(clj-mtrx/array image-seq) (clj-mtrx/array label-seq)]))
       (split-at 8000)))

