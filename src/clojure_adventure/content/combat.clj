(ns clojure-adventure.content.combat
  (:require
   [clojure-adventure.actions :as actions]
   [clojure-adventure.engine.collision :as collision]
   [clojure-adventure.movement :as movement]
   [clojure-adventure.world :as world]))

"Ideas:
 * When I attack, I shoot a fireball that moves over tiles
 * make the 3 tiles perpendicular and in front of me (drawing in the next line) green, and damage in them
   p   (player)
  xxx  (damaging area)"

(defn fireball-attack
  [state caster]
  (update state :world
          #(world/spawn-objects %
                                :fireball [{:pos (:pos caster)
                                            :facing-direction (:facing-direction caster)
                                            :symbol "•"}])))


(defn handle-attacking
  [state action]
  (case action
    :fireball (fireball-attack state (world/get-player (:world state)))
    state))

(defn move-forward
  [state entity-id]
  (movement/try-move-by state entity-id
                        (:facing-direction (world/get-object (:world state) entity-id))))

(defn -is-fireball?
  [[category id :as entity-id]]
  (= category :fireball))

; TODO: I'd much rather have this in a :despawn-on-colllision component
; TODO: I'm having a hard time extracting just the fireball. Does this mean that each collision should be a tuple, and not a set? yes I think
(defn despawn-colliding-projectiles
  [state]
  (reduce (fn [state [a _b :as _collision]]
            (if (-is-fireball? a)
              ; TODO: Crashes if it's already despawned (e.g. when colliding with 2 things)
              (update state :world world/despawn a)
              state))
          state
          (collision/new-collisions state)))

(defn handle-projectiles
  [state]
  (-> state
      (actions/reduce-by-entity-type :fireball move-forward)))

(defn do-turn
  [state action]
  (-> state
      (handle-attacking action)
      (handle-projectiles)))


(defn post-physics
  [state]
  (despawn-colliding-projectiles state))

(comment
  (require '[clojure-adventure.core :as core])
  (-> @core/*state
      (handle-attacking :fireball)
      (handle-projectiles))
  (world/get-player (:world @core/*state))
  :rcf)
