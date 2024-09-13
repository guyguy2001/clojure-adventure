(ns clojure-adventure.ui
  (:require [lanterna.screen :as s]
            [clojure.string :as str]
            [clojure-adventure.vec2 :as vec2]))

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

(defn draw-board
  [screen board]
  (dorun (map (fn [row y]
                (s/put-string screen 0 y (str/join "" row)))
              board (range))))



(defn render-bar
  [width fill-ratio]
  (let [inner-width (- width 2)
        num-full (* width fill-ratio)]
    (apply str (concat ["["] (repeat num-full "#") (repeat (- inner-width num-full) " ") ["]"]))))

(defn render-health-bar
  [width fill-ratio]
  (str "Health: " (render-bar width fill-ratio)))

(defn draw-bottom-pane
  [screen]
  (s/put-string screen 10 (+ 2 (bottom grid-rect)) (render-health-bar 30 0.5)))


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
  (draw-board screen (:board state))
  (draw-vertical-line screen (right grid-rect) (top screen-rect) (bottom screen-rect))
  (draw-inventory screen inventory)
  (draw-horizontal-line screen (bottom grid-rect) 0 (right grid-rect))
  (draw-bottom-pane screen)
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


(comment
;;   (macroexpand-1 '(with-screen screen (println 1)))
;;   (with-screen screen (println sc))
  :rcf)