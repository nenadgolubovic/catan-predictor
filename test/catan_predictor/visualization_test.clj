(ns catan-predictor.visualization-test
  (:require [clojure.test :refer :all]
            [catan-predictor.visualization :as vis]
            [midje.sweet :refer :all]
            [catan-predictor.visualization-services :as services]
            )
  )



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
(fact ":activate-dev-card removes activated dev card and triggers effects correctly"
      (reset! vis/*state
              {:player-turn 1
               :clicked-resource "knight"
               :game-massage nil
               :move-thief false
               :on-dice-7 false
               :road-build false
               :build-roads-activate false
               :players [{:dev-cards ["knight" "road-building" "vp-card"]
                          :army-size 0
                          :victory-points 0}]})                                ; Reset state with initial player and dev cards
      (vis/event-handler {:event/type :activate-dev-card})                    ; Call activate-dev-card event
      (get-in @vis/*state [:players 0 :dev-cards]) => (contains ["road-building" "vp-card"]) ; Check if activated card "knight" is removed from player's dev cards
      (:game-massage @vis/*state) => "SELECT THE AREA YOU WANT TO RESTRICT"    ; Verify game message is set to prompt thief move
      (:move-thief @vis/*state) => true                                        ; Confirm move-thief flag is true, enabling thief movement
      (:on-dice-7 @vis/*state) => true                                         ; Confirm that dice 7 related state is set to true
      (get-in @vis/*state [:players 0 :army-size]) => 1                        ; Army size of current player should increment by 1
      (:clicked-resource @vis/*state) => nil                                   ; clicked-resource should be cleared after activation
      (swap! vis/*state assoc
             :clicked-resource "road-building"                                ; Change clicked-resource to "road-building" for next test
             :players [{:dev-cards ["road-building" "vp-card"]
                        :army-size 1}])
      (vis/event-handler {:event/type :activate-dev-card})                    ; Call activate-dev-card event again for road-building
      (get-in @vis/*state [:players 0 :dev-cards]) => (contains ["vp-card"])  ; Check if "road-building" card is removed
      (:road-build @vis/*state) => true                                       ; Confirm road-building flag is set to true for building roads
      (:game-massage @vis/*state) => "YOU ACTIVATED CARD FOR BUILDING 2 ROAD, PLEASE SELECT 2" ; Check game message prompts player to build roads
      (:build-roads-activate @vis/*state) => true                             ; Confirm build-roads-activate flag is true to allow road building
      (:clicked-resource @vis/*state) => nil)                                 ; clicked-resource cleared again after activation
(fact ":add event adds player to players and clears inputs"
      (reset! vis/*state {:players []
                          :input-name "A"
                          :input-color :red})
      (vis/event-handler {:event/type :add})
      (let [players (:players @vis/*state)]
        (count players) => 1
        (first players) => {:name "A" :color :red})
      (:input-name @vis/*state) => ""
      (:input-color @vis/*state) => nil)
(fact ":remove event removes player at given index from players and decrements players-count"
      (reset! vis/*state {:players [{:name "A"} {:name "B"} {:name "C"}]
                          :players-count 3})
      (vis/event-handler {:event/type :remove :index 1})
      (:players @vis/*state) => [{:name "A"} {:name "C"}]
      (:players-count @vis/*state) => 2)                    ;player-count decreased by 1
(fact ":dice-view rolls two dice and updates state correctly"
      (reset! vis/*state
              {:spots [{:spot-coordinates {:x 1 :y 2}}]
               :game-message nil
               :move-thief false
               :on-dice-7 false
               :dice-rolled false})
      (vis/event-handler {:event/type :dice-view})
      (:dice-1 @vis/*state) => #(and (integer? %) (<= 1 % 6)) ;Check dice values are between 1 and 6
      (:dice-2 @vis/*state) => #(and (integer? %) (<= 1 % 6)) ;Check dice values are between 1 and 6
      (let [dice-sum (+ (:dice-1 @vis/*state) (:dice-2 @vis/*state))] ;Dice sum
        (if (= dice-sum 7)
          (do
            (:game-message @vis/*state) => "SELECT THE AREA YOU WANT TO RESTRICT" ;If sum is 7, check message and flags are set
            (:move-thief @vis/*state) => true
            (:on-dice-7 @vis/*state) => true)
          (do
            (:game-message @vis/*state) => nil              ;If sum != 7, then these flags should be false/nil
            (:move-thief @vis/*state) => false
            (:on-dice-7 @vis/*state) => false)))
      (:dice-rolled @vis/*state) => true)                   ;dice-rolled must be true in all cases
(fact "initial-phase-game event sets the correct state"
      (reset! vis/*state {:players [{:name "P1" :hand []}] :player-turn 1})
      (vis/event-handler {:event/type :initial-phase-game})
      (:settlement-build @vis/*state) => true
      (:fx/type @vis/*state) => vis/initial-phase-game)
(fact ":spots-click on already booked spot shows error message and does not build"
      (reset! vis/*state
              {:settlement-build false
               :town-build false
               :phase "Game"
               :booked-spots [[1 1] [2 2]]
               :players [{:name "A"} {:name "B"}]
               :game-massage nil})
      (vis/event-handler {:event/type :spots-click
                          :spot-coordinates [1 1]})
      (get @vis/*state :game-massage) => "THIS SPOTS ARE ALREADY BOOKED, TOO CLOSE TO OTHER SETTLEMENT, OR NOT CONNECTED"
      (get @vis/*state :booked-spots) => [[1 1] [2 2]])
(fact "spots-click in Second-Initial phase builds settlement, gives resources and sets flags"
      (reset! vis/*state
              {:settlement-build true
               :town-build false
               :phase "Second-Initial"
               :booked-spots []
               :players [{:name "A" :hand []} {:name "B" :hand []}]
               :player-turn 1
               :areas [{:spots [[0.0 0.0]] :resource "wood"}
                       {:spots [[0.2132 0.214]] :resource "dust"}
                       {:spots [[1.45 1.65]] :resource "brick"}]
               :initial-info nil})
      (vis/event-handler {:event/type :spots-click
                          :spot-coordinates [0.0 0.0]})
      (get @vis/*state :settlement-build) => false
      (get @vis/*state :road-build) => true
      (get @vis/*state :initial-info) => "SECOND INITIAL PHASE, SELECT ROAD CONNECTED TO YOUR SETTLEMENT"
      (some #(= % [0.0 0.0]) (get @vis/*state :booked-spots)) => true
      (get-in @vis/*state [:players 0 :hand]) => (just (every-pred #(not= % "dust") (contains ["wood"])))
      (get @vis/*state :phase) => "Second-Initial")
(fact ":roads-click in game phase builds road without changing phase"
      (reset! vis/*state
              {:road-build true
               :build-roads-activate false
               :player-turn 1
               :phase "Game"
               :players [{:name "A"} {:name "B"}]
               :booked-roads [[[1.121 23.32] [23.32 122]]]})
      (vis/event-handler {:event/type :roads-click
                          :road-coordinates [[0.216 -0.00] [0.25 -0.00]]})
      (get @vis/*state :phase) => "Game"
      (get @vis/*state :road-build) => false
      (some #(= [[0.216 -0.00] [0.25 -0.00]] %) (get @vis/*state :booked-roads)) => true)
(fact ":roads-click on already booked road shows error message and does not build"
      (reset! vis/*state
              {:road-build true
               :build-roads-activate false
               :player-turn 1
               :phase "Game"
               :players [{:name "A"} {:name "B"}]
               :booked-roads [[[0.5 0.5] [1.0 1.0]]]})
      (vis/event-handler {:event/type :roads-click
                          :road-coordinates [[0.5 0.5] [1.0 1.0]]})
      (get @vis/*state :phase) => "Game"
      (get @vis/*state :game-massage) => "THIS ROAD IS ALREADY BUILT, CHOOSE ANOTHER"
      (count (get @vis/*state :booked-roads)) => 1)
(fact ":roads-click in second-initial phase decreases player-turn"
      (reset! vis/*state
              {:road-build true
               :build-roads-activate false
               :player-turn 4
               :phase "Second-Initial"
               :players [{:name "A"} {:name "B"} {:name "V"} {:name "D"}]
               :booked-roads [[21546  123] [123 0]]})
      (vis/event-handler {:event/type :roads-click
                          :road-coordinates [[21546  125] [125 0]]})
      (get @vis/*state :phase) => "Second-Initial"
      (get @vis/*state :player-turn) => 3
      (some #(= [[21546  125] [125 0]] %) (get @vis/*state :booked-roads)) => true)
(fact ":roads-click in initial phase switches to Second-Initial phase"
      (reset! vis/*state
              {:road-build true
               :build-roads-activate false
               :player-turn 4
               :phase "Initial"
               :players [{:name "A"} {:name "B"} {:name "C"} {:name "D"}]
               :booked-roads []})
      (vis/event-handler {:event/type :roads-click
                          :road-coordinates [[12.2121 321.421] [12.2121  321]]})
      (get @vis/*state :phase) => "Second-Initial"
      (get @vis/*state :settlement-build) => true
      (some #(= [[12.2121 321.421] [12.2121  321]] %) (get @vis/*state :booked-roads)) => true)
(fact ":circle-click updates state correctly when move-thief is active"
      (reset! vis/*state
              {:move-thief true
               :restricted-area [0 0]
               :restricted-number 8
               :areas [{:center [1 1] :number 5}
                       {:center [0 0] :number 8}
                       {:center [2 2] :number 9}]})
      (vis/event-handler {:event/type :circle-click         ; Simulate a circle click event with coordinates matching one of the areas
                          :center-coordinates [1 1]})
      (let [areas (:areas @vis/*state)
            restricted-area (:restricted-area @vis/*state)
            restricted-number (:restricted-number @vis/*state)
            move-thief (:move-thief @vis/*state)
            on-dice-7 (:on-dice-7 @vis/*state)]
        (get-in areas [1 :number]) => 8                     ;Area [0 0] restored to 8
        (get-in areas [0 :number]) => nil                   ;Area [1 1] number nil because thief moved there
        restricted-area => [1 1]                            ;Restricted area updated to clicked coords
        restricted-number => 5                              ;Restricted number updated to clicked area's number before clearing
        move-thief => false                                 ; move-thief disabled after move
        on-dice-7 => false))                                ;on-dice-7 reset
(fact ":circle-click shows message when move-thief not active"
      (reset! vis/*state {:move-thief false})
      (vis/event-handler {:event/type :circle-click
                          :center-coordinates [1 1]})
      (:game-massage @vis/*state) => "YOU CAN'T MOVE THIEF IF YOU DIDN'T GET 7 ON DICE") ; message
(fact "set-input-name event updates :input-name in state"
      (reset! vis/*state {:input-name nil})
      (vis/event-handler {:event/type :set-input-name
                      :fx/event "A"})
      (:input-name @vis/*state) => "A")
(fact "set-input-color event updates :input-color in state"
      (reset! vis/*state {:input-color nil})
      (vis/event-handler {:event/type :set-input-color
                      :fx/event "red"})
      (:input-color @vis/*state) => "red")
(fact ":end-turn updates player turn and resets state flags"
      (reset! vis/*state
              {:player-turn 1
               :players [{:hand ["wood"]} {:hand ["brick"]}]
               :clicked-resource "wood"
               :dice-rolled true})
      (vis/event-handler {:event/type :end-turn})
      (:player-turn @vis/*state) => 2                       ;player-turn should increment from 1 to 2 (assuming players count is 2)
      (:clicked-resource @vis/*state) => nil                ;clicked-resource should be reset to nil
      (:dice-rolled @vis/*state) => false)                  ;dice-rolled should be reset to false
(fact "buy-dev-card-btn event calls take-development-card and update-players-vp"
      (reset! vis/*state {:players [{:name "P1" :hand [] :vp 0}] :player-turn 1})
      (with-redefs [services/take-development-card (fn [state] (swap! state update-in [:players 0 :hand] conj :dev-card)) ;check does func called
                    services/update-players-vp (fn [state] (swap! state update-in [:players 0 :vp] inc))] ;check does func called
        (vis/event-handler {:event/type :buy-dev-card-btn})
        (get-in @vis/*state [:players 0 :hand]) => (contains :dev-card) ;check dev-card
        (get-in @vis/*state [:players 0 :vp]) => 1))        ;check does it returned vp
(fact ":buy-settlement-btn event updates the state correctly"
      (reset! vis/*state {:settlement-build false
                          :town-build true
                          :road-build true
                          :game-message nil})
      (vis/event-handler {:event/type :buy-settlement-btn})
      (:settlement-build @vis/*state) => true
      (:town-build @vis/*state) => false
      (:road-build @vis/*state) => false
      (:game-message @vis/*state) => "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD SETTLEMENT")
(fact "buy-town-btn event updates the state correctly"
      (reset! vis/*state {:town-build false
                          :settlement-build true
                          :road-build true
                          :game-message nil})
      (vis/event-handler {:event/type :buy-town-btn})
      (:town-build @vis/*state) => true
      (:settlement-build @vis/*state) => false
      (:road-build @vis/*state) => false
      (:game-message @vis/*state) => "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD TOWN")
(fact ":buy-road-btn event updates state correctly"
      (reset! vis/*state {:road-build false
                      :town-build true
                      :settlement-build true
                      :game-message nil})

      (vis/event-handler {:event/type :buy-road-btn})
      (:road-build @vis/*state) => true
      (:town-build @vis/*state) => false
      (:settlement-build @vis/*state) => false
      (:game-message @vis/*state) => "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD ROAD")
(fact ":buy-card-btn event updates the state correctly"
      (reset! vis/*state {:shop-view false
                      :card-shop-buy false
                      :clicked-resource "some-resource"
                      :sell-resource "some-sell"
                      :buy-resource "some-buy"})
      (vis/event-handler {:event/type :buy-card-btn})
      (:shop-view @vis/*state) => true
      (:card-shop-buy @vis/*state) => true
      (:clicked-resource @vis/*state) => nil
      (:sell-resource @vis/*state) => nil
      (:buy-resource @vis/*state) => nil)
(fact "buy-card-btn event updates the state correctly"
      (reset! vis/*state {:shop-view false
                          :card-shop-buy false
                          :clicked-resource "some-resource"
                          :sell-resource "some-sell"
                          :buy-resource "some-buy"})
      (vis/event-handler {:event/type :buy-card-btn})
      (:shop-view @vis/*state) => true
      (:card-shop-buy @vis/*state) => true
      (:clicked-resource @vis/*state) => nil
      (:sell-resource @vis/*state) => nil
      (:buy-resource @vis/*state) => nil)
(fact ":card-click event sets :clicked-resource correctly"
      (reset! vis/*state {:clicked-resource nil})
      (let [event {:event/type :card-click
                   :resource "knight"}]
        (vis/event-handler event)
        (:clicked-resource @vis/*state) => "knight"))
(fact ":buy-this-card updates state correctly"
      (reset! vis/*state                                    ;Scenario 1:when :take-resource-card? is true - just close the shop and clear clicked-resource
              {:player-turn 1
               :take-resource-card? true
               :card-shop-buy true
               :clicked-resource "brick"
               :players [{:hand ["wood" "wood"]}]})
      (vis/event-handler {:event/type :buy-this-card})
      (:card-shop-buy @vis/*state) => false
      (:clicked-resource @vis/*state) => nil
      (get-in @vis/*state [:players 0 :hand]) => ["wood" "wood"] ; hand does not change

      (reset! vis/*state                                    ;Scenario 2: when :take-resource-card? is false - the card is added to the hand
              {:player-turn 1
               :take-resource-card? false
               :card-shop-buy true
               :clicked-resource "brick"
               :buy-resource "knight"
               :players [{:hand ["wood" "wood"]}]})
      (vis/event-handler {:event/type :buy-this-card})
      (:card-shop-buy @vis/*state) => false                 ;card-shop-buy becomes false
      (:card-shop-sell @vis/*state) => true                 ;card-shop-sell becomes true
      (:buy-resource @vis/*state) => "brick"                ;buy-resource is set to clicked-resource before clearing
      (:clicked-resource @vis/*state) => nil                ;clicked-resource is cleared
      (:sell-resource @vis/*state) => nil                   ;sell-resource is cleared
      (get-in @vis/*state [:players 0 :hand]) => (contains ["wood" "wood" "knight"])) ;the card is added to the player's hand
(fact "sell-this-card sells 4 resources for 1 card if player has enough resources, otherwise shows message"
      (reset! vis/*state
              {:player-turn 1
               :players [{:hand ["wood" "wood" "wood" "wood" "brick"]}]
               :clicked-resource "wood"
               :buy-resource "knight"
               :shop-view true
               :card-shop-sell true})

      (vis/event-handler {:event/type :sell-this-card})
      (:shop-view @vis/*state) => false
      (:card-shop-sell @vis/*state) => false
      (get-in @vis/*state [:players 0 :hand]) => (contains ["brick" "knight"] :in-any-order)
      (:clicked-resource @vis/*state) => nil
      (:sell-resource @vis/*state) => nil
      (:buy-resource @vis/*state) => nil
      (contains? @vis/*state :game-message) => false)
