# Tutorial 21 The Real World

In the [previous tutorial][1] we enhanced the `build.boot` building
file to be able to install a CLJ/CLJS library to the local `maven`
repository and finally publish it to
[`clojars`](https://clojars.org/).

In this tutorial we're coming back to the Clojure(Script)
programming language by introducing the most explained and commented
library in the brief history of this awesome programming language:
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
better than others already did, given that between the others you'll
find [Rich Hickey](https://github.com/richhickey), the inventor of
Clojure(Script) and `core.async` library as well,
[Timothy Baldridge](https://github.com/halgari), the guy that
implemented the
[`go` macro](https://github.com/clojure/core.async/blob/a833f6262cdaf92c6b16dd201d1876e0de424e14/src/main/clojure/cljs/core/async/macros.clj)
and [David Nolen](), one of the most fruitful and persevering
clojurian and current maintainer of the ClojureScript compiler.

Thats said, this is a series of tutorials for serious CLJS beginners
and I can't avoid to afford the `core.async` topic as well, even
because of the event-driven nature of the browser itself.

## Nothing new under the sun

The computational model adopted by `core.async`, named
[Communicating Sequential Processes (CSP)](https://en.wikipedia.org/wiki/Communicating_sequential_processes),
has been established in the seventies by
[Tony Hoare](https://en.wikipedia.org/wiki/Tony_Hoare), the same
British scientist who invented the
[quicksort](https://en.wikipedia.org/wiki/Quicksort) algorithm in the
sixties.

Even if a bunch of very innovative programming languages implemented
the CSP computational model,
[The Go Programming Language](https://golang.org/) was the one that 30
years later, at the end of 2009, brought to CSP some notoriety in the
programming communities. The `core.async` library is directly inspired
to the corresponding characteristics of the `go` programming language.

## Simulate the real world

As said by [Rob Pike](https://en.wikipedia.org/wiki/Rob_Pike) in his
awesome talk on
[Go Concurrency Patterns](https://www.youtube.com/watch?v=f6kdp27TYZs),
if we want to simulate or interact with the real world, which is
populated of independent entities, the single sequence execution
computational model is not adequate. Let's say that you're modeling a
car factory.

> NOTE 1: I'm steeling the car factory sample from
> [Tom Baldrige](https://tbaldridge.pivotshare.com) and
> [Eric Normand](http://www.purelyfunctional.tv/core-async).

By using the traditional single sequence execution paradigm, also
called **synchronous**, you could start with something like
this:

```clj
(defn assemble-car []
   (add-engine (add-tires (add-body (make-frame)) 4)))
```

The prefix nature of LISP expressions could make the above forms of
nested function calls difficult to be read. Actually, LISP source
files have to be read bottom-up and inside-out to understand the order
of execution. Aside from the complaint about the number of parentheses
used by LISP, which is not even true, the second most controversial
aspect of the LISP syntax regards the cited way to read the code.

To overcome that reading difficulty, Clojure offers two threading
macros, namely the threading first macro (i.e., `->`) and the
threading last macro (i.e., `->>`). By using the threading first
macro, the above `assemble-car` is transformed in the following form:

```clj
(defn assemble-car []
   (-> (make-frame)
       (add-body)
       (add-tires 4)
       (add-engine)))
```

> NOTE 2: the result of `make-frame` is implicitly passed as **first**
> argument to `add-body`. The result of `add-body` is then implicitly
> passed as **first** argument to `add-tires` and so on. This is why
> `->` is called threading **first** macro.

The `assemble-car` definition is now much more readable than before,
because the order of execution is sequential, from top to bottom. In
the standard **synchronous** model of computation, when a function
(caller) calls another function (callee), the caller waits for the
result of the callee to be able to proceed with the computation. The
entire computation is blocked until each callee returns its result to
the corresponding caller.

Obviously, this is not the way a factory would assemble cars in a real
world, because it would be very inefficient.

Another way to describe a car factory is to see it as a number of
independent processes (i.e., computations) to be coordinated or
combined in some way:

* a process to make the frame
* a process to add the body to the frame
* a process to make tires
* a process to add the tires to the body
* a process to add the engine to the body

The  way  to  combine  those   processes  is  through  an  assembly
line. When a  process is done, it  puts its result on  a conveyor belt
for the next process to be taken  and so on. Each conveyor belt can be
interpreted  as   a  communication  channel  between   processes.  The
processes coordination  is implicitly  defined by  channels connecting
the process and  this is exactly what CSP is  all about: processes and
communication channels.

One of the nice thing about CSP channels, is that they support many
writers ans many readers as well and this allows easily improve
performance. For example, say that the `assemble-car`

To create a channel you use the `chan` function as follows:

```clj
(def c (chan))
```

By default the created channel is unbuffered (0 size). To `put` a value into a channel, you use the ``put!` function as follows:


. This is
exactly how CSP works. There are Processes and there are Channels. 



The problem with the synchronous
model of computation is of efficiency nature. Say that the `g`
function internally does a long I/O operation, the next `h` function
has to wait until `g` returns. In the meantime `g` does its I/O jobs,
the system is blocked and is not using its computation resources at
its best for doing something useful.

Moreover, consider that in same period of time the processor power
growth millions of times, while the speed of I/O operations growth
only few times. 

To overcome this kind of slowness, which is especially annoying in the
user interface context, it has been introduced the
[event-driven programming](https://en.wikipedia.org/wiki/Event-driven_programming)
paradigm in which the flow of the program execution is no more
sequential, but it's determined by events such user actions, sensors
output or messages from other programs/thread.

## Function chains make poor machine

As usual, by solving a current problem, we're frequently setting the
bases for a new problem to manifest itself later. In an event-driven
approach, there is usually a main loop listening for events of various
types. When an event of some type appears, the loop triggers a
function, conveniently known as callback, that has been previously
attached to the corresponding event type. This is exactly what we did
in previous tutorials on DOM manipulation when, in the `init`
functions for the `index.html` and `shopping.html`, we attached few
listeners (another name for callbacks) to various user generated
events: `click`, `blur`, `input`, `mouseover`, `mouseout`, etc.

This way, the logic of the program starts to get fragmented in more
places. When the application becomes more complex, as it always
happens with Single Page Applications, it becomes very difficult to
reason about the application logic and its state. A callback triggered
by the occurrence of an event will trigger a second callback which, in
turn, will triggers a third callback an so on.

This situation is very well known and it also deserved a name:
callback hell. Callback hell affects JS on the client-side (browser)
and even more the server-side (nodejs). Obviously, there are more ways
to approach this issue in JS. 


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
