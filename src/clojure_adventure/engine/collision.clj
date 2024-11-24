(ns clojure-adventure.engine.collision
  (:require [clojure-adventure.world :as world]))

(defn make-solid
  [entity]
  (assoc entity :solid true))

(defn -solid?
  [entity]
  (:solid entity))

; TODO: I might want to change this to "would this entity get stuck if it went there"
(defn is-pos-solid?
  [world pos]
  (some -solid?
        (world/get-objects-at-pos world pos)))

(comment
  (require '[clojure-adventure.core :refer [initial-state]])
  (is-pos-solid? (:world initial-state) {:x 50 :y 10})
  (is-pos-solid? (:world initial-state) {:x 51 :y 11})
  :rcf)