# Tutorial 19 - Survival guide for livin' on the edge 

In the [previous tutorial][1] we afforded two topics:

* the adption of the [pieggieback][2] lib to improve the bREPLing
  experience with CLJS;
* the introduction of the [lein profiles][3] features in the
  `project.clj` file to support a better separation of concerns during
  the lifecycle of a CLS/CLJS mixed project.

## Introduction

When in your project you start using a lib implemented by others, you
can easily end up with few misunderstandings of its use or even with
some unexpected issues. In these cases, the first thing you should do
is to browse and read its documentation. As you now, one problem with
open source software regards the corresponding documentation which is
frequently minimal, if not absent, outdated or requiring a level of
comprehension of the details which you still have to grasp.

Likely, most of the CLJ/CLJS open source libs are hosted on github
which offers an amazing support to collaboration and social
coding. Even if only few CLJ/CLJS libs have an extensive documentation
and/or an associated mailing-list for submitting dubts and questions,
every CLJ/CLJS lib hosted on github is supported by an articulated,
although easy, issue and version control management systems. Those two
systems help a lot in managing almost any kind of distributed and
remote collaboration required by an open source software.

This tutorial is composed of two parts:

* Livin' on the edge (Part 1). In this part we're going to update the
  depenendencies of a set of libs and we'll also fix an issue of a lib
  on which the `modern-cljs` depends on;
* Surviving guide (Part 2). In this part we're going to see what to do
  when the responsiveness of the owner of a lib for which we have
  submitted a push request is not compatible with our project
  schedule.

## Livin' on the edge

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-18
> git checkout -b tutorial-19-step-1
> ```

As you remember, in the [Tutorial 10 - Introducing Ajax][4] we added
to the project's dependencies the [shoreleave-remote][5] and the
[shoreleave-remote-ring][6] libs.

Even if those set of [shoreleave][7] libs did not create any issue in
our project, I always like to use libs which are up-to-date with the
latest available release of the external libs they use.

If you take a look at the `project.clj` file of the
[shoreleave-remote][5] and the [shoreleave-remote-ring][6], you'll
discover that they are both based on the obsolete [lein 1][8] release
and on the `1.4` release of Clojure.

Here is the [shoreleave-remote][8] `project.clj`:

```clj
(defproject shoreleave/shoreleave-remote "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [shoreleave/shoreleave-core "0.3.0"]
                 [shoreleave/shoreleave-browser "0.3.0"]]
  :dev-dependencies [;[cdt "1.2.6.2-SNAPSHOT"]
                     ;[lein-cdt "1.0.0"] ; use lein cdt to attach
                     ;[lein-autodoc "0.9.0"]
                     [lein-marginalia "0.7.1"]])
```

And here is the [shoreleave-remote-ring][9] `project.clj`:

```clj
(defproject shoreleave/shoreleave-remote-ring "0.3.0"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/shoreleave/shoreleave-remote-ring"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.reader "0.7.0"]]
  :dev-dependencies [;[cdt "1.2.6.2-SNAPSHOT"]
                     ;[lein-cdt "1.0.0"] ; use lein cdt to attach
                     ;[lein-autodoc "0.9.0"]
                     [lein-marginalia "0.7.1"]])
```

You detect the fact that they both depend on `lein 1` from few
symptoms:

* the presence of the `:dev-dependencies` section which, in `lein 2`,
  has been deprecated by the introduction of [lein profiles][10];
* the presence of the `lein-cdt` plugin, which has been integrated
  into [swank-clojure][11] a lot of time ago and it now deprecated
  too.

You should also note that the `shoreleave-remote` lib depends on the
[shoreleave-core][12] and the [shoreleave-browser][13] libs as well,
while the `shoreleave-remote-ring` lib further depends on the
[org.clojure.tool-reader][14].

Nothing to worry about too much but, as I said, I prefer to use libs
frequently updated and maintained and this is a good opportunity to be
cooperative with the open source community.

Generally speaking, the updating of the dependencies of a project
requires little efforts which even a Clojure beginner can afford.

### Fork, clone and branch

I always fork and clone the libs I used in my projects. And I strongly
suggest to you to do the same.

Start by [forking][15] all the above `shoreleave` repos, then `git
clone` them all locally. Next add the corresponding upstream
repos. Assuming you've cloned the above repos into `~/dev` directory,
issue the following commands from the terminal.

```bash
# shoreleave-core
cd ~/dev/shoreleave-core
git remote add upstream https://github.com/shoreleave/shoreleave-core.git
git pull upstream master
git push origin master
git checkout -b my-upgrade # create the branch to fix the issue

# shoreleave-browser
cd ~/dev/shoreleave-browser
git remote add upstream https://github.com/shoreleave/shoreleave-browser.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to fix the issue

# shoreleave-remote
cd ~/dev/shoreleave-remote
git remote add upstream https://github.com/shoreleave/shoreleave-remote.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to fix the issue

# shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
git remote add upstream https://github.com/shoreleave/shoreleave-remote-ring.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to fix the issue
```

> NOTE 2: if you fork a repo immediately before to locally clone it, the
> `git pull upstream master` and `git push origin master` commands are
> optionals.

> NOTE 3: the `git checkout -b upgrade` command for creating a new
> branch from the the master is not technically needed, but the github
> community strongly recommends it and I agree with them.

It's now time to upgrade the `project.clj` for each forked repo.

### Make the changes

We start editing the `project.clj` from the `shoreleave-core` lib,
because it's the only one that does not depend on any other
`shoreleave` lib.

Open the `project.clj` from the `~/dev/shoreleave-core` directory and
modify its content as follows:

```clj
(defproject shoreleave/shoreleave-core "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's core auxiliary functions"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

> NOTE 4: I added the `:min-lein-version "2.0.0"` option because we are
> now using the `:profiles` features which is available starting from
> the `lein 2.0.0` release.

> NOTE 5: I kept the [lein-marginalia][16] plugin in the `:dev` profile,
> even if I would have preferred to move it in the `:user` profile
> inside the `~/.lein/profiles.clj`.

> NOTE 6: the `shoreleave` set of libs does not follow the usual habit
> of creating a tag for each release. The master branch should have been
> named `0.3.1-SNAPSHOT` instead. 

*Mutatis mutandis*, repeat the same kind of changes in the
`project.clj` files of the `shoreleave-browser`, `shoreleave-remote`
and `shoreleave-remote-ring` libs as well.
 
```clj
;;; shoreleave-browser
(defproject shoreleave/shoreleave-browser "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's enhanced browser utilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [shoreleave/shoreleave-core "0.3.0"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

```clj
;;; shoreleave-remote
(defproject shoreleave/shoreleave-remote "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [shoreleave/shoreleave-core "0.3.0"]
                 [shoreleave/shoreleave-browser "0.3.0"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

```clj
;;; shoreleave-remote-ring
(defproject shoreleave/shoreleave-remote-ring "0.3.0"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/shoreleave/shoreleave-remote-ring"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.7.7"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

> NOTE 7: I updated the `tools.reader` lib to the latest available
> release which is the `0.7.7`.

If you now think to be now ready to pull request your changes, you're
wrong. We still have to test the changes.

### Test before pull request

If you take a look at the codebase of the above `shoreleave` you'll discover:

1. the `project.clj` does not contains the `lein-cljsbuild` plugin
   needed to emit any JS code from the CLJS codebase;
1. the test directories of all the `shoreleave` libs do not contain
   any unit testing code.

If we want to be really collaborative, we should keep our time and
start coding some unit testing code for each upgraded lib. This means
a lot of work which, *mutatis mutandis*, should follow the numerous
steps explained in the
[Tutorial 16 - It's better to be safe than sorry (Part 3)][17]
customized to the context of the `shoreleave` libs by taking
inspiration from the way we used them in the various `modern-cljs`
tutorials concerning ajax communication.

If someone of you is so kind to make it, I'll be very happy and
[Paul deGrandis][18] more than me. But the one is going to be the
happiest is yourself, beacause you'll become a CLJS unit testing
master.

That said, at least we can verify if the upgraded `shoreleave` libs
are still comiling and working in the context of the `modern-cljs`
project.

#### Commit the changes

First, commit the changes in all the `upgrade` branches of the above
`shoreleave` libs. 

```bash
# commit shoreleave-core
cd ~/dev/shoreleave-core
git commit -am "lein project upgraded to 2.0.0, CLJ dependency updated to 1.5.1"
# commit shoreleave-browser
cd ~/dev/shoreleave-browser
git commit -am "lein project upgraded to 2.0.0, CLJ dependency updated to 1.5.1"
# commit shoreleave-remote
cd ~/dev/shoreleave-remote
git commit -am "lein project upgraded to 2.0.0, CLJ dependency updated to 1.5.1"
# commit shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
git commit -am "lein project upgraded to 2.0.0, CLJ dependency updated to 1.5.1"
```

Next we need to understand how to inform the `modern-cljs` project to
use the just upgraded libs instead of the ones downloded from the
public repositories.

#### Locally install the upgraded libs

In [lein 2][NN] this is very easy. Just issue the `lein install` command
from the main project directory of each upgraded lib.

```bash
# locally install shoreleave-core
cd ~/dev/shoreleave-core
lein install
# locally install shoreleave-browser
cd ~/dev/shoreleave-browser
lein install
# locally install shoreleave-remote
cd ~/dev/shoreleave-remote
lein install
# locally install shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
lein install
```

#### Test modern-cljs project

We are now ready to verify if the upgraded libs are still working at
least when used by the modern-clj project.

```bash
# cd into the modern-cljs main project directory
lein do clean, cljx once, compile


As a very last step, I suggest you to commit the changes as follows:

```bash
git add .
git commit -m "housekeeping"
```

That's all. Stay tuned for the next tutorial of the series.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md


[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[5]: https://github.com/technomancy/leiningen
[6]: https://github.com/emezeske/lein-cljsbuild
[7]: https://github.com/cemerick/piggieback/blob/master/README.md
[8]: https://github.com/clojure/tools.nrepl
[9]: https://github.com/eslick
[10]: http://ianeslick.com/2013/05/17/clojure-debugging-13-emacs-nrepl-and-ritz/
[11]: https://github.com/cemerick
[12]: https://github.com/cemerick/piggieback
[13]: http://localhost:3000/shopping-dbg.html
[14]: https://github.com/technomancy/leiningen/blob/stable/doc/PROFILES.md
[15]: https://github.com/rkneufeld/lein-try
[16]: https://github.com/technomancy/leiningen/tree/master/lein-pprint
[17]: https://github.com/xsc/lein-ancient
[18]: https://github.com/dakrone/lein-bikeshed
[19]: https://github.com/clojure/tools.nrepl#middleware
[20]: https://github.com/lynaghk/cljx
[21]: http://localhost:3000/shopping-dbg.html
