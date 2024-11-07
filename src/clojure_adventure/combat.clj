(ns clojure-adventure.combat
  (:require [clojure-adventure.actions :as actions]
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
  (update state :world
          #(movement/try-move-by % entity-id
                                 (:facing-direction (world/get-object % entity-id)))))

(defn handle-projectiles
  [state]
  (actions/reduce-by-entity-type state :fireball move-forward))

(defn handle-combat
  [state action]
  (-> state
      (handle-attacking action)
      (handle-projectiles)))

(comment
  (require '[clojure-adventure.core :as core])
  (-> @core/*state
      (handle-attacking :fireball)
      (handle-projectiles))
  (world/get-player (:world @core/*state))
  :rcf)


