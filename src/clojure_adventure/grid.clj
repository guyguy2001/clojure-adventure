(;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; Smart Grid
    ;;
    ;; A 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(ns clojure-adventure.grid
  (:require [clojure-adventure.vec2 :as vec2]))



;; (defn -cartesian-product
;;   [coll1 coll2]
;;   (for [a coll1 b coll2] [x y]))

(;; Core
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(defn width
  [grid]
  (if-let [row (get grid 0)]
    (count row)
    0))

(defn height
  [grid]
  (count grid))

(defn in-bounds?
  ([grid {x :x y :y}]
   (in-bounds? grid x y))
  ([grid x y]
   (and
    (>= x 0)
    (>= y 0)
    (< x (width grid))
    (< y (height grid)))))

(defn get-grid
  "Line get-in for grids (abstracts away the x y order). NOTE: Can return nil."
  ([grid x y]
   (get-in grid [y x]))
  ([grid {x :x y :y}]
   (get-grid grid x y)))

; TODO: chagne to [grid [x y] value] to be more similar to assoc-in?
(defn assoc-grid
  [grid x y value]
  (if (in-bounds? grid x y)
    (assoc-in grid [y x] value)
    (throw
     (ex-info (format "Grid index out of bounds. [%d %d]" x y) {:type :grid-out-of-bounds :x x :y y}))))

(defn assoc-grid-vec2
  [grid {x :x y :y} value]
  (assoc-grid grid x y value))

(def empty-cell ".") ; TODO: Move this to population.clj, make empty cells be represented as nil or smth

(defn get-empty-spaces
  [grid]
  (let [positions (for [x (range (width grid)) y (range (height grid))] [x y])]
    (vec (filter (fn [[x y]] (= (get-in grid [y x]) empty-cell)) positions))))


(;; Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(defn combine-items-to-grid
  [grid items]
  (reduce (fn [grid {{x :x y :y} :pos s :symbol dead :dead}]
            (when (or (nil? x) (nil? y))
              (println items s dead))
            (if (not dead) ; TODO: This if is just a workaround until I get despawning working
              (assoc-in grid [y x] s)
              grid))
          grid items))

(defn combine-vec2-layers
  [layer1 layer2]
  (vec (map (fn [row1 row2]
              (vec (map (fn [c1 c2] (if (nil? c2) c1 c2))
                        row1 row2)))
            layer1 layer2)))

(defn combine-layers-1
  [first-layer second-layer]
  (assert (vector? first-layer))
  (cond
    (empty? second-layer) first-layer
    (map? (first second-layer)) (combine-items-to-grid first-layer second-layer)
    (vector? (first second-layer)) (combine-vec2-layers first-layer second-layer)))

(defn combine-layers-r
  ([first-layer]
   first-layer)
  ([first-layer second-layer & layers]
   (assert (vector? first-layer))
   (let [combined-layer
         (combine-layers-1 first-layer second-layer)]
     (apply combine-layers-r (cons combined-layer layers)))))

(defn combine-layers
  [& args]
  (apply combine-layers-r args))


(;; Neighbors? Interactions?
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )


(defn get-neighboars
  [grid pos]
  (filter (partial in-bounds? grid) (map #(vec2/add pos %) vec2/cardinal-directions)))

(comment
  (def grid [[1 2] [3 4]])
  (in-bounds? grid 0 0)
  (in-bounds? grid -1 0)
  (in-bounds? grid 0 -1)
  (in-bounds? grid 1 1)
  (in-bounds? grid 2 1)
  (in-bounds? grid 1 2)
  (in-bounds? grid 2 2)
  (get-neighboars [[1 2] [3 4]] (vec2/vec2 0 0))


  :rcf)

"Grid rewrite thoughts:
 * There's no reason to embed stuff directly into the ascii (like how I did with #), it's kinda silly
   Also the whole concept of layers which are just 2d arrays are kinda silly
 
 Concerete plans:
 * Remove grid2d layers - the inputs are only 1d lists of objects.
 * The result is a 2d grid of vecs of items on that square - maybe not a vec at the start, but it will be a vec in the future.
 * I can easily query for object at pos, and normal objects behave just like #
 * Objects will have :collision false if they are ghosts, and another property which I forgot"