(ns clojure-adventure.actions
  (:require [clojure-adventure.world :as world]))

(;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; Actions.clj
    ;;
    ;; This file is for "ecs like" stuff - being able to execute actions for all :players for example.
    ;; Mainly helpers for core/evaluate-turn, and similar functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

; TODO: This version probably sucks for updating 2 things at once
; for example if the player fights an enemy and both need updating
; whoops
; I think I wouldn't feel like I need this if I just used ->
; with functions that receive the state, and live outside of
; this as global functions
; TODO I need to figure out how to base this on world/ abstractions
(defn apply-to-objects
  "actions: [:players (fn[state player] <new-player>)
             :enemies (fn[state enemy] <new-enemy>)]"
  [state actions]
  (reduce (fn [state [type f]]
            (let  [world (:world state)
                   paths (world/get-paths-of-type world type)]
              (reduce (fn [state path]
                        (update state :world
                                #(world/update-object % path
                                                      (fn [obj] (f state obj)))))
                      state paths)))
          state
          (partition 2 actions)))

(comment
  (require '[clojure-adventure.core :as core]
           '[clojure-adventure.vec2 :as vec2]
           '[clojure-adventure.movement :as movement])
  (get-in core/initial-state [:world :objects])
  (apply-to-objects core/initial-state
                    [:players (fn [{:keys [world]} player]
                                (if (not= :right nil)
                                  (movement/try-move-by world player (vec2/vec2 1 2))
                                  player))

                     :enemies
                     (fn [{:keys [world]} enemy] (core/enemy-turn world enemy))])
  (-> core/initial-state
      (apply-to-objects
       [:enemies (fn [{:keys [world]} e] (core/enemy-turn world e))])
      (get-in [:world :objects]))
  :rcf)

(defn update-with-context
  "Like update, but the callback receives the entire map, not just the original value"
  [map key f]
  (assoc map key (f map)))