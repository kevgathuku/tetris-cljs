(ns acme.frontend.tetris.game-test
  (:require [cljs.test :refer [deftest is testing]]
            [acme.frontend.tetris.game :as game]
            [acme.frontend.tetris.block :as block]))

(deftest init-test
  (testing "creates initial game state"
    (let [g (game/init)]
      (is (nil? (:tetro g)))
      (is (= 0 (:score g)))
      (is (= {} (:points g)))
      (is (= {} (:junkyard g))))))

(deftest new-game-test
  (testing "creates game with tetromino"
    (let [g (game/new-game)]
      (is (some? (:tetro g)))
      (is (= 0 (:score g)))
      (is (seq (:points g))))))

(deftest inc-score-test
  (testing "increments score by value"
    (let [g (game/new-game)]
      (is (= 10 (:score (game/inc-score g 10))))
      (is (= 5 (:score (-> g (game/inc-score 3) (game/inc-score 2))))))))

(deftest move-test
  (testing "moves tetro when valid"
    (let [g (game/new-game)
          moved (game/right g)]
      (is (not= (:location (:tetro g)) (:location (:tetro moved))))))

  (testing "does not move when invalid (at boundary)"
    (let [g (-> (game/init)
                (assoc :tetro (block/create {:shape :o :location [8 0]}))
                (game/show))
          moved (game/right g)]
      (is (= (:location (:tetro g)) (:location (:tetro moved)))))))

(deftest down-test
  (testing "moves tetro down when valid"
    (let [g (game/new-game)
          loc-before (:location (:tetro g))
          loc-after (:location (:tetro (game/down g)))]
      (is (= (second loc-before) (dec (second loc-after))))))

  (testing "merges into junkyard when at bottom"
    (let [g (-> (game/init)
                (assoc :tetro (block/create {:shape :o :location [2 17]}))
                (game/show))
          after-down (game/down g)]
      (is (= 4 (count (:junkyard after-down))))
      (is (not= (:tetro g) (:tetro after-down))))))

(deftest collision-test
  (testing "cannot move into junkyard"
    (let [g (-> (game/init)
                (assoc :junkyard {[5 10] "red" [6 10] "red"})
                (assoc :tetro (block/create {:shape :o :location [4 6]}))
                (game/show))
          after-down (game/down g)]
      (is (= 6 (count (:junkyard after-down)))))))
