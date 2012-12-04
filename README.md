# modern-cljs

A series of tutorials to guide you in creating and setting up
[ClojureScript][5] (CLJS) projects.

# Introduction

This series of tutorials will guide you in creating, setting up and
running simple CLJS projects. The series follows a progressive
enhancement of projects themselves.

Assuming you already have installed [leiningen][9], to run the last
available tutorial without coding:

1. `$ git clone https://github.com/magomimmo/modern-cljs.git`
2. `$ cd modern-cljs/compiler`
3. `$ git clone https://github.com/magomimmo/clojurescript.git`
4. `$ cd clojurescript`
5. `$ ./script/bootstrap`
6. `$ cd ../..`
7. ` $ lein ring server`
8. open a new terminal and cd in modern-cljs
9. `$ lein cljsbuild once`
10. `$ lein trampoline cljsbuild repl-listen`
11. visit [login-dbg.html][11] and/or [shopping-dbg.html][15]
12. play with the repl connected to the browser

> NOTE: If you want to access the code of any single tutorial because
> you don't want to `copy&paste` it or you don't want to write it
> yourself, do as follows:
>
> * `$ git clone https://github.com/magomimmo/modern-cljs.git` (if you
>   have not already done);
> * `$ cd modern-cljs`
> * `$ git checkout tut-01 # for tutorial 1, tut-02 for tutorial 2 etc.`
>
> That said, I suggest coding yourself the content of the tutorials. In
> my experience is always the best choice if you are not already fluent
> in the programming language you have under your fingers.

## [Tutorial 1 - The basic][1]

In the first tutorial you are going to create and configure a very basic
CLJS project.

## [Tutorial 2 - Browser CLJS REPL (bREPL)][2]

In this tutorial you are going to set up a browser connected CLJS REPL
(bRepl) using an external http-server.

## [Tutorial 3 - CLJ based http-server][3]

In this tutorial you are going to substitute the external http-server
with [ring][4], a CLJ based http-server.

## [Tutorial 4 - Modern ClojureScript][6]

In this tutorial we start having some fun with CLJS form validation, by
porting from JS to CLJS the login form example of
[Modern Javascript: Development and desing][7] by [Larry Ullman][8].

## [Tutorial 5 - Introducing Domina][12]

In this tutorial we're going to use [domina library][13] to make our
login form validation more clojure-ish.

## [Tutorial 6 - Easy made Complex and Simple made Easy][14]

In this tutorial we're going to investigate and solve in two different
ways the not so nice issue we met in the last tutorial.

##  [Tutorial 7 - On being doubly aggressive][16]

In this tutorial we're going to explore CLJS/CLS compilation modes by
using the usual `lein-cljsbuild` plugin of `leiningen`, but we'll
discover a trouble we do not know how to manage yet.

## [Tutorial 8 - Learn by contributing][17]

In this tutorial we're going to patch CLJS compiler for solving
the code duplication trouble we met in the previous tutorial.

## [Tutorial 9 - It's better to be safe than sorry - Part 1][18]

In this tutorial we're going to illustrate the clojure testing tool using the
patched CLJS compiler as a true and real-life case.

## [Tutorial 10 - It's better to be safe than sorry - Part 2][19]

In this tutorial we're going to finish the work started in the previus
one by introducing mocks to manage the tests of CLJS compiler in the
mutable world of the file system.

## Tutorial 11 - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[4]: https://github.com/mmcgrana/ring.git
[5]: https://github.com/clojure/clojurescript.git
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[7]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[8]: http://www.larryullman.com/
[9]: https://github.com/technomancy/leiningen
[11]: http://localhost:3000/login-dbg.html
[12]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[13]: https://github.com/levand/domina
[14]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[15]: http://localhost:3000/shopping-dbg.html
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[17]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[18]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[19]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
