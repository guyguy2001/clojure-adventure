(ns clojure-adventure.population-test
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.world :as world]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]))

(defn get-initial-world-grid
  []
  (-> population/starting-map
      (population/populate-square "#" {:x 50 :y 10} 10)
      (grid/assoc-grid 50 15 grid/empty-cell)
      ;(population/populate-grid-inplace "^" 10) ; Removed because of randomness
      ))

(def initial-player [{:pos {:x 53 :y 15} :symbol "@"}])
(def initial-enemies [{:name nil, :pos {:x 80, :y 20}, :symbol "X"}
                      {:name nil, :pos {:x 44, :y 18}, :symbol "X"}
                      {:name nil, :pos {:x 49, :y 11}, :symbol "X"}
                      {:name nil, :pos {:x 17, :y 0}, :symbol "X"}
                      {:name nil, :pos {:x 81, :y 4}, :symbol "X"}])

(def state
  {:world (as-> (world/new-world (get-initial-world-grid)) world
            (world/spawn-objects world :players initial-player)
            (world/spawn-objects world :enemies initial-enemies)
            (world/spawn-objects
             world :other [{:pos {:x 51 :y 13} :symbol "?"
                            :name "Spellbook"}]))
   :interaction-focus nil
   :inventory {:iron 1 :copper 3}})

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

; NOTE: This seems to only check the hard-coded ascii functions, not the place-enemies functions.
; I should get into seeded randomness...
(deftest initial-map-generation-test
  (testing "That spawning the initial map doesn't get regressed"
    (let [expected-state (load-edn "test/clojure_adventure/population_test_data/expected_state.edn")]
      (is (= state expected-state))))
  (testing "Testing that rendering the initial state doesn't get regressed"
    (let [expected-render (load-edn "test/clojure_adventure/population_test_data/expected_combined_grid.edn")
          ; Taken from ui.clj:
          actual-render (grid/combine-layers (:base-grid (:world state)) (map second (world/get-object-list (:world state))))]
      (is (= expected-render actual-render)))))

(comment
  (grid/combine-layers (:base-grid (:world state)) (map second (world/get-object-list (:world state))))
  :rcf)
