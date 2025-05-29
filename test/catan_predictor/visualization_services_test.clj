(ns catan-predictor.visualization-services-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all ]
            [catan-predictor.visualization-elements :refer :all]
            [catan-predictor.visualization-services :as services])
  (:import [javafx.scene.paint Color]
           [javafx.scene.layout Background ]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
           ))


(fact "Function roads return roads"
      (set (services/roads [[0.0 1.0] [1 2] [-1.732 0] [2.0 -2.464] [-4.0 2] [0.0 0.0]
                            [-2.0 -1.0] [-0.866 0.5]]))
      => #{[[-1.732 0] [-0.866 0.5]]
           [[-0.866 0.5] [-1.732 0]]
           [[-0.866 0.5] [0.0 0.0]]
           [[-0.866 0.5] [0.0 1.0]]
           [[0.0 0.0] [-0.866 0.5]]
           [[0.0 0.0] [0.0 1.0]]
           [[0.0 1.0] [-0.866 0.5]]
           [[0.0 1.0] [0.0 0.0]]})
(fact "make-ring-area-centers returns 6 hexagon points around center (0,0) with radius 1"
      (services/make-ring-area-centers 0 0 1)
      => (just
           [[1.0 0.0]
            [0.5 0.866]
            [-0.5 0.866]
            [-1.0 0.0]
            [-0.5 -0.866]
            [0.5 -0.866]]
           :in-any-order))
(fact "make-centers should return correct hexagon centers around input points"
      (services/make-centers [[0 0]])
      => (just [[1.732 0.0]
                [0.866 1.5]
                [-0.866 1.5]
                [-1.732 0.0]
                [-0.866 -1.5]
                [0.866 -1.5]]
               :in-any-order))
(fact "make-centers with two points includes unique ring centers"
      (services/make-centers [[0 0] [1.732 0]])
      => (just [[1.732 0.0]
                [0.866 1.5]
                [-0.866 1.5]
                [-1.732 0.0]
                [-0.866 -1.5]
                [0.866 -1.5]
                [3.464 0.0]
                [2.598 1.5]
                [0.0 0.0]
                [2.598 -1.5]] :in-any-order))
(fact "spots returns correct hexagon points for center [0 0] and standard angles"
      (services/spots [0 0] [0 1 2 3 4 5])
      => (just [[0.866 0.5]
                [0.0 1.0]
                [-0.866 0.5]
                [-0.866 -0.5]
                [0.0 -1.0]
                [0.866 -0.5]]
               :in-any-order))
(fact "make-spots-from-centers returns all unique hexagon spots from given centers"
      (services/make-spots-from-centers [[0 0]])
      => (just [[0.866 0.5]
                [0.0 1.0]
                [-0.866 0.5]
                [-0.866 -0.5]
                [0.0 -1.0]
                [0.866 -0.5]]
               :in-any-order))
(fact "make-spots-from-centers returns unique hexagon spots from multiple centers"
      (services/make-spots-from-centers [[0 0] [1.732 0]])
      => (just [[0.866 0.5]
                [0.0 1.0]
                [-0.866 0.5]
                [-0.866 -0.5]
                [0.0 -1.0]
                [0.866 -0.5]
                [2.598 0.5]
                [1.732 1.0]
                [1.732 -1.0]
                [2.598 -0.5]]
               :in-any-order))
(fact "remove-card removes first card of given type"
      (let [hand ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "ore" "wool"]]
        (services/remove-card "grain" hand) => ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "ore" "wool"]    ; delete first "grain"
        (services/remove-card "brick" hand) => hand                               ; no "brick", return same
        (services/remove-card "wool" hand) => ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "ore"]    ; delete first "wool"
        (services/remove-card "wood" hand) => ["wood" "wood" "wood" "wood" "grain" "grain" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "ore" "wool"]    ; delete first "wood"
        (services/remove-card "ore" hand) => ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "wool"]))   ; delete first "ore"
(fact "remove-n-cards removes correct number of cards of given type"
      (let [hand ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain""ore" "ore" "ore" "ore" "ore" "ore" "ore""wool"]]
        (services/remove-n-cards "grain" hand 2) => ["wood" "wood" "wood" "wood" "wood" "grain" "ore" "ore" "ore" "ore" "ore" "ore" "ore""wool"]     ; remove  2 "grain"
        (services/remove-n-cards "ore" hand 5) => ["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain""ore"  "ore""wool"] ;remove 5 ore
        (services/remove-n-cards "wool" hand 1) =>["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain""ore" "ore" "ore" "ore" "ore" "ore" "ore"] ;remove 1 wool
        (services/remove-n-cards "wool" hand 2) =>["wood" "wood" "wood" "wood" "wood" "grain" "grain" "grain""ore" "ore" "ore" "ore" "ore" "ore" "ore"] ;try to remove 2 wool if only 1 is there
        (services/remove-n-cards "brick" hand 2) => hand    ; if want to delete some resorce even if is not in hand
        (services/remove-n-cards "grain" hand 0) => hand    ; if nothing are deleted
        ))
(fact "buy-settlement removes one card of each required type (grain, wool, brick, wood) from hand"
      (let [hand ["wood" "wood" "wood" "wood" "wood" "brick" "brick" "brick" "wool" "wool" "wool" "grain" "grain" "grain"]]
        (services/buy-settlement hand) => ["wood" "wood" "wood" "wood" "brick" "brick" "wool" "wool" "grain" "grain"] ; remove one of each resource (wood, brick, wool, grain)
        (services/buy-settlement ["wood" "brick" "grain" "wool"]) => [] ; removes all if exactly one of each resource
        (services/buy-settlement ["wood" "wood" "grain" "grain"]) => ["wood" "grain"])) ; no brick or wool
(fact "buy-town removes 2 grain and 3 ore cards from hand"
      (let [hand ["ore" "ore" "ore" "ore" "ore" "grain" "grain" "grain" "grain" "grain" "wood" "brick"]]
        (services/buy-town hand) => ["ore" "ore" "grain" "grain" "grain" "wood" "brick"]; removes 3 ore and 2 grain
        (services/buy-town ["ore" "ore" "ore" "grain" "grain"]) => [] ; removes all if exactly 3 ore and 2 grain
        (services/buy-town ["ore" "grain" "grain" "wood"]) => ["wood"])) ; not enough ore
(fact "buy-road removes 1 wood and 1 brick from hand"
      (let [hand ["wood" "wood" "brick" "brick" "grain"]]
        (services/buy-road hand) => ["wood" "brick" "grain"] ; removes one wood and one brick
        (services/buy-road ["wood" "brick"]) => [] ; removes all if exactly one wood and one brick
        (services/buy-road ["wood" "grain"]) => ["grain"])) ; missing one resource
(fact "buy-development-card removes 1 grain, 1 ore, and 1 wool from hand"
      (let [hand ["grain" "grain" "ore" "ore" "wool" "wool" "wood"]]
        (services/buy-development-card hand) => ["grain" "ore" "wool" "wood"] ; removes one grain, ore, wool
        (services/buy-development-card ["grain" "ore" "wool"]) => [] ; removes all if exactly one of each
        (services/buy-development-card ["grain" "ore" "wood"]) => ["wood"])) ; missing wool
