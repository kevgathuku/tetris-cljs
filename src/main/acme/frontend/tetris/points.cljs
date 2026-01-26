(ns acme.frontend.tetris.points
  (:require
   [acme.frontend.tetris.point :as point]))

(defn move
  "Translates a collection of points by a given offset.

  Args:
    points - Collection of point vectors [[x y] [x y] ...]
    change - Vector [dx dy] representing the translation offset

  Returns:
    Lazy sequence of translated points"
  [points change]
  (map #(point/translate % change) points))

(defn add-color
  "Attaches a color to each point in a collection.

  Args:
    points - Collection of point vectors [[x y] [x y] ...]
    color - String color value to attach to each point

  Returns:
    Lazy sequence of point-color tuples [[[x y] color] ...]"
  [points color]
  (map #(point/add-color % color) points))

(defn rotate
  "Rotates all points in a collection by the specified degrees.

  Args:
    points - Collection of point vectors [[x y] [x y] ...]
    degrees - Rotation angle (0, 90, 180, or 270)

  Returns:
    Lazy sequence of rotated points"
  [points degrees]
  (map #(point/rotate % degrees) points))

(defn valid? [points junkyard]
  (every? #(point/valid? % junkyard) points))

(comment
  (add-color [[1 2]] "red")
  ; (every? #(point/valid?) '(1 2))
  (rotate [[0 1] [1 1]] 90)
  (move [[0 1] [1 1]] [2 2]))
