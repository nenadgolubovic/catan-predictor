(ns catan-predictor.centers
  (:require [catan-predictor.utils :as utils]))

(defn centers [points x y]
  "Centers will be every spot approximately 2 far away from the given [x y] point"
  (let [target-distance 2.000]
    (concat
      [[x y]]
      (filter
        #(= (utils/distance (- (second (first %)) x) (- (first (first %)) y) 3)
            target-distance) points))
    ))

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