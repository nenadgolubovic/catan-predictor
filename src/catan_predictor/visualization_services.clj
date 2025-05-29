(ns catan-predictor.visualization-services
  (:require [catan-predictor.utils :as utils]))



(defn roads
  "Def pairs of spots which make a road, distance is 1 between 2 spots always

  Args: points - a collection of points from which paths are made that are 1 distance apart"
  [points]
  (mapcat (fn [n1]                                          ;mapcat for flattens  all pairs
            (map (fn [n2] [n1 n2])                          ;for each n2 looking for n1 which is on distance 1.000 from n2
                 (filter #(= 1.000 (utils/distance-1-2 n1 %)) points))) ;take all point (n1) from points and calculate distance of 1.000
          points))
(defn make-ring-area-centers
  "This function calculate centers of hexagons on ring

  Args:
  x - x-axis value
  y - y-axis value
  r - radius "
  [x y r]
  (distinct                                                 ;take only unique values
    (utils/round-seq                                        ; round on 3 spot
      (map (fn [k]
             [(+ x (* r (utils/fcos1 k))) (+ y (* r (utils/fsin1 k)))]) [0 1 2 3 4 5] ;for k elements [0 1 2 3 4 5] calculate function X = x+r*cos(k) Y= y+r*cos(k)
           ) 3))
  ;on this way function will calculate spots of hexagons, but not area hexagon, hexagons of centers. Centers of new hexagons,
  ; wraper of initial area is also hexagon , because centers of wraper hexagons is on 0, 60, 120, 180, 240, 300 degrees (k*pi/3) - in fcos1 and fsin1, in relation to initial center
  ;
  )
(defn make-centers
  "This function make centers for full board

  Args: points - collection of points"
  [points]
  (distinct                                                 ;only unique values takes
    (utils/round-seq                                        ;round on 3 decimals
      (mapcat #(make-ring-area-centers (first %) (second %) 1.732) points)
      ;map and concat all elements from points, use make-ring-area-centers to take first and second
      ; element of spots and calculate 1.732 (1*2cos0) distance from there and put center on that spot
      3))
  )
(defn spots
  "I will make function which make hexagon area

  Args:
  [x y] - collection of x value and y value
  ks - collection of numbers
  "

  [[x y] ks]
  (distinct                                                 ; unique values return
    (utils/round-seq                                        ;round on 3
      (map (fn [k]                                          ;
             [(+ x (utils/fcos k)) (+ y (utils/fsin k))])   ;return x and y values of all spots in hexagons with formula cos(1+2k*PI/6) and sin(1+2k*PI/6)
           ks) 3)))
(defn make-spots-from-centers
  "This function makes spots of hexagons from provided centers

  Args: centers - collection of points (centers of hexagons)"
  [centers]
  (distinct
    (utils/round-seq
      (mapcat #(spots [(first %) (second %)] [0 1 2 3 4 5]) ;calculate spots for each provided coords of center, centers is in format [x y], because of that we use first and second
              centers)
      3)))
(defn remove-card
  "Select type of cards and delete one from hand

  Args:
    - cards-type -> type of card which want to be deleted
    - hand -> collection of cards"
  [cards-type hand]
  (let [hand (vec hand)                                     ;make vector from hand collection
        index (some #(when (= (second %) cards-type) (first %)) ; find index of first element which card-type is same as
                    (map-indexed vector hand))]             ; return hand as collection of [index element]
    (if index                                               ;if index not nil (if elements existing in hand)
      (vec (concat (subvec hand 0 index) (subvec hand (inc index))))
      ;return vector of combined 2 subvec (first is from beginning to index, and after index)  on that way are secured that take all elements instead of element with provided index
      hand)))
(defn remove-n-cards
  "Remove n numbers of element of cards
  Args:
    - type-card - resource
    - hand - collection of resources
    - n - number of elements which want to removed"
  [type-card hand n]
  (loop [hand hand                                          ; current state of hand
         n n]                                               ; number of cards which have to be removed
    (if (< 0 n)                                             ; do if n is 0 or higher
      (recur (remove-card type-card hand) (dec n))          ; remove card with is same as arg "type-card" and reduce n by 1
      hand)))                                               ;if n is <=0 return hand
(defn buy-settlement
  "Update hand with buying settlement, reduce resource brick wood wool and grain by one

  Args: hand - collection of resource"
  [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" (remove-n-cards "wool" (remove-n-cards "grain" hand 1) 1) 1) 1) ) ; remove all of them by 1
(defn buy-town
  "Update hand with buying town, reduce resource 2 grain and 3 ores

  Args: hand - collection of resource"
  [hand]
  (remove-n-cards "ore" (remove-n-cards "grain" hand 2) 3))
(defn buy-road
  "Update hand with buying road, reduce resource 1 wood and 1 brick

  Args: hand - collection of resource"
  [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" hand 1) 1))
(defn buy-development-card
  "Update hand with buying development card, reduce resource 1 grain and 1 ore and 1 wool

  Args: hand - collection of resource"
  [hand]
  (remove-n-cards "grain" (remove-n-cards "ore" (remove-n-cards "wool" hand 1) 1) 1))
(defn remove-once
  "split collection on before(all before item appear) and after (from first appear item to end of collection)
  fn doing concatenation of before and after without of first item in coll after

  Args:
    - item - item for remove
    - coll - collection from where we want to delete item"
  [item coll]
  (let [[before after] (split-with #(not= % item) coll)]
    (concat before (rest after))))
(defn create-area
  "make area map with center (coordinate of centers)
  resource (resource of area)
  spots (all spots connected with that area)
  number (dice number which provide resource)

  Args:
    - center  - coordinates of center of area
    - points  - coordinates of spots of area (hexagon)
    - resource  - resource which is area rich
    - numbers - number which present that area"
  [center points resources numbers]
  (let [center center
        resource (rand-nth @resources)                      ;resource is random of current atom resources, resource will be deleted after choosing, so have to be atom to can update
        number (if (= "dust" resource)
                 nil
                 (rand-nth @numbers))                       ;number is random of current atom numbers, numbers will be deleted after choosing, so have to be atom to can update

        spots (filter #(= 1.000 (utils/distance-1-2 [(first center) (second center)] [(first %) (second %)])) points)] ; spots is from provided points all points which is on distance 1 of center
    (swap! resources
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once resource res-list)]
               first-removed)))                             ; when resource has chosen, it will be removed from resources atom
    (swap! numbers
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once number res-list)]; when resource has chosen, it will be removed from numbers atom
               first-removed)))
    {:center   center
     :resource resource
     :spots    spots
     :number   number}                                      ;make map of center, resource, spot and number
    ))
(defn create-areas-from-centers
  "Create areas from collection of centers

  Args:
    - points - collection of points
    - centers - collection of centers
    - resources - collection of resources
    - numbers - numbers "
  [points centers resources numbers]
  (map #(create-area % points resources numbers) centers))  ; do create-area from centers
(defn coords-in-roads?
  "Check if given coordinates are part of any road owned by the specified player.
  This function will be important to know can we build roads in game, if coords of wanted roads for build is in built settlement, than we will be able to build road
   Args:
    - coords: a coordinate pair (e.g. [x y]) to check
    - player-name: the name (string) of the player whose roads to check
    - state: an atom or map containing the game state, which includes players and their roads"
  [coords player-name state]
  (let [roads (:roads (first (filter #(= (:name %) player-name) (:players @state))))] ; take a list of roads for players with name same as arg player-name form state
    (some (fn [[a b]]                                       ;check does it any spot in any coords same as in any road
            (or (= coords a) (= coords b)))                 ;for each road with cords [a b] check does it coords overlaping with a or b
          roads)))


(defn coords-in-settlement-or-roads?
  [coords player-name state]
  (let [player (first (filter #(= (:name %) player-name) (:players @state)))
        settlement (:settlement player)
        roads (:roads player)]
    (some (fn [point]
            (or (some #(= point %) settlement)
                (some (fn [[a b]] (or (= point a) (= point b)))  roads)))
          coords)))
(defn coords-in-last-settlement?
  [coords player-name state]
  (let [player (first (filter #(= (:name %) player-name) (:players @state)))
        last-settlement (last (:settlement player))]
    (some #(= % last-settlement) coords)))
(defn build-settlement
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state)))
        player-idx (dec (:player-turn @state))
        game-phase (= "Game" (:phase @state))]

    (when (or (not game-phase)
              (coords-in-roads? coords (:name player) state))
      (try
        (swap! state update-in [:players player-idx :hand] buy-settlement)
        (catch Exception e
          (swap! state assoc :game-massage e)))

      (swap! state update :spots
             (fn [spots]
               (mapv (fn [spot]
                       (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                         (assoc spot :fill (:color player))
                         spot))
                     spots)))

      (swap! state update :players
             (fn [players]
               (into []
                     (map (fn [v]
                            (if (= (:name v) (:name player))
                              (update v :settlement (fnil conj []) coords)
                              v))
                          players))))

      (swap! state assoc :game-massage "Successful build settlement" ))))
(defn build-town
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state)))
        player-idx (dec (:player-turn @state))]

    (if (some #(= coords %) (:settlement player))
      (do
        (try
          (swap! state update-in [:players player-idx :hand] buy-town)
          (catch Exception e
            (println "No resource")))
        (try
          (swap! state update-in [:players player-idx :settlement] (fn [settlement] (remove #(= % coords) settlement)))
          (catch Exception e
            (println "Not your settlement")))
        (swap! state update :players
               (fn [players]
                 (into []
                       (map (fn [v]
                              (if (= (:name v) (:name player))
                                (update v :towns (fnil conj []) coords)
                                v))
                            players))))

        (swap! state update :spots
               (fn [spots]
                 (mapv (fn [spot]
                         (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                           (assoc spot :radius 20)
                           spot))
                       spots))))
      (print "Not your settlement, you have to build settlement first, then town ")
      )


    ))
(defn build-road
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state)))
        player-idx (dec (:player-turn @state))
        game-phase (= "Game" (:phase @state))]

    (when (or (and game-phase
                   (coords-in-settlement-or-roads? coords (:name player) state))
              (and (not game-phase)
                   (coords-in-last-settlement? coords (:name player) state)))
      (when game-phase
        (try
          (swap! state update-in [:players player-idx :hand] buy-road)
          (catch Exception e
            (swap! state assoc :game-massage e))))

      (swap! state update :roads
             (fn [roads]
               (mapv (fn [road]
                       (if (= (:road-coordinates (:on-mouse-clicked road)) coords)
                         (assoc road :stroke (:color player))
                         road))
                     roads)))

      (swap! state update :players
             (fn [players]
               (into []
                     (map (fn [v]
                            (if (= (:name v) (:name player))
                              (update v :roads (fnil conj []) coords)
                              v))
                          players))))
      )))

(defn get-dice-image-url
  "Return image depends on dice number

  Args: dice-number - number between 1-6 get on dice"
  [dice-number]
  (str "file:resources/static/dice-" dice-number ".png"))

(defn update-winner [state view]
  (let [players (:players @state)
        winner-player (some (fn [player]
                              (when (>= (:vp player) 10)
                                (:name player)))
                            players)]
    (if winner-player
      (swap! state assoc
             :winner winner-player
             :fx/type view)
      @state)))
(defn take-resources [coords number state]
  "function which from board when you pass coordinates of one spots and number and extract info of resources
  connected with that spot"
  (map :resource
       (filter (fn [area]
                 (some (fn [spot] (= coords spot)) (:spots area)))
               (filter #(= (str number) (:number %)) (:areas @state))
               )))
(defn filing-hand-with-resource [coordinates number state]
  "Players who have a settlement or town on the coordinates get the resources.
   Towns give double resources."
  (swap! state update :players
         (fn [players]
           (mapv (fn [player]
                   (cond
                     (some #{coordinates} (:towns player))
                     (update player :hand into (vec (concat (take-resources coordinates number state)
                                                            (take-resources coordinates number state))))
                     (some #{coordinates} (:settlement player))
                     (update player :hand into (vec (take-resources coordinates number state)))
                     :else player))
                 players))))

(defn player-turn-inc
  "Function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1
  Args:
    - number - what is index of player who play
    - num-player - count of players"
  [number num-players]
  (if (= number num-players)                                ;if index of player is same as number of players, return 1, because if 4 players playing and 4th player is in on turn, if we want to end turn and go on next, this will not return 5, it will return 1
    1
    (inc number))                                           ;inc number (index of player)
  )

(defn player-turn-dec
  "function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1

  Args: number - number for decreasing"
  [number]

  (dec number))

(defn take-development-card [state]
  (let [player-idx (dec (:player-turn @state))
        chosen (rand-nth (:development-deck @state))]
    (swap! state update :development-deck #(remove-card chosen %))
    (swap! state update-in [:players player-idx :dev-cards] #(conj % chosen))
    (swap! state update-in [:players player-idx :hand] buy-development-card)
    (println "Chosen card:" chosen)))
(defn add-edge
  [graph node neighbor]
  (update graph node (fnil conj []) neighbor))
(defn build-graph
  [edges]
  (reduce (fn [g [a b]]
            (-> g
                (add-edge a b)
                (add-edge b a)))
          {}
          edges))
(defn find-all-paths
  [graph start end & [path]]
  (let [path (conj (or path []) start)]
    (if (= start end)
      [path]
      (when-let [neighbors (graph start)]
        (apply concat
               (for [node neighbors
                     :when (not (some #(= % node) path))]
                 (find-all-paths graph node end path)))))))
(defn longest-path
  [paths]
  (reduce
    (fn [longest path]
      (if (> (count path) (count longest))
        path
        longest))
    []
    paths))
(defn longest-route-length
  "Based on DFS algorithm and graphs teory [7] "
  [roads]
  (let [graph (build-graph roads)
        nodes (keys graph)
        all-paths (for [start nodes
                        end nodes
                        :when (not= start end)]
                    (find-all-paths graph start end))
        flat-paths (apply concat all-paths)
        longest (longest-path flat-paths)]
    (max 0 (dec (count longest)))))
(defn update-players-vp [state]
  (let [winner-longest-route (:winner-longest-route @state)
        winner-army-size (:winner-army-size @state)]
    (swap! state update :players
           (fn [players]
             (vec
               (map-indexed
                 (fn [idx player]
                   (let [count-settlement (count (:settlement player))
                         count-towns (* 2 (count (:towns player)))
                         vp-cards (count (filter #(= % "victory-point") (:dev-cards player)))
                         longest-route (if (= (:name player) winner-longest-route) 2 0)
                         biggest-army (if (= (:name player) winner-army-size) 2 0)
                         total-vp (+ count-settlement count-towns vp-cards longest-route biggest-army)]
                     (assoc player :vp total-vp)))
                 players))))))
(defn update-current-player-road-length! [state]
  (let [player-idx (dec (:player-turn @state))
        path [:players player-idx :road-length]]
    (swap! state assoc-in path
           (longest-route-length (get-in @state [:players player-idx :roads])))
    )
  )
(defn player-with-longest-route [state]
  (let [players (:players @state)
        current-winner-name (:winner-longest-route @state)
        current-winner (some #(when (= (:name %) current-winner-name) %) players)
        current-winner-length (or (:road-length current-winner) 0)
        max-player (apply max-key #(or (:road-length %) 0) players)
        max-length (or (:road-length max-player) 0)]
    (when (>= max-length 3)
      (when (> max-length current-winner-length)
        (swap! state assoc :winner-longest-route (:name max-player))
        ))))
(defn player-with-largest-army [state]
  (let [players (:players @state)
        current-winner-name (:winner-army-size @state)
        current-winner (some #(when (= (:name %) current-winner-name) %) players)
        current-winner-size (or (:army-size current-winner) 0)
        max-player (apply max-key #(or (:army-size %) 0) players)
        max-size (or (:army-size max-player) 0)]
    (when (>= max-size 3)
      (when (> max-size current-winner-size)
        (swap! state assoc :winner-army-size (:name max-player))))))
(defn update-all-hands-second-half [state]
  (swap! state
         (fn [state]
           (let [players (:players state)]
             (let [updated-players
                   (mapv (fn [player-map]
                           (let [hand (:hand player-map)
                                 new-hand (if (> (count hand) 7)
                                            (let [shuffled (shuffle hand)
                                                  half (quot (count shuffled) 2)]
                                              (drop half shuffled))
                                            hand)]
                             (assoc player-map :hand new-hand)))
                         players)]
               (assoc state :players updated-players))))))