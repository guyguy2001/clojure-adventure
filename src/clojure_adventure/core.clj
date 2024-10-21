(ns clojure-adventure.core
  (:gen-class)
  (:require [clojure-adventure.actions :as actions]
            [clojure-adventure.combat :as combat]
            [clojure-adventure.grid :as grid]
            [clojure-adventure.movement :as movement]
            [clojure-adventure.notifications :as notifications]
            [clojure-adventure.population :as population]
            [clojure-adventure.ui :as ui]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.world :as world]))
(require '[nrepl.server :refer [start-server stop-server]])

(defn dbg
  [val]
  (println val)
  val)


(def direction-by-input
  {:left vec2/left
   :right vec2/right
   :up vec2/up
   :down vec2/down})

(def action-by-input
  {\x :interact
   \f :fireball})

(defn enemy-turn
  [world enemy]
  (movement/try-move-by world enemy (rand-nth vec2/cardinal-directions)))


(defn get-neighboaring-objects
  [state pos]
  (map (partial world/get-object-at-pos state)
       (grid/get-neighboars (get-in state [:world :base-grid]) pos)))

(comment
  (let [state initial-state]
    [(get-neighboaring-objects state (vec2/vec2 52 15))])
  :rcf)

(defn get-interaction-focus-target
  ;todo: name
  [state pos]
  (first ; This first is to take the "key" of [[:players 0] { object data ... }]
   (first (filter (comp not nil?)
                  (get-neighboaring-objects state pos)))))


(comment
  (get-neighboaring-objects initial-state {:x 53 :y 15})
  (get-interaction-focus-target
   initial-state {:x 51, :y 14})
  (world/get-object-list initial-state)
  (:pos (world/get-player @*state))
  (:name (world/get-object @*state (:interaction-focus @*state)))
  :rcf)


(defn get-new-interaction-focus
  [state]
  (get-interaction-focus-target state (:pos (world/get-player state))))

; TODO: get despawning really working so that I have a limit on the copper
; Thought: I want to have the copper number flash green for a second after picking something up.
; The problem is that if I implement that with a thread, it would need to access the state, and will be blocked by the mutex.
; Actually, is that true? The game is computationally instant, only the input takes time.

; TODO: If my current focus is a copper ore, and an enemy walks over, don't focus the enemy
(defn handle-mining
  [state action]
  (if (= action :interact)
    (let [target-path (:interaction-focus state)
          object (world/get-object state target-path)]
      (if (= (:symbol object) "C") ; Big todo
        (-> state
            (update-in [:inventory :copper] inc)
            (assoc-in [:notifications :inventory :copper] :resource-added)
            (world/update-object
             target-path
             (fn [ore] (let [durability (dec (:durability ore))
                             ore (assoc ore :durability durability)
                             ore (if (<= durability 0)
                                   nil
                                   ore)]
                         ore))))
        state))
    state))

(comment
  (let [ore (world/get-object @*state (:interaction-focus @*state))]
    (:dead ore))
  #(if (= 1 2) 3 %)
  (def new-state (-> @*state
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)
                     (handle-mining :interact)))
  (ui/draw-screen @*screen new-state)
  (get-in new-state [:world :objects :other])
  (map second (world/get-object-list new-state))
  :rcf)

(defn evaluate-turn
  [state_ input]
  (let [direction (get direction-by-input input)
        action (get action-by-input input)]
    ; TODO: Add an assert of something for invariants, like [:objects :enemies] being a vec and not a list.
    ; Or maybe abstract it completely so this part of the code can't fuck it up?
    (-> state_
        (update :notifications notifications/clear-notifications)
        (actions/apply-to-objects
         [:players (fn [{:keys [world]} player]
                     (if (not= direction nil)
                       (movement/try-move-by world player direction)
                       player))

          :enemies
          (fn [{:keys [world]} enemy] (enemy-turn world enemy))])

        (actions/update-with-context
         :interaction-focus
         get-new-interaction-focus)

        (handle-mining
         action)

        (combat/handle-combat action))))

(comment
  (def state initial-state)
  (def direction (vec2/vec2 1 0))
  (def actions
    [:player (fn [{:keys [world]} player]
               (if (not= direction nil)
                 (movement/try-move-by world player direction)
                 player))

     :enemies
     (fn [{:keys [world]} enemy] (map #(enemy-turn world enemy)))])
  (actions/apply-to-objects state actions)
  initial-state
  :rcf)

(defn game-loop
  [*screen *state]
  (ui/draw-screen @*screen @*state)
  (loop []

    (let [input (ui/get-input @*screen)]
      (if (= input :delete)
        nil ; Exit the game
        (do
          (try
            (reset! *state (evaluate-turn @*state input))
            (ui/draw-screen @*screen @*state)
            (catch Exception e
              (binding [*out* *err*]
                (println "Encountered exception while running the turn:" e))))
          (recur))))))

(defn get-initial-world-grid
  []
  (-> population/starting-map
      (population/populate-square "#" {:x 50 :y 10} 10)
      (grid/assoc-grid 50 15 grid/empty-cell)
      (population/populate-grid-inplace "^" 10)))

; TODO: This still accesses :world :base-grid
(def initial-state
  (as-> {:world (world/new-world (get-initial-world-grid))
         :interaction-focus nil
         :inventory {:iron 1 :copper 3}
         :notifications (notifications/new-queue)}
        state
    (world/spawn-objects state :players [{:pos {:x 53 :y 15}
                                          :facing-direction (vec2/vec2 1 0)
                                          :symbol "@"}])
    (world/spawn-objects state :enemies (population/populate-grid-return
                                         (get-in state [:world :base-grid]) "X" 5))
    (world/spawn-objects
     state :other  (concat
                    [{:pos {:x 51 :y 13} :symbol "?"
                      :name "Spellbook"}]
                    (population/populate-grid-return-2
                     (get-in state [:world :base-grid])
                     {:symbol "C" :name "Copper Ore" :durability 5} 10)))))

(comment
  (reset! *state initial-state)
  (def state initial-state)
  state
  (def objects (get-in state [:objects]))
  (def pos (vec2/vec2 51 13))
  objects
  (map #(= pos (:pos %)) objects)
  (world/get-object-at-pos state (vec2/vec2 51 13))
  (get-interaction-focus-target state (vec2/vec2 51 12))
  (get-in state [:world :objects])
  (map (partial world/get-object-at-pos (:objects state))
       (grid/get-neighboars (get-in state [:world :base-grid]) (vec2/vec2 51 12)))

  (world/get-object-at-pos state (vec2/vec2 51 13))

  :rcf)

(def *state (atom initial-state))
(def *screen (atom nil))

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
      (reset! *screen screen)
      (game-loop *screen *state)))
  (stop-server server))

(comment
  (-main)
  (get-in @*state [:world :objects :players :data 0])
  (keys @*state)
  (reset! *state initial-state)
  (get-new-interaction-focus @*state)
  (:pos (world/get-player @*state))
  (combat/handle-combat @*state :fireball)
  (world/get-object @*state [:fireball 0])
  (world/get-entries-of-type @*state :fireball)
  :rcf)

; I think that a lot of the issues I encountered during this refactor are caused by the fact that I accessed data directly instead of putting it behind abstractions - for example, by using get-in to get the data from the grid, instead of a grid/world abstraction, that would help me notice that I'm accessing the wrong thing, or would make it so I don't have the "wrong thing" to access in the first place