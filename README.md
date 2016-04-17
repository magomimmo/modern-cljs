## Latest tutorials

### [Tutorial 20 - House Keeping][40]

Step by step guide for publishing a library to clojars repository by
using `boot`.

### [Tutorial 19 - Livin' on the edge][39]

Explain how to make a library compliant with the new Reader
Conditionals extension of CLJ/CLJS compilers.

### [Tutorial 18 - Augmented TDD session][38]

Complete the client-side form validation by exploiting the [TDD][45]
environment augmented with CLJ/CLJS REPLs.

# Modern ClojureScript

> **ATTENTION NOTE**: I'm in the process of publishing the
> [second edition][1] of the series. The main difference from the
> [first edition][2] regards the use of the [Boot][3] build tool instead
> of [Leiningen][4]. This second edition is still a draft version and
> you should be forgiving if you find errors, typos or even bugs in
> the code.


> **WARNING NOTE FOR WINDOWS USERS**: At the moment `boot` does not run
> on MS Windows less than 10. If this is your case, to be able to
> follow the `modern-cljs` series you can use use a
> [virtual machine](https://www.virtualbox.org/) or
> [docker linux container](https://docs.docker.com/windows/).

Modern ClojureScript (`modern-cljs`) is a series of tutorials that
guide you in creating and running [ClojureScript][5] (CLJS) projects.

CLJS is a compiler for the Clojure programming language that targets
JavaScript. It emits JavaScript code which runs in web browsers and
other client-side or server-side JavaScript interpreters
(e.g. [nodejs][6]).

## Required background

These tutorials require that you have some prior programming
experience.  They assume you've gotten your hands dirty by trying a
little Clojure, even if you're not proficient in it yet. It will also
be quite helpful if you have some experience programming for the Web
using HTML, JavaScript and the browser DOM.

If you don't know anything about Clojure (or about Lisp), I recommend
you learn a little bit before starting these tutorials.

There are plenty of outstanding resources on Clojure that are freely
available on the Internet, and you can't overestimate the benefit of
reading a book on Clojure (or another Lisp dialect) to your value as a
programmer.

Here are some book recommendations:

* [Clojure Programming][7]: written by three of the heroes of Clojure,
  it contains everything you need to know about Clojure and its
  ecosystem.
* [Programming Clojure][8]: written by another legendary Clojure
  developer, it's the easiest path to learning Clojure.
* [The Joy of Clojure][9]: the title speaks by itself. A must read!
* [ClojureScript Rational][41] and [ClojureScript Quick Start][42]
* [ClojureScript Up and Running][10]: at the moment, it's the only
  published book on ClojureScript. The book is a bit outdated since
  ClojureScript is evolving quickly. It's brief and useful, especially
  if you want to integrate with external JavaScript libraries.
* [SICP - Structure and Interpretation of Computer Programs][11]: this
  is the best programming book I've read in my very long career. It
  uses [Scheme/Racket][12] (a Lisp dialect) rather than Clojure and is
  available [online][13], in [print][13], or in a
  [lecture series][14].
* [On Lisp][15]: if you want to learn about macros, this is the place
  to start.  It uses Common Lisp (a Lisp dialect) rather than Clojure.

## Required tools

Many people worry about which operating system and editor/IDE are best
for developing in Clojure and ClojureScript. I personally use Mac OS
X, Debian and Ubuntu. I use Emacs as an editor.

Because I'm an old-timer, [*nix][43] and Emacs are the OS and editor I know best.
That being said, in this series of tutorials you're not going to find any
suggestions or reference to operating systems or editors. Use whatever tools
you already have and know. I have too much respect for people developing
IDE/plugins for Clojure/CLJS to say that one is better than another, and
you don't want to combine learning a new programming language with trying
to learn a new programming environment.

> NOTE: If you are interested in learning more about Emacs here are some
[resources to help get you started]
(https://github.com/magomimmo/modern-cljs/blob/master/doc/supplemental-material/emacs-cider-references.md).

You will need to have [git][16] and [Java][34] installed and you'll
need some familiarity with the [basics of git][17].

## Clojure community documentation

Community created clojure documentation sites that you may find helpful are
[clojuredocs](http://clojuredocs.org/) and [Grimoire](https://www.conj.io/).

## Libraries and tools

[Clojure Toolbox](http://www.clojure-toolbox.com/) is a directory of
libraries and tools for CLJ/CLJS.

## Why the name Modern ClojureScript?

You might wonder why this tutorial series is named `modern-cljs` when
ClojureScript is so recent. I started this series in 2012 while trying
to port a few examples from the
[Modern JavaScript: Develop and Design][18] book to ClojureScript, and
now it's too late to change.

# The Tutorials

As said, this is the [second edition][1] of the series and is based on
the [Boot][3] build tool. I'm not going to update or support the
[first edition][2] of the series which was based on the [Leiningen][4]
build tool.

## Introduction

This series of tutorials guides you in creating and running simple CLJS
projects. The bulk of the series follows the progressive enhancement of
a single project.

While working through the tutorials I *strongly* suggest you start at
tutorial 1 and type in all the code for each tutorial yourself.
In my experience this is the the best approach if you're not already
very fluent with the programming language.

## [Tutorial 1 - The Basics][19]

Create and configure a very basic CLJS project.

## [Tutorial 2 - Immediate Feedback Principle][20]

Approach as close as possible the Bret Victor Immediate Feedback
Principle to build a very interactive development environment.

## [Tutorial 3 - House Keeping][21]

Automate the launching of the `boot` command to approach the Immediate
Feedback Development Environment (IFDE).

## [Tutorial 4 - Modern ClojureScript][22]

Have some fun with CLJS form validation by porting the JavaScript login form
example from [Modern JavaScript: Develop and Design][18] to CLJS.

## [Tutorial 5 - Introducing Domina][23]

Use the [Domina library][24] to make our login form validation more Clojure-ish.

## [Tutorial 6 - The Easy Made Complex, and the Simple Made Easy][25]

Investigate and find two different ways to solve an issue from the last
tutorial.

## [Tutorial 7 - Introducing Domina Events][26]

Use Domina events for a more Clojure-ish approach to handling DOM events.

## [Tutorial 8 - DOM Manipulation][27]

Programmatically manipulate DOM elements in response to DOM events.

## [Tutorial 9 - Introducing AJAX][28]

Use AJAX to let the CLJS client-side code communicate with the server.

## [Tutorial 10 - A Deeper Understanding of Domina Events][29]

Apply Domina events to the login form example from the
[4th Tutorial][22].

## [Tutorial 11 - HTML on Top, Clojure on the Bottom][30]

Explore the highest (HTML5) and deepest (Clojure on the server) layers
of the login form example from the [previous tutorial][29].

## [Tutorial 12 - Don't Repeat Yourself][31]

Respect the [Don't Repeat Yourself (DRY) principle][44] by sharing
validators between the client-side CLJS and the server-side Clojure.

## [Tutorial 13 - Better Safe Than Sorry (Part 1)][32]

Set the stage for unit testing by learning about the `Enlive` template
sytem and starting the shopping calculator example. Use code
refactoring to satisfy the [DRY principle][44] and to solve a cyclic
namespaces dependency problem.

## [Tutorial 14 - Better Safe Than Sorry (Part 2)][33]

Add validators to the `shoppingForm`, and do some unit testing.

## [Tutorial 15 - Better Safe Than Sorry (Part 3)][35]

Configure a development environment that simultaneously satisfy in a
single JVM the Immediate Feedback Principle by Bret Victor and the
[Test Driven Development (TDD)][45].

## [Tutorial 16 - On pleasing TDD practitioners][36]

Make the [Test Driven Development][45] Environment more customizable.

## [Tutorial 17 - REPLing with Enlive][37]

Integrate validators into a web form in such a way that the
user will be notified with the corresponding help messages when they
enter invalid values in the form.

## [Tutorial 18 - Augmented TDD session][38]

Complete the client-side form validation by exploiting the [TDD][45]
environment augmented with CLJ/CLJS REPLs.

## [Tutorial 19 - Livin' on the edge][39]

Explain how to make a library compliant with the new Reader
Conditionals extension on CLJ/CLJS compilers.

## [Tutorial 20 - House Keeping][40]

Step by step guide for publishing a library to clojar repository by
using `boot`.

# License

Copyright © Mimmo Cosenza, 2012-2016. Released under the Eclipse Public
License, the same license as Clojure.


[1]: https://github.com/magomimmo/modern-cljs/tree/master/doc/second-edition
[2]: https://github.com/magomimmo/modern-cljs/tree/master/doc/first-edition
[3]: https://github.com/boot-clj/boot
[4]: http://leiningen.org/
[5]: https://github.com/clojure/clojurescript.git
[6]: https://github.com/clojure/clojurescript/wiki/Quick-Start#running-clojurescript-on-nodejs
[7]: http://www.clojurebook.com/
[8]: http://pragprog.com/book/shcloj2/programming-clojure
[9]: http://www.joyofclojure.com/
[10]: http://shop.oreilly.com/product/0636920025139.do
[11]: http://mitpress.mit.edu/sicp/
[12]: http://racket-lang.org/
[13]: http://mitpress.mit.edu/sicp/full-text/book/book.html
[14]: http://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-001-structure-and-interpretation-of-computer-programs-spring-2005/index.htm
[15]: http://www.paulgraham.com/onlisp.html
[16]: http://git-scm.com/
[17]: http://git-scm.com/documentation
[18]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[19]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-01.md
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-02.md
[21]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-04.md
[23]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-05.md
[24]: https://github.com/levand/domina
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-06.md
[26]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-07.md
[27]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-08.md
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
[29]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-10.md
[30]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-11.md
[31]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md
[32]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md
[33]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md
[34]: https://github.com/clojure/clojurescript.git
[35]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md
[36]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
[37]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-17.md
[38]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md
[39]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md
[40]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
[41]: https://github.com/clojure/clojurescript/wiki/Rationale
[42]: https://github.com/clojure/clojurescript/wiki/Quick-Start
[43]: https://en.wikipedia.org/wiki/Unix-like
[44]: https://en.wikipedia.org/wiki/Don%27t_repeat_yourself
[45]: https://en.wikipedia.org/wiki/Test-driven_development
