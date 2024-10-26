(ns clojure-adventure.world-impl.ids-grid
  (:require
   [clojure-adventure.grid :as grid]))

(defn conjv
  [coll x]
  (if (nil? coll)
    [x]
    (conj coll x)))

(comment
  (conjv nil 1)
  (conjv [] 1)
  (conjv [1] 2)
  :rcf)

(defn insert-id
  [grid pos id]
  (grid/update-grid grid pos
                    conjv id))
