(ns clojure-adventure.population
  (:gen-class)
  (:require [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.world :as world]))


(def starting-map (vec (repeat 30 (vec (repeat 100 grid/empty-cell)))))


; populate-vertical-line is my original implementation, and horizontal-line is based on suggestions from slack.
; It doesn't really use the vector 2 abstraction, but is cleaner and probably simpler to understand
(defn populate-horizontal-line
  [world type object pos length]
  (reduce (fn [world i] (world/spawn-object world type
                                            (assoc object :pos (vec2/add pos (vec2/vec2 i 0)))))
          world
          (range length)))


(defn populate-vertical-line
  [world type object pos length]
  (world/spawn-objects world type (map (fn [i] (assoc object :pos (vec2/add pos (vec2/vec2 0 i))))
                                       (range length))))

(defn populate-square
  "###
   # #
   ###"
  [world type object top-left size]
  (-> world
      (populate-horizontal-line type object top-left size)
      (populate-vertical-line type object top-left size)
      (populate-horizontal-line type object (vec2/add top-left (vec2/vec2 0 (dec size))) size)
      (populate-vertical-line type object (vec2/add top-left (vec2/vec2 (dec size) 0)) size)))

(defn populate-grid-return
  "Puts the given character on empty spaces"
  ([grid char n]
   (populate-grid-return grid char n nil))
  ([grid char n name]
   (if (= n 0)
     []
     (let [empty-spaces (grid/old-get-empty-spaces grid)
           [x y] (rand-nth empty-spaces)]
       (cons {:pos (vec2/vec2 x y) :symbol char :name name}
             (populate-grid-return (assoc-in grid [y x] char) ; only modified for old-get-empty-spaces
                                   char
                                   (dec n)
                                   name))))))

(defn populate-grid-return-2
  "Puts the given pre-made object on empty spaces"
  ([grid object n]
   (if (= n 0)
     []
     (let [empty-spaces (grid/old-get-empty-spaces grid)
           [x y] (rand-nth empty-spaces)]
       (cons (assoc object :pos (vec2/vec2 x y))
             (populate-grid-return-2 (assoc-in grid [y x] (:symbol object)) ; only modified for old-get-empty-spaces
                                     object
                                     (dec n)))))))
(comment
;;   (populate-horizontal-line [[grid/empty-cell]] "#" {:x 0 :y 0} 1)
  (assoc [] 0 :hi)
  (assoc [] 1 :hi)
  (assoc nil :foo "bar")
  (assoc-in [] [0 0] :foo)
  (def grid [[grid/empty-cell]])
  (def symbol "#")
  (def top-left (vec2/vec2 0 0))
  (def size 1)
  (populate-square (world/new-world [[["-"]]]) :wall {:symbol "#"} {:x 0 :y 1} 1)
  (populate-square (world/new-world [[["-"]]]) :wall {:symbol "#"} {:x 0 :y 0} 1)
  (populate-square (world/new-world [[["-"]]]) :wall {:symbol "#"} {:x 1 :y 1} 1)
;;   starting-map
;;   (populate-square starting-map "#" {:x 50 :y 10} 10)
  :rcf)

(defn get-random-empty-cell
  [world]
  (rand-nth (grid/get-empty-spaces (:new-grid world))))

(comment
  (require '[clojure-adventure.core :as core])
  (get-random-empty-cell (world/new-world starting-map))
  :rcf)

; TODO: This is incredibly unoptimized. I should just ask for N empty cells prolly?
(defn spawn-at-an-empty-cell
  [world type object]
  (let [pos (get-random-empty-cell world)
        object (assoc object :pos pos)]
    (world/spawn-objects world type [object])))

(defn spawn-at-random-empty-cells
  [world type object n]
  (reduce (fn [world _] (spawn-at-an-empty-cell world type object))
          world
          (range n)))

(defn remove-all-in-cell
  [world pos]
  (reduce (fn [world id]
            (world/despawn world id))
          world
          ; The first is to get the entity id from the "entry"
          (map first (world/get-objects-at-pos world pos))))