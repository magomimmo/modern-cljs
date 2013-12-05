# Modern ClojureScript

Modern ClojureScript (`modern-cljs`) is a series of tutorials that guide you in 
creating, setting up and developing [ClojureScript][1] (CLJS) projects.

CLJS is a compiler for the Clojure programming language that targets
JavaScript. It emits JavaScript code which runs in web browsers or
other client-side or server-side JavaScript interpreters. CLJS has
even been used to [write an excellent iOS application][47]!

## Required background

These tutorials require that you have some prior programming experience. 
They assume you've at least gotten your hands dirty by trying a little Clojure,
even if you're not proficient in it yet, or in the underlying Java and 
JavaScript programming languages. It will also be quite helpful if you have
some experience programming for the Web using HTML, JavaScript and the
browser DOM.

If you don't know anything about Clojure (or about Lisp), I strongly
recommend you learn a little bit about it before starting these tutorials.

There are plenty of outstanding resources on Clojure that are freely available
on the Internet, and you can't overestimate the benefit of reading
a book on Clojure (or another Lisp dialect) to your value as a programmer,
a thinker, and a human being.

Here are some book recommendations:

* [Clojure Programming][29]: written by three of the heroes of Clojure,
  it contains everything you need to know about Clojure and its ecosystem.
* [Programming Clojure][30]: written by another legendary Clojure developer,
  it's the easiest path to learning Clojure.
* [The Joy of Clojure][31]: the title speaks by itself. A must read!
* [**The Annotated Clojure Reference Manual** by Rich Hickey][33]: the
  most often missed Clojure book :).
* [ClojureScript Up and Running][32]: at the moment, it's the only
  published book on ClojureScript. Even though it's not that old, the
  book is a little outdated since ClojureScript is evolving quickly.
  It's so brief you can read it very quickly, and it's still useful,
  especially if you want to integrate with external JavaScript libraries.
* [SICP - Structure and Interpretation of Computer Programs][27]: this
  is the best programming book I've ever read in my very long career. It uses
  [Scheme/Racket][48] (a Lisp dialect) rather than Clojure and is available
  [online][43] or in [print][43].
* [On Lisp][28]: if you want to learn about macros, this is the place to start 
  even though it uses [Common Lisp][49] (a well-known Lisp dialect) rather than
  Clojure.

## Required tools

Many people worry about which operating system and editor/IDE are best
for developing in Clojure and ClojureScript. I personally
use both Mac OS X and Ubuntu, and I use Emacs as an editor. 

Because I'm an old-timer, *nix and Emacs are the OS and editor I know best. 
That being said, in this series of tutorials you're not going to find any
suggestions or reference to OS or IDE/Editor. Use whatever tools you already
have and know. I have too much respect for people developing IDE/plugins for 
Clojure/CLJS to say that one is better than another, and you don't
want to combine learning a new programming language with trying to learn a
new programming environment.

You will need to have [git][45] installed and you'll need some familiarity
with the [very basics of git][46].

Everything else you'll need besides git and a code editor that you're
comfortable with is covered in the tutorials!

# Introduction

This series of tutorials guides you in creating and running simple CLJS
projects. The bulk of the series follows the progressive enhancement of
a single project.

While working through the tutorials I *strongly* suggest you start at
tutorial 1 and type in all the code for each tutorial yourself.
In my experience it's the best choice if you're not already
very fluent with the programming language.

That being said, if you want to jump to the end and see what the final project
resulting from following the tutorials looks like, and assuming you
have already installed [leiningen 2][2], you can run the project from the last
tutorial by following these steps:

1. Get the tutorial repository by running
   `git clone https://github.com/magomimmo/modern-cljs.git`
2. run `cd modern-cljs`
3. run `lein cljx once` # used from tutorial-16 forward
4. run `lein ring server-headless`
5. open a new terminal and cd in the modern-cljs main directory
6. run `lein cljsbuild once`
7. run `lein trampoline cljsbuild repl-listen`
8. open [login-dbg.html][3] and/or [shopping-dbg.html][4] in your browser
9. you can play with the repl you started in step 7 which is now connected
   to the browser

Don't be concerned if steps 3-9 didn't make sense to you just yet,
they'll each be covered in the tutorials.

> NOTE: If you want to skip ahead or back and access the code of any single
> tutorial without typing it or pasting it in, you can do as follows:
>
> * run `git clone https://github.com/magomimmo/modern-cljs.git`
> * run `cd modern-cljs`
> * run `git checkout tutorial-n` # n is 01 for tutorial 1, 02 tutorial-02, etc.

## [Tutorial 1 - The basics][5]

In the first tutorial you are going to create and configure a very basic
CLJS project.

## [Tutorial 2 - Browser CLJS REPL (bREPL)][6]

In this tutorial we set up a browser connected CLJS REPL
(bRepl) using an external http-server.

## [Tutorial 3 - Ring and Compojure][7]

In this tutorial we substitute the external http-server
with [Ring][8], a Clojure HTTP server and middleware, and [Compojure][50],
a routing library for Ring.

## [Tutorial 4 - Modern ClojureScript][9]

In this tutorial we start having some fun with CLJS form validation by
porting the JavaScript login form example from
[Modern Javascript: Development and design][10] by [Larry Ullman][11] to CLJS.

## [Tutorial 5 - Introducing Domina][12]

In this tutorial we use the [Domina library][13] to make our
login form validation more Clojure-ish.

## [Tutorial 6 - The easy made complex, and the simple made easy][14]

In this tutorial we investigate and find two different ways to solve an
issue we ran into in the last tutorial.

##  [Tutorial 7 - Being doubly aggressive][15]

In this tutorial we explore CLJS/CLS compilation modes by
using the `lein-cljsbuild` plugin of `leiningen`, and we
discover a problem and solve it using a feature of the
`lein-cljsbuild` plugin.

## [Tutorial 8 - Introducing Domina events][16]

In this Tutorial we introduce Domina events which, by
wrapping Google Closure Library event management, allow us use a
more Clojure-ish approach to handing DOM events.

## [Tutorial 9 - DOM Manipulation][17]

In this tutorial we programmatically manipulate DOM elements
in response to DOM events.

## [Tutorial 10 - Introducing AJAX][18]

In this tutorial we extend our knowledge of CLJS by
introducing AJAX to let the CLJS client-side code communicate with
the Clojure server-side code.

## [Tutorial 11 - A deeper understanding of Domina events][19]

In this tutorial we enrich our understanding of Domina
events by applying them to the `login form` example from
the [4th Tutorial][9].

## [Tutorial 12 - The highest and the deepest layers][20]

In this tutorial we're going to cover the highest and the deepest
layers of the Login Form example we started to cover in the
[previous tutorial][20].

## [Tutorial 13 - Don't Repeat Yourself][21]

It's important to eliminate code duplication
from our web applications wherever possible. We want to stay compliant
with the Don't Repeat Yourself (DRY) mantra. In this tutorial we respect
the DRY principle by sharing validators between the client-side CLJS
and the server-side Clojure.

## [Tutorial 14 - Better safe than sorry (Part 1)][22]

In this tutorial we are going to prepare the stage for affording the
unit testing topic. We'll also introduce the `Enlive` template sytem
to implement a server-side only version of the Shopping Calculator
aimed at adhering to the progressive enanchement implementation
strategy. We'll even see how to exercize code refactoring to satisfy
the DRY principle and to solve a cyclic namespaces dependency problem.

## [Tutorial 15 - Better safe than sorry (Part 2)][23]

In this tutorial, after having added the validators for the
`shoppingForm`, we do some unit testing.

## [Tutorial 16 - Better safe than sorry (Part 3)][24]

In this tutorial we make our unit tests portable from Clojure to CLJS (and
vice-versa) by using the `clojurescript.test` lib and the `cljx` lein
plugin.

## [Tuturial 17 - REPLing with Enlive][25]

Following our progressive enhancement strategy, in this
tutorial we integrate the form validators for the
server-side Shopping Calculator into the corresponding Web UI, so the user
is notified with the right error messages when typing invalid values
into the form.

## [Tutorial 18 - Housekeeping!][26]

In this tutorial we digress to cover two productivity topics. We setup
a more comfortable browser REPL based on nREPL, and a more
comfortable project structure using the `profiles` features of
[Leiningen][2]

## [Tutorial 19 - Livin' on the edge][35]

In this tutorial we see how to contribute something we need to someone
else's library, and how to publish a snapshot releases on
[clojars][36] to use the enhancement in our own project.

## [Tutorial 20 - Learning by Contributing (Part 1)][37]

In this tutorial, we look at the [Enfocus][38] library with the
objective of sharing as much code as possible with 
[Enlive][39]. We start our open source collaboration by proposing few
improvements to the `Enfocus` directories layout and the adoption of
the [clojurescript.test][40] lib for implementing its unit tests.

## [Tutorial 21 - Learning by Contributing (Part 2)][41]

In [this tutorial][41] we adjust `Enfocus` for packaging
it up into a `jar`. Then we instrument it with the `piggieback` lib,
publish it on `clojars` and use it as a dependency in a very
simple project to demonstrate that the changes we made don't
affect the `Enfocus` codebase which still works as expected.

## [Tutorial 22 - Learning by Contributing (Part 3)][42]

In [this tutorial][42] we improve `Enfocus` more by
applying the separation of concerns principle and by
implementing a few unit tests. In the process we discover a few
bugs in the `enfocus` codebase and correct them by first
interacting with `Enfocus` in the REPL.

# Why the name Modern ClojureScript?

You might be wondering why this tutorial series is named `modern-cljs`
when ClojureScript is only a couple of years old. What 
is ancient ClojureScript? I started this series while trying to port
a few examples from the [Modern JavaScript: Develop and Design][34] book to
ClojureScript, and now it's too late to change! 

It turns out the name is growing more appropriate each day as
ClojureScript continues to evolve quickly. I'd like to think it was a brilliant
choice, but it was just a happy accident. 

# License

Copyright © Mimmo Cosenza, 2012-2013. Released under the Eclipse Public
License, the same license as Clojure.

[1]: https://github.com/clojure/clojurescript.git
[2]: https://github.com/technomancy/leiningen
[3]: http://localhost:3000/login-dbg.html
[4]: http://localhost:3000/shopping-dbg.html
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[8]: https://github.com/ring-clojure/ring
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
[26]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md
[27]: http://mitpress.mit.edu/sicp/
[28]: http://www.paulgraham.com/onlisp.html
[29]: http://www.clojurebook.com/
[30]: http://pragprog.com/book/shcloj2/programming-clojure
[31]: http://joyofclojure.com/
[32]: http://shop.oreilly.com/product/0636920025139.do
[33]: https://twitter.com/richhickey
[34]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[35]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[36]: https://clojars.org/
[37]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-20.md
[38]: https://github.com/ckirkendall/enfocus
[39]: https://github.com/cgrand/enlive
[40]: https://github.com/cemerick/clojurescript.test
[41]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-21.md
[42]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-22.md
[43]: http://mitpress.mit.edu/sicp/full-text/book/book.html
[44]: http://www.amazon.com/Structure-Interpretation-Computer-Programs-Engineering/dp/0262510871/
[45]: http://git-scm.com/
[46]: http://git-scm.com/documentation
[47]: http://keminglabs.com/blog/angular-cljs-weather-app/
[48]: http://racket-lang.org/
[49]: http://en.wikipedia.org/wiki/Common_Lisp
[50]: https://github.com/weavejester/compojure