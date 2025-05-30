(ns catan-predictor.visualization
  (:require [cljfx.api :as fx]
            [catan-predictor.utils :as utils]
            [catan-predictor.visualization-elements :as elem]
            [catan-predictor.visualization-services :as services]
            )
  (:import  [javafx.stage Screen]
            [javafx.scene.paint Color]
            [javafx.scene.layout Background ]
            [javafx.scene.paint Color]
            [javafx.scene.paint ImagePattern]))

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
 :on-close-request (fn [_] (System/exit 0))                 ; if exit from window exit from lein
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
     :on-close-request (fn [_] (System/exit 0))             ; if exit from window exit from lein
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
     :on-close-request (fn [_] (System/exit 0))             ; if exit from window exit from lein
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
     :on-close-request (fn [_] (System/exit 0))             ; if exit from window exit from lein
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

  (case (:event/type event)                                 ;event type takes event
    :activate-dev-card (let [player-idx (dec (:player-turn @*state)) ;in case that is activate-dev-card; player idx is index of player on turn
                             resource (:clicked-resource @*state)] ;resources is clicked-resource of current state, that is activating on event :card-click, in this case will be dev card
                         (swap! *state update-in [:players player-idx :dev-cards] ;update dev card of player
                                (fn [cards]
                                  (let [[before [match & after]] (split-with #(not= % resource) cards)] ; take collection before first clicked card in dev card of player and after (not involving that card)
                                    (vec (concat before after))))) ;merge two collection above-mentioned, excluding clicked card
                          (do (cond
                                (= (:clicked-resource @*state) "knight") ;if clicked card is knight
                                (do                         ;do
                                  (swap! *state assoc :game-massage "SELECT THE AREA YOU WANT TO RESTRICT") ;write message on screen
                                  (swap! *state assoc :move-thief true) ;change :move-thief option for true ( that is important that player could restrict area
                                  (swap! *state update-in [:players player-idx :army-size]
                                         (fnil inc 0))      ;increase army-size by 1, even if is :army-size nil (rules of game)
                                  (swap! *state assoc :on-dice-7 true) ;change option :on-dice-7 to true
                                  (services/player-with-largest-army *state) ;update player who have bigest army
                                  (services/update-players-vp *state) ;update victory points
                                  (services/update-winner *state end-game-view)) ;update winner, if is there
                                ;All of the above states can be changed by drawing a Knight card. Army size rise, vp could increase if some player have the biggest army
                                (= (:clicked-resource @*state) "road-building") ;if activated card is road building
                                (do                         ;do
                                  (swap! *state assoc :road-build true) ;make road build to true, to player can build roads
                                  (swap! *state assoc :game-massage "YOU ACTIVATED CARD FOR BUILDING 2 ROAD, PLEASE SELECT 2") ;write game message on screen
                                  (swap! *state assoc :build-roads-activate true) ;give info to state that road-building activate card is activated
                                  ; this is important when we activate that , state will know that player have opportunity to build 2 roads, will be use in event/type :road click
                                  )


                                )
                              (swap! *state assoc :clicked-resource nil)) ;if not of them are activated , return nil to :clicked resource, nothing will happen
                              ; victory point cannot be activated ( rules of game, not have to activate to get 1 vp

                          )

    :add (do                                                ;add, used for button add-player on home page
           (swap! *state                                    ;change state
                  (fn [s]
                    (-> s                                   ;macro (-> x ), same as (g (f x)) <=> (->x f g), first will be updated :players and with that result will be deleted input-name and input-color
                        (update :players conj {:name (:input-name s) ;input name will be :input name of state, (get in name-input); conj add that in :players map
                                               :color (:input-color s)}) ;color of player will be :input-color ( get in color-dropbox)
                        (assoc :input-name "" :input-color nil))))
           ;change input name to empty string and input color to nil, if we added player, color in dropdown and name will be empty to user know that player has added
           )

    :remove                                                 ;remove player from state :players
    (let [idx (:index event)]                               ;idx is :index which will be removed from ewent
      (when (some? idx)                                     ;if idx not nil
        (swap! *state
               (fn [s]
                 (-> s
                     (update :players                       ;update :players
                             (fn [players]
                               ;make new vector list of players without of one on position idx
                               (vec (concat (take idx players) ;take idx of player who will be deleted, and take all elements till idx
                                            (drop (inc idx) players))))) ;take all after idx
                     (update :players-count dec))))))       ; player-count decrease by 1, to state have input on beginning of game
    ; this function will be called in remove-button and will delete player from game
    :initial-phase-game (do
                          ; event which change view to initial state of game, will be called on button start-game
                          (swap! *state assoc :fx/type initial-phase-game) ;change state to initial-phase game view
                          (swap! *state assoc :settlement-build true)) ;change settlement-build to true to player on move can build initial settlement (rules of game)
    :dice-view  (do
                  ;event which provide view of random dice get on rolling, will be show 2 dices with random numbers 1-6, will be called in dices-button
                  (swap! *state assoc :dice-1 (utils/random-dice-number) :dice-2 (utils/random-dice-number)) ;change state of game and provide info of number on dice 1 and 2 , random get
                  (let [dice-sum (+ (:dice-1 @*state) (:dice-2 @*state))] ;sum of 2 random number rolled on dice
                    (if (= dice-sum 7)                      ; if sum is 7, special event ( rules of game)
                      (do                                   ;do
                        (swap! *state assoc :game-massage "SELECT THE AREA YOU WANT TO RESTRICT") ;write message on screen
                        (swap! *state assoc :move-thief true) ;change state of move-thief  to true, to player can move thief figure and restrict some area
                        (services/update-all-hands-second-half *state) ;if in hand some player have 7, will bi splited on half
                        (swap! *state assoc :on-dice-7 true);change :on-dice-7 to true, to state know that 7 rolled
                        )
                      ;if not 7, this case is more offten
                        (doseq [coord (:spots @*state)]     ;pass through :spots of state
                        (services/filing-hand-with-resource (:spot-coordinates (:on-mouse-clicked coord)) ;filling hand of all players with resources got on dice, if :spots in border with area with number obtained on dice
                                                   dice-sum *state))
                        ))
                  (swap! *state assoc :dice-rolled true))   ;state get info that dice already rolled, and will in blocked again rolling of player, will be disabled dice-button
    :spots-click                                            ;event if click on spot, call in element create-spot-view
    (let [coords (:spot-coordinates event)
          booked-spots (:booked-spots @*state)              ;booked spots from state
          all-spots  (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))) ;all spots
          near-spots (filter #(= 1.000 (utils/distance-1-2 % coords)) all-spots) ;spots which on distance 1.000
          phase (:phase @*state)                            ;phase of game
          areas (:areas @*state)]                           ;all areas
      (if (and (not (:town-build @*state))                  ;if town build not activated, important cuz we want in this case build settlement
               (some #(= % coords) booked-spots))           ;and if settlement already in blocked spots, (distance 1 of some building and already built  something on that spot)
        (swap! *state assoc :game-massage "THIS SPOTS ARE ALREADY BOOKED, TOO CLOSE TO OTHER SETTLEMENT, OR NOT CONNECTED") ;nothing could be build there, write message only
        ;otherwise do
        (do
          (cond
            (= phase "Initial")                             ;if initial phase of game
            (do                                             ;do
              (when (:settlement-build @*state)             ;build-settlement function is true (given in initial *state)
                (services/build-settlement coords *state))  ;build settlement on spot
              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots)))) ;book that spot
              (swap! *state assoc :settlement-build false)  ;change opportunity to build to false
              (swap! *state assoc :road-build true)         ;and road-build to true (rules of game, in initial phase player first chose settlement and road for building initial infrastructure)
              (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL ROAD CONNECTED WITH YOUR SETTLEMENT") ;give info to player to build road
              (services/update-players-vp *state)           ;update victory point to all players, because when build settlement player get 1 victory point
              )

            (= phase "Second-Initial")
            ;if is second initial phase, (rules of game: after first round ( when all players plays initial) state of game will be changed to second-initial phase
            ; in that phase  players have opportunity to build settlement and road nears to that settlement again and get resource which is areas connected with that spot rich
            ; players play in the opposite direction to the initial phase
            (do
              (when (:settlement-build @*state)             ;if settlement-build is true
                (services/build-settlement coords *state))  ;build settlement
              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots)))) ;book spot
              (swap! *state assoc :settlement-build false)  ;change settlement-build function to false
              (swap! *state assoc :road-build true)         ;and give opportunity to build road
              (swap! *state assoc :initial-info "SECOND INITIAL PHASE, SELECT ROAD CONNECTED TO YOUR SETTLEMENT") ; inform player
              (services/update-players-vp *state)           ;update victory points
              (let [matched-areas (filter (fn [area] (some #{coords} (:spots area))) areas) ; this function will give initial resource to player, find all areas which have clicked coords in their :spots
                    all-resources (remove #(= % "dust") (map :resource matched-areas)) ;all resources are all resources of those areas, instead of dust, because by rules of game dust not provide resources
                    player-turn (:player-turn @*state)      ;player on turn
                    player-idx (dec player-turn)]           ;index o player
                (swap! *state assoc-in [:players player-idx :hand] all-resources)) ; give to player who is on turn in hand all resources of areas which is connected with spots where built settlement in second-initial phase

              )

            :else                                           ;else, (only game phase)
            (do
              (cond
                (:settlement-build @*state)                 ; if settlement-build is true (if player have opportunity tu build)
                (services/build-settlement coords *state)   ; build settlement

                (:town-build @*state)                       ;if you have oportunity to build town, on click build town (settlement-build and town-build feature cannot be true in same time)
                (services/build-town coords *state)         ;build town

                :else
                (swap! *state assoc :game-massage "NOTHING ACTIVATE TO BUILD")) ; inform player if no opportunity to build

              (swap! *state update :booked-spots #(vec (set (concat % [coords] near-spots)))) ;also book spots if something has built in game phase
              (swap! *state assoc :settlement-build false)  ;if build something no give more opportunity to player to build, state have to wait other event to activate that feature
              (swap! *state assoc :town-build false)        ;same for towns
              (services/update-players-vp *state)           ;update vp
              (services/update-winner *state end-game-view)
              ;update state if some player already have 10 or more, this not need to bi done in initial and second-initial phase,
              ; because is not possible to someone win in that phase, max vp in that phase is 2, but for win 10 are needed
              ))))
      )
    :roads-click                                            ;function for building roads (roads-click could call this)
    (let [road-coords (:road-coordinates event)             ;clicked road coords
          booked-roads (:booked-roads @*state)              ;booked roads
          phase       (:phase @*state)                      ;phase of game
          road-active (:road-build @*state)                 ;is there road building opportunity
          player-turn (:player-turn @*state)                ;player on turn
          player-count (count (:players @*state))           ;count of players
          build-roads-card-activate? (:build-roads-activate @*state) ;does roads-card-activate, if dev card activated, players get opportunity to build 2 roads by rules of game
          booked-road? (some #(= % road-coords) booked-roads)] ;does click road already in booked roads
      (if road-active                                       ;if is opportunity to build
        (if booked-road?                                    ;and if road booked
          (swap! *state assoc :game-massage "THIS ROAD IS ALREADY BUILT, CHOOSE ANOTHER") ;inform player that cannot build
          (do                                               ;otherwise
            (if build-roads-card-activate?                  ;if road card activated
              (do (services/build-road road-coords *state)  ;build road
                  (swap! *state assoc :build-roads-activate false) ;inform state that card is activated and used, now state get info that cannot build 2 again
                  (swap! *state update :booked-roads conj road-coords) ;put roads coords to booked roads
                  (services/update-current-player-road-length! *state) ;update length of player
                  (services/player-with-longest-route *state) ;update players with the longest rout
                  (services/update-players-vp *state)       ;update vp, because someone could get 2 points on longest route
                  (services/update-winner *state end-game-view)) ;inform state if some player win
              (do (services/build-road road-coords *state)  ; do that 2 times ( because give opportunity to build 2 roads)
                  (swap! *state update :booked-roads conj road-coords)
                  (swap! *state assoc :road-build false)    ;no more opportunity to build after second build road
                  (services/update-current-player-road-length! *state)
                  (services/player-with-longest-route *state)
                  (services/update-players-vp *state)
                  (services/update-winner *state end-game-view)))

            (when (= phase "Initial")                       ;if is initial phase
              (if (= player-turn player-count)              ;when last player build settlement adn road in initial phase , after that initial phase is over
                (do
                  (swap! *state assoc :phase "Second-Initial") ;because of that change phase to second phase
                  (swap! *state assoc :settlement-build true) ;build settlement feature change to true, to players can build settlement in second-initial phase
                  (swap! *state assoc :initial-info "SECOND INITIAL PHASE - PLACE YOUR SECOND SETTLEMENT") ; inform players
                  (services/update-current-player-road-length! *state)) ; update player length route
                (do                                         ;if not, if is still initial phase
                  (swap! *state assoc :settlement-build true) ;change settlement build to true, to next player can build settlement
                  (swap! *state update :player-turn #(services/player-turn-inc % player-count)) ;increase player turn, next player is playing
                  (swap! *state assoc :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS") ;inform player
                  (services/update-current-player-road-length! *state))))

            (when (= phase "Second-Initial")                ;if phase is second-initial
              (swap! *state assoc :settlement-build true)   ;change settlement build to true to players can build settlement
              (when (= player-turn 1)
                ;if player turn is 1 go to game phase (because in second initial phase players play in opposite direction, e.g. if we have
                ; 4 players in initial phase will play in this direction |start-game| player 1 -> player 2 -> player 3 -> player 4 |second-initial-phase| player 4 -> player 3 -> player 2 -> player 1 |game-phase|
                (swap! *state assoc :phase "Game")          ;change phase to game
                (swap! *state assoc :initial-info "GAME")   ;inform players
                (swap! *state assoc :settlement-build false);players cannot build settlement in game phase without activate that on button
                (swap! *state assoc :fx/type game-view)     ;change view
                (services/update-current-player-road-length! *state); update player length route
                )
              (when (not= player-turn 1)                    ; if not first player on turn
                (swap! *state update :player-turn #(services/player-turn-dec %)) ;decrease player turn (4 -> 3 -> 2 -> 1)
                (swap! *state assoc :initial-info "SECOND INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS") ; inform player
                (services/update-current-player-road-length! *state) ;update player with road length
                ))))))

    :circle-click                                           ;if click on circle (call in circle)
    (if (:move-thief @*state)                               ;if move-thief activated
      (let [coords (:center-coordinates event)              ;coords
            areas (:areas @*state)                          ;areas of state
            restricted-area (:restricted-area @*state)      ;if some area is restricted, this is important to save info which area is restricted, to know to return restricted area same number after moving thief rom that location
            restricted-number (:restricted-number @*state)  ;if some number is restricted
            clicked-area (some #(when (= (:center %) coords) %) areas) ;area where is number which is clicked
            clicked-number (:number clicked-area)           ;number of clicked area (2-12)
            areas-restored (if (and restricted-area (some? restricted-number)) ;if restricted area and restricted number (if is not nil)
                             (mapv (fn [area]
                                     (if (= (:center area) restricted-area) ;if center of area is same as restricted area
                                       (assoc area :number restricted-number) ;change number of area to restricted-area
                                       area))
                                   areas)
                             areas)
            areas-updated (mapv (fn [area]
                                  (if (= (:center area) coords) ;if center of areas coords
                                    (assoc area :number nil);change number to nil, on that way number will be nil, and thief will be moved, and because that is not possible too get nil on dices, you cannot get this resource
                                    area))
                                areas-restored)]
        (swap! *state assoc :areas areas-updated)           ;update areas
        (swap! *state assoc :restricted-area coords)        ;make restricted area with clicked coords
        (swap! *state assoc :restricted-number clicked-number) ;restricted number is clicked number
        (swap! *state assoc :move-thief false)              ;after moving thief you are losing opportunity to move again (only one time could be moved)
        (swap! *state assoc :on-dice-7 false)               ;change on dice 7 to false
        )
      (swap! *state assoc :game-massage "YOU CAN'T MOVE THIEF IF YOU DIDN'T GET 7 ON DICE")) ;inform players


    :set-input-name (swap! *state assoc :input-name (:fx/event event)) ; set input name  (call in input-name), event is typing inside input-name, input name will be changed on typing something in input box
    :set-input-color (swap! *state assoc :input-color (:fx/event event)) ;call in color-dropbox, get :input-color depends on your chose, this is important to know what to assign when add player
    :end-turn (do                                           ;end turn call in end-turn-button
                (swap! *state (fn [s](assoc s :player-turn (services/player-turn-inc (:player-turn s) (count (:players s)))))) ;change player turn to next player
                (swap! *state assoc :clicked-resource nil)  ;delete clicked resource, because second player not need inf of previous clicked resource
                (swap! *state assoc :dice-rolled false))    ;give info to state that dice could be rolled again, because previous player end turn, and current player now rolling
    :buy-dev-card-btn (do                                   ;call in buy-dev-card-btn,
                        (services/take-development-card *state) ;take development card to hand
                        (services/update-players-vp *state)); update vp because player can get victory-point card
    :buy-settlement-btn (do                                 ;call in buy-settlement-btn
                          (swap! *state assoc :settlement-build true) ;give opportunity to player to build
                          (swap! *state assoc :town-build false) ;don't give opportunity to buy town
                          (swap! *state assoc :road-build false) ;don't give opportunity to buy road
                          (swap! *state assoc :game-message "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD SETTLEMENT")) ;inform player
    :buy-town-btn (do                                       ;call in buy-town-btn
                    (swap! *state assoc :town-build true)   ;give opportunity to player to build
                    (swap! *state assoc :settlement-build false) ;don't give opportunity to buy settlement
                    (swap! *state assoc :road-build false)  ;don't give opportunity to buy road
                    (swap! *state assoc :game-message "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD TOWN")) ;inform player
    :buy-road-btn (do                                       ;call in buy-road-btn
                    (swap! *state assoc :road-build true)   ;give opportunity to player to build
                    (swap! *state assoc :town-build false)  ;don't give opportunity to buy town
                    (swap! *state assoc :settlement-build false) ;don't give opportunity to buy settlement
                    (swap! *state assoc :game-message "PLEASE SELECT SPOT WHERE YOU WANT TO BUILD ROAD")) ;inform player
    :buy-card-btn (do                                       ;call in buy-card-btn
                    (swap! *state assoc :shop-view true)    ;change view to shop view
                    (swap! *state assoc :card-shop-buy true);give opportunity to player to buy card
                    (swap! *state assoc :clicked-resource nil) ;if previous clicked resource, delete it and give opportunity to player to chose what want to buy
                    (swap! *state assoc :sell-resource nil) ;remove info of sell resource
                    (swap! *state assoc :buy-resource nil) ;remove info of buy resource
                    )
    :exit-shop (do                                          ;call on exit-shop-btn
                (swap! *state assoc :shop-view false)       ;disable shop-view
                (swap! *state assoc :card-shop-buy false)   ;disable opportunity to chose card to buy
                (swap! *state assoc :card-shop-sell false)) ;activate opportunity to select card which want to sell
    :card-click (swap! *state assoc :clicked-resource (:resource event)) ;activate when you press card, state get inform type of clicked card, all clicked resource will increase size on view
    :buy-this-card (                                        ;call on buy-this-card-btn
                     let [take-resource-card (:take-resource-card? @*state)] ;if is opportunity to take resource card
                     (if take-resource-card
                       (do
                         (swap! *state assoc :card-shop-buy false) ;change shop for buying to false, and open sell shop buy
                         (swap! *state assoc :clicked-resource nil)) ;after tell to state what card want to buy
                          (let [buy-card-type (:buy-resource @*state) ;resource which want to buy
                                player-idx (dec (:player-turn @*state)) ;player idx
                                hand-path [:players player-idx :hand] ;path in state
                                current-hand (get-in @*state hand-path) ;hand of current player
                                updated-hand (remove nil? (conj current-hand buy-card-type))] ; remove all nil values add selected cart  to curren player hand
                            (swap! *state assoc-in hand-path updated-hand) ;update hand of player
                            )
                         )
                       (do
                         (swap! *state assoc :card-shop-buy false) ;change card shop buy to false
                         (swap! *state assoc :card-shop-sell true) ;open shop with opportunity to select card for selling
                         (swap! *state assoc :buy-resource (:clicked-resource @*state)) ;give info to state that buy resource is clicked resource
                         (swap! *state assoc :clicked-resource nil);remove clicked card info
                         (swap! *state assoc :sell-resource nil)) ;remove sell-resource info
                       )
    :sell-this-card (do                                     ;activate on button sell-this-card-btn
                      (swap! *state assoc :shop-view false) ;exit from shop
                      (swap! *state assoc :card-shop-sell false) ;inform that player cannot sell again after selling
                      (swap! *state assoc :sell-resource (:clicked-resource @*state)) ;clicked resource will be sell resource
                      (let [player-idx (dec (:player-turn @*state)) ;index ofo current player
                            hand-path [:players player-idx :hand] ;path to hand in state
                            current-hand (get-in @*state hand-path) ;hand
                            sell-resource (:sell-resource @*state) ;sell resource
                            buy-card-type (:buy-resource @*state) ;buy resource
                            sell-count (count (filter #(= % sell-resource) current-hand))] ; count resource for selling

                        (if (< sell-count 4)                ;if is less than 4 same resource
                          (swap! *state assoc :game-massage "NO CARD BOUGHT BECAUSE YOU DONT HAVE ENOUGH RESOURCE TO BUY") ;inform player
                          ;if not
                          (let [new-hand (let [[b1 a1] (split-with #(not= % sell-resource) current-hand) ; keep every resource instead of first resource same as sell resource
                                               hand1 (concat b1 (if (empty? a1) [] (rest a1)))

                                               [b2 a2] (split-with #(not= % sell-resource) hand1) ;second time
                                               hand2 (concat b2 (if (empty? a2) [] (rest a2)))

                                               [b3 a3] (split-with #(not= % sell-resource) hand2) ;third time
                                               hand3 (concat b3 (if (empty? a3) [] (rest a3)))

                                               [b4 a4] (split-with #(not= % sell-resource) hand3) ;forth time for ( 4 cards for 1)
                                               hand4 (concat b4 (if (empty? a4) [] (rest a4)))]
                                           hand4)
                                updated-hand (remove nil? (conj new-hand buy-card-type))]
                            (swap! *state assoc-in hand-path updated-hand) ;update hand of current player
                            (swap! *state dissoc :game-message)))) ;delete message

                      (swap! *state assoc :clicked-resource nil) ;remove info of clicked resource
                      (swap! *state assoc :sell-resource nil) ;remove info of sell resource
                      (swap! *state assoc :buy-resource nil)) ;remove info of buy resource
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