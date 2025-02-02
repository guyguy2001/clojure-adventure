(ns clojure-adventure.world-test
  (:require
   [clojure-adventure.parameters :as parameters]
   [clojure-adventure.population :as population]
   [clojure-adventure.vec2 :as vec2]
   [clojure-adventure.world :refer :all]
   [clojure.test :refer :all]))

(def initial-player [{:pos {:x 53 :y 15} :symbol "@"}])
(def initial-enemies [{:name nil, :pos {:x 80, :y 20}, :symbol "X"}
                      {:name nil, :pos {:x 44, :y 18}, :symbol "X"}
                      {:name nil, :pos {:x 49, :y 11}, :symbol "X"}
                      {:name nil, :pos {:x 17, :y 0}, :symbol "X"}
                      {:name nil, :pos {:x 81, :y 4}, :symbol "X"}])

(def -world
  (-> (new-world parameters/world-width parameters/world-height parameters/background-character)
      (population/populate-square :wall {:symbol "#"} {:x 50 :y 10} 10)
      (population/remove-all-in-cell (vec2/vec2 50 15))
      (spawn-objects :players initial-player)
      (spawn-objects :enemies initial-enemies)
      (spawn-objects :other [{:pos {:x 51 :y 13} :symbol "?"
                              :name "Spellbook"}])))

(deftest get-object-test
  (testing "Testing get-object happy-flow usage"
    (is (= (get-object -world [:players 0])
           {:pos {:x 53, :y 15}, :symbol "@", :type :players, :id [:players 0]}))
    (is (= (get-object -world [:enemies 0])
           {:name nil, :pos {:x 80, :y 20}, :symbol "X", :type :enemies, :id [:enemies 0]})))

  (testing "Testing that wrong keys return nil"
    (is (nil? (get-object -world [:foo 0])))
    (is (nil? (get-object -world [:foo -1])))
    (is (nil? (get-object -world [:players 1])))
    (is (nil? (get-object -world [:players 2])))
    (is (nil? (get-object -world [:players -1])))))

(deftest get-paths-of-type-test
  (testing "Testing get-paths-of-type sanity usage"
    (is (= (get-paths-of-type -world :players)
           [[:players 0]]))
    (is (= (get-paths-of-type -world :enemies)
           [[:enemies 0]
            [:enemies 1]
            [:enemies 2]
            [:enemies 3]
            [:enemies 4]]))))

(defn -with-pos
  ([x]
   {:value x
    :pos (vec2/vec2 0 0)})
  ([x [type _ :as id]]
   {:value x
    :pos (vec2/vec2 0 0)
    :type type
    :id id}))

(deftest get-object-list-test
  (testing "Testing sanity usage"
    (is (=
         (get-object-list (-> (new-world 1 1 ".")
                              (spawn-objects :players (map -with-pos [:a :b]))
                              (spawn-objects :enemies [(-with-pos :c)])))
         [(-with-pos :a [:players 0])
          (-with-pos :b [:players 1])
          (-with-pos :c [:enemies 0])]))))

;; (deftest get-object-at-pos-test
;;   (is (= (get-object-at-pos -world (vec2/vec2 53 15))
;;          [[:players 0] {:pos {:x 53, :y 15}, :symbol "@"}])))


(deftest system-test
  (testing "Testing get-object-list"
    (let [objects (get-object-list -world)
          players (filter #(= (get-in % [:id 0]) :players) objects)]
      (is (= players [{:pos {:x 53 :y 15} :symbol "@"
                       :id [:players 0] :type :players}]))))

  (testing "Testing based on apply-to-objects"
    (let [move-right (fn [player] (update player :pos vec2/add (vec2/vec2 0 1)))
          objects (get-paths-of-type -world :enemies)
          new-world (reduce (fn [world path]
                              (update-object world path
                                             (fn [obj] (move-right obj))))
                            -world objects)
          expected-enemies (mapv move-right initial-enemies)]
      ;; (is (= (get-in new-world [:world :objects :enemies])
      ;;        (mapv move-right initial-enemies))) ; TODO - revive this test. I'm not sure how to ask the question correctly. I probbaly need to store the entities map of the enemies at the start, instead of using the vec
      (is (= (->> (get-object-list new-world)
                  (filter #(= :enemies (get-in % [:id 0])))
                  (map (fn [entity] (dissoc entity :type :id)))) ; dissoc is for removing keys which are harder to compare
             expected-enemies)))))

