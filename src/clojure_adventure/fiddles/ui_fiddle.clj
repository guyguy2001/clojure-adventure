(ns clojure-adventure.fiddles.ui-fiddle
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
  (def screen (do
                (when-let [old-screen (var-get (resolve 'screen))]
                  (s/stop old-screen))
                (setup-screen {:font-size 8})))

  (do ; def-initial-state
    (def state (-> (core/get-initial-state)
                   (core/evaluate-turn :up)
                   (core/evaluate-turn :left)
                   (core/evaluate-turn :left)
                   (core/evaluate-turn :x)))
    (draw-screen screen state))
  (do
    (def state (core/evaluate-turn state (ui/get-input screen)))
    (draw-screen screen state))
  (:board state)
  (def item-name (:name (:interaction-focus state)))
  (defn draw-interaction-prompt
    [key item-name]
    (s/put-string screen
                  10
                  (+ 6 (bottom grid-rect))
                  (format "[%s] %s" key item-name)))
  (s/redraw screen)


  (draw-horizontal-line screen (bottom grid-rect) 0 (right grid-rect))

  (s/put-string screen 10 (+ 2 (bottom grid-rect)) (render-health-bar 30 0.5))
  (s/redraw screen)
  (s/stop screen)
  :rcf)