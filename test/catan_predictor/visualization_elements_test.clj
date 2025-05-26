(ns catan-predictor.visualization-elements-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all ]
            [catan-predictor.visualization-elements :refer :all])
  (:import [javafx.scene.paint Color]))

(fact "create-spot-view returns a valid cljfx circle map"
      (create-spot-view 1.0 0.0)
      => {:fx/type :circle
          :center-x 100.0
          :center-y 0.0
          :radius 10
          :fill (Color/rgb 210 191 145)
          :on-mouse-clicked {:event/type :spots-click
                             :spot-coordinates [1.0 0.0]}})

(fact "create-spot-view returns a valid cljfx circle map with int numbers"
      (create-spot-view 1 0)
      => {:fx/type :circle
          :center-x 100
          :center-y 0
          :radius 10
          :fill (Color/rgb 210 191 145)
          :on-mouse-clicked {:event/type :spots-click
                             :spot-coordinates [1 0]}})
(fact "create-spot-view returns a valid cljfx circle map with int numbers"
      (create-spot-view 1.0 0)
      => {:fx/type :circle
          :center-x 100.0
          :center-y 0
          :radius 10
          :fill (Color/rgb 210 191 145)
          :on-mouse-clicked {:event/type :spots-click
                             :spot-coordinates [1.0 0]}})
(fact "create-line-view returns valid map of line with int value"
  (create-line-view 1 2 3 4)=>
          {:fx/type      :line
           :start-x      100
           :start-y      200
           :end-x        300
           :end-y        400
           :stroke       (Color/rgb 210 180 140)
           :stroke-width 10
           :on-mouse-clicked {:event/type       :roads-click
                              :road-coordinates [[1 2] [3 4]]}})
(fact "create-line-view returns valid map of line with float value"
      (create-line-view 1.0 2.0 3.0 4.0)=>
      {:fx/type      :line
       :start-x      100.0
       :start-y      200.0
       :end-x        300.0
       :end-y        400.0
       :stroke       (Color/rgb 210 180 140)
       :stroke-width 10
       :on-mouse-clicked {:event/type       :roads-click
                          :road-coordinates [[1.0 2.0] [3.0 4.0]]}})
(fact "create-line-view returns valid map of line with negative value"
      (create-line-view 1.0 -2.0 3 -4)=>
      {:fx/type      :line
       :start-x      100.0
       :start-y      -200.0
       :end-x        300
       :end-y        -400
       :stroke       (Color/rgb 210 180 140)
       :stroke-width 10
       :on-mouse-clicked {:event/type       :roads-click
                          :road-coordinates [[1.0 -2.0] [3 -4]]}})