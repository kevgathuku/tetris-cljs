(ns acme.frontend.tetris.point)

(defn origin []
  [0 0])

(defn translate [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn add-color [point color]
  [point color])

(defn left [point] (translate point [-1 0]))
(defn down [point] (translate point [0 1]))
(defn right [point] (translate point [1 0]))

(comment
  (left [8 0])
  (-> (origin) (down) (right) (down))
  (down [8 0]))
