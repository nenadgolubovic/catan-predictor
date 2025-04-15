(ns catan-predictor.spots
  (:require [catan-predictor.utils :as utils]))

(defn spots [[x y] ks]
  "I will make function which make hexagon area"
  (map (fn [k]
         [(+ x (utils/fcos k)) (+ y (utils/fsin k))])
       ks))

(defn make-spots-from-centers
  [centers]
  "This function makes spots of hexagons from provided centers"
  (distinct
    (utils/round-seq
      (mapcat #(spots [(first %) (second %)] [0 1 2 3 4 5])
              centers)
      3)))