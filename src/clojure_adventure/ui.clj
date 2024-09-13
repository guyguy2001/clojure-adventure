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
(def inventory-rect (->Rect (:width grid-rect) 0
                            1000 (:height grid-rect)))
(def inventory-padding (vec2/vec2 10 20))
(def screen-rect (->Rect 0 0 140 40))

(defn with-screen [f]
  (let [screen (s/get-screen :swing {:font-size 16 :cols (:width screen-rect) :rows (:height screen-rect)})]
    (s/start screen)
    (s/clear screen)
    (f screen)
    (s/stop screen)))

(defn get-input
  [screen]
  (s/get-key-blocking screen))

(defn draw-vertical-line
; todo: copied
  [screen x y1 y2]
  (doall
   (map
    (fn [y] (s/put-string screen x y "|"))
    (range y1 y2))))

(defn draw-board
  [screen board]
  (dorun (map (fn [row y]
                (s/put-string screen 0 y (str/join "" row)))
              board (range))))

(def inventory {:wood 2 :iron 2})

(defn show-item [type amount] (format "%s: %d" (name type) amount))

(defn draw-inventory
  [screen inventory]
  (doseq [[type amount] inventory]
    (s/put-string screen
                  (+ (left inventory-rect) (:x inventory-padding))
                  (+ (top inventory-rect) (:y inventory-padding))
                  (show-item type amount))))


(defn draw-screen
  [screen board]
  (s/clear screen)
  (draw-board screen board)
  (draw-vertical-line screen (right grid-rect) (top screen-rect) (bottom screen-rect))
  (draw-inventory screen inventory)
  (s/redraw screen))

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