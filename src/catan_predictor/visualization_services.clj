(ns catan-predictor.visualization-services
  (:require [catan-predictor.utils :as utils]))



(defn roads
  "Def pairs of spots which make a road, distance is 1 between 2 spots always
  Args: points"
  [points]
  (mapcat (fn [n1]                                          ;mapcat for flattens  all pairs
            (map (fn [n2] [n1 n2])                          ;for each n2 looking for n1 which is on distance 1.000 from n2
                 (filter #(= 1.000 (utils/distance-1-2 n1 %)) points))) ;take all point (n1) from points and calculate distance of 1.000
          points))

(defn make-ring-area-centers
  [x y r]
  "This function calculate centers of hexagons on ring"
  (distinct
    (utils/round-seq
      (map (fn [k]
             [(+ x (* r (utils/fcos1 k))) (+ y (* r (utils/fsin1 k)))]) [0 1 2 3 4 5]
           ) 3))
  )
(defn make-centers
  [points]
  "This function make centers for full board"
  (distinct
    (utils/round-seq
      (mapcat #(make-ring-area-centers (first %) (second %) 1.732) points)
      3))
  )
(defn spots [[x y] ks]
  "I will make function which make hexagon area"
  (distinct
    (utils/round-seq
      (map (fn [k]
             [(+ x (utils/fcos k)) (+ y (utils/fsin k))])
           ks) 3)))
(defn make-spots-from-centers
  [centers]
  "This function makes spots of hexagons from provided centers"
  (distinct
    (utils/round-seq
      (mapcat #(spots [(first %) (second %)] [0 1 2 3 4 5])
              centers)
      3)))
(defn remove-card
  [cards-type hand]
  "Select type of cards and delete one from hand"
  (let [hand (vec hand)
        index (some #(when (= (second %) cards-type) (first %))
                    (map-indexed vector hand))]
    (if index
      (vec (concat (subvec hand 0 index) (subvec hand (inc index))))
      hand)))
(defn remove-n-cards
  [type-card hand n]
  (loop [hand hand
         n n]
    (if (< 0 n)
      (recur (remove-card type-card hand) (dec n))
      hand)))
(defn buy-settlement [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" (remove-n-cards "wool" (remove-n-cards "grain" hand 1) 1) 1) 1) )
(defn buy-town
  [hand]
  (remove-n-cards "ore" (remove-n-cards "grain" hand 2) 3))
(defn buy-road
  [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" hand 1) 1))
(defn buy-development-card
  [hand]
  (remove-n-cards "grain" (remove-n-cards "ore" (remove-n-cards "wool" hand 1) 1) 1))
(defn remove-once
  [item coll]
  "split collection on before(all before item appear) and after (from first appear item to end of collection)
  fn doing concatenation of before and after without of first item in coll after"
  (let [[before after] (split-with #(not= % item) coll)]
    (concat before (rest after))))
(defn create-area [center points resources numbers]
  "make area map with center (coordinate of centers)
  resource (resource of area)
  spots (all spots connected with that area)
  number (dice number which provide resource)"
  (let [center center
        resource (rand-nth @resources)
        number (if (= "dust" resource)
                 nil
                 (rand-nth @numbers))

        spots (filter #(= 1.000 (utils/distance-1-2 [(first center) (second center)] [(first %) (second %)])) points)]
    (swap! resources
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once resource res-list)]
               first-removed)))
    (swap! numbers
           (fn [res-list]
             "delete from atom resource and bring back atom without resource"
             (let [first-removed (remove-once number res-list)]
               first-removed)))
    {:center center
     :resource resource
     :spots spots
     :number number}))
(defn create-areas-from-centers
  [points centers resources numbers]
  (map #(create-area % points resources numbers) centers))
(defn coords-in-roads?
  [coords player-name state]
  (let [roads (:roads (first (filter #(= (:name %) player-name) (:players @state))))]
    (some (fn [[a b]]
            (or (= coords a) (= coords b)))
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
  [dice-number]
  "get image depends on dice number"
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
  [number num-players]
  "function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1"
  (if (= number num-players)
    1
    (inc number))
  )
(defn player-turn-dec
  [number]
  "function that increments the player's ordinal number so that we know who has the move,
  if the last player plays then the next player with ordinal number 1"
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