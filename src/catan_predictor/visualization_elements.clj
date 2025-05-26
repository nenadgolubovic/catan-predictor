(ns catan-predictor.visualization-elements
  (:require [catan-predictor.visualization-services :as services]

            )
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]
           [javafx.scene.paint Color]
           [javafx.scene.paint ImagePattern]
           [javafx.scene.image Image]))

(defn create-spot-view [x y]
  "Create view of circle on gui cljfx with radius 10 and center of circle on gui on position x = x*100 y = y*100
  Args:
  -x: x axis value of center of circle
  -y: y axis value of center of circle"
  {:fx/type          :circle                                ;type of component is circle
   :center-x         (* 100 x)                              ;center of x are multiply by 100 to be readable on screen
   :center-y         (* 100 y)                              ;center of x are multiply by 100 to be readable on screen
   :radius           10                                     ;radius of circle is 10
   :fill             (Color/rgb 210 191 145)                ;filling circle with color rgb = 210,191,145
   :on-mouse-clicked {:event/type       :spots-click        ;when click on spot with mouse, activate :spot-click event
                      :spot-coordinates [x y]}              ;pass x y, this is important to event function :spot-click know which spots is clicked
   })
(defn create-line-view
  "Create view of line on gui cljfx with stroke 10px, line connect 2 spots x1 = x1*100 x2 = x2*100 y1 = y1*100 y2 = y2*100
    Args:
    -x1: x axis value of first spot
    -y1: y axis value of first spot
    -x2: x axis value of second spot
    -y2: y axis value of second spot"
  [x1 y1 x2 y2]
  {:fx/type          :line                                  ;type of view is line
   :start-x          (* 100 x1)                             ;first spot - x value
   :start-y          (* 100 y1)                             ;first spot - y value
   :end-x            (* 100 x2)                             ;first spot - x value
   :end-y            (* 100 y2)                             ;second spot - y value
   :stroke (Color/rgb 210 180 140)                          ;color of line is rgb = 210,180,140
   :stroke-width     10                                     ;width of line is 10
   :on-mouse-clicked {:event/type       :roads-click        ;activate function :roads-click
                      :road-coordinates [[x1 y1] [x2 y2]]}});save clicked coordinates of line, important to pass to function :roads-click know what is clicked


(defn background-image []
  "Create and get Background which contain background image,
  Photo ar euploaded from local path resources/static/start-manu-background.jpg,
  the background does not repeat and centralize

  Args: no args"
  (Background.                                              ;create new JavaFX Background
    (into-array BackgroundImage                             ;convert BacgroundImage in Java array
                [(BackgroundImage.                          ;Create new BackgroundImage object
                   (Image. "file:resources/static/start-manu-background.jpg") ;Upload Image as file from location resources/static/start-manu-background.jpg
                   BackgroundRepeat/NO_REPEAT               ;no horizontal repeat
                   BackgroundRepeat/NO_REPEAT               ;no vertically repeat
                   BackgroundPosition/CENTER                ;Center Position
                   (BackgroundSize. 100 100                 ;Image size is 100%*100%
                                    true                    ;width as percentage not px (100%)
                                    true                    ;height as Percentage not px(100%)
                                    true                    ;contain
                                    false))]))) ;  with options for proporcional scaling and contain
(defn add-button [state]
  "Button for adding player on Start Game Page
  On Button is text Add Player and is visible if in input name is not nil or if input color is nil
  The button has the function of adding a player to the game with a name and color that is passed to him via a label and dropdown field on the home page.

  Args: state - state of game
  "
  {:fx/type :button
   :text "Add Player"
   :style     "-fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-background-color: #3F51B5;
                -fx-text-fill: white;
                -fx-padding: 10px 20px;
                -fx-background-radius: 5px;
                -fx-min-width: 200px;
                -fx-min-height: 60px;"                      ;font size 20, bolded text, with background color #3F51B5, white text, vertical padding 10, horizontal padding 20, rounding radius 5x, width 200px and height 60px
   :disable (or (clojure.string/blank? (:input-name @state)) ; button cannot be clicked if input-name of state is empty (nil) or if input color is nil
                (nil? (:input-color @state)))
   :on-action {:event/type :add}                            ;if button has pressed, activate function :add from event-type
   })
(defn remove-button [idx]
  "A button that has the function of deleting a player from the game.
  Pressing the button with an x
  on it deletes the player with the index ids from the game.

  Args: idx - the index of the player I want to delete
  "
  {:fx/type   :button                                       ;type button
   :text      "X"                                           ;x is text on button
   :style     "-fx-font-size: 10px;
                -fx-font-weight: bold;
                -fx-background-color: #3F51B5;
                -fx-text-fill: white;
                -fx-padding: 10px 20px;
                -fx-background-radius: 5px;
                -fx-min-width: 20px;
                -fx-min-height: 20px;"
   :on-action {:event/type :remove                          ;font size 20, bolded text, with background color #3F51B5, white text, vertical padding 10, horizontal padding 20, rounding radius 5px, width 20px and height 20px
               :index idx}})

(defn player-list [state]
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
       (:players @state)))})


(defn dices-button [state]
  "Roll dice button. Press of this button present rolling dice event

  Args: state of game"

  {:fx/type   :button
   :alignment :center                                       ;alignment on center
   :style     "-fx-background-color: transparent;"          ;transparent bacground of button
   :graphic   {:fx/type    :image-view                      ;button is image-view, image which have function of button
               :image      {:fx/type :image
                            :url     "file:resources/static/dices.png"} ;url of dice image
               :fit-width  350                              ;width 350
               :fit-height 200}                             ;height 200
   :on-action {:event/type :dice-view}})                    ;activate event :dice view
(defn dice-views [state]
  "This function present dice view after rolling
  present two dice one nears to second one in horizontal order,
  depends of state of game present image of number on dice
  In game numbers will be allocate randomly

  Args: state - state of the game"

  {:fx/type   :h-box                                        ;horizontal box
   :alignment :center                                       ;in center align
   :children  [{:fx/type    :image-view
                :fit-width  200
                :fit-height 200
                :image      {:fx/type :image
                             :url     (services/get-dice-image-url (get @state :dice-1))} ; url
                :clip {:fx/type :rectangle                  ;image will be cliped in polygon 200*200 with a radius of 40 in width and length
                       :width 200
                       :height 200
                       :arc-width 40
                       :arc-height 40}}
               {:fx/type    :image-view                     ;same as above, just for second image
                :fit-width  200
                :fit-height 200
                :image      {:fx/type :image
                             :url     (services/get-dice-image-url (get @state :dice-2))}
                :clip {:fx/type :rectangle
                       :width 200
                       :height 200
                       :arc-width 40
                       :arc-height 40}}]})
(defn name-input
  "Input text field for entering name of player
  When text is changed, state of game will get :input-name value same as inputed in textbox

  Args: state - state of the game"
  [state]
  {:fx/type :text-field                                     ;type - text field
   :prompt-text "Enter name"                                ;Text which is disappears when input something
   :text (:input-name @state)                               ;when input something text automatically present current :input name of state
   :on-text-changed #(swap! state assoc :input-name %)})    ;when change text :input-name of state will get value from text-field
(defn spots-view
  "Spot view present group of spots on GUI,
  will get from state vector of spots

  Args: state - state of the game"
  [state]
  {:fx/type     :group                                      ;type group
   :translate-x -250                                        ;moved -250px on x axis to be readable and arranged with other elements
   :translate-y -100                                        ;moved -100px on y axis to be readable and arranged with other elements
   :children (:spots @state)                                ;get spots from current state of game
   })
(defn roads-view
  "Road view present group of roads (lines) on GUI,
  will get from state vector of roads

  Args: state - state of the game"
  [state]

  {:fx/type     :group                                      ;type group
   :translate-x -250                                        ;moved -250px on x axis to be readable and arranged with other elements
   :translate-y -100                                        ;moved -100px on y axis to be readable and arranged with other elements
   :children (:roads @state)                                ;get spots from current state of game
   })


(defn buy-settlement-button
  "Present button for buying settlement\"
  Args: No args"
  []
  {:fx/type   :button
   :alignment :bottom-right                                 ;bottom right position
   :text "Buy Settlement"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;" ;white text, background color #ff6666, radius of button is 10 and font size 16
   :padding   2
   :v-box/margin 2
   :on-action {:event/type :buy-settlement-btn}             ;active function buy-settlement-btn from event handler
   }
  )
(defn buy-town-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Town"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   2
   :v-box/margin 2
   :on-action {:event/type :buy-town-btn}
   }
  )
(defn buy-road-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Road"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   2
   :v-box/margin 2
   :on-action {:event/type :buy-road-btn}
   }
  )
(defn buy-dev-card-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Development Card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   2
   :v-box/margin 2
   :on-action {:event/type :buy-dev-card-btn}
   }
  )
(defn buy-card-button
  []
  {:fx/type   :button
   :alignment :bottom-right
   :text "Buy Card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :padding   2
   :v-box/margin 2
   :on-action {:event/type :buy-card-btn}
   }
  )
(defn exit-shop-button
  []
  {:fx/type :button
   :text      "Exit from shop"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :on-action {:event/type :exit-shop}
   })
(defn activate-button
  []
  {:fx/type :button
   :text      "Activate dev card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :on-action {:event/type :activate-dev-card}
   })
(defn hexagon [x1 x2 image]
  (let [image-path (str "file:resources/static/area-" image ".jpg")
        image (Image. image-path)
        pattern (ImagePattern. image)]
    {:fx/type :polygon
     :points (vec (map #(* 100 %) (vec (apply concat (services/spots [x1 x2] [0 1 2 3 4 5])))))
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
   :children [(hexagon x y image )
              (circle x y circle-image)]})
(defn generate-image-hex [state]
  (let [areas (vec (:areas state))]
    (map #(hexagon-with-circle (first (:center %)) (second (:center %)) (:resource %) (:number %)) areas)
    ))
(defn card
  [resource type state]
  (let [image-path (str "file:resources/static/"type"-" resource ".jpg")
        image (Image. image-path)
        pattern (ImagePattern. image)
        selected-resource (:clicked-resource @state)
        width (if (= resource selected-resource) 100 80)
        height (if (= resource selected-resource) 143 115)
        arc-height (if (= resource selected-resource) 15 10)
        arc-width (if (= resource selected-resource) 15 10)]

    {:fx/type :rectangle
     :width width
     :height height
     :arc-height arc-height
     :arc-width arc-width
     :fill pattern
     :stroke :gray
     :stroke-width 1
     :on-mouse-clicked {:event/type :card-click
                        :resource resource}
     }))
(defn shop-buy-card [state]
  {:fx/type :v-box
   :spacing 10
   :alignment :center
   :children [{:fx/type :label
               :text "PLEASE CHOOSE CARD WHICH YOU WANT TO BUY"
               :style "-fx-background-color: white;
                       -fx-background-radius: 5px;
                       -fx-padding: 10px;
                       -fx-font-size: 16px;
                       -fx-font-weight: bold;
                       "}
              {:fx/type :flow-pane
               :hgap 10
               :vgap 10
               :alignment :center
               :children [(card "wood" "resource" state)
                          (card "brick" "resource" state)
                          (card "wool" "resource" state)
                          (card "grain" "resource" state)
                          (card "ore" "resource" state)]}]})
(defn shop-sell-card [state]
  {:fx/type :v-box
   :spacing 10
   :alignment :center
   :children [{:fx/type :label
               :text "PLEASE CHOOSE CARD WHICH YOU WANT TO SELL"
               :style "-fx-background-color: white;
                       -fx-background-radius: 5px;
                       -fx-padding: 10px;
                       -fx-font-size: 16px;
                       -fx-font-weight: bold;
                       "}
              {:fx/type :flow-pane
               :hgap 10
               :vgap 10
               :alignment :center
               :children [(card "wood" "resource" state)
                          (card "brick" "resource" state)
                          (card "wool" "resource" state)
                          (card "grain" "resource" state)
                          (card "ore" "resource" state)]}]})
(defn buy-this-card-btn
  []
  {:fx/type :button
   :text      "Buy this card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :on-action {:event/type :buy-this-card}})
(defn sell-this-card-btn
  []
  {:fx/type :button
   :text      "Sell this card"
   :style     "-fx-font-size: 16px; -fx-background-color: #ff6666; -fx-text-fill: white; -fx-background-radius: 10;"
   :on-action {:event/type :sell-this-card}})
(defn hand-view
  [cards state]
  {:fx/type :h-box
   :alignment :bottom-center
   :children (vec (map #(card % "resource" state) cards))}
  )
(defn hand-dev-view
  [cards state]
  {:fx/type :h-box
   :alignment :bottom-right
   :children (vec (map #(card % "dev" state) cards))}
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
   :padding   2
   :v-box/margin 10
   :on-action {:event/type :end-turn}
   })
(defn table-info
  [state]
  {:fx/type :v-box
   :style "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);"
   :spacing 10
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
                         :vp (or (:vp player) 0)
                         :road-length (or (:road-length player) 0)
                         :army-size (or (:army-size player) 0)
                         :color (:color player)})
                      (:players @state)))
     :columns [{:fx/type :table-column
                :text "Player"
                :cell-value-factory :player
                :style "-fx-font-size: 20px; -fx-text-fill: black;"}
               {:fx/type :table-column
                :text "Victory Points"
                :cell-value-factory :vp
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
(defn color-dropdown
  [state]
  (let [all-colors ["red" "blue" "yellow" "green"]
        used-colors (set (map :color (:players @state)))
        available-colors (remove used-colors all-colors)]
    {:fx/type :combo-box
     :prompt-text "Choose color"
     :value (:input-color @state)
     :items (vec available-colors)
     :on-value-changed #(swap! state assoc :input-color %) }))

