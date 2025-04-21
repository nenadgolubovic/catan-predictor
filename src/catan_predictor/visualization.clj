(ns catan-predictor.visualization
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]))


(defonce *state (atom {:players-count 1}))


(defn background-image []
  (Background.
    (into-array BackgroundImage
                [(BackgroundImage.
                   (Image. "file:resources/static/start-manu-background.jpg")
                   BackgroundRepeat/NO_REPEAT
                   BackgroundRepeat/NO_REPEAT
                   BackgroundPosition/CENTER
                   (BackgroundSize. 1000 1000 true true true false))])))

(defn event-handler [event]
  (case (:event/type event)
    :add (swap! *state update :players-count inc)
    :remove  (swap! *state update :players-count dec)
    nil))

(defn start-game-view [state]
  {:fx/type :stage
   :showing true
   :title "CATAN"
   :scene {:fx/type :scene
           :root {:fx/type :stack-pane
                  :alignment :center
                  :background (background-image)
                  :children [{:fx/type :label
                              :text "WELCOME TO CATAN!"
                              :style "-fx-text-fill: #FFD700;
                                      -fx-font-size: 100px;
                                      -fx-font-weight: bold;
                                      -fx-background-color: rgba(0, 0, 0, 0.5);
                                      -fx-padding: 20px 20px 20px 20px;
                                      -fx-background-radius: 10px;"
                              :translate-y -500}

                             {:fx/type :h-box
                              :spacing 20
                              :alignment :center
                              :children [{:fx/type :button
                                          :text "Add"
                                          :style "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #3F51B5;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"
                                          :on-action {:event/type :add}}

                                         {:fx/type :button
                                          :text "Remove"
                                          :style "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #3F51B5;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"
                                          :on-action {:event/type :remove}}]
                                          :translate-y 100}
                                         {:fx/type :label
                                          :text (str "Number of players " (:players-count state))
                                          :style "-fx-font-size: 20px;
                                                  -fx-font-weight: bold;
                                                  -fx-background-color: #009688;
                                                  -fx-text-fill: white;
                                                  -fx-padding: 10px 20px;
                                                  -fx-background-radius: 5px;
                                                  -fx-min-width: 200px;
                                                  -fx-min-height: 60px;"

                                          :translate-y 0}
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
                                          :on-action (fn [_]
                                                       (println "Game Started!"))
                                          :translate-y 200
                                         }
                                         ]}}})


(def renderer
  (fx/create-renderer
    :opts {:fx.opt/map-event-handler event-handler}))


(defn start-game []
  (fx/mount-renderer *state renderer)
  (swap! *state assoc :fx/type start-game-view))