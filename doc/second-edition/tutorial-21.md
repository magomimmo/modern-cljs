# Tutorial 21 The Real World

In the [previous tutorial][1] we enhanced the `build.boot` building
file to be able to install a CLJ/CLJS library to the local `maven`
repository and finally publish it to
[`clojars`](https://clojars.org/).

In this tutorial we'll came back to the Clojure(Script) programming
language by introducing the most explained and commented library in
the brief history of this awesome programming language:
[`core.async`](https://github.com/clojure/core.async).

## Preamble

To start working, assuming you've git installed, do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
```

## Introduction

There is no way I could explain the
[`core.async`](https://github.com/clojure/core.async) library any
better than others already did, given that the others we're talking
about are [Rich Hickey](https://github.com/richhickey), the inventor
of Clojure(Script) and `core.async` library as well,
[Timothy Baldridge](https://github.com/halgari), the guy that
implemented the
[`go` macro](https://github.com/clojure/core.async/blob/a833f6262cdaf92c6b16dd201d1876e0de424e14/src/main/clojure/cljs/core/async/macros.clj)
and [David Nolen](), perhaps the most fruitful and persevering
clojurian and current maintainer of the ClojureScript compiler.

Thats said, this is a series of tutorials for serious CLJS beginners
and I can't avoid to afford the `core.async` topic as well.

## Nothing new under the sun

The computational model adopted by `core.async` and named
[Communicating Sequential Processes (CSP)](https://en.wikipedia.org/wiki/Communicating_sequential_processes)
has been established in the seventies by
[Tony Hoare](https://en.wikipedia.org/wiki/Tony_Hoare), the same
British scientist who invented the Quick Sort algorithm in the
sixties.

Even if a bunch of very innovative programming languages implemented
the CSP computational model,
[The Go Programming Language](https://golang.org/) was the one that 30
years later, in 2009, brought to CSP some notoriety in the programming
communities. The `core.async` library is directly inspired to the
corresponding implementation of the `go` programming language.

## What's the problem

Let's start by asking ourself what's the problem the CSP computational
model wants to solve?

As said by Rich Hickey in presenting the `core.async` library, *"there
comes a time in all good programs when component or subsystem must
stop communicating directly with one another."*

To explain this statement, we're going to steal a Tim Baldridge's
example.

Let's say we have to simulate a car factory. You'll probably end up
with something like this:

```clj
(defn make-car []
    (-> (make-frame)
        (add-body)
        (map (add-tire) (make-tire 5))
        (add-engine (make-engine))))
```

Here, we modeled a single sequential process, where each function
directly communicates with one another. The result of calling the
`make-frame` is passed down to the `add-body` function. In turn, the
result of `add-body` is passed down to the next function and so on.

Even if this is the way we have been accustomed to code for decades
for simulating the real world, it's not the way the real world works
at the large. Each function of the above example can be represented as
an independent process communicating and coordinating itself with the
others via communication channels.
## Easy Start

Let's start very easy by first adding the `core.async` library to the
`:dependencies` section of the project's `build.boot` build file:

```clj
(set-env!
 ...
 :dependencies '[...
                 [org.clojure/core.async "0.2.374"]
                 ])
...
```
Then, start the CLJ REPL as usual:

```bash
boot repl
...
boot.user=>
```

and require the main `clojure.core.async` namespace:

```clj
boot.user> (require '[clojure.core.async :as a])
nil
```

```clj
(defn boring [msg]
  (loop [i 0]
    (println msg i)
    (Thread/sleep 1000)
    (recur (inc i))))
```

As you probably heard from Rich Hickey talk on `core.async`, *function
chains make poor machines*. What does it means? We should first find
out what are function chains and what are machine to be able to
eventually understand what does it means the function chains make poor
machine and why this statement should be true.

A function chain is very easy to be understand. It's just a function
calling another function, calling another function, and so on, like so:

```clj
boot.user> (defn f [n] n)
#'boot.user/f
boot.user> (defn g [n] (+ n 1))
#'boot.user/g
boot.user> (defn h [n] (+ n 1))
#'boot.user/h
boot.user> (h (g (f 1)))
3
```

As you see the `(h (g (f 1)))` start to be difficult to be read,
because Clojure, as any LISP has to be read from inside out. To
improve the readability of such a form, Clojure has the `->` first
thread macro

```clj
boot.user> (-> 1
               (f)
               (g)
               (h))
3
```

which is read as a sequence of functions in a more imperative way:

* get the number `1`
* apply to it the function `f`
* apply to the above result the function `g`
* apply to the above result the function `h`.

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
