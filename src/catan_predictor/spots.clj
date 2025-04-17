(ns catan-predictor.spots
  (:require [catan-predictor.utils :as utils]))

(defn spots [[x y] ks]
  "I will make function which make hexagon area"
  (distinct
    (utils/round-seq
      (map (fn [k]
             [(+ x (utils/fcos k)) (+ y (utils/fsin k))])
           ks) 3)))

(defn make-spots-from-centers
  [centers]
  "This function makes spots of hexagons from provided centers"
  (distinct
    (utils/round-seq
      (mapcat #(spots [(first %) (second %)] [0 1 2 3 4 5])
              centers)
      3)))

(spots [0.0 0.0] [0 1 2 3 4 5])
(make-spots-from-centers (spots [0.0 0.0] [0 1 2 3 4 5 ] ))

;Spots should have rang [0,1,2] play


(defn upgrade-spot )