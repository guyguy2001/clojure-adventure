(ns clojure-adventure.fiddles.clj.ui-fiddle
  {:clj-kondo/config
   '{:linters {:unused-namespace {:level :off}
               :inline-def {:level :off}}}}
  (:require [lanterna.screen :as s]
            [clojure.string :as str]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.ui :as ui :refer :all]
            [clojure-adventure.core :as core]
            [clojure-adventure.grid :as grid]))


(comment
  (def screen (setup-screen {:font-size 8}))
  (do ; def-initial-state
    (def objects [{:pos {:x 51 :y 13} :symbol "?"}])
    (def board (core/get-initial-board))
    (draw-screen screen {:board (grid/combine-layers board objects)}))
  
  
  (draw-horizontal-line screen (bottom grid-rect) 0 (right grid-rect))

  (s/put-string screen 10 (+ 2 (bottom grid-rect)) (render-health-bar 30 0.5))
  (s/redraw screen)
  (s/stop screen)
  :rcf)