(ns acme.frontend.tetris.points-test
  (:require [cljs.test :refer [deftest is testing]]
            [acme.frontend.tetris.points :as points]))

(deftest move-test
  (testing "translates all points by offset"
    (is (= [[3 4] [4 4]] (vec (points/move [[1 2] [2 2]] [2 2]))))
    (is (= [[0 0]] (vec (points/move [[5 5]] [-5 -5]))))))

(deftest add-color-test
  (testing "attaches color to each point"
    (is (= [[[1 2] "red"] [[3 4] "red"]]
           (vec (points/add-color [[1 2] [3 4]] "red"))))))

(deftest rotate-test
  (testing "rotates all points"
    (is (= [[3 1] [3 2]] (vec (points/rotate [[1 2] [2 2]] 90))))))

(deftest valid-test
  (let [junkyard {[5 19] "red"}]
    (testing "returns true when all points are valid"
      (is (true? (points/valid? [[[0 0] "blue"] [[1 1] "blue"]] {})))
      (is (true? (points/valid? [[[5 18] "red"]] junkyard))))

    (testing "returns false when any point is out of bounds"
      (is (false? (points/valid? [[[0 0] "blue"] [[-1 0] "blue"]] {})))
      (is (false? (points/valid? [[[5 20] "red"]] {}))))

    (testing "returns false when any point collides with junkyard"
      (is (false? (points/valid? [[[5 19] "cyan"]] junkyard)))
      (is (false? (points/valid? [[[0 0] "blue"] [[5 19] "blue"]] junkyard))))))
