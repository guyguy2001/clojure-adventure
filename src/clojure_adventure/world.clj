(ns clojure-adventure.world
  (:require
   [clojure-adventure.grid :as grid]
   [clojure-adventure.utils :as utils]
   [clojure-adventure.vec2 :as vec2]
   [clojure-adventure.world-impl.entities-map :as entities-map]))


(defn new-world
  [base-grid]
  {:base-grid base-grid
   :objects {}})

; Todo: currently everything takes `state`, and that's gonna bite me in the ass if I have...
; not multiple levels, but rather multiple worlds - so I think I'm fine?

(defn -get-absolute-object-path
  "Returns the path you're supposed to use when accessing the state"
  [[type index]]
  [:objects type :data index])
  ; TODO - this doesn't use the entities_map abstractions at all.
  ; Maybe I should also provide an abstraction for the entire :objects dict?
  ; Maybe it should be in place of the current abstraction?

; TODO: Do I want this to return nil if the identifier is nil? I think so, due to clojure^TM
(defn get-object
  [world identifier]
  (get-in world (-get-absolute-object-path identifier))) ; TODO

(comment
  (require '[clojure-adventure.core :as core])
  (def world (:world core/initial-state))
  (get-object world [:players 0])
  (get-object world [:enemies 0])
  :rcf)

(defn get-paths-of-type
  [world type] ; TODO: arg ordering
  (let [map (get-in world [:objects type])
        keys (entities-map/entries-keys map)] ; TODO
    (mapv (fn [k] [type k]) keys))) ; TODO - entities map abstractions

(comment
  (require '[clojure-adventure.core :as core])
  (def world (:world core/initial-state))
  (get-paths-of-type world :players) ; [[:players 0]]
  (get-entries-of-type world :players)
  :rcf)

(defn get-entries-of-type
  [world type]
  (let [paths (get-paths-of-type world type)]
    (mapv (fn [path] [path (get-object world path)])
          paths)))

(defn spawn-objects
  [world type objects]
  (-> world
      (update-in [:objects type]
                 #(as-> % ents
                    (if (nil? ents)
                      entities-map/-empty-map
                      ents)
                    (reduce entities-map/insert ents objects)))))

(comment
  (-> (:world core/initial-state)
      (spawn-objects :players ["p1" "p2"])
      (get-entries-of-type :players))
  :rcf)

(defn despawn
  [world [type key]]
  (update-in world [:objects type]
             #(do
                (assert (entities-map/contains-entity % key))
                (entities-map/remove-entity % key))))

(defn update-object
  [world identifier f & args]
  (as-> world s
    (apply update-in s (-get-absolute-object-path identifier) f args)
    (if (nil? (get-in s (-get-absolute-object-path identifier)))
      (despawn s identifier)
      s)))

(defn dbg
  [x]
  (println x)
  x)

(defn get-object-list
  "Transforms {:players [a b] :enemies [d]} to ([[:players 0] a] [[:players 1] b] [[:enemies 0] d])
   [[:enemies 0] d] is basically a `[deep-key val]` map entry"
  [world]
  (->> (:objects world)
       (keys)
       (map #(get-paths-of-type world %))
       (apply concat)
       (map (fn [path] [path (get-object world path)])))) ; This didn't require changing - yay!

(comment
  (get-object-list {:objects {:players (entities-map/make-entities-map [:a :b])
                              :enemies (entities-map/make-entities-map [:c])}})
  :rcf)

; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
; TODO: There are 2 confliction ideas of `objects`: one is {:players [...] :enemies [...]}, and one is just a list of all the objects in the world.
(defn get-object-at-pos
  [world pos]
  (first (filter #(= pos (:pos (second %))) (get-object-list world))))

(comment
  (require '[clojure-adventure.core :as core])
  (get-object-at-pos (:world core/initial-state) (vec2/vec2 53 15))
  :rcf)



(defn get-player
  [world]
  (get-object world [:players 0]))


(defn -char-of-first
  [cell-items world]
  (:symbol (get-object world (first cell-items))))

(defn render-grid
  [grid world background-char]
  (grid/map-grid (fn [cell]
                   (or (-char-of-first cell world)
                       background-char))
                 grid))

(comment
  ; How do I test this?
  (require '[clojure-adventure.core :as core])
  (require '[clojure-adventure.ui :as ui])
  (require '[lanterna.screen :as s])
  (def screen (do
                  ; Start by cleaning the old screen 
                (when (bound? #'screen)
                  (s/stop (var-get #'screen)))
                (ui/setup-screen {:font-size 8})))
  (def state core/initial-state)
  (def object-entries (get-object-list (:world state)))
  object-entries
  (def starting-map (vec (repeat 30 (vec (repeat 100 [])))))
  (render-grid (grid/combine-to-grid starting-map object-entries) (:world state) ".")
  object-entries
  (->> (grid/combine-to-grid starting-map object-entries)
       (grid/grid-entries)
       (filter (fn [[k v]] (not= v []))))
  (ui/draw-grid screen (render-grid (grid/combine-to-grid starting-map object-entries) (:world state) "."))
  (s/redraw screen)
  :rcf)