(ns catan-predictor.visualization
  (:require [cljfx.api :as fx]
            [catan-predictor.utils :as utils]
            [catan-predictor.spots :as spots]
            [catan-predictor.roads :as roads]
            [catan-predictor.area :as area]
            [catan-predictor.centers :as centers]
            [catan-predictor.player :as player]
            [cljfx.fx :as fx-elem])
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]
           [javafx.scene.shape Polygon]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
           [javafx.scene.image Image]
           [javafx.geometry Rectangle2D]))

(def centers (centers/make-centers (centers/make-ring-area-centers 0.0 0.0 1.732)))
(def points (spots/make-spots-from-centers centers))
(def r (roads/roads points))
(defn create-spot-view [x y]
  {:fx/type          :circle
   :center-x         (* 100 x)
   :center-y         (* 100 y)
   :radius           10
   :fill             (Color/rgb 210 191 145)
   :on-mouse-clicked {:event/type       :spots-click
                      :spot-coordinates [x y]}
   })
(defn create-line-view [x1 y1 x2 y2]
  {:fx/type          :line
   :start-x          (* 100 x1)
   :start-y          (* 100 y1)
   :end-x            (* 100 x2)
   :end-y            (* 100 y2)
   :stroke (Color/rgb 210 180 140)
   :stroke-width     10
   :on-mouse-clicked {:event/type       :roads-click
                      :road-coordinates [[x1 y1] [x2 y2]]}})
(defonce *state (atom {:dice-1      2
                       :dice-2      2
                       :player-turn 1
                       :spots       (map #(create-spot-view (first %) (second %)) points)
                       :roads (map #(create-line-view (first (first %))
                                                      (second (first %))
                                                      (first (second %))
                                                      (second (second %))) r)
                       :areas (vec (area/create-areas-from-centers points centers
                                                                              (atom ["wool" "wool" "wool" "wool"
                                                                                     "brick" "brick" "brick"
                                                                                   "wood" "wood" "wood" "wood"
                                                                                     "ore" "ore" "ore"
                                                                                     "grain" "grain" "grain" "grain"
                                                                                     "dust"] )
                                                                              (atom ["2" "3" "3" "4" "4" "5" "5" "6" "6"
                                                                                     "8" "8" "9" "9" "10" "10" "11" "11" "12" ])))
                       }))



(defn build-settlement
  [coords]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))]
    (swap! *state update :spots
           (fn [spots]
             (mapv (fn [spot]
                     (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                       (assoc spot :fill (:color player))
                       spot))
                   spots)))

    (swap! *state update :players
           (fn [players]
             (into []
                   (map (fn [v]
                          (if (= (:name v) (:name player))
                            (update v :settlement (fnil conj []) coords)
                            v))
                        players))))

    ))
(defn build-town
  [coords]
  (swap! *state update :spots
         (fn [spots]
           (mapv (fn [spot]
                   (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                     (assoc spot :radius 20)
                     spot))
                 spots)))
  )
(defn build-road
  [coords]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))]

    (swap! *state update :roads
           (fn [roads]
             (mapv (fn [road]
                     (if (= (:road-coordinates (:on-mouse-clicked road)) coords)
                       (assoc road :stroke (:color player))
                       road))
                   roads)))

    (swap! *state update :players
           (fn [players]
             (into []
                   (map (fn [v]
                          (if (= (:name v) (:name player))
                            (update v :roads (fnil conj []) coords)
                            v))
                        players)))))
  )
(def a
  (atom {:spots
         [{:fx/type :circle
           :center-x 433.0
           :center-y 50.0
           :radius 10
           :fill "white"
           :on-mouse-clicked {:event/type :spots-click
                              :spot-coordinates [4.33 0.5]}}

          {:fx/type :circle
           :center-x 346.4
           :center-y 100.0
           :radius 10
           :fill "white"
           :on-mouse-clicked {:event/type :spots-click
                              :spot-coordinates [3.464 1.0]}}

          {:fx/type :circle
           :center-x 259.8
           :center-y 50.0
           :radius 20
           :fill "white"
           :on-mouse-clicked {:event/type :spots-click
                              :spot-coordinates [2.598 0.5]}}]}))
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
(defn input-box-player-name []
  {:fx/type :text-field
   :text (:input-name @*state)
   :prompt-text "Player name"
   :on-text-changed  {:event/type :set-input-name}
   })
(defn input-box-player-color []
  {:fx/type :text-field
   :text (:set-input-color @*state)
   :prompt-text "Color"
   :on-text-changed {:event/type :set-input-color}
   })
(defn spots-view []
  {:fx/type     :group
   :translate-x -250
   :translate-y -100
   :children (:spots @*state)
   })
(defn roads-view []
  {:fx/type     :group
   :translate-x -250
   :translate-y -100
   :children (:roads @*state)
   })
(defn buy-settlement-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Settlement"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 10
   :on-action {:event/type :buy-settlement-btn}
   }
  )
(defn buy-town-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Town"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 10
   :on-action {:event/type :buy-town-btn}
   }
  )
(defn buy-dev-card-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Development Card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 10
   :on-action {:event/type :buy-dev-card-btn}
   }
  )
(defn buy-card-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 10
   :on-action {:event/type :buy-card-btn}
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
         :on-mouse-clicked {:event/type :circle-click
                            :center-coordinates [x y]}
         }))
(defn hexagon-with-circle [x y image circle-image]
    {:fx/type :group
     :children [(hexagon x y image)
                (circle x y circle-image)]})
(defn generate-image-hex [state]
  (let [areas (vec (:areas state))]
    (map #(hexagon-with-circle (first (:center %)) (second (:center %)) (:resource %) (:number %)) areas)
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
(defn image-group [state]
  {:fx/type     :group
  :translate-x -250
  :translate-y -100
  :children (vec (generate-image-hex state))})
(defn player-turn-info
  [t]
  "label which provides information on whose turn it is"
  {:fx/type :label
   :text (str "Player " t)
   :style "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: red;"})
(defn end-turn-btn
  []
  {:fx/type   :button
   :text      "End Turn"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 50
   :on-action {:event/type :end-turn}
   })
(defn table-info
  [state]
  {:fx/type :table-view
   :column-resize-policy :constrained
   :style "-fx-background-color: transparent;
         -fx-control-inner-background: transparent;
         -fx-table-cell-border-color: transparent;
         -fx-table-header-border-color: transparent;
         -fx-selection-bar: transparent;
         -fx-selection-bar-non-focused: transparent;"
   :items (vec (map (fn [player]
                 {:player (:name player)
                  :victory-points (or (:victory-points player) 0)
                  :road-length (or (:road-length player) 0)
                  :army-size (or (:knight-length player) 0)
                  :color (:color player)})
               (:players @state)))

   :columns [{:fx/type :table-column
              :text "Player"
              :cell-value-factory :player
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             {:fx/type :table-column
              :text "Victory Points"
              :cell-value-factory :victory-points
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
             {:fx/type :table-column
              :text "Color"
              :cell-value-factory :color
              :style "-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 30px;"
              }
             ]})
(defn shop []
  {:fx/type :stage
   :showing true
   :title "Shop"
   :width 1000
   :height 500
   }
  )
(defn choose-spot-for-settlement
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))]
  {:fx/type :stage
   :showing true
   :title   "CATAN"
   :scene   {:fx/type :scene
             :root    {:fx/type  :stack-pane
                       :style    "-fx-background-color: #1e90ff;"
                       :children [
                                  (hand-view (:hand player))
                                  {:fx/type   :h-box
                                   :alignment :top-left
                                   :children  [{:fx/type   :button
                                                :text      "Exit from the game"
                                                :on-action {:event/type :start-game-view}}]}
                                  {:fx/type   :h-box
                                   :alignment :center-right
                                   :children  [{:fx/type   :v-box
                                                :alignment :center
                                                :children  [(table-info *state)
                                                            (player-turn-info (:name player))
                                                            (dices-button)
                                                            (dice-views)
                                                            (buy-settlement-button)
                                                            (buy-town-button)
                                                            (buy-dev-card-button)
                                                            (buy-card-button)
                                                            (hand-dev-view (:dev-cards player))
                                                            (end-turn-btn)]}]}
                                  (image-group state)
                                  (roads-view)
                                  (spots-view)
                                  ]}}}))

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
                                                    :on-action {:event/type :remove}}
                                                   (input-box-player-name)
                                                   (input-box-player-color)]
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
(defn take-resources [coords number]
  "function which from board when you pass coordinates of one spots and number and extract info of resources
  connected with that spot"
  (map :resource
       (filter (fn [area]
                 (some (fn [spot] (= coords spot)) (:spots area)))
               (filter #(= (str number) (:number %)) (:areas @*state))
               )))
(defn filing-hand-with-resource [coordinates number]
  "Players who have a settlement on the coordinates get the resources"
  (swap! *state update :players
         (fn [players]
           (mapv (fn [player]
                   (if (some #{coordinates} (:settlement player))
                     (update player :hand into (vec (take-resources coordinates number)))
                     player))
                 players))))
(defn handle-numbers-click [number]
  (println "Clicked:" number))
(defn add-player
  [state player-key name color]
  (swap! state update player-key
         (fn [existing-players]
           (conj existing-players (player/create-player name color)))))
(defn player-turn-inc
  [number num-players]
  "function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1"
  (if (= number num-players)
    1
    (inc number))
  )

(defn event-handler [event]
  (case (:event/type event)
    :add (do
           (add-player *state :players (:input-name @*state) (:input-color @*state))
           (println "Current *state after ADD:" @*state))
    :remove (swap! *state update :players-count dec)
    :choose-spot-for-settlement (swap! *state assoc :fx/type choose-spot-for-settlement)
    :start-game-view (swap! *state assoc :fx/type start-game-view)
    :dice-view  (do
                  (swap! *state assoc :dice-1 (utils/random-dice-number) :dice-2 (utils/random-dice-number))
                  (doseq [coord (:spots @*state)]
                    (filing-hand-with-resource (:spot-coordinates (:on-mouse-clicked coord))
                                               (+ (:dice-1 @*state) (:dice-2 @*state))))
                  )
    :spots-click (build-settlement (:spot-coordinates event))
    :roads-click (build-road (:road-coordinates event))
    :circle-click (handle-numbers-click (:center-coordinates event))
    :set-input-name (swap! *state assoc :input-name (:fx/event event))
    :set-input-color (swap! *state assoc :input-color (:fx/event event))
    :end-turn (swap! *state (fn [s](assoc s :player-turn (player-turn-inc (:player-turn s) (count (:players s))))))
    :buy-dev-card-btn ()
    :buy-settlement-btn
    :buy-town-btn
    :buy-card-btn
    nil))
(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler event-handler}))
(defn start-game []
  (fx/mount-renderer *state renderer)
  (swap! *state assoc :fx/type start-game-view))

