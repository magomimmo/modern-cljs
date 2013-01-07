# Tutorial 9 - It'a better to be safe than sorry (Part 1)

In this Part 1 of the tutorial we're going to learn a little bit about
testing. We'll use,  as a real case, the CLJ code we impelemented in the
[previous tutorial][1] to patch CLJS compiler. We'll stop when we'll
meet the barier of the mutable world which will be discussed in Part 2
of this tutorial.

> NOTE 1: Sorry for the content of this tutorial not being specifically
> dedicated to CLJS. But I think that most of the considerations about
> testing are common to both sides of the creek. And sorry for having
> broken the content in two parts. It was too long to stay in a single
> unit of my own attention time.

## Introduction

In the last tutorial dedicated to patch CLJS compiler we have updated
three functions (i.e. `compile-dir`, `compile-root` and `cljs-files-in`)
and introduced a new one (i.e. `exclude-file-names`).

All these code changes have been done trying to respect the *interface*
of each original function in such a way that any previous code written
against those functions would still work without generating any errors.

This is the scenario I prefer for testing. A lot of code already uses
the component you have to patch and that code has to continue to run
transparently, that means as expected and without errors. At the same
time, any new feature you added has to work as expected and, again,
without errors.

Easy to promise and impossibile to demonstrate. The interval between the
easiness of promising and the impossibility of demonstrating is the room
for testing. You can get crazy by trying to fill up the entire space, or
you can just touch its surface. Testing is like securing. You spend
proportionally to the value you assign at yourself becoming sorry when
something will eventually go wrong.

## Where to start from

In deciding what to test, we have more options.

In the top-down approach you start testing the most *external
interfaces/functions* and eventually dive into more internal functions
to be tested as well.

In the bottom-up approach you start testing from more *internal
interface/functions* to eventually fly up towards more external
functions.

You can even start from functions floating in the middle of the space
and postpone later the decision on flying up or diving down.

## Our bottom

Regarding the patch we did in the previous tutorial, we consider the
bottom being `exclude-file-names`, the only new function we added in
patching the compiler. Its scope is to calculate the set of all the CLJS
files to be escluded from a build.

I don't know about you, but I always prefer to start from testing
extreme scenarios and next testing more standard use cases. Here are
some extreme `exclude-file-names` sample calls.

```clojure
;;; extreme samples

(exclude-file-names nil nil)
(exclude-file-names nil [])
(exclude-file-names nil [""])
(exclude-file-names nil [" "])
(exclude-file-names "src/cljs" nil)
(exclude-file-names "src/cljs" [])
(exclude-file-names "src/cljs" [""])
(exclude-file-names "src/cljs" "")
```

What should those calls return? An empty set (i.e. `#{}`), or *the not a
value* `nil`? My personal opinion is that a `nil` return value is surely
better in all cases in which there is a `nil` argument. But what about
the others samples: `(exclude-file-names "src/cljs" [])` and
`(exclude-file-names "src/cljs" [""])` should return `#{}`;
`(exclude-file-names "src/cljs" "")` should return an error, I believe.

## The repl as a manual testing tool

The beauty of a repl is that you can interact with those samples.

### Open the door

Be sure, if it's not already been done, to set `$CLOJURESCRIPT_HOME` to
the patched CLJS compiler and to set `$PATH` to
`$CLOJURESCRIPT_HOME/bin`.

```bash
$ export CLOJURESCRIPT_HOME = path/to/modern-cljs/compiler/clojurescript
$ export PATH = $CLOJURESCRIPT_HOME/bin:$PATH
```

### Start the engine

Launch the repl.

```bash
$ cd $CLOJURESCRIPT_HOME
$ ./script/repl
Clojure 1.4.0
```

### Set the double traction factor

Require `cljs.compiler` namespace

```clojure
user=> (require '[cljs.compiler :as comp])
nil
user=>
```

### Gas

Evaluate some extreme calls

```clojure
user=> (comp/exclude-file-names nil nil)
#{}
user=> (comp/exclude-file-names nil [])
#{}
user=> (comp/exclude-file-names nil [""])

```

## We were bad

Those answears are not the ones I would expect. Before start fixing the
implementation of `exclude-file-names` function, remember you're risking
to repeat the evaluations of those samples of calls more and more times
in the repl. We need a little bit of automation to not get bored and not
have to remember all the calls to test on each little improvement cycle.

## I know, it's boring

The definition of tests is the role of the `clojure.test` namespace
which we're going to `require`. Next, by using `deftest` and `is`
macros, we'll define the tests to be finally executed by `run-tests`.

```clojure
user=> (require '[clojure.test :as test])
nil
user=> (test/deftest test-exclude-file-names (test/is (= nil (comp/exclude-file-names nil nil))))
#'user/test-exclude-file-names
user=> (test/run-tests)

Testing user

FAIL in (test-exclude-file-names) (NO_SOURCE_FILE:6)
expected: (= nil (comp/exclude-file-names nil nil))
  actual: (not (= nil #{}))

Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:type :summary, :pass 0, :test 1, :error 0, :fail 1}
user=>
```

As you can see, the test failed because the expected value `nil` is
different from the actual value `#{}`.

## But it could help

A simple variation of the implementation of `exclude-file-names` will
match our early expectations.

```clojure
(defn exclude-file-names
  "Return a set of absolute paths of files to be excluded"
  [dir exclude-vec]
  (when (and dir exclude-vec)
    (set (filter #(.endsWith ^String % ".cljs")
                 (map #(.getAbsolutePath ^java.io.File %)
                      (mapcat #(file-seq (io/file (str dir java.io.File/separator %)))
                              exclude-vec))))))
```

If you now evaluate the new definition in the repl and call
`(run-tests)` again, you'll pass the first test.

```clojure
user=> (in-ns 'cljs.compiler)
#<Namespace cljs.compiler>
cljs.compiler=> (defn exclude-file-names [dir exclude-vec]
                   (when (and dir exclude-vec)
                      (set (filter #(.endsWith ^String % ".cljs")
                                   (map #(.getAbsolutePath ^java.io.File %)
                                        (mapcat #(file-seq (io/file (str dir java.io.File/separator %)))
                                                exclude-vec))))))
#'cljs.compiler/exclude-file-names
cljs.compiler=> (in-ns 'user)
#<Namespace user>
user=> (test/run-tests)

Testing user

Ran 1 tests containing 1 assertions.
0 failures, 0 errors.
{:type :summary, :pass 1, :test 1, :error 0, :fail 0}
user=>
```

## It's still boring

If you're going to cover a good extension of test cases, you'll find
yourself writing almost the same thing again and again. You then start
cutting and pasting around and you probably finish by having trouble
with a bug caused by a wrong cut and/or paste somewhere.

## Being plural

To moderate the boringness, `clojure.tests` namespace defines the `are`
macro which allows you to group, in a single unit, all test cases for a
function by reducing repetitions. We start from three simple assertions:

* `nil` is the expected result of calling `(exclude-file-names nil nil)`
* `nil` is the expected result of calling `(exclude-file-names nil [])`
* `nil` is the expected result of calling `(exclude-file-names nil [""])`

Now, using the `are` macro, redefine `test-exclude-file-names` in the
repl and re-execute `(run-tests)`

```clojure
user=> (test/deftest test-exclude-file-names
          (test/are [result call] (= result call)
             nil (comp/exclude-file-names nil nil)
             nil (comp/exclude-file-names nil [])
             nil (comp/exclude-file-names nil [""])))
#'user/test-exclude-file-names
user=> (test/run-tests)

Testing user

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
{:type :summary, :pass 3, :test 1, :error 0, :fail 0}
user=>
```
## Again and again

Now that you know enough of the `clojure.test` namespace, it's time to
see where to save those tests in the file system in such a way that we
could reuse them again and again.

Here is the structure of the `src` directory of `$CLOJURESCRIPT_HOME`.

![src directory][2]

You need to have this same structure in the `test` directory of
`$CLOJURESCRIPT_HOME` by adding `clj/cljs` as follows:

```bash
$ cd $CLOJURESCRIPT_HOME
$ mkdir -p test/clj/cljs
$
```

Now create the `compiler_test.clj` file in the just created
`test/clj/cljs` directory and save it after having coded the following
content.

```clojure
(ns cljs.compiler-test
  (:require [clojure.test :as t]
            [cljs.compiler :as c]))

(t/deftest test-exclude-file-names
  (t/are [result call] (= result call)
       nil (c/exclude-file-names nil nil)
       nil (c/exclude-file-names nil [])
       nil (c/exclude-file-names nil [""])))
```

> NOTE 1: notice the corrispondance between the `cljs.compiler-test`
> namespace and its file pathname
> `$CLOJURESCRIPT_HOME/src/test/clj/cljs/compiler_test.clj`.

Try now to launch a new repl and then require the `cljs.compiler-test`
namespace to execute `run-tests`.

```bash
$ cd $CLOJURESCRIPT_HOME
$ ./script/repl
Clojure 1.4.0
user=> (require '[cljs.compiler-test :as c])
FileNotFoundException Could not locate cljs/compiler_test__init.class or cljs/compiler_test.clj on classpath: ....
user=> (use 'cljs.compiler-test)
FileNotFoundException Could not locate cljs/compiler_test__init.class or cljs/compiler_test.clj on classpath: ....
user=>
```

## Another patch?

Opps, `FileNotFoundException`. That's bad. It means that the JVM
classpath is not aware of `test/clj` source directory where
`cljs/compiler_test.clj` lives. Take now a look at the `script/repl`
script and you'll discover that `test/clj` is not set in the JVM
classpath. Add it as follows.

```bash
#!/bin/sh

if [ "$CLOJURESCRIPT_HOME" = "" ]; then
  CLOJURESCRIPT_HOME="`dirname $0`/.."
fi

CLJSC_CP=''
for next in lib/*: src/clj: src/cljs: test/cljs: test/clj; do
  CLJSC_CP="${CLJSC_CP}${CLOJURESCRIPT_HOME}/${next}"
done

java -server -cp "$CLJSC_CP" clojure.main
```

## Run the test

You can know retry the run.

```clojure
$ ./script/repl
Clojure 1.4.0
user=> (require '[clojure.test :as t])
nil
user=> (require '[cljs.compiler-test :as c])
nil
user=> (t/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
{:type :summary, :pass 3, :test 1, :error 0, :fail 0}
user=>
```

Great. All the initial test assertions passed. After a bit of efforts we
discovered how to launch the tests in the repl to protect ourselves from
becoming sorry for not having done a decent job.

## Go ahead and stop again

I'm not going to explain every single assertion eventually used to test
`exclude-file-names`, but sooner or later you start asserting things in
the mutable world of the file system.

To test our `exclude-file-names` function on a real scenario we're
initially going to use the CLJS code base itself as our mutable world.

This time we start coding from the test. Open `compile_test.clj` and add some
reasonable assertions to `test-exclude-file-names`.

```clojure
(ns cljs.compiler-test
  (:require [clojure.test :as t]
            [cljs.compiler :as c]))

(t/deftest test-exclude-file-names
  (t/are [x y] (= x y)
       nil (c/exclude-file-names nil nil)
       nil (c/exclude-file-names nil [])
       nil (c/exclude-file-names nil [""])
       nil (c/exclude-file-names "src/cljs" nil)
       #{} (c/exclude-file-names "src/cljs" [])
       #{} (c/exclude-file-names "src/cljs" [""])
       #{} (c/exclude-file-names "src/cljs" ["non_existent_file.cljs"])
       #{} (c/exclude-file-names "src/cljs" ["non_existent_directory"])
       #{} (c/exclude-file-names "src/cljs" ["non_existent_file.cljs" "non_existent_directory"])
       #{} (c/exclude-file-names "src/cljs" ["cljs/non_existent_file.cljs"])
       #{} (c/exclude-file-names "src/cljs" ["cljs/non_existent_directory"])
       #{} (c/exclude-file-names "src/cljs" ["cljs/non_existent_file.cljs" "cljs/non_existent_directory"])
       ))
```

Launch the repl as before (i.e. `$ ./script/repl`) and require both
`clojure.test` and `cljs.compiler-test`. Finally run the tests.

```clojure
Clojure 1.4.0
user=> (require '[clojure.test :as t])
nil
user=> (require '[cljs.compiler-test :as c])
nil
user=> (t/run-tests)

Testing user

Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
{:type :summary, :pass 0, :test 0, :error 0, :fail 0}
user=> (t/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

FAIL in (test-exclude-file-names) (compiler_test.clj:6)
expected: (= #{} (c/exclude-file-names "src/cljs" [""]))
  actual: (not (= #{} #{"/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/nodejs.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/nodejscli.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/browser/event.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/data.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/walk.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/reader.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/core.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/set.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/zip.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/reflect.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/browser/repl.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/browser/dom.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/string.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/core/reducers.cljs" "/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/clojure/browser/net.cljs"}))

FAIL in (test-exclude-file-names) (compiler_test.clj:6)
expected: (= #{} (c/exclude-file-names "src/cljs" ["non_existent_file.cljs"]))
  actual: (not (= #{} #{"/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/non_existent_file.cljs"}))

FAIL in (test-exclude-file-names) (compiler_test.clj:6)
expected: (= #{} (c/exclude-file-names "src/cljs" ["non_existent_file.cljs" "non_existent_directory"]))
  actual: (not (= #{} #{"/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/non_existent_file.cljs"}))

FAIL in (test-exclude-file-names) (compiler_test.clj:6)
expected: (= #{} (c/exclude-file-names "src/cljs" ["cljs/non_existent_file.cljs"]))
  actual: (not (= #{} #{"/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/non_existent_file.cljs"}))

FAIL in (test-exclude-file-names) (compiler_test.clj:6)
expected: (= #{} (c/exclude-file-names "src/cljs" ["cljs/non_existent_file.cljs" "cljs/non_existent_directory"]))
  actual: (not (= #{} #{"/Users/mimmo/Developer/modern-cljs/compiler/clojurescript/src/cljs/cljs/non_existent_file.cljs"}))

Ran 1 tests containing 12 assertions.
5 failures, 0 errors.
{:type :summary, :pass 7, :test 1, :error 0, :fail 5}
user=>
```
## We were really bad

Oh my God, we got 5 fails of 12 assertions. Let's fix them one by one.

The first fix has to do with the failure of `(exclude-file-names
"src/cljs" [""])` which has resulted in a set of files instead of a void
set (i.e. #{}). It depends on the void string `""` being passed around
as a directory name.

All the other fails have to do with non existent files and/or
directories returned by combining and existing directory with a non
existing file or directory.

Here is the code the fix them all. Open `compiler.clj` from
`$CLOJURESCRIPT_HOME/src/clj/cljs` directory and edit the
`exclude-file-names` function as follows.

```clojure
(defn exclude-file-names [dir exclude-vec]
  "Return a set of absolute paths of files to be excluded"
  (when (and dir (vector? exclude-vec))
    (set (filter #(.endsWith ^String % ".cljs")
                 (map #(.getAbsolutePath ^java.io.File %)
                      (mapcat #(let [file (io/file (str dir) %)]
                                 (when (and (> (count %) 0) (.exists file))
                                   (file-seq file)))
                              exclude-vec))))))
```

There is large room for refactoring the code later, for now be happy to
have passed all 12 assertions we defined in `cljs.compiler-test`
namespace as you can verify by yourself.

Open a new repl as usual. Require `cljs.compiler_test` and
`clojure.test`. Finally run the test associated with `compiler-test`
namespace.

```clojure
$ cd $CLOJURESCRIPT_HOME
$ ./script/repl
Clojure 1.4.0
user=> (require '[clojure.test :as t])
nil
user=> (require '[cljs.compiler-test :as c])
nil
user=> (t/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

Ran 1 tests containing 12 assertions.
0 failures, 0 errors.
{:type :summary, :pass 12, :test 1, :error 0, :fail 0}
user=>
```

## Enter the mutable world

Even a not so careful reader should have noted that the assertions
assumed the existance of the "src/cljs" source directory which lives
inside $CLOJURESCRIPT_HOME directory. If you want to go on and creating
new assertions on a mutable world, you start figthing with it. It is
frequently easier to mock it up.

# Next step - It's better to be safe than sorry (Part 2)

In the [next tutorial][3] we're going to finish the work on testing that has
been started here. We'll introduce mocks as a way to manage the
mutability of the file system when asserting facts on it.

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[2]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/src-dir.png
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
