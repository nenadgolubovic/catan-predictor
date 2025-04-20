(ns catan-predictor.visualization
  (:use seesaw.core))

(defonce app-state (atom {:players        []
                          :current-screen
                          :start
                          :current-player 0}))



(defn show-gui []
  (invoke-later
    (-> (frame :title "Catan"
               :content "HI"
               :on-close :exit)
        pack!
        show!)))