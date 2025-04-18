(ns catan-predictor.visualization
  (:require [quil.core :as q]))



(defn visualization-board
  [roads points centers]
  "Visualization of roads points and centers using quil library"
    ; Set background color to black
 ; Draw points as circles

    ;; Create sketch with specific title, size, setup, and draw functions
    (q/defsketch scatter-plot
                 :title "CATAN - Visualization"
                 :size [1500 1000]
                 :setup (fn [] (q/frame-rate 30)  ; Refresh rate
                          (q/background 0))
                 :draw(fn []
                      ;; Draw roads
                      (doseq [road roads]
                        (let [scaled-x1 (+ 750 (* 50 (first (first road))))
                              scaled-y1 (+ 500 (* 50 (second (first road))))
                              scaled-x2 (+ 750 (* 50 (first (second road))))
                              scaled-y2 (+ 500 (* 50 (second (second road))))]

                          (q/stroke 0 0 255)  ; Blue color for roads
                          (q/stroke-weight 3)
                          ;; Draw the road (line between two points)
                          (q/line scaled-x1 (- (q/height) scaled-y1)
                                  scaled-x2 (- (q/height) scaled-y2))))

                      ;; Draw points (nodes)
                      (doseq [[x y] (concat points centers)]
                        (let [scaled-x (+ 750 (* x 50))
                              scaled-y (+ 500 (* y 50))]

                          (q/no-stroke)
                          ;; Check if the point is a center (red)
                          (if (some #(= [x y] %) centers)
                            (q/fill 255 0 0)  ; Red for centers
                            (q/fill 255))     ; White for other points

                          ;; Draw the point as a circle (ellipse)
                          (q/ellipse scaled-x (- (q/height) scaled-y) 10 10))))))
