(ns catan-predictor.shop)


(defn buy-development-card [])

(defn exchange-cards
  [hand sell-card-type buy-card-type]
  "Select type which want to buy and which want to sell"
  (swap! hand update :cards
         ()))

(defn remove-card
  [cards-type hand]
  "Select type of cards and delete from hand"
  (remove #(= % cards-type) hand)
  )

(defn remove-n-cards
  [type-card hand n]
  (loop [hand hand
         n n]
    (if (< 0 n)
      (recur (remove-card type-card hand) (dec n))
      hand)))


(remove-card  "a" v-of-cards)


(def v-of-cards ["a" "a" "a" "a" "a" "b" "c"])
(remove-n-cards "a" v-of-cards 2)


(def n 3)
(while (< n 0)(remove #(= % "a") v-of-cards )(dec n))

(defn build-settlement [])

(defn build-town [])

(defn build-road [])

