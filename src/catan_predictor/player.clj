(ns catan-predictor.player)

(def player-1 (atom {:settlement []
                     :towns []
                     :roads []
                     :hand []
                     :dev-cards []
                     :color [255 165 0]
                     :victory-points nil
                     :road-length nil
                     :knight-length nil
                     :longest-route false
                     :biggest-army false}))

(defn upgrade-settlement
  [player spot]
  "Define spot as settlement"
  (swap! player update :settlement
         #(conj % spot)))

(defn upgrade-town
  [player spot]
  "Upgrade settlement to town"
  (let [spots (:settlement @player)]
    (if (some #(= % spot) spots)
      (do
        (swap! player update :settlement
               #(remove (fn [x] (= spot x)) %))
        (swap! player update :towns
               #(conj % spot)))
      nil)))

(defn calculate-vp
  "calculate victory points"
  [player]
  (let [count-settlement (count (:settlement @player))
        count-towns  (* 2 (count (:towns @player)))
        vp (count (filter #(= % "victory-point") (:dev-cards @player) ))
        longest-route (if (:longest-route @player) 2 0)
        biggest-army (if (:biggest-army @player) 2 0)]
    (+ count-settlement count-towns vp longest-route biggest-army)))
