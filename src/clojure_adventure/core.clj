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
  [board entity by]
  (let [moved (move-by entity by)]
    (if (= (get-in (:base-grid board) [(:y moved) (:x moved)]) "-")
      moved
      entity)))

(def direction-by-input
  {:left vec2/left
   :right vec2/right
   :up vec2/up
   :down vec2/down})

(defn enemy-turn
  [board enemy]
  (update enemy :pos #(try-move-by board % (rand-nth vec2/cardinal-directions))))


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

; TODO: This version probably sucks for updating 2 things at once
; for example if the player fights an enemy and both need updating
; whoops
; I think I wouldn't feel like I need this if I just used ->
; with functions that receive the state, and live outside of
; this as global functions
(defn apply-to-objects
  "actions: [:players (fn[state player] <new-player>)
             :enemies (fn[state enemy] <new-enemiy>)]"
  [state actions]
  (update-in state [:board :objects]
             (fn [objects]
               (reduce (fn [objects [key f]] (assoc objects key (map (partial f state) (key objects))))
                       objects
                       (partition 2 actions)))))

(comment
  ; TODO:
  ; make sure this function works
  ; I just fucked :interaction-focus
  ; I am sad now
  ; I need to see how people refactor in clojure
  (apply-to-objects
   (get-initial-state)
   [:enemies (fn [{:keys [board]} e] (enemy-turn board e))])
  (def actions [:enemies (fn [{:keys [board]} e] (enemy-turn board e))])
  (def state (get-initial-state))
  (let [objects (get-in state [:board :objects])
        ;; [key f] actions
        key :enemies
        objects {:enemies []}
        f (fn [{:keys [board]} e] (println e))]
    (assoc objects key (map (partial f state) objects))
    (map (partial f state) objects))
  :rcf)

(defn update-with-context
  "Like update, but the callback receives the entire map, not just the original value"
  [map key f]
  (assoc map key (f map)))

(defn get-player
  [board]
  (first (:players (:objects board))))

(defn get-new-interaction-focus
  [{board :board}] ; param is `state`
  (let [{:keys [base-grid objects]} board]
    (get-interaction-focus-target base-grid objects (:pos (get-player board)))))

(defn evaluate-turn
  [state_ input]
  (let [direction (get direction-by-input input)]
    (-> state_
        (apply-to-objects
         [:players (fn [{:keys [board]} player]
                     (if (not= direction nil)
                       (update player :pos #(try-move-by board % direction))
                       player))

          :enemies
          (fn [{:keys [board]} enemy] (enemy-turn board enemy))])

        (update-with-context
         :interaction-focus
         get-new-interaction-focus))))

(comment
  (def state (get-initial-state))
  (def direction (vec2/vec2 1 0))
  (def actions
    [:player (fn [{:keys [board]} player]
               (if (not= direction nil)
                 (update player :pos #(try-move-by board % direction))
                 player))

     :enemies
     (fn [{:keys [board]} enemy] (map #(enemy-turn board enemy)))])
  (apply-to-objects state actions)
  (get-initial-state)
  :rcf)

(defn game-loop
  [screen state]
  (loop [state state]
    (ui/draw-screen screen state)
    (let [input (ui/get-input screen)]
      (if (= input :delete)
        nil ; Exit the game
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
    ; attempt at terminology: board = the "wolrd map", including all of the objects, grid = 2d arrady
    ; walls and stuff like that are "dumb objects", since they don't have a payload (ther _is_ a wall in a certain spot, no more data than that).
    ; players, enemis, etc are smart objects.
    ; Question: the player lives in the board map because it is something that appears on the map.
    ; The inventory doesn't live there because it doesn't appear on the map.
    ; What gives?
    ; Potential answer: the player is an entity that needs to execute logic each turn, the board isn't.
    ; Potential answer: entities live on the board, resources live in the state.
    ; Potential alternative - have the board contain pointers to the objects that are on it, and let them live outside of it.
    ; I'm renaming`board` to `world` for now since this is getting out of freaking hand.
    {:board {:base-grid board
             :objects {:players [{:pos {:x 53 :y 15} :symbol "@"}] ; it's a singleton, but I want everything to be vecs I think.
                       :enemies (population/populate-grid-return board "X" 5)
                       :other [{:pos {:x 51 :y 13} :symbol "?"
                                :name "Spellbook"}]}}
     :interaction-focus nil
     :inventory {}}))

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

(defn get-debug-state1
  []
  {:board (get-initial-board)
   :inventory {:iron 2 :wood 5}})

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]

  (ui/with-screen
    (fn [screen]
      (game-loop screen (get-initial-state)))))

(comment
  (-main)
  :rcf)
; DESIGN TODO:
; * how should objects be represented? stored?


; I think that a lot of the issues I encountered during this refactor are caused by the fact that I accessed data directly instead of putting it behind abstractions - for example, by using get-in to get the data from the grid, instead of a grid/world abstraction, that would help me notice that I'm accessing the wrong thing, or would make it so I don't have the "wrong thing" to access in the first place