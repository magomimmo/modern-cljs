# Tutorial 10 - It'a better to be safe than sorry (Part 2)

In this Part 2 of the tutorial we're going to [continue][1] learning a
little bit more about testing by introducing more features of the
`clojure.test` namespace used to test the patch of CLJS compiler we
proposed in a [previous tutorial][2].

> NOTE 1: Sorry for the content of this tutorial not being specifically
> dedicated to CLJS. But I think that most of the considerations about
> testing are common to both sides of the creek. The series will quickly
> be back on CLJS in the next tutorial.

## Introduction

In the [last tutorial][1] we introduced the need of testing the code
before release it. We started by using `clojure.test` namespace to help
us in predisposing a suite of tests to be repeatedly executed at each
improvement cycle. That tutorial ended at the barier of a mutable world
represented by the structure of CLJS source files and directories on
which to exercise the tests themselves.

## Asserting existance in a mutable world

Let's review the assertions we made in `test-exclude-file-names` test.

```clojure
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

All assertions, but the early three of them, seem to assume the
existance of a `src/cljs` directory in the file system starting from
`$CLOJURESCRIPT_HOME` directory.  The last three seem to assume even the
existance in the file system of `cljs` directory inside the above one.

But what is going to happen in a future release of CLJS compiler if
those directories and files will not exist anymore? Those border
assertions are going pass anyway, because they are true in any possible
world even if they seem to assume the existance of `src/cljs/cljs`
directories. Any other concrete assertion will probably miserably fail,
perhaps months after we set up the tests suite. A foresight worthy of
Cassandra (not to be confused with the notorious no sql db).

## Enter fixture

To address this problem `clojure.test` support the concept of
*fixture*. The primary purpose of fixtures is to mantain a consistent
context on which exercise tests, which means don't let the tests results
depend on an uncontrolled world.

A fixture is an HFO (Higher Order Function) which:

- accepts a function as sole argument;
- the function will be excuted after having predisposed a controlled
  context;
- eventually restore the orginal context after the execution of the passed function.

### Exit fixture

> ATTENTION: I tried more times to use fixtures, but sometimes I observe
> unstable behaviour, so I decided to manage internally to the
> `test-exclude-file-names` function the creation and the desctruction of
> a mutable world of directories and file of CLJS source code.

### Plan the world

Let's start by predisposing the seeds of the mutable world we are going
to create as the context against which evalute the assertions to be
tested.

We're going to define a CLJS source directory and two literal vectors
to represent files and directories contained in it.

Open the `compiler_test.cljs` file from `test/clj/cljs` directory and
edit its content as follows.

```clojure
(ns cljs.compiler-test
  (:require [clojure.test :as t]
            [cljs.compiler :as c]
            [clojure.java.io :as io]))

(def dir-names (atom ["__dir"
                      "__dir/dir1"
                      "__dir/dir2"
                      "__dir/dir2/dir21"]))

(def file-names (atom ["__dir/file1.cljs"
                       "__dir/dir1/file11.cljs"
                       "__dir/dir2/file21.cljs"
                       "__dir/dir2/file22.cljs"
                       "__dir/dir2/dir21/file211.cljs"]))
```

### Create the world

As mentioned above, instead of defining a *fixuture*, we're going to
insert the creation and destruction of the mutable world in the
`test-exclude-file-names` function itself. Before doing that we define
two new functions `create-context` and `clear-context` whose names speck
for themself.

```clojure
(defn create-context []
  (doall (map #(.mkdir (io/file %)) @dir-names))
  (doall (map #(.createNewFile (io/file %)) @file-names)))

(defn clear-context []
  (doall (map #(.delete (io/file %)) @file-names))
  (doall (map #(.delete (io/file %)) (reverse @dir-names))))
```

We used `doall` to force the realization of each element of the involved
sequences (i.e. `@dir-names` and `@file-names`).

### Respect the new mutable world

We configurated the seeds of the mutable world and defined the functions
that are going to create and destroy it after a while. We now need to
insert the two functions in the `test-exclude-file-names` and review the
assertions already wrote.

```clojure
(t/deftest test-exclude-file-names

  ;; create the world
  (create-context)

  ;; execute tests
  (t/are [x y] (= x y)

         ;; border (logical cases)

         nil (c/exclude-file-names nil nil)
         nil (c/exclude-file-names nil [])
         nil (c/exclude-file-names nil [""])
         nil (c/exclude-file-names "__dir" nil)
         #{} (c/exclude-file-names "__dir" [])
         #{} (c/exclude-file-names "__dir" [""])
         #{} (c/exclude-file-names "__dir" ["non_existent_file.cljs"])
         #{} (c/exclude-file-names "__dir" ["non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["non_existent_file.cljs" "non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_file.cljs"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_file.cljs" "cljs/non_existent_directory"]))

  ;; destroy the world
  (clear-context))
```

### Sanity check

We should now verify that everthing is still working as before.

```clojure
% cd $CLOJURESCRIPT_HOME
% ./script/repl
Clojure 1.4.0
Clojure 1.4.0
user=> (require '[clojure.test :as test])
nil
user=> (require '[cljs.compiler-test :as comp])
nil
user=> (test/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

Ran 1 tests containing 12 assertions.
0 failures, 0 errors.
{:type :summary, :pass 12, :test 1, :error 0, :fail 0}
user=>
```

Great, we have not destroyed what was working before the injection of a
mutable world.

## Crossing the border

All the made assertions are going to be true in every possibile mutable
world, even the one in which both `__dir` and `__dir/dir1` do not
exist. It's now time to be less abstract and start asserting facts
regarding the world we frabricated by ourself, a world made of real
directories and files.

Let's start asking few real use cases of exclusion from compilation:

1. exclude a single existent CLJS file (standard)
2. exclude a single directory (standard)
3. exclude a directory and a file which lives in the excluded directory
(border)
4. exclude everything by excluding a file and two directories (border)
5. exclude everything by excluding any file and/or directory which lives
in the main source directory (border).

To convert those use cases in assertions we need some helper vars and
functions to dinamically predispose the expected results of the
`exclude-file-names` calls to be compared with the actual values
calculate by `exclude-file-names`.

Here is the interested code snippet.

```clojure
(def files (map #(io/file %) @file-names))

(def file-paths (map #(str (.getCanonicalPath ^java.io.File (io/file ".")) java.io.File/separator %)
                     @file-names))

(defn get-file-names [coll indeces]
  (map #(nth coll %) indeces))
```

No we can start declaring the assertions relative to the new 5 mentioned
use cases. Here is the `test-exclude-file-names` code.

```clojure
(t/deftest test-exclude-file-names
  (create-context)
  (t/are [x y] (= x y)

         ;; border (logical cases)

         nil (c/exclude-file-names nil nil)
         nil (c/exclude-file-names nil [])
         nil (c/exclude-file-names nil [""])
         nil (c/exclude-file-names "__dir" nil)
         #{} (c/exclude-file-names "__dir" [])
         #{} (c/exclude-file-names "__dir" [""])
         #{} (c/exclude-file-names "__dir" ["non_existent_file.cljs"])
         #{} (c/exclude-file-names "__dir" ["non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["non_existent_file.cljs" "non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_file.cljs"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_directory"])
         #{} (c/exclude-file-names "__dir" ["dir1/non_existent_file.cljs" "cljs/non_existent_directory"])


         ;; real cases


         ;; standard
         ;; exclude a single source file
         (set (get-file-names file-paths [0])) (c/exclude-file-names "__dir" ["file1.cljs"])

         ;; standard
         ;; exclude a single directory containing a directory with a source file and two source files
         (set (get-file-names file-paths [2 3 4])) (c/exclude-file-names "__dir" ["dir2"])

         ;; border
         ;; exclude a directory and a file which happens to be included in the exclude directory
         (set (get-file-names file-paths [4])) (c/exclude-file-names "__dir" ["dir2/dir21" "dir2/dir21/file211.cljs"])

         ;; border
         ;; exclude all files by excluding a file and two directories
         (set file-paths) (c/exclude-file-names "__dir" ["file1.cljs" "dir1" "dir2"])

         ;; border
         ;; exclude all
         (set file-paths) (c/exclude-file-names "__dir" ["."])

         )
  (clear-context))
```

### Sanity check

As usual we know need to exercise again the test to verify that the new
assertions do not fail.

```clojure
% cd $CLOJURESCRIPT_HOME
% ./script/repl
Clojure 1.4.0
user=> (require '[clojure.test :as test])
nil
user=> (require '[cljs.compiler-test :as comp])
nil
user=> (test/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

FAIL in (test-exclude-file-names) (compiler_test.clj:36)
expected: (= (set file-paths) (c/exclude-file-names "__dir" ["."]))
  actual: (not (= #{"/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/file1.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/dir1/file11.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/dir2/file22.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/dir2/file21.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/dir2/dir21/file211.cljs"} #{"/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/./file1.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/./dir1/file11.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/./dir2/file22.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/./dir2/file21.cljs" "/Users/mimmo/tmp/modern-cljs/compiler/clojurescript/__dir/./dir2/dir21/file211.cljs"}))

Ran 1 tests containing 17 assertions.  1 failures, 0 errors.
{:type :summary, :pass 16, :test 1, :error 0, :fail 1} user=>
```

### Fix the bug

Ops, we got a fail on the last assertion, the one that passes `"."` as
the directory to be escluded for meaning everything. The assertion
failed because `exclude-file-names` insert a period `.` in all resulted
filenames to be excluded. Let's fix it. Open `compiler.clj` from
`src/clj/cljs` directory and fix the bug by substituting
`.getCanonicalPath` to .`getAbsolutePath` in the `exclude-file-names`
definition.

```clojure
(defn exclude-file-names [dir exclude-vec]
  "Return a set of canonical paths of files to be excluded"
  (when (and dir (vector? exclude-vec))
    (set (filter #(.endsWith ^String % ".cljs")
                 (map #(.getCanonicalPath ^java.io.File %)
                      (mapcat #(let [file (io/file (str dir) %)]
                                 (when (and (> (count %) 0) (.exists file))
                                   (file-seq file)))
                              exclude-vec))))))
```

### Run test again

Don't kill the repl, do as follow to run the test again.

```clojure
user=> (in-ns 'cljs.compiler)
#<Namespace cljs.compiler>
cljs.compiler=> ;; copy and paste the new definition of exclude-file-names
cljs.compiler=> (defn exclude-file-names [dir exclude-vec]
  "Return a set of absolute paths of files to be excluded"
  (when (and dir (vector? exclude-vec))
    (set (filter #(.endsWith ^String % ".cljs")
                 (map #(.getCanonicalPath ^java.io.File %)
                      (mapcat #(let [file (io/file (str dir) %)]
                                 (when (and (> (count %) 0) (.exists file))
                                   (file-seq file)))
                              exclude-vec))))))

#'cljs.compiler/exclude-file-names
cljs.compiler=> (in-ns 'user)
#<Namespace user>
user=> (test/run-tests 'cljs.compiler-test)

Testing cljs.compiler-test

Ran 1 tests containing 17 assertions.
0 failures, 0 errors.
{:type :summary, :pass 17, :test 1, :error 0, :fail 0}
user=>
```

Great, we passed every single assertion of the test, and if you take a
look at $CLOJURESCRIPT_HOME content you are not going to find any
`__dir` directory. the same world we created has been destryed after
having passed the test.

## Confidence

We're now more confident about `exclude-file-names` behavior even if we
still know it needs some refactoring (i.e. regex). At the moment we're
happy with what we have. Now should be the time to fly up towards the
*external-interfaces* of CLJS compiler. We have more choices: we can
fill bottom-up the entire gap between `exclude-file-names` and `cljsc`
script, passing from `compiler-root` and `compile-dir`; or we could jump
directly to `cljsc` script.

When me, [Federico][3] and [Francesco][4] started to think about the CLJS patch,
we immediately sow that `cljsc.clj`, which is called by `cljsc` script,
needs to be fixed too. We're not going here in discussing the details of
that fix.

We think that we coverefd enough to better undestand why `test` are so
important. In a subsequent tutorial we're going to apply similar
consideration on the client-side (i.e. CLJS and the generated JS).

# Next step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[3]: https://github.com/federico-b
[4]: https://github.com/agofilo
