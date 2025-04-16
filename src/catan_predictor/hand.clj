(ns catan-predictor.hand)


(def hand (atom {:player nil
                 :cards []}))


(defn update-hand
  [hand new-card]
  "Add new card in hand"
  (swap! hand update :cards conj new-card))

(update-hand hand "nan")


(defn get-cards [card])

(defn drop-cards [card])
