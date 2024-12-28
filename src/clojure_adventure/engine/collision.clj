(ns clojure-adventure.engine.collision
  (:require [clojure-adventure.world :as world]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.vec2 :as vec2]))

(defn make-solid
  [entity]
  (assoc entity :solid true))

(defn -solid?
  [entity]
  (:solid entity))

; TODO: I might want to change this to "would this entity get stuck if it went there"
; TODO: Currently this includes the world's edges
(defn is-pos-solid?
  [world pos]
  ; TODO: I maybe should have world implement the grid interface, for stuff like that
  ; Or maybe the "has width/height" interface
  (or (not (grid/in-bounds? (:new-grid world) pos))
      (some -solid?
            (world/get-objects-at-pos world pos))))

(comment
  (require '[clojure-adventure.core :refer [initial-state]])
  (is-pos-solid? (:world initial-state) {:x 50 :y 10})
  (is-pos-solid? (:world initial-state) {:x 51 :y 11})
  :rcf)

(defn -set-of-collisions
  "Given a coll of entity-ids (e.g. :a :b :c) at a certain spot, transforms it into pairs (e.g. #{#{:a :b} #{:b :c} #{:a :c}})"
  [entity-ids]
  (->> (for [a entity-ids
             b entity-ids]
         (if (not= a b) #{a b} nil))
       (filter (comp not nil?))
       (set)))

(comment
  (-set-of-collisions #{:a :b :c})
  :rcf)


(defn -collisions-in-pos
  [world pos]
  (->> (world/get-objects-at-pos world pos)
       (map :id)
       (-set-of-collisions)))

; TODO: Find a way to time this function; Maybe even to profile everything
; The results is a set of all of the collisions.
; Each collision is a set of 2 colliding items. Might make it a set of all of the colliding items later.
(defn calculate-collisions
  [state]
  (reduce (fn [collisions pos]
            (into collisions (-collisions-in-pos (:world state) pos)))
          #{}
          (world/positions (:world state))))

(comment
  (require '[clojure-adventure.core :as core])
  (def world (-> (core/get-initial-world)
                 (world/spawn-object :foo {:pos (vec2/vec2 5 5)})
                 (world/spawn-object :bar {:pos (vec2/vec2 5 5)})
                 (world/spawn-object :baz {:pos (vec2/vec2 5 5)})))
  (def pos (vec2/vec2 5 5))
  (calculate-collisions {:world world})
  (-collisions-in-pos world pos)
  :rcf)