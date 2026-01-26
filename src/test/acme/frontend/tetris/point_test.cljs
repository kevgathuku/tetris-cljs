(ns acme.frontend.tetris.point-test
  (:require [cljs.test :refer [deftest is testing]]
            [acme.frontend.tetris.point :as point]))

(deftest coords-test
  (testing "extracts coordinates from plain point"
    (is (= [4 5] (point/coords [4 5])))
    (is (= [0 0] (point/coords [0 0]))))

  (testing "extracts coordinates from colored point"
    (is (= [4 5] (point/coords [[4 5] "red"])))
    (is (= [0 19] (point/coords [[0 19] "blue"])))))

(deftest in-bounds-test
  (testing "x boundaries with plain points"
    (testing "x < 0 is out of bounds"
      (is (false? (point/in-bounds? [-1 5]))))

    (testing "x = 0 is in bounds"
      (is (true? (point/in-bounds? [0 0]))))

    (testing "x = 9 is in bounds"
      (is (true? (point/in-bounds? [9 0]))))

    (testing "x > 9 is out of bounds"
      (is (false? (point/in-bounds? [10 5])))))

  (testing "y boundaries with plain points"
    (testing "y < 0 is in bounds (pieces start above board)"
      (is (true? (point/in-bounds? [5 -1])))
      (is (true? (point/in-bounds? [5 -10]))))

    (testing "y = 0 is in bounds"
      (is (true? (point/in-bounds? [5 0]))))

    (testing "y = 19 is in bounds (bottom row)"
      (is (true? (point/in-bounds? [5 19]))))

    (testing "y > 19 is out of bounds"
      (is (false? (point/in-bounds? [5 20])))
      (is (false? (point/in-bounds? [5 100])))))

  (testing "with colored points [[x y] color]"
    (is (true? (point/in-bounds? [[5 10] "red"])))
    (is (false? (point/in-bounds? [[-1 5] "blue"])))
    (is (false? (point/in-bounds? [[5 20] "green"])))))

(deftest collide-test
  (let [junkyard {[5 19] "red" [6 19] "red" [7 19] "blue"}]
    (testing "returns true when point collides with junkyard"
      (is (true? (point/collide? [5 19] junkyard)))
      (is (true? (point/collide? [[6 19] "cyan"] junkyard))))

    (testing "returns false when point does not collide"
      (is (false? (point/collide? [5 18] junkyard)))
      (is (false? (point/collide? [[0 0] "red"] junkyard))))

    (testing "returns false with empty junkyard"
      (is (false? (point/collide? [5 19] {}))))))

(deftest valid-test
  (let [junkyard {[5 19] "red"}]
    (testing "returns true when in bounds and no collision"
      (is (true? (point/valid? [5 10] junkyard)))
      (is (true? (point/valid? [[0 0] "blue"] {}))))

    (testing "returns false when out of bounds"
      (is (false? (point/valid? [-1 5] {})))
      (is (false? (point/valid? [5 20] {}))))

    (testing "returns false when colliding with junkyard"
      (is (false? (point/valid? [5 19] junkyard)))
      (is (false? (point/valid? [[5 19] "cyan"] junkyard))))

    (testing "returns false when both out of bounds and colliding"
      (is (false? (point/valid? [10 19] junkyard))))))
