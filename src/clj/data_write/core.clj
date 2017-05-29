(ns data-write.core
  (:require [clojure.data.json :as json]))

(defn write-to-json [data file]
  (->> data 
       json/write-str
       (spit file)))

