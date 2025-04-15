(ns catan-predictor.roads
(:require [catan-predictor.utils :as utils]))


(defn roads
  [points]
  "Def pairs of spots which make a road, distance is 1 between 2 spots always"
  (mapcat (fn [n1]
            (map (fn [n2] [n1 n2]) (filter #(= 1.000 (utils/distance-1-2 n1 %)) points)))
          points))
