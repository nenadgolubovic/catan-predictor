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

(def recourses #{"wool" "brick" "wood" "ore" "grain"})
(def development-card #{"knight" "victory-point" "road-building" "monopoly" "year-of-plenty"})
(def build-type #{"village" "road" "city"})
(def areas-types #{"forest" "pastures" "fields" "hills" "mountains"})



(def centers (centers/make-centers (centers/make-ring-area-centers 0.0 0.0 1.732)))

(def points (spots/make-spots-from-centers centers))

(def r (roads/roads points))

(vis/visualization-board r points centers)










;; I will make hash-map where I will present [area spot] as key and [area spot] as connected area
;; I am trying to automatic update area1 and area2, because spot2 in area1 is equivalent
;; spot6 in area2 and spot3 in area1 is equivalent spot5 in area2
;; for now I will only make when area1 has changed that automatically area2 change

(defn upload-connected-area [[area1 spot1] [area2 spot2]]
  (swap! area2 assoc spot2 (@area1 spot1))
  )

(defn upgrade-village
  [area spot player]
  ;;add village on one spot of area
  ;; upgrade, add building type
  ;; integrate blocking spot, if some spot is not nil,onliest than it could be change
  (let [current-value-of-spot (get @area spot)]
    (if (nil? current-value-of-spot)
      (swap! area assoc spot {:type-of-building "village" :player player})
      nil)
    ))
(defn upgrade-town
  [area spot]
  "Only upgrade village to town"
  (let [current-value-of-spot (get-in @area [spot :type-of-building])]
    (if (= current-value-of-spot "village")
      (swap! area update spot assoc :type-of-building "town")
      nil
      )))
(defn upgrade-road
  [area road player]
  "Upgrade road"
  (let [current-value-of-road (get @area road)]
    (if (nil? current-value-of-road)
      (swap! area assoc road player)
      nil))
  )
(defn add-type
  [area type]
  ;add type to area
  (swap! area assoc :type type)
  )



(def deck-development-card
  {:knight         14
   :victory-point  5
   :road-building  2
   :monopoly       2
   :year-of-plenty 2})

(defn random-card [deck]
  ;; take random card from deck. Deck is argument, repeat - make list of values*keys, merging all sets of values*keys in one set,
  ;; and take one card
  (let [deck (apply concat (map (fn [[card count]] (repeat count card)) deck))] (rand-nth deck)))



(defn value-card
  []
  )


(defn -main
  "I don't do a lot ... yet."
  [& args]
  (println "Hello, World!"))

