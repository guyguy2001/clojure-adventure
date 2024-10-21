(ns clojure-adventure.world
  (:require [clojure-adventure.vec2 :as vec2]
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
  [:world :objects type :data index])
  ; TODO - this doesn't use the entities_map abstractions at all.
  ; Maybe I should also provide an abstraction for the entire :objects dict?
  ; Maybe it should be in place of the current abstraction?

(defn get-object
  [state identifier]
  (get-in state (-get-absolute-object-path identifier))) ; TODO

(comment
  (require '[clojure-adventure.core :as core])
  (def _state core/initial-state)
  (get-object _state [:players 0])
  (get-object _state [:enemies 0])
  :rcf)

(defn get-paths-of-type
  [state type] ; TODO: arg ordering
  (let [map (get-in state [:world :objects type])
        keys (entities-map/entries-keys map)] ; TODO
    (mapv (fn [k] [type k]) keys))) ; TODO - entities map abstractions

(comment
  (require '[clojure-adventure.core :as core])
  (def _state core/initial-state)
  (get-paths-of-type _state :players) ; [[:players 0]]
  :rcf)

(defn get-entries-of-type
  [state type]
  (let [paths (get-paths-of-type state type)]
    (mapv (fn [path] [path (get-object state path)]) paths)))

(defn spawn-objects
  [state type objects]
  (-> state
      (update-in [:world :objects type]
                 #(as-> % ents
                    (if (nil? ents)
                      entities-map/-empty-map
                      ents)
                    (reduce entities-map/insert ents objects)))))

(comment
  (spawn-objects core/initial-state :players ["p1" "p2"])
  :rcf)

(defn despawn
  [state [type key]]
  (update-in state [:world :objects type]
             #(do
                (assert (entities-map/contains-entity % key))
                (entities-map/remove-entity % key))))

(defn update-object
  [state identifier f & args]
  (as-> state s
    (apply update-in s (-get-absolute-object-path identifier) f args)
    (if (nil? (get-in s (-get-absolute-object-path identifier)))
      (despawn s identifier)
      s)))

(comment
  (map #(as-> @core/*state foo
          (despawn foo [:other %])
          (get-object-list foo)
          (count foo))
       (range 10))
  (get-paths-of-type core/new-state :other)
  (get-object-list @core/*state)
  :rcf)

(defn get-object-list
  "Transforms {:players [a b] :enemies [d]} to ([[:players 0] a] [[:players 1] b] [[:enemies 0] d])
   [[:enemies 0] d] is basically a `[deep-key val]` map entry"
  [state]
  (->> (get-in state [:world :objects])
       (keys)
       (map #(get-paths-of-type state %))
       (apply concat)
       (map (fn [path] [path (get-object state path)])))) ; This didn't require changing - yay!

(comment
  (get-object-list {:world {:objects {:players {:data [:a :b]} :enemies {:data [:c]}}}})
  :rcf)

; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
; TODO: There are 2 confliction ideas of `objects`: one is {:players [...] :enemies [...]}, and one is just a list of all the objects in the world.
(defn get-object-at-pos
  [state pos]
  (first (filter #(= pos (:pos (second %))) (get-object-list state))))

(comment
  (require '[clojure-adventure.core :as core])
  (get-object-at-pos core/initial-state (vec2/vec2 53 15))
  :rcf)



(defn get-player
  [state]
  (get-object state [:players 0]))