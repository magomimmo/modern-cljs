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
introduce the [Enlive][5] server side templating system. Then, in the
[Tutorial 17 - Enlive by REPLing][6] we injected into the `Enlive`
templating system the form's input validators for the `Shopping
Calculator`.

In this tutorial we're going to introduce the [Enfocus][] lib which is
a DOM manipulation and templating library for ClojureScript inspired
by [Enlive][]. This claim is interesting enough for who, like me, is
so obsessed by the application of the DRY principle and envisions the
opportunity to share some code between the `Enlive` server side CLJ
code and the `Enfocus` client side CLJS code.

While learning how to use this `Enfocus` lib, we'll try to be
collaborative with its author, [Creighton Kirkendall][], by proposing
few improvements we think could be useful for the next release of the
lib itself.

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

To fill this gap, we could just extend the client side code already
implemented by using the [Domina][] lib, but we want to investigate
the eventuality of sharing some code with the corresponding server
side code and we'll give a try to [Enfocus][].

## Living on the edge with Enfocus

We'll discuss the `"2.0.0-SNAPSHOT"` relase of `Enfocus` because
[Creighton Kirkendall][], the author of the lib, is currently on the
way to publish the next stable release which is more evoluted that the
current one.

`Enfocus` has a directories layout that is a lot different from the
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
  :url "http://ckirkendall.github.io/enfocus-site"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :source-paths ["cljs-src" ".generated/cljs" ".generated/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [domina "1.0.2"]
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
  outdated;
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
> `"0.0-1859"` release of the CLJS compiler.

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

> NOTE 2: The `"0.3.3"` release of `lein-cljsbuild` requires at minimum
> the `"2.1.2"` version of `lein`.

## Features annotation: cljx vs. crossovers

The next natural step would be to update the `cljx` plugin from the
`"0.2.2"` release to the latest `"0.3.0"` available release. As you
can verify, the `cljx-src` directory contains only one file:
`syntax.cljx`.  If you take a look at its code, you'll discover that
there are no annotation at all to treat CLJ code differently from the
CLJS code. In such a case, as said in the [Tutorial XX][], I strongly
prefer to use the [`:crossovers`][] option of the `lein-cljsbuild`
plugin because we're dealing with a *portable/pure* CLJ/CLJS code.

The `:crossovers` option works by copying a portable CLJ source code
to a destination directory which, by default is the
`target/cljsbuild-crossover` directory. It also automatically includes
that generated directory in the `:sourch-paths` of your CLJS
`:builds`.

### Housekeeping

The `cljx` substitution with the `:crossover` option requires some
housekeeping to be done before beeing configurated in the
`project.clj` file:

```bash
# create src/clj and src/cljs directories
cd ~/dev/enfocus/project
mkdir -p src/{clj,cljs}
# move the enfocus dir from cljx-src to src/clj dir
mv cljx-src/enfocus/ src/clj/
# rename the syntax.cljx to syntax.clj
mv src/clj/enfocus/enlive/syntax.cljx src/enfocus/enlive/syntax.clj
# delete the cljx-src dir
rm -rf cljsx-src
```

> NOTE 3: we created both a `src/clj` and `src/cljs` directories because
> this layout is more idiomatin with meixed CLJ/CLJS project.

Having created the `src/clj` directory, we can now move there even the
`macros.clj` file which is currently hosted in the `cljs-src`
directory. I always prefer to keep separated different concerns and
`macros.clj` need to be compiled by CLJ

```bash
mv cljs-src/enfocus/macros.clj src/clj/enfocus/
```

As last step, we want to move the `cljs-src` directory content under
the newly created `src/cljs` directory.

```bash
mv cljs-src/enfocus src/cljs/
rm -rf cljs-src
```

You'll end up with following directories layout in the `project` directory.

```bash
tree
.
├── project.clj
└── src
    ├── clj
    │   └── enfocus
    │       ├── enlive
    │       │   └── syntax.clj
    │       └── macros.clj
    └── cljs
        └── enfocus
            ├── core.cljs
            ├── effects.cljs
            ├── events.cljs
            ├── html
            │   └── test-grid.html
            └── testing.cljs

7 directories, 8 files
```

## Temporary recfactoring

As you can see there are still an `html` directory and a
`testing.cljs` which appear to have nothing to do with the `Enfocus`
source code. On the contrary, they seem to pertain the testing
activity of the `Enfocus` lib. We'll take care of them later. At the
moment let's go back to modify the `project.clj` by substituting the
`cljx` plugin with the `:crossovers` option relative to the
`cljsbuild` plugin and by configuring the project coherently with the
proposed directories layout.

Here is the temporary refactored `project.clj`. 

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :url "http://ckirkendall.github.io/enfocus-site"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :min-lein-version "2.1.2"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1909"
                 [domina "1.0.2"]
                 [org.jsoup/jsoup "1.7.2"]]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild
  {:builds
   [{:builds nil,
     :source-paths ["src/cljs"]
     :compiler
     {:output-dir "../testing/resources/public/cljs",
      :output-to "../testing/resources/public/cljs/enfocus.js",
      :optimizations :whitespace,
      :pretty-print true}}]})
```

There are few things to note:

* we completly removed both the `cljx` from the `:plugins` section and
  the corresponding `:cljx` configuration;
* we completly removed any reference to the `cljx` generated CLJ and
  CLJS files from the main `:source-paths` and from the
  `:source-paths` inside the `:cljsbuild` configuration option;
* we substituted the `cljx` hooks to leiningen tasks with the
  corresponding `cljsbuild` hooks.


> NOTE 4: At the moment we don't care about deployoment issues. For
> example, if we want the file generated by the `:crossovers` to be
> included in a deployable `jar`, we have to set the `:crossover-jar`
> option to `true.`

### Ligh the fire

Let's see if the `Enfocus` lib is still able to compile as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["src/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 9.813483 seconds.
```

> NOTE 5: By having hooked the `cljsbuild` tasks to the `lein` tasks, we
> can now call the `clean` and the `compile` tasks without prefixing
> them with the `cljsbuild` tasks.

This is a very temporary result, because we still have to modify a
fundamental aspect of the `Enlive` project: its unit tests.

## Move the tests

Near the beginning of this tuorial of the series, I said to not care
about the `testing` directory in the main directory of the `Enfocus`
repo, but if you take a look at the `:builds` section of the
`:clisbuild` configuration you'll discover that the emmitted JS file is
saved under that `testing` directory. 

The `enfocus.js` JS file is compiled down by viewing in the `src/cljs`
directory which contains four `cljs` file and an `html` directory. 

> NOTE 6: Actually the compiler uses the `macros.clj` and the
> `syntax.cljs` file generated by the `:crossovers` option of
> `:cljsbuild`.

Both the `html` directory and the `testing.cljs` has nothing to do
with the `Enfocus` codebase. They are eventually used only for testing
the `Enfocus` lib.

For this reason we're going to move them away from the `src/cljs` path
and temporary park the in a `tmp` directory in the main directory of
the `Enfocus` project.

```bash
mkdir -p tmp/enfocus/html
mv src/cljs/enfocus/html/test-grid.html tmp/enfocus/html/
mv src/cljs/enfocus/testing.cljs tmp/enfocus/
```

### Ligh the fire

Let's see if the `Enfocus` lib is still able to compile as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["src/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 8.982888 seconds.
```






First and foremost I strongly believe that unit testing is an
essential activity in any serious software project. That said I don't
want to waste my time in doing stuff which are too tediuos or, worst,
that a consequence of incidental complexities.

One of the things that I hate the most, is when I have 


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

We are not done in making the `Enfocus` more consistent with the cited
*best-practice*.

As you know, one of the main differences between CLJ and CLJS pertains
the `macros` system. CLJS `macros` are written in CLJ and need to be
defined in a separated namespace.

That's why `Enfocus` has a `macros.clj` source file inside the
`src/cljs` source path.

This choice is very common when you only need to define few `macros`
(e.s. the `shoreleave-remote` or the `domina` libs).

But `Enfocus`, by using the `cljx` plugin to generate both CLJ and
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

Let's see if `Enfocus` is still compiling the code as exptected.

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
`"0.2.2."` release to the `"0.3.3"` release inside the `Enfocus`
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

As usual we want to verify if the `Enfocus` project still compiles.

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
unit testing of the `Enfocus` lib which is a PITA. As said in the
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

As usual we want to verify if the `Enfocus` project still compiles.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/testable.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/testable.js" in 24.733073 seconds.
```


At the end of [Enfocus README][] you can read that to test the
`Enfocus` lib you have to issue the following commands:

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


