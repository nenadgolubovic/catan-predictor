(ns catan-predictor.player
  (:require [catan-predictor.shop :as shop]
            [catan-predictor.deck :as deck]))


(defn create-player [name color]
  "create player"
  {:name name
   :settlement []
   :towns []
   :roads []
   :hand []
   :dev-cards []
   :color color
   :victory-points nil
   :road-length nil
   :knight-length nil
   :longest-route false
   :biggest-army false})

(defn buy-dev-card
  [player]
  "Take random card from deck and remove cards from hand for buying (ore,wood,grain) and add in development-hand"
  (let [card (deck/get-random-card deck/deck-development-card)]
    (swap! player update :hand #(shop/buy-development-card %))
    (swap! player update :dev-cards #(conj % card))
    card))

(defn upgrade-settlement
  [player spot]
  "Define spot as settlement"
  (swap! player update :hand
         #(shop/buy-settlement %))
  (swap! player update :settlement
         #(conj % spot)))

(defn upgrade-town
  [player spot]
  "Upgrade settlement to town"
  (let [spots (:settlement @player)]
    (if (some #(= % spot) spots)
      (do
        (swap! player update :hand
               #(shop/buy-town %))
        (swap! player update :settlement
               #(remove (fn [x] (= spot x)) %))
        (swap! player update :towns
               #(conj % spot)))
      nil)))

(defn calculate-vp
  "calculate victory points"
  [player]
  (let [count-settlement (count (:settlement @player))
        count-towns  (* 2 (count (:towns @player)))
        vp (count (filter #(= % "victory-point") (:dev-cards @player) ))
        longest-route (if (:longest-route @player) 2 0)
        biggest-army (if (:biggest-army @player) 2 0)]
    (+ count-settlement count-towns vp longest-route biggest-army)))

(defn get-resource
  [player resource]
  (swap! player update :hand
         #(conj % resource)))
