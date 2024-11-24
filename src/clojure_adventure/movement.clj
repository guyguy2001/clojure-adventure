(ns clojure-adventure.movement
  (:require [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.world :as world]
            [clojure-adventure.engine.collision :as collision]))

(defn try-move-by
  [world entity-id by]
  (let [world (world/update-object world entity-id
                                   assoc :facing-direction by)
        target (vec2/add (:pos (world/get-object world entity-id)) by)]
    (if (not (collision/is-pos-solid? world target))
      (world/move-to world entity-id target)
      world)))