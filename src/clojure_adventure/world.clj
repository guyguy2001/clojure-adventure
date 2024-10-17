(ns clojure-adventure.world
  (:require [clojure-adventure.vec2 :as vec2]))

; Todo: currently everything takes `state`, and that's gonna bite me in the ass if I have...
; not multiple levels, but rather multiple worlds - so I think I'm fine?

(defn -get-absolute-object-path
  "Returns the path you're supposed to use when accessing the state"
  [relative-path]
  (concat [:world :objects] relative-path))

(defn get-object
  [state identifier]
  (get-in state (-get-absolute-object-path identifier)))

(comment
  (require '[clojure-adventure.core :as core])
  (def _state (core/get-initial-state))
  (get-object _state [:players 0])
  (get-object _state [:enemies 0])
  :rcf)

(defn update-object
  [state identifier f & args]
  (apply update-in state (-get-absolute-object-path identifier) f args))

(defn get-paths-of-type
  [state type] ; TODO: arg ordering
  (let [objects (get-in state [:world :objects type])
        paths (mapv (fn [i _obj] [type i]) (range) objects)]
    paths))

(comment
  (require '[clojure-adventure.core :as core])
  (def _state (core/get-initial-state))
  (get-paths-of-type _state :players) ; [[:players 0]]
  :rcf)

(defn get-object-list
  "Transforms {:players [a b] :enemies [d]} to ([[:players 0] a] [[:players 1] b] [[:enemies 0] d])
   [[:enemies 0] d] is basically a `[deep-key val]` map entry"
  [state]
  (->> (get-in state [:world :objects])
       (keys)
       (map #(get-paths-of-type state %))
       (apply concat)
       (map (fn [path] [path (get-object state path)]))))

(comment
  (get-object-list {:world {:objects {:players [:a :b] :enemies [:c]}}})
  :rcf)

; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
; TODO: There are 2 confliction ideas of `objects`: one is {:players [...] :enemies [...]}, and one is just a list of all the objects in the world.
(defn get-object-at-pos
  [state pos]
  (first (filter #(= pos (:pos (second %))) (get-object-list state))))

(comment
  (require '[clojure-adventure.core :as core])
  (get-object-at-pos (core/get-initial-state) (vec2/vec2 53 15))
  :rcf)