(ns clojure-adventure.ui
  (:require [lanterna.screen :as s]
            [clojure.string :as str]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.grid :as grid]))

; TODO: Are these inclusive?
(defprotocol Boundaries
  (left [this])
  (right [this])
  (top [this])
  (bottom [this]))

(defrecord Rect [x y width height]

  Boundaries
  (left [_this] x)
  (right [_this] (+ x width))
  (top [_this] y)
  (bottom [_this] (+ y height)))

(def grid-rect (->Rect 0 0 100 30))
(def inventory-rect (->Rect (inc (:width grid-rect)) 0
                            1000 (:height grid-rect)))
(def inventory-padding (vec2/vec2 1 1))
(def screen-rect (->Rect 0 0 140 40))

(defn setup-screen
  ([] (setup-screen {}))
  ([opts]
   (let [screen (s/get-screen :swing
                              (merge {:font-size 16
                                      :cols (:width screen-rect)
                                      :rows (:height screen-rect)}
                                     opts))]
     (s/start screen)
     (s/clear screen)
     screen)))

(defn with-screen [f]
  (let [screen (setup-screen)]
    (f screen)
    (s/stop screen)))

(defn get-input
  [screen]
  (s/get-key-blocking screen))

(defn draw-horizontal-line
  [screen y x1 x2]
  (doseq [x (range x1 x2)]
    (s/put-string screen x y "—")))

(defn draw-vertical-line
  [screen x y1 y2]
  (doseq [y (range y1 y2)]
    (s/put-string screen x y "|")))

(defn draw-world
  [screen world]
  (let [objects (:objects world)]
    println objects)
  (let [grid (apply grid/combine-layers (:base-grid world) (vals (:objects world)))] ; big todo :( also do I want this to depend on grid?
    (dorun (map (fn [row y]
                  (s/put-string screen 0 y (str/join "" row)))
                grid (range)))))



(defn render-bar
  [width fill-ratio]
  (let [inner-width (- width 2)
        num-full (* width fill-ratio)]
    (apply str (concat ["["] (repeat num-full "#") (repeat (- inner-width num-full) " ") ["]"]))))

(defn render-health-bar
  [width fill-ratio]
  (str "Health: " (render-bar width fill-ratio)))

(defn draw-interaaction-prompt
  [screen key item-name]
  (s/put-string screen
                10
                (+ 6 (bottom grid-rect))
                (format "[%s] %s" key item-name)))

(defn draw-bottom-pane
  [screen state]
  (s/put-string screen 10 (+ 2 (bottom grid-rect)) (render-health-bar 30 0.5))
  (when-let [item-name (:name (:interaction-focus state))]
    (draw-interaaction-prompt screen "X" item-name)))



(def inventory {:wood 2 :iron 2})

(defn show-item [type amount] (format "%s: %d" (name type) amount))

(defn draw-inventory
  [screen inventory]
  (doseq [[[type amount] i] (map vector inventory (range (count inventory)))]
    (s/put-string screen
                  (+ (left inventory-rect) (:x inventory-padding))
                  (+ (top inventory-rect) (:y inventory-padding) i)
                  (show-item type amount))))



; Right now this is a blob that receives all of the game's state and renders stuff.
; Do I want to couple the UI more with the logic? Something that will let me do
; a locality of behaviour between the state and the UI
(defn draw-screen
  [screen state]
  (s/clear screen)
  (draw-world screen (:world state))
  (draw-vertical-line screen (right grid-rect) (top screen-rect) (bottom screen-rect))
  (draw-inventory screen inventory)
  (draw-horizontal-line screen (bottom grid-rect) 0 (right grid-rect))
  (draw-bottom-pane screen state)
  (s/redraw screen))

(comment

  :rcf)
;; (defmacro with-screen
;;   {:clj-kondo/lint-as 'clj-kondo.lint-as/def-catch-all}
;;   [screen-name & body]
;;   `(let [~screen-name (s/get-screen :swing {:font-size 16 :cols 140 :rows 40})]
;;      (s/start ~screen-name)
;;      (s/clear ~screen-name)
;;      ~@body
;;      (s/stop ~screen-name)))
