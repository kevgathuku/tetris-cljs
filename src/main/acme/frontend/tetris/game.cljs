(ns acme.frontend.tetris.game
  (:require
   [acme.frontend.tetris.block :as block]
   [acme.frontend.tetris.points :as points]))

(defn init []
  {:tetro nil :score 0 :points []})

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

(defn move [game move-fn]
  (let [old (:tetro game)
        new (move-fn (:tetro game))
        valid (points/valid? (block/show new))
        moved (block/maybe-move old new valid)]
    (if (identical? moved old)
      game  ; No change, return game as-is to avoid unnecessary updates
      (-> game
          (assoc :tetro moved)
          (show)))))

(defn down [game]
  (move game block/move-down))

(defn left [game]
  (move game block/move-left))

(defn right [game]
  (move game block/move-right))

(defn rotate [game]
  (move game block/rotate))

(comment
  ;; Test scenarios for show function

  ;; Scenario 1: Show with nil tetro (initial state)
  (show (init))
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
