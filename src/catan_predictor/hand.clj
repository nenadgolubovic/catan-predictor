(ns catan-predictor.hand
  (:require [catan-predictor.shop :as shop]))


(def hand (atom {:player nil
                 :cards []}))


(defn update-hand
  [hand new-card]
  "Add new card in hand"
  (swap! hand update :cards conj new-card))

(update-hand hand "1")

(shop/exchange-cards hand "1" "Nenad" 2)




(defn get-cards [card])

(defn drop-cards [card])
