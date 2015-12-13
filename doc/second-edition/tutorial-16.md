# Tutorial 16 - Test Driven Development

In the [previous tutorial][1] we afforded the problem of freezing in a
single JVM a bunch of `boot` tasks supporting the convergence of the
Bret Victor's Immediate Feedback Principle with the Test Driven
Development (TDD) workflow. Even if we were able to find our way in
approaching that objective, we ended up with few questions about the
`tdd` task we defined: could we define the `tdd` task to make it
parameterizable while keeping some sane defaults for its standard use?

In this tutorial we're going to answer that question by introducing
the `boot`'s
[Task Options Domain Specific Language](https://github.com/boot-clj/boot/wiki/Task-Options-DSL).

## Preamble

To start working from the end of the previous tutorial, assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-15
```

## Introduction

Even if I never started a new program from a test failure, that has
more to do with my age that with my opinion about TDD. That said, the
workflow induced from the `tdd` task we ended up in the previous
tutorial would be insulted by any TDD practitioner.  We'd like to
please them a little bit better than that.

## Task Options DSL

As you can read from the Wiki page cited above, `boot` is a servant of
more owners and it has to please them all:

> `boot` tasks are intended to be used from the command line, from a
> REPL, in the project's build.boot file, or from regular Clojure
> namespaces. This requires support for two conceptually different
> calling conventions: with command line options and optargs, and as
> s-expressions in Clojure with argument values.
>
> Furthermore, since tasks are boot's user interface it's important
> that they provide good usage and help documentation in both
> environments.

[Command-Line options and optargs](http://www.catb.org/esr/writings/taoup/html/ch10s05.html)
are Unix terms that any programmer should know about, while
s-expression is a term that is specific for the LISPs'
communities. But we don't want to bother you with long explanations
about those things. Let's be pragmatic and directly afford our needs.

## Short requirements

At the moment in our project we only have the `test/cljc` test
directory, but as soon as the project gets more complicated we'll
probably have to add more testing namespaces specifically dedicated
CLJ or CLJS.

Even if you can name your testing namespaces as you want and place
them wherever you like, it's considered a good practices to keep the
testing namespaces specific for CLJ separated from the ones specific
for CLJS and, in turns, keep both of them separated from testing
namespaces which are portable on CLJ and CLJS as well.

At some point in time you'll probably end up with a directory layout
like the following:

```bash
test/
├── clj
├── cljc
└── cljs
```

that mimics the same directory layout for the source code

```bash
src/
├── clj
├── cljc
└── cljs
```

## Be prepared for changes

To be prepared for any testing scenario, while keeping the more
frequent one as simple as possible (i.e., `boot tdd`), we need to
approach the definitions of our tasks from top to bottom.

For example, we would like to be able to call the `tdd` task by
passing to it one or more test directories to be added to the
`classpath` of the project. Something like that:

```bash
boot tdd -t test/cljc
```
or like this

```bash
boot tdd -t test/clj -t test/cljs -t test/cljc
```

I know, you'd prefer something like the following form

```bash
boot tdd -t test/clj:test/cljs:test/cljc
```

Task options DSL offers direct support for the former mode, named
[multiple options](https://github.com/boot-clj/boot/wiki/Task-Options-DSL#multi-options). If
you prefer (like I do) the `colon` separated directories mode, you
have to parse it by yourself.

## A dummy task

Let's temporarily create a dummy task in the `build.boot` to see how
all that could works:

```clj
(deftask dummy
  "A dummy task"
  [t dirs PATH #{str} ":source-paths"]
  *opts*)
```

Here we're using the task options inside the arguments vector of the
task definition.

```clj
[t dirs PATH #{str} ":source-path"]
```

* `t`: is the shortname
* `dirs`: is the longname
* `PATH`: is the optarg, if provided, indicates that the option expects
  a value
* #{str}: is the CLJ type hint for the option value 
* `":source-path"`: is the description that will be incorporated into
  command line help output

The `dummy` task does not do anything and of you call it at the
terminal, it does not return anything interesting:

```bash
boot dummy
boot dummy -t test/cljc
boot dummy -t test/cljc -t test/cljs
boot dummy -t test/cljc -t test/cljs -t test/clj
```

But if you not pass an argument to the `-t` options, it will return an
error, because the optarg `PATH` has been provided in the definition.

```bash
boot dummy -t
             clojure.lang.ExceptionInfo: java.lang.IllegalArgumentException: Missing required argument for "-t PATH"
    data: {:file
           "/var/folders/17/1jg3ghkx73q4jtgw4z500www0000gp/T/boot.user2289187480377868390.clj",
           :line 29}
java.util.concurrent.ExecutionException: java.lang.IllegalArgumentException: Missing required argument for "-t PATH"
     java.lang.IllegalArgumentException: Missing required argument for "-t PATH"
          boot.core/construct-tasks  core.clj:  682
                                ...
                 clojure.core/apply  core.clj:  630
                  boot.core/boot/fn  core.clj:  712
clojure.core/binding-conveyor-fn/fn  core.clj: 1916
                                ...
```

We can even ask for the help documentation:

```bash
boot dummy -h
A dummy task

Options:
  -h, --help       Print this help info.
  -t, --dirs PATH  Conj PATH onto :source-paths
```

Let's now call the `dummy` task inside the CLJ REPL:

```bash
cd /path/to/modern-cljs
boot repl
```


### Update build.boot

We know enough to update the `testing` task and even to change its
name to `add-paths` by considering that all it does is to add one or
more directories to the `:source-paths` environment variable.

```clj
(deftask add-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (set-env! :source-paths #(into % dirs))
  identity)
```

Substitute the newly defined `add-paths` task to the previous
`testing` task in the `tdd` task definition and finally start it at
the terminal as usual:

```clj
(deftask tdd
  "Launch a TDD Environment"
  []
  (comp
   (serve :dir "target"
          :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (add-paths :dirs #{"test/cljc"})
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env :phantom 
              :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})))
```

```bash
boot tdd
...
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 24.912 sec
```clj

Everything is still working as expected. 

Now that we know how to parameterize a task by using the task options
DSL (Domain Specific Language), we'd like to apply this knowledge to
the `tdd` task as well.


Stay tuned for the next tutorial.

# Next Step - Tutorial 17 - TBD 


Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.


