(ns acme.frontend.tetris.point)

(defn origin []
  {:x 0 :y 0})

(defn translate [point dx dy]
  (-> point
      (update :x + dx)
      (update :y + dy)))

(defn left [point] (translate point -1 0))
(defn down [point] (translate point 0 1))
(defn right [point] (translate point 1 0))

(comment
  (:x (origin))
  (left {:x 8 :y 0})
  (-> (origin) (down) (right) (down))
  (down  {:x 8 :y 2}))
