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

(defn combine-layers
  ([first-layer]
   first-layer)
  ([first-layer second-layer & layers]
   (assert (vector? first-layer))
   (let [combined-layer
         (cond
           (empty? second-layer) (apply combine-layers first-layer layers)
           (map? (first second-layer)) (combine-items-to-grid first-layer second-layer)
           (vector? (first second-layer)) (combine-vec2-layers first-layer second-layer))]
     (apply combine-layers (into [combined-layer] layers)))))