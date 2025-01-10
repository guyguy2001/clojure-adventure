(ns clojure-adventure.movement
  (:require [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.world :as world]
            [clojure-adventure.engine.collision :as collision]))

(defn try-move-by
  [state entity-id by]
  (let [state (update state :world #(world/update-object % entity-id
                                                         assoc :facing-direction by))
        target (vec2/add (:pos (world/get-object (:world state) entity-id)) by)]
    (if (not (collision/is-pos-solid? state target))
      (update state :world #(world/move-to % entity-id target))
      state)))