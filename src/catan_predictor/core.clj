(ns catan-predictor.core
                                            (:gen-class)
                                            (:require [clojure.math :as m]
                                                      [quil.core :as q]
                                                      [catan-predictor.utils :as utils]
                                                      [catan-predictor.roads :as roads]
                                                      [catan-predictor.spots :as spots]
                                                      [catan-predictor.centers :as centers]
                                                      [catan-predictor.visualization :as vis]
                                                      ))



(def build-type #{"village" "road" "city"})
(def areas-types #{"forest" "pastures" "fields" "hills" "mountains"})



(def centers (centers/make-centers (centers/make-ring-area-centers 0.0 0.0 1.732)))

(def points (spots/make-spots-from-centers centers))

(def r (roads/roads points))

(vis/visualization-board r points centers)



(defn -main
  "I don't do a lot ... yet."
  [& args]
  (println "Hello, World!"))

