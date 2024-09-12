(ns clojure-adventure.core
  (:gen-class)
  (:require [lanterna.screen :as s]
            [clojure.string :as str]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.vec2 :as vec2]))

(defn print-board
  [board screen]
  (dorun (map (fn [row y]
                (s/put-string screen 0 y (str/join "" row)))
              board (range))))

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
  ((s/clear screen)
   (print-board (grid/combine-layers board [player] enemies) screen)
   (s/redraw screen)
   (let [input (s/get-key-blocking screen)
         direction (get direction-by-input input)
         player (if (not= direction nil)
                  (update player :pos #(try-move-by board % direction))
                  player)
         enemies (map #(enemy-turn board %) enemies)]
     (game-loop screen {:board board :player player :enemies enemies}))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [screen (s/get-screen :swing)
        board (-> population/starting-map
                  (population/populate-square "#" {:x 50 :y 10} 10)
                  (population/populate-grid-inplace "^" 10))]
    (s/start screen)
    (s/clear screen)
    (game-loop screen
               {:board board
                :player {:pos {:x 50 :y 5} :symbol "@"}
                :enemies (population/populate-grid-return board "X" 5)})
    (s/stop screen)))

