(ns acme.frontend.tetris.point)

(defn origin []
  [0 0])

(defn translate [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn transpose [[x y]] [y x])

(defn mirror [[x y]] [(- 5 x) y])

(defn flip [[x y]] [x (- 5 y)])

(defn rotate [point degrees]
  (cond
    (= 0 degrees)  point
    (= 90 degrees)  (-> point (flip) (transpose))
    (= 180 degrees) (-> point (mirror) (flip))
    (= 270 degrees) (-> point (mirror) (transpose))
    :else                 :unknown))

(defn add-color [point color]
  [point color])

(defn left [point] (translate point [-1 0]))
(defn down [point] (translate point [0 1]))
(defn right [point] (translate point [1 0]))

(comment
  (left [8 0])
  (- 5 2)
  (-> (origin) (down) (right) (down))
  (down [8 0]))
