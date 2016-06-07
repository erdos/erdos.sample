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
    (repeatedly #(val (first (.seqFrom ^clojure.lang.Sorted tree
                                       (rand) false))))))

(defn sample-tree [kps]
  (avl/sample-tree kps))


(comment

  (def distrib {:a 42 :b 10 :c 48 :d 100
                :e 100 :f 34 :G 34 :H 34
                :i 34 :j 4 :k 3 :l 3 :m 32 :n 23
                :o 2 :p 3 :q 3 :r 3})

  (def n 10000)

  (->> (let [tree (sample-tree distrib)]
         (repeatedly #(deref tree)))
       (take n)
       (frequencies)
       (time))

  (->> (sample-seq distrib)
      (take n)
      (frequencies)
      (time))

  )
