# Modern ClojureScript

> ATTENTION NOTE: I'm in the process of gradually updating the
> series. At the moment I updated only the Tutorial-02, while all the
> others are still using aged stuff. Please be patient because it will
> take at least a month to complete the work.

Modern ClojureScript (`modern-cljs`) is a series of tutorials that guide you in 
creating and running [ClojureScript][1] (CLJS) projects.

CLJS is a compiler for the Clojure programming language that targets
JavaScript. It emits JavaScript code which runs in web browsers and
other client-side or server-side JavaScript interpreters
(e.g. [nodejs][2]).

## Required background

These tutorials require that you have some prior programming experience. 
They assume you've gotten your hands dirty by trying a little Clojure,
even if you're not proficient in it yet. It will also be quite helpful if
you have some experience programming for the Web using HTML, JavaScript and the
browser DOM.

If you don't know anything about Clojure (or about Lisp), I
recommend you learn a little bit before starting these tutorials.

There are plenty of outstanding resources on Clojure that are freely
available on the Internet, and you can't overestimate the benefit of
reading a book on Clojure (or another Lisp dialect) to your value as a
programmer.

Here are some book recommendations:

* [Clojure Programming][3]: written by three of the heroes of Clojure,
  it contains everything you need to know about Clojure and its ecosystem.
* [Programming Clojure][4]: written by another legendary Clojure developer,
  it's the easiest path to learning Clojure.
* [The Joy of Clojure][5]: the title speaks by itself. A must read!
* [ClojureScript Up and Running][6]: at the moment, it's the only
  published book on ClojureScript. Even though it's not old, the
  book is a bit outdated since ClojureScript is evolving quickly. It's brief
  and useful, especially if you want to integrate with external
  JavaScript libraries.
* [SICP - Structure and Interpretation of Computer Programs][7]: this
  is the best programming book I've read in my very long career. It uses
  [Scheme/Racket][8] (a Lisp dialect) rather than Clojure and is available
  [online][9], in [print][9], or in a [lecture series][10].
* [On Lisp][28]: if you want to learn about macros, this is the place to start. 
  It uses [Common Lisp][11] (a Lisp dialect) rather than Clojure.

## Required tools

Many people worry about which operating system and editor/IDE are best
for developing in Clojure and ClojureScript. I personally use Mac OS
X, Debian and Ubuntu. I use Emacs as an editor.

Because I'm an old-timer, *nix and Emacs are the OS and editor I know best. 
That being said, in this series of tutorials you're not going to find any
suggestions or reference to operating systems or editors. Use whatever tools
you already have and know. I have too much respect for people developing
IDE/plugins for Clojure/CLJS to say that one is better than another, and
you don't want to combine learning a new programming language with trying
to learn a new programming environment.

You will need to have [git][12] installed and you'll need some familiarity
with the [basics of git][13].

## Why the name Modern ClojureScript?

You might wonder why this tutorial series is named `modern-cljs` when
ClojureScript is so recent. I started this series while trying to port
a few examples from the [Modern JavaScript: Develop and Design][14]
book to ClojureScript, and now it's too late to change.

# The Tutorials

## Introduction

This series of tutorials guides you in creating and running simple CLJS
projects. The bulk of the series follows the progressive enhancement of
a single project.

While working through the tutorials I *strongly* suggest you start at
tutorial 1 and type in all the code for each tutorial yourself.
In my experience this is the the best approach if you're not already
very fluent with the programming language.

## [Tutorial 1 - The Basics][15]

Create and configure a very basic CLJS project.

## [Tutorial 2 - Browser CLJS REPL (bREPL)][16]

Set up a browser-connected CLJS REPL (bRepl) using an external http-server.

## [Tutorial 3 - Ring and Compojure][17]

Replace the external http-server with [Ring][18], a Clojure HTTP server and
middleware, and [Compojure][19], a routing library for Ring.

## [Tutorial 4 - Modern ClojureScript][20]

Have some fun with CLJS form validation by porting the JavaScript login form
example from [Modern JavaScript: Develop and Design][21] to CLJS.

## [Tutorial 5 - Introducing Domina][22]

Use the [Domina library][23] to make our login form validation more Clojure-ish.

## [Tutorial 6 - The Easy Made Complex, and the Simple Made Easy][24]

Investigate and find two different ways to solve an issue from the last
tutorial.

##  [Tutorial 7 - Compilation Modes][25]

Explore CLJS/CLS compilation modes by using the `lein-cljsbuild` plugin of
`leiningen`, and discover a problem and solve it using a feature of the
`lein-cljsbuild` plugin.

## [Tutorial 8 - Introducing Domina Events][26]

Use Domina events for a more Clojure-ish approach to handing DOM events.

## [Tutorial 9 - DOM Manipulation][27]

Programmatically manipulate DOM elements in response to DOM events.

## [Tutorial 10 - Introducing AJAX][28]

Use AJAX to let the CLJS client-side code communicate with the server.

## [Tutorial 11 - A Deeper Understanding of Domina Events][29]

Apply Domina events to the login form example from the [4th Tutorial][20].

## [Tutorial 12 - HTML on Top, Clojure on the Bottom][30]

Explore the highest (HTML5) and deepest (Clojure on the server) layers of the
login form example from the [previous tutorial][29].

## [Tutorial 13 - Don't Repeat Yourself][31]

Respect the Don't Repeat Yourself (DRY) principle by sharing validators between
the client-side CLJS and the server-side Clojure.

## [Tutorial 14 - Better Safe Than Sorry (Part 1)][32]

Set the stage for unit testing by learning about the `Enlive`
template sytem and starting the shopping calculator example. Use code
refactoring to satisfy the DRY principle and to solve a cyclic namespaces
dependency problem.

## [Tutorial 15 - Better Safe Than Sorry (Part 2)][33]

Add validators to the `shoppingForm`, and do some unit testing.

## [Tutorial 16 - Better Safe Than Sorry (Part 3)][34]

Make our unit tests portable between Clojure and CLJS by using the
`clojurescript.test` lib and the `cljx` lein plugin.

## [Tutorial 17 - REPLing with Enlive][35]

Integrate the form validators from the server-side Shopping Calculator into
the Web UI, so the user is notified with the right error messages when 
typing invalid values into the form.

## [Tutorial 18 - Housekeeping!][36]

A digression to cover two topics on CLJS developer productivity: setting up
a more comfortable browser REPL based on nREPL, and a more
comfortable project structure using the `profiles` features of
[Leiningen][37].

## [Tutorial 19 - Livin' On the Edge][38]

Learn how to contribute something we need to someone
else's library, and how to publish snapshot releases on
[clojars][39] to use the enhancement in our own project.

## [Tutorial 20 - Learning by Contributing (Part 1)][40]

Look at the [Enfocus][41] library with the
objective of sharing as much code as possible with 
[Enlive][42]. Start an open source collaboration by proposing a few
improvements to the Enfocus directory structure and the adoption of
the [clojurescript.test][43] library for implementing unit tests.

## [Tutorial 21 - Learning by Contributing (Part 2)][44]

Package [Enfocus][41] into a `jar`, instrument it
with the [Piggieback][45] library, publish it on [clojars][39], and use it
as a dependency in a very simple project to see that the changes we made don't
affect the Enfocus codebase, which still works as expected.

## [Tutorial 22 - Learning by Contributing (Part 3)][46]

Improve [Enfocus](41) by applying separation of concerns and
implementing a few unit tests. In the process, discover some 
bugs and correct them by first interacting with Enfocus in the REPL.

# License

Copyright © Mimmo Cosenza, 2012-2015. Released under the Eclipse Public
License, the same license as Clojure.

[1]: https://github.com/clojure/clojurescript.git
[2]: https://github.com/clojure/clojurescript/wiki/Quick-Start#running-clojurescript-on-nodejs
[3]: http://www.clojurebook.com/
[4]: http://pragprog.com/book/shcloj2/programming-clojure
[5]: http://joyofclojure.com/
[6]: http://shop.oreilly.com/product/0636920025139.do
[7]: http://mitpress.mit.edu/sicp/
[8]: http://racket-lang.org/
[9]: http://mitpress.mit.edu/sicp/full-text/book/book.html
[10]: http://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-001-structure-and-interpretation-of-computer-programs-spring-2005/index.htm
[11]: http://www.paulgraham.com/onlisp.html
[12]: http://git-scm.com/
[13]: http://git-scm.com/documentation
[14]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[15]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[17]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[18]: https://github.com/ring-clojure/ring
[19]: https://github.com/weavejester/compojure
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[21]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[23]: https://github.com/levand/domina
[24]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[26]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[27]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[29]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md
[30]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[31]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-13.md
[32]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[33]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-15.md
[34]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md
[35]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md
[36]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md
[37]: https://github.com/technomancy/leiningen
[38]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[39]: https://clojars.org/
[40]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-20.md
[41]: https://github.com/ckirkendall/enfocus
[42]: https://github.com/cgrand/enlive
[43]: https://github.com/cemerick/clojurescript.test
[44]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-21.md
[45]: https://github.com/cemerick/piggieback
[46]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-22.md
