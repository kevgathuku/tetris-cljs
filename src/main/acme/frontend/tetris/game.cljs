(ns acme.frontend.tetris.game
  (:require
   [acme.frontend.tetris.block :as block]
   [acme.frontend.tetris.point :as point]
   [acme.frontend.tetris.points :as points]))

(def wall-kick-offsets
  "Wall kick offsets to try when rotation fails.
  Attempts are made in order: original position, right, left, further right/left, up."
  [[0 0]   ; No kick - try rotation at current position
   [1 0]   ; Kick right
   [-1 0]  ; Kick left
   [2 0]   ; Kick further right
   [-2 0]  ; Kick further left
   [0 -1]  ; Kick up (useful for I-piece and edge cases)
   ])

(defn init []
  {:tetro nil :score 0 :points [] :junkyard {}})

(defn show [game]
  (if-let [tetro (:tetro game)]
    (assoc game :points (block/show tetro))
    (assoc game :points [])))

(defn new-tetro [game]
  (-> game
      (assoc :tetro (block/create {}))
      (show)))

(defn new-game []
  (-> (init)
      (new-tetro)))

(defn inc-score [game value]
  (update game :score + value))

(defn- move-data [game move-fn]
  (let [old (:tetro game)
        new (move-fn old)
        valid (points/valid? (block/show new) (:junkyard game))]
    {:old old :new new :valid valid}))

(defn move [game move-fn]
  (let [{:keys [old new valid]} (move-data game move-fn)
        moved (block/maybe-move old new valid)]
    (if (identical? moved old)
      game
      (-> game
          (assoc :tetro moved)
          (show)))))

(defn- merge-tetro [game old]
  (let [new-junkyard (into (:junkyard game)
                           (block/show old))]
    (assoc game :junkyard new-junkyard)))

(defn- move-down-or-merge [game {:keys [old new valid]}]
  (if valid
    (-> game
        (assoc :tetro new)
        (show))
    (-> game
        (merge-tetro old)
        (inc-score 1)
        (new-tetro))))

(defn left [game]
  (move game block/move-left))

(defn right [game]
  (move game block/move-right))

(defn down [game]
  (move-down-or-merge game (move-data game block/move-down)))

(comment
  ;; move-data returns {:old, :new, :valid}
  (move-data (new-game) block/move-down)

  ;; down moves piece when not at bottom
  (let [g (new-game)]
    {:before (:tetro g)
     :after (:tetro (down g))})

  ;; down merges when at bottom (y=17 puts lowest point at y=19+1=20, invalid)
  (let [g (-> (init)
              (assoc :tetro (block/create {:shape :o :location [2 17]}))
              (show))]
    {:before-junkyard (:junkyard g)
     :after-junkyard (:junkyard (down g))
     :new-tetro (:tetro (down g))})

  ;; verify junkyard accumulates across multiple merges
  (let [g (-> (init)
              (assoc :tetro (block/create {:shape :o :location [2 17]}))
              (show)
              (down)
              (assoc :tetro (block/create {:shape :o :location [5 17]}))
              (show)
              (down))]
    (count (:junkyard g))))

(defn- try-wall-kicks
  "Attempts rotation with wall kicks.
  Tries each offset in wall-kick-offsets until a valid position is found.

  Args:
    old-tetro - Original tetromino before rotation
    rotated-tetro - Tetromino after rotation (but before position adjustment)

  Returns:
    Valid tetromino with wall kick applied, or old-tetro if no kick works"
  [old-tetro rotated-tetro junkyard]
  (let [try-offset (fn [offset]
                     (let [kicked (update rotated-tetro :location #(point/translate % offset))
                           valid? (points/valid? (block/show kicked) junkyard)]
                       (when valid? kicked)))]
    (or (some try-offset wall-kick-offsets)
        old-tetro)))

(defn rotate
  "Rotates the current tetromino 90° clockwise with wall kick support.
  If rotation at current position fails, tries alternative positions (wall kicks)
  by shifting the piece left, right, or up.

  Args:
    game - Game state map with :tetro key

  Returns:
    Updated game state with rotated tetromino, or unchanged if no valid rotation exists"
  [game]
  (let [old (:tetro game)
        rotated (block/rotate old)
        kicked (try-wall-kicks old rotated (:junkyard game))]
    (if (identical? kicked old)
      game
      (-> game
          (assoc :tetro kicked)
          (show)))))

(comment
  ;; Test scenarios for show function

  ;; Scenario 1: Show with nil tetro (initial state)
  (show (init))
  (:score (inc-score (new-game) 14))
  ;; Expected: {:tetro nil, :score 0, :points []}
  ;; Empty points because tetro is nil (now handled gracefully)

  ;; Scenario 1b: Show on already-shown game (idempotent test)
  (show (new-game))
  ;; Expected: Same result as (new-game) - show is idempotent
  ;; new-game already calls show via new-tetro, so this is a second show call

  ;; Scenario 2: Show with a newly created tetro
  (show {:tetro (block/create {:shape :o :location [5 5]})
         :score 0
         :points []})
  ;; Expected: Game with :points containing 4 colored points for O-block
  ;; Points should be in board coordinates (5+x, 5+y) with "magenta" color

  ;; Scenario 3: Show after creating new game
  (new-game)
  ;; Expected: Fresh game with random tetro and points populated
  ;; :tetro should be a block map, :points should have 4 colored points

  ;; Scenario 4: Show after movement
  (-> (new-game)
      (down))
  ;; Expected: Game with tetro moved down one unit, :points updated
  ;; Points y-coordinates should be 1 greater than before

  ;; Scenario 5: Multiple movements with show
  (-> (new-game)
      (down)
      (right)
      (right)
      (down))
  ;; Expected: Tetro at +2 x, +2 y from start, :points updated each time

  ;; Scenario 6: Invalid movement (should preserve old position)
  (-> {:tetro (block/create {:shape :i :location [10 5]})
       :score 0
       :points []}
      (show)
      (right))  ; Try to move right from x=10 (out of bounds)
  ;; Expected: Tetro should stay at [10 5] because move is invalid
  ;; :points should reflect the unmoved position

  ;; Scenario 7: Rotation updates points
  (-> (new-game)
      (rotate))
  ;; Expected: Tetro rotated 90°, :points reflect rotated shape

  ;; Scenario 8: Chain of operations
  (-> (init)
      (new-tetro)
      (down)
      (left)
      (rotate)
      (down))
  ;; Expected: Each operation updates :tetro and :points correctly
  ;; Final :points should match final tetro position and rotation

  ;; Verify show updates points correctly
  (let [g1 (new-game)
        tetro1 (:tetro g1)
        points1 (:points g1)
        g2 (down g1)
        points2 (:points g2)]
    {:tetro-before tetro1
     :points-before points1
     :points-after points2
     :points-match? (= points2 (block/show (:tetro g2)))})
  ;; Expected: :points-match? should be true

  ;; Test that show is idempotent (calling twice doesn't change result)
  (let [g (new-game)]
    (= (show g) (show (show g)))))
