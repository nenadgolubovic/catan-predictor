(ns catan-predictor.visualization
  (:require [cljfx.api :as fx])
  (:import [javafx.scene.layout Background BackgroundImage BackgroundPosition BackgroundRepeat BackgroundSize]
           [javafx.scene.image Image]))


(defonce state (atom {:players-count 1}))


(defn background-image []
  (Background.
    (into-array BackgroundImage
                [(BackgroundImage.
                   (Image. "file:resources/static/start-manu-background.jpg")
                   BackgroundRepeat/NO_REPEAT
                   BackgroundRepeat/NO_REPEAT
                   BackgroundPosition/CENTER
                   (BackgroundSize. 1000 1000 true true true false))])))

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
                              :style "-fx-text-fill: white;
                                      -fx-font-size: 60px;
                                      -fx-font-weight: bold;
                                      -fx-background-color: rgba(0, 0, 0, 0.5);
                                      -fx-padding: 20px 20px 20px 20px;
                                      -fx-background-radius: 10px;"
                              :translate-y -300}
                             {:fx/type :button
                              :text "Start Game"
                              :style "-fx-font-size: 20px;
                                      -fx-font-weight: bold;
                                      -fx-background-color: #3F51B5;
                                      -fx-text-fill: white;
                                      -fx-padding: 10px 20px;
                                      -fx-background-radius: 5px;
                                      -fx-min-width: 200px;
                                      -fx-min-height: 60px;"
                              :on-action (fn [_]
                                           (println "Game Started!"))}
                             ]}}})

(def renderer
  (fx/create-renderer))

(defn start-game []
  (fx/mount-renderer state renderer)
  (swap! state assoc :fx/type start-game-view))
