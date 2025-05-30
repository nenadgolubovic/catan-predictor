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
  "Function return true if passed coords is in roads or settlement in passed :player

  Args:
    - coords - coordinates on board
    - player-name - name of players for whom we want to check does it have coords in settlement or roads
    - state - state of the game"
  [coords player-name state]
  (let [player (first (filter #(= (:name %) player-name) (:players @state))) ; player who is on turn (find for provided name all info of player in state)
        settlement (:settlement player)                     ;settlements of player
        roads (:roads player)]                              ;roads of player
    (some (fn [point]
            (or (some #(= point %) settlement)              ;check does coords is in settlement
                (some (fn [[a b]] (or (= point a) (= point b)))  roads))) ;check does coords in roads
          coords)))
(defn coords-in-last-settlement?
  "Defined does coords passed coords last in :settlement of player,
  This is important for player in initial phase of game, on that way game give opportunity for player to only build road nears to last settlement,
  because by the rules of game, in initial phase you can only build roads nears to currently built settlement

  Args:
    - coords - coordinates
    - player-name - name of player
    - state - state of the game"
  [coords player-name state]
  (let [player (first (filter #(= (:name %) player-name) (:players @state))) ;get player by provided name
        last-settlement (last (:settlement player))]        ;last added to :settlement vector
    (some #(= % last-settlement) coords)))                  ;true if coords already in settlement vector

;not tested
(defn build-settlement
  "Build settlement on clicked spot if meet condition,
  fill color of spot in color of player

  Args:
    - coords - clicked coords
    - state - state of the game"
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state))) ; get map of player on turn
        player-idx (dec (:player-turn @state))              ;index of player which is on turn
        game-phase (= "Game" (:phase @state))]              ;condition, does it in game phase
    (when (or (not game-phase)
              (coords-in-roads? coords (:name player) state)) ;do only if is not in game phase or if coords in roads
      (swap! state update-in [:players player-idx :hand] buy-settlement) ;delete all cards from hand for buying settlement
      (swap! state update :spots                            ;update :spots from state
             (fn [spots]                                    ;do it for all spots
               (mapv (fn [spot]                             ;return vector of (function element)
                       (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords) ;if pressed coordinates is coords
                         (assoc spot :fill (:color player)) ;fill spots with color of player (visualization of settlement)
                         spot))
                     spots)))
      (swap! state update :players                          ;update players
             (fn [players]                                  ;for each player
               (into []                                     ;take into vector
                     (map (fn [v]
                            (if (= (:name v) (:name player)) ; if name of player in args is same as player from players map
                              (update v :settlement (fnil conj []) coords) ;even if vector is nil add coords to settlement of player
                              v))
                          players))))
      (swap! state assoc :game-massage "Successful build settlement" ))))

(defn build-town
  "Build town for player, change size of clicked spot and present view of town, double bigger circle

   Args:
      - coords - clicked coords
      - state - state of the game"
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state))) ;map of player on turn
        player-idx (dec (:player-turn @state))]             ;index of player on turn

    (if (some #(= coords %) (:settlement player))           ;if coords in settlement only do it, because if settlement is not built, not possible to upgrade settlement to town
      (do
          (swap! state update-in [:players player-idx :hand] buy-town) ;remove from hands 3 ores and 2 grain
          (swap! state update-in [:players player-idx :settlement] (fn [settlement] (remove #(= % coords) settlement))) ; remove coords from player settlement
        (swap! state update :players
               (fn [players]
                 (into []
                       (map (fn [v]
                              (if (= (:name v) (:name player))
                                (update v :towns (fnil conj []) coords)
                                v))
                            players))))                     ;add to coords to player :town vector

        (swap! state update :spots
               (fn [spots]
                 (mapv (fn [spot]
                         (if (= (:spot-coordinates (:on-mouse-clicked spot)) coords)
                           (assoc spot :radius 20)          ;change radius of circle and present them on board as town
                           spot))
                       spots))))
      )
    ))
(defn build-road
  "Build road for player, change color of clicked road and present view of road of player

 Args:
    - coords - clicked coords
    - state - state of the game"
  [coords state]
  (let [player (get (vec (:players @state)) (dec (:player-turn @state)))
        player-idx (dec (:player-turn @state))
        game-phase (= "Game" (:phase @state))]

    (when (or (and game-phase
                   (coords-in-settlement-or-roads? coords (:name player) state))
              ; 1 of 2 condition have to be meet, or that is game phase and that coords is not in roads , or that is not game phase and coords are is in last settlement.
              ; build in game phase if clicked road are connected with already existing settlement of player
              (and (not game-phase)
                   (coords-in-last-settlement? coords (:name player) state))) ;built in initial phase only if roads start or end by last made settlement (rules of game
      (when game-phase
          (swap! state update-in [:players player-idx :hand] buy-road)) ; if game phase remove from hand brick and wood, if not, not have to be deleted, because in initial phase building road is free, so if player

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
(defn update-winner
  "Update winner in state atom, if some player have 10 vp or more state change view and game finish

  Args:
    - state - state of the game
    - view - end view"
  [state view]
  (let [players (:players @state)                           ; collection of players
        winner-player (some (fn [player]
                              (when (>= (:vp player) 10)
                                (:name player)))
                            players)]                       ;define winner-player as first player who have 10 vp or more from players
    (if winner-player                                       ;if winner-player is not nil
      (swap! state assoc                                    ;change :winner in state with name of player who first reach 10 vps
             :winner winner-player
             :fx/type view)                                 ;swap view to view args (in game end view)
      @state)))
(defn take-resources
  "function which from board when you pass coordinates of one spots and number and extract info of resources
  connected with that spot

  Args:
    - coords - coordinates of center of area
    - number - number of area
    - state - state of the game "
  [coords number state]
  (map :resource                                            ;take resources for all areas which represented by a given number
       (filter (fn [area]
                 (some (fn [spot] (= coords spot)) (:spots area))) ; return spot from areas extracted which is same as args coords
               (filter #(= (str number) (:number %)) (:areas @state)) ; return all areas which have :numbers same as arg number
               )))
(defn filing-hand-with-resource [coordinates number state]
  "Players who have a settlement or town on the coordinates get the resources.
   Towns give double resources. Filling hand with returned resources

   Args:
    - coordinates - coordinates
    - number - number from dice
    - state - state of the game"
  [coordinates number state]
  (swap! state update :players                              ;update players map from state
         (fn [players]
           (mapv (fn [player]                               ;map all functions and return vector
                   (cond (some #{coordinates} (:towns player)) ;if coordinates in :towns of player
                     (update player :hand into (vec (concat (take-resources coordinates number state)
                                                            (take-resources coordinates number state))))
                         ;update hand of player  who has in :towns have coordinates passed in functions, return hands which have additional got resources two times
                     (some #{coordinates} (:settlement player))
                     (update player :hand into (vec (take-resources coordinates number state))) ;update hand of player who in :settlement have coords passed in function, return hand which have one additional resource
                     :else player))                         ; if noting not fulfilled of condition, return player (no updates)
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
(defn take-development-card
  "Take development card from development deck

  Args : state - state of the game"
  [state]
  (let [player-idx (dec (:player-turn @state))              ;index of player (if on turn is 3 ( that is means that player-idx is 2) because started with 0 (not 1)
        chosen (rand-nth (:development-deck @state))]       ;random card from :development deck of state
    (swap! state update :development-deck #(remove-card chosen %)) ;remove returned card from development deck of state
    (swap! state update-in [:players player-idx :dev-cards] #(conj % chosen)) ;add development card to player who is on turn, who bought card
    (swap! state update-in [:players player-idx :hand] buy-development-card) ;removed cards from hand (ore, wool, grain)
    ))
(defn add-edge
  "Downloaded from [7]
  Make edges in graph"
  [graph node neighbor]
  (update graph node (fnil conj []) neighbor))
(defn build-graph
  "Downloaded from [7]
  Args: edges - all edges connections of 2 spots"
  [edges]
  (reduce (fn [g [a b]]
            (-> g
                (add-edge a b)
                (add-edge b a)))
          {}
          edges))
(defn find-all-paths
  "Downloaded from [7]

  Args:
    - graph: a map representing the graph, where keys are nodes and values are collections (e.g., vectors) of neighboring nodes.
    - start: the node from which to start the path search.
    - end: the target node where paths should end.
    - path (optional): a list of nodes representing the current path (used internally during recursion)."

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
  "find the longest path of provided paths

  Args: path - all collection of paths"

  [paths]
  (reduce                                                   ;pass through all path
    (fn [longest path]                                      ;take first element as longest, and go on next
      (if (> (count path) (count longest))                  ;if number of elements in next path is bigger then longest
        path                                                ;return path
        longest))                                           ;otherwise return longest
    []
    paths))
(defn longest-route-length
  "Based on DFS algorithm and graphs teory [7]

  Args: roads - number of roads"
  [roads]
  (let [graph (build-graph roads)                           ;build graph of roads
        nodes (keys graph)                                  ;take nodes as keys of graph
        all-paths (for [start nodes
                        end nodes
                        :when (not= start end)]
                    (find-all-paths graph start end))       ;find all parts
        flat-paths (apply concat all-paths)                 ;merge all path into one vec
        longest (longest-path flat-paths)]                  ;longest route
    (max 0 (dec (count longest)))))                         ;return longest ( count of elements of routes)
(defn update-players-vp
  "Calculate all victory points for all players

  Args: state - state of the game"
  [state]
  (let [winner-longest-route (:winner-longest-route @state) ;take winner-longest-route from state, name of winner
        winner-army-size (:winner-army-size @state)]        ;take winner-army-size route from state, name of winner
    (swap! state update :players                            ;update players
           (fn [players]
             (vec
               (map-indexed
                 (fn [idx player]
                   (let [count-settlement (count (:settlement player)) ;count of settlement (that is return 1 vp per settlement, number  of settlement = vp
                         count-towns (* 2 (count (:towns player))) ;all towns return 2 vp, count number of elements in :town and multiply by 2
                         vp-cards (count (filter #(= % "victory-point") (:dev-cards player))) ; count all "victory-point" development cards from dev-cards of player, count number of cards drawn from the development-deck
                         longest-route (if (= (:name player) winner-longest-route) 2 0) ;give 2 vp to player who has the longest route, if name of player is same as name of winner-longest route return 2 vp, if not 0
                         biggest-army (if (= (:name player) winner-army-size) 2 0) ;give 2 vp to player who has the biggest army, if name of player is same as name of winner-longest route return 2 vp, if not 0
                         total-vp (+ count-settlement count-towns vp-cards longest-route biggest-army)] ; total number is sum of above
                     (assoc player :vp total-vp)))          ;add :vp to all players depends on state of them
                 players))))))
(defn update-current-player-road-length!
  "Update lenght of players

  Args: state - state of the game "
  [state]
  (let [player-idx (dec (:player-turn @state))              ; take index of player on turn
        path [:players player-idx :road-length]]            ; path to road-length of player on turn
    (swap! state assoc-in path                              ; change road length of player
           (longest-route-length (get-in @state [:players player-idx :roads]))) ; depends on all roads of player on turn
    )
  )
(defn player-with-longest-route
  "Calculate player with the longest route, if some already there with the longest route,
  player who have 1 or more longer than route of him, could take in advantage and be a winner

  Args: state - state of the game"
  [state]
  (let [players (:players @state)                           ; take all players
        current-winner-name (:winner-longest-route @state)  ; return name of winner with the longest route
        current-winner (some #(when (= (:name %) current-winner-name) %) players) ;get map of current winner
        current-winner-length (or (:road-length current-winner) 0) ;calculate length of route of current winner
        max-player (apply max-key #(or (:road-length %) 0) players) ;return player with the longest route
        max-length (or (:road-length max-player) 0)]        ;calculate route of player with the longest route
    (when (>= max-length 3)                                 ; if length 3 or bigger
      (when (> max-length current-winner-length)            ;if max-length longer than length of winner (not if equal)
        (swap! state assoc :winner-longest-route (:name max-player)) ;winner of longest route is player who have 1 or more bigger route that current winner
        ))))
(defn player-with-largest-army
  "Calculate player with the biggest army, if some already there with the biggest army,
  player who have 1 or more bigger army than army of him, could take in advantage and be a winner of army.
  Army is number of activated knights

  Args: state - state of the game"
  [state]
  (let [players (:players @state)                           ; take all players
        current-winner-name (:winner-army-size @state)      ; return name of winner with the biggest army
        current-winner (some #(when (= (:name %) current-winner-name) %) players) ; get map of current winner
        current-winner-size (or (:army-size current-winner) 0) ;calculate length of route of current winner
        max-player (apply max-key #(or (:army-size %) 0) players);;return player with the longest route
        max-size (or (:army-size max-player) 0)]            ;calculate route of player with the longest route
    (when (>= max-size 3)                                   ; if length 3 or bigger
      (when (> max-size current-winner-size)                ;if max-length longer than length of winner (not if equal)
        (swap! state assoc :winner-army-size (:name max-player)))))) ;winner of longest route is player who have 1 or more bigger route that current winner
(defn update-all-hands-second-half
  " This function divides in half and removes half of the total number of resources, the cards are shuffled to remove them randomly

  Args: state - state of the game"
  [state]
  (swap! state
         (fn [state]
           (let [players (:players state)]                  ; return list of players
             (let [updated-players                          ; return new players
                   (mapv (fn [player-map]                   ; make a vector of new players (function applied to all in collection of players)
                           (let [hand (:hand player-map)    ; hand of player
                                 new-hand (if (> (count hand) 7) ;if you have more than 7 cards
                                            (let [shuffled (shuffle hand) ;shuffle hand
                                                  half (quot (count shuffled) 2)] ;half is first half
                                              (drop half shuffled)) ;keep the rest
                                            hand)]          ;otherwise if is not more than 7 resource, do nothing
                             (assoc player-map :hand new-hand))) ;change hand for all players who have more than 7
                         players)]
               (assoc state :players updated-players))))))  ;update state with new players