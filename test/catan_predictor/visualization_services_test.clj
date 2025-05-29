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
