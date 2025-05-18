(ns catan-predictor.visualization
  (:require [catan-predictor.shop :as shop]
            [cljfx.api :as fx]
            [catan-predictor.utils :as utils]
            [catan-predictor.spots :as spots]
            [catan-predictor.roads :as roads]
            [catan-predictor.area :as area]
            [catan-predictor.centers :as centers]
            [catan-predictor.player :as player]
            [catan-predictor.deck :as deck]
            [cljfx.fx :as fx-elem]
            )
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]
           [javafx.scene.shape Polygon]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
           [javafx.scene.image Image]
           [javafx.geometry Rectangle2D]))


;Have to add functionality:
  ; add function to for dice 7 (put picture on number and block that number) (function block number should be added,
  ; maybe through some state in *state)
  ;
  ; add validation that you buy road if you not have settlement nears by
  ; (maybe you can build-road only if in :settlement of :player, who is on turn, is spot which is on selected :road-coordinates)\

  ; boocking of spots
  ; (make in :spots when you make event spots click that spots get some atribute :book :true and if is true
  ; you cannot click again and also book all settlement fared away by 1)
  ;
  ; if something not build in intiial phase, do again
  ; calculatiuon in table of vp
  ; buy cards for 4 yours
  ; to can activate dev cards
  ; validation that you only can buy settlement and road if you have cards, otherwise message wil be showned
  ; all have to be fitted in window
  ; arrange project to look better

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
                       :town-build false
                       :settlement-build false
                       :road-build false
                       :restricted-area nil
                       :restricted-number nil
                       :move-thief false
                       :game-massage "WELCOME"
                       :phase "Initial"
                       :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS"
                       :players []
                       :players-count 0
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
                       :development-deck ["knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight"
                                          "victory-point" "victory-point" "victory-point" "victory-point" "victory-point"
                                          "road-building" "road-building"
                                          "monopoly" "monopoly"
                                          "year-of-plenty" "year-of-plenty"]
                       }))
;Start of game elements:
(defn background-image []
  (Background.
    (into-array BackgroundImage
                [(BackgroundImage.
                   (Image. "file:resources/static/start-manu-background.jpg")
                   BackgroundRepeat/NO_REPEAT
                   BackgroundRepeat/NO_REPEAT
                   BackgroundPosition/CENTER
                   (BackgroundSize. 1000 1000 true true true false))])))
(defn add-button []
  {:fx/type :button
   :text "Add Player"
   :style     "-fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-background-color: #3F51B5;
                -fx-text-fill: white;
                -fx-padding: 10px 20px;
                -fx-background-radius: 5px;
                -fx-min-width: 200px;
                -fx-min-height: 60px;"
   :disable (or (clojure.string/blank? (:input-name @*state))
                (nil? (:input-color @*state)))
   :on-action {:event/type :add}
   })
(defn remove-button [idx]
  {:fx/type   :button
   :text      "X"
   :style     "-fx-font-size: 10px;
                -fx-font-weight: bold;
                -fx-background-color: #3F51B5;
                -fx-text-fill: white;
                -fx-padding: 10px 20px;
                -fx-background-radius: 5px;
                -fx-min-width: 20px;
                -fx-min-height: 20px;"
   :on-action {:event/type :remove
               :index idx}})
(defn player-list []
  {:fx/type :v-box
   :spacing 10
   :style "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
   :children
   (concat
     [{:fx/type :h-box
       :style  "-fx-font-weight: bold; -fx-padding: 5;"
       :spacing 20
       :children [{:fx/type :label :text "Name" :style "-fx-pref-width: 150px;"}
                  {:fx/type :label :text "Color" :style "-fx-pref-width: 150px;"}
                  {:fx/type :label :text "Remove" :style "-fx-pref-width: 200px;"}]}]
     (map-indexed
       (fn [idx {:keys [name color]}]
         {:fx/type :h-box
          :spacing 20
          :alignment :center-left
          :style "-fx-padding: 5; -fx-border-color: lightgray; -fx-border-width: 0 0 1 0;"
          :children [{:fx/type :label
                      :text name
                      :style "-fx-pref-width: 150px;"}
                     {:fx/type :label
                      :text color
                      :style "-fx-pref-width: 150px;"}
                     (remove-button idx)]})
       (:players @*state)))})
(defn coords-in-roads?
  [coords player-name]
  (let [roads (:roads (first (filter #(= (:name %) player-name) (:players @*state))))]
    (some (fn [[a b]]
            (or (= coords a) (= coords b)))
          roads)))
(defn coords-in-settlement-or-roads?
  [coords player-name]
  (let [player (first (filter #(= (:name %) player-name) (:players @*state)))
        settlement (:settlement player)
        roads (:roads player)]
    (some (fn [point]
            (or (some #(= point %) settlement)
                (some (fn [[a b]] (or (= point a) (= point b)))  roads)))
      coords)))
(defn coords-in-last-settlement?
  [coords player-name]
  (let [player (first (filter #(= (:name %) player-name) (:players @*state)))
        last-settlement (last (:settlement player))]
    (some #(= % last-settlement) coords)))
(defn build-settlement
  [coords]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))
        player-idx (dec (:player-turn @*state))
        game-phase (= "Game" (:phase @*state))]

    (when (or (not game-phase)
              (coords-in-roads? coords (:name player)))
      (try
        (swap! *state update-in [:players player-idx :hand] shop/buy-settlement)
        (catch Exception e
          (println "No resource")))

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

      (println "Updated player info:" (get (vec (:players @*state)) player-idx)))))
(defn build-town
  [coords]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))
        player-idx (dec (:player-turn @*state))]

    (if (some #(= coords %) (:settlement player))
      (do
        (try
          (swap! *state update-in [:players player-idx :hand] shop/buy-town)
          (catch Exception e
            (println "No resource")))
        (try
          (swap! *state update-in [:players player-idx :settlement] (fn [settlement] (remove #(= % coords) settlement)))
          (catch Exception e
            (println "Not your settlement")))
        (swap! *state update :players
               (fn [players]
                 (into []
                       (map (fn [v]
                              (if (= (:name v) (:name player))
                                (update v :towns (fnil conj []) coords)
                                v))
                            players))))

        (swap! *state update :spots
               (fn [spots]
                 (mapv (fn [spot]
                         (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                           (assoc spot :radius 20)
                           spot))
                       spots))))
      (print "Not your settlement, you have to build settlement first, then town ")
      )


  ))
(defn build-road
  [coords]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))
        player-idx (dec (:player-turn @*state))
        game-phase (= "Game" (:phase @*state))]

    (when (or (and game-phase
                   (coords-in-settlement-or-roads? coords (:name player)))
              (and (not game-phase)
                   (coords-in-last-settlement? coords (:name player))))

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
                          players))))

      (println "Updated player info:" (get (vec (:players @*state)) player-idx)))))
(defn color-dropdown []
  (let [all-colors ["red" "blue" "yellow" "green"]
        used-colors (set (map :color (:players @*state)))
        available-colors (remove used-colors all-colors)]
    {:fx/type :combo-box
     :prompt-text "Choose color"
     :value (:input-color @*state)
     :items (vec available-colors)
     :on-value-changed #(swap! *state assoc :input-color %) }))
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
(defn name-input []
  {:fx/type :text-field
   :prompt-text "Enter name"
   :text (:input-name @*state)
   :on-text-changed #(swap! *state assoc :input-name %)})
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
(defn buy-road-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Road"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   10
   :v-box/margin 10
   :on-action {:event/type :buy-road-btn}
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
   :text (str "PLAYER TURN: " t)
   :style "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"})
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
  {:fx/type :v-box
   :style "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
   :children
   [{:fx/type :table-view
     :column-resize-policy :constrained
     :style "-fx-background-color: white;
             -fx-control-inner-background: white;
             -fx-table-cell-border-color: lightgray;
             -fx-table-header-border-color: lightgray;
             -fx-selection-bar: #cce5ff;
             -fx-selection-bar-non-focused: #99ccff;"
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
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}
               {:fx/type :table-column
                :text "Victory Points"
                :cell-value-factory :victory-points
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}
               {:fx/type :table-column
                :text "Road Length"
                :cell-value-factory :road-length
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}
               {:fx/type :table-column
                :text "Army Size"
                :cell-value-factory :army-size
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}
               {:fx/type :table-column
                :text "Color"
                :cell-value-factory :color
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}]}]})
(defn shop []
  {:fx/type :stage
   :showing true
   :title "Shop"
   :width 1000
   :height 500
   }
  )
(defn game-view
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))]
    {:fx/type :stage
     :showing true
     :title   "CATAN"
     :scene   {:fx/type :scene
               :root    {:fx/type  :border-pane
                         :style    "-fx-background-color: #1e90ff;"
                         :top {:fx/type   :v-box
                               :alignment :center
                               :padding   10
                               :children  [(player-turn-info (:name player))
                                           {:fx/type :label
                                            :style "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
                                            :text (:game-massage @*state)}]}
                         :left {:fx/type   :v-box
                                :padding   10
                                :alignment :top-left
                                :children  [{:fx/type   :button
                                             :text      "Exit from the game"
                                             :on-action {:event/type :start-game-view}}]}

                         :right {:fx/type   :v-box
                                 :spacing   10
                                 :padding   10
                                 :alignment :center
                                 :children  [(table-info *state)
                                             (dices-button)
                                             (dice-views)
                                             (buy-settlement-button)
                                             (buy-town-button)
                                             (buy-road-button)
                                             (buy-dev-card-button)
                                             (buy-card-button)
                                             (hand-dev-view (:dev-cards player))
                                             (end-turn-btn)]}
                         :bottom (hand-view (:hand player))
                         :center {:fx/type :stack-pane
                                  :children [(image-group state)
                                             (roads-view)
                                             (spots-view)]}}}}))
(defn start-game-view [state]
  {:fx/type :stage
   :showing true
   :title   "CATAN"
   :scene   {:fx/type :scene
             :root    {:fx/type    :v-box
                       :alignment :top-center
                       :spacing   20
                       :padding   20
                       :background (background-image)

                       :children [{:fx/type :label
                                   :text "WELCOME TO CATAN!"
                                   :style "-fx-text-fill: #FFD700;
                                           -fx-font-size: 60px;
                                           -fx-font-weight: bold;
                                           -fx-background-color: rgba(0, 0, 0, 0.5);
                                           -fx-padding: 20px 40px;
                                           -fx-background-radius: 10px;"
                                   }

                                  {:fx/type :v-box
                                   :style "-fx-background-color: white;
                                           -fx-padding: 15px;
                                           -fx-background-radius: 10px;
                                           -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 0);"
                                   :max-width 600
                                   :children [(player-list)]}

                                  {:fx/type :h-box
                                   :spacing 20
                                   :alignment :center
                                   :children [(add-button)
                                              (name-input)
                                              (color-dropdown)]}

                                  {:fx/type :button
                                   :text "Start Game"
                                   :style "-fx-font-size: 40px;
                                           -fx-font-weight: bold;
                                           -fx-background-color: #FF5722;
                                           -fx-text-fill: white;
                                           -fx-padding: 10px 20px;
                                           -fx-background-radius: 5px;
                                           -fx-min-width: 400px;
                                           -fx-min-height: 100px;"
                                   :on-action {:event/type :initial-phase-game}}]}}})
(defn initial-phase-game
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))]
    {:fx/type :stage
     :showing true
     :title   "GAME SETUP - CATAN"
     :scene   {:fx/type :scene
               :root    {:fx/type  :stack-pane
                         :style    "-fx-background-color: #1e90ff;"
                         :children [
                                    {:fx/type   :v-box
                                     :alignment :top-center
                                     :padding   10
                                     :children  [{:fx/type :label
                                                  :text    (:initial-info @*state)
                                                  :style   "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"}
                                                 (player-turn-info (:name player))]}
                                    {:fx/type   :h-box
                                     :alignment :top-left
                                     :padding   10
                                     :children  [{:fx/type   :button
                                                  :text      "Exit from the game"
                                                  :on-action {:event/type :start-game-view}}]}
                                    {:fx/type   :h-box
                                     :alignment :center-right
                                     :children  [{:fx/type   :v-box
                                                  :alignment :center
                                                  :children  [(table-info *state)
                                                              (end-turn-btn)]}]}
                                    (hand-view (:hand player))
                                    (image-group state)
                                    (roads-view)
                                    (spots-view)
                                    ]}}}))
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
(defn player-turn-dec
  [number]
  "function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1"
   (dec number))
(defn take-development-card []
  (let [player-idx (dec (:player-turn @*state))
        chosen (rand-nth (:development-deck @*state))]
    (swap! *state update :development-deck #(shop/remove-card chosen %))
    (swap! *state update-in [:players player-idx :dev-cards] #(conj % chosen))
    (swap! *state update-in [:players player-idx :hand] shop/buy-development-card)
    (println "Chosen card:" chosen)))

(defn event-handler [event]
  (case (:event/type event)
    :add (do
           (swap! *state
                  (fn [s]
                    (-> s
                        (update :players conj {:name (:input-name s)
                                               :color (:input-color s)})
                        (assoc :input-name "" :input-color nil))))
           (println "Current *state after ADD:" @*state))
    :remove
    (let [idx (:index event)]
      (println "Remove player with idx:" idx)
      (when (some? idx)
        (swap! *state
               (fn [s]
                 (-> s
                     (update :players
                             (fn [players]
                               (vec (concat (take idx players) (drop (inc idx) players)))))
                     (update :players-count dec))))))
    :initial-phase-game (do
                          (swap! *state assoc :fx/type initial-phase-game)
                          (swap! *state assoc :settlement-build true))
    :start-game-view (swap! *state assoc :fx/type start-game-view)
    :dice-view  (do
                  (swap! *state assoc :dice-1 (utils/random-dice-number) :dice-2 (utils/random-dice-number))
                  (let [dice-sum (+ (:dice-1 @*state) (:dice-2 @*state))]
                    (if (= dice-sum 7)
                     (do
                       (swap! *state assoc :game-massage "SELECT THE AREA YOU WANT TO RESTRICT")
                       (swap! *state assoc :move-thief true)
                       )
                      (doseq [coord (:spots @*state)]
                        (filing-hand-with-resource (:spot-coordinates (:on-mouse-clicked coord))
                                                   dice-sum)))))
    :spots-click
      (let [coords (:spot-coordinates event)
            phase  (:phase @*state)
            areas (:areas @*state)
            ]
        (cond
          (= phase "Initial")
          (do
            (when (:settlement-build @*state)
              (build-settlement coords))
            (swap! *state assoc :settlement-build false)
            (swap! *state assoc :road-build true)
            (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL ROAD CONNECTED WITH YOUR SETTLEMENT"))

          (= phase "Second-Initial")
          (do
            (when (:settlement-build @*state)
              (build-settlement coords))
            (swap! *state assoc :settlement-build false)
            (swap! *state assoc :road-build true)
            (swap! *state assoc :initial-info "SECOND INITIAL PHASE, SELECT ROAD CONNECTED TO YOUR SETTLEMENT")
            (let [matched-areas (filter (fn [area] (some #{coords} (:spots area))) areas)
                  all-resources (remove #(= % "dust") (map :resource matched-areas))
                  player-turn (:player-turn @*state)
                  player-idx (dec player-turn)]
              (swap! *state assoc-in [:players player-idx :hand] all-resources))
            )
        :else
        (do
          (cond
            (:settlement-build @*state) (build-settlement coords)
            (:town-build @*state)       (build-town coords)
            :else                       (println "Nothing active to build"))
          (swap! *state assoc :settlement-build false)
          (swap! *state assoc :town-build false))))

    :roads-click
    (let [road-coords (:road-coordinates event)
          phase       (:phase @*state)
          road-active (:road-build @*state)
          player-turn (:player-turn @*state)
          player-count (count (:players @*state))]
      (if road-active
        (do
          (build-road road-coords)
          (swap! *state assoc :road-build false)

          (when (= phase "Initial")
            (if (= player-turn player-count)
              (do
                (swap! *state assoc :phase "Second-Initial")
                (swap! *state assoc :settlement-build true)
                (swap! *state assoc :initial-info "SECOND INITIAL PHASE - PLACE YOUR SECOND SETTLEMENT"))
              (do
                (swap! *state assoc :settlement-build true)
                (swap! *state update :player-turn #(player-turn-inc % player-count))
                (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS"))))
          (when (= phase "Second-Initial")
            (swap! *state assoc :settlement-build true)
            (when (= player-turn 1)
              (swap! *state assoc :phase "Game")
              (swap! *state assoc :initial-info "GAME")
              (swap! *state assoc :settlement-build false)
              (swap! *state assoc :fx/type game-view))
            (when (not= player-turn 1)
              (swap! *state update :player-turn #(player-turn-dec %))
              (swap! *state assoc :initial-info "SECOND INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS"))))
        (println "No active to build road, press buy road button")))

    :circle-click
    (if (:move-thief @*state)
      (let [coords (:center-coordinates event)
            areas (:areas @*state)
            restricted-area (:restricted-area @*state)
            restricted-number (:restricted-number @*state)
            clicked-area (some #(when (= (:center %) coords) %) areas)
            clicked-number (:number clicked-area)
            areas-restored (if (and restricted-area (some? restricted-number))
                             (mapv (fn [area]
                                     (if (= (:center area) restricted-area)
                                       (assoc area :number restricted-number)
                                       area))
                                   areas)
                             areas)
            areas-updated (mapv (fn [area]
                                  (if (= (:center area) coords)
                                    (assoc area :number nil)
                                    area))
                                areas-restored)]
        (swap! *state assoc :areas areas-updated)
        (swap! *state assoc :restricted-area coords)
        (swap! *state assoc :restricted-number clicked-number)
        (swap! *state assoc :move-thief false)
        )
      (swap! *state assoc :game-massage "YOU CAN'T MOVE THIEF IF YOU DIDN'T GET 7 ON DICE"))


    :set-input-name (swap! *state assoc :input-name (:fx/event event))
    :set-input-color (swap! *state assoc :input-color (:fx/event event))
    :end-turn (swap! *state (fn [s](assoc s :player-turn (player-turn-inc (:player-turn s) (count (:players s))))))
    :buy-dev-card-btn (take-development-card)
    :buy-settlement-btn (do
                          (swap! *state assoc :settlement-build true)
                          (swap! *state assoc :town-build false)
                          (swap! *state assoc :road-build false))
    :buy-town-btn (do
                    (swap! *state assoc :town-build true)
                    (swap! *state assoc :settlement-build false)
                    (swap! *state assoc :road-build false))
    :buy-road-btn (do
                    (swap! *state assoc :road-build true)
                    (swap! *state assoc :town-build false)
                    (swap! *state assoc :settlement-build false))
    :buy-card-btn
    nil))
(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler event-handler}))
(defn start-game []
  (fx/mount-renderer *state renderer)
  (swap! *state assoc :fx/type start-game-view))

