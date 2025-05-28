(ns catan-predictor.core-test
  (:require [midje.sweet :refer :all]
            [catan-predictor.core :refer :all]
            [catan-predictor.visualization :as vis]))

(fact "-main calls vis/start-game"
      (against-background (vis/start-game) => :called)
      (let [result (-main)]
        result => :called))