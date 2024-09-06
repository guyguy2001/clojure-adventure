(ns clojure-adventure.core
  (:gen-class)
  (:require [lanterna.terminal :as t]
            [lanterna.screen :as s]
            [clojure.string :as str]))

(def starting-map (vec (repeat 10 (vec (repeat 100 "-")))))
(defn print-board
  [board screen]
  (dorun (map (fn [row y]
                (s/put-string screen 0 y (str/join "" row)))
              board (range))))

(defn combine-items-to-board
  [board items]
  (reduce (fn [board {{x :x y :y} :pos s :symbol}] (assoc-in board [y x] s))
          board items))

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
           (empty? second-layer) (combine-layers first-layer layers)
           (map? (first second-layer)) (combine-items-to-board first-layer second-layer)
           (vector? (first second-layer)) (combine-vec2-layers first-layer second-layer))]
     (apply combine-layers (into [combined-layer] layers)))))

(defn move-by
  [pos {x :x y :y}]
  (update
   (update pos :x (partial + x))
   :y (partial + y)))

(defn vec2
  [x y]
  {:x x :y y})

(def direction-by-input
  {:left (vec2 -1 0)
   :right (vec2 1 0)
   :up (vec2 0 -1)
   :down (vec2 0 1)})

(defn game-loop
  [screen {board :board player :player}]
  ((s/clear screen)
   (print-board (combine-layers board [player]) screen)
   (s/redraw screen)
   (let [input (s/get-key-blocking screen)
         direction (get direction-by-input input)
         player (if (not= direction nil)
                  (update player :pos #(move-by % direction))
                  player)]
     (game-loop screen {:board board :player player}))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [screen (s/get-screen :swing)]
    (s/start screen)
    (s/clear screen)
    (game-loop screen
               {:board starting-map :player {:pos {:x 50 :y 5} :symbol "@"}})
    (s/stop screen)))


