(ns catan-predictor.core
  (:gen-class))

(def recourses #{"wool" "brick" "wood" "ore" "grain"})
(def development-card #{"knight" "victory-point" "road-building" "monopoly" "year-of-plenty"})
(def build-type #{"village" "road" "city"})
(def areas-types #{"forest" "pastures" "fields" "hills" "mountains"})


;;-----------------------------AREA----------------------------------------------------
;Area present area in basic catan game, using atom to present features of one area
; :position- :position on field of game, :type - areas-type, spot1 to spot 6 present spot on border of area
; and road presents borders of area
; one area in game is like:
;            spot1___road12___spot2
;                /            \road23
;         road61/              \
;         spot6/     kind       \spot3
;              \                /
;         road56\              /road34
;                \            /
;            spot5---road45---spot4
;
(def area (atom {:position nil
                 :type nil
                 :spot1 nil
                 :spot2 nil
                 :spot3 nil
                 :spot4 nil
                 :spot5 nil
                 :spot6 nil
                 :road12 nil
                 :road23 nil
                 :road34 nil
                 :road56 nil
                 :road61 nil}))

(defn add-village
  ;;add village on one spot of area
  [area spot player]
  (swap! area assoc spot player))



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
