(ns acme.frontend.tetris.block
  (:require [acme.frontend.tetris.point :as point] [acme.frontend.tetris.points :as points]))

(def shapes
  {:t [[1 0] [2 0] [3 0] [2 1]]
   :o [[2 0] [3 0] [2 1] [3 1]]
   :l [[2 0] [2 1] [2 2] [3 2]]
   :i [[2 0] [2 1] [2 2] [2 3]]
   :s [[2 0] [3 0] [1 1] [2 1]]
   :z [[1 0] [2 0] [2 1] [3 1]]
   :j [[3 0] [3 1] [2 2] [3 2]]})

(defn create [{:keys [rotation location]
               :or {rotation 0
                    location [2 0]}}]
  {:shape (rand-nth (keys shapes))
   :rotation rotation
   :location location})

(defn move-right [tetro]
  (update tetro :location point/right))

(defn move-left [tetro]
  (update tetro :location point/left))

(defn move-down [tetro]
  (update tetro :location point/down))

(defn rotate [tetro]
  (update tetro :rotation #(mod (+ % 90) 360)))

(defn points [{:keys [shape]}]
  (get shapes shape :unknown))

(defn show [{:keys [location] :as tetro}]
  (-> tetro (points) (points/move location) (vec)))

(comment
  (def x (create {:location [1 1]}))
  (prn x)
  (move-right x)
  (move-down (create {:location [1 2]}))
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
