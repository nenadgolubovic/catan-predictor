(ns catan-predictor.deck
  (:require [catan-predictor.shop :as shop]))


(def deck-development-card (atom ["knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight" "knight"
                                  "victory-point" "victory-point" "victory-point" "victory-point" "victory-point"
                                  "road-building" "road-building"
                                  "monopoly" "monopoly"
                                  "year-of-plenty" "year-of-plenty"])
  )
(defn get-random-card [deck-atom]
  (let [deck @deck-atom
        chosen (rand-nth deck)]
    (swap! deck-atom (fn [deck] (shop/remove-card chosen deck)))
    chosen))


(defn take-development-card
  [hand deck hand-dev-cards]
  "Take random card from deck and remove cards from hand for buying (ore,wood,grain) and add in development-hand"
  (let [card (get-random-card deck)]
    (swap! hand update :cards #(shop/buy-development-card %))
    (swap! hand-dev-cards update :cards #(conj % card))
    card))




