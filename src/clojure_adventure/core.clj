(ns clojure-adventure.core
  (:gen-class)
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.ui :as ui]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.world :as world]))
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


(defn get-neighboaring-objects
  [state pos]
  (map (partial world/get-object-at-pos state)
       (grid/get-neighboars (get-in state [:world :base-grid]) pos)))

(comment
  (let [state (get-initial-state)]
    [(get-neighboaring-objects state (vec2/vec2 52 15))])
  :rcf)

(defn get-interaction-focus-target
  ;todo: name
  [state pos]
  (first ; This first is to take the "key" of [[:players 0] { object data ... }]
   (first (filter (comp not nil?)
                  (get-neighboaring-objects state pos)))))


(comment
  (get-neighboaring-objects (get-initial-state) {:x 53 :y 15})
  (get-interaction-focus-target
   (get-initial-state) {:x 51, :y 14})
  (world/get-object-list (get-initial-state))
  (:pos (get-player @*state))
  (:name (world/get-object @*state (:interaction-focus @*state)))
  :rcf)

; TODO: This version probably sucks for updating 2 things at once
; for example if the player fights an enemy and both need updating
; whoops
; I think I wouldn't feel like I need this if I just used ->
; with functions that receive the state, and live outside of
; this as global functions
; TODO I need to figure out how to base this on world/ abstractions
(defn apply-to-objects
  "actions: [:players (fn[state player] <new-player>)
             :enemies (fn[state enemy] <new-enemy>)]"
  [state actions]
  (reduce (fn [state [type f]]
            (let  [paths (world/get-paths-of-type state type)]
              (reduce (fn [state path]
                        (world/update-object state path
                                             (fn [obj] (f state obj))))
                      state paths)))
          state
          (partition 2 actions)))

(comment
  (apply-to-objects @*state
                    [:players (fn [{:keys [world]} player]
                                (if (not= :right nil)
                                  (update player :pos #(try-move-by world % (vec2/vec2 1 2)))
                                  player))

                     :enemies
                     (fn [{:keys [world]} enemy] (enemy-turn world enemy))])
  :rcf)

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
  [state]
  (world/get-object state [:players 0]))

(defn get-new-interaction-focus
  [state]
  (get-interaction-focus-target state (:pos (get-player state))))

; TODO: get despawning really working so that I have a limit on the copper
; Thought: I want to have the copper number flash green for a second after picking something up.
; The problem is that if I implement that with a thread, it would need to access the state, and will be blocked by the mutex.
; Actually, is that true? The game is computationally instant, only the input takes time.

; TODO: If my current focus is a copper ore, and an eenmy walks over, don't focus the enemy
(defn handle-mining
  [state action]
  (if (= action :interact)
    (let [target-path (:interaction-focus state)
          object (world/get-object state target-path)]
      (if (= (:symbol object) "C") ; Big todo
        (-> state
            (update-in [:inventory :copper] inc)
            (world/update-object
             target-path
             (fn [ore] (let [durability (dec (:durability ore))
                             ore (assoc ore :durability durability)
                             ore (if (<= durability 0)
                                   (assoc ore :dead true)
                                   ore)]
                         ore))))
        state))
    state))

(comment
  (let [ore (world/get-object @*state (:interaction-focus @*state))]
    (:dead ore))
  #(if (= 1 2) 3 %)
  :rcf)

(defn evaluate-turn
  [state_ input]
  (let [direction (get direction-by-input input)
        action (get action-by-input input)]
    ; TODO: Add an assert of something for invariants, like [:objects :enemies] being a vec and not a list.
    ; Or maybe abstract it completely so this part of the code can't fuck it up?
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
              :enemies (vec (population/populate-grid-return grid "X" 5))
              :other (vec (concat
                           [{:pos {:x 51 :y 13} :symbol "?"
                             :name "Spellbook"}]
                           (population/populate-grid-return-2 grid {:symbol "C" :name "Copper Ore" :durability 5} 10)))}}
     :interaction-focus nil
     :inventory {:iron 1 :copper 3}}))

(comment
  (def state (get-initial-state))
  state
  (def objects (get-in state [:objects]))
  (def pos (vec2/vec2 51 13))
  objects
  (map #(= pos (:pos %)) objects)
  (world/get-object-at-pos state (vec2/vec2 51 13))
  (get-interaction-focus-target state (vec2/vec2 51 12))
  (get-in state [:world :objects])
  (map (partial world/get-object-at-pos (:objects state))
       (grid/get-neighboars (:world state) (vec2/vec2 51 12)))

  (world/get-object-at-pos state (vec2/vec2 51 13))

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


(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  #_{:clj-kondo/ignore [:inline-def]}
  (defonce server (start-server :port 7888 :handler (nrepl-handler)))
  (ui/with-screen
    (fn [screen]
      (game-loop screen *state)))
  (stop-server server))

(comment
  (-main)
  (get-in @*state [:world :objects :players 0])
  (keys @*state)
  (reset! *state (get-initial-state))
  (get-new-interaction-focus @*state)
  (:pos (get-player @*state))
  :rcf)
; DESIGN TODO:
; * how should objects be represented? stored?


; I think that a lot of the issues I encountered during this refactor are caused by the fact that I accessed data directly instead of putting it behind abstractions - for example, by using get-in to get the data from the grid, instead of a grid/world abstraction, that would help me notice that I'm accessing the wrong thing, or would make it so I don't have the "wrong thing" to access in the first place