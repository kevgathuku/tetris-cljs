(ns acme.frontend.tetris.block
  (:require [acme.frontend.tetris.point :as point] [acme.frontend.tetris.points :as points]))

(def shapes
  {:t [[1 2] [2 2] [3 2] [2 3]]
   :o [[2 1] [3 1] [2 2] [3 2]]
   :l [[2 1] [2 2] [2 3] [3 3]]
   :i [[2 1] [2 2] [2 3] [2 4]]
   :s [[2 2] [3 2] [1 3] [2 3]]
   :z [[1 2] [2 2] [2 3] [3 3]]
   :j [[3 1] [3 2] [2 3] [3 3]]})

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
