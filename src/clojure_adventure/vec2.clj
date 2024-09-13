(ns clojure-adventure.vec2)

(defn vec2
  [x y]
  {:x x :y y})

(def left (vec2 -1 0))
(def right (vec2 1 0))
(def up (vec2 0 -1))
(def down (vec2 0 1))

(def cardinal-directions [left right up down])

(defn -apply-items
  "Example: `(-apply-itmes + (vec2 [1 2])  vec2 ([3 4]))` => `(vec2 [4 6])`"
  [f & vecs]
  (vec2 (apply f (map :x vecs))
        (apply f (map :y vecs))))

(defn add
  [u v]
  (-apply-items + u v))

(defn sub
  [u v]
  (-apply-items - u v))
