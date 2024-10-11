(ns clojure-adventure.core
  (:gen-class)
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.ui :as ui]
            #_[clojure.tools.nrepl.server
               :refer [start-server stop-server]]))
(require '[nrepl.server :refer [start-server stop-server]])

(defn dbg
  [val]
  (println val)
  val)

(defn move-by
  [pos {x :x y :y}]
  (-> pos
      (update :x (partial + x))
      (update :y (partial + y))))


(defn try-move-by
  [world entity by]
  (let [moved (move-by entity by)]
    (if (= (grid/get-grid (:base-grid world) (:x moved) (:y moved)) "-")
      moved
      entity)))

(def direction-by-input
  {:left vec2/left
   :right vec2/right
   :up vec2/up
   :down vec2/down})

(def action-by-input
  {\x :interact})

(defn enemy-turn
  [world enemy]
  (update enemy :pos #(try-move-by world % (rand-nth vec2/cardinal-directions))))


(defn get-object-list
  "Transforms {:players [a b c] :enemies [d]} to [a b c d]"
  [objects-dict]
  (->> objects-dict
       (map val)
       (apply concat)))

(comment
  (map val {1 2 :3 4})
  (get-object-list {:players [:a :b] :enemies [:c]})
  (concat [:a :b] [:c])
  :rcf)

; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
; TODO: There are 2 confliction ideas of `objects`: one is {:players [...] :enemies [...]}, and one is just a list of all the objects in the world.
(defn get-object-at-pos
  [objects pos]
  (first (filter #(= pos (:pos %)) (get-object-list objects))))


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
             :enemies (fn[state enemy] <new-enemy>)]"
  [state actions]
  (update-in state [:world :objects]
             (fn [objects]
               (reduce (fn [objects [key f]] (assoc objects key (mapv (partial f state) (key objects))))
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
   [:enemies (fn [{:keys [world]} e] (enemy-turn world e))])
  (def actions [:enemies (fn [{:keys [world]} e] (enemy-turn world e))])
  (def state (get-initial-state))
  (let [objects (get-in state [:world :objects])
        ;; [key f] actions
        key :enemies
        objects {:enemies []}
        f (fn [{:keys [world]} e] (println e))]
    (assoc objects key (map (partial f state) objects))
    (map (partial f state) objects))
  :rcf)

(defn update-with-context
  "Like update, but the callback receives the entire map, not just the original value"
  [map key f]
  (assoc map key (f map)))

(defn get-player
  [world]
  (first (:players (:objects world))))

(defn get-new-interaction-focus
  [{world :world}] ; param is `state`
  (let [{:keys [base-grid objects]} world]
    (get-interaction-focus-target base-grid objects (:pos (get-player world)))))

(defn handle-mining
  [state action]
  (if (= action :interact)
    (let [object (:interaction-focus state)]
      (if (= (:symbol object) "C") ; Big todo
        (update-in state [:inventory :copper] inc)
        state))
    state))

(defn evaluate-turn
  [state_ input]
  (let [direction (get direction-by-input input)
        action (get action-by-input input)]
    (-> state_
        (apply-to-objects
         [:players (fn [{:keys [world]} player]
                     (if (not= direction nil)
                       (update player :pos #(try-move-by world % direction))
                       player))

          :enemies
          (fn [{:keys [world]} enemy] (enemy-turn world enemy))])

        (update-with-context
         :interaction-focus
         get-new-interaction-focus)

        (handle-mining
         action))))

(comment
  (def state (get-initial-state))
  (def direction (vec2/vec2 1 0))
  (def actions
    [:player (fn [{:keys [world]} player]
               (if (not= direction nil)
                 (update player :pos #(try-move-by world % direction))
                 player))

     :enemies
     (fn [{:keys [world]} enemy] (map #(enemy-turn world enemy)))])
  (apply-to-objects state actions)
  (get-initial-state)
  :rcf)

(defn game-loop
  [screen *state]
  (loop []
    (ui/draw-screen screen @*state)
    (let [input (ui/get-input screen)]
      (if (= input :delete)
        nil ; Exit the game
        (do
          (reset! *state (evaluate-turn @*state input))
          (recur))))))

(defn get-initial-world-grid
  []
  (-> population/starting-map
      (population/populate-square "#" {:x 50 :y 10} 10)
      (grid/assoc-grid 50 15 "-")
      (population/populate-grid-inplace "^" 10)))

(defn get-initial-state
  []
  (let [grid (get-initial-world-grid)] ; TODO: this let is bad, and the board doesn't know the enemies aren't empty
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
    ; NOT to be confused with bevy's `world` - (:world state) is the container of the objects that live on the grid, some state (inrecation-focus for example) is outside of it
    {:world {:base-grid grid
             :objects
             {:players [{:pos {:x 53 :y 15} :symbol "@"}] ; it's a singleton, but I want everything to be vecs I think.
              :enemies (population/populate-grid-return grid "X" 5)
              :other (concat
                      [{:pos {:x 51 :y 13} :symbol "?"
                        :name "Spellbook"}]
                      (population/populate-grid-return grid "C" 10 "Copper Ore"))}}
     :interaction-focus nil
     :inventory {:iron 1 :copper 3}}))

(comment
  (def state (get-initial-state))
  state
  (def objects (get-in state [:objects]))
  (def pos (vec2/vec2 51 13))
  objects
  (map #(= pos (:pos %)) objects)
  (get-object-at-pos (get-in state [:objects]) (vec2/vec2 51 13))
  (get-interaction-focus-target (:base-grid (:world state)) (:objects (:world state)) (vec2/vec2 51 12))
  (get-in state [:world :objects])
  (map (partial get-object-at-pos (:objects state))
       (grid/get-neighboars (:world state) (vec2/vec2 51 12)))

  (get-object-at-pos (get-in state [:objects]) (vec2/vec2 51 13))

  :rcf)

(defn get-debug-state1
  []
  {:world {:base-grid (get-initial-world-grid)
           :objects {:players [{:pos {:x 53 :y 15} :symbol "@"}] ; it's a singleton, but I want everything to be vecs I think.
                     :enemies []
                     :other [{:pos {:x 51 :y 13} :symbol "?"
                              :name "Spellbook"}]}}
   :interaction-focus nil
   :inventory {:iron 2 :wood 5}})

(def *state (atom (get-initial-state)))
(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))

;; (defmacro with-server
;;   [& exprs]
;;   `(let [server# (start-server :port 40001)]
;;      ~@exprs
;;      (stop-server server#)))

(defonce server (start-server :port 7888 :handler (nrepl-handler)))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]

  (ui/with-screen
    (fn [screen]
      (game-loop screen *state)))
  (stop-server server))

(comment
  (-main)
  (get-in @*state [:world :objects :players 0])
  (keys @*state)
  (reset! *state (get-initial-state))
  :rcf)
; DESIGN TODO:
; * how should objects be represented? stored?


; I think that a lot of the issues I encountered during this refactor are caused by the fact that I accessed data directly instead of putting it behind abstractions - for example, by using get-in to get the data from the grid, instead of a grid/world abstraction, that would help me notice that I'm accessing the wrong thing, or would make it so I don't have the "wrong thing" to access in the first place