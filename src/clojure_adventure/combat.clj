(ns clojure-adventure.combat
  (:require [clojure-adventure.world :as world]
            [clojure-adventure.actions :as actions]
            [clojure-adventure.vec2 :as vec2]))

"Ideas:
 * When I attack, I shoot a fireball that moves over tiles
 * make the 3 tiles perpendicular and in front of me (drawing in the next line) green, and damage in them
   p   (player)
  xxx  (damaging area)"

(defn fireball-attack
  [state caster]
  (world/spawn-objects state :fireball [{:pos (:pos caster)
                                         :facing-direction (:facing-direction caster)
                                         :symbol "∘︎"}]))


(defn handle-attacking
  [state action]
  (case action
    :fireball (fireball-attack state (world/get-player state))
    state))

(defn move-forward
  [_state entity]
  (update entity :pos vec2/add (:facing-direction entity)))

(defn handle-projectiles
  [state]
  (actions/apply-to-objects state
                            [:fireball move-forward]))

(defn handle-combat
  [state action]
  (-> state
      (handle-attacking action)
      (handle-projectiles)))


