(ns clojure-adventure.utils
  (:require
   [clojure.set :refer [union]]))

(defn fdbg
  [f]
  (fn [& args]
    (let [fname (-> f meta :name)
          _ (println (format "(%s %s) -> ?" fname args))
          result (apply f args)]
      (println (format "(%s %s) -> %s" fname args result))
      result)))

(defn eq-ignoring
  [m1 m2 & ignored-fields]
  (let [keys (union (set (keys m1)) (set (keys m2)))]
    (every? boolean (map #(or (some #{%} ignored-fields)
                              (= (get m1 %) (get m2 %)))
                         keys))))

(comment
  (union (set (keys {:a 4})) (set (keys {:a 5})))
  (contains? [:a :b] :a)
  (eq-ignoring {:a 5 :b 1} {:a 5 :b 2} :b)
  (let [ignored-fields [:b]
        m1 {:a 5 :b 1}
        m2 {:a 5 :b 2}
        keys (union (set (keys m1)) (set (keys m2)))]
    (every? boolean (map #(or (some #{%} ignored-fields)
                              (= (get m1 %) (get m2 %)))
                         keys)))
  :rcf)
