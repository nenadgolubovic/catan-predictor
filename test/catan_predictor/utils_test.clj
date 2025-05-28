(ns catan-predictor.utils-test
  (:require [midje.sweet :refer :all]
            [catan-predictor.utils :refer :all]))

(fact "random-dice-number returns int between 1 and 6 inclusive"
      (doseq [_ (range 100)]
        (let [r (random-dice-number)]
          (integer? r) => true
          (<= 1 r 6) => true)))

(fact "math-round rounds correctly"
      (math-round 1.2345 2) => 1.23
      (math-round 1.2345 3) => 1.235
      (math-round 0.0004 3) => 0.0
      (math-round -2.3456 2) => -2.35)

(fact "fcos returns cos( (1+2k)*PI/6 ) rounded to 5 decimals"
      (fcos 0) => 0.86603  ; cos(PI/6)
      (fcos 1) => 0.0      ; cos(PI/2)
      (fcos 2) => -0.86603 ; cos(5PI/6))
)
(fact "fsin returns sin( (1+2k)*PI/6 ) rounded to 5 decimals"
      (fsin 0) => 0.5
      (fsin 1) => 1.0
      (fsin 2) => 0.5)

(fact "fcos1 returns cos(k*PI/3) rounded to 5 decimals"
      (fcos1 0) => 1.0
      (fcos1 1) => 0.5
      (fcos1 2) => -0.5
      (fcos1 3) => -1.0)

(fact "fsin1 returns sin(k*PI/3) rounded to 5 decimals"
      (fsin1 0) => 0.0
      (fsin1 1) => 0.86603
      (fsin1 2) => 0.86603
      (fsin1 3) => 0.0)

(fact "round-seq rounds each pair in sequence"
      (round-seq [[1.23456 2.34567] [3.98765 4.12345]] 3) => [[1.235 2.346] [3.988 4.123]])

(fact "distance-1-2 calculates Euclidean distance rounded to 3 decimals"
      (distance-1-2 [0 0] [3 4]) => 5.0
      (distance-1-2 [1 1] [1 1]) => 0.0
      (distance-1-2 [-1 -2] [4 5]) => 8.602)