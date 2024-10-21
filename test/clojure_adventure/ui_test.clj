(ns clojure-adventure.ui-test
  (:require [clojure.test :refer :all]
            [clojure-adventure.ui :refer :all]
            [lanterna.screen :as s]))

(defn create-put-string-mock
  "[[screen x y name], ...]
     :_ is 'dontcare'"
  [expected-calls]
  (let [i (atom 0)]
    (fn [& args]
      (doseq [[arg expected-arg] (map vector args (expected-calls @i))]
        (assert (or (= expected-arg :_)
                    (= arg expected-arg))))
      (swap! i inc)
      nil)))

(deftest test-ui-output
  (testing "draw-inventory prints the right things"
    (with-redefs [s/put-string (create-put-string-mock
                                [[:screen :_ :_ (show-item :wood 1)]
                                 [:screen :_ :_ (show-item :iron 2)]])]
      (draw-inventory :screen {:wood 1 :iron 2} {}))))
