(ns clojure-adventure.movement
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.vec2 :as vec2]))

(defn try-move-by
  [world entity by]
  (let [entity (assoc entity :facing-direction by)
        moved (update entity :pos vec2/add by)]
    (if (= (grid/get-grid (:base-grid world) (:pos moved)) "-")
      moved
      entity)))