(ns clojure-adventure.world-impl.entities-map)

(def -empty-map {:next 0, :data {}})

(defn insert
  [map entity]
  (as-> map m
    (assoc-in m [:data (:next m)] entity)
    (update m :next inc)))

(defn make-entities-map
  [entities]
  (reduce insert -empty-map entities))

(defn values
  [map]
  (vals (:data map)))

(defn entries
  [entities-map]
  (map (fn [[k v]] [k v]) entities-map))

(defn get-entity
  [entities-map key]
  (get-in entities-map [:data key]))

(comment
  (def entities
    {:players {:next 2
               :data {0 "p1"
                      1 "p2"}}})
  (make-entities-map ["p1" "p2"])
  :rcf)