(ns clojure-adventure.actions
  (:require [clojure-adventure.world :as world]))

(;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; Actions.clj
    ;;
    ;; This file is for "ecs like" stuff - being able to execute actions for all :players for example.
    ;; Mainly helpers for core/evaluate-turn, and similar functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(defn reduce-by-entity-type
  "example: (reduce-by-entity-type state :players (fn [state player-id] state))"
  [state type f]
  (reduce f
          state
          (world/get-paths-of-type (:world state) type)))

(comment
  (require '[clojure-adventure.core :as core])
  (reduce-by-entity-type @core/*state :players (fn [state player]
                                                 (println player)
                                                 state))
  :rcf)

(defn update-with-context
  "Like update, but the callback receives the entire map, not just the original value"
  [map key f]
  (assoc map key (f map)))