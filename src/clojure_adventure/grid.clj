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
  ([grid pos value]
   (assoc-grid grid (:x pos) (:y pos) value))
  ([grid x y value]
   (if (in-bounds? grid x y)
     (assoc-in grid [y x] value)
     (throw
      (ex-info (format "Grid index out of bounds. [%d %d]" x y) {:type :grid-out-of-bounds :x x :y y})))))

(defn assoc-grid-vec2
  [grid {x :x y :y} value]
  (assoc-grid grid x y value))

(defn update-grid
  [grid pos f & args]
  (let [x (:x pos), y (:y pos)]
    (if (in-bounds? grid x y)
      (apply update-in grid [y x] f args)
      (throw
       (ex-info (format "Grid index out of bounds. [%d %d]" x y) {:type :grid-out-of-bounds :x x :y y})))))

(def empty-cell ".") ; TODO: Move this to population.clj, make empty cells be represented as nil or smth


(;; Layers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(defn combine-items-to-grid
  [grid items]
  (reduce (fn [grid {{x :x y :y} :pos s :symbol}]
            (assoc-in grid [y x] s))
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


(defn get-neighboaring-cells
  [grid pos]
  (->> vec2/cardinal-directions
       (map #(vec2/add pos %))
       (filter #(in-bounds? grid %))))

(comment
  (def grid [[1 2] [3 4]])
  (in-bounds? grid 0 0)
  (in-bounds? grid -1 0)
  (in-bounds? grid 0 -1)
  (in-bounds? grid 1 1)
  (in-bounds? grid 2 1)
  (in-bounds? grid 1 2)
  (in-bounds? grid 2 2)
  (get-neighboaring-cells [[1 2] [3 4]] (vec2/vec2 0 0))


  :rcf)

; It seems like now I'm operating on Grid<Vec<world-identifiers>> as my source of truth, and also rendering a Grid<char>.
(defn add-to-grid
  [grid [path entity]]
  (update-grid grid
               (:pos entity)
               #(conj % path)))

(defn combine-to-grid
  [starting-grid entries]
  (reduce add-to-grid starting-grid entries))

(defn grid-entries
  [grid]
  (for [x (range (width grid))
        y (range (height grid))]
    [(vec2/vec2 x y) (get-grid grid x y)]))

(defn map-entries-grid
  [f grid]
  (reduce (fn [grid [pos cell]]
            (assoc-grid grid pos
                        (f pos cell)))
          grid
          (grid-entries grid)))

(defn map-grid
  [f grid]
  (mapv (fn [row]
          (mapv (fn [cell] (f cell))
                row))
        grid))

(comment
  (map-entries-grid (fn [x y] [x y]) [[1 2] [3 4]])
  (map-grid (fn [x] [x]) [[1 2] [3 4]])
  :rcf)

(defn is-cell-empty
  [ids-grid pos]
  (= (get-grid ids-grid pos) []))

(defn get-empty-spaces
  "Accepts a gird of vecs of ids, and returns the positions with empty cells."
  [ids-grid]
  (->> ids-grid
       (grid-entries)
       (filterv (fn [[_pos v]] (= v [])))
       (mapv (fn [[pos _v]] pos))))

(comment
  (get-empty-spaces [[[1] []] [[] [4]]])
  :rcf)

(comment
  (def starting-grid [[[] []] [[] []]])
  (def entries [[:player {:pos (vec2/vec2 0 1) :symbol "@"}]
                [:enemy {:pos (vec2/vec2 1 0) :symbol "+"}]
                [:foo {:pos (vec2/vec2 1 0) :symbol "+"}]])
  (combine-to-grid starting-grid entries)
  :rcf)