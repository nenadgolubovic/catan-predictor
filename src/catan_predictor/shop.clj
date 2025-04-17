(ns catan-predictor.shop)


(defn buy-development-card [])

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

(add-cards [ "a" "a" "b"] ["c"] )

(defn exchange-cards
  [hand sell-card-type buy-card-type n]
  "Select type which want to buy and which want to sell"
  (swap! hand update :cards
         #(remove-n-cards sell-card-type % n))
  (swap! hand update :cards
         #(add-cards [buy-card-type] %)))


(defn build-settlement [])

(defn build-town [])

(defn build-road [])

