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
* #{str}: is the CLJ type hint for the passed argument. Here the type
  hint is saying that the arguments will be interpreted as a set of
  strings;
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
used in the `dummy` task definition says. To proceed with the next
step, stop the `boot` process.

### Update build.boot

We know enough to start updating the `testing` task. Considering that
what `testing` really does it is to add paths to the `:source-paths`
environment variable, we'll change its name to `add-source-paths` as
well:

```clj
(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (merge-env! :source-paths dirs)
  identity)
```

> NOTE 1: here I used the `merge-env!` expression instead of the
> `set-env!` one because it makes the code easier to be read.


Boot's tasks are like
[Ring](https://github.com/ring-clojure/ring/wiki/Concepts#handlers)
handlers, but instead of taking a request map and returning a response
map, they take and return a
[fileset object](https://github.com/boot-clj/boot/wiki/Filesets). `add-source-paths`
does not do anything special. It only merges the `:source-paths` value
with the passed one and returns the `identity` function to make it a
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
   (add-source-paths :dirs #{"test/cljc"}) ;; before was testing
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env :phantom 
              :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})))
```

There is one more thing to be done for pleasing a `tdd` user: add the
same task option to `tdd` as well, in such a way the she can pass a
test directory to be added to the `:source-paths` variable from the
command line

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

> NOTE 2: the `-t` default value is now `#{"test/cljc" "test/clj"
> "test/cljs"}`. This is because even if none of those directories
> exists or contains any testing namespace, the internal `merge-env!`
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
task to be treated as task options.

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
[`http-kit`](http://www.http-kit.org/) web server as well.

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

Here the `-v` task option is the only one I'm currently interested in.

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
future (e.g. `-j, --on-jsload SYM`), when the project will evolve, at the
moment I'm not interested in exposing some of them to the `tdd` task
as well.

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

At the moment we are interested to expose to the `tdd` task the
`-n, --namespaces NAMESPACE` option only.

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

A total of 8 new task options to be exposed for the `tdd` task. Two of
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

As you see, a boolean task option works like a switch. To proceed with
the next step, stop the REPL.

## Add flag task options to tdd

We are now ready to add the `-k` and `-v` options to the `tdd` task definition.

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [t dirs PATH #{str} "test paths"
   k httpkit bool "Use http-kit web server instead of jetty"
   v verbose bool "Print which files have changed"]
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
its mode to `verbose`. If you now modify one of the watched file
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

Still working on [jetty](http://www.eclipse.org/jetty/) and without a
verbose report from the `watch` subtask.  To proceed with the next
step, stop the `boot` process.

## Add web server port

The `-p` option is very easy to be instructed as well.

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [...
   p port PORT int "The web server port to listen on (default: 3000)"]
  (let [...]
    (comp
     (serve ...
            :port port)
     ...)))
```

I leave to you the verification of passing or not passing the port
number (note the `int` type hint) when starting the `tdd` task.  To
proceed to the next step, stop the `boot` process.

## Add CLJS compilation options

As we saw in a previous paragraph, the `cljs-test` subtask has a
couple of compiler options we're interested in:

* `-o, --out-file VAL` to set name of the JS output file generated by
  the CLJS compiler
* `-O, --optimizations LEVEL` to set the compiler optimization options
  (i.e., `none`, `whitespace`, `simple` and `advanced`).

It also offers the `-e, --js-env VAL` option to choose the JS engine
to run test with. We'll leave the `-n, --namespaces NS` option to for
later, because this option is critical.

We're not going to explain the details of those options, because it
looks like the previous ones. Here is the update `tdd` task
definition:

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [e testbed        ENGINE kw     "The JS testbed engine (default phantom)" 
   k httpkit               bool   "Use http-kit web server (default jetty)"
   o output-to      NAME   str    "The JS output file name for test (default main.js)"
   O optimizations  LEVEL  kw     "The optimization level (default none)"
   p port           PORT   int    "The web server port to listen on (default 3000)"
   t dirs           PATH   #{str} "Test paths (default test/clj test/cljs test/cljc)"   
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

You can now test the `tdd` task by setting the various options as you
like at the command line.

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
Google Closure Libraries when you set the optimization mode to `none`.
The `source-map` features, which is activated by default with the
`none` mode, can be set for the other optimization modes as
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
be called from JS scripts in an HTML page? The reason for that
`:export` metadata (i.e., `^`) was to protect the `init` function from
being aggressively renamed by the CLJS compiler when set to `advanced`
mode.

The kind of job done by the Google Closure Compiler when used with
`advanced` mode is already awesome by itself. Recently, it has even
been added a new
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

We expressly left the `-n, --namespace NAMESPACE` option as the latest
to be treated. This option occurs in both the `test` task, specific
for CLJ, and the `test-cljs` task, specific for CLJS.

Let's see again the help for both the `test` and the `test-cljs`
tasks:

```bash
boot test -h
Run clojure.test tests in a pod.

The --namespaces option specifies the namespaces to test. The default is to
run tests in all namespaces found in the project.
...
Options:
  -h, --help                  Print this help info.
  -n, --namespaces NAMESPACE  Conj NAMESPACE onto the set of namespace symbols to run tests in.
...
```

```bash
boot test-cljs -h
Run cljs.test tests via the engine of your choice.

 The --namespaces option specifies the namespaces to test. The default is to
 run tests in all namespaces found in the project.

Options:
...
  -n, --namespaces NS        Conj NS onto namespaces whose tests will be run. All tests will be run if
                                 ommitted.
...
```

Note that if you do not specify one or more test namespaces, they both
run tests in all namespaces of the project (`test-cljs` even gests the
namepaspace from the immutable classpath).

Even if their behviour is not exactely the same, they both `conj` the
optional namespace onto the set of namespace symbols to run tests in.

This is very easily treated with the task options as follows:

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [...
   n namespaces     NS     #{sym} "the set of namespace symbols to run tests in"]
  (let [...]
    (comp
     ...
     (test-cljs ...
                :namespaces namespaces)
     (test :namespaces namespaces))))
```

Let's if this simple solution it works by first calling it with the
portable `modern-cljs.shopping.validators-test` test namespace and
then without any `-n` option:


```bash
boot tdd -n modern-cljs.shopping.validators-test
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
Elapsed time: 31.878 sec
```

The first test, aside form the time it takes the very first CLJS
compilation, worked like a charm. Now stop the `boot` process and
restart it without passing to it any option.

```bash
boot tdd
...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing adzerk.boot-reload.connection
...
Testing modern-cljs.shopping.validators-test
...

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.core

...

Testing modern-cljs.login.validators

...

Testing modern-cljs.shopping.validators

...

Testing modern-cljs.shopping.validators-test

...

Testing modern-cljs.login.validators

Testing modern-cljs.shopping.validators

Testing modern-cljs.shopping.validators-test

...

Ran 2 tests containing 26 assertions.
0 failures, 0 errors.
Elapsed time: 45.640 sec
```

What the hell is happening here? The `test-cljs` task evaluated all
the project namespaces, even the ones from the used libs. The `test`
task evaluates all the namespaces we defined in the project and for
some reason it evaluated the portable ones two times each.

Let's now see what happens if you force a bug in the `validators-test`
namespace:

```clj
...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing adzerk.boot-reload.connection
...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty" "Quantity has to be an integer number" "Quantity can't be negative"]}))
...
Ran 1 tests containing 13 assertions.
1 failures, 0 errors.

...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:9)
Shopping Form Validation / Happy Path
expected: nil
  actual: {:quantity
           ["Quantity can't be empty"
            "Quantity has to be an integer number"
            "Quantity can't be negative"]}
    diff: + {:quantity
             ["Quantity can't be empty"
              "Quantity has to be an integer number"
              "Quantity can't be negative"]}

...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:9)
Shopping Form Validation / Happy Path
expected: nil
  actual: {:quantity
           ["Quantity can't be empty"
            "Quantity has to be an integer number"
            "Quantity can't be negative"]}
    diff: + {:quantity
             ["Quantity can't be empty"
              "Quantity has to be an integer number"
              "Quantity can't be negative"]}

Testing modern-cljs.templates.shopping

Ran 2 tests containing 26 assertions.
2 failures, 0 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 2, :pass 24, :fail 2, :error 0, :type :summary}
                        clojure.core/ex-info            core.clj: 4593
           adzerk.boot-test/eval549/fn/fn/fn       boot_test.clj:   73
crisptrutski.boot-cljs-test/eval651/fn/fn/fn  boot_cljs_test.clj:  109
           adzerk.boot-cljs/eval268/fn/fn/fn       boot_cljs.clj:  200
           adzerk.boot-cljs/eval226/fn/fn/fn       boot_cljs.clj:  134
crisptrutski.boot-cljs-test/eval621/fn/fn/fn  boot_cljs_test.clj:   79
      adzerk.boot-cljs-repl/eval491/fn/fn/fn  boot_cljs_repl.clj:  171
              boot.task.built-in/fn/fn/fn/fn        built_in.clj:  284
              boot.task.built-in/fn/fn/fn/fn        built_in.clj:  281
      adzerk.boot-reload/eval391/fn/fn/fn/fn     boot_reload.clj:  120
         adzerk.boot-reload/eval391/fn/fn/fn     boot_reload.clj:  119
        boot.task.built-in/fn/fn/fn/fn/fn/fn        built_in.clj:  233
           boot.task.built-in/fn/fn/fn/fn/fn        built_in.clj:  233
              boot.task.built-in/fn/fn/fn/fn        built_in.clj:  230
         pandeiro.boot-http/eval314/fn/fn/fn       boot_http.clj:   83
                         boot.core/run-tasks            core.clj:  701
                           boot.core/boot/fn            core.clj:  711
         clojure.core/binding-conveyor-fn/fn            core.clj: 1916
                                         ...
Elapsed time: 9.480 sec
```

Oh my God. The `test-cljs` reevaluated again all the project
namespaces, including the one defined in the used libs, and the `test`
task evaluated all the project namespaces. Again it evaluated the
portable `validators-test` two times.

Correct the forced bug and you'll see again the same behavior. Too
bad.

That said, let's see at least if the current `tdd` configuration is
able to manage a new test namespace while it's running.

Create a new `validators_test.cljc` portable `.cljc` file in the
`test/cljc/modern_cljs/login` test directory.

Define a very simple unit test with a single assertion for the
`user-credential-errors` validator we defined in a
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md#validatorscljc).

```clj
(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest user-credential-errors-test
  (testing "Login Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (user-credential-errors "me@me.com" "weak1")))))
```

As soon as you save the file the test machinery get triggered

```bash
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
...
Testing modern-cljs.login.validators-test
...
Testing modern-cljs.shopping.validators-test
...

Ran 2 tests containing 14 assertions.
0 failures, 0 errors.

...
Testing modern-cljs.login.validators-test
...
Testing modern-cljs.shopping.validators-test
...
Testing modern-cljs.login.validators-test
...
Testing modern-cljs.shopping.validators-test
...

Ran 4 tests containing 28 assertions.
0 failures, 0 errors.
Elapsed time: 10.529 sec
```

As you see, the test machinery behavior is still unacceptable, but at
least the newly defined unit test for the `login` form validator got
seen and correctly evaluated by both the CLJ and CLJS engines.

## Give up?

I'm not a TDD practitioner, but if I were, I'd never accept to restart
the environment to add new test files, like `tdd` requires if we
launch it by specifying the initial test namespaces to be run with the
`-n` option and neither I would accept to wait so long to see the test
results as it happens when `tdd` is launched without specifying any
test namespace to be run.

At the moment we have to accept a trade off:

* be explicit with the test namespaces you want to run test in by
  specifying them on the command line with the `-n`;
* stop and restart the `tdd` task when you need to add tests in a new
  test namespace.

```bash
boot tdd -n modern-cljs.shopping.validators-test -n modern-cljs.login.validators-test
...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 2 tests containing 14 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 2 tests containing 14 assertions.
0 failures, 0 errors.
Elapsed time: 24.895 sec
```

## Add login test assertion

While `boot` is running, let's now add more assertions to the only one
test we previously defined in the `modern-cljs.login.validators-test`
namespace. Open the corresponding
`test/cljc/modern_cljs/login/validators_test.cljc` file and start
adding few assertions to the `user-credential-errors-test` test
function:

```clj
(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest user-credential-errors-test
  (testing "Login Form Validation"
    
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (user-credential-errors "me@me.com" "weak1")))
    
    (testing "/ Email presence"
      (are [expected actual] (= expected actual)
        "Email can't be empty."
        (first (:email (user-credential-errors "" "")))
        "Email can't be empty."
        (first (:email (user-credential-errors "" nil)))
        "Email can't be empty."
        (first (:email (user-credential-errors "" "weak1")))
        "Email can't be empty."
        (first (:email (user-credential-errors "" "weak")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil nil)))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "weak1")))
        "Email can't be empty."
        (first (:email (user-credential-errors nil "weak")))))

    (testing "/ Password presence"
      (are [expected actual] (= expected actual)
        "Password can't be empty."
        (first (:password (user-credential-errors "" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors nil "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "me@me.com" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "me" "")))
        "Password can't be empty."
        (first (:password (user-credential-errors "" nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors nil nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors "me@me.com" nil)))
        "Password can't be empty."
        (first (:password (user-credential-errors "me" nil)))))

    (testing "/ Email validity"
      (are [expected actual] (= expected actual)
        "The provided email is invalid."
        (first (:email (user-credential-errors "me" "")))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me.me" nil)))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me@me" "weak")))
        "The provided email is invalid."
        (first (:email (user-credential-errors "me.me@me" "weak1")))))

    (testing "/ Password validity"
      (are [expected actual] (= expected actual)
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "weak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors "" "lessweak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "lessweak")))
        "The provided password is invalid"
        (first (:password (user-credential-errors nil "toolongforthat")))))))
```

As you save the file, `tdd` will trigger the recompilation and the
tests run as well counting 38 assertions in 2 tests each.

## Specific CLJ test in a portable test namespace

Now add to the same portable test-namespace a test that is specific
for CLJ, i.e. the one regarding the existence of the domain of an
email address:

```clj
(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :as v :refer [user-credential-errors]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

#?(:clj (deftest email-domain-errors-test
          (testing "Email domain existence"
            (are [expected actual] (= expected actual)
              "The domain of the email doesn't exist."
              (first (:email (v/email-domain-errors "me@googlenospam.com")))))))
```

Note as to be able to continue to share the requirement of the
`modern-cljs.login.validators` namespace between CLJ and CLJS, we
added the ``v` alias. This way we can call the `email-domain-errors`
function which is defined in the `modern-cljs.login.validators`
portable namespace for CLJ only. Obviously we had to use the `#?`
reader conditional.

Here is the result in the running `tdd` task:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...Unexpected response code: 400

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 2 tests containing 38 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 39 assertions.
0 failures, 0 errors.
Elapsed time: 4.024 sec
```

The CLJS test namespaces still have 2 tests containing 38
assertions, while the the CLJ test namespaces now have 3 tests
containing 39 assertions.

To proceed to the next step, stop any related `boot` process.

## Code clean up

We are almost done. There is one more thing I like to do: cleaning the
`tdd` code by introducing a global map for all the defaults we used
and add the above two test-namespaces as defaults as well to allow the
`tdd` task to be called without specifying any option at the command
line.

Here is the cleaned `build.boot` file:

```clj
(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"html"}
 :target-path "target"

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.2"]
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; add bREPL
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL 
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"]
                 [hiccups "0.3.0"]
                 [compojure "1.4.0"]                   ;; for routing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojars.magomimmo/valip "0.4.0-SNAPSHOT"]
                 [enlive "1.1.6"]
                 [adzerk/boot-test "1.0.6"]
                 [crisptrutski/boot-cljs-test "0.2.1-SNAPSHOT"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-test :refer [test]]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]]
         )

(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :output-to "main.js"
               :testbed :phantom
               :target "target"
               :namespaces '#{modern-cljs.shopping.validators-test
                              modern-cljs.login.validators-test}})

(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (merge-env! :source-paths dirs)
  identity)

(deftask tdd
  "Launch a customizable TDD Environment"
  [e testbed        ENGINE kw     "the JS testbed engine (default phantom)" 
   k httpkit               bool   "Use http-kit web server (default jetty)"
   n namespaces     NS     #{sym} "the set of namespace symbols to run tests in"
   o output-to      NAME   str    "the JS output file name for test (default main.js)"
   O optimizations  LEVEL  kw     "the optimization level (default none)"
   p port           PORT   int    "the web server port to listen on (default 3000)"
   t dirs           PATH   #{str} "test paths (default test/clj test/cljs test/cljc)"   
   v verbose               bool   "Print which files have changed (default false)"]
  (let [dirs (or (:test-dirs defaults))
        output-to (or output-to (:output-to defaults))
        testbed (or testbed (:testbed defaults))
        namespaces (or namespaces (:namespaces defaults))]
    (comp
     (serve :dir (:target defaults)                                
            :handler 'modern-cljs.core/app
            :resource-root (:target defaults)
            :reload true
            :httpkit httpkit
            :port port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload)
     (cljs-repl)
     (test-cljs :out-file output-to 
                :js-env testbed 
                :namespaces namespaces
                :optimizations optimizations)
     (test :namespaces namespaces))))

(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :dir (:target defaults)                                
          :handler 'modern-cljs.core/app               ;; ring hanlder
          :resource-root (:target defaults)            ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)))
```

Note as we used a map to set the defaults for the various options

```clj
(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :output-to "main.js"
               :testbed :phantom
               :target "target"
               :namespaces '#{modern-cljs.shopping.validators-test
                              modern-cljs.login.validators-test}})
```

and how we used the map of defaults in the `let/or` form:

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [e testbed        ENGINE kw     "the JS testbed engine (default phantom)" 
   k httpkit               bool   "Use http-kit web server (default jetty)"
   n namespaces     NS     #{sym} "the set of namespace symbols to run tests in"
   o output-to      NAME   str    "the JS output file name for test (default main.js)"
   O optimizations  LEVEL  kw     "the optimization level (default none)"
   p port           PORT   int    "the web server port to listen on (default 3000)"
   t dirs           PATH   #{str} "test paths (default test/clj test/cljs test/cljc)"   
   v verbose               bool   "Print which files have changed (default false)"]
  (let [dirs (or (:test-dirs defaults))
        output-to (or output-to (:output-to defaults))
        testbed (or testbed (:testbed defaults))
        namespaces (or namespaces (:namespaces defaults))]
    (comp
     (...)))
```

One more thing. The time it takes the `tdd` task to recompile and run
the tests would be still judged unacceptable by a TDD practitioner
when is longer than a second. In the `tdd` task, most of time is spent
by the `test-cljs` to internally create and start a new instance of
the underlying phantom JS engine again and again anytime it has to
rerun the tests. We're not going to solve this problem in this
tutorial, but at least we now know where to look at if we wanted.

That's it. Stop any `boot` related process and reset the git branch.

```bash
git reset --hard
```

Stay tuned for the next tutorial.

# Next Step - Tutorial 17 - TBD 

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md

