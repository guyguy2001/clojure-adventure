(;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; Smart Grid
    ;;
    ;; A 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
 )

(ns clojure-adventure.grid
  (:gen-class))


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

(defn get-empty-spaces
  [grid]
  (let [positions (for [x (range (width grid)) y (range (height grid))] [x y])]
    (vec (filter (fn [[x y]] (= (get-in grid [y x]) "-")) positions))))
