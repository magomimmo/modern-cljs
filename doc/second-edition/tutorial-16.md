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

To start working from the end of the [previous tutorial][1], assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-15
```

## Introduction

Even if I never started a new program from a failed test, that has
more to do with my age that with my opinion about TDD. That said, the
workflow induced from the `tdd` task we ended up in the
[previous tutorial][1] would be criticized by any TDD practitioner.
We'd like to please them a little bit better than that.

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
[s-expression](https://en.wikipedia.org/wiki/S-expression) is a term
that is specific for the LISP's communities. But we don't want to
bother you with long explanations about those things. Let's be
pragmatic and directly afford our needs.

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
have to parse the passed argument by yourself.

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
clojure.core/binding-conveyor-fn/fn  core.clj: 1916                                ...
```

We can even ask for the help documentation:

```bash
boot dummy -h
A dummy task

Options:
  -h, --help       Print this help info.
  -t, --dirs PATH  Conj PATH onto :source-paths
```

Let's now call the `dummy` task from the CLJ REPL:

```bash
cd /path/to/modern-cljs
boot repl
...
boot.user=>
```

```clj
boot.user=> (dummy)
{}
```

```clj
boot.user=> (dummy "-t" "test/cljs")
{:dirs #{"test/cljs"}}
```

```clj
boot.user=> (dummy "-t" "test/clj" "-t" "test/cljs")
{:dirs #{"test/clj" "test/cljs"}}
```

```clj
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

Boot's tasks are like
[Ring](https://github.com/ring-clojure/ring/wiki/Concepts#handlers)
handlers, but instead of taking a request map and returning a response
map, they take and return a
[fileset object](https://github.com/boot-clj/boot/wiki/Filesets). `add-source-paths`
does not do anything special. It only alters the `:source-paths`
environment variable and returns the `identity` function to make it a
composable task.

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

There is more thing to be done for pleasing a `tdd` user: add the same
task option to `tdd` as well, in such a way the she can pass a test
directory to be added to the `:source-paths` variable from the command
line

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

> NOTE 1: the `-t` default value is now `#{"test/cljc" "test/clj"
> "test/cljs"}`. This is because even if none of those directories
> exists or contains any testing namespace, the internal `set-env!`
> function does not complain.

Note how we used the DSL to define the same `-t` option of the
`add-source-paths` task. It only has a different
description. Moreover, we used the `let` and the `or` forms to give it
a default value and finally passed the evaluated argument to the
internal `add-source-paths` call. This is an idiomatic way to use task
options.

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

The report is still working as expected. To proceed with the next
step, stop the `boot` process.

## Appetite comes with eating

Now that we learned something new, we'd like to use it to make the
`tdd` task even more customizable. There are more things in the `tdd`
task to be treated with task options.

As you'll see some of those things are not easy at all to be
introduced (e.g., namespaces to run test in), while others are very
simple.

Let's start with the simplest ones.

## Start small, grow fast

To have a more clear idea of the customization options we could
introduce for the `tdd` tasks, let's review the help documentation for
each subtask it internally uses.

### serve task options

Following is the `serve` task's help:

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

Some of those `serve` task options could be useful for `tdd` as well:

* `-d, --dir PATH`
* `-H, --handler SYM`
* `-r, --resource-root ROOT`
* `-p, --port PORT`
* `-k, --httpkit`

In the contest of the current project, I'm interested in the `-p` task
option. But I'd like to test the project on the famous asynchronous
`httpkit` web server as well.

### watch task options

Following is the `watch` task's help:

```bash
boot watch -h
Call the next handler when source files change.

Debouncing time is 10ms by default.

Options:
  -h, --help     Print this help info.
  -q, --quiet    Suppress all output from running jobs.
  -v, --verbose  Print which files have changed.
  -M, --manual   Use a manual trigger instead of a file watcher.
```

Here the `-v` task option is the only one I'm interested in.

### reload task options

Following is the `reload` task's help:

```bash
boot reload -h
Live reload of page resources in browser via websocket.

The default configuration starts a websocket server on a random available
port on localhost.

Open-file option takes three arguments: line number, column number, relative
file path. You can use positional arguments if you need different order.
Arguments shouldn't have spaces.
Examples:
vim --remote +norm%sG%s| %s
emacsclient -n +%s:%s %s

Options:
  -h, --help               Print this help info.
  -b, --ids BUILD_IDS      Conj [BUILD IDS] onto only inject reloading into these builds (= .cljs.edn files)
  -i, --ip ADDR            Set the (optional) IP address for the websocket server to listen on to ADDR.
  -p, --port PORT          Set the (optional) port the websocket server listens on to PORT.
  -w, --ws-host WSADDR     Set the (optional) websocket host address to pass to clients to WSADDR.
  -j, --on-jsload SYM      Set the (optional) callback to call when JS files are reloaded to SYM.
  -a, --asset-path PATH    Set the (optional) asset-path. This is removed from the start of reloaded urls to PATH.
  -s, --secure             Flag to indicate whether the client should connect via wss. Defaults to false.
  -o, --open-file COMMAND  Set the (optional) command to run when warning or exception is clicked on HUD. Passed to format to COMMAND.
```

Even if there are some very interesting options to be used in the
future, when the project will evolve, at the moment I'm not interested
in exposing some of them to the `tdd` task as well.

### cljs-repl task options

Following is the `cljs-repl` task's help:

```bash
boot cljs-repl -h
Start a ClojureScript REPL server.

The default configuration starts a websocket server on a random available
port on localhost.

Options:
  -h, --help                   Print this help info.
  -b, --ids BUILD_IDS          Conj [BUILD IDS] onto only inject reloading into these builds (= .cljs.edn files)
  -i, --ip ADDR                Set the IP address for the server to listen on to ADDR.
  -n, --nrepl-opts NREPL_OPTS  Set options passed to the repl task to NREPL_OPTS.
  -p, --port PORT              Set the port the websocket server listens on to PORT.
  -w, --ws-host WSADDR         Set the (optional) websocket host address to pass to clients to WSADDR.
  -s, --secure                 Flag to indicate whether the client should connect via wss. Defaults to false.
```

As the previous `reload` task: at the moment I'm not interested to
expose any of those options to the `tdd` task.

### test-cljs task options

Following is the `cljs-test` task's help:

```bash
boot test-cljs -h
Run cljs.test tests via the engine of your choice.

 The --namespaces option specifies the namespaces to test. The default is to
 run tests in all namespaces found in the project.

Options:
  -h, --help                 Print this help info.
  -e, --js-env VAL           Set the environment to run tests within, eg. slimer, phantom, node,
                                 or rhino to VAL.
  -n, --namespaces NS        Conj NS onto namespaces whose tests will be run. All tests will be run if
                                 ommitted.
  -s, --suite-ns NS          Set test entry point. If this is not provided, a namespace will be
                                 generated to NS.
  -O, --optimizations LEVEL  Set the optimization level to LEVEL.
  -o, --out-file VAL         Set output file for test script to VAL.
  -c, --cljs-opts VAL        Set compiler options for CLJS to VAL.
  -x, --exit?                Exit immediately with reporter's exit code.
```

Here there are a bunch of interesting task options to be exposed to `tdd` as well, namely:

* `-e, --js-env VAL` to choose the JS engine to be used as test bed;
* `-n, --namespaces NS` to choose the test namespace to run test in
* `-O, --optimizations LEVEL` to run the tests with different CLJS
  optimizations (i.e. `none`, `whitespace`, `simple` and `advanced`;
* `o, --out-file VAL` to choose the name of the JS file generated by
  the CLJS compiler.

### test task options

Following is the `test` task's help:

```bash
boot test -h
Run clojure.test tests in a pod.

The --namespaces option specifies the namespaces to test. The default is to
run tests in all namespaces found in the project.

The --exclusions option specifies the namespaces to exclude from testing.

The --filters option specifies Clojure expressions that are evaluated with %
bound to a Var in a namespace under test. All must evaluate to true for a Var
to be considered for testing by clojure.test/test-vars.

Options:
  -h, --help                  Print this help info.
  -n, --namespaces NAMESPACE  Conj NAMESPACE onto the set of namespace symbols to run tests in.
  -e, --exclusions NAMESPACE  Conj NAMESPACE onto the set of namespace symbols to be excluded from test.
  -f, --filters EXPR          Conj EXPR onto the set of expressions to use to filter namespaces.
  -r, --requires REQUIRES     Conj REQUIRES onto extra namespaces to pre-load into the pool of test pods for speed.
```

Stay tuned for the next tutorial.

# Next Step - Tutorial 17 - TBD 


Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md

