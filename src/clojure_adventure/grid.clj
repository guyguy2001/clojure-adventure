(;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; Smart Grid
    ;;
    ;; A 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(ns clojure-adventure.grid)


;; (defn -cartesian-product
;;   [coll1 coll2]
;;   (for [a coll1 b coll2] [x y]))

(defn width
  [grid]
  (if-let [row (get grid 0)]
    (count row)
    0))

(defn height
  [grid]
  (count grid))

; TODO: chagne to [grid [x y] value] to be more similar to assoc-in?
(defn assoc-grid
  [grid x y value]
  (if (and
       (< y (height grid))
       (< x (width grid)))
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
