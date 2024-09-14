(ns clojure-adventure.fiddles.clj.ui-fiddle
  {:clj-kondo/config
   '{:linters {:unused-namespace {:level :off}
               :inline-def {:level :off}
               :refer-all {:level :off}}}}
  (:require [lanterna.screen :as s]
            [clojure.string :as str]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.ui :as ui :refer :all]
            [clojure-adventure.core :as core]
            [clojure-adventure.grid :as grid]))


(comment
  (def screen (setup-screen {:font-size 8}))
  (do ; def-initial-state
    (def state (-> (core/get-initial-state)
                   (core/evaluate-turn :up)
                   (core/evaluate-turn :left)
                   (core/evaluate-turn :left)))
    (draw-screen screen state))
  (:board state)
  (:interaction-focus state)
  (core/get-interaction-focus-target (:board state) (:objects state) (get-in state [:player :pos]))
  (:interaction-focus (-> state (core/evaluate-turn :left)))


  (draw-horizontal-line screen (bottom grid-rect) 0 (right grid-rect))

  (s/put-string screen 10 (+ 2 (bottom grid-rect)) (render-health-bar 30 0.5))
  (s/redraw screen)
  (s/stop screen)
  :rcf)