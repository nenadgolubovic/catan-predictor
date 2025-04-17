(ns catan-predictor.shop)




(defn remove-card
  [cards-type hand]
  "Select type of cards and delete one from hand"
  (let [index (some #(when (= (second %) cards-type) (first %))
                    (map-indexed vector hand))]
    (if index
      (into (subvec hand 0 index) (subvec hand (inc index))) ;Take all elements from 0 to index and from index+1 to end
      hand)))

(defn remove-n-cards
  [type-card hand n]
  (loop [hand hand
         n n]
    (if (< 0 n)
      (recur (remove-card type-card hand) (dec n))
      hand)))

(defn add-cards
  [card-type  hand]
  "Add n cards of a given type in the hand"
  (into hand card-type))

(defn exchange-cards
  [hand sell-card-type buy-card-type n]
  "Select type which want to buy and which want to sell"
  (swap! hand update :cards
         #(remove-n-cards sell-card-type % n))
  (swap! hand update :cards
         #(add-cards [buy-card-type] %)))

(def resources #{"wool" "brick" "wood" "ore" "grain"})
(defn buy-settlement
  [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" (remove-n-cards "wool" (remove-n-cards "grain" hand 1) 1) 1) 1))
(defn buy-town
  [hand]
  (remove-n-cards "ore" (remove-n-cards "grain" hand 2) 3))

(defn buy-road
  [hand]
  (remove-n-cards "wood" (remove-n-cards "brick" hand 1) 1))

(defn buy-development-card
  [hand]
  (remove-n-cards "grain" (remove-n-cards "ore" (remove-n-cards "wool" hand 1) 1) 1))




(defn buy-town
  [])
(defn buy-settlement
  [])
(defn buy-road
  [])



