(ns catan-predictor.shop)




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





