(ns clojure-adventure.core
  (:gen-class)
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.ui :as ui]))


(defn move-by
  [pos {x :x y :y}]
  (-> pos
      (update :x (partial + x))
      (update :y (partial + y))))


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


; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
(defn get-object-at-pos
  [objects pos]
  (first (filter #(= pos (:pos %)) objects)))


(defn get-neighboaring-objects
  [grid objects pos] ; todo: is grid neccessary?
  (map (partial get-object-at-pos objects)
       (grid/get-neighboars grid pos)))

(defn get-interaction-focus-target
  ;todo: name
  [grid objects pos]
  (first (filter (comp not nil?)
                 (get-neighboaring-objects grid objects pos))))

(defn evaluate-turn
  [_state input]
  (let [direction (get direction-by-input input)]
    (reduce (fn [state f] (f state))
            _state
            [(fn [state]
               (update state :player
                       (fn [player]
                         (if (not= direction nil)
                           (update player :pos #(try-move-by (:board state) % direction))
                           player))))
             (fn [state]
               (assoc state
                      :enemies
                      (map #(enemy-turn (:board state) %) (:enemies state))))
             (fn [state]
               (assoc state
                      :interaction-focus ;focus == the thing the player is interacting with
                      (get-interaction-focus-target (:board state)
                                                    (:objects state)
                                                    (get-in state [:player :pos]))))])))

(defn game-loop
  [screen state]
  ((loop [state state]
     (ui/draw-screen screen state)
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
  (let [board (get-initial-board)] ; TODO: this let is bad, and the board doesn't know the enemies aren't empty
    {:board board
     :player {:pos {:x 53 :y 15} :symbol "@"}
     :enemies (population/populate-grid-return board "X" 5)
     :interaction-focus nil
     :inventory {}
     :objects [{:pos {:x 51 :y 13} :symbol "?"}]}))

(comment
  (def state (get-initial-state))
  state
  (def objects (get-in state [:objects]))
  (def pos (vec2/vec2 51 13))
  objects
  (map #(= pos (:pos %)) objects)
  (get-object-at-pos (get-in state [:objects]) (vec2/vec2 51 13))
  (get-interaction-focus-target (:board state) (:objects state) (vec2/vec2 51 12))
  (map (partial get-object-at-pos (:objects state))
       (grid/get-neighboars (:board state) (vec2/vec2 51 12)))

  (get-object-at-pos (get-in state [:objects]) (vec2/vec2 51 13))

  :rcf)

(defn get-debug-state
  []
  {:board (get-initial-board)
   :inventory {:iron 2 :wood 5}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (ui/with-screen
    (fn [screen]
      (game-loop screen (get-initial-state)))))

(comment
  (-main)
  :rcf)
; DESIGN TODO:
; * how should objects be represented? stored?