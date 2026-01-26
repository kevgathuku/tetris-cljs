(ns acme.frontend.tetris.block
  (:require [acme.frontend.tetris.point :as point] [acme.frontend.tetris.points :as points]))

(def shapes
  {:t [[1 2] [2 2] [3 2] [2 3]]
   :o [[2 2] [3 2] [2 3] [3 3]]
   :l [[2 2] [2 3] [2 4] [3 4]]
   :i [[2 1] [2 2] [2 3] [2 4]]
   :s [[2 2] [3 2] [1 3] [2 3]]
   :z [[1 2] [2 2] [2 3] [3 3]]
   :j [[3 2] [3 3] [2 4] [3 4]]})

(def colors
  {:t "coral"
   :o "magenta"
   :l "red"
   :i "silver"
   :s "limegreen"
   :z "yellow"
   :j "turquoise"})

(defn create
  "Creates a new tetromino block.

  Args:
    opts - Optional map with keys:
      :shape - Keyword for shape type (:t :o :l :i :s :z :j), defaults to random
      :rotation - Initial rotation in degrees (0, 90, 180, 270), defaults to 0
      :location - Starting position vector [x y], defaults to [2 -2]

  Returns:
    Map with keys :shape, :rotation, :location

  Note: Default location [2 -2] is calibrated to spawn blocks at top of board
        for shapes centered around y=2.5. See CLAUDE.md rotation section."
  [{:keys [rotation location shape]
    :or {rotation 0
         shape (rand-nth (keys shapes))
         location [2 -2]}}]
  {:shape shape
   :rotation rotation
   :location location})

(defn move-right
  "Moves a tetromino one unit to the right.

  Args:
    tetro - Tetromino map with :location key

  Returns:
    Updated tetromino map"
  [tetro]
  (update tetro :location point/right))

(defn move-left
  "Moves a tetromino one unit to the left.

  Args:
    tetro - Tetromino map with :location key

  Returns:
    Updated tetromino map"
  [tetro]
  (update tetro :location point/left))

(defn move-down
  "Moves a tetromino one unit down.

  Args:
    tetro - Tetromino map with :location key

  Returns:
    Updated tetromino map"
  [tetro]
  (update tetro :location point/down))

(defn rotate
  "Rotates a tetromino 90 degrees clockwise.
  Rotation wraps at 360 degrees back to 0.

  Args:
    tetro - Tetromino map with :rotation key

  Returns:
    Updated tetromino map with new rotation value"
  [tetro]
  (update tetro :rotation #(mod (+ % 90) 360)))

(defn maybe-move
  "Returns new tetromino if valid, otherwise returns old tetromino.
  Used for conditional movement validation.

  Args:
    old - Original tetromino before movement
    new - New tetromino after attempted movement
    valid? - Boolean indicating if the move is valid

  Returns:
    new if valid? is true, old if valid? is false"
  [old new valid?]
  (if valid? new old))

(defn points
  "Gets the shape definition points for a tetromino.

  Args:
    tetro - Tetromino map with :shape key

  Returns:
    Vector of point coordinates [[x y] [x y] ...] in shape-local space (1-4 grid)"
  [{:keys [shape]}]
  (get shapes shape :unknown))

(defn color
  "Gets the color for a tetromino based on its shape.

  Args:
    tetro - Tetromino map with :shape key

  Returns:
    String color value"
  [{:keys [shape]}]
  (get colors shape :unknown))

(defn show
  "Converts a tetromino to absolute board coordinates with colors for rendering.

  Transforms the tetromino through the following pipeline:
  1. Get shape-local coordinates from shape definition
  2. Apply rotation transformation
  3. Translate to board position by adding location vector
  4. Attach color to each point

  Args:
    tetro - Tetromino map with :shape, :rotation, :location keys

  Returns:
    Map of {[x y] color} in board space (0-indexed grid)"
  [{:keys [location rotation] :as tetro}]
  (-> tetro (points) (points/rotate rotation) (points/move location) (points/add-color (color tetro)) (into {})))

(comment
  (def x (create {:location [1 1]}))
  (prn x)
  (move-right x)
  (move-down (create {:location [1 2]}))
  (color (create {}))
  (color (create {:shape :o}))
  (show (create {:shape :o}))
  (points/rotate (points  (create {:shape :o})) 90)
  (move-left x)
  (points (create {}))
  (points/move (points x) [2 3])
  (defn- rotate-degrees
    [degrees]
    (if (= degrees 270) 0 (+ degrees 90)))

  (rotate-degrees 180)
  (rotate-degrees 270)
  (vec (vals  (:location (create {}))))
  (rotate x)
  (get-in {:owner "Jacky" :a [1 2]} [:a  0])
  (defn- rotate-old [tetro]
    (update tetro :rotation rotate-degrees))
  (rotate-old x)
  (take 5 (iterate rotate x))
  (repeatedly 10 #(rand-nth shapes)))
