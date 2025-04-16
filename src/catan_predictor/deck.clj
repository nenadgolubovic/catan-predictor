(ns catan-predictor.deck)


(def deck-development-card
  (atom {:knight         14
         :victory-point  5
         :road-building  2
         :monopoly       2
         :year-of-plenty 2}))


(defn get-random-card [deck-atom]
  (let [deck @deck-atom
        cards (apply concat (map (fn [[card count]] (repeat count card)) deck))
        chosen (rand-nth cards)]
    (swap! deck-atom update chosen dec)
    chosen))

(def resources #{"wool" "brick" "wood" "ore" "grain"})


