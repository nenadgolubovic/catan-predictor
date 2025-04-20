(ns catan-predictor.area
  (:require [catan-predictor.shop :as shop]
            [catan-predictor.spots :as spots]
            [catan-predictor.utils :as utils]
            [catan-predictor.roads :as roads]
            [catan-predictor.centers :as centers]
            [catan-predictor.visualization :as vis]))


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

(defn remove-once
  [item coll]
  "split collection on before(all before item appear) and after (from first appear item to end of collection)
  fn doing concatenation of before and after without of first item in coll after"
  (let [[before after] (split-with #(not= % item) coll)]
    (concat before (rest after))))

(defn create-area [center points resources numbers]
  "make area map with center (coordinate of centers)
  resource (resource of area)
  spots (all spots connected with that area)
  number (dice number which provide resource)"
  (let [center center
        resource (rand-nth @resources)     ; Randomly pick a resource
        number (if (= "dust" resource)
                 nil
                 (rand-nth @numbers))

        spots (filter #(= 1.000 (utils/distance-1-2 [(first center) (second center)] [(first %) (second %)])) points)]
    (swap! resources
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once resource res-list)]
               first-removed)))
    (swap! numbers
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once number res-list)]
               first-removed)))
    {:center center
     :resource resource
     :spots spots
     :number number}))


(defn create-areas-from-centers
  [points centers resources numbers]
  (map #(create-area % points resources numbers) centers))

(print @numbers)
(print @resources)
(create-areas-from-centers points centers resources numbers)





(def center [1.732 0.0])
