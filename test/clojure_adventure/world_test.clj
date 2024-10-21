(ns clojure-adventure.world-test
  (:require [clojure-adventure.grid :as grid]
            [clojure-adventure.population :as population]
            [clojure-adventure.vec2 :as vec2]
            [clojure-adventure.world :refer :all]
            [clojure.test :refer :all]))

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
  (as-> {:world (new-world (get-initial-world-grid))
         :interaction-focus nil
         :inventory {:iron 1 :copper 3}}
        state
    (spawn-objects state :players initial-player)
    (spawn-objects state :enemies initial-enemies)
    (spawn-objects
     state :other [{:pos {:x 51 :y 13} :symbol "?"
                    :name "Spellbook"}])))

(deftest get-object-test
  (testing "Testing get-object happy-flow usage"
    (is (= (get-object state [:players 0])
           {:pos {:x 53, :y 15}, :symbol "@"}), (= (get-object state [:players 0])
                                                   {:pos {:x 53, :y 15}, :symbol "@"}))
    (is (= (get-object state [:enemies 0])
           {:name nil, :pos {:x 80, :y 20}, :symbol "X"})))

  (testing "Testing that wrong keys return nil"
    (is (nil? (get-object state [:foo 0])))
    (is (nil? (get-object state [:foo -1])))
    (is (nil? (get-object state [:players 1])))
    (is (nil? (get-object state [:players 2])))
    (is (nil? (get-object state [:players -1])))))

(deftest get-paths-of-type-test
  (testing "Testing get-paths-of-type sanity usage"
    (is (= (get-paths-of-type state :players)
           [[:players 0]]))
    (is (= (get-paths-of-type state :enemies)
           [[:enemies 0]
            [:enemies 1]
            [:enemies 2]
            [:enemies 3]
            [:enemies 4]]))))

(deftest get-object-list-test
  (testing "Testing sanity usage"
    (is (=
         (get-object-list (-> {:world (new-world [])}
                              (spawn-objects :players [:a :b])
                              (spawn-objects :enemies [:c])))
         [[[:players 0] :a] [[:players 1] :b] [[:enemies 0] :c]]))))

(deftest get-object-at-pos-test
  (is (= (get-object-at-pos state (vec2/vec2 53 15))
         [[:players 0] {:pos {:x 53, :y 15}, :symbol "@"}])))


(deftest system-test
  (testing "Testing get-object-list"
    (let [objects (get-object-list state)
          players-items (filter #(= (get-in % [0 0]) :players) objects)
          players (mapv second players-items)]
      (is (= players [{:pos {:x 53 :y 15} :symbol "@"}]))))

  (testing "Testing based on apply-to-objects"
    (let [move-right (fn [player] (update player :pos vec2/add (vec2/vec2 0 1)))
          objects (get-paths-of-type state :enemies)
          new-state (reduce (fn [state path]
                              (update-object state path
                                             (fn [obj] (move-right obj))))
                            state objects)
          expected-enemies (mapv move-right initial-enemies)
          expected-items (mapv (fn [i v] [[:enemies i] v]) (range) expected-enemies)]
      ;; (is (= (get-in new-state [:world :objects :enemies])
      ;;        (mapv move-right initial-enemies))) ; TODO - revive this test. I'm not sure how to ask the question correctly. I probbaly need to store the entities map of the enemies at the start, instead of using the vec
      (is (= (->> (get-object-list new-state)
                  (filter #(= :enemies (get-in % [0 0]))))
             expected-items)))))

