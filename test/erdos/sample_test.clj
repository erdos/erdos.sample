(ns erdos.sample-test
  (:require [clojure.test :refer :all]
            [erdos.sample :refer [sample-tree sample-seq]]))

(comment

 (do
   ;; two measurements for performance

   (def distrib {:a 42 :b 10 :c 48 :d 100
                 :e 100 :f 34 :G 34 :H 34
                 :i 34 :j 4 :k 3 :l 3 :m 32 :n 23
                 :o 2 :p 3 :q 3 :r 3})

   (def n 10000)

   (def tree (sample-tree distrib))

   (-> tree (assoc :a 11) (get :a))

   (get tree :a)

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

 )
