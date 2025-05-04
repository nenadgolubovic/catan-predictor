(ns catan-predictor.utils
  (:require [clojure.math :as m]
            ))

(defn random-dice-number
  []
  "get random number from 1 to 6"
  (+ 1 (rand-int 6)))

(defn math-round
  [n decimals]
  "Round number n on decimal places (decimals)"
  (/ (m/round (* n (m/pow 10 decimals))) (m/pow 10 decimals)))

(defn fcos
  [k]
  "calculate cos of 1+2k*PI/6"
  (math-round (m/cos (* m/PI (/ (+ 1 (* 2 k)) 6))) 5))

(defn fsin
  [k]
  "calculate sin of 1+2k*PI/6"
  (math-round (m/sin (* m/PI (/ (+ 1 (* 2 k)) 6))) 5))

(defn fcos1
  [k]
  "calculate cos of k*PI/3"
  (math-round (m/cos (* m/PI (/ k 3))) 5))

(defn fsin1
  [k]
  "calculate sin of k*PI/3"
  (math-round (m/sin (* m/PI (/ k 3))) 5))




(defn round-seq
  [seq n]
  "Round sequence on n decimals"
  (map (fn [[x y]] [(math-round x 3) (math-round y n)]) seq))


(defn distance
  [x y decimals]
  "Distance from center 0.0 0.0
  x-> x axis value
  y-> y axis value
  decimals -> round on decimals"
  (let [dist (m/sqrt (+ (m/pow x 2) (m/pow y 2)))]
    (math-round dist decimals)))

(defn distance-1-2
  [[x1 y1] [x2 y2]]
  "Distance between 2 spots
  x1 -> x-axis of spot1
  x2 -> x-axis of spot2
  y1 -> y-axis of spot1
  y2 -> y-axis of spot2"
  (math-round (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))
                         ) 3) )


