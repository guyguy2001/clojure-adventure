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

(def cardinal-directions [vec2/left vec2/right vec2/up vec2/down])

(def direction-by-input
  {:left vec2/left
   :right vec2/right
   :up vec2/up
   :down vec2/down})

(defn enemy-turn
  [grid enemy]
  (update enemy :pos #(try-move-by grid % (rand-nth cardinal-directions))))

(defn game-loop
  [screen {board :board player :player enemies :enemies}]
  ((ui/draw-screen screen (grid/combine-layers board [player] enemies))
   (let [input (ui/get-input screen)
         direction (get direction-by-input input)
         player (if (not= direction nil)
                  (update player :pos #(try-move-by board % direction))
                  player)
         enemies (map #(enemy-turn board %) enemies)]
     (game-loop screen {:board board :player player :enemies enemies}))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [board (-> population/starting-map
                  (population/populate-square "#" {:x 50 :y 10} 10)
                  (population/populate-grid-inplace "^" 10))]
    (ui/with-screen
      (fn [screen]
        (game-loop screen
                   {:board board
                    :player {:pos {:x 50 :y 5} :symbol "@"}
                    :enemies (population/populate-grid-return board "X" 5)})))))

