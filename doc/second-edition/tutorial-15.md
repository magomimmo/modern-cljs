# Tutorial 15 - Better Safe Than Sorry (Part 3)

In the [previous tutorial][1] we introduced the CLJ/CLJS standard way
for unit testing a namespace. To reach that result we dynamically
altered the `boot` runtime environment. 

In this tutorial we're going to freeze that needed change in the
`boot` building file.

## Preamble

To start working from the end of the previous tutorial, assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-14
```

## Introduction

In the [previous tutorial][1] we added few assertions for unit testing
the `modern-cljs.shopping.validators` namespace and then we exercised
them by manually calling `run-tests` from both CLJ REPL and CLJS bREPL
to run the unit tests containing those assertions. As you remember,
before running the unit tests we had to alter the IFDE runtime
environment to add the `test/cljc` to its `:source-paths` keyword
option and then we had to require the appropriate CLJ/CLJS namespace
together with the namespace containing the unit tests themselves.

Obviously this very manual workflow is only acceptable while you're
learning how to introduce unit testing in your CLJ/CLJS mixed project.

In this tutorial of the series our objective is to progressively
reduce as much as possible the above manual intervention to run unit
tests on both the CLJ and the CLJS platforms.


## Testing task

The first thing we want to eliminate is the need to add the
`test/cljc` directory to the `:source-paths` environment variable any
time we start the IFDE runtime.

Let's create a a new task in the `build.boot` configuration file for
the project.

```clj
(deftask testing
  "Add test/cljc for CLJ/CLJS testing purpouse"
  []
  (set-env! :source-paths #(conj % "test/cljc"))
  identity)
```

As you see this new task, which has to be added immediately before the
`dev`, replicates the same thing we manually did in the previous
tutorial while the IFDE was running. The `identity` function is
returning the task itself in such a way that you can compose with
other tasks.

### Composing tasks

To see how the newly defined `testing` task can be used by composing
it with the `dev` task, start the IFDE as follows:

```bash
cd /path/to/modern-cljs
boot testing dev
...
Elapsed time: 26.385 sec
```

As you see for composing the `dev` task with the `testing` task which
adds the `test/cljc` directory to the `:source/paths` environment
variable, we only placed the `testing` task before the `testing` one.

Let's see if it works like expected by starting the CLJ REPL:

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

Now verify that the `:source-paths` environment variable has been altered by the `testing` task:

```clj
boot.user=> (get-env :source-paths)
#{"/Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/3ap/6c94bi" "/Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/3ap/-9b70fr" "src/cljs" "test/cljc" "src/cljc" "src/clj"}
```

As you see the `test/cljc` has been added to it. Now do the same
things we did in the [previous tutorial][1] by requiring the
`clojure.test` and the `modern-cljs.shopping.validators-test`
namespaces before calling `run-tests`:

```clj
boot.user=> (require '[clojure.test :as t]
                     '[modern-cljs.shopping.validators-test])
nil
boot.user=> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
```

So far, so good. Let's now repeat the same thing in the CLJS
bREPL. First start the bREPL as usual:

```clj
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:49916 >>
<< waiting for client to connect ... Connection is ws://localhost:49916
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

> NOTE 1: remember to visit the
> [shopping URL](http://localhost:3000/shopping.html) to activate the
> bREPL.

Then require the appropriate namespaces

```clj
cljs.user=> (require '[cljs.test :as t :include-macros true]
                     '[modern-cljs.shopping.validators-test :as v])
nil
```

and finally evaluate the `run-test` function again

```clj
cljs.user=> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
nil
```

Not so bad, but you want more, especially when adopting a Test Driven
Development (TDD) methodology. Before proceeding to the next step, stop
any `boot` related process.

## boot-test task

`boot` does not came with a `test` pre-build task like it does
[Leiningen](https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md#tests). Fortunately,
the `boot` community created a `boot-test` task for all of us. Let's
add it our `build.boot` file as usual.

```clj
(set-env!
 ...
 :dependencies '[
                 ...
                 [adzerk/boot-test "1.0.6"]
                 ])

(require ...
         '[adzerk.boot-test :refer [test]])
```

As usual take a moment to get the help documentation for this new task:

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

As you see there is a `-n` command line option we could use to specify
a namespace to be run.

Let's try it

```bash
boot testing test -n modern-cljs.shopping.validators-test

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
```

WOW, it worked. All the assertions of the sole unit test we defined in
the `modern-cljs.shopping.validators-test` namespace have been
executed.

Don't forget that the `boot-test` task is for CLJ only and it has
nothing to offer for CLJS. Later we'll afford CLJS testing as well.

## CLJ TDD

We still want more to cover a typical TDD workflow in which any time
you modify your source code the corresponding tests are executed again
and again.

The composable nature of `boot` tasks is the answer. Do you remember
when at the beginning of this series we introduced the `watch` task
for triggering the CLJS recompilation anytime we modify a CLJS source
code? Here we're going to use it again for automating the execution of
the tests anytime a CLJ file change is saved.

```bash
boot testing watch test -n modern-cljs.shopping.validators-test

Starting file watcher (CTRL-C to quit)...


Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 4.244 sec
```

Now modify one of the assertions in the
`test/cljc/shopping/validators-test.cljc` to generate a failure:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0")
           ...))))
```

As soon as you save the file you'll receive the following failure:

```bash
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:9)
Shopping Form Validation / Happy Path
expected: nil
  actual: {:quantity
           ["Quantity can't be empty."
            "Quantity has to be an integer number."
            "Quantity can't be negative."]}
    diff: + {:quantity
             ["Quantity can't be empty."
              "Quantity has to be an integer number."
              "Quantity can't be negative."]}

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 1, :pass 12, :fail 1, :error 0, :type :summary}
                clojure.core/ex-info       core.clj: 4593
   adzerk.boot-test/eval549/fn/fn/fn  boot_test.clj:   73
boot.task.built-in/fn/fn/fn/fn/fn/fn   built_in.clj:  233
   boot.task.built-in/fn/fn/fn/fn/fn   built_in.clj:  233
      boot.task.built-in/fn/fn/fn/fn   built_in.clj:  230
                 boot.core/run-tasks       core.clj:  701
                   boot.core/boot/fn       core.clj:  711
 clojure.core/binding-conveyor-fn/fn       core.clj: 1916
                                 ...
Elapsed time: 0.473 sec
```

> NOTE 2: on Mac OSX there is strange behavior producing a long waiting
> time before you receive the above result.

As you see the modified assertion pertaining the `Happy Path`
failed. Correct it and save the file again. 

```bash
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 0.252 sec
```

Even if we're now dealing with CLJ only, our tentative for supporting
the TDD workflow seems to work. But we lost the CLJ REPL experience
offered by IFDE.

Could we have both of them? I have nothing against TDD approach, but
once you experienced a CLJ/CLJS REPL you'd like to have it at your
disposal in whichever development environment you code, being it a TDD
environment or not.

We postpone the satisfaction of this requirement for later. At the
moment we'd like first to replicate on the CLJS platform what we
already got on the CLJ once.

Before proceeding with next step, stop the above `boot` process.

## CLJS TDD

If you want to test any CLJS code, sooner or later you end up by
testing the emitted JS on an headless browser. The most famous of them
all is [PhantomJS][7] which is based on [WebKit][8].

### Install phantomjs

To install PhantomJS follow the [instruction][9] for your Operating
System. On any *nix OS it should be enough to download the compressed
file, decompress it and add its `bin` directory to the `PATH` environment
variable.

> NOTE 3: I currently use phantomjs 1.9.2. In my understanding, if you
> want to run the latest 2.0.0 release on Mac OS X you
> [need a workaround](https://github.com/ariya/phantomjs/issues/12900).

### boot-cljs-test

To be able to run CLJS unit tests adopting the same TDD modality we
ended up for CLJ unit testing, you need to add to the `build.boot`
file the `boot-cljs-test` task specifically devoted for CLJS which is
able to use a plethora of JS Engine, being [PhantomJS][7] one of them.

The procedure to add a new task is always the same: add it to the
dependencies section of the `build.boot` file and require the needed
namespace/symbols.


```clj
(set-env!
 ...
 :dependencies '[
                 ...
                 [crisptrutski/boot-cljs-test "0.2.1-SNAPSHOT"]
                 ])

(require ...
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])
```

Then asks for its help documentation as usual:

```bash
cd /path/to/modern-cljs
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

At the moment we're interested in two options:

* `-e`, regarding the engine environment to run tests within
* `-n`, the same as `boot-test`, i.e. the namespaces containing we're
  interested in.

### Light the fire

We're now ready to light the fire. Mutatis Mutandis, we're going to
use the same tasks composition we already adopted for the `boot-test`
task when dealing with CLJ unit testing:

```clj
boot testing watch test-cljs -e phantom -n modern-cljs.shopping.validators-test

Starting file watcher (CTRL-C to quit)...

Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 15.975 sec
```

Great. We obtained the same results previously obtained from the
`boot-test` task, which is exactly what we were expecting.

There are few things to be noted. First, as we learnt form the
`boot-cljs-test` help documentation, we passed the `-e phantom` JS
engine environment we previously installed and the `-n
modern-cljs.shopping.validators-test` namespace containing our unit
tests. Secondly, `boot-test-cljs` internally uses the CLJS compiler
using compiler options defaulted to some value. For example, instead
of generating the `main.js` JS file as the `boot-cljs` did, it
generates the `output.js` JS file.

Now repeat the same experiments we previously did by modifying an
expected result from a unit test assertion in the
`test/cljc/shopping/validators-test.cljc` file to verify if the
process is able to recompile and rerun the tests as soon as we save
the changes.

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0")                ;; quantity is now empty
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))
  ...))
```

When you save the file, the `watch` task triggers the CLJS
recompilation and rerun the sole unit test contained in the
`modern-cljs.shopping.validators-test` namespace. Following is the
obtained result:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty." "Quantity has to be an integer number." "Quantity can't be negative."]}))

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.
Elapsed time: 4.779 sec
```

As you see the `(= nil (validate-shopping-form "" "0" "0" "0"))`
failed, because `(validate-shopping-form "" "0" "0" "0")` is now
returning `{:quantity
["Quantity can't be empty." "Quantity has to be an integer number." "Quantity can't be negative."]}`
instead of the expected `nil` value.

Correct the induced bug, save the file and wait for the new result:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 3.906 sec
```

Great. Everything got recompiled and the previously failed assertion
passed. For proceeding to the next step, stop the `boot` process.

## More automation

Even if we reached a fairly good results with CLJ and CLJS unit
testing approaching the TDD workflow, there are still few things we'd
like to improve:

1. we'like to combine the CLJ/CLJS unit testing in the same `boot`
  command;
1. we'd like to combine the resulted combined `boot` command with the
  `boot dev` command in such a way that we're going to use one JVM
  only for all the tasks;
1. we'd like to call the `boot` command without passing it such a long
  option values;

Let's start from the first item.

## TDD task

As we previously learnt, the composable nature of `boot` tasks allows
to define a new task that is the result of the composition of other
already defined tasks.

Let's try to define a new `tdd` (Test Driven Development) task which combine the `test` and the
`cljs-test` tasks.

```clj
(deftask tdd 
  "Launch a TDD Environment"
  []
  (comp 
   (testing)
   (watch)
   (test-cljs :js-env :phantom :namespaces #{'modern-cljs.shopping.validators-test})
   (test :namespaces #{'modern-cljs.shopping.validators-test})))
```

> Note 4: the above tasks composition first mimics the same
> composition we previously created at the command line and then
> appends the `test` task after the `test-cljs` task. The order of the
> two unit testing task is important.

Place the new task definition in the `build.boot` file after the
definition of the `testing` task.

Now run the newly defined `tdd` task:

```bash
cd /path/to/modern-cljs
boot tdd

Starting file watcher (CTRL-C to quit)...

Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 18.276 sec
```

So far, so good. The `boot tdd` command first compiled any CLJS files,
ran the CLJS unit tests and finally ran on the JVM the same unit tests
contained in the `modern-cljs.shopping.valuators-test` namespace.

The results are exactly the expected ones. All the assertions
succeeded on both CLJS and CLJ platforms. 

Now force again a failure for one of the assertions in the
`validators_test.cljs`:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0")           ;; force a failure
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))

  ...))
```

After you save the file you'll receive the following report:

```bash
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty." "Quantity has to be an integer number." "Quantity can't be negative."]}))

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:9)
Shopping Form Validation / Happy Path
expected: nil
  actual: {:quantity
           ["Quantity can't be empty."
            "Quantity has to be an integer number."
            "Quantity can't be negative."]}
    diff: + {:quantity
             ["Quantity can't be empty."
              "Quantity has to be an integer number."
              "Quantity can't be negative."]}

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 1, :pass 12, :fail 1, :error 0, :type :summary}
                        clojure.core/ex-info            core.clj: 4593
           adzerk.boot-test/eval549/fn/fn/fn       boot_test.clj:   73
crisptrutski.boot-cljs-test/eval651/fn/fn/fn  boot_cljs_test.clj:  109
           adzerk.boot-cljs/eval268/fn/fn/fn       boot_cljs.clj:  200
           adzerk.boot-cljs/eval226/fn/fn/fn       boot_cljs.clj:  134
crisptrutski.boot-cljs-test/eval621/fn/fn/fn  boot_cljs_test.clj:   79
        boot.task.built-in/fn/fn/fn/fn/fn/fn        built_in.clj:  233
           boot.task.built-in/fn/fn/fn/fn/fn        built_in.clj:  233
              boot.task.built-in/fn/fn/fn/fn        built_in.clj:  230
                         boot.core/run-tasks            core.clj:  701
                           boot.core/boot/fn            core.clj:  711
         clojure.core/binding-conveyor-fn/fn            core.clj: 1916
                                         ...
Elapsed time: 2.621 sec
```

It worked again as expected. Because of the code change into the
`validators_test.cljs` file, the `tdd` task recompiled all the CLJS
source files, ran the CLJS unit test, reporting the failure, and
finally ran the CLJ test reporting the same failure.

Now correct the above forced bug. You should receive the following
report:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 2.033 sec
```

Great. We obtained again the expected results and addressed the first
item in the nice-to-have list we started from: we now have a TDD
environment for both CLJ and CLJS in the same JVM. for proceeding with
the next step, stop the `boot` process.

## Approaching TDD

As said at the beginning of the series, one of the most attractive
features of `boot` building tool when compared with `leiningen` is its
potential ability to run any task in the same JVM, being the other to
be able to easily alter the runtime environment while you use it.

Let's see if we are able to align ourselves to those potentialities.

As we previously saw, thanks to the `watch` task, the `test-cljs` task
triggers the CLJS recompilation anytime we modify and save a `.cljs`
or a `.cljc` file.

The rising question is now the following: could we compose the
`test-cljs` task with the `serve`, `reload` and `cljs-repl` tasks in
the same way we composed them in the `dev` task with the `cljs` task?

Let's start by reading the `test-cljs` help documentation again:

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

Uhm, that's interesting. Aside form the `-e`, `-n`, `-s` and `-x`
options, the remaining `-O`, `-o` and `-c` options seem to deal with
the CLJS compiler options.

At the moment we're only interested to the `-o` option, because is the
one setting the name of the JS file generated by the CLJS compiler. As
we previously while playing with the newly defined `tdd` task, the
default filename is `output.js`, while the default filename generated
by the `cljs` task that we composed in the `dev` task and included in
the `html` pages (i.e. `index.html` and `shopping.html`) is `main.js`.

Let's try to rearrange the `tdd` task composition by:

* prepending the `serve` task in the same way we did for the `dev`
  task;
* adding the `reload` task to trigger the reloading of static
  resources as we did for the `dev` task;
* adding the `cljs-repl` task immediately before the `test-cljs` task
  in the same way we did for the `dev` task;
* passing the `"main.js"` value to the `:out-file` option for the
  `test-cljs` task.

Here is the updated `tdd` task definition you have to be substituted
to the previous one in the `build.boot` file:

```clj
(deftask tdd 
  "Launch a TDD Environment"
  []
  (comp
   (serve :dir "target"                                
          :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (testing)
   (watch)
   (reload)
   (cljs-repl)
   (test-cljs :out-file "main.js" 
              :js-env :phantom 
              :namespaces #{'modern-cljs.shopping.validators-test})
   (test :namespaces #{'modern-cljs.shopping.validators-test})))
```

### Light the fire

Here we'are. Let's see if we were able to build a development
environment able to simultaneously satisfy the Bret Victor Immediate
Feedback Principle and the application of the TDD workflow for both
the client and the server code.

``bash
cd /path/to/modern-cljs
boot tdd
Starting reload server on ws://localhost:51346
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2015-12-10 19:19:57.361:INFO::clojure-agent-send-off-pool-0: Logging initialized @14127ms
2015-12-10 19:20:02.689:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2015-12-10 19:20:02.753:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@4079c6c9{HTTP/1.1}{0.0.0.0:3000}
2015-12-10 19:20:02.754:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @19520ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

nREPL server started on port 51347 on host 127.0.0.1 - nrepl://127.0.0.1:51347
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
Elapsed time: 29.060 sec
```

So fa, so good. The web server started and the unit tests have been
executed with success on both the client (i.e. CLJS) and the server sides
(i.e. CLJ).

Now visit the usual
[Shopping Form](http://localhost:3000/shopping.html) and play with it.

> NOTE 4: remember that even if we already have defined and tested the
> validators for both the client and the server sides of the Shopping
> Form, we still have to attach them to form itself.

So fa, so good. Everything is still working as expected.

Now run the CLJ REPL as usual:

```bash
# from a new terminal
boot repl -c
...
boot.user> 
```

And play with it. Still working. So far, so good.

> NOTE 5: I use emacs+cider (release 0.10.0). It means that I can create
> more `nrepl-client` connections with the running `nrepl-server`
> implicitly started from the above `boot tdd` command. I use one
> connection for the CLJ REPL, and another connection for the CLJS bREPL
> without starting any new JVM instance. In other words I run everything
> on a single JVM instance. On the contrary, when you run the above
> `boot repl -c` command, you're creating a new JVM instance and if you
> want to start a CLJS bREPL while keeping the CLJ REPL to play with,
> you'll end up with a total of three JVM instance, without counting the
> one eventually created by your IDE.

Now start the CLJS bREPL.

```clj
boot.user> (start-repl)
<< started Weasel server on ws://127.0.0.1:51363 >>
<< waiting for client to connect ... Connection is ws://localhost:51363
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
```

Still working. So fa, so good.

As a final verification repeat the kind of assertion failures we force
above:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty." "Quantity has to be an integer number." "Quantity can't be negative."]}))

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:9)
Shopping Form Validation / Happy Path
expected: nil
  actual: {:quantity
           ["Quantity can't be empty."
            "Quantity has to be an integer number."
            "Quantity can't be negative."]}
    diff: + {:quantity
             ["Quantity can't be empty."
              "Quantity has to be an integer number."
              "Quantity can't be negative."]}

Ran 1 tests containing 13 assertions.
1 failures, 0 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 1, :pass 12, :fail 1, :error 0, :type :summary}
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
Elapsed time: 6.369 sec
```

You'll got the same results as in the previous forced failure. So fa
so good. Now correct the forced bug and see the results again:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...Unexpected response code: 400

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 3.418 sec
```

Success again. Even if we're very happy with the reached results,
there are few things we still have to satisfy the third requirement of
the list we auto-assigned to ourself....

Stay tuned for the next tutorial.

# Next Step - [Tutorial 17: Enlive by REPLing][21]

In the [next tutorial][21] we're going to integrate the validators for
the Shopping Calculator into the corresponding WUI (Web User
Interface) in such a way that the user will be notified with the right
error messages when she/he types in invalid values in the form.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/cemerick
[4]: https://github.com/cemerick/clojurescript.test
[5]: https://github.com/cemerick/clojurescript.test#why
[6]: https://github.com/cemerick/clojurescript.test#using-with-lein-cljsbuild
[7]: http://phantomjs.org/
[8]: http://en.wikipedia.org/wiki/WebKit
[9]: http://phantomjs.org/download.html
[10]: https://github.com/cemerick/clojurescript.test/blob/0.0.4/runners/phantomjs.js
[11]: https://help.github.com/articles/fork-a-repo
[12]: https://github.com/cemerick/clojurescript.test#usage
[13]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[14]: https://github.com/emezeske
[15]: https://github.com/emezeske/lein-cljsbuild/blob/0.3.2/doc/CROSSOVERS.md#sharing-macros-between-clojure-and-clojurescript
[16]: https://github.com/lynaghk/cljx
[17]: https://github.com/lynaghk
[18]: https://github.com/emezeske/lein-cljsbuild
[19]: https://github.com/technomancy/leiningen
[20]: https://github.com/cgrand/enlive
[21]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-01.md
