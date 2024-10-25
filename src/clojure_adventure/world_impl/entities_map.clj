(ns clojure-adventure.world-impl.entities-map
  (:refer-clojure :exclude [get]))

(def -empty-map {:next 0, :data {}})

(defn insert
  [map entity]
  (let [map (if (nil? map) -empty-map map)]
    (as-> map m
      (assoc-in m [:data (:next m)] entity)
      (update m :next inc))))

(defn contains-entity
  [map key]
  (contains? (:data map) key))

(defn remove-entity
  [map key]
  (update map :data dissoc key))

(comment
  (def example-map (-> -empty-map
                       (insert "foo")
                       (insert "bar")))
  example-map
  (remove-entity example-map 0)
  (remove-entity example-map 1)
  (remove-entity example-map 2)
  :rcf)

(defn make-entities-map
  [entities]
  (reduce insert -empty-map entities))

(defn entries-keys
  [map]
  (keys (:data map)))

(defn values
  [map]
  (vals (:data map)))

(defn entries
  [entities-map]
  (map (fn [[k v]] [k v]) (:data entities-map)))

(defn get
  [entities-map key]
  (get-in entities-map [:data key]))

(comment
  (def entities
    {:players {:next 2
               :data {0 "p1"
                      1 "p2"}}})
  (make-entities-map ["p1" "p2"])
  :rcf)