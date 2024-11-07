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

(defn remove-from-vec
  [vec x]
  (let [new-vec (remove #(= % x) vec)]
    (assert (= (count new-vec) (dec (count vec))))
    new-vec))

(defn remove-id
  [grid pos id]
  (assert (not (nil? pos)))
  (grid/update-grid grid pos
                    remove-from-vec id))

(comment
  (vec (remove #(= % 1) [1 2 3]))
  (vec (remove #(= % 0) [1 2 3]))
  :rcf)