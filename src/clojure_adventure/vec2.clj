(ns clojure-adventure.vec2)

(defn vec2
  [x y]
  {:x x :y y})

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
