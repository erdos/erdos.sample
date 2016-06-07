(ns erdos.sample
  (:require [erdos.sample.avl :as avl]))

(defn sample-seq
  "Given a map of [sample probability] pairs, returns a lazy seq
  of random samples."
  [kps]
  (assert (map? kps))
  (assert (every? number? (vals kps)))
  (let [vkps (map float (vals kps))
        sum (reduce + 0.0 vkps)
        vs  (for [v vkps] (/ v sum))
        intervals (reductions + 0 vs)
        tree      (apply sorted-map (interleave intervals (keys kps)))]
    (repeatedly #(val (first (.seqFrom tree (rand) false))))))

(defn sample-tree [kps]
  (avl/sample-tree kps))


(comment

  (-> (let [tree (sample-tree {:a 42 :b 10 :c 48 :d 100})
          s (repeatedly #(deref tree))
            s (take 10000 s)] s)
      (frequencies)
      (time))

  (-> (let [s (sample-seq {:a 42 :b 10 :c 48 :d 100})
            s (take 10000 s)] s)
      (frequencies)
      (time))

  )
