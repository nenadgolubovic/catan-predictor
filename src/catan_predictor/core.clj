(ns catan-predictor.core
  (:gen-class)
  (:require [clojure.math :as m]
            [quil.core :as q]
            ))

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
;;
;;
;; Make a hexagon

(defn round-5 [x] (/ (m/round (* x 10000)) 10000.0))
(defn fcos [k] (round-5 (m/cos (* m/PI (/ (+ 1 (* 2 k)) 6)))))
(defn fsin [k] (round-5 (m/sin (* m/PI (/ (+ 1 (* 2 k)) 6)))))
(defn fcos1 [k] (round-5 (m/cos (* m/PI (/ k 3)))))
(defn fsin1 [k] (round-5 (m/sin (* m/PI (/ k 3)))))
;;I want the point from which the other points are made to remain connected to them.
(defn spots [[x y] ks]
  "I will make function which make hexagon area"
  (map (fn [k]
         [(+ x (fcos k)) (+ y (fsin k))])                   ; Za svaku k vrednost izračunaj tačku
       ks))



(defn math-round [n decimals]
  (/ (m/round (* n (m/pow 10 decimals))) (m/pow 10 decimals)))
(defn round-seq
  [seq n]
  (map (fn [[x y]] [(math-round x 3) (math-round y n)]) seq))



;;now I will try to paint red centers of areas
;; I will paint red only coordinate which on distance from center 1, 2 and 4
;; function for distance
;;
;; Euclid distance
;;


(defn distance [x y decimals]
  (let [dist (m/sqrt (+ (m/pow x 2) (m/pow y 2)))]
    (math-round dist decimals)))
;;I realize that centers is on angels (k*pi/6) ke{0,1..12}
;; centers are on 2cos(0) = 2 for first ring)
;; for second ring

(defn centers [points x y]
  "Centers will be every spot approximately 2 far away from the given [x y] point"
  (let [target-distance 2.000]
    (concat
      [[x y]]
      (filter
        #(= (distance (- (second (first %)) x) (- (first (first %)) y) 3)
            target-distance) points))
    ))



;;Second Approach: to make function which make area and more areas

(defn make-ring-area-centers
  [x y r]
  "This function calculate centers of hexagons on ring"
  (distinct
    (round-seq
      (map (fn [k]
             [(+ x (* r (fcos1 k))) (+ y (* r (fsin1 k)))]) [0 1 2 3 4 5]
           ) 3))
  )


(defn make-centers
  [points]
  "This function make centers for full board"
  (distinct
    (round-seq
      (mapcat #(make-ring-area-centers (first %) (second %) 1.732) points)
      3))
  )


(defn make-spots-from-centers
  [centers]
  "This function makes spots of hexagons from provided centers"
  (distinct
    (round-seq
      (mapcat #(spots [(first %) (second %)] [0 1 2 3 4 5])
              centers)
      3)))

(def centers (make-centers (make-ring-area-centers 0.0 0.0 1.732)))

(def points (make-spots-from-centers centers))



(defn distance-1-2
  [[x1 y1] [x2 y2]]
  (math-round (Math/sqrt (+ (Math/pow (- x2 x1) 2) (Math/pow (- y2 y1) 2))
                         ) 3) )

(defn roads
  [points]
  "Def pairs of spots which make a road, distance is 1 between 2 spots always"
  (mapcat (fn [n1]
            (map (fn [n2] [n1 n2]) (filter #(= 1.000 (distance-1-2 n1 %)) points)))
          points))

(def r (roads points))

;; ==========================================================================================
;; To be easier I will make visualize
;; Used https://github.com/quil/quil/blob/master/README.md

(defn setup []
  ; g/frame-rate - seconds to refresh plots when I make some changes
  ; g/background - color of plot (= 0 black)
  (q/frame-rate 30)
  (q/background 0))



(defn draw-board []
  ;; draw roads
  (doseq [road r]
    (let [scaled-x1 (+ 750 (* 50 (first (first road))))
          scaled-y1 (+ 500 (* 50 (second (first road))))
          scaled-x2 (+ 750 (* 50 (first (second road))))
          scaled-y2 (+ 500 (* 50 (second (second road))))]
      (q/stroke 0 0 255)  ; blue roads
      (q/stroke-weight 3)
      (q/line [scaled-x1 (- (q/height) scaled-y1)]
              [scaled-x2 (- (q/height) scaled-y2)])))

  ;; draw points
  (doseq [[x y] (concat points centers)]
    (let [scaled-x (+ 750 (* x 50))
          scaled-y (+ 500 (* y 50))]
      (q/no-stroke) ; no stroke around points
      (if (some #(= [x y] %) centers)
        (q/fill 255 0 0)  ; centers red
        (q/fill 255))     ; points white
      (q/ellipse scaled-x (- (q/height) scaled-y) 10 10))))



(q/defsketch scatter-plot
             :title "CATAN - Nenad"
             :size [1500 1000]
             :setup setup
             :draw draw-board)


;; ==========================================================================================








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
  (let [current-value-of-spot (get-in @area [spot :type-of-building])]
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
  {:knight         14
   :victory-point  5
   :road-building  2
   :monopoly       2
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

