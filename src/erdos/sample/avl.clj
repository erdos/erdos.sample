(ns ^{:author "Janos Erdos"} erdos.sample.avl
    "AVL-tree implementation with custom meta information stored")


(deftype AVLSimpleNode [left, right ;; left/right children
                        val          ;; value stored in node
                        ^int height  ;; height of node (0 for leaves)
                        m]           ;; meta map of node
  ;;; Object (toString [this] (print-avl this))
  clojure.lang.IObj
  (withMeta [_ m]
    (new AVLSimpleNode left right val height m))
  (meta [_] m))


(defn node->str
  "Recursively stringify an AVL tree."
  [^AVLSimpleNode node]
  (when node
    (let [left (.left node)
          right (.right node)]
      (str "(" (str val) ". " (if left left "_")
           " " (if right (str right) "_")  " ... " (.m node) ")"))))


(defmacro ^:private ->AVLSimpleNode
  "Simplified constructor for AVLSimpleNode."
  [left right val]
  `(let [left# ^AVLSimpleNode ~left
         right# ^AVLSimleNode ~right
         val# ~val
         height# (inc (max (if left#  (.height ^AVLSimpleNode left#)  0)
                           (if right# (.height ^AVLSimpleNode right#) 0)))]
     (AVLSimpleNode. left# right# val# height# nil)))


;; This macro is not available in old versions of Clojure.
(defmacro ^:private -?>
  ([x] x)
  ([x y & xs]
   (if (seq xs)
     `(let [x# ~x]
        (if (some? x#)
          (-?> (~y x#) ~@ xs)))
     x)))


(defmacro ^:private map-entry [k v]
  `(new clojure.lang.MapEntry ~k ~v))


(defn- balance-right [^AVLSimpleNode node fun]
  ;; (A (BC)) -> ((A B) C) when c is too heavy
  ;; (assert (.right node))
  (fun (->AVLSimpleNode
        (fun (->AVLSimpleNode
              (-> node .left) (->  node ^AVLSimpleNode (.right) .left) (-> node .val)))
        (-> node ^AVLSimpleNode (.right) .right)
        (-> node ^AVLSimpleNode (.right) .val))))


(defn- balance-left [^AVLSimpleNode node fun]
  ;; ((A B) C) -> (A (B C)) when a is too heavy
  ;; (assert (.left node))
  (fun (->AVLSimpleNode
        (-> node ^AVLSimpleNode (.left) (.left))
        (fun (->AVLSimpleNode
              (-> node ^AVLSimpleNode (.left) .right) (-> node .right) (-> node .val)))
        (-> node ^AVLSimpleNode (.left) (.val)))))


(defn- node-height-diff [^AVLSimpleNode node]
  (- (if (.left node) (.height ^AVLSimpleNode (.left node)) 0)
     (if (.right node) (.height ^AVLSimpleNode (.right node)) 0)))


(defn- balance-with-fn
  "Automatically balances tree then calls fun on changed nodes."
  [^AVLSimpleNode node fun]
  (let [diff (node-height-diff node)]
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
     (cond
      (< x (.val node))
      (let [ll (insert-with-fn (.left node) x < fun)]
        (->AVLSimpleNode ll (.right node) (.val node)))
      (< (.val node) x)
      (let [rr (insert-with-fn (.right node) x < fun)]
        (->AVLSimpleNode (.left node) rr (.val node)))
      :x=val
      (->AVLSimpleNode (.left node) (.right node) x))
     (balance-with-fn fun))))


;;; XXX: impl this.
(defn remove-with-fn [^AVLSimpleNode node x < fun]
  (when (some? node)
    (->
     (cond
      (< x (.val node))
      (let [ll (remove-with-fn (.left node) x < fun)]
        (->AVLSimpleNode ll (.right node) (.val node)))
      (< (.val node) x)
      (let [rr (remove-with-fn (.right node) x < fun)]
        (->AVLSimpleNode (.left node) rr (.val node)))
      :x=val
      ;; TODO: recusrively rebalance whole branch.
      (throw (IllegalStateException.
              "Not implemented yet!"))
      )
     (balance-with-fn fun))))


(defn avl-find [^AVLSimpleNode node < x not-found]
  (if node
    (cond
     (< x (.val node)) (recur (.left node) < x not-found)
     (< (.val node) x) (recur (.right node) < x not-found)
     :otherwise        (.val node))
    not-found))


(defn avl-insert [^AVLSimpleNode node, < x]
  (insert-with-fn node x < identity))


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
         clojure.lang.ILookup
         (valAt [_ k] (second (avl-find tree << [k] nil)))
         (valAt [_ k not-found] (second (avl-find tree << [k] [nil not-found])))
         clojure.lang.Associative
         (containsKey [_ k] (some? (avl-find tree << [k])))
         (entryAt [_ k] (avl-find tree << [k]))
         (assoc [_ k v] (f (insert tree (map-entry k v))))))
     (reduce insert nil xs))))
;; (str (probability-tree {:a 1 :b 1 :c 1}))
;; (deref (probability-tree {:a 1 :b 1 :c 1}))

:good
