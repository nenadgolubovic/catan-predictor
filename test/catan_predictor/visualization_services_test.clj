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

