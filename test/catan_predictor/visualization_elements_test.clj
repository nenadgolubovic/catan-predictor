(ns catan-predictor.visualization-elements-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all ]
            [catan-predictor.visualization-elements :refer :all]
            [catan-predictor.visualization-services :as services])
  (:import [javafx.scene.paint Color]
           [javafx.scene.layout Background ]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
))

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
(fact "background-image return valid Background obj"
      (let [bg (background-image)]
        (.getImages bg) => truthy
        (instance? Background bg) => true
        (count (.getImages bg)) => 1
        (-> (.getImages bg)
            (.get 0)
            (.getImage)
            (.getUrl)) => (contains "start-manu-background.jpg")))
(fact "add-button returns a map with expected properties"
      (let [state (atom {:input-name "" :input-color nil})  ;Case 1: both name is blank and color is nil -> button should be disabled
            btn (add-button state)]
        (:text btn) => "Add Player"
        (:disable btn) => true
        (:on-action btn) => {:event/type :add})
      (let [state (atom {:input-name "A" :input-color nil}) ;Case 2: name is present but color is nil -> button should be disabled
            btn (add-button state)]
        (:disable btn) => true)
      (let [state (atom {:input-name "" :input-color :blue});Case 3: color is present but name is blank -> button should be disabled
            btn (add-button state)]
        (:disable btn) => true)
      (let [state (atom {:input-name "A" :input-color :blue}) ;Case 4: both name and color are present -> button should be enabled
            btn (add-button state)]
        (:disable btn) => false))
(fact "player-list generates VBox with a header and rows for each player"
      (let [state (atom {:players [{:name "A" :color "Red"}
                                   {:name "B" :color "Blue"}]})
            result (player-list state)
            children (:children result)]
        (:fx/type result) => :v-box                         ;VBox check
        (:spacing result) => 10
        (count children) => 3 ; 1 header + 2 players ; Header check
        (let [header (first children)]
          (:fx/type header) => :h-box
          (map :text (:children header)) => ["Name" "Color" "Remove"])
        (let [row1 (second children)]                       ;First player row
          (:fx/type row1) => :h-box
          (map :text (take 2 (:children row1))) => ["A" "Red"])
        (let [row2 (nth children 2)]                        ;Second player row
          (:fx/type row2) => :h-box
          (map :text (take 2 (:children row2))) => ["B" "Blue"])))
(fact "dices-button returns a button with an image and correct properties"
      (let [state (atom {})
            result (dices-button state)]
        (:fx/type result) => :button                        ;Basic button structure
        (:alignment result) => :center
        (:style result) => "-fx-background-color: transparent;"
        (:on-action result) => {:event/type :dice-view}
        (let [graphic (:graphic result)]                    ;Check the graphic part (the image)
          (:fx/type graphic) => :image-view
          (:fit-width graphic) => 350
          (:fit-height graphic) => 200

          (let [image (:image graphic)]
            (:fx/type image) => :image
            (:url image) => "file:resources/static/dices.png"))))
(fact "dice-views returns an h-box with two dice images correctly configured"
      (against-background                                   ;Setup the background mocks for services/get-dice-image-url
        [(services/get-dice-image-url 3) => "url-to-dice-3.png"
         (services/get-dice-image-url 5) => "url-to-dice-5.png"])

      (let [state (atom {:dice-1 3 :dice-2 5})
            result (dice-views state)
            children (:children result)]
        (:fx/type result) => :h-box                         ;Check root element properties
        (:alignment result) => :center
        (count children) => 2
        (let [dice1 (first children)]
          (:fx/type dice1) => :image-view                   ;Check first dice image
          (:fit-width dice1) => 200
          (:fit-height dice1) => 200
          (:url (:image dice1)) => "url-to-dice-3.png"

          (let [clip (:clip dice1)]
            (:fx/type clip) => :rectangle
            (:width clip) => 200
            (:height clip) => 200
            (:arc-width clip) => 40
            (:arc-height clip) => 40))
        (let [dice2 (second children)]                      ;Check second dice image
          (:fx/type dice2) => :image-view
          (:fit-width dice2) => 200
          (:fit-height dice2) => 200
          (:url (:image dice2)) => "url-to-dice-5.png"

          (let [clip (:clip dice2)]
            (:fx/type clip) => :rectangle
            (:width clip) => 200
            (:height clip) => 200
            (:arc-width clip) => 40
            (:arc-height clip) => 40))))
(fact "name-input returns a text-field with correct prompt and bound text"
      (let [state (atom {:input-name "A"})
            result (name-input state)]
        (:fx/type result) => :text-field                    ;Check basic fx type and prompt text
        (:prompt-text result) => "Enter name"
        (:text result) => "A"                           ;Check that text reflects current state's :input-name
        (let [on-change (:on-text-changed result)]          ;Check that on-text-changed updates the state correctly
          (on-change "B")
          (:input-name @state) => "B")))
(fact "spots-view returns a group with correct translation and children"
      (let [spots-data [{:x 10 :y 20} {:x 30 :y 40}]
            state (atom {:spots spots-data})
            result (spots-view state)]
        (:fx/type result) => :group                         ;Check fx type
        (:translate-x result) => -250                       ;Check translation
        (:translate-y result) => -100
        (:children result) => spots-data))                  ;Check children equal to spots in state
(fact "roads-view returns a group with correct translation and children"
      (let [roads-data [{:start [0 0] :end [10 10]} {:start [5 5] :end [15 15]}]
            state (atom {:roads roads-data})
            result (roads-view state)]


        (:fx/type result) => :group                         ; Check fx type


        (:translate-x result) => -250                       ; Check translation
        (:translate-y result) => -100
        (:children result) => roads-data))
(fact "buy-settlement-button returns a button with correct properties"
      (let [btn (buy-settlement-button)]
        (:fx/type btn) => :button
        (:alignment btn) => :bottom-right
        (:text btn) => "Buy Settlement"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:padding btn) => 2
        (:v-box/margin btn) => 2
        (:on-action btn) => {:event/type :buy-settlement-btn}))
(fact "buy-town-button returns a button with correct properties"
      (let [btn (buy-town-button)]
        (:fx/type btn) => :button
        (:alignment btn) => :bottom-right
        (:text btn) => "Buy Town"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:padding btn) => 2
        (:v-box/margin btn) => 2
        (:on-action btn) => {:event/type :buy-town-btn}))
(fact "buy-road-button returns a button with correct properties"
      (let [btn (buy-road-button)]
        (:fx/type btn) => :button
        (:alignment btn) => :bottom-right
        (:text btn) => "Buy Road"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:padding btn) => 2
        (:v-box/margin btn) => 2
        (:on-action btn) => {:event/type :buy-road-btn}))
(fact "buy-dev-card-button returns a button with correct properties"
      (let [btn (buy-dev-card-button)]
        (:fx/type btn) => :button
        (:alignment btn) => :bottom-right
        (:text btn) => "Buy Development Card"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:padding btn) => 2
        (:v-box/margin btn) => 2
        (:on-action btn) => {:event/type :buy-dev-card-btn}))
(fact "buy-card-button returns a button with correct properties"
      (let [btn (buy-card-button)]
        (:fx/type btn) => :button
        (:alignment btn) => :bottom-right
        (:text btn) => "Buy Card"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:padding btn) => 2
        (:v-box/margin btn) => 2
        (:on-action btn) => {:event/type :buy-card-btn}))
(fact "exit-shop-button returns a button with correct properties"
      (let [btn (exit-shop-button)]
        (:fx/type btn) => :button
        (:text btn) => "Exit from shop"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:on-action btn) => {:event/type :exit-shop}))
(fact "activate-button returns a button with correct properties"
      (let [btn (activate-button)]
        (:fx/type btn) => :button
        (:text btn) => "Activate dev card"
        (:style btn) => "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
        (:on-action btn) => {:event/type :activate-dev-card}))
(fact "hexagon returns a polygon with correct image and 12 points"
      (let [x 1
            y 1
            image "forest"
            result (hexagon x y image)
            points (:points result)]
        (:fx/type result) => :polygon                       ;type is polygon?
        (count (:points result)) => 12                      ;12 coordinates
        (every? number? points) => truthy            ;each number are number
        (:stroke result) => "black"                         ;existing stroke and fill
        (:stroke-width result) => 1                         ;stroke width is 1
        (instance? ImagePattern (:fill result)) => true))
(fact "circle returns a circle with correct center, radius and fill"
      (let [result (circle 1 2 nil)
            fill (:fill result)]
        (:fx/type result) => :circle
        (:center-x result) => 100
        (:center-y result) => 200
        (:radius result) => 20
        (:stroke result) => "black"
        (:stroke-width result) => 1
        (instance? ImagePattern fill) => true
        (:on-mouse-clicked result) => (contains {:event/type :circle-click
                                                 :center-coordinates [1 2]})))
(fact "circle uses provided image name"
      (let [result (circle 3 4 "custom-image")
            fill (:fill result)]
        (:fx/type result) => :circle
        (:center-x result) => 300
        (:center-y result) => 400
        (instance? ImagePattern fill) => true))
(fact "hexagon-with-circle returns a group map containing hexagon and circle children"
      (let [x 10
            y 20
            image :hex-image
            circle-image :circle-image

            hexagon (fn [x y image] {:shape :hexagon :x x :y y :image image}) ;Mock hexagon and circle functions for testing
            circle (fn [x y circle-image] {:shape :circle :x x :y y :image circle-image})

            hexagon-with-circle (fn [x y image circle-image];Override hexagon-with-circle locally to use mocks
                                  {:fx/type :group
                                   :children [(hexagon x y image)
                                              (circle x y circle-image)]})]

        (hexagon-with-circle x y image circle-image)        ;The expected output is a map with :fx/type :group and children containing mocked hexagon and circle maps
        => {:fx/type :group
            :children [{:shape :hexagon :x 10 :y 20 :image :hex-image}
                       {:shape :circle :x 10 :y 20 :image :circle-image}]}))
(fact "generate-image-hex maps hexagon-with-circle over all areas in state"
      (let [areas [{:center [1 2] :resource :wood :number 5}
                   {:center [3 4] :resource :brick :number 8}]
            state {:areas areas}
            hexagon-with-circle (fn [x y image circle-image];Mock hexagon-with-circle to return a predictable string for testing
                                  (str "hex-circle-" x "-" y "-" image "-" circle-image))
            generate-image-hex (fn [state]                  ;Override the original hexagon-with-circle with mock inside this test
                                 (let [areas (vec (:areas state))]
                                   (map #(hexagon-with-circle (first (:center %))
                                                              (second (:center %))
                                                              (:resource %)
                                                              (:number %)) areas)))]

        (generate-image-hex state)
        => (just ["hex-circle-1-2-:wood-5"
                  "hex-circle-3-4-:brick-8"])))
(fact "card returns rectangle with larger size if resource is selected"
      (let [state (atom {:clicked-resource :wood})
            resource :wood
            type "resource-type"
            result (card resource type state)]
        (:width result) => 100
        (:height result) => 143
        (:arc-height result) => 15
        (:arc-width result) => 15
        (:fill result) := (fn [fill] (instance? ImagePattern fill))
        (:stroke result) => :gray
        (:stroke-width result) => 1
        (:on-mouse-clicked result) => {:event/type :card-click :resource resource}))
(fact "card returns rectangle with smaller size if resource is not selected"
      (let [state (atom {:clicked-resource :brick})
            resource :wood
            type "resource-type"
            result (card resource type state)]
        (:width result) => 80
        (:height result) => 115
        (:arc-height result) => 10
        (:arc-width result) => 10))

(fact "buy-this-card-btn returns a button with correct text and event"
      (let [btn (buy-this-card-btn)]
        (:fx/type btn) => :button
        (:text btn) => "Buy this card"
        (:style btn) => (contains "-fx-background-color: #ff6666")
        (:on-action btn) => {:event/type :buy-this-card}))
(fact "sell-this-card-btn returns a button with correct text and event"
      (let [btn (sell-this-card-btn)]
        (:fx/type btn) => :button
        (:text btn) => "Sell this card"
        (:style btn) => (contains "-fx-background-color: #ff6666")
        (:on-action btn) => {:event/type :sell-this-card}))
(fact "hand-view returns h-box with correct children count for given cards"
      (let [cards ["wood" "brick" "wool"]
            state (atom {})
            view (hand-view cards state)]
        (:fx/type view) => :h-box
        (:alignment view) => :bottom-center
        (count (:children view)) => (count cards)
        (every? #(= :rectangle (:fx/type %)) (:children view)) => truthy))
(fact "hand-dev-view returns h-box with correct children count for dev cards"
      (let [cards ["knight" "monopoly"]
            state (atom {})
            view (hand-dev-view cards state)]
        (:fx/type view) => :h-box
        (:alignment view) => :bottom-right
        (count (:children view)) => (count cards)
        (every? #(= :rectangle (:fx/type %)) (:children view)) => truthy))
(fact "image-group returns group with translated position and children"
      (let [state {:areas [{:center [10 20] :resource "wood" :number 5}
                           {:center [30 40] :resource "brick" :number 8}]}
            view (image-group state)]
        (:fx/type view) => :group
        (:translate-x view) => -250
        (:translate-y view) => -100
        (sequential? (:children view)) => true))
(fact "player-turn-info returns label with correct text and style"
      (let [player "A"
            view (player-turn-info player)]
        (:fx/type view) => :label
        (:text view) => "PLAYER TURN: A"
        (:style view) => (contains "-fx-font-size: 20px")
        (:style view) => (contains "-fx-text-fill: white")))
(fact "end-turn-btn returns button with correct text, style, padding, margin and event"
      (let [btn (end-turn-btn)]
        (:fx/type btn) => :button
        (:text btn) => "End Turn"
        (:style btn) => (contains "-fx-background-color: #ff6666")
        (:padding btn) => 2
        (:v-box/margin btn) => 10
        (:on-action btn) => {:event/type :end-turn}))
(fact "table-info returns a v-box with styled table and correct player data"
      (let [state (atom {:players [{:name "A" :vp 5 :road-length 3 :army-size 2 :color "red"}
                                   {:name "B" :vp nil :road-length nil :army-size nil :color "blue"}]})
            view (table-info state)
            table (first (:children view))] ; get the table-view component inside v-box
        (:fx/type view) => :v-box
        (:style view) => (contains "-fx-background-color: white")
        (:fx/type table) => :table-view
        (:items table) => [{:player "A" :vp 5 :road-length 3 :army-size 2 :color "red"}
                           {:player "B" :vp 0 :road-length 0 :army-size 0 :color "blue"}]
        (count (:columns table)) => 5
        (map :text (:columns table)) => ["Player" "Victory Points" "Road Length" "Army Size" "Color"]))
(fact "color-dropdown returns a combo-box with available colors"
      (let [state (atom {:players [{:color "red"} {:color "blue"}]
                         :input-color "yellow"})
            view (color-dropdown state)]
        (:fx/type view) => :combo-box
        (:prompt-text view) => "Choose color"
        (:value view) => "yellow"
        (:items view) => (just ["yellow" "green"] :in-any-order)   ; should contain colors not used ("yellow", "green")
        ((:on-value-changed view) "green")
        @state => (contains {:input-color "green"})))
(fact "shop-buy-card returns a v-box with correct label and 5 cards"
      (let [state (atom {:clicked-resource nil})
            result (shop-buy-card state)
            children (:children result)
            label (first children)
            flow-pane (second children)
            card-children (:children flow-pane)]
        (:fx/type result) => :v-box                         ;vbox check
        (:spacing result) => 10
        (:alignment result) => :center
        (:fx/type label) => :label                          ; label check
        (:text label) => "PLEASE CHOOSE CARD WHICH YOU WANT TO BUY"
        (:fx/type flow-pane) => :flow-pane                  ;Flow-pane check
        (:hgap flow-pane) => 10
        (:vgap flow-pane) => 10
        (:alignment flow-pane) => :center
        (count card-children) => 5                          ;cards check does it 5
        (every? #(= (:fx/type %) :rectangle) card-children) => true
        (every? #(contains? (:on-mouse-clicked %) :resource) card-children) => true
        (set (map #(get-in % [:on-mouse-clicked :resource]) card-children))
        => #{"wood" "brick" "wool" "grain" "ore"}))         ;check does every card type resource
(fact "shop-sell-card returns a v-box with correct label and 5 cards"
      (let [state (atom {:clicked-resource nil})
            result (shop-sell-card state)
            children (:children result)
            label (first children)
            flow-pane (second children)
            card-children (:children flow-pane)]
        (:fx/type result) => :v-box                         ;vbox check
        (:spacing result) => 10
        (:alignment result) => :center
        (:fx/type label) => :label                          ;label check
        (:text label) => "PLEASE CHOOSE CARD WHICH YOU WANT TO SELL"
        (:fx/type flow-pane) => :flow-pane
        (:hgap flow-pane) => 10                             ;Flow-pane check
        (:vgap flow-pane) => 10
        (:alignment flow-pane) => :center
        (count card-children) => 5                          ;cards check does it 5
        (every? #(= (:fx/type %) :rectangle) card-children) => true
        (every? #(contains? (:on-mouse-clicked %) :resource) card-children) => true
        (set (map #(get-in % [:on-mouse-clicked :resource]) card-children))
        => #{"wood" "brick" "wool" "grain" "ore"}))         ;check does every card type resource

