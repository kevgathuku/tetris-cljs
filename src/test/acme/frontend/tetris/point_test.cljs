(ns acme.frontend.tetris.point-test
  (:require [cljs.test :refer [deftest is testing]]
            [acme.frontend.tetris.point :as point]))

(deftest in-bounds?-test
  (testing "with plain points [x y]"
    (testing "x < 1 is out of bounds"
      (is (false? (point/in-bounds? [0 5])))
      (is (false? (point/in-bounds? [-1 5])))
      (is (false? (point/in-bounds? [-10 0]))))

    (testing "x = 1 is in bounds (lower boundary)"
      (is (true? (point/in-bounds? [1 0])))
      (is (true? (point/in-bounds? [1 10]))))

    (testing "x between 1 and 10 is in bounds"
      (is (true? (point/in-bounds? [2 5])))
      (is (true? (point/in-bounds? [5 0])))
      (is (true? (point/in-bounds? [9 19]))))

    (testing "x = 10 is in bounds (upper boundary)"
      (is (true? (point/in-bounds? [10 0])))
      (is (true? (point/in-bounds? [10 19]))))

    (testing "x > 10 is out of bounds"
      (is (false? (point/in-bounds? [11 5])))
      (is (false? (point/in-bounds? [20 0])))
      (is (false? (point/in-bounds? [100 15])))))

  (testing "with colored points [[x y] color]"
    (testing "x < 1 is out of bounds"
      (is (false? (point/in-bounds? [[0 5] "red"])))
      (is (false? (point/in-bounds? [[-1 5] "blue"])))
      (is (false? (point/in-bounds? [[-10 0] "green"]))))

    (testing "x = 1 is in bounds (lower boundary)"
      (is (true? (point/in-bounds? [[1 0] "red"])))
      (is (true? (point/in-bounds? [[1 10] "blue"]))))

    (testing "x between 1 and 10 is in bounds"
      (is (true? (point/in-bounds? [[2 5] "red"])))
      (is (true? (point/in-bounds? [[5 0] "coral"])))
      (is (true? (point/in-bounds? [[9 19] "magenta"]))))

    (testing "x = 10 is in bounds (upper boundary)"
      (is (true? (point/in-bounds? [[10 0] "yellow"])))
      (is (true? (point/in-bounds? [[10 19] "turquoise"]))))

    (testing "x > 10 is out of bounds"
      (is (false? (point/in-bounds? [[11 5] "red"])))
      (is (false? (point/in-bounds? [[20 0] "silver"])))
      (is (false? (point/in-bounds? [[100 15] "limegreen"]))))))
