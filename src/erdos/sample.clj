(ns ^{:author "Janos Erdos"} erdos.sample
    (:require [erdos.sample.avl :as avl]))


(defn sample-seq
  "Given a map of [sample probability] pairs, returns a lazy seq
  of random samples."
  [kps]
  (assert (map? kps))
  (assert (every? number? (vals kps)))
  (let [vkps (map float (vals kps))
        sum (reduce + vkps)
        vs  (for [v vkps] (/ v sum))
        intervals (reductions + 0 vs)
        tree      (apply sorted-map (interleave intervals (keys kps)))]
    (repeatedly #(val (first (.seqFrom ^clojure.lang.Sorted tree
                                       (rand) false))))))


(defn sample-tree
  "Given a map of [sample probability] pairs, returns an object
  that returns random sample when deref-ed.

  The returned object has support for fast conj, disj, count, assoc, dissoc, get, deref, count."
  [kps] (avl/sample-tree kps))
