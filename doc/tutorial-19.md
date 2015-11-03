# Tutorial 19 - Livin' On the Edge

In the [previous tutorial][1] we afforded two topics:

* the adoption of the [pieggieback][2] lib to improve the bREPLing
  experience with CLJS;
* the introduction of the [lein profiles][3] concept in the
  `project.clj` file to support a better separation of concerns during
  the lifecycle of a CLS/CLJS mixed project.

## Introduction

When you start using a lib implemented by others, you can easily end
up with few misunderstandings of its use or even with some unexpected
issues. In these cases the first thing you should do is to browse and
read its documentation. As you know, one problem with open source
software regards the corresponding documentation which is frequently
minimal, if not absent, outdated or requiring a level of comprehension
of the details which you still have to grasp.

Likely, most of the CLJ/CLJS open source libs are hosted on
[github][27] which offers an amazing support for collaboration and
social coding. Even if only few CLJ/CLJS libs have an extensive
documentation and/or an associated mailing-list for submitting doubts
and questions, every CLJ/CLJS lib hosted on github is supported by an
articulated, although easy, issue and version control management
systems. Those two systems help a lot in managing almost any
distributed and remote collaboration requirements.

This tutorial is composed of two parts:

* *Livin' on the edge*. In this part we're going to update the
  dependencies of a set of libs which the `modern-cljs` depends on;
* *A Survival guide*. In this part we're going to see what to do when
  the responsiveness of the owner of a lib for which we have submitted
  a pull request is not compatible with our project schedule or when
  she/he does not accept to merge into the master branch.

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

Because the `tools.reader` release (i.e. `"0.7.0"`) used by the
`shoreleave-remote-ring` lib is not compatible with the latest CLJS
releases (e.g. `"0.0-2069"`), we were forced to use a
non canonical set of the `shoreleave` libs which I published on
`clojars` repository to be able to use a recent CLJS release
(e.g. `"0.0-2069"`).

But even if a lib does not create any issue in my projects, I always
prefer to use libs which are up-to-date with the latest available
release of the libs they depend on.

If you take a look at the `project.clj` file of the
[shoreleave-remote][5] and the [shoreleave-remote-ring][6], you'll
discover that they are both based on the obsolete `lein 1` release and
on the `1.4` release of Clojure.

Here is the [shoreleave-remote][5] `project.clj`:

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

And here is the [shoreleave-remote-ring][6] `project.clj`:

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

* the presence of the `:dev-dependencies` section, which in `lein 2`
  has been deprecated by the introduction of [lein profiles][3];
* the presence of the `lein-cdt` plugin, which has been integrated
  into `swank-clojure` a lot of time ago and it is now deprecated too.

You should also note that the `shoreleave-remote` lib depends on the
[shoreleave-core][8] and the [shoreleave-browser][9] libs as well,
while the `shoreleave-remote-ring` lib further depends on the
[org.clojure/tool-reader][10] which is outdated too.

Nothing to worry about too much but, as I said, I prefer to use libs
frequently updated and maintained and this is a good opportunity to be
cooperative with the open source community.

Generally speaking, the updating of the dependencies of a project
requires little efforts which even a Clojure beginner can afford. In
this tutorial we're going to use the `shoreleave` libs as a use case
for introducing the collaboration process.

### Fork, clone and branch

I always fork and clone the libs I use in my projects. And I strongly
suggest to you to do the same.

Start by [forking][11] all the above `shoreleave` repos. Then `git
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
> manage and issue and eventually pull request your solution to the
> owner of the repo, the github community strongly recommends it, and I
> agree with them.

### Shoreleave quirks

If you take a deeper look at the variuos `project.clj` files of the
cited `shoreleave` libs, you'll discover few characterizing quirks:

* even if only the `shoreleave-remote-ring` contains any CLJ code, all
  of them reference the "1.4.0" release of the Clojure language;
* none of the cited `shoreleave` libs references the `lein-cljsbuild`
  plugin;
* none the cited `shoreleave` libs defines any [tagged release][12].

#### Update shoreleave-core's `project.clj` file

The first `shoreleave` lib we're going to work on is the most basic
(i.e.  `shoreleave-core`), because it is the only one not depending on
any other.

As you remember from the [Tutorial 18 - Housekeeping][1], we already
suggested to add the dependendencies and/or plugins you want to be
available in any project you're working on into the
`~/.lein/profiles.clj` file.

All the cited `shoreleave` libs use the [lein-marginalia][13]
plugin. `lein-marginalia` plugin is a *kind* of
[literate programming][14] implementation that parses CLJ and CLJS
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

> NOTE 4: This first change allows us to remove from the `project.clj`
> of the `shoreleave-core` lib its dependency on the `"1.4.0"` release
> of the Clojure programming language as well.

But what about a tagged release of the `shoreleave-core` lib itself?
As said, there are no explicit `tagged` releases for this lib (and
neither for all the others `shoreleave` libs).

At the moment, we're not going to change any *interface/API* in the
codebase of the lib. So, to be compliant with the
[Semantic Versioning][15] guidelines, we're going to label it as a
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
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"})
```

#### Where is my lein-cljsbuild plugin?

Wait a minute. From the very beginning of the `modern-cljs` series of
tutorials we have been learning about the `lein-cljsbuild` plugin as
the main tool to be used for configuring and managing a CLJS-based
project. We're now dealing with a CLJS-based project which doesn't
even contain a reference to it. What's is going on here?

The fact is that all the cited pure CLJS-based `shoreleave` libs only
contains CLJS source code to be used by other projects, which are the
ones to be configured and managed by the `lein-cljsbuild` plugin.

That said, I would still have expected to see the `lein-cljsbuild`
plugin to be used for configuring and managing the CLJS unit testing
code, but none of the cited `shoreleave` lib contains any unit
tests. Considering that the `shoreleave` libs have been published when
the `clojurescript.test` lib was still in the [Chas Emerick's][16]
head only, this is not a surprise.

If we want to be really collaborative, we should keep our time and
start coding some unit testing code for each `shoreleave` lib. This
means a lot of work which, *mutatis mutandis*, should follow the
numerous steps explained in the
[Tutorial 16 - It's better to be safe than sorry (Part 3)][17].

If someone of you is so kind to work on it, I'll be very happy and
[Paul deGrandis][18] even more than me. But the one which is going to
be the happiest is yourself, because you'll become a CLJS unit
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

> NOTE 5: As you see, we updated the `shoreleave-core` dependency to
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

> NOTE 6: As you see, we updated both the `shoreleave-core` and
> `shoreleave-browser` dependencies to the newly created
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
                 [org.clojure/tools.reader "0.8.0"]])
```

> NOTE 7: As you see, we update both the CLJ release to "1.5.1" and the
> `tools.reader` release to "0.8.0".

### Locally install the upgraded `shoreleave` libs

Now that we have upgraded all `shoreleave` libs which are used by the
`modern-cljs` project, we need to locally install them in such a way
that they can be seen by the `modern-cljs` `project.clj` file.

Leiningen offers a very handy `lein install` command to reach this
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

### Update the `project.clj` file of the `modern-cljs` project

As a last change before lighting the fire, update the `project.clj`
file of the `modern-cljs` project itself by upgrading its references
to the newly crerated shoreleave SNAPSHOT releases.

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

Before going on by launching the `modern-clj` project, let's inspect
the full project's dependencies by issuing the `lein deps :tree` command.

```bash
cd ~/dev/modern-cljs
lein deps :tree
 ...
 [shoreleave/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
   [org.clojure/tools.reader "0.8.0"]
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

Now cross your finger and issue the `lein clean-test!` command to
clean, compile and run from scratch the unit tests defined for the
`modern-cljs`.

```bash
lein clean-test!
Deleting files generated by lein-cljsbuild.
Rewriting test/cljx to target/test/clj (clj) with features #{clj} and 0 transformations.
Rewriting test/cljx to target/test/cljs (cljs) with features #{cljs} and 1 transformations.
...
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

The updates of the `shoreleave` libs don't seem to break the
code. This is not a surprise, because we have not touched any CLJS
source code. You can even try to interact with the `modern-cljs`
*Login* and *Shopping Calculator* forms to verify that everything is
still working as expected, or run a `bREPL` connection as explained in
the [previous tutorial][1] to directly interact with the DOM.

Obviuosly, this kind of tests can't be considered in any way something
to count on. I leave to you the honor and the burden to fill the gap
by adding the CLJS unit testing code as described in few of the
previous tutorials of the series.

### Commit the changes

It's now time to commit all the implemented changes and to push them
to the forked repo.

```bash
# shoreleave-core
cd ~/dev/shoreleave-core
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
git push origin upgrade

# shoreleave-browser
cd ~/dev/shoreleave-browser
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
git push origin upgrade

# shoreleave-remote
cd ~/dev/shoreleave-remote
git commit -am "removed lein-marginalia and clj 1.4.0 deps"
git push origin upgrade

# shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
git commit -am "removed lein-marginalia and clj updated to 1.5.1"
git push origin upgrade

# modern-cljs
cd ~/dev/modern-cljs
git commit -am "Step 1"
```

### What's next?

Let's summarize what we already did:

* we forked and cloned all the direct and indirect `shoreleave` libs
  which the `modern-cljs` depends on;
* we created an `upgrade` branch for each forked/cloned `shoreleave`
  lib;
* we updated the `project.clj` content of each `shoreleave` lib by
  removing the CLJ "1.4.0" and the `lein-marginalia` dependencies;
* we marked all the interested `shoreleave` lib as `"0.3.1-SNAPSHOT"`
  to adhere to the semantic version styleguide;
* we updated the `shoreleave-remote-ring` to the latest available
  releases of its dependencies;
* we locally installed all the updated `shoreleave` libs;
* we changed the `modern-cljs` project by updating its references to
  the `shoreleave` libs
* we cleaned up, recompiled and ran the `modern-cljs` unit tests to
  see if the `shoreleave` updates break the code;
* we committed and pushed against the corresponding forked repos the
  updated `shoreleave` libs.

In a real scenario, we should now [pull request][19] the
[upstream repos][20] for each modified `shoreleave` lib and wait until
the owner of the repos will accept and merge our pull requests.

But what if the owner is lazy or for any reason she/he does not agree
to merge our pull requests?

The above modified repos only live locally on your computer and they
can't be directly shared with other developers or even with other
computers.

You have more options:

* you can publish a lib on the [clojars][21] public repository. This
  way the lib will be available to everybody;
* you can publish a lib on a private repository. This way the
  accessability to the updated libs is governed by the rules defined
  in the repository itself. Generally this is the right choice when
  you want to make a lib available to other devs without making it
  public.

In the next part of this tutorial we're going to inspect the first
option only.

## A Survival guide

The process of publishing a CLJ/CLJS lib on [clojars][21] is pretty
simple and even if it is fully explained in the [lein tutorial][22],
I'm going to summarize its characteristics and the most fundamental
steps.

Keep in mind that any release ending in "-SNAPSHOT" is not an official
release and you should rely on them only when you really need (which
is not our fictional scenario). Also remember that by adding a
snapshot dependency to your project, you will cause `lein` to slow
down its dependencies search.

Clojars offers two repositories, [Classic][25] and [Releases][26]. The
Classic repository, which is the one we're going to use, has no
restrictions and anyone may publish a lib into it.

That said, if you want to push your own version of somebody else's
lib, which is our case, you should qualify the project name by putting
`org.clojars.<your-username>/` in front of the project name in its
`project.clj` file.

### Create the branches for publishing on clojars

The above requirement forces us to go back, modify the name of the
project for each lib and consequently update any reference to them in
the others libs. Due to the fact that we eventually want to pull
request the upstreams repos, we need to create a new branch for each
modified lib to be published on clojars as follows:

```bash
cd ~/dev/shoreleave-core
git checkout -b clojars

cd ~/dev/shoreleave-browser
git checkout -b clojars

cd ~/dev/shoreleave-remote
git checkout -b clojars

cd ~/dev/shoreleave-remote-ring
git checkout -b clojars
```

Now open each `project.clj` and update their project name and
dependencies by using your clojars' group name as follows:

```clj
;;; shoreleave-core
(defproject org.clojars.magomimmo/shoreleave-core "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's core auxiliary functions"
  :url "https://github.com/magomimmo/shoreleave-core/tree/clojars"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"})
```

> NOTE 9: As you see we updated the `:url` attribute of the project as
> well.

> NOTE 10: Obviously, your group-id will be different from mine which is
> `org.cljars.magomimmo`.

```clj
;;; shoreleave-browser
(defproject org.clojars.magomimmo/shoreleave-browser "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's enhanced browser utilities"
  :url "https://github.com/magomimmo/shoreleave-browser/tree/clojars"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojars.magomimmo/shoreleave-core "0.3.1-SNAPSHOT"]])
```

```clj
;;; shoreleave-remote
(defproject org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "https://github.com/magomimmo/shoreleave-remote/tree/clojars"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojars.magomimmo/shoreleave-core "0.3.1-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-browser "0.3.1-SNAPSHOT"]])
```

```clj
;;; shoreleave-remote-ring
(defproject org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/magomimmo/shoreleave-remote-ring/tree/clojars"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.8.0"]])
```

As usual commit your changes.

```bash
# shoreleave-core
cd ~/dev/shoreleave-core
git commit -am "added group-id name"
git push origin clojars

# shoreleave-browser
cd ~/dev/shoreleave-browser
git commit -am "added group-id name"
git push origin clojars

# shoreleave-remote
cd ~/dev/shoreleave-remote
git commit -am "added group-id name"
git push origin clojars

# shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
git commit -am "added group-id name"
git push origin clojars
```

We can now move to the next step.

### Register and publish on clojars.org

To publish a library on [clojars][21] you first need to
[register with it][23] and then you're ready to publish the snapshots
on clojars by using the very handy `lein deploy clojars` command as
follows:

```bash
# shoreleave-core
cd ~/dev/shoreleave-core
lein deploy clojars
No credentials found for clojars (did you mean `lein deploy clojars`?)
See `lein help deploy` for how to configure credentials.
Username: magomimmo
Password:
Wrote /Users/mimmo/Developer/shoreleave-core/pom.xml
Created /Users/mimmo/Developer/shoreleave-core/target/shoreleave-core-0.3.1-SNAPSHOT.jar
Could not find metadata org.clojars.magomimmo:shoreleave-core:0.3.1-SNAPSHOT/maven-metadata.xml in clojars (https://clojars.org/repo/)
Sending org/clojars/magomimmo/shoreleave-core/0.3.1-SNAPSHOT/shoreleave-core-0.3.1-20130925.070534-1.pom (3k)
    to https://clojars.org/repo/
Sending org/clojars/magomimmo/shoreleave-core/0.3.1-SNAPSHOT/shoreleave-core-0.3.1-20130925.070534-1.jar (10k)
    to https://clojars.org/repo/
Retrieving org/clojars/magomimmo/shoreleave-core/maven-metadata.xml (1k)
    from https://clojars.org/repo/
Sending org/clojars/magomimmo/shoreleave-core/0.3.1-SNAPSHOT/maven-metadata.xml (1k)
    to https://clojars.org/repo/
Sending org/clojars/magomimmo/shoreleave-core/maven-metadata.xml (1k)
    to https://clojars.org/repo/
```

As you see, you're asked for your clojars' credential, but it's also
possibile to have them read from an [encrypted file][24].

Now repeat del `lein deploy clojars` command for the remaining
snapshot release of the modified `shoreleave` libs.

```bash
# shoreleave-browser
cd ~/dev/shoreleave-browser
lein deploy clojars
...

# shoreleave-remote
cd ~/dev/shoreleave-remote
lein deploy clojars
...

# shoreleave-remote-ring
cd ~/dev/shoreleave-remote-ring
lein deploy clojars
```

Next verify that your `shoreleave` shanpshot releases are available on
`clojars` by issuing the following `lein search` command:

```bash
lein search group:org.clojars.magomimmo
Updating the search index. This may take a few minutes...
Searching over Group ID...
 == Showing page 1 / 1
...
[org.clojars.magomimmo/shoreleave-core "0.3.1-SNAPSHOT"] A smarter client-side with ClojureScript : Shoreleave's core auxiliary functions
[org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"] A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support
[org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"] A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities
[org.clojars.magomimmo/shoreleave-browser "0.3.1-SNAPSHOT"] A smarter client-side with ClojureScript : Shoreleave's enhanced browser utilities
```

> NOTE 11: The first time you run the `lein search` command it will take
> a lot of time to return results because it has to create the index.

> NOTE 12: Obviously you should search for your clojars' group name,
> not for mine.

### Update the modern-cljs project

Having published all the snapshot releases of the `shoreleave` libs
directly on indirectly used in the `modern-cljs` project, we now have
to modify its `project.clj` by updating the group-id of its
dependencies as follows:

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [...
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"]
                 ...]

  ...)
```

> NOTE 13: Obviously, as group-id you should use your clojars' group
> name, not mine.


### Light the fire

You're now ready to light the fire again for verifying if everything
is still working as expected by issuing the usual `lein clean-test!`
alias command:

```bash
lein clean-test!
Deleting files generated by lein-cljsbuild.
Retrieving org/clojars/magomimmo/shoreleave-remote-ring/0.3.1-SNAPSHOT/shoreleave-remote-ring-0.3.1-20130925.094608-2.pom from clojars
Retrieving org/clojars/magomimmo/shoreleave-remote/0.3.1-SNAPSHOT/shoreleave-remote-0.3.1-20130925.094521-2.pom from clojars
Retrieving org/clojars/magomimmo/shoreleave-remote-ring/0.3.1-SNAPSHOT/shoreleave-remote-ring-0.3.1-20130925.094608-2.jar from clojars
Retrieving org/clojars/magomimmo/shoreleave-remote/0.3.1-SNAPSHOT/shoreleave-remote-0.3.1-20130925.094521-2.jar from clojars
Rewriting test/cljx to target/test/clj (clj) with features #{clj} and 0 transformations.
Rewriting test/cljx to target/test/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
...
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

Great. As you see, `lein` correctly retrieved the snapshot releases of
the `shoreleave-remote` and `shoreleave-remote-ring` that we just
published on clojars.

You can even verify that those libs have been cashed in your
`~/.m2/repository` by issuing the following command (remember to
subtitute my clojars group id with yours).

```bash
ls -la ~/.m2/repository/org/clojars/magomimmo/shoreleave*
/Users/mimmo/.m2/repository/org/clojars/magomimmo/shoreleave-browser:
total 24
drwxr-xr-x   7 mimmo  staff  238 Sep 25 11:43 .
drwxr-xr-x   7 mimmo  staff  238 Sep 18 19:26 ..
drwxr-xr-x  12 mimmo  staff  408 Sep 25 11:45 0.3.1-SNAPSHOT
-rw-r--r--   1 mimmo  staff  339 Sep 25 11:43 maven-metadata-clojars.xml
-rw-r--r--   1 mimmo  staff   40 Sep 25 11:43 maven-metadata-clojars.xml.sha1
-rw-r--r--   1 mimmo  staff  180 Sep 25 11:43 resolver-status.properties

/Users/mimmo/.m2/repository/org/clojars/magomimmo/shoreleave-core:
total 24
drwxr-xr-x   7 mimmo  staff  238 Sep 25 11:42 .
drwxr-xr-x   7 mimmo  staff  238 Sep 18 19:26 ..
drwxr-xr-x  12 mimmo  staff  408 Sep 25 11:42 0.3.1-SNAPSHOT
-rw-r--r--   1 mimmo  staff  412 Sep 25 11:42 maven-metadata-clojars.xml
-rw-r--r--   1 mimmo  staff   40 Sep 25 11:42 maven-metadata-clojars.xml.sha1
-rw-r--r--   1 mimmo  staff  180 Sep 25 11:42 resolver-status.properties

/Users/mimmo/.m2/repository/org/clojars/magomimmo/shoreleave-remote:
total 24
drwxr-xr-x   7 mimmo  staff  238 Sep 25 11:45 .
drwxr-xr-x   7 mimmo  staff  238 Sep 18 19:26 ..
drwxr-xr-x  12 mimmo  staff  408 Sep 25 12:25 0.3.1-SNAPSHOT
-rw-r--r--   1 mimmo  staff  338 Sep 25 11:45 maven-metadata-clojars.xml
-rw-r--r--   1 mimmo  staff   40 Sep 25 11:45 maven-metadata-clojars.xml.sha1
-rw-r--r--   1 mimmo  staff  180 Sep 25 11:45 resolver-status.properties

/Users/mimmo/.m2/repository/org/clojars/magomimmo/shoreleave-remote-ring:
total 24
drwxr-xr-x   7 mimmo  staff  238 Sep 25 11:46 .
drwxr-xr-x   7 mimmo  staff  238 Sep 18 19:26 ..
drwxr-xr-x  12 mimmo  staff  408 Sep 25 12:25 0.3.1-SNAPSHOT
-rw-r--r--   1 mimmo  staff  343 Sep 25 11:46 maven-metadata-clojars.xml
-rw-r--r--   1 mimmo  staff   40 Sep 25 11:46 maven-metadata-clojars.xml.sha1
-rw-r--r--   1 mimmo  staff  180 Sep 25 11:46 resolver-status.properties
```

You can finally commit the changes by issuing the usual `git` commands
as follows:

```bash
git add .
git commit -m "Step 2"
```

That's all. Stay tuned for the next tutorial of the series.

# Next Step - [Tutorial 20 - Learn by Contributing (Part 1)][28]

In the next tutorial, while studying the [Enfocus][29] lib with the
objective of sharing as much code as possibile with the corresponding
[Enlive][30] lib, we'll start to be collaborative by proposing few
improvements to the `Enfocus` directories layout and the adoption of
the [clojurescript.test][31] lib for implementing its unit tests.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md
[2]: https://github.com/cemerick/piggieback
[3]: https://github.com/technomancy/leiningen/blob/stable/doc/PROFILES.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[5]: https://github.com/shoreleave/shoreleave-remote
[6]: https://github.com/shoreleave/shoreleave-remote-ring
[7]: https://github.com/shoreleave
[8]: https://github.com/shoreleave/shoreleave-core
[9]: https://github.com/shoreleave/shoreleave-browser
[10]: https://github.com/clojure/tools.reader
[11]: https://help.github.com/articles/fork-a-repo
[12]: http://git-scm.com/book/en/Git-Basics-Tagging
[13]: https://github.com/gdeer81/lein-marginalia
[14]: http://en.wikipedia.org/wiki/Literate_programming
[15]: http://semver.org/
[16]: https://github.com/cemerick
[17]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md
[18]: https://github.com/ohpauleez
[19]: https://help.github.com/articles/be-social#pull-requests
[20]: https://help.github.com/articles/syncing-a-fork
[21]: https://clojars.org/
[22]: https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md#publishing-libraries
[23]: https://clojars.org/register
[24]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#authentication
[25]: http://clojars.org/repo/
[26]: http://releases.clojars.org/repo/
[27]: https://github.com/
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-20.md
[29]: https://github.com/ckirkendall/enfocus
[30]: https://github.com/cgrand/enlive
[31]: https://github.com/cemerick/clojurescript.test
