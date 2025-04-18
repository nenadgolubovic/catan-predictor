(ns catan-predictor.core
                                            (:gen-class)
                                            (:require [clojure.math :as m]
                                                      [quil.core :as q]
                                                      [catan-predictor.utils :as utils]
                                                      [catan-predictor.roads :as roads]
                                                      [catan-predictor.spots :as spots]
                                                      [catan-predictor.centers :as centers]
                                                      [catan-predictor.visualization :as vis]
                                                      [catan-predictor.area :as area]
                                                      ))



(def build-type #{"village" "road" "city"})
(def areas-types #{"forest" "pastures" "fields" "hills" "mountains"})



(def centers (centers/make-centers (centers/make-ring-area-centers 0.0 0.0 1.732)))

(def points (spots/make-spots-from-centers centers))

(def r (roads/roads points))
(def resources (atom ["wool" "wool" "wool" "wool"
                      "brick" "brick" "brick"
                      "wood" "wood" "wood" "wood"
                      "ore" "ore" "ore"
                      "grain" "grain" "grain" "grain"
                      "dust"
                      ]))

(def numbers (atom [2 3 3 4 4 5 5 6 6 8 8 9 9 10 10 11 11 12 ]))

(def areas (area/create-areas-from-centers points centers resources numbers))
(vis/visualization-board r points centers areas)



(defn -main
  "I don't do a lot ... yet."
  [& args]
  (println "Hello, World!"))

