# Tutorial 16 - On pleasing TDD practitioners

In the [previous tutorial][1] we looked at the problem of using a
single JVM to run multiple `boot` tasks supporting Bret Victor's
Immediate Feedback Principle and the [Test Driven Development (TDD)][6]
workflow. While we are approaching that objective, we have ended up
with a few questions about the `tdd` task we defined. Namely, is it
possible to define the `tdd` task so that it is parameterizable, while
also keeping some sane defaults for typical usage?

In this tutorial we're going to answer that question by introducing
`boot`'s
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

While I have never started a new program by first writing a failing
test, that has more to do with my age than with my opinion about
[TDD][6]. That said, the workflow induced from the `tdd` task we ended up
in the [previous tutorial][1] would be criticized by any [TDD][6]
practitioner.  We'd like to please them a little bit more than at
present.

## Task Options Domain Specific Language

As you can read from the Wiki page cited above, `boot` is a servant of
many owners and it has to please them all:

> `boot` tasks are intended to be used in many ways, including from
> the command line, from a REPL, in the project's build.boot file, and
> from regular Clojure namespaces. This requires support for two
> conceptually different calling conventions: with command line
> options and optargs, and as s-expressions in Clojure with function
> arguments.  Furthermore, since task definitions are the API between
> `boot` and the user, it's important that they provide good usage and
> help documentation in both environments.

[Command-Line options and optargs](http://www.catb.org/esr/writings/taoup/html/ch10s05.html)
are Unix terms that any programmer should know about, while
[s-expression](https://en.wikipedia.org/wiki/S-expression) is a term
that is specific to the LISP's community. But we don't want to
bother you with long explanations about these things. So, let's be
pragmatic and get directly into our needs.

## Short requirements

At the moment in our project we only have the `test/cljc` test
directory, but as soon as the project gets more complicated we'll
probably need to add more testing namespaces specifically dedicated to
CLJ or CLJS.

Even though you may organize and name your testing namespaces
arbitrarily, it's considered good practices to keep any testing
namespaces specific to CLJ separated from the ones specific to
CLJS. We also want to keep both of those separated from testing
namespaces which are portable between CLJ and CLJS as well.

At some point you'll probably end up with a directory layout something
like the following:

```bash
test/
├── clj
├── cljc
└── cljs
```

This mimics the directory layout for the source code:

```bash
src/
├── clj
├── cljc
└── cljs
```

## Be prepared for changes

To be prepared for any testing scenario, while keeping the more common
ones as simple as possible (e.g., `boot tdd`), we need to design our
task definitions from top to bottom.

For example, we would like to be able to call the `tdd` task by
passing to it one or more test directories to be added to the
`classpath` of the project. Something like this:

```bash
boot tdd -t test/cljc
```
or this:

```bash
boot tdd -t test/clj -t test/cljs -t test/cljc
```

I know, you'd prefer something like the following form:

```bash
boot tdd -t test/clj:test/cljs:test/cljc
```

The task options [DSL][3] offers direct support for the former mode, named
[multiple options](https://github.com/boot-clj/boot/wiki/Task-Options-DSL#multi-options). If
you prefer (like I do) the `colon` separated directories mode, you
have to parse the command arguments yourself.

## A dummy task

Let's temporarily create a dummy task in the `build.boot` to see how
this could work:

```clj
(deftask dummy
  "A dummy task"
  [t dirs PATH #{str} ":source-paths"]
  *opts*)
```

Here we're using the task options inside the argument vector of the
task definition.

```clj
[t dirs PATH #{str} ":source-path"]
```

* `t`: is the shortname         eg: boot dummy -t
* `dirs`: is the longname       eg: boot dummy --dirs
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

However, if you do not pass an argument to the `-t` option, it will return an
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

Now let's now call the `dummy` task from the CLJ REPL:

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

As you see, the value assigned to the `:dirs` keyword in the `*opts*`
map is a set of string, exactly like the [DSL][3] `#{str}` type hint we
used in the `dummy` task definition says. To proceed with the next
step, stop the `boot` process.

### Update build.boot

We now know enough to start updating the `testing` task. Considering that
all the `testing` task does is add paths to the `:source-paths`
environment variable, we'll change its name to `add-source-paths` as
well:

```clj
(deftask add-source-paths
  "Add paths to :source-paths environment variable"
  [t dirs PATH #{str} ":source-paths"]
  (merge-env! :source-paths dirs)
  identity)
```

> NOTE 1: Here I used the `merge-env!` function instead of
> `set-env!` since I think it makes the code easier to be read.


Boot's tasks are like
[Ring](https://github.com/ring-clojure/ring/wiki/Concepts#handlers)
handlers, but instead of taking a request map and returning a response
map, they take and return a
[fileset object](https://github.com/boot-clj/boot/wiki/Filesets). `add-source-paths`
does not do anything special. It only merges the `:source-paths` value
with the command-line argumment and returns the `identity` function to make it a
composable task.

Now substitute the newly defined `add-source-paths` task for the
previous `testing` task in the `tdd` task definition:

```clj
(deftask tdd
  "Launch a TDD Environment"
  []
  (comp
   (serve :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (add-source-paths :dirs #{"test/cljc"})      ; old name was testing
   (watch)
   (reload :ws-host "localhost")
   (cljs-repl)
   (test-cljs :out-file "main.js"
              :js-env :phantom
              :namespaces '#{modern-cljs.shopping.validators-test}
              :update-fs? true)
   (test :namespaces '#{modern-cljs.shopping.validators-test})
   (target :dir #{"target"})))
```

We need to do one more thing to please a `tdd` user: add the
same task option to `tdd` as well, so that we can pass a
test directory on the command line and have it added
to the `:source-paths` variable.

```clj
(deftask tdd
  "Launch a customizable TDD Environment"
  [t dirs PATH #{str} "test paths"]
  (let [dirs (or dirs #{"test/cljc" "test/clj" "test/cljs"})]
    (comp
     (serve :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true)
     (add-source-paths :dirs dirs)
     (watch)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file "main.js"
                :js-env :phantom
                :namespaces '#{modern-cljs.shopping.validators-test}
                :update-fs? true)
     (test :namespaces '#{modern-cljs.shopping.validators-test})
     (target :dir #{"target"}))))
```

> NOTE 2: The `-t` default value is now `#{"test/cljc" "test/clj"
> "test/cljs"}`. In boot versions previous to 2.7.1 even if none of these directories
> exist or contains any testing namespace, the internal `merge-env!`
> function will not complain, but from 2.7.1 they must exist, or an exception java.nio.file.NoSuchFileException will be thrown

Note how we used the [DSL][3] to define the same `-t` option as for the
`add-source-paths` task. The only difference for the `tdd` task is the
description "test paths". Moreover, we used the `let` and `or` forms to give it
a default value, before finally passing the argument to the
internal `add-source-paths` function. This is the idiomatic way to use task
options.

Before using the newly updated `tdd` task, let see its help text:

```bash
boot tdd -h
Launch a customizable TDD Environment

Options:
  -h, --help       Print this help info.
  -t, --dirs PATH  Conj PATH onto test paths
```

Now call it from the command line:

```bash
boot tdd -t test/cljc
...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

Now that we have learned something new, we'd like to use it to make the
`tdd` task even more customizable. There are more things in the `tdd`
task that may be treated as task options.

Let's start with the simplest ones.

## Start small, grow fast

To have a clearer idea of the customization options available to
the `tdd` task, let's review the help documentation for
each subtask it uses internally.

### Serve task options

The following is the `serve` task's help text:

```bash
boot serve -h
Start a web server on localhost, serving resources and optionally a directory.
Listens on port 3000 by default.

Options:
  -h, --help                Print this help info.
  -d, --dir PATH            PATH sets the directory to serve; created if doesn't exist.
  -H, --handler SYM         SYM sets the ring handler to serve.
  -i, --init SYM            SYM sets a function to run prior to starting the server.
  -c, --cleanup SYM         SYM sets a function to run after the server stops.
  -r, --resource-root ROOT  ROOT sets the root prefix when serving resources from classpath.
  -p, --port PORT           PORT sets the port to listen on. (Default: 3000).
  -k, --httpkit             Use Http-kit server instead of Jetty
  -s, --silent              Silent-mode (don't output anything)
  -t, --ssl                 Serve via Jetty SSL connector on localhost on default port 3443 using cert from ./boot-http-keystore.jks
  -T, --ssl-props SSL       SSL sets override default SSL properties e.g. "{:port 3443, :keystore "boot-http-keystore.jks", :key-password "p@ssw0rd"}".
  -R, --reload              Reload modified namespaces on each request.
  -n, --nrepl REPL          REPL sets nREPL server parameters e.g. "{:port 3001, :bind "0.0.0.0"}".
  -N, --not-found SYM       SYM sets a ring handler for requested resources that aren't in your directory. Useful for pushState.
```

Some of those `serve` task options could be useful for `tdd` as well:

* `-d, --dir PATH`
* `-H, --handler SYM`
* `-r, --resource-root ROOT`
* `-p, --port PORT`
* `-k, --httpkit`

In the context of the current project, I'm interested in the `-p` task
option. But I'd like to test the project on the famous asynchronous
[`http-kit`](http://www.http-kit.org/) web server as well.

### Watch task options

Following is the `watch` task's help:

```bash
boot watch -h
Call the next handler when source files change.

Options:
  -h, --help           Print this help info.
  -q, --quiet          Suppress all output from running jobs.
  -v, --verbose        Print which files have changed.
  -M, --manual         Use a manual trigger instead of a file watcher.
  -d, --debounce MS    MS sets debounce time (how long to wait for filesystem events) in milliseconds.
  -i, --include REGEX  Conj REGEX onto the set of regexes the paths of changed files must match for watch to fire.
  -e, --exclude REGEX  Conj REGEX onto the set of regexes the paths of changed files must not match for watch to fire.
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

Client options can also be set in .cljs.edn file, using property :boot-reload, e.g.
:boot-reload {:on-jsload frontend.core/reload}

Options:
  -h, --help                  Print this help info.
  -b, --ids BUILD_IDS         Conj [BUILD IDS] onto only inject reloading into these builds (= .cljs.edn files)
  -i, --ip ADDR               ADDR sets the IP address for the websocket server to listen on. (optional).
  -p, --port PORT             PORT sets the port the websocket server listens on. (optional).
      --ws-port PORT          PORT sets the port the websocket will connect to. (optional).
  -w, --ws-host WSADDR        WSADDR sets the websocket host clients connect to. Defaults to current host. (optional).
  -s, --secure                Flag to indicate whether the client should connect via wss. Defaults to false.
  -j, --on-jsload SYM         SYM sets the callback to call when JS files are reloaded. (client, optional).
      --asset-host HOST       HOST sets the asset-host where to load files from. Defaults to host of opened page. (client, optional).
  -a, --asset-path PATH       PATH sets sets the output directory for temporary files used during compilation. (optional).
  -c, --cljs-asset-path PATH  PATH sets the actual asset path. This is added to the start of reloaded urls. (optional).
  -o, --open-file COMMAND     COMMAND sets the command to run when warning or exception is clicked on HUD. Passed to format. (optional).
  -v, --disable-hud           Toggle to disable HUD. Defaults to false (visible).
  -t, --target-path VAL       VAL sets target path to load files from, used WHEN serving files using file: protocol. (optional).
      --only-by-re REGEXES    Conj REGEXES onto vector of path regexes (for `boot.core/by-re`) to restrict reloads to only files within these paths (optional).
```

Even if there are some very interesting options available for the future
(e.g. `-j, --on-jsload SYM`), at the
moment I'm not interested in exposing any of them for the `tdd` task.

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

As with the previous `reload` task, at the moment I'm not interested in
exposing any of those options for the `tdd` task.

### Test-cljs task options

The following is the `cljs-test` task's help text:

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
  -u, --update-fs?           Only if this is set does the next task's filset include
                                  and generated or compiled cljs from the tests.
  -x, --exit?                Exit immediately with reporter's exit code.
```

Now, here are a bunch of interesting task options we wish to expose for the `tdd` task:

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
Run clojure.test tests in a pod. Throws on test errors or failures.

The --namespaces option specifies the namespaces to test. The default is to
run tests in all namespaces found in the project.

The --exclusions option specifies the namespaces to exclude from testing.

The --filters option specifies Clojure expressions that are evaluated with %
bound to a Var in a namespace under test. All must evaluate to true for a Var
to be considered for testing by clojure.test/test-vars.

The --junit-output-to option specifies the path to a directory relative to the
target directory where a junit xml file for each test namespace will be
generated by using the clojure.test.junit facility. When present it will make
the target to be synced even when there are test errors or failures

Options:
  -h, --help                      Print this help info.
  -c, --clojure VERSION           VERSION sets the version of Clojure for testing.
  -n, --namespaces NAMESPACE      Conj NAMESPACE onto the set of namespace symbols to run tests in.
  -e, --exclusions NAMESPACE      Conj NAMESPACE onto the set of namespace symbols to be excluded from test.
  -f, --filters EXPR              Conj EXPR onto the set of expressions to use to filter namespaces.
  -X, --exclude REGEX             REGEX sets the filter for excluded namespaces.
  -I, --include REGEX             REGEX sets the filter for included namespaces.
  -r, --requires REQUIRES         Conj REQUIRES onto extra namespaces to pre-load into the pool of test pods for speed.
  -s, --shutdown FN               Conj FN onto functions to be called prior to pod shutdown
  -S, --startup FN                Conj FN onto functions to be called at pod startup
  -j, --junit-output-to JUNITOUT  JUNITOUT sets the directory where a junit formatted report will be generated for each ns.

```

For the `tdd` task, at the moment we are interested only in the `-n,
--namespaces NAMESPACE` option. Note that the `test` task's
default behavior when you do not specify a namespace with the `-n`
option is different from that of the corresponding `test-cljs` task.
While `test-cljs` will run only namespaces containing
tests, `test` would run any project's namespaces, without filtering
out the ones not containing tests.

## Task options list for tdd

To summarize, this is the list of the candidate options tasks we'd
like to expose for the `tdd` task as its options:

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

There are a total of 8 new task options to be exposed for the `tdd` task. Two of
them, namely `-k` and `-v`, are
[flags (boolean options)](https://github.com/boot-clj/boot/wiki/Task-Options-DSL#flags). You
recognize this kind of option from the fact that they do not have an
optarg like the others (e.g., `PORT`, `VAL`, `NS`, `EXPR`, etc.).

## Dummy task again

Let's see how a boolean task option works by exploiting the `dummy`
task again.

```clj
(deftask dummy
  "A dummy task"
  [t dirs PATH #{str} ":source-paths"
   v verbose bool "print which files have changed"
   k httpkit bool "use http-kit web server instead of jetty"]
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
     (serve :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true
            :httpkit httpkit)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file "main.js"
                :js-env :phantom
                :namespaces '#{modern-cljs.shopping.validators-test}
                :update-fs? true)
     (test :namespaces '#{modern-cljs.shopping.validators-test})
     (target :dir #{"target"}))))
```

Note that this time we just passed down the boolean options as read
from the [DSL][3] machinery. This is because in a boolean context `nil` is
equivalent to `false`.

Let's now see `-k` and `-v` at work:

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

This is the first time we have explicitly used the `http-kit` asynchronous
web server and as you see the `tdd` task first downloaded and then
started it.

> NOTE 3: Actually the `reload` tasks internally uses `http-kit` to
> establish a websocket connection with the browser, but it uses an
> older release.

The `-v` option correctly instructed the `watch` subtask
to set its mode to `verbose`. If you now modify one of the watched
file (e.g., `modern_cljs/shopping/validators_test.cljc`), you'll see
the `watch` verbose option at work:

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
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

Now we are again using [jetty](http://www.eclipse.org/jetty/), and we don't have a
verbose report from the `watch` subtask.  To proceed with the next
step, stop the `boot` process.

## Add a web server port

The `-p` option is very easy to use as well.

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

I will leave it to you to verify the working of the `port` option
(note the `int` type hint) when starting the `tdd` task.  Before
proceeding to the next step, stop the `boot` process.

## Add CLJS compilation options

As we saw in a previous paragraph, the `cljs-test` subtask has a
couple of compiler options we're interested in:

* `-o, --out-file VAL` to set name of the JS output file generated by
  the CLJS compiler
* `-O, --optimizations LEVEL` to set the compiler optimization options
  (i.e., `none`, `whitespace`, `simple` and `advanced`).

Another option is the `-e, --js-env VAL` option to choose the JS engine
to run the tests with. We'll leave the `-n, --namespaces NS` option to for
later, because this option is critical.

We're not going to explain the details of those options, because they work
like the previous ones. Here is the updated `tdd` task definition:

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
  (let [dirs        (or dirs #{"test/cljc" "test/clj" "test/cljs"})
        output-to   (or output-to "main.js")
        testbed     (or testbed :phantom)]
    (comp
     (serve :handler        'modern-cljs.core/app
            :resource-root  "target"
            :reload         true
            :httpkit        httpkit
            :port           port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file       output-to
                :js-env         testbed
                :namespaces     '#{modern-cljs.shopping.validators-test}
                :update-fs?     true
                :optimizations  optimizations)
     (test :namespaces '#{modern-cljs.shopping.validators-test})
     (target :dir #{"target"}))))
```

Please note how we exploited the idiomatic way of using the `let`
and `or` forms to set default arguments for the `dirs`, `output-to` and
the `testbed` options.

You may now test the `tdd` task experiment with the various options
at the command line.

Below we call the `tdd` task without any options,
to demonstrate that we can still use the simplest form of the command.

```bash
boot tdd
...
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

Before proceeding to the next step, stop the `boot` process.

## A short digression on CLJS compiler optimizations

We have not yet talked much about CLJS compiler
optimizations. Not long ago, the CLJS compiler optimization
management was cumbersome. The most annoying optimization option to use
was `none`, because it required us to explicitly link in
a bunch of Google Closure JS libs in the HTML pages. At the same time
the `none` optimization was also the fastest option for compiling CLJS
code into JS code, which was a bit contradictory.

Moreover, the `source-map` option was not available until recently.
Before that, debugging CLJS code by setting breakpoints in the
generated JS code quickly became a [PITA][4].

These incidental complexities are now gone. You still have `none`,
`whitespace`, `simple` and `advanced` optimization modes available, but the CLJS
compiler is
[now able to handle on its own](https://github.com/clojure/clojurescript/wiki/Compiler-Options#main)
the addition of the needed Google Closure Libraries when you set the
optimization mode to `none`.

The `source-map` feature, which is activated by default with the
`none` optimization mode, can also be set for the other optimization modes.
As a result, debugging CLJS code using the
development tools available in your browser is now as simple as with plain JS code.

The best description of CLJS compiler optimization modes I have found
is the one available in the `cljs` help:

```bash
boot cljs -h
...
Available --optimization levels (default 'none'):

* none         No optimizations. Bypass the Closure compiler completely.
* whitespace   Remove comments, unnecessary whitespace, and punctuation.
* simple       Whitespace + local variable and function parameter renaming.
* advanced     Simple + aggressive renaming, inlining, dead code elimination.

Source maps can be enabled via the --source-map flag. This provides information the
browser needs to map locations in the compiled JavaScript to the corresponding
locations in the original ClojureScript source files.
...
```

The most intriguing compiler optimization mode (or level) is the
`advanced` one. Do you remember, in
[Tutorial 6 - The Easy Made Complex and the Simple Made Easy,](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-06.md)
we introduced the `:export` metadata to allow the `init` function to
be called from JS scripts in an HTML page? The reason for the
`:export` metadata (i.e., `^`) was to protect the `init` function from
being aggressively renamed by the CLJS compiler when using the `advanced`
optimization mode.

The work performed by the Google Closure Compiler with
`advanced` optimizations is quite awesome by itself. Recently, a new
[`modules` option](https://github.com/clojure/clojurescript/wiki/Compiler-Options#modules)
has been added, which breaks up the generated JS file into multiple small pieces,
so that Single Page Applications (SPA) are able to download only
the parts of the resulting JS code required by a given application.

We're not going to explain right now how this new `modules` option works, but
we'll come back to it in a later tutorial.

For the moment, we suggest that you use the `-O` flag to test
the various CLJS compiler optimizations for the `tdd` task:

```bash
boot tdd -O whitespace
...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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
and look at the size of the `main.js` file in the `target`
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
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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
as expected. Now check the size of the `main.js` file generated
using `advanced` optimization:

```bash
ls -lah target/main.js
-rw-r--r--  1 mimmo  staff   387K Dec 15 00:34 target/main.js
```

Less the 400K, included the `cljs.test` lib and the tests themselves.
And we haven't even compressed the file via gzip yet. Not bad!

Before proceeding with the next step, stop the `boot` process.

## Test Namespaces

We expressly left the `-n, --namespace NAMESPACE` option as the last
to be examined. This option occurs in both the `test` task, specific
to CLJ, and the `test-cljs` task, specific to CLJS.

Let's review the help text for both the `test` and the `test-cljs` tasks:

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

As we noted previously, if you do not specify one or more test namespaces,
their behavior is different, and is something we have to live with
until the `test` task is eventually updated to align with the `test-cljs` task.

Although their behavior is not exactly the same, the two tasks both `conj` the
optional test namespaces onto the original set of testing namespaces.

This is easily verified with the task option as follows:

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
     (test :namespaces namespaces)
     (target :dir #{"target"}))))
```

Let's see if this simple solution works by calling the `tdd` task with the
portable `modern-cljs.shopping.validators-test` test namespace and
then without the `-n` option:


```bash
boot tdd -n modern-cljs.shopping.validators-test
...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

This test, aside form the time it takes for the very first CLJS
compilation, worked like a charm. Now stop the `boot` process and
restart it without passing to it any options:

```bash
boot tdd
Starting reload server on ws://localhost:50274
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2015-12-23 15:56:34.780:INFO::clojure-agent-send-off-pool-0: Logging initialized @10971ms
2015-12-23 15:56:40.121:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2015-12-23 15:56:40.165:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@6cd12d4{HTTP/1.1}{0.0.0.0:3000}
2015-12-23 15:56:40.167:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @16359ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

nREPL server started on port 50275 on host 127.0.0.1 - nrepl://127.0.0.1:50275
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.core

Testing modern-cljs.login

Testing modern-cljs.login.validators

Testing modern-cljs.remotes

Testing modern-cljs.shopping.validators

Testing modern-cljs.shopping.validators-test

Testing modern-cljs.templates.shopping

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 33.700 sec
```

As you see, the different behavior of `test-cljs` and `test` when
called without specifying any namespace is now evident: the `test`
task is wasting time in running tests even in namespaces which do not
contain any test.

Let's now see what happens if you create a failing test in the
`validators-test` namespace as we've done before:

```clj
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty" "Quantity has to be an integer number" "Quantity can't be negative"]}))

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.

Testing modern-cljs.core

Testing modern-cljs.login

Testing modern-cljs.login.validators

Testing modern-cljs.remotes

Testing modern-cljs.shopping.validators

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

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 1, :pass 12, :fail 1, :error 0, :type :summary}
                             clojure.core/ex-info            core.clj: 4593
                adzerk.boot-test/eval548/fn/fn/fn       boot_test.clj:   73
 crisptrutski.boot-cljs-test/return-fileset/fn/fn  boot_cljs_test.clj:  113
     crisptrutski.boot-cljs-test/eval689/fn/fn/fn  boot_cljs_test.clj:   98
                adzerk.boot-cljs/eval267/fn/fn/fn       boot_cljs.clj:  200
                adzerk.boot-cljs/eval225/fn/fn/fn       boot_cljs.clj:  134
     crisptrutski.boot-cljs-test/eval660/fn/fn/fn  boot_cljs_test.clj:   66
crisptrutski.boot-cljs-test/capture-fileset/fn/fn  boot_cljs_test.clj:  106
           adzerk.boot-cljs-repl/eval490/fn/fn/fn  boot_cljs_repl.clj:  171
                   boot.task.built-in/fn/fn/fn/fn        built_in.clj:  284
                   boot.task.built-in/fn/fn/fn/fn        built_in.clj:  281
           adzerk.boot-reload/eval390/fn/fn/fn/fn     boot_reload.clj:  120
              adzerk.boot-reload/eval390/fn/fn/fn     boot_reload.clj:  119
             boot.task.built-in/fn/fn/fn/fn/fn/fn        built_in.clj:  233
                boot.task.built-in/fn/fn/fn/fn/fn        built_in.clj:  233
                   boot.task.built-in/fn/fn/fn/fn        built_in.clj:  230
              pandeiro.boot-http/eval313/fn/fn/fn       boot_http.clj:   83
                              boot.core/run-tasks            core.clj:  701
                                boot.core/boot/fn            core.clj:  711
              clojure.core/binding-conveyor-fn/fn            core.clj: 1916
                                              ...
Elapsed time: 9.093 sec
```

We see the same behavior. The `test-cljs` worked as expected, while `test`
examined all of the project's namespaces.

Correct the failing test and you'll again see the same behavior. Now,
let's see if the current `tdd` configuration is able to
manage a new test namespace while it's running.

Create a new `validators_test.cljc` portable (i.e., `.cljc`) file in
the `test/cljc/modern_cljs/login` test directory. Define a very simple
unit test with a single assertion for the `user-credential-errors`
validator we defined in a
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

As soon as you save the file the test machinery get triggered.

As you see, the `test` machinery behavior is still unacceptable from a
performance point of view, but at least the newly defined unit test
for the `login` form validator got seen and correctly evaluated by
both the CLJ and CLJS engines.

## Give up?

I am not a [TDD][6] practitioner, but if I were, I would consider it
unacceptable to be forced to restart the development environment every
time I added a new unit test file. Unfortunately, this is what the
`tdd` task requires if we launch it by specifying the initial test
namespaces using the `-n` option. I would also find it unacceptable
waiting so long for test results, as currently occurs when the `test`
behavior of `tdd` is called without specifying any test namespaces to
run.

For the moment, we have to accept a tradeoff:

* Be explicit with the test namespaces to run by
  specifying them on the command line with the `-n`
* Stop and restart the `tdd` task when you need to add a new test namespace.

```bash
boot tdd -n modern-cljs.shopping.validators-test -n modern-cljs.login.validators-test
...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

With `boot` still running, let's add more assertions to the only test
defined in the `modern-cljs.login.validators-test` namespace. Open the
corresponding file `test/cljc/modern_cljs/login/validators_test.cljc`
and add a few assertions to the `user-credential-errors-test`
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

When you save the file, `tdd` will recompile the file and rerun the
tests, counting 38 assertions in 2 tests each.

## Specific CLJ test in a portable test namespace

Let's now add a CLJ-only test to the portable test-namespace; i.e. the
test that an email address has a valid domain:

```clj
(ns modern-cljs.login.validators-test
  (:require [modern-cljs.login.validators :as v :refer [user-credential-errors]]
            #?( :clj   [clojure.test  :refer         [deftest are testing]]
                :cljs  [cljs.test     :refer-macros  [deftest are testing]] )))

#?( :clj  (deftest email-domain-errors-test
            (testing "Email domain existence"
              (are [expected actual] (= expected actual)
                "The domain of the email doesn't exist."
                (first (:email (v/email-domain-errors "me@googlenospam.com")))))))
```

In order to continue to share the `modern-cljs.login.validators`
namespace between CLJ and CLJS, we added the `v` alias. This way we
can call the `email-domain-errors` function which is defined in the
`modern-cljs.login.validators` portable namespace for CLJ only. Of
course, we also had to use the `#?` reader conditional since this test
only works on the JVM.

Here is the result in the running `tdd` task:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
• main.js
Running cljs tests...

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

The CLJS test namespaces still have 2 tests containing 38 assertions,
while the the CLJ test namespaces now have 3 tests containing 39
assertions.

To proceed to the next step, stop any related `boot` process.

## Code clean up

We are almost done. There is one more thing I would like to do:
cleaning up the `tdd` code by introducing a global map for all the
defaults we used, and adding the above two test-namespaces as defaults
so that the `tdd` task can be called without specifying any command
line options.

Here is the reworked `build.boot` file:

```clj
(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 :resource-paths #{"html"}

 :dependencies '[ [org.clojure/clojure                 "1.8.0"]            ; add CLJ
                  [org.clojure/clojurescript           "1.9.473"]          ; add CLJS
                  [org.clojure/tools.nrepl             "0.2.12"]           ; needed by bREPL
                  [adzerk/boot-cljs                    "1.7.228-2"]
                  [adzerk/boot-test                    "1.2.0"]
                  [adzerk/boot-reload                  "0.5.1"]
                  [adzerk/boot-cljs-repl               "0.3.0"]            ; add bREPL
                  [com.cemerick/piggieback             "0.2.1"]            ; needed by bREPL
                  [compojure                           "1.5.2"]            ; for routing
                  [crisptrutski/boot-cljs-test         "0.2.1-SNAPSHOT"]
                  [enlive                              "1.1.6"]
                  [hiccups                             "0.3.0"]
                  [javax.servlet/javax.servlet-api     "3.1.0"]
                  [pandeiro/boot-http                  "0.7.6"]
                  [weasel                              "0.7.0"]            ; needed by bREPL

                  [org.clojars.magomimmo/domina                     "2.0.0-SNAPSHOT"]
                  [org.clojars.magomimmo/valip                      "0.4.0-SNAPSHOT"]
                  [org.clojars.magomimmo/shoreleave-remote-ring     "0.3.3"]
                  [org.clojars.magomimmo/shoreleave-remote          "0.3.1"] ] )

(require '[adzerk.boot-cljs               :refer [cljs]]
         '[adzerk.boot-reload             :refer [reload]]
         '[adzerk.boot-cljs-repl          :refer [cljs-repl start-repl]]
         '[adzerk.boot-test               :refer [test]]
         '[crisptrutski.boot-cljs-test    :refer [test-cljs]]
         '[pandeiro.boot-http             :refer [serve]] )

(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :output-to "main.js"
               :testbed :phantom
               :namespaces '#{modern-cljs.shopping.validators-test
                              modern-cljs.login.validators-test}} )

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
  (let [dirs        (or dirs (:test-dirs defaults))
        output-to   (or output-to (:output-to defaults))
        testbed     (or testbed (:testbed defaults))
        namespaces  (or namespaces (:namespaces defaults))]
    (comp
     (serve :handler 'modern-cljs.core/app
            :resource-root "target"
            :reload true
            :httpkit httpkit
            :port port)
     (add-source-paths :dirs dirs)
     (watch :verbose verbose)
     (reload :ws-host "localhost")
     (cljs-repl)
     (test-cljs :out-file output-to
                :js-env testbed
                :namespaces namespaces
                :update-fs? true
                :optimizations optimizations)
     (test :namespaces namespaces)
     (target :dir #{"target"}))))

(deftask dev
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :handler 'modern-cljs.core/app    ; ring handler
          :resource-root "target"           ; root classpath
          :reload true)                     ; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

Note that we used a map to set the defaults for the various options:

```clj
(def defaults {:test-dirs #{"test/cljc" "test/clj" "test/cljs"}
               :output-to "main.js"
               :testbed :phantom
               :namespaces '#{modern-cljs.shopping.validators-test
                              modern-cljs.login.validators-test}})
```

We also set default values using `let` and `or`:

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
  (let [dirs        (or dirs (:test-dirs defaults))
        output-to   (or output-to (:output-to defaults))
        testbed     (or testbed (:testbed defaults))
        namespaces  (or namespaces (:namespaces defaults))]
    (comp
     (...)))
```

There is one last thing to consider. The time taken by the `tdd` task
to run the unit tests may be judged unacceptably long by a strict [TDD][6]
practitioner, since it requires more than 1-10 seconds. In the `tdd`
task, most of the time is spent in the `test` task to process
namespaces which don't contain any tests, and also internally in the
`test-cljs` task which starts a new instance of PhantomJS every time
it runs.  We're not going to worry about solving these problems in
this tutorial, but we can remember these points for future
improvements.

That's it for now. Stop any `boot` related process and reset the git
branch.

```bash
git reset --hard
```

# [Next Step - Tutorial 17 - REPLing with Enlive][2]

In the [next tutorial][2] we're going to integrate the validators for
the Shopping Calculator into the corresponding [WUI][5]
in such a way that the user will be notified with the
corresponding help messages when they enter invalid values in
the form.

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-17.md
[3]: https://en.wikipedia.org/wiki/Domain-specific_language
[4]: https://en.wiktionary.org/wiki/pain_in_the_ass
[5]: https://en.wikipedia.org/wiki/User_interface#Types
[6]: https://en.wikipedia.org/wiki/Test-driven_development
