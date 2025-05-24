(ns catan-predictor.centers
  (:require [catan-predictor.utils :as utils]))



(defn make-ring-area-centers
  [x y r]
  "This function calculate centers of hexagons on ring"
  (distinct
    (utils/round-seq
      (map (fn [k]
             [(+ x (* r (utils/fcos1 k))) (+ y (* r (utils/fsin1 k)))]) [0 1 2 3 4 5]
           ) 3))
  )

(defn make-centers
  [points]
  "This function make centers for full board"
  (distinct
    (utils/round-seq
      (mapcat #(make-ring-area-centers (first %) (second %) 1.732) points)
      3))
  )