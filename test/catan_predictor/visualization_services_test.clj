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
(fact "remove-once removes first occurrence of item from collection"
      (services/remove-once "grain" ["wood" "grain" "grain" "ore"]) => ["wood" "grain" "ore"] ; removes first "grain"
      (services/remove-once "brick" ["wood" "grain" "ore"]) => ["wood" "grain" "ore"] ; item not found, returns same
      (services/remove-once "wood" ["wood" "grain" "wood"]) => ["grain" "wood"] ; removes first "wood"
      (services/remove-once "ore" []) => [] ; empty collection returns empty
      )
(fact "create-area creates area map with correct keys and updates atoms"
      (let [resources (atom ["wood" "grain" "dust"])
            numbers (atom [5 8 10])
            center [0 0]
            points [[0 1] [1 0] [1 1] [2 2]]]
        (let [area (services/create-area center points resources numbers)]
          (:center area) => center                          ;if center is center of area
          (contains? area :resource) => true                ;if is resource exist in area
          (contains? area :number) => true                  ;if is number exist in area
          (contains? area :spots) => true                   ;if is spots exist in area
          (some #(= (:resource area) %) ["wood" "grain" "dust"]) => true ; if exist in area resource some wood grain dust..
          (if (= (:resource area) "dust")                   ;if dust is resource return nil in number
            (:number area) => nil
            (some #(= (:number area) %) [5 8 10]) => true)
          (not-any? #(= (:resource area) %) @resources) => true
          (if (:number area) (not-any? #(= (:number area) %) @numbers) => true
                             true))))
(fact "create-areas-from-centers returns collection of areas with correct structure"
      (let [resources (atom ["wood" "grain" "dust"])
            numbers (atom [5 8 10])
            points [[0 1] [1 0] [1 1] [2 2]]
            centers [[0 0] [1 1] [2 2]]
            areas (services/create-areas-from-centers points centers resources numbers)]
        (count areas) => (count centers)
        (every? map? areas) => true
        (every? #(contains? % :center) areas) => true
        (every? #(contains? % :resource) areas) => true
        (every? #(contains? % :spots) areas) => true
        (every? #(contains? % :number) areas) => true))
(fact "coords-in-roads? returns true if coords are in player's roads, else nil"
      (let [state (atom {:players [{:name "A"
                                    :roads [[[0 0] [1 0]] [[2 2] [3 3]]]}
                                   {:name "B"
                                    :roads [[[4 4] [5 5]]]}]})]
        (services/coords-in-roads? [0 0] "A" state) => true
        (services/coords-in-roads? [1 0] "A" state) => true
        (services/coords-in-roads? [2 2] "A" state) => true
        (services/coords-in-roads? [3 3] "A" state) => true
        (services/coords-in-roads? [5 5] "A" state) => nil
        (services/coords-in-roads? [4 4] "B" state) => true
        (services/coords-in-roads? [6 6] "B" state) => nil
        (services/coords-in-roads? [0 0] "C" state) => nil))
(fact "build-town updates the game state correctly when upgrading a settlement to a town"
      (let [initial-state (atom
                            {:player-turn 1
                             :players [{:name "player1"
                                        :hand {:ore 3 :grain 2 :other 5}
                                        :settlement [[1 1] [2 2]]
                                        :towns []}
                                       {:name "player2"
                                        :hand {:ore 5 :grain 5}
                                        :settlement [[3 3]]
                                        :towns []}]
                             :spots [{:on-mouse-clicked {:spot-coordinates [1 1]} :radius 10}
                                     {:on-mouse-clicked {:spot-coordinates [2 2]} :radius 10}
                                     {:on-mouse-clicked {:spot-coordinates [3 3]} :radius 10}]})
            coords [1 1]]
        (services/build-town coords initial-state)
        (not-any? #(= coords %) (get-in @initial-state [:players 0 :settlement])) => true ;Check that the settlement at coords is removed
        (some #(= coords %) (get-in @initial-state [:players 0 :towns])) => true ;Check that the coords are added to towns
        (let [spots (:spots @initial-state)                 ;Find the spot with matching coordinates
              matching-spot (first (filter #(= (:spot-coordinates (:on-mouse-clicked %)) coords) spots))
              updated-radius (:radius matching-spot)]
          (= updated-radius 20)) => true
        (not= {:ore 3 :grain 2 :other 5} (get-in @initial-state [:players 0 :hand])) => true)) ;Check that the player's hand has changed (since buy-town is not mocked)
(fact "build-settlement updates game state correctly when building settlement during game phase"
      (let [initial-state (atom
                            {:phase "Game"
                             :player-turn 1
                             :players [{:name "player1"
                                        :hand {:brick 1 :wood 1 :grain 1 :sheep 1}
                                        :settlement []
                                        :color "red"}
                                       {:name "player2"
                                        :hand {:brick 2 :wood 2}
                                        :settlement []
                                        :color "blue"}]
                             :spots [{:on-mouse-clicked {:spot-coordinates [1 1]} :fill "white"}
                                     {:on-mouse-clicked {:spot-coordinates [2 2]} :fill "white"}]})
            coords [1 1]]

        (with-redefs [services/coords-in-roads? (fn [_ _ _] true)] ;  coords-in-roads? to always return true for testing settlement build logic.
          (services/build-settlement coords initial-state)
          (:fill (first (filter #(= (:spot-coordinates (:on-mouse-clicked %)) coords)
                                (:spots @initial-state)))) => "red" ;color to red
          (some #(= coords %) (get-in @initial-state [:players 0 :settlement])) => true ; coords in settlements
          (not= {:brick 1 :wood 1 :grain 1 :sheep 1}
                (get-in @initial-state [:players 0 :hand])) => true ;spend resources
          (:game-massage @initial-state) => "Successful build settlement"))) ;message
(fact "build-road updates game state correctly when building a road during game phase"
      (with-redefs [services/coords-in-settlement-or-roads?
                    (fn [coords player-name state]
                      (or (= coords [[-1.541 0.000] [0.265 0.541]])
                          (= coords [[0.265 0.541] [-1.541 0.000]])))] ;coords in settlement in road
        (let [initial-state (atom
                              {:phase "Game"
                               :player-turn 1
                               :players [{:name "player1"
                                          :hand {:brick 1 :wood 1 :other 3}
                                          :roads []
                                          :color "red"}
                                         {:name "player2"
                                          :hand {:brick 2 :wood 2}
                                          :roads []
                                          :color "blue"}]
                               :roads [{:on-mouse-clicked {:road-coordinates [[-1.541 0.000] [0.265 0.541]]} :stroke "gray"}
                                       {:on-mouse-clicked {:road-coordinates [[12.21 12.2] [321.2 432.1]]} :stroke "gray"}]})
              coords [[-1.541 0.000] [0.265 0.541]]]
          (services/build-road coords initial-state)
          (let [roads (:roads @initial-state)
                updated-road (first (filter #(= (:road-coordinates (:on-mouse-clicked %)) coords) roads))]
            (:stroke updated-road)) => "red"                ;check does change collor
          (some #(= coords %) (get-in @initial-state [:players 0 :roads])) => true ;added to player roads
          (not= {:brick 1 :wood 1 :other 3} (get-in @initial-state [:players 0 :hand])) => true))) ; hand updated (resources spent)
(fact "get-dice-image-url returns correct image URL for dice number"
      (services/get-dice-image-url 2) => "file:resources/static/dice-2.png"
      (services/get-dice-image-url 3) => "file:resources/static/dice-3.png"
      (services/get-dice-image-url 6) => "file:resources/static/dice-6.png")
(fact "player-turn-inc increments player index and resets to 1 after last player"
      (services/player-turn-inc 1 4) => 2     ; after 1 go 2
      (services/player-turn-inc  3 4) => 4     ; after 3 go 4
      (services/player-turn-inc  4 4) => 1     ; after 4 go 1
      (services/player-turn-inc  5 5) => 1     ; after 5, go 1
      (services/player-turn-inc  2 5) => 3)
(fact "player-turn-dec decreases player number by 1"
      (services/player-turn-dec 3) => 2
      (services/player-turn-dec 1) => 0
      (services/player-turn-dec 10) => 9)
(fact "update-winner sets winner and view if player has 10+ vp"
      (let [state (atom {:players [{:name "A" :vp 10} {:name "B" :vp 5}]})]
        (services/update-winner state :end) => {:players [{:name "A" :vp 10} {:name "B" :vp 5}] :winner "A" :fx/type :end}))
(fact "update-winner does nothing if no player has 10+ vp"
      (let [state (atom {:players [{:name "A" :vp 7} {:name "B" :vp 9}]})]
        (services/update-winner state :end) => {:players [{:name "A" :vp 7} {:name "B" :vp 9}]}))
(fact "update-winner sets winner and view if player has more than 10 vp"
      (let [state (atom {:players [{:name "A" :vp 11} {:name "B" :vp 5}]})]
        (services/update-winner state :end) => {:players [{:name "A" :vp 11} {:name "B" :vp 5}] :winner "A" :fx/type :end}))
(fact "take-resources returns correct resources for a given spot and number"
      (let [state (atom {:areas [{:number "6" :resource "wood" :spots [[0 0] [1 1]]}
                                 {:number "6" :resource "brick" :spots [[2 2] [3 3]]}
                                 {:number "8" :resource "grain" :spots [[0 0] [4 4]]}]})]

        (services/take-resources [0 0] 6 state) => ["wood"]
        (services/take-resources [2 2] 6 state) => ["brick"]
        (services/take-resources [0 0] 8 state) => ["grain"]
        (services/take-resources [9 9] 6 state) => []))
(fact "filing-hand-with-resource adds correct resources to players based on settlements and towns"
      (let [state (atom {:areas [{:number "6" :resource "wood" :spots [[1 1] [2 2]]}
                                 {:number "6" :resource "wool" :spots [[2 1] [2 2]]}
                                 {:number "8" :resource "wood" :spots [[1 1] [1 2]]}]
                         :players [{:name "Player1" :settlement [[1 1]] :towns [] :hand [ "ore"]}
                                   {:name "Player2" :settlement [] :towns [[1 1]] :hand []} ; nothing in towns and towns
                                   {:name "Player3" :settlement [] :towns [] :hand ["ore"]}]})] ; nothing in settlements and towns
        (services/filing-hand-with-resource [1 1] 6 state)
        (:hand (first (:players @state))) => ["ore" "wood"]          ; settlement - one resource
        (:hand (second (:players @state))) => ["wood" "wood"]  ; town - doubled resource
        (:hand (nth (:players @state) 2)) => ["ore"]))
(fact "take-development-card should give player a specific card and update deck and hand"
      (let [initial-dev-deck ["knight" "knight" "road-building" "victory-point" "victory-point"]
            dummy-hand ["grain" "wool" "wood" "ore" "brick" "ore"]
            state (atom {:player-turn 1
                         :development-deck initial-dev-deck
                         :players [{:dev-cards [] :hand dummy-hand}
                                   {:dev-cards [] :hand dummy-hand}]})]

        (with-redefs [rand-nth (fn [_] "knight")]
          (services/take-development-card state)

          (:development-deck @state) => (just ["knight" "road-building" "victory-point" "victory-point"])
          (get-in @state [:players 0 :dev-cards]) => ["knight"]
          (sort (get-in @state [:players 0 :hand])) => (just ["brick" "ore" "wood"]))))
(fact "coords-in-settlement-or-roads? returns true if any coord is in player's settlement or roads"
      (let [state (atom {:players [{:name "A"
                                    :settlement [[1.0 2.0] [3.0 4.0] [5.0 6.0]]
                                    :roads [[[1.0 2.0] [4.0 5.0]]]}
                                   {:name "B"
                                    :settlement [[6.0 7.0]]
                                    :roads   [[[2.0 5.0] [7.0 8.0]] [[10.0 10.0] [12.0 12.0]]]}]})]
        (services/coords-in-settlement-or-roads? [[1.0 2.0]] "A" state) => truthy ;coords is in settlement
        (services/coords-in-settlement-or-roads? [[5.0 6.0]] "A" state) => truthy ;coords is in settlement
        (services/coords-in-settlement-or-roads? [[4.0 5.0]] "A" state) => truthy ;coords is in roads
        (services/coords-in-settlement-or-roads? [[9.0 9.0]] "A" state) => falsey ; coords is not in roads and settlement
        (services/coords-in-settlement-or-roads? [[6.0 7.0]] "B" state) => truthy ;check for second player
        (services/coords-in-settlement-or-roads? [[0.0 0.0] [3.0 4.0]] "A" state) => truthy ;at least one is match
        (services/coords-in-settlement-or-roads? [[0.0 0.0] [9.0 9.0]] "A" state) => falsey)) ;no one is match
(fact "coords-in-last-settlement? returns true if given coord matches player's last settlement"
      (let [state (atom {:players [{:name "A"
                                    :settlement [[1 2] [3 4] [5 6]]}
                                   {:name "B"
                                    :settlement [[7 8] [9 10]]}]})]
        (services/coords-in-last-settlement? [[5 6]] "A" state) => truthy ; [5 6] is last settlement
        (services/coords-in-last-settlement? [[3 4]] "A" state) => falsey ; [3 4] nije poslednja
        (services/coords-in-last-settlement? [[9 10]] "B" state) => truthy ; [9 10] last for B player
        (services/coords-in-last-settlement? [[1 1]] "B" state) => falsey)) ; [1 1] not last for B player
(fact "add-edge adds neighbor to a graph node correctly"
      (services/add-edge {1 [1.234]} 1 0.0) => {1 [1.234 0.0]} ; add neigbour if one of coords is same
)
(fact "build-graph creates a bidirectional graph from edges"
      (services/build-graph [["a" "b"] ["a" "c"]]) => {"a" ["b" "c"], "b" ["a"], "c" ["a"]} ;strings
      (services/build-graph []) => {}                       ;no edges empty
      (services/build-graph [[0.123 0] [0 1.3]]) => {0.123 [0], 0 [0.123 1.3], 1.3 [0]}) ;numbers
(fact "Testing find-all-paths with numeric nodes"
      (let [test-graph
            {1   [0.0 1.4]
             0.0 [1.4 2]
             1.4 [2]
             2   []}]
        (services/find-all-paths test-graph 1 2) => (just [[1 0.0 1.4 2] [1 0.0 2] [1 1.4 2]] :in-any-order) ;
        (services/find-all-paths test-graph 1 1.4) => (just [[1 0.0 1.4] [1 1.4]] :in-any-order)
        (services/find-all-paths test-graph 2 1) => empty?     ; no paths
        (services/find-all-paths test-graph 0.0 2) => (just [[0.0 1.4 2] [0.0 2]] :in-any-order)))
(fact "returns the longest path from a collection of paths"
     (services/longest-path  [[1 2 3 4] [1 2 3] [1 2]]) => [1 2 3 4]
     (services/longest-path  []) => []
     (services/longest-path  [[1]]) => [1]
      (services/longest-path [[1 2] [3 4]]) => [1 2]) ; if is same length, return first
(fact "longest-route-length returns correct length for simple chain"
      (let [roads [[[0.0 1.0] [1.0 2.0]]
                   [[1.0 2.0] [3.0 4.0]]
                   [[4.0 5.0] [3.0 4.0]]]]
        (services/longest-route-length roads)
        => 3)) ; cuz nodes A-B-C-D, path 3 (4 nodes - 1)
(fact "update-players-vp calculate victory point of each player"
      (let [state (atom {:players [{:name "A"
                                    :settlement [[0 1.2] [2.32 -5] ["a" "b"]]  ; 3 settlements = 3 VP
                                    :towns [["s" "c"]]             ; 1 town * 2 = 2 VP
                                    :dev-cards ["victory-point" "knight"]} ; 1
                                   {:name "B"
                                    :settlement [["dsadass" "a"]]        ; 1 settlement = 1 VP
                                    :towns [[43.12 32.2] [2.31 231.2]]         ; 2 towns * 2 = 4 VP
                                    :dev-cards ["knight" "knight"]}]
                         :winner-longest-route "A"
                         :winner-army-size "B"})]
        (services/update-players-vp state)
        (let [players (:players @state)
              A (first players)
              B (second players)]

          ;; Očekivanja
          (:vp A) => (+ 3 2 1 2 0) ; settlements + towns + victory-point cards + the longest route + the biggest army (not)
          (:vp B) => (+ 1 4 0 0 2)))) ; settlements + towns + victory-point cards + the longest route + the biggest army
(fact "update-current-player-road-length! update road length of current player"
      (let [state (atom {:player-turn 1
                         :players [{:name "A" :road-length 0 :roads [
                                                                     [[1 1] [1 1]] ; no connected with other
                                                                     [[0.866 2.5] [-0.866 5]] ; connected with last
                                                                     [[0.0 2.0] [0.866 2.3]] ; connected with last
                                                                     [[0.0 2.0] [0.866 2.5]]]
                                    }]})]
        (services/update-current-player-road-length! state)
        (get-in @state [:players 0 :road-length]) => 3))
(fact "player-with-longest-route assigns correct player as winner of longest route"
      (let [state (atom {:players [{:name "A" :road-length 2}
                                   {:name "B" :road-length 4}
                                   {:name "C" :road-length 3}]
                         :winner-longest-route "A"})]
        (services/player-with-longest-route state)
        (:winner-longest-route @state) => "B"))
(fact "player-with-largest-army assigns correct player as winner of largest army"
      (let [state (atom {:players [{:name "A" :army-size 2}
                                   {:name "B" :army-size 4}
                                   {:name "C" :army-size 3}]
                         :winner-army-size "A"})]
        (services/player-with-largest-army state)
        (:winner-army-size @state) => "B"))
(fact "update-all-hands-second-half removes half of shuffled cards if player has more than 7"
      (let [initial-hand (range 10)                         ; 0 1 2 3 4 .. 9
            state (atom {:players [{:name "A" :hand initial-hand}
                                   {:name "B" :hand ["1" "2" "3" "4" "5" "6" "7" "7" "7"]}
                                   {:name "C" :hand ["1" "2" "3" "4" "5" "6" "7" ]}
                                   {:name "D" :hand [1 2 3 4 5]}]})] ; less od 7

        (services/update-all-hands-second-half state)
        (count (get-in @state [:players 0 :hand])) => 5     ; count 5, because of shuffle order is not same
        (count (get-in @state [:players 1 :hand])) => 5     ; odd number, 9/2, 4.5 round on 5, because of shuffle order is not same
        (get-in @state [:players 2 :hand])  => ["1" "2" "3" "4" "5" "6" "7" ]     ; have 7 strings, no changes
        (get-in @state [:players 3 :hand]) => [1 2 3 4 5])) ; no shuffle, nothing change
