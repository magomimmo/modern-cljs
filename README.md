# modern-cljs

A series of tutorials to guide you in creating and setting up
[ClojureScript][1] (CLJS) projects.

# Introduction

This series of tutorials will guide you in creating, setting up and
running simple CLJS projects. The series follows a progressive
enhancement of projects themselves.

Assuming you already have installed [leiningen 2][2], to run the last
available tutorial without coding:

1. `$ git clone https://github.com/magomimmo/modern-cljs.git`
2. `$ cd modern-cljs`
3. ` $ lein ring server-headless`
4. open a new terminal and cd in modern-cljs
5. `$ lein cljsbuild once`
6. `$ lein trampoline cljsbuild repl-listen`
7. visit [login-dbg.html][3] and/or [shopping-dbg.html][4]
8. play with the repl connected to the browser

> LATEST NEWS: Starting from the [11th tutorial][19], we dicided to
> directly include the `1.0.2-SNAPSHOT` of [domina][4] source code in
> the `modern-cljs` code. Consequently, we removed
> `[domina "1.0.2-SNAPSHOT"]` from the project dependencies. We also
> upgraded `lein-cljsbuild` to `0.2.10` version, even if we know that
> there is an open issue about a very boring and apparently useless
> waiting time after cljsbuild has completed any CLJS compilation.

> PREVIOUS NOTE 1: `modern-cljs` has been tested with **lein version
> 2.0.0-preview10**. On January 10th 2013 lein reached version
> **2.0.0-RC1**. Due to lein-ring 0.7.5 depending from leinjacker
> version 0.2.0, lein 2.0.0-RC1 does not work. If you still want to
> use RC1 version of lein, you need to update modern-cljs dependencies
> with **lein-ring 0.8.0-SNAPSHOT** which includes **leinjacker
> 0.4.1** as updated dependency. If, instead, you prefer to stay with
> version 2.0.0-preview10 of lein, you need to downgrade lein by
> running the following command at terminal prompt: `$ lein upgrade
> "2.0.0-preview10"`

> NOTE 2: If you want to access the code of any single tutorial because
> you don't want to `copy&paste` it or you don't want to write it
> yourself, do as follows:
>
> * `$ git clone https://github.com/magomimmo/modern-cljs.git`
> * `$ cd modern-cljs`
> * `$ git checkout tutorial-01 # for tutorial 1, tutorial-02 for tutorial 2 etc.`
>
> That said, I suggest coding yourself the content of the tutorials. In
> my experience is always the best choice if you are not already fluent
> in the programming language you have under your fingers.

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
discover a trouble we do not know how to manage yet.

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

## Next tutorial - TDB

TO BE DONE

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
