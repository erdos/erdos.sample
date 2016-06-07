<p align="center"><img src="https://raw.githubusercontent.com/erdos/erdos.sample/master/doc/logo.png" alt="erdos.stats logo"/></p>


# erdos.sample

A Clojure library for random sampling for a given a discrete distribution. The distribution can be a result of measurements (how many times an event happened?) or a merely mathematical model.

The background idea is simple: intervals are created based on the probabilities. Then a tree data structure is used to check which interval contains a given random number. The key for the found interval is returned.


## Usage

1. Clone the repo: `git clone https://github.com/erdos/erdos.sample`
2. Run REPL an require namespaces: `lein repl`, `(require [erdos.sample :as s])`

This package contains the following namespaces:

### erdos.sample

Default sampling data structures.

**(sample-seq m)**

Given a map of `[sample probability]` pairs, returns a lazy seq
of random samples.

*Example:*

`(sample-seq {:a 1 :b 3}) => (:a :b :b :a :b :b :b :a :b :a :b :b :b :b :a ...)`

**(sample-tree m)**

Given a map of `[sample probability]` pairs, returns a new sampling tree instance. This supports the following function calls:

 - `(count tree)` Returns the number of items in this distribution in _O(1)_ time.
 - `@tree` Returns a random sample with the given discrete distribution in _O(log(n))_ time.
 - `(conj tree [item prob]), (assoc tree item prob)` Adds a new item-probability pair to the distribution in _O(log(n))_ time.
 - `(disj tree [item prob])` Removes an item-probability pair from the distribution in _O(log(n))_ time.
 - `(seq tree)` Returns a seq of all sample-probability pairs.

It is slightly slower that `sample-seq` but returns a persistent data structure compatible with common clojure functions.


### erdos.sample.avl

General purpose AVL-tree implementation.


## License

Copyright © 2016 Janos Erdos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
