(ns catan-predictor.visualization
  (:require [cljfx.api :as fx]
            [catan-predictor.utils :as utils]
            [catan-predictor.visualization-elements :as elem]
            [catan-predictor.visualization-services :as services]
            )
  (:import  [javafx.stage Screen]))

;; ============================================================================
;; This project is based on the board game Catan [1] [2].
;; Clojure code function based on book [3]
;; Game based on cljfx library for making GUI [4] [5]
;; Images of elements downloaded from [6]
;; ============================================================================

;; state of the game, used defonce to one value defined only once, to avoid redefined when code reload
(defonce *state
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

(defn start-game-view
  "Start game view"
  [state]
  (let [screen-bounds (.getVisualBounds (Screen/getPrimary)) ; Get the visual bounds of the primary display screen
  width (.getWidth screen-bounds); Extract the width of the visible screen area (excluding taskbar/dock)
  height (.getHeight screen-bounds)] ; Extract the height of the visible screen area
{:fx/type :stage
 :width width                                               ; put width of your screen
 :height height                                             ; put height of app as high of your screen, to be maximised
 :x (.getMinX screen-bounds)                                ; Takes the minimum x coordinate of the visible part of the screen (screen-bounds).
 :y (.getMinY screen-bounds)                                ; Takes the minimum y coordinate of the visible part of the screen (screen-bounds).
   :showing true                                            ; show window
   :title   "CATAN"                                         ; title of GUI
   :scene   {:fx/type :scene                                ; make scene
             :root    {:fx/type    :v-box                   ; elements of scene
                       :alignment :top-center               ; started on top center
                       :spacing   20
                       :padding   20
                       :background (elem/background-image)  ; background is image

                       :children [{:fx/type :label          ;Welcome message
                                   :text "WELCOME TO CATAN!"
                                   :style "-fx-text-fill: #FFD700;
                                           -fx-font-size: 60px;
                                           -fx-font-weight: bold;
                                           -fx-background-color: rgba(0, 0, 0, 0.5);
                                           -fx-padding: 20px 40px;
                                           -fx-background-radius: 10px;"
                                   }

                                  {:fx/type :v-box          ;player list present
                                   :style "-fx-background-color: white;
                                           -fx-padding: 15px;
                                           -fx-background-radius: 10px;
                                           -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 0);"
                                   :max-width 600
                                   :children [(elem/player-list *state)]}

                                  {:fx/type :h-box
                                   :spacing 20
                                   :alignment :center
                                   :children [(elem/add-button *state) ;in one row added "add button", name input box and drop-down list
                                              (elem/name-input *state)
                                              (elem/color-dropdown *state)]}

                                  {:fx/type :button         ;button for starting game
                                   :text "Start Game"
                                   :style "-fx-font-size: 40px;
                                           -fx-font-weight: bold;
                                           -fx-background-color: #FF5722;
                                           -fx-text-fill: white;
                                           -fx-padding: 10px 20px;
                                           -fx-background-radius: 5px;
                                           -fx-min-width: 400px;
                                           -fx-min-height: 100px;"
                                   :on-action {:event/type :initial-phase-game}}]}}})) ; move state of game to initial phase game
(defn initial-phase-game
  "Initial phase of game view

  Args: state-state of the game"
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state))) ;player which is on turn
        screen-bounds (.getVisualBounds (Screen/getPrimary))
        width (.getWidth screen-bounds)
        height (.getHeight screen-bounds)]
    {:fx/type :stage
     :width width
     :height height
     :x (.getMinX screen-bounds)
     :y (.getMinY screen-bounds)
     :showing true
     :title   "GAME SETUP - CATAN"
     :scene   {:fx/type :scene
               :root    {:fx/type  :stack-pane              ;stack pane
                         :style    "-fx-background-color: #1e90ff;"
                         :children [
                                    {:fx/type   :v-box
                                     :alignment :top-center
                                     :padding   10
                                     :children  [{:fx/type :label
                                                  :text    (:initial-info @*state) ;put current initial info in label
                                                  :style   "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"}
                                                 (elem/player-turn-info (:name player))
                                                 {:fx/type :label
                                                  :text    (:game-massage @*state) ; put current game message in label
                                                  :style   "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"}]}
                                    {:fx/type   :h-box
                                     :alignment :center-right
                                     :children  [{:fx/type   :v-box
                                                  :alignment :center
                                                  :children  [(elem/table-info *state) ; present table info
                                                              ]}]}
                                    (elem/hand-view (:hand player) *state) ;present hand of player on turn
                                    (elem/image-group state) ;present areas (board)
                                    (elem/roads-view *state) ;roads
                                    (elem/spots-view *state) ;spots
                                    ]}}}))                  ; Whe all process are finished state will change view to game view

(defn game-view
  "Game view of game, main view

  Args: state - state of the game"
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state))) ;define current player
        hand (:hand player)                                 ;define current hand of player on turn
        dev (:dev-cards player)                             ;define current dev cards of player on turn
        screen-bounds (.getVisualBounds (Screen/getPrimary)) ; maximise window
        width (.getWidth screen-bounds)
        height (.getHeight screen-bounds)
        ore-count (count (filter #(= % "ore") hand))        ;count numbers of ore resource of player on turn
        grain-count (count (filter #(= % "grain") hand))    ;count numbers of grain resource of player on turn
        wood-count (count (filter #(= % "wood") hand))      ;count numbers of wood resource of player on turn
        wool-count (count (filter #(= % "wool") hand))      ;count numbers of wool resource of player on turn
        brick-count (count (filter #(= % "brick") hand))    ;count numbers of brick resource of player on turn
        show-buy-town? (and (>= ore-count 3) (>= grain-count 2)) ;define boolean value, does current player on turn have possibilities to buy town
        show-buy-settlement? (and (>= wool-count 1) (>= grain-count 1) (>= wood-count 1) (>= brick-count 1)) ;define boolean value, does current player on turn have possibilities to buy settlement
        show-buy-dev? (and (>= wool-count 1) (>= grain-count 1) (>= ore-count 1)) ;define boolean value, does current player on turn have possibilities to buy dev card
        show-buy-road? (and (>= brick-count 1) (>= wood-count 1)) ;define boolean value, does current player on turn have possibilities to buy road
        show-dev-cards? (some? dev)                         ;show dev cards only if player have
        card-freq (frequencies hand)                        ;define map of hand where values present number of cards
        dev-cards-clicked? (contains? #{"knight" "road-building"} (:clicked-resource @*state)) ;define bol;ean value does clicked cart dev card
        has-four-of-a-kind? (some (fn [[_ cnt]] (>= cnt 4)) card-freq) ;define does player have 4 same cards in hand
        dice-rolled? (:dice-rolled @*state)                 ;does dice already rolled in turn
        on-dice-7? (:on-dice-7 @*state)                     ;does current player get 7
        shop-view? (:shop-view @*state)                     ;did activate shop
        ]
    {:fx/type :stage
     :width width
     :height height
     :x (.getMinX screen-bounds)
     :y (.getMinY screen-bounds)
     :showing true
     :title   "CATAN"
     :scene   {:fx/type :scene
               :root    {:fx/type  :border-pane
                         :style    "-fx-background-color: #1e90ff;"
                         :top {:fx/type   :v-box
                               :alignment :center
                               :padding   10
                               :children  [(elem/player-turn-info (:name player)) ; provide info, who is on turn
                                           {:fx/type :label
                                            :style "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
                                            :text (:game-massage @*state)}]}

                         :right {:fx/type :scroll-pane      ; make pane of elements which can be scrolled (made that because of number of elements
                                 :fit-to-width true
                                 :padding 10
                                 :content {:fx/type   :v-box
                                           :spacing   10
                                           :alignment :center
                                           :children (when (not on-dice-7?) (remove nil? ;only if dice is not 7,remove all nils values and show following
                                                                                    [(elem/table-info *state) ;table infp
                                                                                     (when (not shop-view?)(when (not dice-rolled?)(elem/dices-button *state))) ; if shop is not activated and dice is not rolled yet, present dices button
                                                                                     (when (not shop-view?)(when dice-rolled? (elem/dice-views *state))); if shop is not activated and dice already rolled, present dices view
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-settlement? (elem/buy-settlement-button)))) ; if shop is not activated and dice already rolled, present buy settlement button if there is possibilities to buy
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-town? (elem/buy-town-button)))) ; if shop is not activated and dice already rolled, present buy town button if there is possibilities to buy
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-road? (elem/buy-road-button)))) ; if shop is not activated and dice already rolled, present buy road button if there is possibilities to buy
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-dev? (elem/buy-dev-card-button)))) ; if shop is not activated and dice already rolled, present buy dev card button if there is possibilities to buy
                                                                                     (when (not shop-view?)(when dice-rolled? (when has-four-of-a-kind? (elem/buy-card-button)))) ; if shop is not activated and dice already rolled, present buy card button if there is possibilities to buy
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-dev-cards? (elem/hand-dev-view (:dev-cards player) *state)))) ; if shop is not activated and dice already rolled, show dev cards if player on turn have them
                                                                                     (when (not shop-view?)(when dice-rolled? (when dev-cards-clicked? (elem/activate-button)))) ;  if shop is not activated and dice already rolled, if player click dev card, open button to activate card
                                                                                     (when (not shop-view?)(when dice-rolled? (elem/end-turn-btn))) ;if shop is not activated and dice already rolled, give possibilities to player press end turn button
                                                                                     ])) ;all of this made to player have clear view what can do, what not. So on this way if player no have resource to buy e.g. settlement, app will not give that possibilities to him
                                           }}
                         :bottom (elem/hand-view (:hand player) *state) ;on bottom always present which resource are available for player on turn
                         :center
                         {:fx/type :anchor-pane             ; made to centralize elements, distance for left,right,top and bottom side
                          :children
                          [{:fx/type :stack-pane
                            :anchor-pane/left 0             ; distance from left
                            :anchor-pane/right 0            ; distance from right
                            :anchor-pane/top 0              ; distance from top
                            :anchor-pane/bottom 0           ; distance from bottom
                            :children
                            (if shop-view?                  ; if is shop-view activated (if pressed buy-cards), this is made if player go to shop, that cannot see board because is not relevant and will be cleared to player what to do
                              [(cond
                                 (:card-shop-buy @*state)  {:fx/type :v-box ; press buying service
                                                            :spacing 8
                                                            :alignment :center
                                                            :children [(elem/shop-buy-card *state) ; function which will send inf oto state that want to buy card
                                                                       (elem/buy-this-card-btn) ;button to tell state that clicked card are for buying
                                                                       (elem/exit-shop-button )]} ;button for possibilities to exit from shop
                                 (:card-shop-sell @*state) {:fx/type :v-box ;press selling service
                                                            :spacing 8
                                                            :alignment :center
                                                            :children [(elem/shop-sell-card *state) ;function which will send inf oto state that want to sell card
                                                                       (elem/sell-this-card-btn) ;button to tell state that clicked card are for selling
                                                                       (elem/exit-shop-button)]} ;button for possibilities to exit from shop
                                 :else nil)]
                              [(elem/image-group state)     ; this will be presented even if 7 are on dice
                               (elem/roads-view *state)     ; this will be presented even if 7 are on dice
                               (elem/spots-view *state)])}]}; this will be presented even if 7 are on dice

                         }}}))


(defn end-game-view
  "End game view, present winner. Will be activated if somebody reach 10 victory points"
  [state]
  (let [screen-bounds (.getVisualBounds (Screen/getPrimary))
        width (.getWidth screen-bounds)
        height (.getHeight screen-bounds)
        winner (:winner @*state)]
    {:fx/type :stage
     :width width
     :height height
     :x (.getMinX screen-bounds)
     :y (.getMinY screen-bounds)
     :showing true
     :title "CATAN - Game Over"
     :scene {:fx/type :scene
             :root {:fx/type :border-pane
                    :style "-fx-background-color: #1e90ff;"
                    :top {:fx/type :v-box
                          :alignment :center
                          :padding 10
                          :children [{:fx/type :label
                                      :text (str "PLAYER " winner " WINS!") ;show label with name of player who won
                                      :style "-fx-font-size: 72pt; -fx-font-weight: bold; -fx-text-fill: white;"
                                      :alignment :center}]}
                    :center {:fx/type :stack-pane
                             :children [{:fx/type :label
                                         :text "Thank you for playing CATAN" ;show end message
                                         :style "-fx-font-size: 24pt; -fx-text-fill: white;"
                                         :alignment :center}]}
                    }}}))




(defn event-handler
"
   This function handles UI events triggered by the user, such as button clicks,  selections, or keyboard input. It receives an event object and the current
   application state (usually an atom), and performs the necessary state updates  or side effects (e.g., showing a dialog, navigating screens, etc.).
   Function will get event depends on action on gui, all possible events could be passed through visualization element.

  Args - event -The event object containing information about the user action.
  "
  [event]

  (case (:event/type event)
    :activate-dev-card (let [player-idx (dec (:player-turn @*state))
                             resource (:clicked-resource @*state)]
                         (swap! *state update-in [:players player-idx :dev-cards]
                                (fn [cards]
                                  (let [[before [match & after]] (split-with #(not= % resource) cards)]
                                    (vec (concat before after)))))
                          (do (cond
                                (= (:clicked-resource @*state) "knight")
                                (do
                                  (swap! *state assoc :game-massage "SELECT THE AREA YOU WANT TO RESTRICT")
                                  (swap! *state assoc :move-thief true)
                                  (swap! *state update-in [:players player-idx :army-size]
                                         (fnil inc 0))
                                  (swap! *state assoc :on-dice-7 true)
                                  (services/player-with-largest-army *state)
                                  (services/update-players-vp *state)
                                  (services/update-winner *state end-game-view))

                                (= (:clicked-resource @*state) "road-building")
                                (do
                                  (swap! *state assoc :road-build true)
                                  (swap! *state assoc :game-massage "YOU ACTIVATED CARD FOR BUILDING 2 ROAD, PLEASE SELECT 2")
                                  (swap! *state assoc :build-roads-activate true)
                                  )


                                )
                              (swap! *state assoc :clicked-resource nil))

                          )

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
                        (services/update-all-hands-second-half *state)

                        (swap! *state assoc :on-dice-7 true)
                        )

                        (doseq [coord (:spots @*state)]
                        (services/filing-hand-with-resource (:spot-coordinates (:on-mouse-clicked coord))
                                                   dice-sum *state))
                        ))

                  (swap! *state assoc :dice-rolled true))
    :spots-click
    (let [coords (:spot-coordinates event)
          booked-spots (:booked-spots @*state)
          all-spots (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732)))
          near-spots (filter #(= 1.0 (utils/distance-1-2 % coords)) all-spots)
          phase (:phase @*state)
          areas (:areas @*state)]
      (if (and (not (:town-build @*state))
               (some #(= % coords) booked-spots))
        (swap! *state assoc :game-massage "THIS SPOTS ARE ALREADY BOOKED, TOO CLOSE TO OTHER SETTLEMENT, OR NOT CONNECTED")

        (do
          (cond
            (= phase "Initial")
            (do
              (when (:settlement-build @*state)
                (services/build-settlement coords *state))
              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots))))
              (swap! *state assoc :settlement-build false)
              (swap! *state assoc :road-build true)
              (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL ROAD CONNECTED WITH YOUR SETTLEMENT")
              (services/update-players-vp *state)
              )

            (= phase "Second-Initial")
            (do
              (when (:settlement-build @*state)
                (services/build-settlement coords *state))
              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots))))
              (swap! *state assoc :settlement-build false)
              (swap! *state assoc :road-build true)
              (swap! *state assoc :initial-info "SECOND INITIAL PHASE, SELECT ROAD CONNECTED TO YOUR SETTLEMENT")
              (services/update-players-vp *state)
              (let [matched-areas (filter (fn [area] (some #{coords} (:spots area))) areas)
                    all-resources (remove #(= % "dust") (map :resource matched-areas))
                    player-turn (:player-turn @*state)
                    player-idx (dec player-turn)]
                (swap! *state assoc-in [:players player-idx :hand] all-resources))

              )

            :else
            (do
              (cond
                (:settlement-build @*state)
                (services/build-settlement coords *state)

                (:town-build @*state)
                (services/build-town coords *state)

                :else
                (println "Nothing active to build"))

              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots))))
              (swap! *state assoc :settlement-build false)
              (swap! *state assoc :town-build false)
              (services/update-players-vp *state)
              (services/update-winner *state end-game-view)
              ))))
      )
    :roads-click
    (let [road-coords (:road-coordinates event)
          booked-roads (:booked-roads @*state)
          phase       (:phase @*state)
          road-active (:road-build @*state)
          player-turn (:player-turn @*state)
          player-count (count (:players @*state))
          build-roads-card-activate? (:build-roads-activate @*state)
          booked-road? (some #(= % road-coords) booked-roads)]
      (if road-active
        (if booked-road?
          (swap! *state assoc :game-massage "THIS ROAD IS ALREADY BUILT, CHOOSE ANOTHER")
          (do
            (if build-roads-card-activate?
              (do (services/build-road road-coords *state)

                  (swap! *state assoc :build-roads-activate false)
                  (swap! *state update :booked-roads conj road-coords)
                  (services/update-current-player-road-length! *state)
                  (services/player-with-longest-route *state)
                  (services/update-players-vp *state)
                  (services/update-winner *state end-game-view))
              (do (services/build-road road-coords *state)
                  (swap! *state update :booked-roads conj road-coords)
                  (swap! *state assoc :road-build false)
                  (services/update-current-player-road-length! *state)
                  (services/player-with-longest-route *state)
                  (services/update-players-vp *state)
                  (services/update-winner *state end-game-view)))

            (when (= phase "Initial")
              (if (= player-turn player-count)
                (do
                  (swap! *state assoc :phase "Second-Initial")
                  (swap! *state assoc :settlement-build true)
                  (swap! *state assoc :initial-info "SECOND INITIAL PHASE - PLACE YOUR SECOND SETTLEMENT")
                  (services/update-current-player-road-length! *state)
                  (services/player-with-longest-route *state)
                  (services/update-players-vp *state)
                  (services/update-winner *state end-game-view))
                (do
                  (swap! *state assoc :settlement-build true)
                  (swap! *state update :player-turn #(services/player-turn-inc % player-count))
                  (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS")
                  (services/update-current-player-road-length! *state)
                  (services/player-with-longest-route *state)
                  (services/update-players-vp *state)
                  (services/update-winner *state end-game-view))))

            (when (= phase "Second-Initial")
              (swap! *state assoc :settlement-build true)
              (when (= player-turn 1)
                (swap! *state assoc :phase "Game")
                (swap! *state assoc :initial-info "GAME")
                (swap! *state assoc :settlement-build false)
                (swap! *state assoc :fx/type game-view)
                (services/update-current-player-road-length! *state)
                (services/player-with-longest-route *state)
                (services/update-players-vp *state)
                (services/update-winner *state end-game-view))
              (when (not= player-turn 1)
                (swap! *state update :player-turn #(services/player-turn-dec %))
                (swap! *state assoc :initial-info "SECOND INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS")
                (services/update-current-player-road-length! *state)
                (services/player-with-longest-route *state)
                (services/update-players-vp *state)
                (services/update-winner *state end-game-view))))))
      (println "No active to build road, press buy road button")
      (services/update-current-player-road-length! *state)
      (services/player-with-longest-route *state)
      (services/update-players-vp *state)
      (services/update-winner *state end-game-view))

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
        (swap! *state assoc :on-dice-7 false)
        )
      (swap! *state assoc :game-massage "YOU CAN'T MOVE THIEF IF YOU DIDN'T GET 7 ON DICE"))


    :set-input-name (swap! *state assoc :input-name (:fx/event event))
    :set-input-color (swap! *state assoc :input-color (:fx/event event))
    :end-turn (do
                (swap! *state (fn [s](assoc s :player-turn (services/player-turn-inc (:player-turn s) (count (:players s))))))
                (swap! *state assoc :clicked-resource nil)
                (swap! *state assoc :dice-rolled false))
    :buy-dev-card-btn (do
                        (services/take-development-card *state)
                        (services/update-players-vp *state))
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
    :buy-card-btn (do
                    (swap! *state assoc :shop-view true)
                    (swap! *state assoc :card-shop-buy true)
                    (swap! *state assoc :clicked-resource nil))
    :exit-shop ((swap! *state assoc :shop-view false)
                (swap! *state assoc :card-shop-buy false)
                (swap! *state assoc :card-shop-sell false))
    :card-click (swap! *state assoc :clicked-resource (:resource event))
    :buy-this-card (let [take-resource-card (:take-resource-card? @*state)]
                     (if take-resource-card
                       (do
                         (swap! *state assoc :card-shop-buy false)
                         (swap! *state assoc :clicked-resource nil))
                          (let [buy-card-type (:buy-resource @*state)
                                player-idx (dec (:player-turn @*state))
                                hand-path [:players player-idx :hand]
                                current-hand (get-in @*state hand-path)
                                updated-hand (remove nil? (conj current-hand buy-card-type))]
                            (swap! *state assoc-in hand-path updated-hand)
                            )
                         )
                       (do
                         (swap! *state assoc :card-shop-buy false)
                         (swap! *state assoc :card-shop-sell true)
                         (swap! *state assoc :buy-resource (:clicked-resource @*state))
                         (swap! *state assoc :clicked-resource nil))
                       )
    :sell-this-card (do
                      (swap! *state assoc :shop-view false)
                      (swap! *state assoc :card-shop-sell false)
                      (swap! *state assoc :sell-resource (:clicked-resource @*state))
                      (let [player-idx (dec (:player-turn @*state))
                            hand-path [:players player-idx :hand]
                            current-hand (get-in @*state hand-path)
                            sell-resource (:sell-resource @*state)
                            buy-card-type (:buy-resource @*state)
                            sell-count (count (filter #(= % sell-resource) current-hand))]

                        (if (< sell-count 4)
                          (swap! *state assoc :game-massage "NO CARD BOUGHT BECAUSE YOU DONT HAVE ENOUGH RESOURCE TO BUY")

                          (let [new-hand (let [[b1 a1] (split-with #(not= % sell-resource) current-hand)
                                               hand1 (concat b1 (if (empty? a1) [] (rest a1)))

                                               [b2 a2] (split-with #(not= % sell-resource) hand1)
                                               hand2 (concat b2 (if (empty? a2) [] (rest a2)))

                                               [b3 a3] (split-with #(not= % sell-resource) hand2)
                                               hand3 (concat b3 (if (empty? a3) [] (rest a3)))

                                               [b4 a4] (split-with #(not= % sell-resource) hand3)
                                               hand4 (concat b4 (if (empty? a4) [] (rest a4)))]
                                           hand4)
                                updated-hand (remove nil? (conj new-hand buy-card-type))]
                            (swap! *state assoc-in hand-path updated-hand)
                            (swap! *state dissoc :game-message))))

                      (swap! *state assoc :clicked-resource nil))
    )
  )



(defn start-game []
  "
  Initializes and starts the game UI.
  This function performs two main tasks:
  1. Creates a new JavaFX renderer using `cljfx`, with the provided event handler.
  2. Mounts the renderer to the application state atom (`*state`), and sets
  the root UI component to `start-game-view`
  The renderer is responsible for keeping the UI in sync with the state,
  while the event handler processes user interactions.
  [4]
  Args : no args
  "
  (let [renderer (fx/create-renderer                        ;create cljfx renderer
                   :opts {:fx.opt/map-event-handler event-handler})] ; pass event handler
    (fx/mount-renderer *state renderer)                     ;conect render with state
    (swap! *state assoc :fx/type start-game-view)))         ;start view