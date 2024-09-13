(ns clojure-adventure.core
  (:gen-class)
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.ui :as ui]))


(defn move-by
  [pos {x :x y :y}]
  (update
   (update pos :x (partial + x))
   :y (partial + y)))


(defn try-move-by
  [grid entity by]
  (let [moved (move-by entity by)]
    (if (= (get-in grid [(:y moved) (:x moved)]) "-")
      moved
      entity)))

(def direction-by-input
  {:left vec2/left
   :right vec2/right
   :up vec2/up
   :down vec2/down})

(defn enemy-turn
  [grid enemy]
  (update enemy :pos #(try-move-by grid % (rand-nth vec2/cardinal-directions))))

(defn game-loop
  [screen {board :board player :player enemies :enemies}]
  ((ui/draw-screen screen {:board (grid/combine-layers board [player] enemies)})
   (let [input (ui/get-input screen)
         direction (get direction-by-input input)
         player (if (not= direction nil)
                  (update player :pos #(try-move-by board % direction))
                  player)
         enemies (map #(enemy-turn board %) enemies)]
     (game-loop screen {:board board :player player :enemies enemies}))))

(defn get-initial-board
  []
  (-> population/starting-map
      (population/populate-square "#" {:x 50 :y 10} 10)
      (grid/assoc-grid 50 15 "-")
      (population/populate-grid-inplace "^" 10)))

(defn get-initial-state
  []
  {:board (get-initial-board)
   :inventory {}})

(defn get-debug-state
  []
  {:board (get-initial-board)
   :inventory {:iron 2 :wood 5}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [board (get-initial-board)]
    (ui/with-screen
      (fn [screen]
        (game-loop screen
                   {:board board
                    :player {:pos {:x 53 :y 15} :symbol "@"}
                    :enemies (population/populate-grid-return board "X" 5)})))))

(comment
  (-main)
  :rcf)
; DESIGN TODO:
; * how should objects be represented? stored?