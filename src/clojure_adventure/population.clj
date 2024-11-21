(ns clojure-adventure.population
  (:gen-class)
  (:require [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.world :as world]))


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

(comment
  (populate-square (world/new-world 1 1 "-") :wall {:symbol "#"} {:x 0 :y 1} 1)
  (populate-square (world/new-world 1 1 "-") :wall {:symbol "#"} {:x 0 :y 0} 1)
  (populate-square (world/new-world 1 1 "-") :wall {:symbol "#"} {:x 1 :y 1} 1)
  :rcf)

(defn get-random-empty-cell
  [world]
  (rand-nth (grid/get-empty-spaces (:new-grid world))))

(comment
  (require '[clojure-adventure.core :as core])
  (get-random-empty-cell (core/get-initial-world))
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
          (map :id (world/get-objects-at-pos world pos))))