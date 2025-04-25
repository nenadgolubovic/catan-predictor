(ns catan-predictor.visualization
  (:require [cljfx.api :as fx]
            [catan-predictor.utils :as utils]
            [catan-predictor.spots :as spots]
            [catan-predictor.roads :as roads]
            [catan-predictor.area :as area]
            [catan-predictor.centers :as centers]
            [cljfx.fx :as fx-elem])
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]
           [javafx.scene.shape Polygon]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
           [javafx.scene.image Image]
           [javafx.geometry Rectangle2D]))


(defonce *state (atom {:players-count 1 :show-popup? false}))

(defn background-image []
  (Background.
    (into-array BackgroundImage
                [(BackgroundImage.
                   (Image. "file:resources/static/start-manu-background.jpg")
                   BackgroundRepeat/NO_REPEAT
                   BackgroundRepeat/NO_REPEAT
                   BackgroundPosition/CENTER
                   (BackgroundSize. 1000 1000 true true true false))])))
(defn get-dice-image-url
  [dice-number]
  "get image depends on dice numer"
  (str "file:resources/static/dice-" dice-number ".png"))
(defn dices-button
  []
  "Roll dice button"
  {:fx/type   :button
   :alignment :center
   :style     "-fx-background-color: transparent;"
   :graphic   {:fx/type    :image-view
               :image      {:fx/type :image
                            :url     "file:resources/static/dices.png"}
               :fit-width  350
               :fit-height 200
               }
   :on-action {:event/type :dice-view}}
  )
(defn dice-views
  []
  {:fx/type   :h-box
   :alignment :center
   :children  [{:fx/type    :image-view
                :fit-width  200
                :fit-height 200
                :image      {:fx/type :image
                             :url     (get-dice-image-url (get @*state :dice-1))}
                :clip {:fx/type :rectangle
                       :width 200
                       :height 200
                       :arc-width 40
                       :arc-height 40}}
               {:fx/type    :image-view
                :fit-width  200
                :fit-height 200
                :image      {:fx/type :image
                             :url     (get-dice-image-url (get @*state :dice-2))}
                :clip {:fx/type :rectangle
                       :width 200
                       :height 200
                       :arc-width 40
                       :arc-height 40}}]
   })
(defn dices []
  {:fx/type   :v-box
   :alignment :center
   :children
   (dices-button)})

(def centers (centers/make-centers (centers/make-ring-area-centers 0.0 0.0 1.732)))
(def points (spots/make-spots-from-centers centers))
(def r (roads/roads points))

(defn create-spot-view [x y]
  {:fx/type          :circle
   :center-x         (* 100 x)
   :center-y         (* 100 y)
   :radius           10
   :fill             "white"
   :on-mouse-clicked {:event/type       :spots-click
                      :spot-coordinates [x y]}
   })
(defn create-line-view [x1 y1 x2 y2]
  {:fx/type          :line
   :start-x          (* 100 x1)
   :start-y          (* 100 y1)
   :end-x            (* 100 x2)
   :end-y            (* 100 y2)
   :stroke           "black"
   :stroke-width     10
   :on-mouse-clicked {:event/type       :roads-click
                      :road-coordinates [[x1 y1] [x2 y2]]}})
(defn spots-view []
  {:fx/type     :group
   :translate-x -250
   :translate-y -100
   :children    (map #(create-spot-view (first %) (second %)) points)
   })
(defn roads-view []
  {:fx/type     :group
   :translate-x -250
   :translate-y -100
   :children    (map #(create-line-view (first (first %))
                                        (second (first %))
                                        (first (second %))
                                        (second (second %))) r)
   })
(defn shop-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :style     "-fx-background-color: transparent;"
   :graphic   {:fx/type    :image-view
               :image      {:fx/type :image
                            :url  "file:resources/static/shop.png"}
               :fit-width  200
               :fit-height 200}
   :on-action {:event/type :shop-view}
   }
  )
(defn hexagon [x1 x2 image]
  (let [image-path (str "file:resources/static/area-" image ".jpg")
        image (Image. image-path)
        pattern (ImagePattern. image)]
        {:fx/type :polygon
         :points (vec (map #(* 100 %) (vec (apply concat (spots/spots [x1 x2] [0 1 2 3 4 5])))))
         :fill pattern
         :stroke "black"
         :stroke-width 1
         }))
(defn circle [x y image]
  (let [image-name (if (nil? image) "pawn" image)
        image-path (str "file:resources/static/" image-name ".jpg")
        image (Image. image-path)
        pattern (ImagePattern. image)]
        {:fx/type :circle
         :center-x (* 100 x)
         :center-y (* 100 y)
         :radius 20
         :fill pattern
         :stroke "black"
         :stroke-width 1
         }))
(defn hexagon-with-circle [x y image circle-image]
    {:fx/type :group
     :children [(hexagon x y image)
                (circle x y circle-image)]})
(defn generate-image-hex []
  (let [data (vec (area/create-areas-from-centers points centers
                                                  (atom ["wool" "wool" "wool" "wool"
                                                         "brick" "brick" "brick"
                                                         "wood" "wood" "wood" "wood"
                                                         "ore" "ore" "ore"
                                                         "grain" "grain" "grain" "grain"
                                                         "dust"] )
                                                  (atom ["2" "3" "3" "4" "4" "5" "5" "6" "6"
                                                         "8" "8" "9" "9" "10" "10" "11" "11" "12" ])))]
    (map #(hexagon-with-circle (first (:center %)) (second (:center %)) (:resource %) (:number %)) data)
         ))
(defn card
  [resource type]
  (let [image-path (str "file:resources/static/"type"-" resource ".jpg")
        image (Image. image-path)
        pattern (ImagePattern. image)]
  {:fx/type :rectangle
   :width 160
   :height 230
   :arc-height 20
   :arc-width 20
   :fill pattern
   :stroke :gray
   :stroke-width 1
   }))
(defn hand-view
  [cards]
  {:fx/type :h-box
   :alignment :bottom-center
   :children (vec (map #(card % "resource") cards))}
  )
(defn hand-dev-view
  [cards]
  {:fx/type :h-box
   :alignment :bottom-right
   :children (vec (map #(card % "dev") cards))}
  )
(defn image-group []
  {:fx/type     :group
  :translate-x -250
  :translate-y -100
  :children (vec (generate-image-hex))})
(defn end-turn-btn
  []
  {:fx/type   :button
   :text      "End Turn"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 50
   })
(defn table-info
  []
  {:fx/type :table-view
   :column-resize-policy :constrained
   :style "-fx-background-color: transparent;
         -fx-control-inner-background: transparent;
         -fx-table-cell-border-color: transparent;
         -fx-table-header-border-color: transparent;
         -fx-selection-bar: transparent;
         -fx-selection-bar-non-focused: transparent;"
   :items [{:player "Player 1" :vp 5 :road-length 5 :army-size 1}
          {:player "Player 2" :vp 4 :road-length 2 :army-size 2}
          {:player "Player 3" :vp 4 :road-length 1 :army-size 3}
          {:player "Player 4" :vp 5 :road-length 3 :army-size 4}]
   :columns [{:fx/type :table-column
              :text "Player"
              :cell-value-factory :player
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             {:fx/type :table-column
              :text "Victory Points"
              :cell-value-factory :vp
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             {:fx/type :table-column
              :text "Road Length"
              :cell-value-factory :road-length
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             {:fx/type :table-column
              :text "Army Size"
              :cell-value-factory :army-size
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             ]})
(defn choose-spot-for-settlement
  [state]
  {:fx/type :stage
   :showing true
   :title   "CATAN"
   :scene   {:fx/type :scene
             :root    {:fx/type  :stack-pane
                       :style    "-fx-background-color: #1e90ff;"
                       :children [(hand-view ["grain" "ore"])
                                  {:fx/type   :h-box
                                   :alignment :top-left
                                   :children  [{:fx/type   :button
                                                :text      "Exit from the game"
                                                :on-action {:event/type :start-game-view}}]}
                                  {:fx/type   :h-box
                                   :alignment :center-right
                                   :children  [{:fx/type   :v-box
                                                :alignment :center
                                                :children  [(table-info)
                                                            (dices-button)
                                                            (dice-views)
                                                            (shop-button)
                                                            (hand-dev-view ["year-of-plenty" "knight" "monopoly" "victory-point" "road-building"])
                                                            (end-turn-btn)]}]}
                                  (image-group)
                                  (roads-view)
                                  (spots-view)

                                  ]}}})
(defn start-game-view [state]
  {:fx/type :stage
   :showing true
   :title   "CATAN"
   :scene   {:fx/type :scene
             :root    {:fx/type    :stack-pane
                       :alignment  :center
                       :background (background-image)
                       :children   [{:fx/type     :label
                                     :text        "WELCOME TO CATAN!"
                                     :style       "-fx-text-fill: #FFD700;
                                      -fx-font-size: 100px;
                                      -fx-font-weight: bold;
                                      -fx-background-color: rgba(0, 0, 0, 0.5);
                                      -fx-padding: 20px 20px 20px 20px;
                                      -fx-background-radius: 10px;"
                                     :translate-y -500}

                                    {:fx/type     :h-box
                                     :spacing     20
                                     :alignment   :center
                                     :children    [{:fx/type   :button
                                                    :text      "Add"
                                                    :style     "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #3F51B5;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"
                                                    :on-action {:event/type :add}}

                                                   {:fx/type   :button
                                                    :text      "Remove"
                                                    :style     "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #3F51B5;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"
                                                    :on-action {:event/type :remove}}]
                                     :translate-y 100}
                                    {:fx/type     :label
                                     :text        (str "Number of players " (:players-count state))
                                     :style       "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #009688;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"

                                     :translate-y 0}
                                    {:fx/type     :button
                                     :text        "Start Game"
                                     :style       "-fx-font-size: 40px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #FF5722;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 400px;
                                                  -fx-min-height: 100px;"
                                     :on-action   {:event/type :choose-spot-for-settlement}
                                     :translate-y 200
                                     }
                                    ]}}})


(defn handle-click [coordinates]
  (println "Clicked:" coordinates))
(defn handle-road-click [coordinates]
  (println "Clicked:" coordinates))

(defn event-handler [event]
  (case (:event/type event)
    :add (swap! *state update :players-count inc)
    :remove (swap! *state update :players-count dec)
    :choose-spot-for-settlement (swap! *state assoc :fx/type choose-spot-for-settlement)
    :start-game-view (swap! *state assoc :fx/type start-game-view)
    :dice-view (swap! *state assoc :dice-1 (utils/random-dice-number) :dice-2 (utils/random-dice-number))
    :spots-click (handle-click (:spot-coordinates event))
    :roads-click (handle-road-click (:road-coordinates event))

    nil))

(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler event-handler}))


(defn start-game []
  (fx/mount-renderer *state renderer)
  (swap! *state assoc :fx/type start-game-view))