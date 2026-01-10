(ns acme.frontend.tetris.points
  (:require
   [acme.frontend.tetris.point :as point]))

(defn move [points change]
  (map #(point/translate % change) points))

(comment
  (move [[0 1] [1 1]] [2 2]))
