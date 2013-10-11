# Tutorial 20 - Learn by Contributing (Part 1)

In the [previous tutorial][1] we described a couple of approaches for
surviving while living on the edge of a continuosly changing CLJ/CLJS
libs used as dependencies in our projects.

We ended up by publishing to [Clojars][2] a set of four
[Shoreleave][3] libs which the `modern-cljs` project directly or
indirectly depended on.

This way you may become collaborative with the CLJ/CLJS communities
and, at the same time, indipendent from someone else's decision to
merge or refuse your pull requests.

In this tutorial we're getting back to CLJS and trying to be
collaborative as well while learning how to use the [Enfocus][4] lib.

## Introduction

In the [Tutorial 14 - Its better to be safe than sorry (Part 1)][5] we
introduced the [Enlive][6] server side templating system. Then, in the
[Tutorial 17 - Enlive by REPLing][7] we injected into the `Enlive`
templating system the input validators for the `Shopping Calculator`
form.

In this tutorial we're going to introduce the [Enfocus][4] lib which
is a DOM manipulation and templating library for ClojureScript
inspired by [Enlive][6]. This claim is interesting enough for who,
like me, is so obsessed by the DRY principle and envisions the
opportunity to share some code between the `Enlive` server side CLJ
code and the `Enfocus` client side CLJS code.

## Preamble

In the [Tutorial 9 - DOM Manipulation][8] we used the [Domina][9] lib
to manipulate the DOM of the `Shopping Calculator` in reaction to the
events triggered by the user interation with the browser.

Then, in the subsequent tutorials, even if we augumented the `Shopping
Calculator` sample by adding to it a bit of Ajax and by preparing both
the input validators and the corresponding unit tests, we missed to
merge those validators in the CLJS client side code.

To fill this gap, we could just extend the CLJS client side code
already implemented by using the [Domina][9] lib, but we want to
investigate the eventuality of sharing some code with the
corresponding server side code and we'll give a try to [Enfocus][4].

## Living on the edge with Enfocus

We'll discuss the `"2.0.0-SNAPSHOT"` relase of `Enfocus` because
[Creighton Kirkendall][10], the author of the lib, is currently on the
way to publish the next stable release which is more evoluted than the
current `"1.0.1"`.

By taking a look at the [Enfocus repo][4] there are few things to note
about it:

* first, the directories layout for the project is not so usual;
* second, it does not use the [clojurescript.test][11] lib by the
  great [Chas Emerick][12] to implement the lib unit testing;
* third, due to the necessity to serve *legacy* code in production,
  most of its dependencies and plugins are outdated.
  
It seems to be a perfect opportunity to be collaborative with
[Creighton Kirkendall][10] by helping him in improving his very
interesting and promising lib.

## Fork, clone and branch

Let's start by [forking][13], cloning and branching the `Enfocus`
repo.

```bash
cd ~/dev
# clone your forked repo
git clone https://github.com/<your-github-name>/enfocus.git
cd enfocus
# add the upstream repo
git remote add upstream https://github.com/ckirkendall/enfocus.git
# create the upgrade branch 
git checkout -b tutorial-20
```

`Enfocus` has two main directories, `project` and `testing`. At the
moment we do not care about the `testing` directory, because we'll
later try to introduce the [clojurescript.test][11] lib for unit
testing and we'll focus our attention on the `project` directory only.

## Separation on concerns - Step 1

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

Now take a look at an almost *standard* directories layout of a
fictional CLJ/CLJS mixed project. I call it almost *standard*, because
it directly extends the default directories layout created by the
`lein new any-project` command.

```bash
tree
.
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

As you see, this *quasi standard* directories layout for a CLJ/CLJS
mixed project tries to keep separated from each other every type of
code (e.g. `clj` vs `cljs` vs `cljx`) and their scope too (e.g. `src`
vs `test`).

It even keeps separated the static resources from the code
(i.e. `resources` vs. `src`) and the development resources from the
production resources (e.g. `dev-resources` vs `resources`).

Our first contribution to the `Enfocus` project will be to normalize
its directories layout by using the above directories layout as a
reference.

> NOTE 1: `Enfocus` is not a true CLJ/CLJS mixed project. It just
> happens that it defines few macros in the `macros.clj` file. As you
> know this is one of the main differences between CLJ and CLJS and
> the CLJS macros have to be evaluted by the CLJ compiler at
> compile-time.

### Moving stuff around

Let's modify the `Enfocus` directories layout by beeing more observant
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

> NOTE 2: At the moment we did not created the `dev-resources`
> directory. Being `Enfocus` a CLJS lib, its static resoruces for unit
> testing purpouse should not be parked in the `resources`
> directory. We'll take care of that later.

The above commands first created the needed *standard* directories,
then moved the CLJ code under the `src/clj` directory and the CLJS
code under the `src/cljs` directory. Finally deleted the original
`cljs-src` and `cljx-src` directories.

The reason why we did not createe a `src/cljx` directory and moved
instead the `cljx-src/enfocus` directory to the `src/clj` one, is
strictly related to a simple observation of the `syntax.cljx` source
code:

> `syntax.cljx` does not contain any feature annotation and the two
> generated `syntax.clj` and `syntax.cljs` files will be identical to
> it. In such a case we say that the source code is *portable* from
> CLJ to CLJS and viceversa without any intervention.

As said in the
[Tutorial 16 - It's better to be safe than sorry (part 3)][14], I
always prefer to use the [:crossovers][15] setting of the
[lein-cljsbuild][16] plugin when I need to share a *portable* CLJ/CLJS
code and to use the [cljx][17] plugin by [Kevin Lynagh][18] only when
I have to deal with *annotaded* code *ported* from CLJ to CLJS or
viceversa.

> NOTE 3: It's my opinion that the only exception to this rule is when
> you have to deal with both *portable* and *ported* codebase. In such
> a case you can use the `cljx` pluing only, and save some typing in
> the `:clsjbuild` configuration. But you loose the explicit
> distintion between *portable* and *ported* codebase. For this reason
> in the `modern-cljs` series we used both the `:crossovers` setting
> and the `:cljx` plugin.

### Keep moving

The `html` directory now laying in the `src/cljs` path has nothing to
do with the CLJS codebase and the name of the `testing.cljs` file is
suspicious as well: if you take a look at it you'll see testing code
only.

As said, we are preparing the field for substituting the original
`Enfocus` testing code with correspondent unit tests based on the
[clojurescript.test][11] lib and we're going to move them away from
the `src/cljs` codebase by parking it into a `temp` directory for
later reference.

```bash
mkdir -p temp/enfocus
mv src/cljs/enfocus/testing.cljs temp/enfocus/
mv src/cljs/enfocus/html temp/enfocus/
```

### Reflect the changes in the project.clj

The new directories layout needs now to be reflected in the
`project.clj` file as follows.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  ...
  :plugins [[lein-cljsbuild "0.3.0"]]
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:crossovers [enfocus.enlive.syntax]

   :builds
   [{:builds nil,
     :source-paths ["src/cljs" "test/cljs"]
     ...}]})
```

The `:source-paths` setting for CLJ codebase is now set to read CLJ
files only. We also added the `:test-paths` setting to host CLJ unit
testing code, just in case we had later to take care of it.

Next we hooked the `cljsbuild` subtasks to the lein tasks and
substituted the `cljx` plugin and its `:cljx` configuration rules with
the corresponding `:crossovers` option, which is set to the
`enfocus.enlive.syntax` *portable* namespace.

Finally, we set the `:source-paths` for CLJS codebase to read CLJS
files only from the `src/cljs` path and form the `test/cljs` path in
preparation for unit testing.

### Light the fire

Let's see if the project is still compiling as expected.
	
```bash
MacBook-di-Sinapsi:enfocus mimmo$
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["src/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 8.145292 seconds.
```

Not bad. The succeded compilation is not something to count on. It is
not even testable at the moment, because there are no unit tests in
the `test/cljs" path to be executed.

## Separation of concerns: step 2

In the previous paragraph we separated the concern about CLJ codebase
from the concern about CLJS codebase.  By moving the `testing.cljs`
file and the `html` directory away from the `src/cljs/enfocus` path,
we also started to separate the concern about the `Enfocus` lib `in
se` from the concern about its testing code.

That said, the `:cljsbuild` build setting is still saving the emitted
JS file in the old `testing` directory. Let's work on that too.

### Keep moving

First we want to move the `testing` directory under the `project/temp`
temporary directory to keep it as a reference for later testing code
implementation.

```bash
lein clean # delete the generated `enfocus.js` before moving its containing directory
mv ../testing/ temp/
```

We now need to reflect this move in the `:builds` section of the
`project.clj` file by deleting the `:output-dir` setting and changing
the `:output-to` setting to the `"resources/public/js/enfocus.js"`
JS file.

```bash
(defproject enfocus "2.0.0-SNAPSHOT"
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

By having moved everything to the `project` directory of the `Enfocus`
repo, we can now move all the `project` directory content one level
down.

```bash
cd ..
mv project/project.clj .
mv project/src .
mv project/test .
mv project/resources .
mv project/temp .
rm -rf project
```

Here is the obtained directories layout.

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
Not bad. If you're curious about where `lein` is parking all the files
generated by the compilation task, here the layout of the `target` and
the `resources` directories.

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

> NOTE 4: The `cljsbuild` plugin generates the `syntax.cljs` file in
> the default `cljsbuild-crossover` directory. This directory is
> silently added to the `:source-paths` setting of the build for
> emitting the `syntax.js` JS file. The `syntax.js` file is then
> passed, as all the others JS files produced by the CLJS compiler, to
> the Google Closure Compiler for producing the final `enfocus.js` JS
> file. The optimization level of the final `enfocus.js` JS file
> depends on the `:optimizations` value configured in the `:compiler`
> setting.

## Update dependencies and plugins

Now that the directories layout is more consistent with the *
augumented default lein template*, we can focus our attention on
upating the `Enfocus` dependencies and plugins references.

### Upgrade to lein-cljsbuild `"0.3.3"`

The latest current available [lein-cljsbuild][16] plugin release is
the `"0.3.3"`. Before this release `lein-cljsbuild` silently download
a specific CLJS release.

The `"0.3.3"` release of `lein-cljsbuild` plugin now warns you when
you do not specify a CLJS dependency in your project and implicitly
downloads the `"0.0-1859"` CLJS release which, in turn, requires the
CLJ `"1.5.1"` release.  The `"0.3.3"` release also requires a `lein`
release `"2.1.2"` or higher.

> NOTE 5: Being CLJS a very young language, it's very frequently
> updated. The latest available CLJS release at the moment of this
> writing is the `"0.0-1913"`.

Wow, four changes in one shot to be edited in the `project.clj` file.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :min-lein-version "2.1.2"
  ...
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1913"]
                 ...]

  :plugins [[lein-cljsbuild "0.3.3"]]
  ...)
```

### Add more CLJS optimizations 

There is still one thing to be improved in the `project.clj`. As we
learnt from the [Tutorial 7 - Being doubly aggressive][20] which I
suggest you to review, CLJS offers 4 different compiler optimizations:

* `:none` (we are not going to use it)
* `:whitespace`
* `:simple`
* `:advanced`

Actually, the `project.clj`'s `:builds` setting uses the `whitespace`
optimization only. We want to be sure that `Enfocus` works with any
optimization level, because we can't anticipate how this `lib` will be
used in a someone else's CLJS project.

When you have more builds to your `:builds` settings and you want to
name them, you need to change its value from a vector to a map as
follows:

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
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

* `:whitespace`: emits the `resources/public/js/whitespace.js`
  JS file;
* `:simple`: emits the `resources/public/js/simple.js` JS file;
* `:advanced`: emits the `resources/public/js/advanced.js` JS
  file.

### Light the fire

Let's see if the project is still compiling as expected for each
optimization level.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/simple.js" in 17.844951 seconds.
Compiling "resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/advanced.js" in 8.713793 seconds.
Compiling "resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/whitespace.js" in 3.366037 seconds.
```

Good. It works.

> NOTE 6: Take into account that once you hook `cljsbuild` subtasks to
> `lein` tasks, if you want to compile a named `cljsbuild` build
> (e.g. `whitespace`) you can't pass its name to the `lein compile`
> task. You have to use the `lein cljsbuild once whitespace` or `lein
> cljsbuild auto whitespace` commands.  `

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

I would love to not waste my time in learning a new CLJ/CLJ lib by
having first to learn its project structure and/or its unit testing
approaches on both sides of the border.

The CLJ/CLJS programming languages already requires enough efforts for
switching our minds up side down in order to afford such a unit
testing fragmentation and uncertainty as well.

In few previous tutorials of the series we introduced and used
[clojure.test][21] and [clojurescript.test][11] libs for our unit
testing purpouse.

Aside from any expressivity considerations about `clojure.test` and
`clojurescript.test` libs, which I'm not going to discuss in this
context, the only reason I decided to use them depends on the fact
that they allow me to share unit testing API and the same unit tests
code for both CLJ and CLJS codebase. To me this reason alone it's
enough to make a choice.

Enough words. Let's step ahead by introducing the `clojurescript.test`
lib for testing the `Enfocus` lib.

If you don't remeber how to setup unit testing with
[clojurescript.test][11] I strongly suggest to review the
[Tutorial 16 - It's better to be safe than sorry (part 3)][22] of this
series. In this tuorial we're working a little bit faster and without
explaining every single detail.

In a previous paragraph we already created the `test/clj` and
`test/cljs` directories for hosting the CLJ and CLJS unit tests.

We now need to add the [phantomjs headless browser][23] and configure
the `:test-commands` setting in the `:cljsbuild` section of the
`project.clj` file [as we did][24] in the context of the `modern-cljs`
project.

If you still have the `modern-cljs` repo in your development
directory, you can copy the `runners` directory from there. Otherwise
just clone the `modern-cljs` repo and do the following:

```bash
# clone the modern-cljs project
cd ~/dev
git clone https://github.com/magomimmo/modern-cljs.git
cd ~/dev/enfocus/
# recursively copy the runners directory
cp -R ~/dev/modern-cljs/runners .
```

> NOTE 7: Unfortunately, AFIK, there is no way to add `phantomjs` as a
> dependency in a project. This means that you have to donwload and
> install it apart from the project itself.

The table is now set and can now modify the `project.clj` by adding
the [clojurescript.test][11] lib and the `:test-commands` setting.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :dependencies [...
                 [com.cemerick/clojurescript.test "0.0.4"]
                 ...]
  ...
  :cljsbuild
  {...
   :test-commands {"whitespace"
                   ["runners/phantomjs.js" "resources/public/js/whitespace.js"]

                   "simple"
                   ["runners/phantomjs.js" "resources/public/js/simple.js"]

                   "advanced"
                   ["runners/phantomjs.js" "resources/public/js/advanced.js"]}})
```

As you see we added the `clojurescript.test` dependency and a test
command for each JS file emitted by each build.

> NOTE 8: At the moment we don't care about differentiating lein
> profiles. This is something we'll afford later.

### Light the fire

Let's see if the `Enfocus` lib is still able to compile as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-0/cemerick/cljs/test.cljs
Successfully compiled "resources/public/js/simple.js" in 44.950013 seconds.
Compiling "resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-1/cemerick/cljs/test.cljs
Successfully compiled "resources/public/js/advanced.js" in 25.737024 seconds.
Compiling "resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-2/cemerick/cljs/test.cljs
Successfully compiled "resources/public/js/whitespace.js" in 8.730157 seconds.
```

lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "dev-resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-0/cemerick/cljs/test.cljs
Successfully compiled "dev-resources/public/js/simple.js" in 44.950013 seconds.
Compiling "dev-resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 file:/Users/mimmo/.m2/repository/com/cemerick/clojurescript.test/0.0.4/clojurescript.test-0.0.4.jar!/cemerick/cljs/test.cljs
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-1/cemerick/cljs/test.cljs
Successfully compiled "dev-resources/public/js/advanced.js" in 25.737024 seconds.
Compiling "dev-resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
WARNING: set-print-fn! already refers to: cljs.core/set-print-fn! being replaced by: cemerick.cljs.test/set-print-fn! at line 252 /Users/mimmo/devel/enfocus/target/cljsbuild-compiler-2/cemerick/cljs/test.cljs
Successfully compiled "dev-resources/public/js/whitespace.js" in 8.730157 seconds.
```

> NOTE 9: You [already know][26] about the above *WARNING* caused by the
> `clojurescript.test` lib.

Not bad, `Enfocus` is still compiling as expected. Even if we still
have to implement unit tests, we set everything in place.

> NOTE 10: If you issue the `lein test` command without having defined
> any CLJS unit test yet, you'll receive more error messages. To
> overcome this problem you can create a single CLJS unit test file
> (e.g. `test/cljs/enfocus/core-test.clj`) containing the
> corresponding namespace declaration and a failing unit test, just to
> remember at each run that the unit tests have to be implemented yet.
>
> ```clj
> (ns enfocus.core-test
>   (:require-macros [cemerick.cljs.test :as m :refer (is deftest)])
>   (:require [cemerick.cljs.test :as t]))
> 
> (deftest empty-test 
>   (is (= 0 1)))
>```

### Commit your work

We can now commit our work, but if you issue the `git status` command
it will not show you niether the addition of any created directory
which is still empty (see NOTE 11 below) and neither the `runners`
directory.

The latter is because of the presence of the `*.js` exclusion rule in
the `.gitignore` file.

```bash
...
*.js
testing/resources/public/cljs/
...
project/.lein-plugins
project/.generated
```

To adapt the `.gitignore` file to the new directories layout without
loosing the `runners/phantomjs.js` file, remove the `testing` and
`project` rules and add a new rule for the JS files.

```bash
...
resources/public/js/*.js
...
```

You can naw safetly commit your work.

```bash
lein clean
git add .
git rm -r project testing
git commit -m "learn by contributing"
```

> NOTE 11: Git doesn't allow to add empty directories to a repo
> [without doing some tricks][25] and when you checkout the branch
> you'll not find those empty directories anymore (e.g. `test/clj` and
> `resorces`).  If you really want to track those directories right
> now without following the above trick, you can just `touch` a file
> for each empty directory and then issue the `git commit -am "touch
> few file to track empty directories"`.

## What's next

We should now start implementing few unit tests based on the
[clojurescript.test][11] testing lib. We are going to postpone this
topic in a next tutorial for a couple of reasons:

* we want anticipate few `project.clj` settings which
  affect the `jar` packaging to be prepared for any CLJ/CLJS mixed project
  deployment;
* to be able to even think about needed unit tests for a lib, we
  should first learn at least its basic use cases.

### Enfocus Packaging

When we previously substituted the `cljx` plugin with the
`:crossovers` setting of the `cljsbuild` plugin, we substituted the
`:hooks [cljx.lhooks]` setting with the corresponding `:hooks
[leiningen.cljsbuild]` setting as well.

This way the `cljsbuild` tasks are hooked to the main `lein`
tasks. That's why we were able to issue the `lein do clean, compile`
chain of commands instead of the more verbose `lein do clean,
cljsbuild clean, cljsbuild once` chain of commands. Following is the
list of the supported `lein` tasks when `cljsbuild` is hooked to it:

```bash
* lein clean # calls lein cljsbuild clean task
* lein compile #call lein cljsbuild once task
* lein test # call lein cljsbuild test task
* lein jar
```

We already used the first three hooked `lein` tasks. Let's now try the
`lein jar` task by first see the associated help:

```bash
lein help jar
Package up all the project's files into a jar file.

Create a $PROJECT-$VERSION.jar file containing project's source files as well
as .class files if applicable. If project.clj contains a :main key, the -main
function in that namespace will be used as the main-class for executable jar.

With an argument, the jar will be built with an alternate main.

Arguments: ([main] [])
```

Good. the `lein jar` tasks seems to be what we need to package the
`Enfocus` lib.

Let's try it.

```bash
lein do clean, compile, jar
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
...
Successfully compiled "resources/public/js/whitespace.js" in 8.692111 seconds.
Compiling ClojureScript.
Created /Users/mimmo/devel/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
```

Now take a look at the `enfocus-2.0.1-SNAPSHOT.jar` jar package.

```bash
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
    92 Fri Oct 11 15:57:26 CEST 2013 META-INF/MANIFEST.MF
  3579 Fri Oct 11 15:57:26 CEST 2013 META-INF/maven/enfocus/enfocus/pom.xml
   152 Fri Oct 11 15:57:26 CEST 2013 META-INF/maven/enfocus/enfocus/pom.properties
  1875 Fri Oct 11 15:57:26 CEST 2013 META-INF/leiningen/enfocus/enfocus/project.clj
  1875 Fri Oct 11 15:57:26 CEST 2013 project.clj
 11526 Fri Oct 11 15:57:26 CEST 2013 META-INF/leiningen/enfocus/enfocus/README.textile
118087 Fri Oct 11 15:57:06 CEST 2013 public/js/advanced.js
728197 Fri Oct 11 15:56:36 CEST 2013 public/js/simple.js
1330646 Fri Oct 11 15:57:14 CEST 2013 public/js/whitespace.js
  1928 Fri Oct 11 10:26:10 CEST 2013 enfocus/enlive/syntax.clj
  4386 Fri Oct 11 10:26:10 CEST 2013 enfocus/macros.clj
```

Oh my God. The jar contains all the `cljsbuild` generated JS files and
the CLJ source files, but it does not contain any CLJS source
file. Aside from the `META/INF` stuff and `project.clj` we would like
to package in the `Enfocus` jar only the needed CLJ/CLJS source files
to use the lib itself in a CLJ/CLJS project (e.g. `modern-cljs`). More
precisely:

* clj sources: `macros.clj` and `syntax.clj`
* cljs sources: `syntax.cljs`, `core.cljs`, `events.cljs` and
  `effects.cljs`.

### Fill the package

We'll go on step by step.

#### Include `:crossovers` generated CLJS sources

To include the `syntax.cljs` file generated by the `:crossovers`
setting we need to add to the `cljsbuild` section the `:crossover-jar
true` setting as well.

```clj
(defproject enfocus "2.0.1-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :crossover-jar true
   ...})
```

Run the `lein jar` command again and inspect the emitted jar package.

```bash
lein jar
Compiling ClojureScript.
Created /Users/mimmo/devel/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
```

```bash
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
...
  2105 Fri Oct 11 16:46:00 CEST 2013 enfocus/enlive/syntax.cljs
```

The `syntax.cljs` is now included in the jar package. First problem
solved.

#### Include any CLJS sources

The `cljsbuild` plugin offers a `:jar true` setting for each `build`
in the `:builds` section which is very similar to the just used
`:crossover-jar` setting.

```clj
(defproject enfocus "2.0.1-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :builds {...
            :advanced
            {:source-paths ["src/cljs" "test/cljs"]
             :jar true
             ...}}})
```

Here we decided to add the `:jar true` setting to the `:advanced`
build only. As a matter of facts this should be enough, because we
want to include in the jar package the `cljs` sources only an not the
corresponding JS files emitted by the compiler for each optimization
level.

Run the `lein jar` command again and inspect the emitted jar package.

```bash
lein jar
Compiling ClojureScript.
Created /Users/mimmo/devel/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
```

```bash
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
...
118087 Fri Oct 11 15:57:06 CEST 2013 public/js/advanced.js
728197 Fri Oct 11 15:56:36 CEST 2013 public/js/simple.js
1330646 Fri Oct 11 15:57:14 CEST 2013 public/js/whitespace.js
  1928 Fri Oct 11 10:26:10 CEST 2013 enfocus/enlive/syntax.clj
  4386 Fri Oct 11 10:26:10 CEST 2013 enfocus/macros.clj
  2105 Fri Oct 11 17:08:26 CEST 2013 enfocus/enlive/syntax.cljs
 23336 Fri Oct 11 17:08:26 CEST 2013 enfocus/core.cljs
  4247 Fri Oct 11 17:08:26 CEST 2013 enfocus/events.cljs
  6851 Fri Oct 11 17:08:26 CEST 2013 enfocus/effects.cljs
   168 Fri Oct 11 17:08:26 CEST 2013 enfocus/core_test.cljs
```

That's better than before, but still unsatisfactory. As you see, even
if all the `cljs` sources are now included in the jar package, the JS
sources are still there and the `core_test.cljs` fictional unit
test as well.

#### Remove any generated JS sources

Let's take care of the generated JS first. At the beginning of this
tutorial we have shown the *quasi standard directories layout* of a
mixed CLJ/CLJS project and talked a bit about the separation of
concerns principle applied to the project static resources. The static
resources used for using the lib have to be parked in the `resources`
directory, while the static resources to be used in the
developing/testing phase have to be parked in the `dev-resources`
drectory. This is because `lein jar` command includes in the generated
package only the static resources parked in the `resourses`
directory. In the `Enfocus` project the emitted JS files are used for
testing purpouse only, not for deploying the lib to third parties
developers. 

Let's move the `resources` to the `dev-resources` one and consequently
modify any referece to it in the `project.clj`.

```bash
mv resources dev-resources
```

```clj
(defproject enfocus "2.0.1-SNAPSHOT"
  ...
  {...
   :builds {:whitespace
            {...
             :compiler
             {:output-to "dev-resources/public/js/whitespace.js"
              ...}}
             
            :simple 
            {...
			 :compiler
             {:output-to "dev-resources/public/js/simple.js"
              ...}}
             
            :advanced
            {...
             :compiler
             {:output-to "dev-resources/public/js/advanced.js"
              ...}}}
   :test-commands {"whitespace"
                   ["runners/phantomjs.js" "dev-resources/public/js/whitespace.js"]
                   
                   "simple"           
                   ["runners/phantomjs.js" "dev-resources/public/js/simple.js"]
                   
                   "advanced"
                   ["runners/phantomjs.js" "dev-resources/public/js/advanced.js"]}})
```

Here we have modified the each `:output-to` setting and each
corresponding `:test-commands` setting from referecing the `resources`
directory to referencing the `dev-resources` directory.

Run the `lein jar` task again and inspect the emitted jar package.

```bash
lein jar
Compiling ClojureScript.
Created /Users/mimmo/devel/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
```

```bash
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
...
  1928 Fri Oct 11 10:26:10 CEST 2013 enfocus/enlive/syntax.clj
  4386 Fri Oct 11 10:26:10 CEST 2013 enfocus/macros.clj
  2105 Fri Oct 11 19:56:20 CEST 2013 enfocus/enlive/syntax.cljs
 23336 Fri Oct 11 19:56:20 CEST 2013 enfocus/core.cljs
  4247 Fri Oct 11 19:56:20 CEST 2013 enfocus/events.cljs
  6851 Fri Oct 11 19:56:20 CEST 2013 enfocus/effects.cljs
   168 Fri Oct 11 19:56:20 CEST 2013 enfocus/core_test.cljs
```

The JS sources are not included anymore. Much better than before. Now
we only have to find the way to not include the `core_test.cljs`
fictional unit test.

#### Remove unit tests

The `core_test.cljs` unit test is included in the jar package because
the `:advanced` build that we have selected to be augumented with the
`:jar true` setting still has the `"test/cljs"` directory set as value
in its `:source-paths` setting.

But wait minute, if we remove the `"test/cljs"` directory from the
`:advanced` build, we will not be able to test it anymore. The easier
way to solve this problem is to copy the build with a new name, remove
the `:jar true` setting form the `:advanced` build and finally remove
the `"test/cljs"` directory from the `:source-paths` setting of the
new build.

```clj
(defproject enfocus "2.0.1-SNAPSHOT"
  ...
   :cljsbuild
  {...
   :builds {:deploy
            {:source-paths ["src/cljs"]
             :jar true
             :compiler
             {:output-to "dev-resources/public/js/enfocus.js"
              :optimizations :advanced
              :pretty-print false}}
            ...
			:advanced
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/advanced.js"
              :optimizations :advanced
              :pretty-print false}}}
   ...)
```

Run the `lein jar` task again and inspect the emitted jar package.

```bash
lein jar
Compiling ClojureScript.
Compiling "dev-resources/public/js/enfocus.js" from ["src/cljs"]...
Successfully compiled "dev-resources/public/js/enfocus.js" in 38.729721 seconds.
Created /Users/mimmo/devel/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
```

> NOTE 12: This time the `lein jar` task runs the compilation of the new
> `:deploy` build as well.

```bash
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
    92 Fri Oct 11 20:16:10 CEST 2013 META-INF/MANIFEST.MF
  3579 Fri Oct 11 20:16:10 CEST 2013 META-INF/maven/enfocus/enfocus/pom.xml
   152 Fri Oct 11 20:16:10 CEST 2013 META-INF/maven/enfocus/enfocus/pom.properties
  2170 Fri Oct 11 20:16:10 CEST 2013 META-INF/leiningen/enfocus/enfocus/project.clj
  2170 Fri Oct 11 20:16:10 CEST 2013 project.clj
 11526 Fri Oct 11 20:16:10 CEST 2013 META-INF/leiningen/enfocus/enfocus/README.textile
  1928 Fri Oct 11 10:26:10 CEST 2013 enfocus/enlive/syntax.clj
  4386 Fri Oct 11 10:26:10 CEST 2013 enfocus/macros.clj
  2105 Fri Oct 11 20:16:10 CEST 2013 enfocus/enlive/syntax.cljs
 23336 Fri Oct 11 20:16:10 CEST 2013 enfocus/core.cljs
  4247 Fri Oct 11 20:16:10 CEST 2013 enfocus/events.cljs
  6851 Fri Oct 11 20:16:10 CEST 2013 enfocus/effects.cljs
```

Great. We now have a jar package which includes exactly what is needed
by a third party deveoper to use the `Enfocus` artifact.


At the moment the changes proposed for `Enfocus` in this tutorial have
been kindly accepeted by [Creighton Kirkendall][10] as
"2.0.1-SNAPSHOT" branch.

I don't suggest to immediately us it, because it's just started and
still without any unit tests implemented yet.

Stay tuned for the next tutorial.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
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
[26]: https://github.com/magomimmo/modern-cljs/blob/2bcc97da57080246406f0caff0a6e018d54b39d5/doc/tutorial-18.md#step-3---run-the-brepl
