# Tutorial 16 - On pleasing TDD practitioners

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

### Serve task options

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

### Watch task options

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

### Reload task options

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

### Cljs-repl task options

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

### Test-cljs task options

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

### Test task options

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

At the moment we are interested to expose to the `tdd` task both the
`-n, --namespaces NAMESPACE` and the `-f, --filters EXPR` options.

## Task options list for tdd

To summarize, this is the list of the candidate options tasks we'd
like to be exposed by the `tdd` task as its options:

* `serve` task options
  * `-p, --port PORT`
  * `-k, --httpkit`
* `watch` task options
  * `-v, --verbose`
* `cljs-test` task options
  * `-e, --js-env VAL`
  * `-n, --namespaces NS`
  * `-O, --optimizations LEVEL`
  * `-o, --out-file VAL`
* `test` task options
  * `-n, --namespaces NAMESPACE`
  * `-f, --filters EXPR`

A total of 9 new task options to be exposed for the `tdd` task. Two of
them, namely `-k` and `-v`, are
[flags boolean options](https://github.com/boot-clj/boot/wiki/Task-Options-DSL#flags). You
recognize this kind of options from the fact that they do not have an
optarg like the others (e.g., `PORT`, `VAL`, `NS`, `EXPR`, etc.).

## Dummy task again

Let's see how a boolean task option works by exploiting the `dummy`
task again.

```clj
(deftask dummy
  "A dummy task"
  [t dirs PATH #{str} ":source-paths"
   v verbose bool "print which files have changed"
   k httpkit bool "use httt-kit web server instead of jetty"]
  *opts*)
```

Start the REPL

```bash
boot repl
...
boot.user=>
```

and evaluate few `dummy` calls:

```clj
boot.user=> (dummy)
{}
```

```clj
boot.user=> (dummy "-t" "test/cljc")
{:dirs #{"test/cljc"}}
```
```clj
boot.user=> (dummy "-t" "test/cljc" "-v")
{:dirs #{"test/cljc"}, :verbose true}
```

```clj
boot.user=> (dummy "-t" "test/cljc" "-v" "-k")
{:dirs #{"test/cljc"}, :verbose true, :httpkit true}
```

As you see, a boolean task option works like a flag to switch it
on. To proceed with the next step, stop the REPL.

## Add flag task options to tdd

We are now ready to add the `-k` and `-v` options to the `tdd` task definition.

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [t dirs PATH #{str} "test paths"
   k httpkit bool "use http-kit web server instead of jetty"
   v verbose bool "print which files have changed"]
  (let [dirs (or dirs #{"test/cljc" "test/clj" "test/cljs"})]
    (comp
     (serve :dir "target"                                
            :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true
            :httpkit httpkit)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload)
     (cljs-repl)
     (test-cljs :out-file "main.js" 
                :js-env :phantom 
                :namespaces '#{modern-cljs.shopping.validators-test})
     (test :namespaces '#{modern-cljs.shopping.validators-test}))))
```

Note that this time we just passed down the boolean options as read
from the DSL machinery. This is because in a boolean context `nil` is
equivalent to `false`.

Let's now see `-k` and `-v` at work

```bash
boot tdd -k -v
Retrieving http-kit-2.1.18.jar from https://clojars.org/repo/
...
Started HTTP Kit on http://localhost:3000
...
◉ :cp modern_cljs/login.cljs
◉ :cp modern_cljs/login.clj
◉ :cp shopping.html
◉ :cp modern_cljs/templates/shopping.clj
◉ :cp css/styles.css
◉ :cp modern_cljs/shopping/validators.cljc
◉ :cp index.html
◉ :cp adzerk/boot_reload.cljs
◉ :cp modern_cljs/core.clj
◉ :cp modern_cljs/login/validators.cljc
◉ :cp modern_cljs/shopping/validators_test.cljc
◉ :cp adzerk/boot_cljs_repl.cljs
◉ :cp modern_cljs/shopping.cljs
◉ :cp modern_cljs/remotes.clj
...
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 27.700 sec
```

This is the first time we used the `http-kit` asynchronous web server
and as you see the `tdd` task first downloaded and then started
it. The `-v` option correctly instructed the `watch` subtask to set
its mode to `verbose`. If you now modify one of the observed file
(e.g., `modern_cljs/shopping/validators_test.cljc`), you'll see the
`watch` verbose option at work:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0") ;; force a bug
           ...))
    ...))
```

```bash
◉ :cp modern_cljs/shopping/validators_test.cljc

...
Compiling ClojureScript...
• main.js
Running cljs tests...
...
Elapsed time: 2.543 sec
```

Correct the forced bug and the `watch` subtask will trigger the
recompilation and rerun the tests.

```bash
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "1" "0" "0" "0") ;; correct the bug
           ...))
    ...))
```

```bash
◉ :cp modern_cljs/shopping/validators_test.cljc

Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 2.130 sec
```

Now stop the `boot` process and rerun the `tdd` task without any
option set.

```bash
boot tdd
...
Started Jetty on http://localhost:3000
...
Elapsed time: 28.143 sec
```

Still working on jetty and without a verbose report from the `watch`
subtask.  To proceed with the next step, stop the `boot` process.

## Add web server port

The `-p` option is very easy to be instructed as well.

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [...
   p port PORT int "the web server port to listen on (default: 3000)"]
  (let [...]
    (comp
     (serve ...
            :port port)
     ...)))
```

I leave to you the verification of passing or not passing the port
number (note above the `int` type hint) when starting the `tdd` task.
To proceed to the next step, stop the `boot` process.

## Add CLJS compilation options

As we saw in a previous paragraph, the `cljs-test` subtask has a
couple of compiler options we're interested in:

* `-o, --out-file VAL` to set name of the JS output file generated by
  the CLJS compiler
* `-O, --optimizations LEVEL` to set the compiler optimization options
  (i.e., `none`, `whitespace`, `simple` and `advanced`).

It also offers the `-e, --js-env VAL` option to choose the testbed JS
engine to run test with. We'll leave the `-n, --namespaces NS` option
to choose the test namespaces to be run within the testbed for later,
because this option is very cumbersome from a TDD practitioner point
of view.

We're not going to be too detailed in explaining the details of these
`test-cljs` options, because they look like the previous ones. Here is
the update `tdd` task definition:

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [e testbed        ENGINE kw     "the JS testbed engine (default phantom)" 
   k httpkit               bool   "Use http-kit web server (default jetty)"
   o output-to      NAME   str    "the JS output file name for test (default main.js)"
   O optimizations  LEVEL  kw     "the optimization level (default none)"
   p port           PORT   int    "the web server port to listen on (default 3000)"
   t dirs           PATH   #{str} "test paths (default test/clj test/cljs test/cljc)"   
   v verbose               bool   "Print which files have changed (default false)"]
  (let [dirs (or dirs #{"test/cljc" "test/clj" "test/cljs"})
        output-to (or output-to "main.js")
        testbed (or testbed :phantom)]
    (comp
     (serve :dir "target"                                
            :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true
            :httpkit httpkit
            :port port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload)
     (cljs-repl)
     (test-cljs :out-file output-to 
                :js-env testbed 
                :namespaces '#{modern-cljs.shopping.validators-test}
                :optimizations optimizations)
     (test :namespaces '#{modern-cljs.shopping.validators-test}))))
```

You should only note how we exploited the idiomatic way (i.e. `let`
and `or` forms) to set the default arguments for the `output-to` and
the `testbed` options.

You can now test the `tdd` task by calling it from the command line by
setting the various options as you like.

Following is the test on the `tdd` task called without any option,
just to demonstrate that we can still use the simplest form of the
command.

```bash
boot tdd
...
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

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
Elapsed time: 29.043 sec
```

To proceed to the next step, stop the `boot` process.

## A short digression on CLJS compiler optimizations

We never talked too much in this series about CLJS compiler
optimizations. Not so long time ago the CLJS compiler optimization
management was cumbersome. The most annoyed optimization option to be
configured was the `none` one, because it required to explicitly link
a bunch of Google Closure JS libs in the HTML pages. At the same time
the `none` optimization was also the quickest to compile down CLJS
code into JS code: a kind of counter sense.

Moreover, until a recent past, the `source-map` option was not
available. Debugging a CLJS code by setting breakpoints in the
generated JS code could quickly become a PITA.

Those incidental complexities are gone. You still have `none`,
`whitespace`, `simple` and `advanced` optimization mode, but the CLJS
compilers is now able to manage by itself the addition of the needed
Google Closure Libraries when you set the optimization mode to `node`.
The `source-map` features, which is activated by default with the
`none` mode, can be set for the other optimization mode as
well. Consequently, the debugging experience of CLJS code from the
development tools of your browser is now as simple as with JS code
itself.

The shortest description on CLJS compiler optimization modes I found
is the one available in the `cljs` help:

```bash
boot cljs -h
...
Available --optimization levels (default 'none'):

* none         No optimizations. Bypass the Closure compiler completely.
* whitespace   Remove comments, unnecessary whitespace, and punctuation.
* simple       Whitespace + local variable and function parameter renaming.
* advanced     Simple + aggressive renaming, inlining, dead code elimination.

Source maps can be enabled via the --source-map flag. This provides what the
browser needs to map locations in the compiled JavaScript to the corresponding
locations in the original ClojureScript source files.
...
```

The most intriguing compiler optimization mode (or level) is the
`advanced` one. Do you remember when in the
[Tutorial 6 - The Easy Made Complex and the Simple Made Easy](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-06.md)
we introduced the `:export` metadata to allow the `init` function to
be called from JS scripts in an HTML page? The reason of that
`:export` metadata (i.e., `^`) was to protect the `init` function from
being aggressively renamed by the CLJS compiler when set to `advanced`
mode.

The kind of job done by the Google Closure Compiler when used with
`advanced` mode is already awesome by itself. Recently, it has been
even been added a new
[`modules` option](https://github.com/clojure/clojurescript/wiki/Compiler-Options#modules)
which is able to break the JS generated file in small pieces to better
support the needs of Single Page Applications (SPA) to download only
the needed parts of the JS code depending on the use of the
application itself.

We're not going to explain right now this new `modules` option, but
we'll come back to it in a later tutorial.

At the moment we suggest you to test the various CLJS compiler options
by using the `-O` command-line options we previously prepared for the
`tdd` task.

```bash
boot tdd -O whitespace
...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 34.868 sec
```

Visit the [Shopping Form URL](http://localhost:3000/shopping.html) to
verify that the calculator is still working. Now open a new terminal
and look at the dimension of the `main.js` file in the `target`
directory:

```bash
# from a new terminal
cd /path/to/modern-cljs
ls -lah target/main.js
-rw-r--r--  1 mimmo  staff   2.1M Dec 15 00:27 target/main.js
```

More than 2M, including the `cljs.test` lib and the tests
themselves. Stop the `boot` process and restart it by setting the
`advanced` optimization option:

```bash
boot tdd -O advanced
...
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 43.847 sec
```

Verify again that the
[Shopping Form](http://localhost:3000/shopping.html) is still working
as expected. See now the dimension of the `main.js` file generated
with the `advanced` option:

```bash
ls -lah target/main.js
-rw-r--r--  1 mimmo  staff   387K Dec 15 00:34 target/main.js
```

Less the 400K, included the `cljs.test` lib and the tests themselves.
And you still have to zip the file. Not so bad.

To proceed with the next step, stop the `boot` process.

## Test Namespaces



TBD

* `cljs-test` task options
  * `-n, --namespaces NS`
* `test` task options
  * `-n, --namespaces NAMESPACE`
  * `-f, --filters EXPR`


Stay tuned for the next tutorial.

# Next Step - Tutorial 17 - TBD 


Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md

