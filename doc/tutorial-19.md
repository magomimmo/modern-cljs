# Tutorial 19 - Survival guide for livin' on the edge 

In the [previous tutorial][1] we afforded two topics:

* the adoption of the [pieggieback][2] lib to improve the bREPLing
  experience with CLJS;
* the introduction of the [lein profiles][3] concept in the
  `project.clj` file to support a better separation of concerns during
  the lifecycle of a CLS/CLJS mixed project.

## Introduction

When you start using a lib implemented by others, you can easily end
up with few misunderstandings of its use or even with some unexpected
issues. In these cases, the first thing you should do is to browse and
read its documentation. As you now, one problem with open source
software regards the corresponding documentation which is frequently
minimal, if not absent, outdated or requiring a level of comprehension
of the details which you still have to grasp.

Likely, most of the CLJ/CLJS open source libs are hosted on github
which offers an amazing support for collaboration and social
coding. Even if only few CLJ/CLJS libs have an extensive documentation
and/or an associated mailing-list for submitting dubts and questions,
every CLJ/CLJS lib hosted on github is supported by an articulated,
although easy, issue and version control management systems. Those two
systems help a lot in managing almost any distributed and remote
collaboration requirements.

This tutorial is composed of two parts:

* Livin' on the edge (Part 1). In this part we're going to update the
  depenendencies of a set of libs which the `modern-cljs` depends on;
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

* the presence of the `:dev-dependencies` section which in `lein 2`
  has been deprecated by the introduction of [lein profiles][10];
* the presence of the `lein-cdt` plugin, which has been integrated
  into [swank-clojure][11] a lot of time ago and it is now deprecated
  too.

You should also note that the `shoreleave-remote` lib depends on the
[shoreleave-core][12] and the [shoreleave-browser][13] libs as well,
while the `shoreleave-remote-ring` lib further depends on the
[org.clojure.tool-reader][14] which is outdated too.

Nothing to worry about too much but, as I said, I prefer to use libs
frequently updated and maintained and this is a good opportunity to be
cooperative with the open source community.

Generally speaking, the updating of the dependencies of a project
requires little efforts which even a Clojure beginner can afford.

### Fork, clone and branch

I always fork and clone the libs I use in my projects. And I strongly
suggest to you to do the same.

Start by [forking][15] all the above `shoreleave` repos, then `git
clone` them all locally. Next add the corresponding upstream
repos. Assuming you've cloned the above repos into the `~/dev`
directory, issue the following commands from the terminal.

```bash
# shoreleave-core
cd ~/dev/shoreleave-core
git remote add upstream https://github.com/shoreleave/shoreleave-core.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to manage an issue

# shoreleave-browser
cd ~/dev/shoreleave-browser
git remote add upstream https://github.com/shoreleave/shoreleave-browser.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to manage an issue

# shoreleave-remote
cd ~/dev/shoreleave-remote
git remote add upstream https://github.com/shoreleave/shoreleave-remote.git
git pull upstream master
git push origin master
git checkout -b upgrade # create the branch to manage an issue

# shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
git remote add upstream https://github.com/shoreleave/shoreleave-remote-ring.git
git pull upstream master
git push origin master
git checkout -b upgrade # # create the branch to manage an issue
```

> NOTE 2: if you fork a repo immediately before to locally clone it, the
> `git pull upstream master` and `git push origin master` commands are
> optionals.

> NOTE 3: the `git checkout -b upgrade` command for creating a new
> branch from the master is not technically needed, but if you want to
> manage and issue and eventuallu pull request your solution to the
> owner of the repo, the github community strongly recommends it, and I
> agree with them.

### Shoreleave quirks

If you take a deeper look at the variuos `project.clj` files of the
cited `shoreleave` libs, you'll discover few characterizing quirks:

* even if only the `shoreleave-remote-ring` contains any CLJ code, all
  of them reference the "1.4.0" release of the Clojure language;
* none of the cited `shoreleave` libs references the `lein-cljsbuild`
  plugin;
* none the cited `shoreleave` libs defines any [tagged release][16]. 

#### Update shoreleave-core's `project.clj` file 

The first `shoreleave` lib we're going to work on is the most basic
(i.e.  `shoreleave-core`), because it is the only one not depending on
any other.

As you remember from the [Tutorial 18 - Housekeeping][1], we already
suggested to add the dependendencies and/or plugins you want to be
available in any project you're working on into the
`~/.lein/profiles.clj` file.

All the cited `shoreleave` libs use the [lein-marginalia][17]
plugin. `lein-marginalia` plugin is a *kind* of
[litarature programming][18] implementation that parses CLJ and CLJS
code and outputs a side-by-side source view with appropriate comments
and docstrings aligned.

`lein-marginalia` could be useful in other CLJ/CLJS projects as well
and I personally prefer, instead of adding it to the `:plugins`
section of the `:dev` profile in each `project.clj` file, to include
it in the `~/.lein/profiles.clj` file as follows:

```clj
{:user {:plugins [[lein-pprint "1.1.1"]
                  [lein-ancient "0.4.4"]
                  [lein-bikeshed "0.1.3"]
                  [lein-try "0.3.2"]
                  [lein-marginalia "0.7.1"]]}}
```

This first change allows us to remove from the `project.clj` of the
`shoreleave-core` lib even its dependency on the `"1.4.0"` release of
the Clojure programming language.

But what about a tagged release of the `shoreleave-core` lib itself?
As said, there are no explicit `tagged` releases for this lib (and
neither for all the other `shoreleave` libs).

At the moment, we're not going to change any *interface/API* in the
CLJS codebase og the lib. So, to be compliant with the
[Semantic Versioning][19] guidelines, we're going to label it as a
SNAPSHOT version (i.e. "0.3.1-SNAPSHOT").

`cd` in the `shoreleave-core` main directory and change the
corresponding `project.clj` as follows:

```clj
(defproject shoreleave/shoreleave-core "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's core auxiliary functions"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in
            LICENSE_epl.html"})
```

#### Where is my lein-cljsbuild plugin?

Wait a minute. From the very beginning of the `modern-cljs` series of
tutorials we have been learning about the `lein-cljsbuild` plugin as
the main tool ot be used for configuring and managing a CLJS-based
project. But we're dealing with a CLJS-based project which doesn't
even contain a reference to it. What's is going on here?

The fact is that all the cited pure CLJS-based `shoreleave` libs have
no unit testing code and contain only CLJS source code to be used by
CLJS-based projects. Those are the ones to be configured and managed
by the `lein-cljsbuild` plugin.

If we want to be really collaborative, we should keep our time and
start coding some unit testing code for each `shoreleave` lib. This
means a lot of work which, *mutatis mutandis*, should follow the
numerous steps explained in the
[Tutorial 16 - It's better to be safe than sorry (Part 3)][20].

If someone of you is so kind to work on it, I'll be very happy and
[Paul deGrandis][21] even more than me. But the one which is going to
be the happiest is yourself, beacause you'll become a CLJS unit
testing master.

That said, at minimum we can verify if our `shoreleave/shoreleave-core
"0.3.1-SNAPSHOT"` lib is still working in the context of the
`modern-cljs` codebase by first modifying all the others `shoreleave`
libs which are directly or indirectly used by the `modern-cljs`
project.

#### Update the remaining pure CLJS `shoreleave` libs

Modify the `project.clj` file of each `shoreleave` lib in the same way
we already did with the `shoreleave-core` one.

```clj
;;; shoreleave-browser project.clj
(defproject shoreleave/shoreleave-browser "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's enhanced browser utilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[shoreleave/shoreleave-core "0.3.1-SNAPSHOT"]])
```

> NOTE 4: As you see, we updated the `shoreleave-core` dependency to
> the newly created "0.3.1-SNAPSHOT" release.


```clj
;;; shoreleave-remote project.clj
(defproject shoreleave/shoreleave-remote "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[shoreleave/shoreleave-core "0.3.1-SNAPSHOT"]
                 [shoreleave/shoreleave-browser "0.3.1-SNAPSHOT"]])
```

> NOTE 5: As you see, we updated both the `shoreleave-core` and
> `shoreleave-browser` depenendencies to the newly created
> "0.3.1-SNAPSHOT".

#### Update the `shoreleave-remote-ring` lib

The last `shoreleave` lib we want to upgrade is the only one which
does not include any CLJS source code. Here is the new `project.clj`

```clj
(defproject shoreleave/shoreleave-remote-ring "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/shoreleave/shoreleave-remote-ring"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.7.7"]])
```

> NOTE 6: As you see, we update both the CLJ release to "1.5.1" and the
> `tools.reader` release to "0.7.7".

### Locally install the upgraded `shoreleave` libs

Now that we have upgraded all `shoreleave` libs which are used by the
`modern-cljs` project, we need to install them locally in such a way
that they can be seen by the `modern-cljs` `project.clj` file.

[Lein][21] offers a very handy `lein install` command to reach this
goal. Execute the following commands and you're almost done.

```bash
cd ~/dev/shoreleave-core
lein install
cd ~/dev/shoreleave-browser
lein install
cd ~/dev/shoreleave-remote
lein install
cd ~/dev/shoreleave-remote-ring
lein install
```

### Update the `modern-cljs` `project.clj` file

As a last change before lighting the fire is to update the `project.clj`
file of the `modern-cljs` project itself by updatating its references
to the used shoreleave libs.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [...
                 [shoreleave/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
                 [shoreleave/shoreleave-remote "0.3.1-SNAPSHOT"]
				 ...]
				 ...)
```

### Light the fire

Before to go on by launching the `modern-clj` project, let's inspect
the full project's dependencies by issuing the `lein deps :tree` command.

```bash
lein deps :tree
 ...
 [shoreleave/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
   [org.clojure/tools.reader "0.7.7"]
 [shoreleave/shoreleave-remote "0.3.1-SNAPSHOT"]
   [shoreleave/shoreleave-browser "0.3.1-SNAPSHOT"]
   [shoreleave/shoreleave-core "0.3.1-SNAPSHOT"]
```

Great. As you can verify by yourself all the direct
(i.e. `shoreleave-remote` and `shoreleave-remote-ring`) and the
inderect `shoreleave` dependencies (i.e. `shoreleave-core` and
`shoreleave-browser`) have been correctly updated to the
"0.3.1-SNAPSHOT" releases which have been locally installed by the
`lein install` commands.

Now cross your finger and issue the `lein clean-test!` command to run
the unit tests defined for the `modern-cljs` project after having
cleaned and recompiled everythig from schratch.

```bash
# from the main modern-cljs directory
lein clean-test!
Deleting files generated by lein-cljsbuild.
Rewriting test/cljx to target/test/clj (clj) with features #{clj} and 0 transformations.
Rewriting test/cljx to target/test/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
Compiling "target/test/js/testable.js" from ["src/cljs" "target/test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/Developer/modern-cljs/target/cljsbuild-compiler-0/cemerick/cljs/test.cljs
Successfully compiled "target/test/js/testable.js" in 17.022109 seconds.
Compiling "target/test/js/testable_pre.js" from ["src/brepl" "src/cljs" "target/test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/Developer/modern-cljs/target/cljsbuild-compiler-1/cemerick/cljs/test.cljs
Successfully compiled "target/test/js/testable_pre.js" in 6.951452 seconds.
Compiling "resources/public/js/modern_dbg.js" from ["src/brepl" "src/cljs"]...
Successfully compiled "resources/public/js/modern_dbg.js" in 3.653896 seconds.
Compiling "target/test/js/testable_dbg.js" from ["src/brepl" "src/cljs" "target/test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/Developer/modern-cljs/target/cljsbuild-compiler-3/cemerick/cljs/test.cljs
Successfully compiled "target/test/js/testable_dbg.js" in 2.998267 seconds.
Compiling "resources/public/js/modern_pre.js" from ["src/brepl" "src/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 5.327515 seconds.
Compiling "resources/public/js/modern.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 4.758359 seconds.
Compiling ClojureScript.

lein test modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Running all ClojureScript tests.
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
```

The `shoreleave` libs updates don't seem to have broken the previous
code. You can even try to interact with the `modern-cljs` Login and
Shopping Calculator forms to verify that everything is still working
as expected.

Obviuosly, this kind of runs can't be considered in any way something
to count on. I leave to you both the honor and the burden to fill the
gaps by adding the CLJS unit testing code as described in few of the
previous tutorials of the series.

### Commit the changes

It's noew time to commit all the implemented changes.

```bash
cd ~/dev/shoreleave-core
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
cd ~/dev/shoreleave-browser
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
cd ~/dev/shoreleave-remote
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
cd ~/dev/shoreleave-remote-ring
git commit -am "removed lein-marginalia and clj updated to 1.5.1"
cd ~/dev/modern-cljs
git commit -am "totorial-19-step-1"
```

NOTE 7: 


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
