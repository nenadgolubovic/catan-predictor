(ns catan-predictor.core
  (:gen-class))

(def recourses #{"wool" "brick" "wood" "ore" "grain"})
(def development-card #{"knight" "victory-point" "road-building" "monopoly" "year-of-plenty"})
(def build-type #{"village" "road" "city"})
(def areas #{"forest" "pastures" "fields" "hills" "mountains"})

(def deck-development-card
  {:knight 14
   :victory-point 5
   :road-building 2
   :monopoly 2
   :year-of-plenty 2})

(defn random-card [deck]
  ;; take random card from deck. Deck is argument, repeat - make list of values*keys, merging all sets of values*keys in one set,
  ;; and take one card
  (let [deck (apply concat (map (fn [[card count]] (repeat count card)) deck))] (rand-nth deck)))

(defn value-card
  []
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
