# Tutorial 20 - Learn by collaborating

In the [previous tutorial][1] we described a couple of approaches to
survive while livin' on the edge of a continuosly changing CLJ/CLJS
libs used as dependencies in our projects. We ended up by publishing
to [clojars][2] a set of four [shoreleave][3] libs which the
`modern-cljs` project directly or indirectly depends on. This way you
can become collaborative with the CLJ/CLJS communities and, at the
same time, more indipendent from someone else's decision to merge or
refuse your pull requests.

In this tutorial we're getting back to CLJS.

## Introduction

In the [Tutorial 14 - Its better to be safe than sorry (Part 1)][4] we
introduce the [enlive][5] templating system for Clojure. Then, in the
[Tutorial 17 - Enlive by REPLing][6] we encapsuleted into the `enlive`
templating system the form's input validators for the `Shopping
Calculator`.

As you remember, even if we were able to share the same codebase of
the validators and their corresponding unit tests between the server
side CLJ code and the client side CLJS code, we only injected the
validators in the server side code implemented by using [Enlive][]

In this tutorial we are going to inject of the validators in the
client side code too while investigating the feasibility of sharing as
much code as possibile with the already implemented server side code
for the `Shopping Calculator` page.

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-19
> git checkout -b tutorial-20-step-1
> ```

## Short review

In the [Tutorial 9 - DOM Manipulation][7] we used the events'
management of the [domina][8] lib to implement the client side DOM
manipulation of the `Shopping Calculator` as a consequence of the
events triggered by the user interation with the browser.

Then, in the subsequent tutorials, even if we augumented the `Shopping
Calculator` sample by adding a bit of Ajax and by preparing both the
input validators and the corresponding unit tests, we missed to merge
those validators in the CLJS code as we did in the server side
implementation.

We could just extend the client side code already implemented by using
the [Domina][] lib, but we want to investigate the eventuality of
sharing some code with the corresponding server side code. 

## Enter Enfocus

[Enfocus][] is a DOM manipulation and templating library for
ClojureScript inspired by [Enlive][]. This statement is intriguing
enough to make our best efforts for learning it. Then we will try to
fullfill our obsession with the application of the DRY principle.

We'll start easy. Once we learnt enough about [Enfocus][] we'll try to
apply it in the context of the `Shopping Calculator` by substituting
it to the [domina][] lib.

## Living on the edge with enfocus

First, we're going to use the "2.0.0-SNAPSHOT" release of
[Enfocus][]. This is because [Creighton Kirkendall][] is currently on
the way to release the `"2.0.0"` tagged version, which is more
evoluted that the current "1.0.1" stable release.

Before adding `enfocus` to our project let's take a look at its
`project.clj`. `enfocus` has a directory structure that is a different
from the usual one.

```bash
tree
.
├── README.textile
├── project
│   ├── cljs-src
│   │   └── enfocus
│   │       ├── core.cljs
│   │       ├── effects.cljs
│   │       ├── events.cljs
│   │       ├── html
│   │       │   └── test-grid.html
│   │       ├── macros.clj
│   │       └── testing.cljs
│   ├── cljx-src
│   │   └── enfocus
│   │       └── enlive
│   │           └── syntax.cljx
│   └── project.clj
└── testing
    ├── project.clj
    ├── resources
    │   └── public
    │       ├── css
    │       │   └── test.css
    │       ├── templates
    │       │   ├── template1.html
    │       │   └── test-grid.html
    │       └── test.html
    └── src
        └── enfocus
            └── ring.clj

14 directories, 15 files
```

We have two main directories, `project` and `testing`, and they both
contain a specific `project.clj`. First, let's take a look at the one
in the `project` directory.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :source-paths ["cljs-src" ".generated/cljs" ".generated/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [domina "1.0.1" :exclusions [org.clojure/clojurescript]]
                 [org.jsoup/jsoup "1.7.2"]
                 ]
  :plugins [[lein-cljsbuild "0.3.0"]
            [com.keminglabs/cljx "0.2.2"]]
  :cljx {:builds [{:source-paths ["cljx-src"]
                   :output-path ".generated/clj"
                   :rules cljx.rules/clj-rules}

                  {:source-paths ["cljx-src"]
                   :output-path ".generated/cljs"
                   :extension "cljs"
                   :rules cljx.rules/cljs-rules}]}
  :cljsbuild
  {:builds
   [{:builds nil,
     :source-paths ["cljs-src" ".generated/cljs"]
     :compiler
     {:output-dir "../testing/resources/public/cljs",
      :output-to "../testing/resources/public/cljs/enfocus.js",
      :optimizations :whitespace,
      :pretty-print true}}]}
  :hooks [cljx.hooks])
```

### Call for normalization

There are few things to note.

First, as we already learnt in the [previous tutorial][], most of the
time you have to deal with CLJS libs, the sole purpouse of the
inclusion of the [lein-cljsbuild][] plugin in their corresponding
`project.clj` is for emitting a JS file for unit testing.

That said, even if the `[clojurescript.test][] lib is mature enough to
be used for implementing unit tests for CLJS libs, very few of them
use it and [Enfocus][] makes no exception. This is very unfortunate,
because we have to deal with very fragmented unit testing approches,
basically one for each CLJS lib.  [Enfocus][] even introduced a
specific project structure, just to deal with code testing.

Secondly, most of the [Enfocus's][] dependencies and plugins are
outdated, even if we've choosen to use its snapshot release. 

### Learn by collaborating

It seems to be a perfect opportunity to be collaborative with
[Creighton Kirkendall][]:

* first by updating all the [Enfocus][] depenendncies and plugins
* then by trying to normalize its project structure to a more
  idiomatic approach.
* finally by introducing the [clojurescript.test][] lib for
  implementing few unit tests.

### Dependencies and plugins update

Let's start by [fork][], clone and branch the [Enfocus][] repo.

`bash
cd ~/dev # the directory where you clone the forked repos
git clone https://github.com/<your-github-name>/enfocus.git
cd enfocus
git remote add upstream https://github.com/ckirkendall/enfocus.git
git checkout -b upgrade
```

Generally speacking th updating of the dependencies and the plugins of
a lib is not a big deal. Let's do that step by step.

#### Update the cljx plugin release and configuration

Open the [Enfocus' `project.clj`][] file from the `project` directory
and start by updating the `cljx` pluging from the `"0.2.2"` release to
the `"0.3.0"` release.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :plugins [...
            [com.keminglabs/cljx "0.3.0"]]
			
  :cljx {:builds [{:source-paths ["cljx-src"]
                   :output-path ".generated/clj"
                   :rules :clj}

                  {:source-paths ["cljx-src"]
                   :output-path ".generated/cljs"
                   :rules :cljs}]}
  ...)
```

As you see, we modified the rules for each `cljx` build to be
compliant with the latest available `"0.3.0"` release. Not a big
deal. But wait a minute, a [second change][] is the syntax for
annotation. This change seems to require to edit the `syntax.cljx`
file, which is the only file hosted inside the `cljx-src` directory.

```bash
tree project/cljx-src/
project/cljx-src/
└── enfocus
    └── enlive
        └── syntax.cljx

2 directories, 1 file
```

But the `syntax.cljx` does not contain any syntax annotation. This
means that it uses *pure clojure* code only. In such a case I would
have prefered to use the [lein-cljsbuild crossover][] feature instead
of the `cljx` plugin. Eventually I'll take care of my prederences
later. The good news is that we have nothing to modify in the
`syntax.cljx` source code.

That said, there are other little things that I would have configured
differently. The `lein clean` command always delete by defalut the
`target` directory. If we instruct the `cljx` builds to save the
generated code under this directory, the `lein clean` code will delete
it without any other instruction or configuration. Let's edit the
`cljx` builds as follows:

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  :source-paths ["cljs-src" "target/cljs" "target/clj"]
  ...
  :cljx {:builds [{...
                   :output-path "target/clj"
                   ...}

                  {...
                   :output-path "target/cljs"
                   ...}]}
  :cljsbuild
  {:builds
   [{...
     :source-paths ["cljs-src" "target/cljs"]
     ...}}]}
  ...)
```

> NOTE 2: The `output-path` changes in the `:clj` and `:cljs` builds'
> rules required two more changes in the `project.clj` file:
> 
> * the update of the CLJ `:source-paths`
> * the update of the CLJS `:source-paths`

> NOTE 3: I suggest to commit the changes as follows:
> 
> ```bash
> git commit -am "cljx updated to 0.3.0"
> ```

#### Update the `lein-cljsbuild` plugin and configuration

The second step is to update the [lein-cljsbuild][] plugin to the
latest `"0.3.3"` available release.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :min-lein-version "2.1.2"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]
                 ...]
  :plugins [[lein-cljsbuild "0.3.3"]
            ...]
  ...)
```

> NOTE 4: The `0.3.3` release of the `lein-cljsbuild` plugin now requires to update:
> 
> * the `lein` release (at least the `"2.1.2"`)
> * the CLJ release (`"1.5.1"`)
> * to explicitly configure a CLJS build if you don't want to receive a
>   warning message. As you see we configured the latest available CLJS
>   compiler at the time of writing.

> NOTE 5: I suggest to commit the changes as follows:
> 
> ```bash
> git commit -am "cljsbuild updated to 0.3.3"
> ```

#### Update the `domina` lib reference

There is one last `enfocus` dependency that we want to update: the
[domina][] lib. Actually `enfocus` uses the `"1.0.1"` release which
has few bugs that have be fixed in the `"1.0.2-SNAPSHOT"` release.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :dependencies [...
                 [domina "1.0.2-SNAPSHOT" :exclusions [org.clojure/clojurescript]]
                 ...]
  ...)
```

The complete content of the updated `project.clj` hosted under the
`project` folder of `enfocus` is the following:

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :min-lein-version "2.1.2"
  :source-paths ["cljs-src" "target/cljs" "target/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"]
                 [domina "1.0.2-SNAPSHOT" :exclusions [org.clojure/clojurescript]]
                 [org.jsoup/jsoup "1.7.2"]]
  :plugins [[lein-cljsbuild "0.3.3"]
            [com.keminglabs/cljx "0.3.0"]]
  :cljx {:builds [{:source-paths ["cljx-src"]
                   :output-path "target/clj"
                   :rules :clj}

                  {:source-paths ["cljx-src"]
                   :output-path "target/cljs"
                   :rules :cljs}]}
  :cljsbuild
  {:builds
   [{:builds nil,
     :source-paths ["cljs-src" "target/cljs"]
     :compiler
     {:output-dir "../testing/resources/public/cljs",
      :output-to "../testing/resources/public/cljs/enfocus.js",
      :optimizations :whitespace,
      :pretty-print true}}]}
  :hooks [cljx.hooks])
```

> NOTE 6: I suggest to commit the changes as follows:
> 
> ```bash
> git commit -am "domina updated to 1.0.2-SNAPSHOT"
> ```

### Ligh a small fire

Let's see if the `enfocus` lib is still able to compile as expected.

```bash
cd ~/dev/enfocus/project
lein do clean, cljx once, cljsbuild once
Rewriting cljx-src to target/clj (clj) with features #{clj} and 0 transformations.
Rewriting cljx-src to target/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["cljs-src" "target/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 9.323355 seconds.
```

Ok. At least the CLJS compilation is still working, but there are
other things that in my opnion could be improved in the project
organization of `enfocus`.

### Where are my usual project directories?

As you have seen from the very beginning of the `modern-cljs` series,
the `lein new <project-name>` command automatically creates both a
`src` and a `test` dirs. I tried to understand why `enfocus` adopted a
different directories layout, but I was not able to grasp it.

We, as users of someone else's code, always start to study a lib by
first reading its `README.md` file, then by reading its `project.clj`
to get a first impression of the used components and project
structure.

Most of the CLJ/CLJS repos have the same directories structure, at
least at the first level. Generally speacking there is `project.clj`,
a `src` and a `test` directories. When a lib has both a CLJ and a CLJS
codebase, the corresponding sources and unit tests lay under the
`src/clj`, `src/cljs`, `test/clj` and `test/cljs` directories.

It's my personal opinion that we, as libs developers, should try to be
consistent with that directories layout, unless there is a very good
reason for not doing that.

In the next step, we're going to reorganize the `enfocus` project
structure following the cited *standard*.

### Normalize the project structure

First let's see the actual `enfocus` directories layout

```bash
# clean
cd ~/deb/enfocus/project
lein do clean, cljsbuild clean
cd ..
# tree
tree
.
├── README.textile
├── project
│   ├── cljs-src
│   │   └── enfocus
│   │       ├── core.cljs
│   │       ├── effects.cljs
│   │       ├── events.cljs
│   │       ├── html
│   │       │   └── test-grid.html
│   │       ├── macros.clj
│   │       └── testing.cljs
│   ├── cljx-src
│   │   └── enfocus
│   │       └── enlive
│   │           └── syntax.cljx
│   └── project.clj
└── testing
    ├── project.clj
    ├── resources
    │   └── public
    │       ├── css
    │       │   └── test.css
    │       ├── templates
    │       │   ├── template1.html
    │       │   └── test-grid.html
    │       └── test.html
    └── src
        └── enfocus
            └── ring.clj

14 directories, 15 files
```

At the moment we don't care about the `testing` directory. We start by
creting the `src/clj`, `src/cljs`, `src/cljx` and the corresponding
`test/clj`, `test/cljs` and `test/cljx` directories.

```bash
cd ~/dev/enfocus
mkdir -p src/{clj,cljs,cljx}
mkdir -p test/{clj,cljs,cljx}
```

Next, we're going to move the content of the `cljx-src` and
`cljs-src` under the `src/cljx` and the `src/cljs` directories.

```bash
mv project/cljx-src/enfocus src/cljx
mv project/cljs-src/enfocus src/cljs
```

Then, we move the `project.clj` to the main `enfocus` directory.

```bash
mv project/project.clj .
```

We can now safetly delete the `project` directory.

```bash
rm -rf project
```

Here is the updated directory layout.

```bash
tree
.
├── README.textile
├── project.clj
├── src
│   ├── clj
│   ├── cljs
│   │   └── enfocus
│   │       ├── core.cljs
│   │       ├── effects.cljs
│   │       ├── events.cljs
│   │       ├── html
│   │       │   └── test-grid.html
│   │       ├── macros.clj
│   │       └── testing.cljs
│   └── cljx
│       └── enfocus
│           └── enlive
│               └── syntax.cljx
├── test
│   ├── clj
│   ├── cljs
│   └── cljx
└── testing
    ├── project.clj
    ├── resources
    │   └── public
    │       ├── css
    │       │   └── test.css
    │       ├── templates
    │       │   ├── template1.html
    │       │   └── test-grid.html
    │       └── test.html
    └── src
        └── enfocus
            └── ring.clj

19 directories, 15 files
```

There is still some work to do, but we can now edit again the
`project.clj` for updating its content to new directories layout.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :source-paths ["src/cljs" ...]
  ...
  :cljx {:builds [{:source-paths ["src/cljx"]
                   ...
				   ...}

                  {:source-paths ["src/cljx"]
                   ...
				   ...}]}
  :cljsbuild
  {:builds
   [{:builds nil,
     :source-paths ["src/cljs" ...]
     :compiler
     {:output-dir "testing/resources/public/cljs"
      :output-to "testing/resources/public/cljs/enfocus.js"
      ...
      ...}}]}
  ...)
```

To reflect the new directories layout, we updated the CLJ
`:source-paths`, the `:source-paths` of the `cljx` builds, the
`:source-paths` of the `cljsbuild` builds and finally the
`:output-dir` and the `:output-to` attributes of the `cljsbuild`
compiler's options.

NOTE 7: I suggest to commit the changes as follows:
 
```bash
git add project.clj src
git rm -r project
git commit -m "updated directories layout"
```


#### Ligh a small fire

Let's see if the `enfocus` lib is still able to compile as expected.

```bash
Giacomo-Cosenzas-iMac:enfocus mimmo$ lein do clean, cljsbuild clean, cljx once, cljsbuild once
Deleting files generated by lein-cljsbuild.
Rewriting src/cljx to target/clj (clj) with features #{clj} and 0 transformations.
Rewriting src/cljx to target/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
Compiling "testing/resources/public/cljs/enfocus.js" from ["src/cljs" "target/cljs"]...
Compiling "testing/resources/public/cljs/enfocus.js" failed.
clojure.lang.ExceptionInfo: failed compiling file:src/cljs/enfocus/testing.cljs
...
Caused by: clojure.lang.ExceptionInfo: /Users/mimmo/dev/enfocus/../testing/resources/public/templates/template1.html (No such file or directory)
...
```

Ops, what did happen?

#### Fix it again Tony

It seems that the compiler is looking for a `template1.html` file and
it's not able to find it. 

That's all. Stay tuned for the next tutorial of the series.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[2]: https://clojars.org/
[3]: https://github.com/shoreleave
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[5]: https://github.com/cgrand/enlive
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md


[2]: https://github.com/cemerick/piggieback
[3]: https://github.com/technomancy/leiningen/blob/stable/doc/PROFILES.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[5]: https://github.com/shoreleave/shoreleave-remote
[6]: https://github.com/shoreleave/shoreleave-remote-ring

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
