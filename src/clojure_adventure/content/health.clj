(ns clojure-adventure.content.health)

(defn add-health-component
  [entity max-hp current-hp]
  (-> entity
      (assoc :hp {:current current-hp
                  :max max-hp})))

(defn has-health-component
  [entity]
  (contains? entity :hp))

(defn hp
  [entity]
  (get-in entity [:hp :current]))

(defn reduce-health
  [entity by]
  (update-in entity [:hp :current] - by))

(defn max-hp
  [entity]
  (get-in entity [:hp :max]))

(defn hp-ratio
  [entity]
  (/ (hp entity) (max-hp entity)))