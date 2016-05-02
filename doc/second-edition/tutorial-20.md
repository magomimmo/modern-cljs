# Tutorial 20 - House Keeping

In the [previous tutorial][1] we explained how to make a
library compliant with 
[Reader Conditionals](http://clojure.org/reference/reader#_reader_conditionals). As
an example, we exploited the
[`valip`](https://github.com/magomimmo/valip) library that we already
used in the `modern-cljs` project for validating the input fields of
the Login and Shopping forms we adopted as CLJ/CLJS playground.

Even if we reached a decent result, we left some work to be done,
namely the deployment of the updated library to
[clojars](https://clojars.org/), the notorious community repository
for open source Clojure libraries.

## Preamble

Unless you completed the [previous tutorial][1] and finally committed
your work in the `reader-conditionals` branch of the `valip` project
you created while following the tutorial itself, to be able to work on
this new tutorial, you need to do as follows:

```bash
git clone https://github.com/magomimmo/valip.git
cd valip
git checkout se-tutorial-19
git checkout -b reader-conditionals
```

## Introduction

In this tutorial we're going to fill that gap. But first we have to
digress on a couple of topics we left uncovered about the `boot` build
tool, because they constitute a prerequisite to the `clojars`
deployment itself, namely:

* [POM](https://maven.apache.org/pom.html#Introduction) (Project
  Object Model)
* [Dependency Scope](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)

## Minimal POM (Project Object Model)

To quickly finish the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#locally-install-valip),
we switched to `leiningen` and locally installed the updated version
of `valip` by using the `lein install` task. This was because the
`project.clj` build file used by `leiningen` already had the
[minimal information](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html#Minimal_POM)
needed to create and package the `valip` library, while we had not
updated the corresponding `build.boot` build file used by `boot` with
the same information, namely:

* the `groupId:` it has to follow the java package name rules; e.g.,
  `org.clojars.magomimmo`;
* the `artifactId`: it is the name of the `jar` file without version;
  e.g., `valip`;
* the `version`: it is suggested to follow the
  [Semantic Version Specification](http://semver.org/); e.g.,
  "0.4.0-SNAPSHOT"

    > NOTE 1: `-SNAPSHOT` qualifies a version
    > "as-yet-unreleased". Under the hood, `maven`, on which both
    > `leiningen` and `boot` are based, will fetch the most recently
    > deployed `-SNAPSHOT` version. Even if this behavior slows down
    > the build process, in a continuous integration scenario it
    > guarantees up-to-date builds, while minimizing the amount of
    > rebuilding that is required for each integration step.

In `boot`, to create a project's POM, package it in a `jar` and
finally install it in the local `maven` repository on your machine,
you can chain the following three
[built-in](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj)
`boot` tasks:

* `pom`
* `jar`
* `install`

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

As you see, to create a minimal `pom.xml` file for a project you need
to specify at least the `-p` and the `-v` command line options. Before
testing the `pom` task, let's first get rid of the stuff generated in
the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#locally-install-valip),
when we ran the `lein install` command:

```bash
# delete files generated in the project directory
cd /path/to/valip
lein clean
rm -rf pom.xml
# delete the jar file from the local maven repository
rm -rf ~/.m2/repository/org/clojars/magomimmo/valip
```

Now launch the `pom` task at the command line, by providing the minimal
information needed to generate the `pom.xml` file:

```bash
boot pom -p org.clojars.magomimmo/valip -v 0.4.0-SNAPSHOT
Writing pom.xml and pom.properties...
```

> NOTE 2: remember to substitute `magomimmo` with your github name as
> `groupId`

Even if the command reported the writing of the `pom.xml` and the
`pom.properties` files, which is a misleading message to me, if you
take a look at the project directory you won't see them:

```bash
tree
.
├── README.md
├── boot.properties
├── build.boot
├── pom.xml
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
```

> NOTE 3: after I wrote the [previous tutorial][1], I decided to move
> the `def.clj` file containing the `valip` macros to a more
> conventional place and rename it as `macros.clj`. You could do the
> same thing as a very simple exercise.

This is because we did not chain the `target` task after the `pom`
one. Before the `2.5.5.` release, `boot` did not have a `target` task.
The default behavior was to automatically write to an implicit
`target` directory, which is not the case anymore when you set, as we
did in the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#enter-boot),
the `BOOT_EMIT_TARGET` environment to `no` in the `boot.properties`
file of the `valip` project.

## The `show` task

In `boot`, the local installation process of a project doesn't need to
create a target directory in your project directory to succeed. If you
want to see what is going on in the `boot`'s
[fileset](https://github.com/boot-clj/boot/wiki/Filesets) before and
after the execution of any `boot` task, you can use the `show`
built-in task:

Let's view the docstring for the `show` task:

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

Wow, this is a very handy task indeed. Let's play on the
command line by chaining it before and after the `pom` task as
follows:

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

Initially, the `fileset` includes the `valip` source code files
only. Then, the `pom` task adds the `META-INF` directory containing
what later will be needed to package the project into a `jar` file.

## The `jar` task

Let's now view the `jar` docstring:

```bash
boot jar -h
Build a jar file for the project.

Options:
  -h, --help              Print this help info.
  -f, --file PATH         Set the target jar file name to PATH.
  -M, --manifest KEY=VAL  Conj [KEY VAL] onto the jar manifest map.
  -m, --main MAIN         Set the namespace containing the -main function to MAIN.
```

At the moment, we're not interested in any of its command line
options. The `jar`'s help does not tell you, but if you do not specify
a file name with the `-f` option, the `jar` task will
[concatenate](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj#L629)
the project's `artifactId` and `version` as
[default name](https://github.com/boot-clj/boot/blob/master/boot/core/src/boot/task/built_in.clj#L630),
which is what we want.

Try it:

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

We are almost done. `install` is the next built-in task to be chained
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

This help is mostly about corner cases which are not in not our
scenario. We'll stay with the default behavior, without passing any
options to the `install` task:

```bash
boot pom -p org.clojars.<your_github_name>/valip -v 0.4.0-SNAPSHOT jar install
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
```

Now take a look at your local `maven` repository:

```bash
tree ~/.m2/repository/org/clojars/<your_github_name>/valip
├── 0.4.0-SNAPSHOT
│   ├── _maven.repositories
│   ├── maven-metadata-local.xml
│   ├── valip-0.4.0-SNAPSHOT.jar
│   └── valip-0.4.0-SNAPSHOT.pom
└── maven-metadata-local.xml
```

It is worth nothing that the `valip` directory is still clean, because
we did not chain any `target` task:

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
```

## Task Options

The previous paragraphs explained the local installation of `valip`
step by step, but it is not something you'd like to repeat again and
again after any project change.

From a build tool such as `boot` you would expect something like the `lein
install` command we used in the [previous tutorial][1]: enter
`task-options!`.

`task-options!` allows us to add any task option to the `build.boot`
build file.  If you're only using built-in tasks, you can place it
just after the `set-env!` form. If you are using other tasks, you'll
place `task-options!` after the requirement form. Following is the
complete updated version of the `build.boot` build file for the
`valip` project:

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [adzerk/boot-test "1.1.0"]
                 [adzerk/boot-cljs "1.7.170-3"]
                 [crisptrutski/boot-cljs-test "0.2.1"]])

(require '[adzerk.boot-test :refer [test]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

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

> NOTE 4: we moved the `test` and the `test-cljs` option arguments
> into the `task-options!` form as well.

> NOTE 5: we enriched the `pom` task with more information, including optional
> fields like `:description`, `:url`, `:scm` and `:license` that are not
> part of a minimal POM required for the project to be packaged and
> installed.

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

It worked. Now stop the running `boot` process and try to reinstall
`valip` in the local `maven` repository by chaining the three cited
tasks as follows:

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

Nice job, but if you now think we're done, you're **wrong!** At least,
we have to test the local installation of the `valip` library in the
context of a project, as we already did in the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md#test-valip-in-a-project-context).

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

Then edit its `build.boot` file by substituting the `valip` dependency
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

Next, start the [TDD][3] environment as usual:

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

Previously, we should have taken a look at the content of the
generated `jar` file installed by `boot` in the local repository,
instead of quickly proceeding with the `valip` installation:

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
happen? *The answer, my friend, is blowin' in the wind*, namely in the
`boot`
[wiki](https://github.com/boot-clj/boot/wiki/Boot-Environment#env-keys):

* `:resource-paths`: A set of path strings. These paths will be on the
  classpath and **the files contained will be emitted as final
  artifacts**;
* `:source-paths`: A set of path strings. These paths will be on the
  classpath but **the files contained will not be emitted as final
  artifacts**.

Ahah! Even if the `:source-paths` set of directories will be on the
`classpath` of the project, as we already knew from the fact that
`valip` was able to be compiled and tested, the contained source files
would never be emitted in the final artifacts, as we just discovered
by listing the content of the generated `jar` file.

On the contrary, the files contained in the set of directories of the
`:resource-paths` would be emitted in the final `jar` file.

## `install-jar` task

Now, go back to `valip`'s `build.boot` build file. By just setting
the `:resource-paths` environment variable with the same `#{"src"}`
value of the `:source-paths` one, we should be able to solve the
problem of including `valip`'s source files into the `jar`. While
we are at it, let's define a new `install-jar` task, which first sets
the `:resource-paths` environment variable and then composes the
`pom`, `jar` and `install` built-in tasks:

```clj
;; append at the end of `build.boot`
(deftask install-jar
  []
  (merge-env! :resource-paths #{"src"})
  (comp
   (pom)
   (jar)
   (install)))
```

Let's see if it works:

```bash
cd /path/to/valip
boot install-jar
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

That's much better. Run the `modern-cljs` again project to see the
result:

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

Now we are talking. Just to be sure that everything is still working
as at the end of the
[Tutorial 18](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md#on-improving-ux-user-experience),
visit the [Shopping Calculator](http://localhost:3000/shopping.html)
URL to play with the Shopping Calculator.

You can now stop the `boot` process and go back again to the `valip`
project directory, because there is something new to learn before
deploying the `valip` library to `clojars` and making it available to
anybody else.

## Artifact Versioning

When we made the `valip` library compliant with the Reader
Conditionals extension, we changed its version identifier from
`"0.3.2"` to `"0.4.0-SNAPSHOT"`. The reason why there are so many
`0.x.y` versioned libraries around, it's because until their APIs get
stable, they never get versioned with a major version number. In the
mean time, as dictated by the
[The Semantic Version Specification](http://semver.org/), anything
could change. By considering that a lot of open source libraries stay
as unstable for a long time, their minor version number is generally
treated as major version number, meaning that minor version number
increments do not guarantee any backward compatibility. This is why we
incremented the minor version only, and not the major version as well,
even if our `valip` version is not backward compatible with the
`0.3.2`

## What's next

Let's first summarize what we already did with `valip`:

* we forked and cloned the `valip` library by Chas Emerick;
* we added the remote upstream repo;
* we created the `reader-conditionals` branch;
* we modified the leiningen `project.clj` build file to update the CLJ
  dependency from `1.4.0` to `1.7.0`;
* we made substantial changes to the `valip` source and test files to
  make it compatible with the Reader Conditionals extension of
  CLJ/CLJS compilers and to introduce few corner cases tests;
* we incremented the minor-version only of the library, because, even if
  it would break any preexisting use of the `valip` library, its major
  version is still `0`;
* we qualified the new minor version as SNAPSHOT, because it's
  as-yet-unreleased;
* we bootified `valip` by creating the corresponding `build.boot` build
  file and the `boot.properties` file to pin the project to the
  `2.5.5` release of `boot`;
* we tested the `valip` library in the context of the `modern-cljs`
  project by installing it into the local maven repository.

In a real world scenario, the next step would be to submit a pull
request to the upstream `valip` repository, and wait until the owner
of the repo would eventually accept and merge our pull request.

But what if the owner is lazy or for any reason she/he does not agree
to merge our pull requests?

Currently, the modified `valip` library lives on your computer only,
and it can't be directly shared with other developers or even with
other computers.

You have a couple of options:

* you can publish the updated library to clojars public
  repository. This way the library will be available to everybody;
* you can publish a lib on a private repository. This way the
  accessibility to the updated `valip` library is governed by the
  rules defined in the repository itself. Generally, this is the right
  choice when you want to make a library available to other devs
  without making it public.

In the next part of this tutorial we're going to inspect the first
option only.

## A Quick Survival Guide

The process of publishing a CLJ/CLJS lib to clojars is pretty simple.
As already said, keep in mind that any release ending in "-SNAPSHOT"
is not an official release and you should rely on them only when you
really need (which it is not our fictional scenario). Also remember
that by adding a SNAPSHOT dependency to your project, you will cause
any build tool to slow down its dependencies search.

Clojars offers two repositories, Classic and Releases. The Classic
repository, which is the one we're going to use, has no restrictions
and anyone may publish a lib to it.

That said, if you want to push your own version of somebody else's
library, which is our case, do not use the original `groupId`: use
your personal `groupId`, e.g., `org.clojars.<your_github_name>`.

### `bootlaces` task

To publish a library to clojars's Classic Repository, you first need
to [register](https://clojars.org/register) with it and you're almost
ready. Actually, there is another very handy `boot` task to be used:
[`bootlaces`](https://github.com/adzerk-oss/bootlaces).

The `bootlaces` task is aimed at simplifying the typical workflow of
publishing a library to clojars.

Open the `build.boot` file to add the `bootlaces` task and to require
its main namespace as well:

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[...
                 [adzerk/bootlaces "0.1.13"]])

(require '...
         '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot]])
```

Now edit the `valip`'s `build.boot` file as suggested by `bootlaces`'s
[README.md](https://github.com/adzerk-oss/bootlaces#usage) file

```clj
(set-env! ...)

(require ...)

(def +version+ "0.4.0-SNAPSHOT")
(bootlaces! +version+)
```

and you're ready to go:

```bash
boot build-jar push-snapshot
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.
Username:
Password:
             clojure.lang.ExceptionInfo: java.lang.AssertionError: Assert failed: current git branch is reader-conditionals but must be master
                                         (or (not ensure-branch) (= b ensure-branch))
    data: {:file
           "/var/folders/8z/yj2xnrdj0hb5mswc1kfyjmw00000gn/T/boot.user8233124078905763746.clj",
           :line 33}
java.util.concurrent.ExecutionException: java.lang.AssertionError: Assert failed: current git branch is reader-conditionals but must be master
                                         (or (not ensure-branch) (= b ensure-branch))
               java.lang.AssertionError: Assert failed: current git branch is reader-conditionals but must be master
                                         (or (not ensure-branch) (= b ensure-branch))
     boot.task.built-in/fn/fn/fn/fn   built_in.clj:  782
  adzerk.bootlaces/eval715/fn/fn/fn  bootlaces.clj:   54
  adzerk.bootlaces/eval754/fn/fn/fn  bootlaces.clj:   62
     boot.task.built-in/fn/fn/fn/fn   built_in.clj:  716
     boot.task.built-in/fn/fn/fn/fn   built_in.clj:  618
     boot.task.built-in/fn/fn/fn/fn   built_in.clj:  342
                boot.core/run-tasks       core.clj:  794
                  boot.core/boot/fn       core.clj:  804
clojure.core/binding-conveyor-fn/fn       core.clj: 1916
                                ...
```

Uhm, not such a nice shot. After having required your clojars
credentials, `boot` complained about the fact that your current branch
is not the master. As default, `bootlaces` assumes that you only
publish a snapshot release from a master branch, but you can overwrite
the default with `task-options!`. Indeed, the `push-snapshot` task
internally uses the built-in `push` task:

```clj
(deftask push-snapshot
  "Deploy snapshot version to Clojars."
  [f file PATH str "The jar file to deploy."]
  (comp (collect-clojars-credentials)
        (push :file file :ensure-snapshot true)))
```

Now take a look at the `push` docstring and pay
attention on the `--ensure-*` options:

```bash
boot push -h
Deploy jar file to a Maven repository.

...
Options:
  ...
  -B, --ensure-branch BRANCH  Set the required current git branch to BRANCH.
  -C, --ensure-clean          Ensure that the project git repo is clean.
  -R, --ensure-release        Ensure that the current version is not a snapshot.
  -S, --ensure-snapshot       Ensure that the current version is a snapshot.
  -T, --ensure-tag TAG        Set the SHA1 of the commit the pom's scm tag must contain to TAG.
  -V, --ensure-version VER    Set the version the jar's pom must contain to VER.
```

Interesting, we can easily change the `push` task's behavior by just
setting the `:ensure-branch` options to `nil` in the `task-options!`
section we already used previosly to configure the `pom`, `test` and
`test-cljs` tasks:

```clj
(task-options!
 push {:ensure-branch nil}
 pom {...}
 test {...}
 test-cljs {...})
```

Shoot again:

```bash
boot build-jar push-snapshot
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.
Username:
Password:
             clojure.lang.ExceptionInfo: java.lang.AssertionError: Assert failed: project repo is not clean
                                         (or (not ensure-clean) clean?)
...
```

This time, the `push-snapshot` task complains about the fact that the git
branch is not clean. This is something that we should like, because
generally speaking you're not publishing something that is still to be
committed, right? So, let's be nice to ourselves and commit the work
we have done so far

```clj
cd /path/to/valip
got commit -am "prepare for publish to clojar"
```

and shoot the snapshot again:

```bash
boot build-jar push-snapshot
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.
Username:
Password:
Deploying valip-0.4.0-SNAPSHOT.jar...
```

## Real world check

Even if we already checked the Reader Conditionals compliant `valip`
library within the `modern-cljs` context, we want to be sure
`modern-cljs` is still working with the new snaphost release of
`valip` by downloading it from `clojars`, instead of using the one we
installed in the local `maven` repository of our machine. The
accomplishment of this assignment is very easy.

First, delete the installed `valip` form the local `maven` repository

```bash
rm -rf ~/.m2/repository/org/clojars/<your_github_name/valip
```

then re-run the `tdd` task from the `modern-cljs` project home
directory:

```bash
cd /path/to/modern-cljs
boot tdd
Retrieving valip-0.4.0-20160111.164819-6.jar from https://clojars.org/repo/
...
Elapsed time: 36.476 sec
```

Boom! Did you note the `Retrieving valip-....` notification? Now,
interactively test the usual
[Shopping Calculator](http://localhost:3000/shopping.html) to verify
that everything is still working. Then stop the `boot` process, but if
you think you're done, you're **wrong** again!

## Dependency scope

The very last topic of this tutorial has to do with dependency
management. `boot`, being based on `maven`, uses the same semantics as
`maven` when dealing with dependency scope. Take into account that
the dependency scope controls the dependency transitivity as well. In
`maven` there are [6 scopes available](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope):

* compile
* provided
* runtime
* test
* system
* import


I have to admit that I never saw more than a couple of them, namely
"test" and "provided", in the context of `boot` build files and even
less with `leiningen`, which offers
[`profiles` for such a thing](http://librelist.com/browser//leiningen/2014/9/7/clarifications-about-scope-in-dependencies/#32fcd639eb09c94545bec6a56c200167).

> NOTE 6: when you don't specify a `:scope`, `maven` assumes `"compile"`

### Test Scope

The test scope indicates that a dependency is only required for the
compilation and the test phases of the library itself and it's not
required to consume the library from another application.

Let's contextualize this concept within the `valip` library by
analyzing its dependencies starting from the ones catheterized by a
very clear role. All the `boot` tasks play a role in the building,
testing and publishing of a library, but they are not consumed by an
application using the library itself. We can safely say that all of
them should have the `:scope` set to `"test"`

### Provided Scope

But what about Clojure and ClojureScript compilers? Obviously, we need
them to compile `valip`. Any application consuming the `valip` source
code need to compile it as well, but it will provide those compiler by
itself. We can safely say that both the CLJ and the CLJS compilers should have the `:scope` set to `"provided"`.

It follows the entire `build.boot` build file filled with the
dependencies scope.

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[[org.clojure/clojure "1.7.0" :scope "provided"]
                 [org.clojure/clojurescript "1.7.228" :scope "provided"]
                 [adzerk/boot-test "1.1.0" :scope "test"]
                 [adzerk/boot-cljs "1.7.170-3" :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.1" :scope "test"]
                 [adzerk/bootlaces "0.1.13" :scope "test"]])

(require '[adzerk.boot-test :refer [test]]
         '[adzerk.boot-cljs :refer [cljs]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]]
         '[adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot]])

(def +version+ "0.4.0-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 push {:ensure-branch nil}
 pom {:project 'org.clojars.magomimmo/valip
      :version +version+
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

(deftask install-jar
  []
  (merge-env! :resource-paths #{"src"})
  (comp
   (pom)
   (jar)
   (install)))
```

You can now re-publish `valip` to `clojars`

```bash
boot build-jar push-snapshot
Writing pom.xml and pom.properties...
Writing valip-0.4.0-SNAPSHOT.jar...
Installing valip-0.4.0-SNAPSHOT.jar...
CLOJARS_USER and CLOJARS_PASS were not set; please enter your Clojars credentials.
Username:
Password:
Deploying valip-0.4.0-SNAPSHOT.jar...
```

That's all folks. Stay tune for the next tutorial.

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md
[3]: https://en.wikipedia.org/wiki/Test-driven_development
