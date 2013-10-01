# Tutorial 20 - Learn by collaborating (Part 1)

In the [previous tutorial][1] we described a couple of approaches to
survive while livin' on the edge of a continuosly changing CLJ/CLJS
libs used as dependencies in our projects. We ended up by publishing
to [Clojars][2] a set of four [Shoreleave][3] libs which the
`modern-cljs` project directly or indirectly depended on. This way you
can become collaborative with the CLJ/CLJS communities and, at the
same time, more indipendent from someone else's decision to merge or
refuse your pull requests.

In this tutorial we're getting back to CLJS to become collaborative
while learning how to use the [Enfocus][] lib.

## Introduction

In the [Tutorial 14 - Its better to be safe than sorry (Part 1)][4] we
introduce the [Enlive][5] templating system for Clojure. Then, in the
[Tutorial 17 - Enlive by REPLing][6] we injected into the `enlive`
templating system the form's input validators for the `Shopping
Calculator` by using the cited [Enlive][5] lib.

In this tutorial we're going to introduce the [Enfocus][] lib which is
a DOM manipulation and templating library for ClojureScript inspired
by [Enlive][]. 

While learning how to use this new lib, we'll meet few things that in
our modest opinion could be improved to make the structure of the lib
itself more understandable by the developers that want to use it. 

## Preamble

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

As said, [Enfocus][] is a DOM manipulation and templating library for
ClojureScript inspired by [Enlive][]. This statement is intriguing
enough to make our best efforts for learning it with the final intent
of fullfilling our obsession with the application of the DRY
principle. Indeed, we'll try to share as much code as possibile
between the server side HTML transofrmation of the `Shopping
Calculator` and the corresponding client side DOM manipulation.

## Living on the edge with Enfocus

We'll discuss the `"2.0.0-SNAPSHOT"` relase of `Enfocus` because
[Creighton Kirkendall][], the author of the lib, is currently on the
way to publish the next stable release which is more evoluted that the
current one.

`Enfocus` has a directory structure that is a lot different from the
usual ones.

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

It has two main directories, `project` and `testing`. At the moment we
do not care about the `testing` directory and focus our attenction on
the `project.clj` in the `project` directory.

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

## Call for collaboration

There are few things to note.

* first, most of the `Enfocus` dependencies and plugins are
  outdated, even if we've choosen to use its snapshot release;
* second, even if the [clojurescript.test][] lib is mature enough for
  implementing CLJS unit tests, `Enfocus` doesn't use it and
  introduced instead a specific project structure just to deal with
  code testing.

It seems to be a perfect opportunity to be collaborative with
[Creighton Kirkendall][].

## Fork, clone and branch

Let's start by [fork][], clone and branch the repo.

`bash
cd ~/dev # the directory where you clone the forked repos
git clone https://github.com/<your-github-name>/enfocus.git
cd enfocus
git remote add upstream https://github.com/ckirkendall/enfocus.git
git checkout -b upgrade
```

Generally speacking the updating of the dependencies and the plugins
of a lib is not a big deal. Let's do it step by step.

## Update the cljsbuild plugin

Open the `Enfocus` [project.clj][] file from the `project` directory
and start by updating the `cljsbuild` pluging references from the
`"0.3.0"` release to the `"0.3.3"` release.

Remember that the `"0.3.3"` release of `cljsbuild` requires the CLJ
`"1.5.1"` release and to explicitly declare a specific CLJS compiler
you want to use in the project dependencies.

> NOTE 1: If you do not configure a specific CLJS compiler release,
> the `cljsbuild` plugin will warn you and will include the
> `"0.0-1803"` release of the CLJS compiler.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :min-lein-version "2.1.3"
  :dependencies [[org.clojure/clojure "1.5.1"]
	             [org.clojure/clojurescript "0.0-1909"]
                 ...]
  :plugins [[lein-cljsbuild "0.3.3"]
            ...]
  ...)
```

## Features annotation: cljx vs. crossovers

The next natural step would be to update the `cljx` plugin from the
`"0.2.2"` release to the latest `"0.3.0"` available release. The
`cljx-src` directory contains just the `syntax.cljx` file. If you take
a look at its code, you'll discover that there are no annotation at
all. In such a case, as said in the [Tutorial XX][], I strongly prefer
to use the `:crossovers` option of the `cljsbuild` plugin because
we're dealing with a *portable/pure* CLJ code.



As you see, we modified the rules for each `cljx` build to be
compliant with the latest available `"0.3.0"` release. Not a big deal.

But wait a minute, a [second required change][] between the `"0.2.2"`
and the `"0.3.0"` releases of the `cljx` plugin is the syntax for the
features annotation. This change would requires to edit the
`syntax.cljx` file, which is the only file hosted inside the
`cljx-src` directory.

```bash
tree project/cljx-src/
project/cljx-src/
└── enfocus
    └── enlive
        └── syntax.cljx

2 directories, 1 file
```

If you take a look at this file, you'll discover that it does not
contain any syntax annotation. This means that it uses *portable
clojure* code only. In such a case, as I said in a
[previous tutorial][] I prefer to use the
[lein-cljsbuild crossover][].

Instead of 
That said, there are other little things that I would have configured
differently. The `lein clean` command always delete by defalut the
`target` directory. If we instruct the `cljx` builds to save the
generated code under the `target` directory, then the `lein clean`
code will delete it without any other instruction or
configuration. Let's edit the `cljx` builds as follows:

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

> NOTE 4: The `0.3.3` release of the `lein-cljsbuild` plugin now
> requires:

> * to update the `lein` release (at least the `"2.1.2"`);
> * to update the CLJ release (`"1.5.1"`);
> * to explicitly configure a CLJS release if you don't want to receive a
>   warning message and use a CLJS release bounded with the `"0.3.3"
>   lein-cljsbuild` plugin. As you see we configured the latest
>   available CLJS release (i.e. `"0.0-1909"`) at the time of writing.

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
`project` directory of `enfocus` is the following:

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

### Ligh the fire

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

Ok. The CLJS compilation process is still working, but there are other
things that in my opinion could be improved in the project
organization of `enfocus`.

### Where are my usual project directories?

As you have seen from the very beginning of the `modern-cljs` series,
the `lein new <project-name>` command creates both a `src` and a
`test` dirs. I tried to understand why `enfocus` adopted a different
directories layout, but I was not able to grasp it.

We, as users of someone else's code, always start to study a lib by
first reading its `README.md` file and then by reading its
`project.clj` to get a first impression of the used components and
project structure.

Most of the CLJ/CLJS repos have the same directories structure, at
least at the first level. Generally speacking there is `project.clj`
file and the `src` and `test` directories. When a lib has both a CLJ
and a CLJS codebase, the corresponding sources and unit tests lay
under the `src/clj`, `src/cljs`, `test/clj` and `test/cljs`
directories.

It's my personal opinion that we, as libs developers, should try to be
consistent with that directories layout, unless there is a very good
reason for not doing that.

In the next step, we're going to reorganize the `enfocus` project
structure trying to be more consistent with the cited *best practice*.

### Normalize the project structure

First let's review the current `enfocus` directories layout

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

At the moment we don't care about the `testing` directory and start to
adapt the directories layout with the proposed *best practice* by
creating the `src/clj`, `src/cljs`, `src/cljx` directories and the
corresponding `test/clj`, `test/cljs` and `test/cljx` directories.

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

Here is the obtained directories layout.

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

There is still some work to do, but we can begin to edit again the
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
`:source-paths` of the `cljsbuild` build and finally the `:output-dir`
and the `:output-to` attributes of the `cljsbuild` compiler's options.

> NOTE 7: I suggest to commit the changes as follows:
>  
> ```bash
> git add project.clj src
> git rm -r project
> git commit -m "updated directories layout"
> ```

#### Ligh the fire

Let's see if the `enfocus` lib is still able to compile as expected.

```bash
lein do clean, cljsbuild clean, cljx once, cljsbuild once
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
it's not able to find it. But wait a minute, the exception is raised
while compiling the `testing.cljs` source file which resides in the
`src/cljs` source path.

```bash
$ tree src/cljs/
src/cljs/
└── enfocus
    ├── core.cljs
    ├── effects.cljs
    ├── events.cljs
    ├── html
    │   └── test-grid.html
    ├── macros.clj
    └── testing.cljs

2 directories, 6 files
```

The above command shows that even the `test-grid.html` is hosted under
the `src/cljs` source path. They both have nothing to do with the
`enfocus` lib *in se*. They only pertain to the lib testing.

So, we can temporarily move both the `testing.clj` and the
`test-grid.html` files under the `test/cljs` path.

> NOTE 8: We'll take care of testing in the next tutorial by adopting
> the `clojurescript.test` lib which I personally prefer to any other
> current approach in unit testing CLJS code.

```bash
mv src/cljs/enfocus/testing.cljs test/cljs/
mv src/cljs/enfocus/html test/cljs/
lein do clean, cljsbuild clean, cljx once, cljsbuild once
Deleting files generated by lein-cljsbuild.
Rewriting src/cljx to target/clj (clj) with features #{clj} and 0 transformations.
Rewriting src/cljx to target/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
Compiling "testing/resources/public/cljs/enfocus.js" from ["src/cljs" "target/cljs"]...
Successfully compiled "testing/resources/public/cljs/enfocus.js" in 8.191779 seconds.
```

Good. We are still able to compile.

> NOTE 9: I suggest to commit the changes as follows:
> 
> ```bash
> git add test
> git rm src/cljs/enfocus/html/test-grid.html src/cljs/enfocus/testing.cljs
> git commit -m "removed testing stuff from the src/cljs source path"
> ```

#### Separation of concerns

We are not done in making the `enfocus` more consistent with the cited
*best-practice*.

As you know, one of the main differences between CLJ and CLJS pertains
the `macros` system. CLJS `macros` are written in CLJ and need to be
defined in a separated namespace.

That's why `enfocus` has a `macros.clj` source file inside the
`src/cljs` source path.

This choice is very common when you only need to define few `macros`
(e.s. the `shoreleave-remote` or the `domina` libs).

But `enfocus`, by using the `cljx` plugin to generate both CLJ and
CLJS code starting from a `cljx` code, is a little bit
different. Indeed its `project.clj` contains an explicit
`:source-paths` setting for compiling the CLJ code:

```clj
(defroject enfocus "2.0.0-SNAPSHOT"
  ...
  :source-paths ["src/cljs" "target/cljs" "target/clj"]
  ...)
```

What I don't like here is the fact that we have to reference the
`src/cljs` and the `target/cljs` source paths, which contain CLJS code
too, to compile CLJ code.

To fix this very small annoyance, let's move the `macros.clj` file
under the `src/clj` source path and make the corresponding changes in
the `project.clj`

```bash
mv src/cljs/enfocus/macros.clj src/clj/enfocus/
```

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...:
  :source-paths ["src/clj" "target/clj"]
  ...)
```

As you see, in the context of the CLJ compilation, by having set the
CLJ `source-paths` to `["src/clj" "target/clj"]` we now have to deal
with CLJ source files only, a kind of application of the separation of
concerns principle.

#### Ligh the fire

Let's see if `enfocus` is still compiling the code as exptected.

```bash
lein do clean, cljsbuild clean, cljx once, cljsbuild once
Deleting files generated by lein-cljsbuild.
Rewriting src/cljx to target/clj (clj) with features #{clj} and 0 transformations.
Rewriting src/cljx to target/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
Compiling "testing/resources/public/cljs/enfocus.js" from ["src/cljs" "target/cljs"]...
Successfully compiled "testing/resources/public/cljs/enfocus.js" in 18.647835 seconds.
```

Ok it worked again.

> NOTE 10: AGGIUNGERE NOTA SU COMMIT

#### Falling in the expression problem again

As discussed in the
[Tutorial 17 - It's better to be safe than sorry (Part 3)][], I
generally prefer to use the `:crossovers` option of the
[lein-cljsbuild][] plugin when I have to deal with portable libs
(e.g. [valip][]) and to use the [cljx][] lein plugin when I have to
deal with ported libs (e.g. [clojurescript.test][]).

As shown when we updated the reference to the [cljx][] plugin from the
`"0.2.2."` release to the `"0.3.3"` release inside the `enfocus`
`project.clj` file, we discovered that were non [syntax annotation][],
which means that you're dealing with a portable namespace of *pure
clojure* code.

We can then semplify the `project.clj` file by adding a `:crossovers`
section in the the `:cljsbuild` section and completely removing the
`cljx` plugin.

First move the `syntax.cljx` file from the `src/cljx` source path to
the `src/clj` source path and rename it as `syntax.clj`

```bash
cd ~/dev/enfocus
mkdir src/clj/enfocus/enlive
mv src/cljx/enfocus/enlive/syntax.cljx src/clj/enfocus/enlive/syntax.clj
```

Next remove the `cljx` plugin reference in the `project.clj`, add the
`:crossovers` option to the `:cljsbuld` section and consequently adjust
the CLJ `:source-paths` attribute.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :source-paths ["src/clj"]
  ...
  :plugins [[lein-cljsbuild "0.3.3"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:crossovers [enfocus.enlive.syntax]
              :builds 
              [{:builds nil,
                :source-paths ["src/cljs"]
                ...
				}]})
```

As you see:

* we removed the `target/clj` source path from the CLJ `:source-paths`
  attribute;
* we removed the `cljx` from the project `:plugins` section;
* we removed the `cljx` hook and added the `cljsbuild` hook;
* we added the `:crossovers` section into the `:cljsbuild` option by
  setting its value to the `enfocus.enlive.syntax` namespace;
* we removed the `target/cljs` source path from the CLJS
  `:source-paths` attribtute.

#### Light the fire

As usual we want to verify if the `enfocus` project still compiles.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "testing/resources/public/cljs/enfocus.js" from ["src/cljs"]...
Successfully compiled "testing/resources/public/cljs/enfocus.js" in 18.76938 seconds.
```

Not bad at the moment. You can now safetly delete both the `src/cljx`
and the `test/cljx` directory which are not used anymore.

> NOTA 11: By having added the `hook` for the `cljsbuild` plugin, the
> `lein clean` and the `lein compile` commands implicitely call the
> corresponding `lein cljsbuild clean` and `lein cljsbuild once` tasks.

```bash
rm -rf src/cljx test/cljx
```

> NOTA 12: AGGIUNGERE NOTA SU COMMIT

### No pain no gain

If you think we are done, you're wrong. We still have to deal with the
unit testing of the `enfocus` lib which is a PITA. As said in the
[previous tutorial][], if we want to be really collaborative with a
such a great community, we should at least prepare the unit testing
field and implement few unit tests as a guide.

It makes no sense to restructure so deeply the project structure of a
someone else's lib and then run away when the hard work is facing
us. And it would be a little bit cheeky too.

In this last part of the tutorial we're going to prepare the unit
testing mechanics. In the next tutorial we're going to implent few
unit tests as a guidance for going on with implementing the others.

### Unit testig preparation

The careful reader may have notice that both the `:output-dir` and the
`:output-to` compiler's options of the `cljsbuild` section are set to
a path hosted by the `testing` directory, which is one of the things I
do not understand the reason why.

Not been able to understand the reason why of this decision, I'll
reduce those compiler's options to something that I know.

First create a more standard directories layout for hosting any
required html page, any required css file and finally the JS files
emitted by the CLJS compiler. 

```bash
cd ~/dev/enfocus
mv teststing/resources resources
mkdir testing/resources/public/js
```

Next we have to update the `project.clj` file to adapt it at the new
directories layout for the resources.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :cljsbuild {...
              [{...
			    {:output-to "resources/public/js/testable.js"
                 ...}}]})
```

The emitted JS file will be saved as `testable.js` under the
`"resources/public/js"` directory.

> NOTE 13: We removed the `:output-dir` option because its default,
> under the `target` directory, is sane enough and will be automatically
> deleted by the `cljsbuild clean` command.

NOTE 14: As soon as we'll implemented some unit tests we'll add 
#### Light the fire

As usual we want to verify if the `enfocus` project still compiles.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/testable.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/testable.js" in 24.733073 seconds.
```


At the end of [Enfocus README][] you can read that to test the
`enfocus` lib you have to issue the following commands:

```bash
cd ~/dev/enfocus/testing
lein deps
lein repl

```



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


