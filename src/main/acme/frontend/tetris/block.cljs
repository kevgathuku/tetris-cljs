(ns acme.frontend.tetris.block
  (:require [acme.frontend.tetris.point :as point]))

(def shapes [:i :t :o :l :j :z :s])

(defn create [{:keys [rotation location]
               :or {rotation 0
                    location {:x 4 :y 1}}}]
  {:shape (rand-nth shapes)
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

(defn points [tetro]
  (vec (vals (:location tetro))))

(comment
  (def x (create {:location {:x 1 :y 1}}))
  (move-right x)
  (move-down x)
  (move-left x)
  (defn- rotate-degrees
    [degrees]
    (if (= degrees 270) 0 (+ degrees 90)))

  (rotate-degrees 180)
  (rotate-degrees 270)
  (rotate x)
  (defn- rotate-old [tetro]
    (update tetro :rotation rotate-degrees))
  (rotate-old x)
  (take 5 (iterate rotate x))
  (repeatedly 10 #(rand-nth shapes)))
