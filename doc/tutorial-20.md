# Tutorial 20 - Learn by collaborating (Part 1)

In the [previous tutorial][1] we described a couple of approaches to
survive while livin' on the edge of a continuosly changing CLJ/CLJS
libs used as dependencies in our projects. We ended up by publishing
to [Clojars][2] a set of four [Shoreleave][3] libs which the
`modern-cljs` project directly or indirectly depended on. This way you
became collaborative with the CLJ/CLJS communities and, at the same
time, more indipendent from someone else's decision to merge or refuse
your pull requests.

In this tutorial we're getting back to CLJS and trying to be
collaborative as well while learning how to use the [Enfocus][4] lib.

## Introduction

In the [Tutorial 14 - Its better to be safe than sorry (Part 1)][5] we
introduced the [Enlive][6] server side templating system. Then, in the
[Tutorial 17 - Enlive by REPLing][7] we injected into the `Enlive`
templating system the form's input validators for the `Shopping
Calculator`.

In this tutorial we're going to introduce the [Enfocus][4] lib which
is a DOM manipulation and templating library for ClojureScript
inspired by [Enlive][6]. This claim is interesting enough for who,
like me, is so obsessed by the application of the DRY principle and
envisions the opportunity to share some code between the `Enlive`
server side CLJ code and the `Enfocus` client side CLJS code.

## Preamble

In the [Tutorial 9 - DOM Manipulation][8] we used the events'
management of the [Domina][9] lib to implement the client side DOM
manipulation of the `Shopping Calculator` as a consequence of the
events triggered by the user interation with the browser.

Then, in the subsequent tutorials, even if we augumented the `Shopping
Calculator` sample by adding a bit of Ajax and by preparing both the
input validators and the corresponding unit tests, we missed to merge
those validators in the CLJS code as we did in the
[Tutorial 17 - Enlive REPLing][7] for server side implementation.

To fill this gap, we could just extend the client side code already
implemented by using the [Domina][9] lib, but we want to investigate
the eventuality of sharing some code with the corresponding server
side code and we'll give a try to [Enfocus][4].

## Living on the edge with Enfocus

We'll discuss the `"2.0.0-SNAPSHOT"` relase of `Enfocus` because
[Creighton Kirkendall][10], the author of the lib, is currently on the
way to publish the next stable release which is more evoluted that the
stable one (i.e. `"1.0.1"`).

By taking a look at the [Enfocus repo][4] there are few things to note
about it:

* first, the directories layout for the project is not so usual;
* second, it does not use the [clojurescript.test][11] lib by the
  great [Chas Emerick][12] to implement the lib unit testing;
* third, most of its dependencies and plugins are outdated.
  
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
moment we do not care about the `testing` directory, becausee we'll
later try to introduce the [clojurescript.test][11] lib for unit
testing the `Enfocus` lib. We'll focus our attention on the `project`
directory and the corresponding included `project.clj`.

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
is directly derived by extending the default directories layout
created by the `lein new any-project` command.

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

As you see, this *quasi* standard directories layout for a mixed
CLJ/CLJS project tries to keep separated all the kind of code from
each other (i.e. `clj`, `cljs` and `cljx`) and their scope in the
project too (`src` vs. `test`). It even keeps separated the static
resources from the code (i.e. `resources` vs. `src`) and and from
their scope as weel (`resources` vs. `dev-resources`).

Out first contribution to the `Enfocus` project will be to normalize
its diresctories layout to the above application of the separation of
concern principle.

> NOTE 1: `Enfocus` is not a true CLJ/CLJS mixed project. It just
> happens that it defines few macros in the `macros.clj` file. As you
> know this is one of the main differences between CLJ and CLJS and the
> CLJS macros have to be compiled by the CLJ compiler not the CLJS one.

### Moving stuff around

Let's modify the `Enfocus` directories layout by beeing more observant
to the separation of concerns principle.

```bash
cd ~/dev/enfocus/project
mkdir -p src/{clj,cljs}
mkdir -p test/{clj,cljs}
mkdir -p dev-resources/public/{css,js}
mv cljs-src/enfocus src/cljs
mv cljx-src/enfocus src/clj
mv src/clj/enfocus/enlive/syntax.cljx src/clj/enfocus/enlive/syntax.clj
mv src/cljs/enfocus/macros.clj src/clj/enfocus/
rm -rf cljs-src cljx-src
```

> NOTE 2: We created the `dev-resources` only: by being `Enfocus` a
> CLJS lib, it only needs static resoruces for unit testing purpouse.

The above commands first created the needed *standard* directories,
then moved the CLJ code under the `src/clj` directory and the CLJS
code under the `src/cljs` directory. Finally deleted the original
`cljs-src` and `cljx-src` directories.

The reason why we did not createe a `src/cljx` directory and we
instead moved the `cljx-src/enfocus` directory to the `src/clj` is
strictly related to a simple observation of the `syntax.cljx` source
code:

> it does not contain any features annotation and the two generated
> `syntax.clj` and `syntax.cljs` files will be identical to it. In
> such a case we say that the source code is *portable* from CLJ to
> CLJS and viceversa without any intervention. That's why we renamed
> the `syntax.cljx` file as `syntax.clj`.

As said in the
[Tutorial 16 - It's better to be safe than sorry (part 3)][14], I
always prefer to use the [:crossovers][15] feature of the
[lein-cljsbuild][16] plugin when I need to share a portable CLJ/CLJS
code and to use the [cljx][17] plugin by [Kevin Lynagh][18] only when
I have to deal with annotaded code ported from CLJ to CLJS or
viceversa.

> NOTE 3: It's my opinion that the only eventual exception to this
> rule is when you have to deal with both a portable and a ported
> codebase. In such a case you can use the `cljx` pluing only and save
> some typing in the `:clsjbuild` configuration. But you loose the
> explicit distintion between portable and ported codebase.

### Keep moving

It's very easy to understand that the `html` directory laying in the
`src/cljs` codebase has nothing to do with CLJS code. The name of the
`testing.cljs` is suspicious as well and if you take a look at it
you'll see testing code only. As said at the beginning of this
tutorial, we'd like to substitute the original `Enfocus` testing code
with correspondent unit tests based on the [clojurescript.test][11]
lib. So, we're going to move them away from the `src/cljs` codebase
and park into a `temp` directory to keep them available as a reference
when later we'll implement the unit tests.

```bash
mkdir -p temp/enfocus
mv src/cljs/enfocus/testing.cljs temp/enfocus/
mv src/cljs/enfocus/html temp/enfocus/
```

### Reflect the changes in the project.clj

The directories layout changes need now to be reflected in the
`project.clj` file as shown in the following fragment.

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
   :crossover-jar true

   :builds
   [{:builds nil,
     :source-paths ["src/cljs" "test/cljs"]
     ...}]})
```

The `:source-paths` option for CLJ is now set to read CLJ files only
and we added the `:test-paths` option to host any eventual CLJ unit
testing code just in case we had later to take care of it.

We hooked the `cljsbuild` subtasks to the lein tasks and substituted
the `cljx` plugin and its `:cljx` configuration rules with the
corresponding `:crossovers` option set to the `enfocus.enlive.syntax`
portable namespace.

> NOTE 4: We added the `[:crossover-jar true]` option configuration as
> well to allow `lein` to include the generated `syntax.cljs` when
> when generating the `jar` packaging of the `Enfocus` project.

Finally, we set the `:source-paths` for the CLJS code to the CLJS
codebase and its corresponding unit tests code. This is in preparation
for the unit tests implemented with the `clojurescript.test` lib.

### Light the fire

Let's see if the project is still compiling as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "../testing/resources/public/cljs/enfocus.js" from ["src/cljs"]...
Successfully compiled "../testing/resources/public/cljs/enfocus.js" in 8.145292 seconds.
```

Not bad. The succeded compilation is not something to count on and it
is not even testable at the moment, because there are more tests to be
executed. 

> NOTE 5: I suggest to frequently commit you changes while refactoring
> the `Enfocus` project.
> 
> ```bash
> git add .
> git rm -r cljs-src cljx-src
> git commit -m "separation of concerns: step 1"
> ```

## Separation of concerns: step 2

In the previous paragraph we separated the concern about CLJ codebase
from the concern about CLJS codebase.  By moving the `testing.cljs`
and the `html` away from the `src/cljs/enfocus` directory, we also
started to separate the concern about the `Enfocus` lib `in se` from
the concern about its tests. That said, the `:cljsbuild` build is
still saving the emitted JS file in the `testing` directory. Let's
work on that too.

### Keep moving

First we want to move the `testing` directory under the `project/temp`
temporary directory to keep it as a reference for later.

```bash
mv ../testing/ temp/
```

### Reflect the changes in the project.clj

We're are almost done. We now need to reflect in the `:builds` section
of the `project.clj` file the last move.

Here is the fragment of the `project.clj` file been modified to
reflect the more *standard* directories layout introduced at the
beginning of the tutorial.

```bash
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :builds
   [{...
     :compiler
     {:output-to "dev-resources/public/js/enfocus.js"
      ...}}]})
```

### Last move before step ahead

By having moved everything to the `project` directory of the
`Enfocus` repo, we can now move down of one level its content.

```bash
cd ~/dev/enfocus
mv project/project.clj .
mv project/src .
mv project/test .
mv project/dev-resources .
mv project/temp .
rm -rf project
```

Here is the obtained directories layout.

```bash
tree
.
├── README.textile
├── dev-resources
│   └── public
│       ├── css
│       └── js
├── project.clj
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
cd ~/dev/enfocus # if not already done
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "dev-resources/public/js/enfocus.js" from ["src/cljs"]...
Successfully compiled "dev-resources/public/js/enfocus.js" in 7.742492 seconds.
```

Not bad. Now clean the project and commit your work in preparation for
the next steps ahaead.

```bash
lein clean
git add .
git rm -r project/ testing/
git commit -m "separation of concerns: step 2"
```

## Update dependencies and plugins

Now that the directories layout seems to be more compatible with the
*default lein template*, we can focus our attention to the `Enfocus`
used releases of plugins and dependencies.

### Upgrade to lein-cljsbuild "0.3.3"

The latest available [lein-cljsbuild][16] plugin release is the
`"0.3.3"`. Before this release, `lein-cljsbuild` would silently
downloaded a specific CLJS release. As you know CLJS is a very very
young language and it's very frequently updated thanks by its great
commiter (i.e. [David Nolen][19]). The latest available CLJS release
at the moment of this writing is the `"0.0-1913"`. If you have decided
to give to CLJS a try, you've already decided for *livin' on the
edge*, so be consequent with this decision.

The `"0.3.3"` release of `lein-cljsbuild` plugin now warns if you have
not explicitly specified a CLJS dependency in your project and
implicitly donwloads the `"0.0-1859"` CLJS release which in turns
requires the `"1.5.1"` release of CLJ. It also requires a `lein`
release `"2.1.2"` or higher.

Wow, tree upgrades in one shot. Here is the fragment of the modified
`project.clj`.

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

There is still one thing I don't like to much in the `project.clj`. As
we learnt from the [Tutorial 7 - Being doubly aggressive][20] which I
suggest you to review, CLJS offers 4 different compiler optimizations:

* `:none` (we have not yet used in this series)
* `:whitespace`
* `:simple`
* `:advanced`

Actually the `project.clj`'s `:builds` setting uses the `whitespace`
optimization only. We want to be sure that `Enfocus` works with any
optimization, because we can't anticipate how this `lib` will be used
in a someone else's CLJS project.

When you add more builds to your `:builds` settings and you want to
name them, you need to change its value from a vector to a map.

Here is the fragment of the modified `project.clj`.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  ...
  :cljsbuild
  {...
   :builds {:whitespace
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/whitespace.js"
              :optimizations :whitespace
              :pretty-print true}}
            
            :simple
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/simple.js"
              :optimizations :simple
              :pretty-print false}}

            :advanced
            {:source-paths ["src/cljs" "test/cljs"]
             :compiler
             {:output-to "dev-resources/public/js/advanced.js"
              :optimizations :advanced
              :pretty-print false}}}})
```

We now have four CLJS builds, one for each optimization level:

* `:whitespace`: it emits the `dev-resources/public/js/whitespace.js`
  JS file;
* `:simple`: it emits the `dev-resources/public/js/simple.js` JS file;
* `:advanced`: it emits the `dev-resources/public/js/advanced.js` JS
  file.

### Light the fire

Let's see if the project is still compiling as expected for each
optimization level.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "dev-resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/simple.js" in 18.15866 seconds.
Compiling "dev-resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/advanced.js" in 8.297606 seconds.
Compiling "dev-resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/whitespace.js" in 3.341929 seconds.
```

Good. It works.

> NOTE 6: Take into account that once you hook `cljsbuild` subtasks to
> `lein` tasks, if you want to compile a specific `cljsbuild` build
> (e.g. `:whitespace`) you can't pass `whitespace` to the `lein compile`
> task. You still have to use the `lein cljsbuild once whitespace` or
> `lein cljsbuild auto whitespace` commands.
> 
> ```bash
> lein do clean, cljsbuild once whitespace
> Deleting files generated by lein-cljsbuild.
> Compiling ClojureScript.
> Compiling "dev-resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
> Successfully compiled "dev-resources/public/js/whitespace.js" in 10.414104 seconds.
> ```

## Enter clojurescript.test

At the moment there are no clear winners for unit testing CLJ code,
even if the [clojure.test][21] lib is included with the CLJ core. The
scenario for unit testing CLJS code is even more fragmented, which is
a PITA when you want to unit test your CLJS code or when you want to
learn a CLJS lib by taking a look at its unit tests too.

In few previous tutorials of the series we introduced and used
[clojure.test][21] and [clojurescript.test][11] libs for our unit
testing purpouse. Aside from any expressivity considerations about
`clojure.test` and `clojurescript.test` libs which I'm not going to
discuss in this context, the only true reason why I decided to use
them depends on the fact that they allow me to share the same unit
tests codebase for both CLJ and CLJS code. To me even this reason
alone it's enough to adopt them.

I would love to not waste my time in learning a new CLJ/CLJ lib by
having first to learn its project structure and/or its unit testing
approach. The CLJ/CLJS programming languages already requires enough
efforts for switching our mind up side down in order to afford such a
fragmentation in the unit testing approach as well.

Enough words. Let's step ahead by introducing the `clojurescript.test`
lib as our choosen unit testing lib for the `Enfocus` lib.

If you don't remeber how to setup unit testing with
[clojurescript.test][11] I strongly suggest to review the
[Tutorial 16 - It's better to be safe than sorry (part 3][22] of this
series, because in this one we're working a little bit faster and
without explaining every single detail.

In a previous paragraph we already created the `test/clj` and
`test/cljs` directories for hosting the CLJ and CLJS unit tests.

We now need to add the [phantomjs headless browser][23] and configure
the `:test-commands` setting in the `:cljsbuild` section of the
`project.clj` file [as we did][24] in the context of the `modern-cljs`
project.

If you still have the `modern-cljs` repo in your development
directory, you can copy the `runners` directory from there. Otherwise
just clone the `mpdern-cljs` repo and do the following:

```bash
# clone the modern-cljs project
cd ~/dev
git clone https://github.com/magomimmo/modern-cljs.git
cd ~/dev/enfocus/
# recursively copy the runners directory
cp -r ~/dev/modern-cljs/runners .
```

> NOTE 7: Unfortunately, AFIK, there is no way to add `phantomjs` as a
> dependency of a project. This means that you have to donwload it and
> install it apart from the project itself.

The table is set. We can now modify the `project.clj` by adding the
[clojurescript.test][11] lib, the `:test-commands` and reviewing the
`:builds` configurations emitting the JS code.

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
                   ["runners/phantomjs.js" "dev-resources/public/js/whitespace.js"]

                   "simple"
                   ["runners/phantomjs.js" "dev-resources/public/js/simple.js"]

                   "advanced"
                   ["runners/phantomjs.js" "dev-resources/public/js/advanced.js"]}})
```

As you see we just added the `clojurescript.test` dependency and a
test command for each JS file emitted by each build.

> NOTE 8: At the moment we don't care about differentiating lein
> profiles. This is something we'll eventualy afford in a later turorial
> specifically dedicated to lib/project deployment.

### Light the fire

Let's see if the `Enfocus` lib is still able to compile as expected.

```bash
lein do clean, compile
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "dev-resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/simple.js" in 17.89025 seconds.
Compiling "dev-resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/advanced.js" in 8.204821 seconds.
Compiling "dev-resources/public/js/whitespace.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "dev-resources/public/js/whitespace.js" in 3.281859 seconds.
```

Not bad, `Enfocus` is still compiling as expected. Even if we have
still a long way to reach our target of unit testing `Enfocus` by
using `clojurescript.test` lib, we set everything in place. 

### Commit your work

We could now commit our work, but if you issue the `git status`
command it will not show you the addition of the `runners` directory.

```bash
# On branch tutorial-20
# Changes not staged for commit:
#   (use "git add <file>..." to update what will be committed)
#   (use "git checkout -- <file>..." to discard changes in working directory)
#
#	modified:   project.clj
#
no changes added to commit (use "git add" and/or "git commit -a")
```

This is because of the presence of the `*.js` exlusion rule in the
`.gitignore` file.

```bash
pom.xml
*jar
.lein-failures
.lein-deps-sum
target
.settings
.metadata
.project
.classpath
*.js
testing/resources/public/cljs/
*.*#
.#*
*.*~

project/.lein-plugins
project/.generated
```

To adapt the `.gitignore` file to the new directories layout and to
the new `project.clj` settings, modify it as follows:

```bash
pom.xml
*jar
.lein-failures
.lein-deps-sum
target
.settings
.metadata
.project
.classpath
dev-resources/*.js
*.*#
.#*
*.*~
```

You can naw safetly commit your work.

```bash
git add .
git commit -m "added clojurescript.test"
```

## What's next

In the next tutorial we'll start implementing unit tests by using the
`clojurescript.test` lib. At the moment the proposed modification of
the `Enfocus` lib have been kindly accepeted by
[Creighton Kirkendall][10] for the next release as `Enfocus
"2.0.1-SNAPSHOT".

```clj
(defproject enfocus "2.0.1-SNAPSHOT"
  ...)
```
  
I don't suggest to immediately us it, because the "2.0.1-SNAPSHOT" is
just started and still without any unit test been implemented yet.

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
