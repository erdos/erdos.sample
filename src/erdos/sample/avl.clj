(ns erdos.sample.avl
  "AVL-tree implementation with custom meta information stored")

;;; (defn- print-avl [node] (str "(" (str val) ". " (if left (str left) "_") " " (if right (str right) "_")  " ... " m ")"))

(deftype AVLSimpleNode [left, right ;; left/right children
                        val          ;; value stored in node
                        ^int height  ;; height of node (0 for leaves)
                        m]           ;; meta map of node
  ;;; Object (toString [this] (print-avl this))
  clojure.lang.IObj
  (withMeta [_ m]
    (new AVLSimpleNode left right val height m))
  (meta [_] m))

(defmacro ^:private ->AVLSimpleNode [left right val]
  `(let [left# ^AVLSimpleNode ~left
         right# ^AVLSimleNode ~right
         val# ~val]
     (AVLSimpleNode. left# right# val#
        (inc (max (if left# (.height left#) 0)
                  (if right# (.height right#) 0)))
        nil)))

;; not in old clj.
(defmacro ^:private -?>
  ([x] x)
  ([x y & xs]
   (if (seq xs)
     `(let [x# ~x]
        (if (some? x#)
          (-?> (~y x#) ~@ xs)))
     x)))


(defn- balance-right [^AVLSimpleNode node fun]
  ;(assert (.right node))
  ;; (A (BC)) -> ((A B) C) when c is too heavy
  (fun (->AVLSimpleNode
        (fun (->AVLSimpleNode
              (-> node .left) (-> node .right .left) (-> node .val)))
        (-> node .right .right)
        (-> node .right .val))))


(defn- balance-left [^AVLSimpleNode node fun]
  ;(assert (.left node))
  ;; ((A B) C) -> (A (B C)) when a is too heavy
  (fun (->AVLSimpleNode
        (-> node .left .left)
        (fun (->AVLSimpleNode
              (-> node .left .right) (-> node .right) (-> node .val)))
        (-> node .left .val))))


(defn- balance-with-fn [^AVLSimpleNode node fun]
  (let [diff (- (if (.left node) (.height (.left node)) 0)
                (if (.right node) (.height (.right node)) 0))]
    (cond (= diff -2)
          (balance-right node fun)
          (= diff +2)
          (balance-left node fun)
          :else
          (fun node))))


(defn insert-with-fn [^AVLSimpleNode node x < fun]
  (if (nil? node)
    (fun (->AVLSimpleNode nil nil x))
    (->
     (if (< x (.val node))
       (let [ll (insert-with-fn (.left node) x < fun)]
         (->AVLSimpleNode ll (.right node) (.val node)))
       (let [rr (insert-with-fn (.right node) x < fun)]
         (->AVLSimpleNode (.left node) rr (.val node))))
     (balance-with-fn fun))))


(defn insert [^AVLSimpleNode node, x]
  (insert-with-fn node x < identity))

(defn insert-fn-sum [^AVLSimpleNode node, x]
  (insert-with-fn node x <
      (fn [node]
        (with-meta node
          {;:height (.height node)
           :sum (+ (-> node .left meta (:sum 0))
                   (-> node .right meta (:sum 0))
                   (or (.val node) 0))}))))

(defn avl-seq [^AVLSimpleNode node]
  (when node
    (cons (.val node) (concat (avl-seq (.left node)) (avl-seq (.right node))))))

(defn node-add-sum-count [^AVLSimpleNode node]
  (with-meta node
    {:sum (+ (-> node .left meta (:sum 0))
             (-> node .right meta (:sum 0))
             (or (second (.val node)) 0))
     :count (+ (-> node .left meta (:count 0))
               (-> node .right meta (:count 0))
               1)}))

(defn sample-tree [xs]
  ;; xs: pairs of value-probability
  (let [<<  (fn [a b] (pos? (compare (first a) (first b))))
        insert (fn [node x] (insert-with-fn node x << node-add-sum-count))
        sample (fn [^AVLSimpleNode
                   node ^double rand] ;; find by sum
                 ;(assert (some? node))
                 (let [left (-> node .left meta (:sum 0))
                       mid (-> node .val second)
                       right (-> node .right meta (:sum 0))]
                   ;(println rand " - " left mid right)
                   (cond (< rand left)
                         (recur (.left node) rand)
                         (< rand (+ left mid))
                         (first (.val node))
                         :rand<left+mid+right
                         (recur (.right node) (- rand left mid))
                         )))]
    ((fn f [tree]
       (reify
         Object
         (toString [_] (str "P" tree))
         clojure.lang.IDeref
         (deref [_] (sample tree (* (Math/random) (-> tree meta :sum))))
         clojure.lang.IPersistentCollection
         (count [_] (-> tree meta :count))
         (cons [_ x] (f (insert tree x)))
         (empty [_] nil)
         (equiv [x other] (= x other))
         (seq [_] (avl-seq tree))
         ))
     (reduce insert nil xs))))
;; (str (probability-tree {:a 1 :b 1 :c 1}))
;; (deref (probability-tree {:a 1 :b 1 :c 1}))

:good
