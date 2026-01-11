(ns acme.frontend.tetris.points
  (:require
   [acme.frontend.tetris.point :as point]))

(defn move [points change]
  (map #(point/translate % change) points))

(defn add-color [points color]
  (map #(point/add-color % color) points))

(defn rotate [points degrees]
  (map #(point/rotate % degrees) points))

(comment
  (add-color [[1 2]] "red")
  (rotate [[0 1] [1 1]] 90)
  (move [[0 1] [1 1]] [2 2]))
