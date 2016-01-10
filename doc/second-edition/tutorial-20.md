# Tutorial 20 - House Keeping

In the [previous tutorial][1] we tried to explain how to make a
library compliant with the
[Readers Conditional](http://clojure.org/reader#The%20Reader--Reader%20Conditionals). As
an example, we used the [`valip`](https://github.com/magomimmo/valip)
library that we already used in the `modern-cljs` project for
validating the input field of the Login and Shopping forms we used as
CLJ/CLJS playground. 

Even if we reached a decent result, we left some work to be done,
namely the deployment of the updated library to
[clojars](https://clojars.org/), the notorious community repository
for open source Clojure libraries.

## Introduction

In this tutorial we're going to fill that gap. But first we have to
digress on a couple of topics we left uncover about the `boot` build
tool, because they constitute a prerequisite to the `clojars`
deployment itself:

* [POM](https://maven.apache.org/pom.html#Introduction) (Project
  Object Model): information about the project
* [Dependency Scope](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html):
  used to limit the transitivity of a dependency.

## Minimal POM (Project Object Model)

To quickly finish the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#locally-install-valip),
we locally installed the updated version of `valip` by using the `lein
install` task instead of of using the `boot`build tool. This was
because the `project.clj` build file used by `lein` already had the
[minimal information](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html#Minimal_POM)
(i.e., `(defproject org.clojars.<your_github_name>/valip
"0.4.0-SNAPHOST" ...)`) needed to create and package the `valip`
library, while we did not informed the corresponding `build.boot`
build file used by `boot` with the same information.

You can easily fill the gap by chaining three [built-in](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj) `boot` tasks:

* `pom`: to create the `pom.xml` POM file
* `jar`: to package the project in a `jar` file
* `install`: to locally install the `jar` file in the local maven repository.

## The `pom` task

Let's look at the `pom` docstring first:

```bash
cd /path/to/valip
boot pom -h
Create project pom.xml file.

The project and version must be specified to make a pom.xml.

Options:
  -h, --help                   Print this help info.
  -p, --project SYM            Set the project id (eg. foo/bar) to SYM.
  -v, --version VER            Set the project version to VER.
  -d, --description DESC       Set the project description to DESC.
  -u, --url URL                Set the project homepage url to URL.
  -s, --scm KEY=VAL            Conj [KEY VAL] onto the project scm map (KEY is one of url, tag, connection, developerConnection).
  -l, --license NAME:URL       Conj [NAME URL] onto the map {name url} of project licenses.
  -o, --developers NAME:EMAIL  Conj [NAME EMAIL] onto the map {name email} of project developers.
  -D, --dependencies SYM:VER   Conj [SYM VER] onto the project dependencies vector (overrides boot env dependencies).
```

As you see, to create a minimal `pom.xml` file for the `valip` project
you need to specify at least the `-p, --project` and the `-v,
--version` command line options. As said, in the `project.clj` build
file of `leiningen` that information was already present in the
project definition `(defproject org.clojars.<your_github_name>/valip
"0.4.0-SNAPSHOT" ...)`:

* `org.clojars.<your_github_name>` is the `groupId` of the project;
* `valip` is the `artifactId` of the project;
* `"0.4.0-SNAPSHOT"` is the project's `version`.

Before trying to run the `pom` task, let's first get rid of the stuff
generated in the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#locally-install-valip)
by running the `lein install` task to locally install `valip`:

```bash
rm -rf pom.xml target
```

Let's now launch the `pom` task at the command line, by providing the
needed information about the project artifact (as `groupId/artifactId`
symbol) and its version as follows:

```bash
boot pom -p org.clojars.magomimmo/valip -v 0.4.0-SNAPSHOT
Writing pom.xml and pom.properties...
```

> NOTE 1: remember to substitute `magomimmo` with your github name ad
> `groupId`

Even if the command reported the writing of the `pom.xml` and the
`pom.properties` files, which is a misleading message to me, if you
take a look at the project directory you'll not see them:

```bash
tree
.
├── README.md
├── boot.properties
├── build.boot
├── project.clj
├── resources
├── src
│   └── valip
│       ├── core.cljc
│       ├── macros.clj
│       └── predicates.cljc
└── test
    └── valip
        └── test
            ├── core.cljc
            └── predicates.cljc

6 directories, 9 files
```

> NOTE 1: after I wrote the [previous tutorial][1], I decided to move
> and rename the `def.clj` file containing the `valip` macros to a more
> conventional place (i.e., in the same directory of the other source
> files) and with a more explicit name (i.e., `macros.clj`). You could
> do the same thing as a very simple exercise).

This is because we did not chain the `target` task after the `pom`
one. Before the `2.5.5.` release, `boot` did not have a `target` task.
The default behavior was to automatically write to an implicit
`target` directory, which is not the case anymore when you set, as we
did in the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#enter-boot),
the `BOOT_EMIT_TARGET` environment to `no` in the `boot.properties`
file of the `valip` project.

## The `show` task

In `boot`, the local installation process doesn't need to create a
target directory in your project directory to succeed, but you can use
the `show` task to see what's going on in the `boot`'s
[fileset](https://github.com/boot-clj/boot/wiki/Filesets) before and
after the execution of any `boot` task.

Let's read the `show` docstring:

```bash
boot show -h
Print project/build info (e.g. dependency graph, etc).

Options:
  -h, --help              Print this help info.
  -C, --fake-classpath    Print the project's fake classpath.
  -c, --classpath         Print the project's full classpath.
  -d, --deps              Print project dependency graph.
  -e, --env               Print the boot env map.
  -f, --fileset           Print the build fileset object.
  -l, --list-pods         Print the names of all active pods.
  -p, --pedantic          Print graph of dependency conflicts.
  -P, --pods REGEX        Set the name filter used to select which pods to inspect to REGEX.
  -U, --update-snapshots  Include snapshot versions in updates searches.
  -u, --updates           Print newer releases of outdated dependencies.
```

Wow, this is a very handy task to be used indeed. Let's play at the
command line by chaining it before and after the `pom` task and by
passing to it the `-f` option flag:

```bash
boot show -f pom -p org.clojars.magomimmo/valip -v 0.4.0-SNAPSHOT show -f
valip
├── core.cljc
├── macros.clj
└── predicates.cljc
Writing pom.xml and pom.properties...
META-INF
└── maven
    └── org.clojars.magomimmo
        └── valip
            ├── pom.properties
            └── pom.xml
valip
├── core.cljc
├── macros.clj
└── predicates.cljc
```

Initially, the `fileset` included only the `valip` source code
files. Then, the `pom` task added to it the `META-INF` directory
containing what's needed to later package the project into a `jar`
file.

## The `jar` task

To package `valip` library in a `jar` you use, and as it could be
different, the `jar` task. Let's read its docstring:

```bash
boot jar -h
Build a jar file for the project.

Options:
  -h, --help              Print this help info.
  -f, --file PATH         Set the target jar file name to PATH.
  -M, --manifest KEY=VAL  Conj [KEY VAL] onto the jar manifest map.
  -m, --main MAIN         Set the namespace containing the -main function to MAIN.
```

At the moment we're not interested in any of its command line
options. Even if the `jar`'s help does not tell you, if you do not
specify a file name with the `-f` option, the `jar` task will use the
[concatenation](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj#L629)
of the project's `artifactId` and `version` as
[default name](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj#L630).

Let's try it:

```bash
boot pom -p org.clojars.magomimmo/valip -v 0.4.0-SNAPSHOT jar show -f
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
META-INF
└── maven
    └── org.clojars.magomimmo
        └── valip
            ├── pom.properties
            └── pom.xml
valip
├── core.cljc
├── macros.clj
└── predicates.cljc
valip-0.4.0-SNAPSHOT.jar
```

Do you see the `valip-0.4.0-SNAPSHOT.jar` file shown by the `show -f`
task?

## The `install` task

We are almost done.  `install` is the next built-in task to be chained
for installing the `valip` library in the `maven` local
repository. Let's view its docstring:

```bash
boot install -h
Install project jar to local Maven repository.

The --file option allows installation of arbitrary jar files. If no
file option is given then any jar artifacts created during the build
will be installed.

The pom.xml file that's required when installing a jar can usually be
found in the jar itself. However, sometimes a jar might contain more
than one pom.xml file or may not contain one at all.

The --pom option can be used in these situations to specify which
pom.xml file to use. The optarg denotes either the path to a pom.xml
file in the filesystem or a subdir of the META-INF/maven/ dir in which
the pom.xml contained in the jar resides.

Example:

  Given a jar file (warp-0.1.0.jar) with the following contents:

      .
      ├── META-INF
      │   ├── MANIFEST.MF
      │   └── maven
      │       └── tailrecursion
      │           └── warp
      │               ├── pom.properties
      │               └── pom.xml
      └── tailrecursion
          └── warp.clj

  The jar could be installed with the following boot command:

      $ boot install -f warp-0.1.0.jar -p tailrecursion/warp

Options:
  -h, --help       Print this help info.
  -f, --file PATH  Set the jar file to install to PATH.
  -p, --pom PATH   Set the pom.xml file to use to PATH.
```

This help is mostly about corner cases, which it's not our
scenario. We'll stay with the default behavior, without passing any
option to the `install` task.

Before going, be sure to delete `valip` from your local maven
repository:

```bash
rm -rf ~/.m2/repository/org/clojars/<your_github_name>/valip/
```

Then chain the `install` task as follows

```bash
boot pom -p org.clojars.<your_github_name>/valip -v 0.4.0-SNAPSHOT jar install
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
```

and see its side-effects in your local maven repository:

```bash
tree ~/.m2/repository/org/clojars/<your_github_name>/valip
├── 0.4.0-SNAPSHOT
│   ├── _maven.repositories
│   ├── maven-metadata-local.xml
│   ├── valip-0.4.0-SNAPSHOT.jar
│   └── valip-0.4.0-SNAPSHOT.pom
└── maven-metadata-local.xml

1 directory, 5 files
```

Also verify that the `valip` directory is still clean:

```bash
tree
.
├── README.md
├── boot.properties
├── build.boot
├── project.clj
├── src
│   └── valip
│       ├── core.cljc
│       ├── macros.clj
│       └── predicates.cljc
└── test
    └── valip
        └── test
            ├── core.cljc
            └── predicates.cljc

5 directories, 9 files
```

## Task Options

The previous paragraphs explained the local installation of `valip`
step by step, but it is not something you'd like to repeat again and
again after any change in the project.

From `boot` you would expect something like the `lein install` command
we used in the [previous tutorial][1]: enter
`task-options!`. `task-options!` allows to add any task option in the
`build.boot` build file, just after the `set-env!` form, if you're
only using built-in tasks, and after the `require` form, when you're
using other tasks for which you may want to set some options within
the `task-options!` form itself. Here it's the complete revisited
version of the `build.boot` build file for the `valip` project:

```clj
(set-env!
 :source-paths #{"src"}
 
 :dependencies '[
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [adzerk/boot-test "1.1.0"]
                 [adzerk/boot-cljs "1.7.170-3"]
                 [crisptrutski/boot-cljs-test "0.2.1"]
                 ])

(require '[adzerk.boot-test :refer [test]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]]
         )

(task-options!
 pom {:project 'org.clojars.magomimmo/valip
      :version "0.4.0-SNAPSHOT"
      :description "Functional validation library for Clojure and ClojureScript. 
                    Forked from https://github.com/cemerick/valip"
      :url "http://github.com/magomimmo/valip"
      :scm {:url "http://github.com/magomimmo/valip"}
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 test {:namespaces #{'valip.test.core 'valip.test.predicates}}
 test-cljs {:namespaces #{'valip.test.core 'valip.test.predicates}})

(deftask testing
  []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask tdd
  "Launch a CLJ TDD Environment"
  []
  (comp
   (testing)
   (watch)
   (test-cljs)
   (test)))
```

Note as we moved the `test` and the `test-cljs` option arguments into
the `task-options!` form as well. While at it, we also exploit the
opportunity to enrich the `pom` task with other information, even the
ones, like `:description`, `:url`, `:scm` and `:license` that do no
make part of a minimal POM.

## Shoot the gun

We can now run the `tdd` task to verify that everything is still
working:

```bash
boot tdd

Starting file watcher (CTRL-C to quit)...

Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 91 assertions.
0 failures, 0 errors.

Testing valip.test.core

Testing valip.test.predicates

Ran 21 tests containing 97 assertions.
0 failures, 0 errors.
Elapsed time: 15.959 sec
```

It worked. Now stop the running `boot` process and reinstall `valip`
in the local maven repository as follows:

```bash
boot pom jar install
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
```

Verify that the installation process succeeded:

```bash
tree ~/.m2/repository/org/clojars/<your_github_name/valip/
├── 0.4.0-SNAPSHOT
│   ├── _maven.repositories
│   ├── maven-metadata-local.xml
│   ├── valip-0.4.0-SNAPSHOT.jar
│   └── valip-0.4.0-SNAPSHOT.pom
└── maven-metadata-local.xml

1 directory, 5 files
```

Nice job. If you think you're done, you're **wrong!**.  You have at
least to test the local installation of `valip` lib in the context of
a project, as we already did in the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#test-valip-in-a-project-context)
after having used the `lein install` command.

## Test valip in a project context

First, if not already done, clone the `modern-cljs` project in a
temporary directory and checkout the `se-tutorial-18` branch as
follows:

```bash
cd /path/to/tmp
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
```

Then edit the `build.boot` file by substituting the `valip` dependency
with the newly updated one:

```clj
(set-env!
 ...
 
 :dependencies '[
                 ...
                 [org.clojars.<your_github_name>/valip "0.4.0-SNAPSHOT"]
                 ...
                 ])
```

## Start TDD

Start the TDD environment

```bash
boot tdd
Starting reload server on ws://localhost:60606
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2016-01-10 14:12:42.402:INFO::clojure-agent-send-off-pool-0: Logging initialized @10813ms
             clojure.lang.ExceptionInfo: java.io.FileNotFoundException: Could not locate valip/core__init.class or valip/core.clj on classpath., compiling:(modern_cljs/login/validators.cljc:1:1)
    data: {:file
           "/var/folders/17/1jg3ghkx73q4jtgw4z500www0000gp/T/boot.user2098590056263435211.clj",
           :line 31}
java.util.concurrent.ExecutionException: java.io.FileNotFoundException: Could not locate valip/core__init.class or valip/core.clj on classpath., compiling:(modern_cljs/login/validators.cljc:1:1)
clojure.lang.Compiler$CompilerException: java.io.FileNotFoundException: Could not locate valip/core__init.class or valip/core.clj on classpath., compiling:(modern_cljs/login/validators.cljc:1:1)
          java.io.FileNotFoundException: Could not locate valip/core__init.class or valip/core.clj on classpath.
...
Elapsed time: 26.573 sec
```

Argh. This is not so lovely! What's is happening here? It seems that
the `boot tdd` task is not able to find the source files of the
`valip` library in the `classpath`.

Perhaps it's time to take a look at the content of the generated `jar`
file installed by `boot` in the local maven repository:

```bash
jar -tvf ~/.m2/repository/org/clojars/magomimmo/valip/0.4.0-SNAPSHOT/valip-0.4.0-SNAPSHOT.jar
     0 Sun Jan 10 14:12:26 CET 2016 META-INF/
     0 Sun Jan 10 14:12:26 CET 2016 META-INF/maven/
     0 Sun Jan 10 14:12:26 CET 2016 META-INF/maven/org.clojars.magomimmo/
     0 Sun Jan 10 14:12:26 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/
   158 Sun Jan 10 14:12:26 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/pom.properties
  1930 Sun Jan 10 14:12:26 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/pom.xml
    25 Sun Jan 10 14:12:26 CET 2016 META-INF/MANIFEST.MF
```

That's very bad. The locally installed `jar` package for the `valip`
library does not contain any `valip` source files. How this could
happen? *The answer, my friend, is blowin' in the* `boot`, namely in
its
[wiki](https://github.com/boot-clj/boot/wiki/Boot-Environment#env-keys):

* `:resource-paths`: A set of path strings. These paths will be on the
  classpath and **the files contained will be emitted as final
  artifacts**;
* `:source-paths`: A set of path strings. These paths will be on the
  classpath but **the files contained will not be emitted as final
  artifacts**.

So, even if the `:source-paths` set of directories will be on the
`classpath` of the project, as we already know from the `valip`
library to be able to be compiled and test, the contained files will
not be emitted in the final artifacts, as we just discovered by
listing the content of the **emitted final artifact**, i.e., the `jar
file.

On the contrary, the files contained in the set of directories of the
`:resource-paths` will be emitted in the final `jar` file as well.

So, the easiest solution seems to be to set the `:resource-paths` in
the `set-env!` form as follows:

```clj
(set-env!
 :source-paths #{"src"}
 :resource-paths #{"src"}
 
 :dependencies '[...])
 ```

Let's try:

```bash
cd /path/to/valip
boot pom jar install
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
```

Now list the content of the generated `jar` package file again

```bash
jar -tvf ~/.m2/repository/org/clojars/magomimmo/valip/0.4.0-SNAPSHOT/valip-0.4.0-SNAPSHOT.jar
     0 Sun Jan 10 15:25:14 CET 2016 valip/
   995 Sun Jan 10 10:02:55 CET 2016 valip/core.cljc
  1027 Sun Jan 10 10:02:55 CET 2016 valip/macros.clj
  4944 Sun Jan 10 10:02:55 CET 2016 valip/predicates.cljc
     0 Sun Jan 10 15:25:14 CET 2016 META-INF/
     0 Sun Jan 10 15:25:14 CET 2016 META-INF/maven/
     0 Sun Jan 10 15:25:14 CET 2016 META-INF/maven/org.clojars.magomimmo/
     0 Sun Jan 10 15:25:14 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/
   158 Sun Jan 10 15:25:14 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/pom.properties
  1930 Sun Jan 10 15:25:14 CET 2016 META-INF/maven/org.clojars.magomimmo/valip/pom.xml
    25 Sun Jan 10 15:25:14 CET 2016 META-INF/MANIFEST.MF
```

That's much better. Let now run again the `modern-cljs` project to see
the result:

```bash
cd /path/to/modern-cljs
boot tdd
...
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 57 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 4 tests containing 58 assertions.
0 failures, 0 errors.
Writing target dir(s)...
Elapsed time: 28.470 sec
```

Now we talk. Just to be sure that everything is still working as for
the end of the [Tutorial 18][2], visit the
[Shopping Calculator](http://localhost:3000/shopping.html) URL to play
with the Shopping Calculator.

You can now stop the `boot` process and go back to `valip`, because
there is something new to learn before deploy `valip` to the `clojars`
repository for making it available to anybody else.

## Semantic versioning

I bet that anyone knows about the `-SNAPSHOT` qualifier used as
postfix in the `valip` artifactId name: it is considered an
incremental version (or "as-yet-unreleased"). Under the wood, `maven`,
on which are based both `boot` and `leiningen` build tools, will fetch
the most recently deployed SNAPSHOT version. Even if this behavior
slows down the build process, in a continuous integration scenario it
guarantees up-to-date builds, while minimizing the amount of
rebuilding that is required for each integration step.

You certainly noted that, when we made the `valip` library compliant
with the Reader Conditional extension, we changed its version
identifier from `"0.3.2"` to `"0.4.0-SNAPSHOT"`. The reason why there
are so many `0.x.y` versioned libraries around it's because until
their APIs get stable, they never get versioned with a major version
number. In the mean time, as dictated by the
[The Semantic Version Specification](http://semver.org/), anything
could change. By considering that a lot of open source libraries stand
as unstable for a long time, their minor version number is generally
treated as major numbers, meaning that minor version number increments
do not guarantee any backward compatibility, as they were major number
increment. This is why we incremented the minor version only and not
the major version as well, even if our version will not be backward
compatible with the one from Chas Emerick.

## What's next

Let's summarize what we already did until now starting from the
previous tutorial:

* we forked and cloned the `valip` library by Chas Emerick;
* we added the remote upstream repo;
* we created the `reader-conditionals` branch;
* we modified the leiningen `project.clj` build file to update the CLJ
  dependency from `1.4.0` to `1.7.0`;
* we made substantial changes to the `valip` source and test code to make it
  compatible with the Reader Conditional extension of CLJ/CLJS
  compilers and to introduce few corner cases tests;
* we increment the minor-version only of the library, because, even if
  they will break any preexisting use of the `valip` library, its
  major version is still `0`;
* we qualified the new minor version as SNAPSHOT, because it's in
  as-yet-unreleased phase;
* we bootify `valip` by creating the corresponding `build.boot` build
  file and the `boot.properties` file to pin the project to the
  `2.5.5` release of `boot`;
* we tested the `valip` library in the context of the `modern-cljs`
  project by installing it into the local maven repository;

In a real world scenario, we should now submit a pull request to the
upstream `valip` repository and wait until the owner would eventually
accept and merge our pull request.

But what if the owner is lazy or for any reason she/he does not agree
to merge our pull requests?

The above modified repo only lives locally on your computer and it
can't be directly shared with other developers or even with other
computers.

You have more options:

* you can publish the updated library in the clojars public
  repository. This way the library will be available to everybody;
* you can publish a lib on a private repository. This way the
  accessibility to the updated `valip` library is governed by the
  rules defined in the repository itself. Generally this is the right
  choice when you want to make a library available to other devs without
  making it public.

In the next part of this tutorial we're going to inspect the first
option only.

## A Survival guide

The process of publishing a CLJ/CLJS lib on clojars is pretty simple.
As already said, keep in mind that any release ending in "-SNAPSHOT"
is not an official release and you should rely on them only when you
really need (which is not our fictional scenario). Also remember that
by adding a SNAPSHOT dependency to your project, you will cause any
build tool to slow down its dependencies search.

Clojars offers two repositories, [Classic]() and [Releases](). The Classic
repository, which is the one we're going to use, has no restrictions
and anyone may publish a lib into it.

That said, if you want to push your own version of somebody else's
library, which is our case, do not use the original `groupId`: use
`org.clojars.<your_github_name>`, exactly as we did with the `valip`
library.

We can move to the next step.

## Register and publish on clojars.org

To publish a library on clojars you first need to
[register](https://clojars.org/register) with it and you're almost
ready to publish the `valip` SNAPSHOT on clojars.

But first there is another very handy `boot` task to be used:
[`bootlaces`](https://github.com/adzerk-oss/bootlaces) and it's aimed
at simplifying the creation and publication of `jar` files.



## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md
