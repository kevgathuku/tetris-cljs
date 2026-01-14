(ns acme.frontend.tetris.point-test
  (:require [cljs.test :refer [deftest is testing]]
            [acme.frontend.tetris.point :as point]))

(deftest in-bounds?-test
  (testing "with plain points [x y]"
    (testing "x < 0 is out of bounds"
      (is (false? (point/in-bounds? [-1 5])))
      (is (false? (point/in-bounds? [-10 0]))))

    (testing "x = 0 is in bounds (lower boundary)"
      (is (true? (point/in-bounds? [0 0])))
      (is (true? (point/in-bounds? [0 19]))))

    (testing "x between 0 and 9 is in bounds"
      (is (true? (point/in-bounds? [1 5])))
      (is (true? (point/in-bounds? [5 0])))
      (is (true? (point/in-bounds? [8 19]))))

    (testing "x = 9 is in bounds (upper boundary)"
      (is (true? (point/in-bounds? [9 0])))
      (is (true? (point/in-bounds? [9 19]))))

    (testing "x > 9 is out of bounds"
      (is (false? (point/in-bounds? [10 5])))
      (is (false? (point/in-bounds? [20 0])))
      (is (false? (point/in-bounds? [100 15])))))

  (testing "with colored points [[x y] color]"
    (testing "x < 0 is out of bounds"
      (is (false? (point/in-bounds? [[-1 5] "blue"])))
      (is (false? (point/in-bounds? [[-10 0] "green"]))))

    (testing "x = 0 is in bounds (lower boundary)"
      (is (true? (point/in-bounds? [[0 0] "red"])))
      (is (true? (point/in-bounds? [[0 19] "blue"]))))

    (testing "x between 0 and 9 is in bounds"
      (is (true? (point/in-bounds? [[1 5] "red"])))
      (is (true? (point/in-bounds? [[5 0] "coral"])))
      (is (true? (point/in-bounds? [[8 19] "magenta"]))))

    (testing "x = 9 is in bounds (upper boundary)"
      (is (true? (point/in-bounds? [[9 0] "yellow"])))
      (is (true? (point/in-bounds? [[9 19] "turquoise"]))))

    (testing "x > 9 is out of bounds"
      (is (false? (point/in-bounds? [[10 5] "red"])))
      (is (false? (point/in-bounds? [[20 0] "silver"])))
      (is (false? (point/in-bounds? [[100 15] "limegreen"]))))))
