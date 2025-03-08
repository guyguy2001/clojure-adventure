(ns clojure-adventure.content.layers)
; TODO: Maybe replace this with collision layers
(def players :players)
(def enemies :enemies)
(def bullets :bullets)


(defn add-collision-component
  [entity {layers :layers mask :mask}]
  (-> entity
      (assoc :collision-layers (set layers))
      (assoc :collision-mask (set mask))))

(comment
  (add-collision-component {} {:layers #{1 2 3} :mask [4 5 7]})
  :rcf)

(defn add-layer
  "(add-faction ent :player)"
  [entity layer]
  (update entity :collision-layers
          #(conj (or % #{}) layer)))

(comment
  (-> {}
      (add-layer :foo)
      (add-layer :bar))
  :rcf)

(defn layers
  [entity]
  (:collision-layers entity))

(comment
  (-> {}
      (add-layer :foo)
      (add-layer :bar)
      (layers))
  :rcf)


(defn add-collision-mask
  [entity layer]
  (update entity :collision-mask
          #(conj (or % #{}) layer)))

(defn collision-mask
  [entity]
  (:collision-mask entity))

(comment
  (add-collision-mask {} :foo)
  (-> {}
      (add-collision-mask :foo)
      (add-collision-mask :bar)
      (collision-mask))
  :rcf)

(defn add-enemy-layers
  [entity layers]
  (assoc entity :enemy-layers (set layers)))

(defn get-enemy-layers
  [entity]
  (:enemy-layers entity))

(comment
  (-> {}
      (add-enemy-layers #{:foo :bar})
      (get-enemy-layers))
  :rcf)