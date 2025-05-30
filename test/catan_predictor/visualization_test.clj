(ns catan-predictor.visualization-test
  (:require [clojure.test :refer :all]
            [catan-predictor.visualization :as vis]
            [midje.sweet :refer :all]
            [catan-predictor.visualization-services :as services]
            [catan-predictor.visualization-elements :as elem]
            )
  (:import (javafx.geometry Rectangle2D)
           [javafx.stage Screen]))


(defonce *state-test
         (atom {:dice-1      2                              ;info of dice number, initialize with 2 (we can initialize with every number
                :dice-2      2                       ;info of second dice number
                :player-turn 1                       ;player on turn (index of him +1), if we have 4 players this could take value from 1 to 4
                :town-build false                    ;does it allow to build town, not on start
                :settlement-build false              ;does it allow to build settlement, not on start
                :road-build false                    ;does it allow to build roads, not on start
                :restricted-area nil                 ;which area is restricted, when move thief this will change nil to info of restricted area
                :restricted-number nil               ;which number is restricted, number stated on restricted area
                :clicked-resource nil                ;which resource is clicked during game, if you press on some cards that nil will change to name of that card, important to state know what is clicked and to resize that card
                :buy-resource nil                    ;which resource is selected for buying
                :on-dice-7 false                     ;info does it take 7 on dice rolling
                :sell-resource nil                   ;which resource is selected for selling
                :move-thief false                    ;does is allow to move thief
                :card-shop-buy false                 ;present does is open shop
                :dice-rolled false                   ;does it dice rolled during same turn
                :take-resource-card? false           ;
                :winner-longest-route nil            ;present which player has the longest route
                :shop-view false                     ;does shop view available
                :winner-army-size nil                ;who has the biggest army, who activate most knights
                :activated-road-building false       ;present does is activate development card "road building"
                :winner nil                          ;present winner
                :game-massage "WELCOME"              ;game-message
                :phase "Initial"                     ;phase of the game
                :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS" ;initial info on start game
                :players []                          ;current players in game
                :players-count 0                     ;count of players
                :spots (map #(elem/create-spot-view (first %) (second %)) (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732)))) ;make spots , from center 0.0 with distance 1.732 (2*cos30) find another center, make spots from centers and create spot view
                :booked-spots []                     ;which spots are booked
                :booked-roads []                     ;which roads are booked
                :roads (map #(elem/create-line-view (first (first %))
                                                    (second (first %))
                                                    (first (second %))
                                                    (second (second %))) (services/roads (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))))) ; create roads from centers and connect spots in format [[x1 y1] [x2 y2]]
                :areas (vec (services/create-areas-from-centers (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))) (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732)) ; make areas from center with resources and numbers
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
                                   ]                 ;development deck
                }))

(fact "start-game-view returns a valid GUI map"
      (let [view (vis/start-game-view {})]
        (map? view) => true
        (:fx/type view) => :stage                           ;does it stabe
        (:showing view) => true                             ;does it showing
        (:title view) => "CATAN"
        (get-in view [:scene :fx/type]) => :scene           ;check does it roote
        (get-in view [:scene :root :fx/type]) => :v-box     ;check does v-box exist
        (vector? (get-in view [:scene :root :children])) => true ;check is there children
        (count (get-in view [:scene :root :children])) => 4 ;4 elements in children
        (get-in view [:scene :root :children 0 :fx/type]) => :label ; check does first label text
        (get-in view [:scene :root :children 0 :text]) => "WELCOME TO CATAN!" ;check message
        (get-in view [:scene :root :children 3 :fx/type]) => :button ;check does it button with test
        (get-in view [:scene :root :children 3 :text]) => "Start Game" ;check does it Start game
        ))
(fact "initial-phase-game returns stage with expected structure"
      (reset! *state-test {:players [{:name "A" :hand []}]
                      :player-turn 1
                      :initial-info "A, place your settlement."
                      :game-massage "You can place a road now."})
      (let [view (vis/initial-phase-game *state-test)
            root-children (get-in view [:scene :root :children])]
        (:fx/type view) => :stage
        (:title view) => "GAME SETUP - CATAN"
        (get-in view [:scene :root :fx/type]) => :stack-pane;check does it stack pane
        (count root-children) => 6                          ;6  elements in root
        (let [vbox (first root-children)                    ;does it vbox and labels inside
              labels (get-in vbox [:children])]
          (count labels) => 3                               ;3 labels there
          (get-in labels [0 :text]) => "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS"
          (get-in labels [2 :text]) => "WELCOME")))
(facts "game-view basic rendering"
       (let [fake-state (atom
                          {:players [{:name "Player1"
                                      :hand ["ore" "grain" "wood" "wool" "brick"]
                                      :dev-cards nil}]
                           :player-turn 1
                           :game-massage "Welcome to Catan"
                           :dice-rolled false
                           :on-dice-7 false
                           :shop-view false
                           :clicked-resource nil})
             result (vis/game-view fake-state)]
         (fact "stage title should be CATAN"
               (:title result) => "CATAN")
         (fact "root element type should be :border-pane"
               (get-in result [:scene :root :fx/type]) => :border-pane)))
(fact "end-game-view basic rendering"
      (reset! vis/*state {:winner "Player1"})
      (let [result (vis/end-game-view @vis/*state)
            top-label (get-in result [:scene :root :top :children 0])]
        (:title result) => "CATAN - Game Over"
        (:text top-label) => "PLAYER Player1 WINS!"
        (:style top-label) => (contains "-fx-font-size: 72pt")))
(facts "start-game updates *state atom"
       (fact "sets :fx/type to start-game-view"
             (reset! vis/*state {})
             (vis/start-game)
             @vis/*state => (contains {:fx/type vis/start-game-view})))