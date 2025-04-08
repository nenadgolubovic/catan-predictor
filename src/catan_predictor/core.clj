(ns catan-predictor.core
  (:gen-class)
  (:require [clojure.math :as m]))

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



;;-----------------------------BOARD----------------------------------------------------
;; board should look like this
;; -----------------------------
;;               PLAYER1
;;
;;              /\ /\ /\
;;             |1 |2 |3 |
;;             /\ /\ /\ /\
;;            |4 |5 |6 |7 |
;;           /\ /\ /\ /\ /\
;;  PLAYER4 |8 |9 |10|11|12|   PLAYER2
;;           \/ \/ \/ \/ \/
;;            |13|14|15|16|
;;             \/ \/ \/ \/
;;             |17|18|19|
;;              \/ \/ \/
;;
;;               PLAYER2
;; --------------------------
;; Board is good but have to think about that one spot could belong to 3 areas
(def board (atom {:areas
                  (mapv (fn [n] {:area-name (str "area" n)
                                   :position nil
                                   :type nil
                                   :spots (mapv (fn [n] {:spot-name (str "spot" n)
                                                         :connected-spot [(if (= n 6) 1 (inc n))
                                                                          (if (= n 1) 6 (- n 1))]
                                                         :belonging nil
                                                         :type-of-building nil })
                                                (range 1 7))
                                   :paths (mapv (fn [[n m]] {:path-name (str "path" n m)
                                                             :spot-connection [n m]
                                                             :build? false
                                                             :player nil})
                                                [[1 2] [2 3] [3 4] [5 6] [6 1]])
                                   })
                        (range 19))
                  })
                 )


;;Function above generate duplicates of spots,
;; When I tried to illustrate I realize that board is made by hexagons, and when you merge all hexagons you get bigger hexagons
;; So I will try to make function which will make hexagons with degree N
;; If I put argument N equals 1 then only make one hexagon
;; If I put argument N equals 2 then make one hexagon and one layer of hexagons around that hexagon
;; If I put argument N equals 3 then make around that bigger hexagon one more hexagon layer (this is board of Catan)
;; I will change a numeration of board
;; I will present hexagon as (a,b)
;; Central will be (0,0)
;;
;;              /  \ /  \
;;             |1,0 |0,1 |
;;            /  \ /  \ /  \
;;           |1,-1|0,0 |-1,1|
;;            \  / \  / \  /
;;             |-1,0|0,-1|
;;              \  / \  /

;; I will try to do something with theory of graphs, maybe with matrix of neigbours
;; matrix of hex graph
;;    1
;;  6/ \ 2
;;  |   |
;; 5 \ / 3
;;    4
;;
;;    1 2 3 4 5 6
;; 1[[0 1 0 0 0 1]
;; 2 [1 0 1 0 0 0]
;; 3 [0 1 0 1 0 0]
;; 4 [0 0 1 0 1 0]
;; 5 [0 0 0 1 0 1]
;; 6 [1 0 0 0 1 0]]
;;
;; Matrix of distance
;;    1 2 3 4 5 6
;; 1[[0 1 2 3 2 1]
;; 2 [1 0 1 2 3 2]
;; 3 [2 1 0 1 2 3]
;; 4 [3 2 1 0 1 2]
;; 5 [2 3 2 1 0 1]
;; 6 [1 2 3 2 1 0]]
;;
;; For existing problem we have 54 spots, I have to realize how to make graph
;; I will fill manually until I find betters solution, and make for smaller "Catan" with 7 hexagons

;; Try to make spots by x,y coordinate
;; Center x,y = 0,0 ,
;; circle r = r
;; Spots are on angles 30,90, 150, 210,270,330, that is 30 + pi/3*k
;; ke{0,1,2,3,4,5}
;; x = r*cos((1+2k)/6*pi)
;; y = r*sin((1+2k)/6*pi)
;;
;; Make a hexagon

(defn round-5 [x] (/ (m/round (* x 10000)) 10000.0))
(defn fcos [k r] (round-5 (* r (m/cos  (* m/PI (/ (+ 1 (* 2 k)) 6))))))
(defn fsin [k r] (round-5 (* r (m/sin  (* m/PI (/ (+ 1 (* 2 k)) 6))))))

(defn spots [x y r ks]
  (map (fn [k]
         [(+ x (fcos k r)) (+ y (fsin k r))]) ; Za svaku k vrednost izračunaj tačku
       ks))

(spots 0 0 1 [0 1 2 3 4 5])
(def k [0 1 2 3 4 5])

(fcos 9 1)
(fsin 9 1)
;; I will make hash-map where I will present [area spot] as key and [area spot] as connected area
;; I am trying to automatic update area1 and area2, because spot2 in area1 is equivalent
;; spot6 in area2 and spot3 in area1 is equivalent spot5 in area2
;; for now I will only make when area1 has changed that automatically area2 change

(defn upload-connected-area [[area1 spot1] [area2 spot2]]
  (swap! area2 assoc spot2 (@area1 spot1))
  )

(defn upgrade-village
  [area spot player]
  ;;add village on one spot of area
  ;; upgrade, add building type
  ;; integrate blocking spot, if some spot is not nil,onliest than it could be change
  (let [current-value-of-spot (get @area spot)]
  (if (nil? current-value-of-spot)
  (swap! area assoc spot {:type-of-building "village" :player player})
  nil)
  ))
(defn upgrade-town
  [area spot]
  "Only upgrade village to town"
  (let [current-value-of-spot (get-in @area [spot :type-of-building] )]
    (if (= current-value-of-spot "village")
      (swap! area update spot assoc :type-of-building "town")
      nil
      )))
(defn upgrade-road
  [area road player]
  "Upgrade road"
  (let [current-value-of-road (get @area road)]
    (if (nil? current-value-of-road)
      (swap! area assoc road player)
      nil))
  )
(defn add-type
  [area type]
  ;add type to area
  (swap! area assoc :type type)
  )



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
  "I don't do a lot ... yet."
  [& args]
  (println "Hello, World!"))
