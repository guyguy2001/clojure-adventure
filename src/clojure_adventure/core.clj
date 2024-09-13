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


(defn evaluate-turn
  [state input]
  (let [direction (get direction-by-input input)]
    (-> state
        (update :player
                (fn [player]
                  (if (not= direction nil)
                    (update player :pos #(try-move-by (:board state) % direction))
                    player)))
        (assoc :enemies
               (map #(enemy-turn (:board state) %) (:enemies state)))
        (assoc :interaction-focus ;focus == the thing the player is interacting with
               nil))))

(defn game-loop
  [screen state]
  ((loop [state state]
     (ui/draw-screen screen {:board (grid/combine-layers (:board state) [(:player state)] (:enemies state))})
     (let [input (ui/get-input screen)]
       (recur (evaluate-turn state input))))))

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