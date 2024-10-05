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
  [grid x y]
  (get-in grid [y x]))

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

(defn get-empty-spaces
  [grid]
  (let [positions (for [x (range (width grid)) y (range (height grid))] [x y])]
    (vec (filter (fn [[x y]] (= (get-in grid [y x]) "-")) positions))))


(;; Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(defn combine-items-to-grid
  [grid items]
  (reduce (fn [grid {{x :x y :y} :pos s :symbol}] (assoc-in grid [y x] s))
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