(ns clojure-adventure.utils)

(defn fdbg
  [f]
  (fn [& args]
    (let [fname (-> f meta :name)
          _ (println (format "(%s %s) -> ?" fname args))
          result (apply f args)]
      (println (format "(%s %s) -> %s" fname args result))
      result)))