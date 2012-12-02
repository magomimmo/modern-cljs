# Tutorial 9 - It'a better to be safe than sorry

In this tutorial we're going to learn about testing the CLJ code we
impelemented in the [previous tutorial][8] to patch CLJS compiler.

> NOTE 1: The content of this tutorial is not specifically dedicated to
> CLJS, but few considerations about testing apply to CLJ as well to
> CLJS.

## Introduction

In the last tutorial dedicated to patch CLJS compiler we have updated
three functions (i.e. `compile-dir`, `compile-root` and `cljs-files-in`)
and introduced a new one (i.e. `exclude-file-names`).

All these code changes have been done trying to respect the *interface*
of each original function in such a way that any previous code written
against those functions would still work without generating any errors.

This is the scenario I prefer for testing. A lot of code already uses
the component you have to patch and that code has to continue to run
transparently, that means without errors. At the same time the new added
`:exclude` compilation option has to work as expected, again without
errors.

Easy to promise and impossibile to demonstrate. The interval between the
easiness and the impossibility is the room for testing. You can get
crazy by trying to fill up the entire space, or you can just touch its
surface. Testing is like securing. You spend proportionally to the value
you assign at your sorry.

## Where to start from

In deciding what to test, we have more options.

In the top-down approach you start testing the most *external
interfaces/functions* and eventually dive into more internal functions
to be tested as well.

In the bottom-up apporach you start testing from more *internal
interface/functions* to eventually fly towards more external
functions.

You can even start from functions floating in the middle of the space
and postpone later the decision on flying up or diving down.

## Our bottom

Regarding the patch we did in the previous tutorial, we consider the
bottom being `exclude-file-names`, the only new function we
difined. Its scope is to calculate the set of all the CLJS files to be
escluded from a compilation.

Here are few samples of `exclude-file-names` calls:

```clojure
;;; extreme samples

(exclude-file-names nil nil)
(exclude-file-names nil [])
(exclude-file-names nil [""])
(exclude-file-names nil [" "])
(exclude-file-names nil "")
(exclude-file-names "src/cljs" nil)
(exclude-file-names "src/cljs" [])
(exclude-file-names "src/cljs" [""])
(exclude-file-names "src/cljs" "")
```

What should those calls return? An empty set (i.e. `#{}`), or the not a
value `nil`? My personal opinion is that a `nil` return value is surely
better in all cases in which there is a `nil` argument. But what about
the others samples: `(exclude-file-names "src/cljs" [])` and
`(exclude-file-names "src/cljs" [""])` should return `#{}`;
`(exclude-file-names "src/cljs" "")` should return an error, I believe.


## The repl as a manual testing tool

You con evaluate those calls directly in the repl:

1. be sure, if it's not already been done, to set `$CLOJURESCRIPT_HOME` to
the patched CLJS compiler and to set `$PATH` to
`$CLOJURESCRIPT_HOME/bin`.

```bash
$ export CLOJURESCRIPT_HOME = path/to/modern-cljs/compiler/clojurescript
$ export PATH = $CLOJURESCRIPT_HOME/bin:$PATH
```

2. launch the repl

```bash
$ cd $CLOJURESCRIPT_HOME
$ ./script/repl
Clojure 1.4.0
```

3. require `cljs.compiler` namespace

```clojure
user=> (require '[cljs.compiler :as comp])
nil
user=>
```

3. evaluate the extreme calls

user=> (comp/exclude-file-names nil nil)
#{}
user=> (comp/exclude-file-names nil [])
#{}
user=> (comp/exclude-file-names nil [""])

```

## We were bad

Those answears are not the ones I would expect and the third sample call
put the terminal in non responsive state. Before start hacking the
implementation of `exclude-file-names` function, remember you're risking
to repeat the evaluations of those samples of calls more and more times
in the repl. We need a little bit of automation to not get bored and not
have to remember all the calls on each little implementation
improvement.

## I know, it's boring

The automation of tests is the role of the `clojure.test`
namespace we're going to `require` as `test`. Next, by using `deftest`
and `is` macros we define the tests to be finally executed by
`run-tests`.

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
match our expetations.

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

If you evaluate the new function definition in the repl and call again
`(run-tests)` you'll pass the first test.

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
yourself writing almost the same thing again and again. You start by
cutting and pasting and you finish by having trouble with few typo bugs.

## Being plural

`clojure.tests` namespace contains even the `are` macro which allows you
to group in a single unit all test cases for a function.

Redefine `test-exclude-file-names` in the repl and execute again `(run-tests)`
```clojure
user=> (test/deftest test-exclude-file-names
          (test/are [x y] (= x y)
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
## From what to where

Now that you know enough of `clojure.test` namespace it's time to see
where to put those tests been executed in the repl, if we want to save
them in the file system for the next session.

Here is the structure of the `src` directory of `$CLOJURESCRIPT_HOME`.

![src directory][2]

You need to have the same structure in the `test` directory of
`$CLOJURESCRIPT_HOME` by adding `clj/cljs` as follows:

```bash
$ cd $CLOJURESCRIPT_HOME
$ mkdir -p test/clj/cljs
$
```

## From where to how

Create the `compiler_test.clj` file in the just created `test/clj/cljs`
directory and save it with the following content

```clojure
(ns cljs.compiler-test
  (:require [clojure.test :as t]
            [cljs.compiler :as c]))

(t/deftest test-exclude-file-names
  (t/are [x y] (= x y)
       nil (c/exclude-file-names nil nil)
       nil (c/exclude-file-names nil [])
       nil (c/exclude-file-names nil [""])))
```

Try now to launch a new repl and then require the `cljs.compiler-test`
namespace to launch `run-tests`.

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

Opps, `FileNotFoundException`. That's bad. It means that the java
classpath is not aware of `test/clj` source directory where
`cljs/compiler_test.clj` lives. Take now a look at the `script/repl`
script and you'll discover that `test/clj` is not set in the java
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

## Now play

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

Great. All the test assertions are passed. After a bit of efforts, we
know how to launch the tests from the repl to protect yourself from
becoming sorry for not having done a decent job.

## Go ahaed and stop again

I'm not going to explain every single assertion to test
`exclude-file-names`. But there a lot of them that relate to the new
`:exclude` compilation option we proposed. Sooner or later you start
assering thing in the mutable world of the file system.


# Next step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[2]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/src-dir.png
