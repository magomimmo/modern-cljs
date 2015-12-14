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

Even if I never started a new program from a failed test, that has
more to do with my age that with my opinion about TDD. That said, the
workflow induced from the `tdd` task we ended up in the previous
tutorial would be criticized by any TDD practitioner.  We'd like to
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
s-expression is a term that is specific for the LISP's
communities. But we don't want to bother you with long explanations
about those things. Let's be pragmatic and directly afford our needs.

## Short requirements

At the moment in our project we only have the `test/cljc` test
directory, but as soon as the project gets more complicated we'll
probably have to add more testing namespaces specifically dedicated to
CLJ or CLJS.

Even if you may call your testing namespaces as you want and place
them wherever you like, it's considered a good practices to keep any
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
frequent one as simple as possible (e.g., `boot tdd`), we need to
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

* `t`: is the shortname;
* `dirs`: is the longname;
* `PATH`: is the optarg. When provided, indicates that the option expects
  an argument to be passed at call time;
* #{str}: is the CLJ type hint for the passed argument; 
* `":source-path"`: is the description that will be incorporated into
  command line help output.

The `dummy` task does not do anything. If you call it at the terminal,
it does not return anything interesting:

```bash
boot dummy
boot dummy -t test/cljc
boot dummy -t test/cljc -t test/cljs
boot dummy -t test/cljc -t test/cljs -t test/clj
```

But if you do not pass an argument to the `-t` options, it will return an
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
...
boot.user=> (dummy "-t" "test/clj" "-t" "test/cljs" "-t" "test/cljc")
{:dirs #{"test/clj" "test/cljs" "test/cljc"}}
boot.user=> (dummy)
{}
boot.user=> (dummy "-t" "test/cljs")
{:dirs #{"test/cljs"}}
boot.user=> (dummy "-t" "test/clj" "-t" "test/cljs")
{:dirs #{"test/clj" "test/cljs"}}
boot.user=> (dummy "-t" "test/clj" "-t" "test/cljs" "-t" "test/cljc")
{:dirs #{"test/clj" "test/cljs" "test/cljc"}}
```

As you see the value assigned to the `:dirs` keyword in the `*opts*`
map is a set of string, exactly like the DSL `#{str}` type hint we
used in the `dummy` task definition. To proceed with the next step,
stop the `boot` process.

### Update build.boot

We know enough to start updating the `testing` task. Considering that
what `testing` really does it is to add paths to the `:source-paths`
environment variable, we'll change its name to `add-source-paths` as
well:

```clj
(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (set-env! :source-paths #(into % dirs))
  identity)
```

Boot's tasks are like [Ring][1] handlers, but instead of taking a
request map and returning a response map, they take and return a
[fileset object][2]. `add-source-paths` does not do anything
special. It only alters the `:source-paths` environment variable and
returns the identity function.

Now substitute the newly defined `add-source-paths` task to the
previous `testing` task in the `tdd` task definition:

```clj
(deftask tdd
  "Launch a TDD Environment"
  []
  (comp
   (serve :dir "target"
          :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (add-source-paths :dirs #{"test/cljc"})
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env :phantom 
              :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})))
```

But there is more thing to be done for pleasing a `tdd` user. We can
add the same task option to `tdd` as well, in such a way the she can
pass a test directory to be added to the `:source-paths` variable from
the command line:

```bash
(deftask tdd
  "Launch a customizable TDD Environment"
  [t dirs PATH #{str} "test paths"]
  (let [dirs (or dirs #{"test/cljc" "test/clj" "test/cljs"})] 
    (comp
     (serve :dir "target"                                
            :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true)
     (add-source-paths :dirs dirs)
     (watch)
     (reload)
     (cljs-repl)
     (test-cljs :out-file "main.js" 
                :js-env :phantom 
                :namespaces '#{modern-cljs.shopping.validators-test})
     (test :namespaces '#{modern-cljs.shopping.validators-test}))))
```

Note how we used the DSL to define the same `-t` option of the
`add-source-paths` task, with just a different description. Moreover,
we used the `let` and the `or` forms to give it a default value and
finally passed the evaluated value in the internal call to
`add-source-paths`. This is an idiomatic way to use task options.

Before starting the newly updated `tdd` task, let see its help

```bash
boot tdd -h
Launch a customizable TDD Environment

Options:
  -h, --help       Print this help info.
  -t, --dirs PATH  Conj PATH onto test paths
```

Now call it at the command line:

```bash
boot tdd -t test/cljc
...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 25.077 sec
```

## Appetite comes with eating

Now that we learned something new, we'd like to use it to make the
`tdd` task even more customizable. There are more things in the `tdd`
task to be treated with task options.

As you'll see some of those nice to have customizable things are not
easy at all to be introduced (e.g., namespaces to run test in), while
others are very simple.

Let's start with the simplest ones.

## Start small, grow fast

The first component of the `tdd` task is the `serve` one. Let's review its help:

```bash
boot serve -h
Start a web server on localhost, serving resources and optionally a directory.
Listens on port 3000 by default.

Options:
  -h, --help                Print this help info.
  -d, --dir PATH            Set the directory to serve; created if doesn't exist to PATH.
  -H, --handler SYM         Set the ring handler to serve to SYM.
  -i, --init SYM            Set a function to run prior to starting the server to SYM.
  -c, --cleanup SYM         Set a function to run after the server stops to SYM.
  -r, --resource-root ROOT  Set the root prefix when serving resources from classpath to ROOT.
  -p, --port PORT           Set the port to listen on. (Default: 3000) to PORT.
  -k, --httpkit             Use Http-kit server instead of Jetty
  -s, --silent              Silent-mode (don't output anything)
  -R, --reload              Reload modified namespaces on each request.
  -n, --nrepl REPL          Set nREPL server parameters e.g. "{:port 3001, :bind "0.0.0.0"}" to REPL.
```

At the moment and in the contest of the `tdd` task, we could be
interested in passing a port and a Ring handler. Personally, I'm very
interested in testing the famous asynchronous httpkit as well.



```bash boot tdd ...  Running cljs
tests...  Testing modern-cljs.shopping.validators-test

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


