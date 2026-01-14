(ns acme.frontend.tetris.point)

(defn origin
  "Returns the origin point at coordinates [0 0]."
  []
  [0 0])

(defn translate
  "Translates a point by adding delta values.

  Args:
    point - Vector [x y] representing a coordinate
    delta - Vector [dx dy] representing the translation offset

  Returns:
    New point [x+dx y+dy]"
  [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])

(defn transpose
  "Swaps x and y coordinates of a point.
  Used as part of rotation transformations.

  Args:
    point - Vector [x y]

  Returns:
    Vector [y x]"
  [[x y]] [y x])

(defn mirror
  "Mirrors a point horizontally around center x=2.5.
  Part of the rotation transformation centered at (2.5, 2.5).

  Args:
    point - Vector [x y]

  Returns:
    Vector [(- 5 x) y]"
  [[x y]] [(- 5 x) y])

(defn flip
  "Flips a point vertically around center y=2.5.
  Part of the rotation transformation centered at (2.5, 2.5).

  Args:
    point - Vector [x y]

  Returns:
    Vector [x (- 5 y)]"
  [[x y]] [x (- 5 y)])

(defn rotate
  "Rotates a point around center (2.5, 2.5) by the specified degrees.

  Args:
    point - Vector [x y] to rotate
    degrees - Rotation angle (0, 90, 180, or 270)

  Returns:
    Rotated point vector [x y]

  Note: Rotation center is tied to shape coordinate system.
        See CLAUDE.md for relationship between rotation center,
        shape coordinates, and starting location."
  [point degrees]
  (cond
    (= 0 degrees)  point
    (= 90 degrees)  (-> point (flip) (transpose))
    (= 180 degrees) (-> point (mirror) (flip))
    (= 270 degrees) (-> point (mirror) (transpose))
    :else                 :unknown))

(defn add-color
  "Attaches a color to a point, creating a point-color tuple.

  Args:
    point - Vector [x y]
    color - String color value (e.g., 'red', '#FF0000')

  Returns:
    Vector [[x y] color]"
  [point color]
  [point color])

(defn left
  "Moves a point one unit left (decreases x by 1)."
  [point] (translate point [-1 0]))

(defn down
  "Moves a point one unit down (increases y by 1)."
  [point] (translate point [0 1]))

(defn right
  "Moves a point one unit right (increases x by 1)."
  [point] (translate point [1 0]))

(defn in-bounds?
  "Checks if a point is within valid board x-coordinate bounds (0-9).
  Handles both plain points [x y] and colored points [[x y] color].

  Args:
    point - Either [x y] or [[x y] color] in board coordinates

  Returns:
    true if 0 <= x <= 9, false otherwise"
  [point]
  (let [[x _] (if (vector? (first point))
                (first point)  ; Colored point [[x y] color] -> extract [x y]
                point)]        ; Plain point [x y]
    (and (>= x 0) (<= x 9))))

(comment
  (left [8 0])
  (- 5 2)
  (-> (origin) (down) (right) (down))
  (down [8 0]))
