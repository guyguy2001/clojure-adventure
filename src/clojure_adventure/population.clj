(ns clojure-adventure.population
  (:gen-class)
  (:require [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.grid :as grid]))


(def starting-map (vec (repeat 20 (vec (repeat 100 "-")))))

(defn populate-horizontal-line
  [grid symbol start length]
  (reduce (fn [grid pos] (grid/assoc-grid-vec2 grid pos symbol))
          grid
          (map (fn [i] (vec2/add start (vec2/vec2 i 0))) (range length))))


(defn populate-vertical-line
  [grid symbol start length]
  (reduce (fn [grid {x :x y :y}] (assoc-in grid [y x] symbol))
          grid
          (map (fn [i] (vec2/add start (vec2/vec2 0 i))) (range length))))

(defn populate-square
  "###
   # #
   ###"
  [grid symbol top-left size]
  (-> grid
      (populate-horizontal-line symbol top-left size)
      (populate-vertical-line symbol top-left size)
      (populate-horizontal-line symbol (vec2/add top-left (vec2/vec2 0 (dec size))) size)
      (populate-vertical-line symbol (vec2/add top-left (vec2/vec2 (dec size) 0)) size)))


(defn populate-grid-inplace
  "Puts the given character on empty spaces"
  [grid char n]
  (if (= n 0)
    grid
    (let [empty-spaces (grid/get-empty-spaces grid)
          [x y] (rand-nth empty-spaces)]
      (populate-grid-inplace (assoc-in grid [y x] char)
                             char
                             (dec n)))))

(defn populate-grid-return
  "Puts the given character on empty spaces"
  ([grid char n]
   (if (= n 0)
     []
     (let [empty-spaces (grid/get-empty-spaces grid)
           [x y] (rand-nth empty-spaces)]
       (cons {:pos (vec2/vec2 x y) :symbol char}
             (populate-grid-return (assoc-in grid [y x] char) ; only modified for get-empty-spaces
                                   char
                                   (dec n)))))))
(comment
;;   (populate-horizontal-line [["-"]] "#" {:x 0 :y 0} 1)
  (assoc [] 0 :hi)
  (assoc [] 1 :hi)
  (assoc nil :foo "bar")
  (assoc-in [] [0 0] :foo)
  (def grid [["-"]])
  (def symbol "#")
  (def top-left (vec2/vec2 0 0))
  (def size 1)
  (populate-square [["-"]] "#" {:x 1 :y 1} 1)
  (populate-square [["-"]] "#" {:x 0 :y 0} 1)
  (populate-square [["-"]] "#" {:x 1 :y 1} 1)
;;   starting-map
;;   (populate-square starting-map "#" {:x 50 :y 10} 10)
  :rcf)
