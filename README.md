# modern-cljs

A series of tutorials to guide you in creating and setting up
[ClojureScript][1] (CLJS) projects.

# Introduction

This series of tutorials will guide you in creating, setting up and
running simple CLJS projects. The series follows a progressive
enhancement of the project itself.

> NOTE 1: I suggest to code yourself the content of the series. In my
> experience it is always the best choice if you are not already fluent
> with the programming language you have under your fingers.

That said, assuming you already have installed [leiningen 2][2], to
run the latest available tutorial without coding:

1. `$ git clone https://github.com/magomimmo/modern-cljs.git`
2. `$ cd modern-cljs`
3. `$ lein cljx once # from tutorial-16 forward`
4. `$ lein ring server-headless`
5. open a new terminal and cd in the modern-cljs main directory
6. `$ lein cljsbuild once`
7. `$ lein trampoline cljsbuild repl-listen`
8. visit [login-dbg.html][3] and/or [shopping-dbg.html][4]
9. play with the repl connected to the browser

> NOTE 2: If you want to access the code of any single tutorial because
> you don't want to `copy&paste` it or you don't want to write it
> yourself, do as follows:
>
> * `$ git clone https://github.com/magomimmo/modern-cljs.git`
> * `$ cd modern-cljs`
> * `$ git checkout tutorial-01 # for tutorial 1, tutorial-02 for tutorial 2 etc `

## [Tutorial 1 - The basic][5]

In the first tutorial you are going to create and configure a very basic
CLJS project.

## [Tutorial 2 - Browser CLJS REPL (bREPL)][6]

In this tutorial you are going to set up a browser connected CLJS REPL
(bRepl) using an external http-server.

## [Tutorial 3 - CLJ based http-server][7]

In this tutorial you are going to substitute the external http-server
with [ring][8], a CLJ based http-server.

## [Tutorial 4 - Modern ClojureScript][9]

In this tutorial we start having some fun with CLJS form validation, by
porting from JS to CLJS the login form example of
[Modern Javascript: Development and design][10] by [Larry Ullman][11].

## [Tutorial 5 - Introducing Domina][12]

In this tutorial we're going to use [domina library][13] to make our
login form validation more clojure-ish.

## [Tutorial 6 - Easy made Complex and Simple made Easy][14]

In this tutorial we're going to investigate and solve in two different
ways the not so nice issue we met in the last tutorial.

##  [Tutorial 7 - On being doubly aggressive][15]

In this tutorial we're going to explore CLJS/CLS compilation modes by
using the usual `lein-cljsbuild` plugin of `leiningen`, but we'll
discover a trouble we'll solve by using a new feature of the `0.3.0` of
`lein-cljsbuild` plugin.

## [Tutorial 8 - Introducing Domina events][16]

In this Tutorial we're going to introduce domina events which, by
wrapping Google Closure Library event management, allows to follow a
more clojure-ish approach in handing DOM events.

## [Tutorial 9 - DOM Manipulation][17]

In this tutorial we'are going to face the need to programmatically
manipulate DOM elements as a result of the occurrence of some DOM
events.

## [Tutorial 10 - Introducing Ajax][18]

In this tutorial we're going to extend our comprehension of CLJS by
introducing Ajax to let the CLJS client-side code to communicate with
the CLJ server-side code.

## [Tutorial 11 - A deeper understanding of Domina events][19]

In this tutorial we're going to enrich our understanding of Domina events
by applying them to the `login form` example we introduced in the [4th Tutorial][9].

## [Tutorial 12 - The highest and the deepest layers][20]

In this tutorial we're going to cover the highest and the deepest layers
of the Login Form example we started to cover in the [previous tutorial][20].

## [Tutorial 13 - Don't Repeat Yourself while crossing the border][21]

One of our long term objectives is to eliminate any code duplication
from our web applications.  That's like to say we want firmly stay as
compliant as possible with the Don't Repeat Yourself (DRY)
principle. In this tutorial we're going to respect the DRY principle
by sharing validators between the client (i.e. CLJS) and the server
(i.e. CLJ).

## [Tutorial 14 - It's better to be safe than sorry (Part 1)][22]

In this tutorial we are going to prepare the stage for affording the
unit testing topic. We'll also introduce the `Enlive` template sytem
to implement a server-side only version of the Shopping Calculator
aimed at adhering to the progressive enanchement implementation
strategy. We'll even see how to exercize code refactoring to satisfy
the DRY principle and to solve a cyclic namespaces dependency problem.

## [Tutorial 15 - It's better to be safe than sorry (Part 2)][23]

In this tutorial, after having added the validators for the
`shoppingForm`, we're going to introduce unit testing.

## [Tutorial 16 - It's better to be safe than sorry (Part 3)][24]

In this tutorial we make unit testing portable from CLJ to CLJS (and
vice-versa) by using the `clojurescript.test` lib and the `cljx` lein
plugin.

## [Tuturial 17 - Enlive by REPLing][25]

To be respectful with the progressive enhancement strategy, in this
tutorial we're going to integrate the form validators for the
server-side Shopping Calculator into the corresponding WUI (Web User
Interface) in such a way that the user will be notified with the
right error messages when the she/he types in invalid values
in the form.

# License

Copyright © Mimmo Cosenza, 2012-2013. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/clojure/clojurescript.git
[2]: https://github.com/technomancy/leiningen
[3]: http://localhost:3000/login-dbg.html
[4]: http://localhost:3000/shopping-dbg.html
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[8]: https://github.com/mmcgrana/ring.git
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[10]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[11]: http://www.larryullman.com/
[12]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[13]: https://github.com/levand/domina
[14]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[15]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[17]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[18]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[19]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[21]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-13.md
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[23]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-15.md
[24]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md
