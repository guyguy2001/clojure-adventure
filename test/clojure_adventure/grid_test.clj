(ns clojure-adventure.grid-test
  (:require [clojure.test :refer :all]
            [clojure-adventure.grid :refer :all]))

(def initial-board (vec (repeat 30 (vec (repeat 100 "-")))))

(def testing-objects-1
  [{:pos {:x 51 :y 13} :symbol "?"} {:pos {:x 53 :y 15} :symbol "@"}])
(def testing-objects-2
  [[{:pos {:x 51 :y 13} :symbol "?"}] [{:pos {:x 53 :y 15} :symbol "@"}]])
(def expected-result
  (-> initial-board
      (assoc-grid 51 13 "?")
      (assoc-grid 53 15 "@")))

(deftest test-layers
  (testing "Combining layers gives the right result"
    (assert (=
             (combine-layers initial-board testing-objects-1)
             expected-result))
    (assert (=
             (apply combine-layers initial-board testing-objects-2)
             expected-result))))