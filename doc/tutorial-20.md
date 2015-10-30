# Tutorial 20 - Learning by Contributing (Part 1)

In the [previous tutorial][1] we described a couple of approaches for
surviving while living on the edge of continuously changing CLJ/CLJS
libs used as dependencies in our projects.

We ended up publishing to [Clojars][2] a set of four
[Shoreleave][3] libs on which the `modern-cljs` project directly or
indirectly depends.

This way you may become collaborative with the CLJ/CLJS communities
and, at the same time, independent from someone else's decision to
merge or refuse your pull requests.

In this tutorial we're getting back to CLJS and trying to be
collaborative as well while learning how to use the [Enfocus][4] lib.

## Introduction

In [Tutorial 14 - Better Safe Than Sorry (Part 1)][5] we
introduced the [Enlive][6] server-side templating system. Then, in
[Tutorial 17 - REPLing with Enlive][7] we injected our
input validators for the `Shopping Calculator` form into the
Enlive template.

In this tutorial we're going to introduce the [Enfocus][4] lib,
a DOM manipulation and templating library for ClojureScript
inspired by [Enlive][6]. This claim is interesting enough for anyone who,
like me, is so obsessed with the DRY principle and envisions the
opportunity to share code between the `Enlive` server-side CLJ
code and the `Enfocus` client-side CLJS code.

## Preamble

In [Tutorial 9 - DOM Manipulation][8] we used the [Domina][9] lib
to manipulate the DOM of the `Shopping Calculator` in reaction to the
events triggered by the user interaction with the browser.

Then, in the subsequent tutorials, we augumented the `Shopping
Calculator` sample by adding a bit of AJAX and preparing both
the input validators and corresponding unit tests, but we missed the opportunity to
merge those validators in the CLJS client-side code.

To fill this gap, we could just extend the CLJS client-side code
already implemented by using [Domina][9], but we want to
investigate the eventuality of sharing some of the
corresponding server-side code so we'll give [Enfocus][4] a try.

## Living on the edge with Enfocus

We'll discuss the `"2.0.0-beta3"` beta release of `Enfocus` because
[Creighton Kirkendall][10], the author of the lib, has not yet
published the next stable release which is more evolved than the
current `"1.0.1"`.

Taking a look at the [Enfocus repo][4], there are few things to note
about it:

* first, the directory layout for the project is a bit unusual;
* second, it does not use [clojurescript.test][11] (by the
  great [Chas Emerick][12]) to implement unit testing;
* third, due to the necessity to serve *legacy* code in production,
  most of its dependencies and plugins are outdated.

It seems to be a perfect opportunity to collaborate with
[Creighton Kirkendall][10] by helping him in improving a very
interesting and promising lib.

## Clone and branch

Let's start by cloning and branching the `Enfocus`
repo.

```bash
cd ~/dev
git clone https://github.com/ckirkendall/enfocus.git
cd enfocus
git checkout tags/2.0.0-beta3
git checkout -b tutorial-20
```

`Enfocus` has two main directories, `project` and `testing`. At the
moment we do not care about the `testing` directory, because later we'll
try to introduce [clojurescript.test][11] for unit
testing; for now we'll focus our attention on the `project` directory only.

## Separation of concerns - Step 1

Take a look at the `project` directory.

```bash
tree project/
project/
├── cljs-src
│   └── enfocus
│       ├── core.cljs
│       ├── effects.cljs
│       ├── events.cljs
│       ├── html
│       │   └── test-grid.html
│       ├── macros.clj
│       └── testing.cljs
├── cljx-src
│   └── enfocus
│       └── enlive
│           └── syntax.cljx
└── project.clj

6 directories, 8 files
```

Now take a look at an *almost* standard directories layout of a
fictional mixed CLJ/CLJS project. I call it *almost* standard, because
it directly extends the default directory layout created by the
`lein new my-project` command.

```bash
tree my-project
my-project
├── LICENSE
├── README.md
├── dev-resources
│   └── public
│       ├── css
│       ├── js
│       └── test.html
├── doc
│   └── intro.md
├── project.clj
├── resources
│   └── public
│       ├── css
│       ├── index.html
│       └── js
├── src
│   ├── clj
│   │   └── any_project
│   │       └── core.clj
│   ├── cljs
│   │   └── any-project
│   │       └── core.cljs
│   └── cljx
│       └── annotated.cljx
└── test
    ├── clj
    │   └── any_project
    │       └── core_test.clj
    └── cljs
        └── any-project
            └── core_test.cljs
20 directories, 11 files
```

As you see, this *quasi standard* directory layout for a mixed CLJ/CLJS
project tries to separate every type of
code (e.g., `clj` vs `cljs` vs `cljx`) and their scope too (e.g., `src`
vs `test`).

It even separates static resources from the code
(i.e., `resources` vs. `src`) and development resources from
production resources (i.e., `dev-resources` vs `resources`).

Our first contribution to the `Enfocus` project will be to normalize
its directory layout by using the above directory layout as a
reference.

### Moving stuff around

Let's modify the `Enfocus` directory layout, being more observant
of the separation of concerns principle.

```bash
cd ~/dev/enfocus/project
mkdir -p src/{clj,cljs}
mkdir -p test/{clj,cljs}
mkdir -p resources/public/{css,js}
mv cljs-src/enfocus src/cljs
mv cljx-src/enfocus src/clj
mv src/clj/enfocus/enlive/syntax.cljx src/clj/enfocus/enlive/syntax.clj
mv src/cljs/enfocus/macros.clj src/clj/enfocus/
rm -rf cljs-src cljx-src
```

> NOTE 1: At the moment, we did not create the `dev-resources`
> directory. As `Enfocus` is a CLJS lib, its static resources for unit
> testing should not be parked in the `resources`
> directory. We'll take care of this later.

The above commands first created the *standard* directories,
then moved the CLJ code under the `src/clj/` directory and the CLJS
code under the `src/cljs/` directory. Next we changed the file
extension of `syntax` file from `cljx` to `clj` and finally we deleted
the original `cljs-src/` and `cljx-src/` directories.

The reason why we did not create a `src/cljx/` directory and instead
moved `cljx-src/enfocus` to `src/clj/` is
related to a simple observation of the `syntax.cljx` source
code:

> `syntax.cljx` does not contain any feature annotations and the two
> generated `syntax.clj` and `syntax.cljs` files will be identical to
> it. In such a case we say that the source code is *portable* from
> CLJ to CLJS and vice-versa without any intervention.

As said in
[Tutorial 16 - Better Safe Than Sorry (Part 3)][14], I
always prefer to use the [:crossovers][15] setting of the
[lein-cljsbuild][16] plugin when sharing *portable* CLJ/CLJS
code and to use the [cljx][17] plugin by [Kevin Lynagh][18] only when
dealing with *annotated* code *ported* from CLJ to CLJS or
vice-versa.

> NOTE 2: It's my opinion that the only exception to this rule is when
> dealing with a codebase containing both *portable* and *ported* code. In such
> a case you can use the `cljx` plugin only, and save some typing in
> the `:clsjbuild` configuration. But you lose the explicit
> distinction between *portable* and *ported* code. For this
> reason in the `modern-cljs` series we used both the `:crossovers`
> setting and the `:cljx` plugin.

### Keep moving

The `html` directory now residing in the `src/cljs/` directory has nothing to
do with the CLJS codebase and the name of the `testing.cljs` file is
suspicious as well--if you take a look at it, you'll see testing code
only.

As said, we are preparing the field for substituting the original
`Enfocus` testing code with corresponding unit tests based on the
[clojurescript.test][11] lib. For now, we're going to move them away from
`src/cljs/` and park them in a `temp/` directory for
later reference.

```bash
mkdir -p temp/enfocus
mv src/cljs/enfocus/testing.cljs temp/enfocus/
mv src/cljs/enfocus/html temp/enfocus/
```

### Update project.clj to reflect our changes

The new directory layout needs to be reflected in the
`project.clj` file as follows.

```clj
(defproject enfocus "2.1.0-SNAPSHOT"
  ...
  :source-paths ["src/clj" "src/cljs"] ; see ATTENTION NOTE in tutorial-01
  :test-paths ["test/clj" "test/cljs"] ; see ATTENTION NOTE in tutorial-01
  ...
  ;; cljx plugin has been removed
  :plugins [[lein-cljsbuild "0.3.0"]]
  ;; cljsbuild is now hooked to lein task
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:crossovers [enfocus.enlive.syntax]
   :builds
   [{:builds nil,
     :source-paths ["src/cljs" "test/cljs"]
     ...
     }]})
```

> NOTE 3: For logistical reasons we updated the `Enfocus` semantic
> version to `"2.1.0-SNAPSHOT"`. Considering that we're going to
> touch only its directory layout and `project.clj`
> configuration, we should have incremented the `patch` number only.

As explained in the ATTENTION NOTE of the very [first tutorial][28] of
this series, the `cljsbuild` plugin does not add its
`source-paths` to the Leiningen `:source-paths` so we need to add them
manually. The same thing must be done for `:test-paths` as well.

We also hooked the `cljsbuild` subtasks to the lein tasks
and substituted the `cljx` plugin and its `:cljx` configuration rules
with the corresponding cljsbuild `:crossovers` option, which is set to the
`enfocus.enlive.syntax` *portable* namespace.

Finally, we set `:source-paths` for the CLJS codebase to read CLJS
files from `src/cljs/` and from `test/cljs/` in
preparation for unit testing.

### Light the fire

Let's see if the project is still compiling as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 6.081459 seconds.
```

Not bad. Successful compilation is not something to count on. It is
not even testable at the moment, because there are no unit tests in
the `test/cljs/` path to be executed.

## Separation of concerns: step 2

In the previous paragraph we separated the CLJ codebase
from the CLJS codebase.  By moving `testing.cljs`
and the `html/` directory away from `src/cljs/enfocus/`,
we also started to separate the concern about the `Enfocus` lib *in
se* from the concern about its testing code.

That said, the `:cljsbuild` build setting is still saving the emitted
JS file in the old `testing/` directory. Let's work on that too.

### Keep moving

First we want to move the `testing/` directory under the `temp/`
temporary directory to keep it as a reference for later testing code
implementation.

```bash
lein clean # delete the generated `enfocus.js` before moving its containing directory
mv ../testing temp/
```

We now need to reflect this move in the `:builds` section of
`project.clj` by deleting the `:output-dir` setting and changing
the `:output-to` setting to `"resources/public/js/enfocus.js"`.

```bash
(defproject enfocus "2.1.0-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :builds
   [{...
     :compiler
     {:output-to "resources/public/js/enfocus.js"
      ...}}]})
```

### Last move before step ahead

By having moved everything to the `project/` directory of the `Enfocus`
repo, we can now move the entire contents of the `project/` directory one level
up.

```bash
cd ..
mv project/* .
rmdir project
```

Here is the resulting directory layout.

```bash
tree
.
├── README.textile
├── project.clj
├── resources
│   └── public
│       ├── css
│       └── js
├── src
│   ├── clj
│   │   └── enfocus
│   │       ├── enlive
│   │       │   └── syntax.clj
│   │       └── macros.clj
│   └── cljs
│       └── enfocus
│           ├── core.cljs
│           ├── effects.cljs
│           └── events.cljs
├── temp
│   ├── enfocus
│   │   ├── html
│   │   │   └── test-grid.html
│   │   └── testing.cljs
│   └── testing
│       ├── project.clj
│       ├── resources
│       │   └── public
│       │       ├── css
│       │       │   └── test.css
│       │       ├── templates
│       │       │   ├── template1.html
│       │       │   └── test-grid.html
│       │       └── test.html
│       └── src
│           └── enfocus
│               └── ring.clj
└── test
    ├── clj
    └── cljs

23 directories, 15 files
```

### Light the fire

Let's see if the project is still compiling as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/enfocus.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/enfocus.js" in 7.742492 seconds.
```
Not bad. If you're curious about where `lein` is storing all the files
generated by the compilation task, here is the layout of the `target/` and
`resources/` directories.

```bash
tree target/ resources/
target/
├── cljsbuild-compiler-0
│   ├── cljs
│   │   ├── core.cljs
│   │   └── core.js
│   ├── clojure
│   │   ├── string.cljs
│   │   └── string.js
│   ├── domina
│   │   ├── css.cljs
│   │   ├── css.js
│   │   ├── support.cljs
│   │   ├── support.js
│   │   ├── xpath.cljs
│   │   └── xpath.js
│   ├── domina.cljs
│   ├── domina.js
│   └── enfocus
│       ├── core.js
│       ├── effects.js
│       ├── enlive
│       │   └── syntax.js
│       └── events.js
└── cljsbuild-crossover
    └── enfocus
        └── enlive
            └── syntax.cljs
resources/
└── public
    ├── css
    └── js
        └── enfocus.js

12 directories, 18 files
```

> NOTE 4: The `cljsbuild` plugin generates `syntax.cljs` in
> the default `cljsbuild-crossover` directory. This directory is
> silently added to the `:source-paths` setting of the build for
> emitting the `syntax.js` JS file. The `syntax.js` file is then
> passed, as all the other JS files produced by the CLJS compiler, to
> the Google Closure Compiler for producing the final `enfocus.js` JS
> file. The optimization level of the final `enfocus.js` JS file
> depends on the `:optimizations` value configured in the `:compiler`
> setting.

## Update dependencies and plugins

Now that the directory layout is more consistent with the
augmented default lein template, we can focus our attention on
updating the `Enfocus` dependency and plugin references.

### Upgrade to lein-cljsbuild `"1.0.0"`

The most recent [lein-cljsbuild][16] plugin release is `"1.0.0"`.

Starting from the `"0.3.3"` release of `lein-cljsbuild` warns
you when you do not specify a CLJS dependency in your project and
implicitly downloads the `"0.0-1859"` CLJS release which, in turn,
requires the CLJ `"1.5.1"` release.  The `"1.0.0"` release of `cljsbuild` also
requires `lein` release `"2.2.0"` or higher.

> NOTE 5: Since CLJS is a very young language, it's very frequently
> updated. The latest available CLJS release at the moment of this
> writing is the `"0.0-2069"`.

Wow, four changes in one shot to be made to the `project.clj` file.

> NOTE 6: Whenever you need to change the `project.clj` file, remember
> to first clean the project.
>
> ```bash
> lein clean
> ```

Here is the relevant fragment of the changes to `project.clj`.

```clj
(defproject enfocus "2.1.0-SNAPSHOT"
  ...
  :min-lein-version "2.2.0"
  ...
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]
                 ...]

  :plugins [[lein-cljsbuild "1.0.0"]]
  ...)
```

### Add more CLJS optimizations

There is still one more thing to be improved in `project.clj`. As we
learned from [Tutorial 7 - Compilation Modes][20] which I
suggest that review, CLJS offers 4 different compiler optimization levels:

* `:none` (we are not going to use this)
* `:whitespace`
* `:simple`
* `:advanced`

Actually, `project.clj`'s `:builds` setting uses the `:whitespace`
optimization level only. We want to be sure that `Enfocus` works with any
optimization level, because we can't anticipate how this lib will be
used in someone else's CLJS project.

When you have more than one build in your `:builds` settings and you want to
name them, you need to change its value from a vector to a map as
follows:

```clj
(defproject enfocus "2.1.0-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :builds {:whitespace
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "resources/public/js/whitespace.js"
              :optimizations :whitespace
              :pretty-print true}}

            :simple
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "resources/public/js/simple.js"
              :optimizations :simple
              :pretty-print false}}

            :advanced
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "resources/public/js/advanced.js"
              :optimizations :advanced
              :pretty-print false}}}})
```

We now have three CLJS builds, one for each optimization level:

* `:whitespace`: emits `resources/public/js/whitespace.js`;
* `:simple`: emits `resources/public/js/simple.js`;
* `:advanced`: emits `resources/public/js/advanced.js`.

### Light the fire

Let's see if the project compiles as expected for each
optimization level.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
WARNING: Extending an existing JavaScript type - use a different symbol name instead of js/String e.g string at line 691 src/cljs/enfocus/core.cljs
Successfully compiled "resources/public/js/simple.js" in 18.728384 seconds.
Compiling "resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
WARNING: Extending an existing JavaScript type - use a different symbol name instead of js/String e.g string at line 691 src/cljs/enfocus/core.cljs
Successfully compiled "resources/public/js/advanced.js" in 10.86743 seconds.
Compiling "resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
WARNING: Extending an existing JavaScript type - use a different symbol name instead of js/String e.g string at line 691 src/cljs/enfocus/core.cljs
Successfully compiled "resources/public/js/whitespace.js" in 6.84827 seconds.
```

Not so good. Even if the builds have been compiled, we received the
same warning for each compilation. That's because we upgraded the
ClojureScript compiler to the new `"0.0-2069"` release which is much
less tolerant of syntax errors. Here the error regards the direct
reference to the base `js/String` type while extending the `ISelector`
protocol.

A good opportunity to fix a dangerous bug. Open `core.cljs`
and substitute the `js/String` type with the `string` type in the
`(extend-protocol ISelector ...)` call as follows:

```clj
(extend-protocol ISelector
  ...
  string
  ...
)
```

> NOTE 7: Remember that once you hook `cljsbuild` subtasks to
> `lein` tasks, if you want to compile a named `cljsbuild` build
> (e.g., `whitespace`) you can't pass its name to the `lein compile`
> task. You have to use the `lein cljsbuild once whitespace` or `lein
> cljsbuild auto whitespace` commands.

> ```bash
> lein do clean, cljsbuild once whitespace
> Deleting files generated by lein-cljsbuild.
> Compiling ClojureScript.
> "resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
> Successfully compiled "resources/public/js/whitespace.js" in 10.414104 seconds.
> ```

## Enter clojurescript.test

At the moment there are no clear winners for unit testing CLJ code,
even if the [clojure.test][21] lib is included with the CLJ core. The
scenario for unit testing CLJS codebase is even more fragmented and
questionable, which is a PITA for all of us as CLJ/CLJS developers.

I would love not to waste my time in learning a new CLJ/CLJS lib by
having first to learn its project structure and/or its unit testing
approaches on both sides of the border.

The CLJ/CLJS programming languages already require enough effort for
switching our minds upside down in order to afford such
fragmentation and uncertainty.

In a few previous tutorials of the series we introduced and used
[clojure.test][21] and [clojurescript.test][11] for our unit
testing purposes.

Aside from any expressivity considerations about `clojure.test` and
`clojurescript.test`, which I'm not going to discuss in this
context, the only reason I decided to use them depends on the fact
that they allow me to share unit testing API and the same unit testing
code across both CLJ and CLJS codebases. To me this reason alone is enough
to make a choice.

Enough words. Let's move forward by introducing `clojurescript.test`
for testing the `Enfocus` lib.

If you don't remember how to set up unit testing with
[clojurescript.test][11] I strongly suggest that you review
[Tutorial 16 - Better Safe Than Sorry (Part 3)][22] of this
series. In the current tutorial we'll work a little bit faster and without
explaining every single detail.

In a previous paragraph we already created the `test/clj/` and
`test/cljs/` directories for hosting the CLJ and CLJS unit tests.

We now need to install the `phantomjs` headless browser and configure
the `:test-commands` setting in the `:cljsbuild` section of
`project.clj` [as we did][24] in the context of the `modern-cljs`
project.

> NOTE 8: Unfortunately, AFAIK, there is no way to add `phantomjs` as a
> dependency in a project. This means that you have to download and
> install it separately from the project itself.

The table is now set and you can now modify `project.clj` by adding
the [clojurescript.test][11] lib and the `:test-commands` setting.

```clj
(defproject enfocus "2.1.0-SNAPSHOT"
  ...
  :plugins [...
            [com.cemerick/clojurescript.test "0.2.1"]]
  ...
  :cljsbuild
  {...
   :test-commands {"whitespace"
                   ["phantomjs" :runner "resources/public/js/whitespace.js"]

                   "simple"
                   ["phantomjs" :runner "resources/public/js/simple.js"]

                   "advanced"
                   ["phantomjs" :runner "resources/public/js/advanced.js"]}})
```

As you see we added `clojurescript.test` to the `:plugins` section
and a test command for each JS file emitted by each build.

> NOTE 9: At the moment we don't care about differentiating lein
> profiles. This is something we'll revisit later.

### Light the fire

Let's see if the `Enfocus` lib still compiles as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/simple.js" in 19.280334 seconds.
Compiling "resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/advanced.js" in 11.981145 seconds.
Compiling "resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/whitespace.js" in 7.638818 seconds.
```

Not bad, `Enfocus` is still compiling as expected. Even if we still
have to implement unit tests, everything is in place.

If you issue the `lein test` command without having defined
any CLJS unit test, you'll receive more error messages.

```bash
lein test
Compiling ClojureScript.

lein test user

Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
Running all ClojureScript tests.
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
ReferenceError: Can't find variable: cemerick

  phantomjs://webpage.evaluate():2
  phantomjs://webpage.evaluate():5
  phantomjs://webpage.evaluate():5
Subprocess failed
```

To overcome this problem you can create a single CLJS unit test file
(e.g., `test/cljs/enfocus/core_test.cljs`) containing the
corresponding namespace declaration and a failing unit test, just to
remember at each run that the unit tests have yet to be implemented.

```clj
(ns enfocus.core-test
  (:require-macros [cemerick.cljs.test :as m :refer (is deftest)])
  (:require [cemerick.cljs.test :as t]))

(deftest empty-test
  (is (= 0 1)))
```

If you now recompile everything you'll no longer receive those warnings.

```bash
lein do clean, compile, test
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
...
lein test user

Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
Running all ClojureScript tests.

Testing enfocus.core-test

FAIL in (empty-test) (:)
expected: (= 0 1)
  actual: (not (= 0 1))

Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:test 1, :pass 0, :fail 1, :error 0, :type :summary}

Testing enfocus.core-test

FAIL in (empty-test) (:)
expected: (= 0 1)
  actual: (not (= 0 1))

Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:test 1, :pass 0, :fail 1, :error 0, :type :summary}

Testing enfocus.core-test

FAIL in (empty-test) (:)
expected: (= 0 1)
  actual: (not (= 0 1))

Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:test 1, :pass 0, :fail 1, :error 0, :type :summary}
Subprocess failed
```

Good. As you see after the usual compilation, now the unit tests for
each CLJS build return the expected failing result. Note that
`lein test` executes all the three `test-commands`. If you want to
execute just one of them you need to run the `lein cljsbuild test`
command as follows:

```bash
lein cljsbuild test whitespace
Compiling ClojureScript.
Running ClojureScript test: whitespace

Testing enfocus.core-test

FAIL in (empty-test) (:)
expected: (= 0 1)
  actual: (not (= 0 1))

Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:test 1, :pass 0, :fail 1, :error 0, :type :summary}
Subprocess failed
```

### Commit your work

We can now commit our work, but if you issue the `git status` command
it will not include any created directory which is
still empty (see NOTE 12 below).

You can safely commit your work anyway. But before doing that clean
the builds and remove the empty directories `project` and `testing`.

```bash
lein clean
git add .
git rm -r project testing
git commit -m "learn by contributing - part 1"
```

> NOTE 12: Git doesn't allow the addition of empty directories to a repo
> [without doing some tricks][25] and when you checkout the branch
> again you won't find those empty directories anymore
> (e.g., `test/clj/` and `resources/`).  If you really want to track those
> directories right now without following the above trick, you can
> just `touch` a file for each empty directory and then issue `git
> commit -am "touch few file to track empty directories"`.

## Final note

At the moment the changes proposed for `Enfocus` in this tutorial have
been kindly accepted by [Creighton Kirkendall][10] in the master
branch. Due to some fixes which required to extend the `ITransform`
and `ISelector` protocol to support `nil` args, the `enfocus` version
has been upgraded to `"2.1.0-SNAPSHOT"`.

Use it at your own convenience (and risk too).

Stay tuned for the next tutorial.

# Next Step - [Tutorial 21 - Learning by Contributing (Part 2)][26]

In the [next tutorial][26] we're going to adjust `Enfocus` for
packaging it as a `jar`. We'll then instrument it with
`piggieback`, publish to `clojars` and we'll even use it as a
dependency in a very simple project to demonstrate that all the
changes we made are not affecting the `Enfocus` codebase, which is still
working as expected.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[2]: https://clojars.org/
[3]: https://github.com/shoreleave
[4]: https://github.com/ckirkendall/enfocus
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[6]: https://github.com/cgrand/enlive
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[9]: https://github.com/levand/domina
[10]: https://github.com/ckirkendall
[11]: https://github.com/cemerick/clojurescript.test
[12]: https://github.com/cemerick
[13]: https://help.github.com/articles/fork-a-repo
[14]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md#no-black-magic
[15]: https://github.com/emezeske/lein-cljsbuild/blob/master/doc/CROSSOVERS.md
[16]: https://github.com/emezeske/lein-cljsbuild
[17]: https://github.com/lynaghk/cljx
[18]: https://github.com/lynaghk
[19]: https://github.com/swannodette
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[21]: https://github.com/clojure/clojure/blob/master/src/clj/clojure/test.clj
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md
[23]: http://phantomjs.org/
[24]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md#instructing-lein-cljsbuild-about-phantomjs
[25]: http://stackoverflow.com/questions/115983/how-do-i-add-an-empty-directory-to-a-git-repository
[26]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-21.md
[27]: https://github.com/magomimmo/modern-cljs/blob/cda20296ab8008ada32fb879c4d8fadc50357f59/doc/tutorial-18.md#step-3---run-the-brepl
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
