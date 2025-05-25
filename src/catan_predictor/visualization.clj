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


(defonce *state
         (atom {:dice-1      2
                       :dice-2      2
                       :player-turn 1
                       :town-build false
                       :settlement-build false
                       :road-build false
                       :restricted-area nil
                       :restricted-number nil
                       :clicked-resource nil
                       :buy-resource nil
                       :on-dice-7 false
                       :sell-resource nil
                       :move-thief false
                       :card-shop-buy false
                       :dice-activate true
                       :dice-rolled false
                       :card-card-sell false
                       :take-resource-card? false
                       :winner-longest-route nil
                       :shop-view false
                       :winner-army-size nil
                       :activated-road-building false
                       :winner nil
                       :game-massage "WELCOME"
                       :phase "Initial"
                       :initial-info "INITIAL PHASE OF GAME, PLEASE SELECT YOUR INITIAL SETTLEMENTS"
                       :players []
                       :players-count 0
                       :spots (map #(elem/create-spot-view (first %) (second %)) (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))))
                       :booked-spots []
                       :booked-roads []
                       :roads (map #(elem/create-line-view (first (first %))
                                                      (second (first %))
                                                      (first (second %))
                                                      (second (second %))) (services/roads (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732)))))
                       :areas (vec (services/create-areas-from-centers (services/make-spots-from-centers (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))) (services/make-centers (services/make-ring-area-centers 0.0 0.0 1.732))
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
                                          ]
                       }))

(defn start-game-view [state]
  (let [screen-bounds (.getVisualBounds (Screen/getPrimary))
  width (.getWidth screen-bounds)
  height (.getHeight screen-bounds)]
{:fx/type :stage
 :width width
 :height height
 :x (.getMinX screen-bounds)
 :y (.getMinY screen-bounds)
   :showing true
   :title   "CATAN"
   :scene   {:fx/type :scene
             :root    {:fx/type    :v-box
                       :alignment :top-center
                       :spacing   20
                       :padding   20
                       :background (elem/background-image)

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
                                   :children [(elem/player-list *state)]}

                                  {:fx/type :h-box
                                   :spacing 20
                                   :alignment :center
                                   :children [(elem/add-button *state)
                                              (elem/name-input *state)
                                              (elem/color-dropdown *state)]}

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
                                   :on-action {:event/type :initial-phase-game}}]}}}))
(defn initial-phase-game
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))  screen-bounds (.getVisualBounds (Screen/getPrimary))
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
               :root    {:fx/type  :stack-pane
                         :style    "-fx-background-color: #1e90ff;"
                         :children [
                                    {:fx/type   :v-box
                                     :alignment :top-center
                                     :padding   10
                                     :children  [{:fx/type :label
                                                  :text    (:initial-info @*state)
                                                  :style   "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"}
                                                 (elem/player-turn-info (:name player))
                                                 {:fx/type :label
                                                  :text    (:game-massage @*state)
                                                  :style   "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"}]}
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
                                                  :children  [(elem/table-info *state)
                                                              ]}]}
                                    (elem/hand-view (:hand player) *state)
                                    (elem/image-group state)
                                    (elem/roads-view *state)
                                    (elem/spots-view *state)
                                    ]}}}))
(defn game-view
  [state]
  (let [player (get (vec (:players @*state)) (dec (:player-turn @*state)))
        hand (:hand player)
        dev (:dev-cards player)
        screen-bounds (.getVisualBounds (Screen/getPrimary))
        width (.getWidth screen-bounds)
        height (.getHeight screen-bounds)
        ore-count (count (filter #(= % "ore") hand))
        grain-count (count (filter #(= % "grain") hand))
        wood-count (count (filter #(= % "wood") hand))
        wool-count (count (filter #(= % "wool") hand))
        brick-count (count (filter #(= % "brick") hand))
        show-buy-town? (and (>= ore-count 3) (>= grain-count 2))
        show-buy-settlement? (and (>= wool-count 1) (>= grain-count 1) (>= wood-count 1) (>= brick-count 1))
        show-buy-dev? (and (>= wool-count 1) (>= grain-count 1) (>= ore-count 1))
        show-buy-road? (and (>= brick-count 1) (>= wood-count 1))
        show-dev-cards? (some? dev)
        card-freq (frequencies hand)
        dev-cards-clicked? (contains? #{"knight" "road-building"} (:clicked-resource @*state))
        has-four-of-a-kind? (some (fn [[_ cnt]] (>= cnt 4)) card-freq)
        dice-rolled? (:dice-rolled @*state)
        on-dice-7? (:on-dice-7 @*state)
        shop-view? (:shop-view @*state)
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
                               :children  [(elem/player-turn-info (:name player))
                                           {:fx/type :label
                                            :style "-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;"
                                            :text (:game-massage @*state)}]}

                         :right {:fx/type :scroll-pane
                                 :fit-to-width true
                                 :padding 10
                                 :content {:fx/type   :v-box
                                           :spacing   10
                                           :alignment :center
                                           :children (when (not on-dice-7?) (remove nil?
                                                                                    [(elem/table-info *state)
                                                                                     (when (not shop-view?)(when (not dice-rolled?)(elem/dices-button *state)))
                                                                                     (when (not shop-view?)(when dice-rolled? (elem/dice-views *state)))
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-settlement? (elem/buy-settlement-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-town? (elem/buy-town-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-road? (elem/buy-road-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-buy-dev? (elem/buy-dev-card-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when has-four-of-a-kind? (elem/buy-card-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when show-dev-cards? (elem/hand-dev-view (:dev-cards player) *state))))
                                                                                     (when (not shop-view?)(when dice-rolled? (when dev-cards-clicked? (elem/activate-button))))
                                                                                     (when (not shop-view?)(when dice-rolled? (elem/end-turn-btn)))
                                                                                     ]))
                                           }}
                         :bottom (elem/hand-view (:hand player) *state)
                         :center
                         {:fx/type :anchor-pane
                          :children
                          [{:fx/type :stack-pane
                            :anchor-pane/left 0
                            :anchor-pane/right 0
                            :anchor-pane/top 0
                            :anchor-pane/bottom 0
                            :children
                            (if shop-view?
                              [(cond
                                 (:card-shop-buy @*state)  {:fx/type :v-box
                                                            :spacing 8
                                                            :alignment :center
                                                            :children [(elem/shop-buy-card *state)
                                                                       (elem/buy-this-card-btn)
                                                                       (elem/exit-shop-button )]}
                                 (:card-shop-sell @*state) {:fx/type :v-box
                                                            :spacing 8
                                                            :alignment :center
                                                            :children [(elem/shop-sell-card *state)
                                                                       (elem/sell-this-card-btn)
                                                                       (elem/exit-shop-button)]}
                                 :else nil)]
                              [(elem/image-group state)
                               (elem/roads-view *state)
                               (elem/spots-view *state)])}]}

                         }}}))
(defn end-game-view [state]
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
                                      :text (str "PLAYER " winner " WINS!")
                                      :style "-fx-font-size: 72pt; -fx-font-weight: bold; -fx-text-fill: white;"
                                      :alignment :center}]}
                    :center {:fx/type :stack-pane
                             :children [{:fx/type :label
                                         :text "Thank you for playing CATAN"
                                         :style "-fx-font-size: 24pt; -fx-text-fill: white;"
                                         :alignment :center}]}
                    }}}))


(defn event-handler [event]

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
    :dice-view  (when (:dice-activate @*state)
                  (do
                  (swap! *state assoc :dice-1 (utils/random-dice-number) :dice-2 (utils/random-dice-number))
                  (let [dice-sum (+ (:dice-1 @*state) (:dice-2 @*state))]
                    (if (= dice-sum 7)
                      (do
                        (swap! *state assoc :game-massage "SELECT THE AREA YOU WANT TO RESTRICT")
                        (swap! *state assoc :move-thief true)
                        (services/update-all-hands-second-half *state)
                        (swap! *state assoc :dice-activate false)
                        (swap! *state assoc :on-dice-7 true)
                        )

                        (doseq [coord (:spots @*state)]
                        (services/filing-hand-with-resource (:spot-coordinates (:on-mouse-clicked coord))
                                                   dice-sum *state))
                        ))
                  (swap! *state assoc :dice-activate false)
                  (swap! *state assoc :dice-rolled true)))
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
                (swap! *state assoc :dice-activate true)
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


(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler event-handler}))
(defn start-game []
  (fx/mount-renderer *state renderer)
  (swap! *state assoc :fx/type start-game-view))