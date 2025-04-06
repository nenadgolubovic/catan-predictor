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

;;merge all cards in deck
(def deck (apply concat (map (fn [[card count]] (repeat count card)) deck-development-card)))

(def random-card (rand-nth deck))


(defn value-card
  []
  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
