(ns clojure-adventure.interaction
  (:require
   [clojure-adventure.world :as world]))



(defn -get-nearest-interactable-entity
  [state pos]
  (:id
   (first (filter (comp not nil?)
                  (world/get-neighboaring-objects (:world state) pos)))))


(comment
  (require '[clojure-adventure.core :as core])
  (world/get-neighboaring-objects (:world core/initial-state) {:x 53 :y 15})
  (-get-nearest-interactable-entity
   core/initial-state {:x 51, :y 14})
  (world/get-object-list (:world core/initial-state))
  (:pos (world/get-player (:world @core/*state)))
  (:name (world/get-object (:world @core/*state) (:interaction-focus @core/*state)))
  :rcf)


(defn get-new-interaction-focus
  [state]
  (-get-nearest-interactable-entity state (:pos (world/get-player (:world state)))))