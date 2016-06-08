<p align="center"><img src="https://raw.githubusercontent.com/erdos/erdos.sample/master/doc/logo.png" alt="erdos.stats logo"/></p>


# erdos.sample

A Clojure library for random sampling for a given a discrete distribution. The distribution can be a result of measurements (how many times an event happened?) or a merely mathematical model.

The background idea is simple: intervals are created based on the event probabilities. Then a tree data structure is used to check which interval contains a given random number. The key for the found interval is returned.


## Usage

1. Clone the repo: `git clone https://github.com/erdos/erdos.sample`
2. Run REPL an require namespaces: `lein repl`, `(require [erdos.sample :as s])`

This package contains the following namespaces:

### erdos.sample

Default sampling data structures.

**(sample-seq m)**

Given a map of `[sample frequency]` pairs, returns a lazy seq
of random samples.

*Example:*

`(sample-seq {:a 1 :b 3}) => (:a :b :b :a :b :b :b :a :b :a :b :b :b :b :a ...)`

**(sample-tree m)**

Given a map of `[sample frequency]` pairs, returns a new sampling tree instance. This supports the following function calls:

| function |   description  | time complexity |
|----------|----------------|-----------------|
| `(count tree`) | Returns the number of items in this distribution | _O(1)_ |
| `@tree`        | Returns a random sample | _O(log(n))_ |
| `(conj tree [item prob])` <br/> `(assoc tree item prob)` | Adds a new item-frequency pair to the distribution | _O(log(n))_ |
| `(disj tree [item prob])` | Removes an item-probability pair. | _O(log(n))_ |
| `(seq tree)`              | Returns a seq of all sample-probability pairs. | _O(1)_ |

In runtime complexity _n_ is the number of distinct items in the distribution. 
It is slightly slower that `sample-seq` but returns a persistent data structure compatible with common clojure functions.

### erdos.sample.avl

General purpose AVL-tree implementation.

## License

__The MIT License (MIT)__

Copyright (c) 2016 Janos Erdos

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
