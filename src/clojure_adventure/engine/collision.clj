(ns clojure-adventure.engine.collision
  (:require [clojure-adventure.world :as world]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.vec2 :as vec2]
            [clojure.set :as set]))

(defn make-solid
  [entity]
  (assoc entity :solid true))

(defn -solid?
  [entity]
  (:solid entity))

; TODO: I might want to change this to "would this entity get stuck if it went there"
; TODO: Currently this includes the world's edges
(defn is-pos-solid?
  [state pos]
  ; TODO: I maybe should have world implement the grid interface, for stuff like that
  ; Or maybe the "has width/height" interface
  (or (not (grid/in-bounds? (:new-grid (:world state)) pos))
      (some -solid?
            (world/get-objects-at-pos (:world state) pos))))

(comment
  (require '[clojure-adventure.core :refer [initial-state]])
  (is-pos-solid? initial-state {:x 50 :y 10})
  (is-pos-solid? initial-state {:x 51 :y 11})
  :rcf)

(;;;;; Collision
 )

(defn collision-state
  []
  {:collisions {:new #{}
                :ongoing #{}
                :pending #{}}})

(defn new-collisions
  [state]
  (get-in state [:collisions :new]))

(defn ongoing-collisions
  [state]
  (get-in state [:collisions :ongoing]))

; Currently disabled - each collision is a set instead of 2 tuples
(defn -set-of-collisions-old
  "Given a coll of entity-ids (e.g. :a :b :c) at a certain spot, transforms it into pairs (e.g. #{#{:a :b} #{:b :c} #{:a :c}})"
  [entity-ids]
  (->> (for [a entity-ids
             b entity-ids]
         (if (not= a b) #{a b} nil))
       (filter (comp not nil?))
       (set)))

(comment
  (-set-of-collisions-old #{:a :b :c})
  :rcf)

(defn -set-of-collisions
  "Given a coll of entity-ids (e.g. :a :b :c) at a certain spot, transforms it into pairs (e.g. #{#{:a :b} #{:b :c} #{:a :c}})"
  [entity-ids]
  (->> (for [a entity-ids
             b entity-ids]
         (if (not= a b) [a b] nil))
       (filter (comp not nil?))
       (set)))

(defn -set-of-collisions-with
  "Like -set-of-collisions, but there is 1 collider colliding into a cell"
  [mover colliders]
  (->> (for [e colliders] [[mover e] [e mover]])
       (apply concat)
       (set)))

(comment
  (-set-of-collisions #{:a :b :c})
  (-set-of-collisions-with :a #{:b :c})
  (flatten [[1 2] [3 4]])
  (flatten [[[1 2] [3 4]] [[1 2] [3 4]]])
  :rcf)

(defn -collisions-in-pos
  [world pos]
  (->> (world/get-objects-at-pos world pos)
       (map :id)
       (-set-of-collisions)))

; TODO: Find a way to time this function; Maybe even to profile everything
; BIG TODO: I think this should include when objects try to go to a wall and collide with it???
   ; Might not be that bad actually
(defn stationary-collisions
  "The results is a set of all of the collisions.
   Each collision is a tuple of 2 colliding items.
   Might make it a set of all of the colliding items if I drop the tuples idea."
  [state]
  (->> (world/positions (:world state))
       (map (fn [pos] (-collisions-in-pos (:world state) pos)))
       (apply concat)
       (set)))

(comment
  (require '[clojure-adventure.core :as core])
  (def world (-> (core/get-initial-world)
                 (world/spawn-object :foo {:pos (vec2/vec2 5 5)})
                 (world/spawn-object :bar {:pos (vec2/vec2 5 5)})
                 (world/spawn-object :baz {:pos (vec2/vec2 5 5)})))
  (def pos (vec2/vec2 5 5))
  (stationary-collisions {:world world})
  (-collisions-in-pos world pos)
  :rcf)

(defn update-collisions
  [state]
  (let [current-collisions (set/union (get-in state [:collisions :pending])
                                      (stationary-collisions state))
        past-collisions (set/union (get-in state [:collisions :new]) (get-in state [:collisions :ongoing]))

        new-collisions (set/difference current-collisions past-collisions)
        ongoing-collisions (set/difference current-collisions new-collisions)]
    (-> state
        (assoc-in [:collisions :ongoing] ongoing-collisions)
        (assoc-in [:collisions :new] new-collisions)
        (assoc-in [:collisions :pending] #{}))))

(comment
  (require '[clojure-adventure.core :as core])
  (-> core/initial-state
      (update-collisions)
      (update-collisions)
      :collisions)
  :rcf)

(defn notify-collision
  ; TODO: document that this is just for stuff that doesn't get on the same clel
  [state mover-id target-pos]
  (let [colliders (map :id (world/get-objects-at-pos (:world state) target-pos))
        collisions (-set-of-collisions-with mover-id colliders)]
    (update-in state [:collisions :pending] set/union collisions)))

(comment
  (require '[clojure-adventure.core :refer [initial-state]])
  (:collisions (notify-collision initial-state :foo {:x 50 :y 10}))
  :rcf)
