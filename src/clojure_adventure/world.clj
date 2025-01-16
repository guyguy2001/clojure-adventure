(ns clojure-adventure.world
  (:require
   [clojure-adventure.grid :as grid]
   [clojure-adventure.vec2 :as vec2]
   [clojure-adventure.world-impl.entities-map :as entities-map]
   [clojure-adventure.world-impl.ids-grid :as ids-grid]))


; TODO: Only change hte parameters in this commit, make base-grid be generated here, and in the next commit make it unneeded

(defn new-world
  [width height background-symbol]
  {:background-symbol background-symbol
   :new-grid (grid/grid-of width height [])
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

; TODO: I think the whole consept of entities-map is too complicated.
; I should just use a uuid for each thing. (get-in world [:objects uuid]) would be the privat impl,
; and if i want the type I can just ask for it from there.
(defn spawn-object
  [world type object]
  (assert (not (nil? (:pos object))))
  (let [pos (:pos object)
        [new-map short-id] (entities-map/insert
                            (get-in world [:objects type])
                            object)
        full-id [type short-id]
        new-map (entities-map/update-entity new-map short-id
                                            #(-> %
                                                 (assoc :type type)
                                                 (assoc :id full-id)))]
    (-> world
        (assoc-in [:objects type]
                  new-map)
        (update :new-grid ids-grid/insert-id pos full-id))))

(defn spawn-objects
  [world type objects]
  (reduce (fn [world obj] (spawn-object world type obj))
          world objects))

(comment
  (-> (:world core/initial-state)
      (spawn-objects :players ["p1" "p2"])
      (get-entries-of-type :players))
  :rcf)

(defn despawn
  [world [type key :as id]]
  (if (not (entities-map/contains-entity (get-in world [:objects type]) key))
    world ; TODO: read up on what to do if I try to despawn an object twice.
    (let [pos (:pos (get-object world id))]
      (-> world
          (update-in [:objects type]
                     #(do
                        (assert (entities-map/contains-entity % key))
                        (entities-map/remove-entity % key)))
          (update :new-grid #(ids-grid/remove-id % pos id))))))

(defn update-object
  [world identifier f & args]
  (let [path (-get-absolute-object-path identifier)
        new-object (apply f (get-in world path) args)]
    (if (nil? new-object)
      (despawn world identifier)
      (assoc-in world path new-object))))

; TODO: rename?
(defn positions
  [world]
  (grid/keys-grid (:new-grid world)))

(comment
  (positions (new-world 5 3 "?"))
  :rcf)

(defn dbg
  [x]
  (println x)
  x)

(defn get-object-list
  "Transforms {:players [a b] :enemies [d]} to (a b d)"
  [world]
  (->> (:objects world)
       (keys)
       (map #(get-paths-of-type world %))
       (apply concat)
       (map #(get-object world %))))


(comment
  (get-object-list {:objects {:players (entities-map/make-entities-map [:a :b])
                              :enemies (entities-map/make-entities-map [:c])}})

  (require '[clojure-adventure.core :as core])
  (get-object-list (:world core/initial-state))
  :rcf)


(defn width
  [world]
  (grid/width (:new-grid world)))

(defn height
  [world]
  (grid/height (:new-grid world)))

(comment
  (require '[clojure-adventure.core :as core])
  (width (:world core/initial-state))
  (height (:world core/initial-state))
  :rcf)


; TODO: I need to create an abstraction of the grid and the objects on it (layers).
; both for performance (O(1) access from xy to object), and also logical abstraction
; TODO: There are 2 confliction ideas of `objects`: one is {:players [...] :enemies [...]}, and one is just a list of all the objects in the world.
(defn get-objects-at-pos
  [world pos]
  (filter #(= pos (:pos %)) (get-object-list world)))

(defn get-object-at-pos
  [world pos]
  (first (get-objects-at-pos world pos)))

(comment
  (require '[clojure-adventure.core :as core])
  (get-object-at-pos (:world core/initial-state) (vec2/vec2 53 15))
  :rcf)

(defn get-neighboaring-objects
  [world pos]
  (map #(get-object-at-pos world %)
       (grid/get-neighboaring-cells (:new-grid world) pos)))

(comment
  (require '[clojure-adventure.core :as core])
  (let [world (:world core/initial-state)]
    (get-neighboaring-objects world (vec2/vec2 52 15)))
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

(defn ensure-invariants
  [world]
  ; Make sure that all entities have an id, a type, and a pos
  (doseq [entity (get-object-list world)]
    (assert (not (nil? (:pos entity))) (format "Entity missing an :pos - %s" entity))
    (assert (not (nil? (:id entity))) (format "Entity missing an :id - %s" entity))
    (assert (not (nil? (:type entity))) (format "Entity missing an :type - %s" entity)))
  world)

(comment
  ; How do I test this?
  (require '[clojure-adventure.core :as core]
           '[clojure-adventure.ui :as ui]
           '[lanterna.screen :as s])
  (def screen (do
                  ; Start by cleaning the old screen 
                (when (bound? #'screen)
                  (s/stop (var-get #'screen)))
                (ui/setup-screen {:font-size 8})))
  (def state @core/*state)
  (def objects (get-object-list (:world state)))
  (def starting-map (vec (repeat 30 (vec (repeat 100 [])))))
  (->> (grid/combine-to-grid starting-map objects)
       (grid/grid-entries)
       (filter (fn [[_k v]] (not= v []))))
  (ui/draw-grid screen (render-grid (grid/combine-to-grid starting-map objects) (:world state) "."))
  (s/redraw screen)
  :rcf)

(defn is-cell-empty
  [world pos]
  (grid/is-cell-empty (:new-grid world) pos))

(defn move-to
  [world entity-path pos]
  (let [old-pos (:pos (get-object world entity-path))]
    (-> world
        (update-object entity-path assoc :pos pos)
        (update :new-grid #(-> %
                               (ids-grid/remove-id old-pos entity-path)
                               (ids-grid/insert-id pos entity-path))))))
