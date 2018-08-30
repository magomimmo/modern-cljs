# Tutorial 15 - Better Safe Than Sorry (Part 3)

In the [previous tutorial][1] we introduced the CLJ/CLJS standard way
to unit test namespaces. To reach that result we dynamically
altered the `boot` runtime environment.

In this tutorial we start by freezing that needed change in the `build.boot`
file. Then we go on to configure a development environment
that allows a single JVM to simultaneously satisfy the Immediate Feedback
Principle by Bret Victor and enable [Test Driven Development (TDD)][9].

## Preamble

To start working from the end of the previous tutorial, assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-14
```

## Introduction

In the [previous tutorial][1], we added a few assertions to unit test the
`modern-cljs.shopping.validators` namespace. We then exercised those
assertions by manually calling `run-tests` from the CLJ REPL and the
CLJS bREPL as well.

As you remember, before running those unit tests we had first to alter
the IFDE runtime environment by adding the `test/cljc` directory to
the `:source-paths` environment variable and then, depending on the
runtime platform of the active REPL, we had to require the appropriate
CLJ/CLJS namespace together with the namespace containing the unit
tests themselves.

Obviously this manual workflow is only acceptable while you're
learning how to introduce unit testing in your CLJ/CLJS mixed project.

In this tutorial, our objective is to progressively
reduce as much as possible the number of manual steps. We'll end up
with a kind of live development environment that will satisfy
Bret Victor's Immediate Feedback Principle and also enable a [TDD][9] workflow.

## Testing task

The first thing we want to eliminate is the need to add the
`test/cljc` directory to the `:source-paths` environment variable each
time we start the IFDE runtime.

Let's create a new task in the `build.boot` configuration file and name it `testing`:

```clj
(deftask testing
  "Add test/cljc for CLJ/CLJS testing purpose"
  []
  (set-env! :source-paths #(conj % "test/cljc"))
  identity)
```

As you can see, this new task, which must be added immediately before the
`dev` task, replicates the same thing we did manually in the previous
tutorial while the IFDE was running. The `identity` function is
returning the task itself in such a way that you can compose it with
other tasks.

### Composing tasks

To see how the newly defined `testing` task can be used along with the `dev`
task, start the IFDE as follows:

```bash
cd /path/to/modern-cljs
boot testing dev
...
Elapsed time: 26.385 sec
```

As you see, to compose the `dev` task with the `testing` one,
we only needed to place the `testing` task before the `dev` one.
This will add the `test/cljc` directory to the `:source/paths` environment
variable, as we desired.

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
boot.user> (pprint (get-env :source-paths))
#{"/Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/41s/6c94bi"
  "/Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/41s/-9b70fr"
  "src/cljs" "test/cljc" "src/cljc" "src/clj"}
nil
```

The `test/cljc` has been correctly added. Now do the same thing
we did in the [previous tutorial][1] by requiring the `clojure.test`
and `modern-cljs.shopping.validators-test` namespaces before
calling `run-tests`:

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

and finally evaluate the `run-tests` function again

```clj
cljs.user=> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
nil
```

Not so bad, but we want more, especially when adopting a [Test Driven Development (TDD)][9] workflow. Before proceeding to the next step, stop
any `boot` related process.

## boot-test task

`boot` does not have a `test` pre-build task like
[Leiningen](https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md#tests)
does. Fortunately, the `boot` community created the [boot-test][10] task
for that job. Let's add it to our `build.boot` file as usual.

```clj
(set-env!
 ...
 :dependencies '[
                 ...
                 [adzerk/boot-test "1.2.0"]
                 ])

(require ...
         '[adzerk.boot-test :refer [test]])
```

As usual take a moment to read the help documentation for this new
task:

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

As you see, there is a `-n` command line option (i.e. `:namespaces`
when used inside `build.boot`) that we could use to specify the namespace to
run tests in.

Let's try it

```bash
boot testing test -n modern-cljs.shopping.validators-test

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
```

It worked. All the assertions of the sole unit test we defined in
`modern-cljs.shopping.validators-test` have been executed
and they all succeeded.

Don't forget that the `boot-test` task is for CLJ only and it has
nothing to offer for CLJS. Later we'll add support for CLJS testing as well.

## CLJ TDD

While we are making good progress, we still want to create a [TDD][9]-style
workflow. Any time you modify your source code the corresponding unit
tests should be executed again.

The composable nature of `boot` tasks is the answer. Do you remember
when at the beginning of this series we introduced the `watch` task
for triggering the CLJS recompilation anytime we modified a CLJS source
file? Here we're going to use `watch` again to automatically run our
unit tests anytime a CLJ file is modified.

```bash
boot testing watch test -n modern-cljs.shopping.validators-test

Starting file watcher (CTRL-C to quit)...


Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 4.244 sec
```

Now modify one of the assertions in the
`test/cljc/modern_cljs/shopping/validators_test.cljc` to force a failure:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0") ;; forced bug
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

> NOTE 2: on Mac OSX sometimes there is strange behavior producing a
> long waiting time before the recompilation is triggered.

As you see, the `watch` task triggered the unit tests to run and
the modified assertion pertaining to the `Happy Path`
failed. Correct it and save the file again.

```bash
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 0.252 sec
```

Even though, for now, we're only dealing with CLJ files, our plan for supporting
the [TDD][9] workflow seems to be working. But we are missing the CLJ REPL experience
offered by IFDE.

Could we have both of them? I have nothing against [TDD][9] approach, but
once you experienced a CLJ/CLJS REPL you'd like to have it available
regardless of development style ([TDD][9] or otherwise).

For the moment, we'll postpone this requirement until
later paragraphs. Indeed, we'd like first to replicate on the CLJS platform
what we have already done for the CLJ platform.

Before proceeding with next step, stop the above `boot` process.

## CLJS TDD

If you want to test CLJS code, sooner or later you will end up
testing the emitted JS in a headless browser. The most famous of them
all is [PhantomJS][2] which is based on [WebKit][3].

### Install PhantomJS

To install PhantomJS follow the [instructions][4] for your Operating
System. On any [*nix][6] OS it should be enough to download the compressed
file, decompress it and add its `bin` directory to the `PATH` environment
variable.

> NOTE 3: I currently use phantomjs 1.9.2. The 2.0 release had some  
> [issues](https://github.com/ariya/phantomjs/issues/12900) on Mac OS X, however
> these have been resolved in the 2.1 release.

## boot-cljs-test

To be able to run CLJS unit tests with the same [TDD][9] workflow we
used for CLJ unit testing, you need to add to `build.boot`
the [boot-cljs-test][11] task specifically devoted for CLJS, which is
compatible with a plethora of JS Engines, including [PhantomJS][2].

The procedure to add a new task is always the same: add it to the
dependencies section of the `build.boot` file and require its exported
namespace by referring the needed symbols as well.

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

Then, the help documentation as usual:

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

At first glance, it looks like we should be interested in two options:

* `-e, --js-env` (i.e. `:js-env` inside `build.boot`), setting the
  JS engine to use for running tests
* `-n, --namespaces` (i.e. `:namespaces` inside `build.boot`),
  setting the namespaces for which unit tests will be run

Knowing a little bit about how `boot`
[fileset](https://github.com/boot-clj/boot/wiki/Filesets) works, we
may be interested in the `-u, --update-fs?` option as well, since
it allows us to populate the `target` directory as `cljs` does.

### Light the fire

We're now ready to light the fire. *[Mutatis mutandis][7]*, we're going to
use the same task composition we've already adopted for the `boot-test`
task when dealing with CLJ unit testing:

```clj
boot testing watch test-cljs -u -e phantom -n modern-cljs.shopping.validators-test

Starting file watcher (CTRL-C to quit)...

Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 15.975 sec
```

Not too shabby. We obtained the same results previously achieved with the
`boot-test` task, which is exactly what we were expecting.

There are few things to be noted.

First, as we learned from the `boot-cljs-test` help documentation, we
used `phantom` as headless browser, and we also passed the
`modern-cljs.shopping.validators-test` as the sole namespace to run
tests for.

Secondly, the `boot-test-cljs` task internally uses the CLJS compiler
by overwriting some default values, as the :main option reported in the warning. Further more, instead of generating
the `main.js` JS file as the `boot-cljs` task did, it generates the
`output.js` JS file.

> NOTE 4
> The warning is no longer printed out from the version 0.3.0 of boot-test-cljs. 
> Nonetheless I prefer to continue to use the old version 0.2.1 since the new one is still facing some [problems][12].

Now repeat the same experiments we did previously by modifying the
unit test assertion in
`test/cljc/modern_cljs/shopping/validators_test.cljc` to verify that `boot` will
recompile any changed files and rerun the unit tests as soon as we save
a file:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0")    ;; forced bug
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))
  ...))
```

When you save the file, the `watch` task triggers the CLJS
recompilation and reruns the sole unit test contained in the
`modern-cljs.shopping.validators-test` namespace. Following is the
reported result:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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
failed because `(validate-shopping-form "" "0" "0" "0")` is now
returning `{:quantity
["Quantity can't be empty." "Quantity has to be an integer number." "Quantity can't be negative."]}`
instead of the expected `nil` value.

Correct the induced bug, save the file and wait for the new report:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
• output.js
Running cljs tests...
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
Elapsed time: 3.906 sec
```

Everything got recompiled and the previously failing assertion is now
passing. Before proceeding to the next step, stop the `boot` process.

## More automation

Even though we've gotten good results with CLJ and CLJS unit
testing and the [TDD][9] workflow, there are still some things
we'd like to improve:

1. We'd like to combine the CLJ/CLJS unit testing into a single `boot`
  command, which means in a single JVM;
1. In turn, we'd like to combine the combined command with the `boot
  dev` command, again to use a single JVM;
1. We'd like to call the `boot` command with some sane defaults to
   reduce its length and the options we have to remember.

Let's start with the first goal.

## TDD task

As we have previously seen, the composable nature of `boot` tasks allows
us to define a new task that is the result of the composition of other
previously defined tasks.

Let's try to define a new `tdd` ([Test Driven Development][9]) task which
combines the `test` and the `cljs-test` tasks.

```clj
(deftask tdd
  "Launch a TDD Environment"
  []
  (comp
   (testing)
   (watch)
   (test-cljs :update-fs? true :js-env :phantom :namespaces '#{modern-cljs.shopping.validators-test})
   (test :namespaces '#{modern-cljs.shopping.validators-test})
   (target :dir #{"target"})))
```

> Note 5: The above task composition mimics the same composition we
> previously created at the command line, and appends the `test` task
> after the `test-cljs` task. The order of the two unit testing tasks
> is important. A subsequent tutorial will better explain the Task
> Options [DSL][8] used by `boot` to
> offer the same options at the command line and as it does
> in the `build.boot` file.

Place the new task definition in `build.boot` after the
`testing` task.

Now run the newly defined `tdd` task:

```bash
cd /path/to/modern-cljs
boot tdd

Starting file watcher (CTRL-C to quit)...

Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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
succeeded on both the CLJS and CLJ platforms.

Now force again a failure for one of the assertions in the
`validators_test.cljs`:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0")      ;; forced bug
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))

  ...))
```

After you save the file you'll receive the following report:

```bash
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

It worked again as expected. Because of the code change in the
`validators_test.cljs` file, the `tdd` task recompiled all the CLJS
source files, ran the CLJS unit test, reporting the failure, and
finally ran the CLJ test reporting the same failure.

Now correct the bug you inserted above. You should see the following
report:

```clj
Writing suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

We again obtained the expected results, and we also addressed the first
item in the above nice-to-have list: a [TDD][9] environment
for both CLJ and CLJS running in the same JVM. Before proceeding with the
next step, stop the `boot` process.

## A single JVM instance, multiple tasks

As we said at the beginning of this series, one of the most attractive
features of the `boot` build tool, compared with `leiningen`,
is its ability to run multiple tasks in the same JVM without
them interfering with each other. We have already unified the CLJ and the CLJS unit
testing tasks. Let's see if we are also able to run the
original `dev` task in the same JVM.

As we saw previously, thanks to the `watch` task, the `test-cljs` task
triggers the CLJS recompilation anytime we modify and save a `.cljs`
or a `.cljc` file.

The question now is the following: could we compose the
`test-cljs` task with the `serve`, `reload` and `cljs-repl` tasks in
the same way that we've already composed them in the `dev` task with the
`cljs` task?

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
  -u, --update-fs?           Only if this is set does the next task's filset include
                                  and generated or compiled cljs from the tests.
  -x, --exit?                Exit immediately with reporter's exit code.
```

That's interesting. Aside form the `-e`, `-n`, `-s` , `-u` and `-x`
options, the remaining `-O`, `-o` and `-c` options seem to deal with
the CLJS compiler options.

At the moment we're only interested in the `-o` (i.e. `:out-file` when
used inside `build.boot`) option, because it is the option for setting
the name of JS file generated by the CLJS compiler. As we previously saw while
playing with the newly defined `tdd` task, the default filename is
`output.js`.

Recall that the default filename generated by the `cljs` task that we
composed in the `dev` task and included in the `html` pages is
`main.js`.

Let's try to rearrange the `tdd` task composition by:

* prepending the `serve` task in the same way we did for the `dev`
  task;
* adding the `reload` task to trigger the reloading of static
  resources as we did for the `dev` task. I added the :ws-host
  to specify the default host for web-socket;
* adding the `cljs-repl` task immediately before the `test-cljs` task
  in the same way we did for the `dev` task;
* passing the `"main.js"` value to the `:out-file` option for the
  `test-cljs` task.

Here is the updated `tdd` task definition. Please substitute it for the
previous one in `build.boot`:

```clj
(deftask tdd
  []
  (comp
   (serve :handler 'modern-cljs.core/app
          :resource-root "target"
          :reload true)
   (testing)
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

### Light the fire

OK, we're ready. Let's see if we were able to build a development
environment that simultaneously satisfies Bret Victor's Immediate
Feedback Principle and enables the [TDD][9] workflow for both
the client and the server code. And, all of that should run
in a single JVM.

```bash
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
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

So far, so good. The web server started and the unit tests have been
executed with success on both the client (i.e. CLJS) and the server
sides (i.e. CLJ).

Now visit the usual
[Shopping Form](http://localhost:3000/shopping.html) and play with it.

> NOTE 6: Remember that even if we already defined and tested the
> validators for both the client and the server sides of the Shopping
> Calculator, we still have to attach them to the form itself. So, if
> you do not follow the happy path you'll get an error anyway.

Everything is still working as expected. Now run the CLJ REPL as
usual:

```bash
# from a new terminal
boot repl -c
...
boot.user>
```

And play with it

```clj
boot.user> (require '[clojure.test :as t]
                    '[modern-cljs.shopping.validators-test])
nil
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
```

Still working.

> NOTE 7: I use emacs+cider (release 0.10.0). It means that I can
> create more `nrepl-client` connections with the running
> `nrepl-server` implicitly started from the above `boot tdd`
> command. I use one nrepl connection for the CLJ REPL, and second
> nrepl connection for the CLJS bREPL without starting any new JVM
> instances. In other words I run everything on a single JVM
> instance. However, when you run the above `boot repl -c`
> command, you're creating a new JVM instance and if you want to start
> a CLJS bREPL while keeping the CLJ REPL in foreground,
> you'll end up with a total of three JVM instance, without counting
> the one eventually created by your Editor/IDE.

> If you are interested in learning about Emacs and CIDER
[here are some resources]
(https://github.com/magomimmo/modern-cljs/blob/master/doc/supplemental-material/emacs-cider-references.md).

Now start the CLJS bREPL

```clj
boot.user> (start-repl)
<< started Weasel server on ws://127.0.0.1:51363 >>
<< waiting for client to connect ... Connection is ws://localhost:51363
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
```

And play with it

```clj
cljs.user> (require '[cljs.test :as t :include-macros true]
                    '[modern-cljs.shopping.validators-test])
nil
cljs.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
WARNING: doo's exit function was not properly set
#object[TypeError TypeError: Cannot read property 'call' of null]
nil
```

Still working, even if we're receiving a warning that regards
[`doo`](https://github.com/bensu/doo), a library internally used by
`test-cljs` to run `cljs.test` on different JS Environment. As a final
confirmation, repeat the kind of forced failures we did before:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

You'll get the same results as in the previous forced failure. Now
correct the forced bug and observe the report again:

```clj
Writing suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
WARNING: Replacing ClojureScript compiler option :main with automatically set value.
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

Even though we've succeeded again, there are still a few things we'd like to
improve.

The `boot tdd` command does not accept any command-line options, so we have to
accept the ones hardwired into the task definition in `build.boot`.

What if we want to compile CLJS with a different optimization level
from the default `none` value? What if we create new namespaces for
testing other portions of the source code? And what if we want to
specifically test non portable CLJ or CLJS code?

For now, stop any `boot` process and reset the git repository

```bash
git reset --hard
```

# [Next Step - Tutorial 16 - On pleasing TDD practitioners][5]

In the [next tutorial][5] we're going to make the `tdd` task more
customizable in order to please [TDD][9] practitioners.

Copyright © Mimmo Cosenza, 2012-2016. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md
[2]: http://phantomjs.org/
[3]: http://en.wikipedia.org/wiki/WebKit
[4]: http://phantomjs.org/download.html
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
[6]: https://en.wikipedia.org/wiki/Unix-like
[7]: https://en.wikipedia.org/wiki/Mutatis_mutandis
[8]: https://en.wikipedia.org/wiki/Domain-specific_language
[9]: https://en.wikipedia.org/wiki/Test-driven_development
[10]: https://github.com/adzerk-oss/boot-test
[11]: https://github.com/crisptrutski/boot-cljs-test
[12]: https://github.com/crisptrutski/boot-cljs-test/issues/54
