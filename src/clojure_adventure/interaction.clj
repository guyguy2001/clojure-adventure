(ns clojure-adventure.interaction
  (:require
   [clojure-adventure.world :as world]))



(defn get-interaction-focus-target
  ;todo: name
  [state pos]
  (first ; This first is to take the "key" of [[:players 0] { object data ... }]
   (first (filter (comp not nil?)
                  (world/get-neighboaring-objects (:world state) pos)))))


(comment
  (require '[clojure-adventure.core :as core])
  (world/get-neighboaring-objects (:world core/initial-state) {:x 53 :y 15})
  (get-interaction-focus-target
   core/initial-state {:x 51, :y 14})
  (world/get-object-list (:world core/initial-state))
  (:pos (world/get-player (:world @core/*state)))
  (:name (world/get-object (:world @core/*state) (:interaction-focus @core/*state)))
  :rcf)


(defn get-new-interaction-focus
  [state]
  (get-interaction-focus-target state (:pos (world/get-player (:world state)))))