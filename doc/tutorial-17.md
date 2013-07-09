# Tutorial 17 - Less is more

In the [previous tutorial][1] we reached an important milestone on our
path towards the elimination of as much as possibile code
duplication. Indeed, we were able to share the same form validation
and corresponding unit testing codebase between the client and
server. Anytime in the future we should need to update the validation
rules, we'll do it in one shared place only and the corresponding unit
tests too.

## Introduction

In this tutorial we're going to integrate the validators for the
Shopping Calculator into the corresponding WUI (Web User Interface) in
such a way that the user will be notified with some error messages when
the she/he types in invalid values in the form.

We have two options. We can start from integrating the validators into
the server-side code and then integrating them into che client-side
code or the opposite.

To show any error message to the user from the server-side, we need to
implement some kind of HTML transformation. To show an error message
to the user from the client-side, we need to implement some kind of
DOM manipulation. I don't know why the people call them
differentely. To me HTML transformation or DOM manipulation express
the same concept.
 



On the server-side we'll need to implement some kind of HTML
transformation. On the client-side we'll need to implement some kind
of DOM manipulation.

We implemented the client-side of the Shopping Calculator by using the
[Domina][2] lib and the server-side by using the [Enilve][3] lib. Wouldn't be great if we could 

Wouldn't be great if we could share the integration code too.



In this tutorial of the series we are going to make movable to the
client-side of the web app even the complementary
`modern-cljs.shopping.validators-test` namespace, which tests the
`modern-cljs.shopping.validators` namespace.

## Repeating the DRY principle

We insisted so many times on adhering to the DRY principle, that I'm
pretty sure you remember we used the [portable version of the Valip][2]
validation lib, because it allows us to share the same validation code
on both sides of a web app.

But what about the unit testing? The server-side test namespace
(i.e. `modern-cljs.shopping.validators-test`), by requiring the
`clojure.test` lib, which depends on the JVM, doesn't allow to be
exploited on the client-side.

As usual, if you search github for the [Chas Emerick][3] repositories
you get a concrete answer to almost everything regarding
CLJ/CLJS. This to says that he recentely wrote the
[clojurescript.test][4] testing lib with the goal of being a maximal
port of `clojure.test` to CLJS.

The motivation he gave in the [Why Paragraph][5] of the lib is the
same we articulated for our `modern-cljs.shopping.validators`
namespace.

> I want to be able to write portable tests to go along with my
> portable Clojure[Script], and clojure.test's model is Good Enough
> (it's better than that, actually). Combine with something like cljx
> or lein-cljsbuild's crossovers to make your ClojureScripting a whole
> lot more pleasant.

## Preparing the field to dance on the border again

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> $ git clone https://github.com/magomimmo/modern-cljs.git
> $ cd modern-cljs
> $ git checkout tutorial-15
> $ git checkout -b tutorial-16-step-1
> ```

The first step, as usual, is to add the `clojurescript.test` lib to the
`:dependencies` section of the `project.clj` file.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ....
  :dependencies [...
                 ...
                 [com.cemerick/clojurescript.test "0.0.4"]]
  ...
  ...
)
```

Chas Emerick even [explained][6] how to use the `clojurescript.test`
lib within `lein-cljsbuild` plugin. So, it seems we're in a good
position to make a step ahead to satisfy my obsession with the DRY
principle.

### Installing Phantoms

If you want to test any CLJS code, sooner or later you end up by
testing the emitted JS on an headless browser. The most famous of them
all is [PhantomJS][7] which is based on [WebKit][8].

To install PhantomJS follow the [instruction][9] for your Operating
System. On any *nix OS it should be enough to download the compressed
file, uncompress it and add its `bin` directory to the `PATH` environment
variable.

### Interfacing lein-cljsbuild with PhantomJS

We now need to interface the `lein-cljsbuild` plugin with the PhantomJS
headless browser binary command. To make PhantomJS launchable from
`lein-cljsbuild`, we have to exploit the `lein-cljsbuild` built-in
support for running external CLJS test.

As always, [Chas Emerick][3] already interfaced PhantomJS for us by
creating a `phantomjs.js` script which we can use for our project by
grabbing it from the [clojurescript.test][4] lib.

Create a new `runners` directory in your `modern-cljs` project
directory.

```bash
$ mkdir runners
```

Now grab the content of the [script][10] and save it as `pahntomjs.js` in
the newly created `runners` directory.

```js
#!/usr/bin/env phantomjs

// reusable phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
p.injectJs(require('system').args[1]);

p.onConsoleMessage = function (x) { console.log(x); };
p.evaluate(function () {
    cemerick.cljs.test.set_print_fn_BANG_(function(x) {
        x = x.replace(/\n/g, "");
        console.log(x);
    });
});

var success = p.evaluate(function () {
  var results = cemerick.cljs.test.run_all_tests();
  console.log(results);
  return cemerick.cljs.test.successful_QMARK_(results);
});

phantom.exit(success ? 0 : 1);
```

> NOTE 2: I have the habit to [fork][11] any repository I use and I
> suggest you to do the same. Sooner or later you can even give an
> help in fixing bugs, correcting the spell/grammar or other minutiae
> by directly using the `GitHub` pull requests facility.

### Instructing lein-cljsbuild about PhantomJS

To instruct `lein-cljsbuild` to launch PhantomJS for testing purpouse we
need to add the built-in `:test-commands` subtask in the `:cljsbuild`
section of the `project.clj` file as follows:

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :cljsbuild {...
              ...
              :test-commands {"phantomjs-whitespace"
                              ["runners/phantomjs.js" "resources/public/js/modern_dbg.js"]

                              "phantomjs-simple"
                              ["runners/phantomjs.js" "resources/public/js/modern_pre.js"]

                              "phantomjs-advanced"
                              ["runners/phantomjs.js" "resources/public/js/modern.js"]}
              ...
              ...
)
```

The value of `:test-commands` is a map in which each key is a name,
and each value is a vector of two elements: the pathname of the
`phantomjs.js` launcher we just grabbed from [Chas Emerick][3] and the
pathname of the JS file to be loaded in the headless browser.

In the `modern-cljs` project we defined three CLJS builds, one for
each optimization option of the GCLS compiler (i.e. `whitespace`,
`simple` and `advanced`).

Each build emits a corresponding JS file (i.e. `modern_dbg.js`,
`modern_pre.js` and `modern.js`). So, we end up with three key/value
pairs to be inserted in the map, one pair for each emitted JS file.

### Instructing lein-cljsbuild about CLJS test directory

By taking a look at the `:builds` section of the `:cljsbuild` task,
you can verify that the emitted `modern_dbg.js` and `modern_pre.js` JS
files are generated by looking for the CLJS code saved in the
`src/cljs` and in the `src/brepl` directories. The `modern.js` JS
file, instead, is emitted by considering the CLJS files from the
`src/cljs` directory only.

In the [previous tutorial][1] we already arranged the `test` directory
to host `cljs` code by creating the `test/cljs` directory, which now
we have to add to the `:source-paths` section of each build as
follows:

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :cljsbuild {...
              ...
              :builds
              {:dev
               {:source-paths [... ... "text/cljs"]
                ...
				...}}

               :pre-prod
               {:source-paths [... ... "text/cljs"]
                ...
				...}}

               :prod
               {:source-paths [... ... "text/cljs"]
                ...
				...}}}})
```

This way, the CLJS/GCLS compilers will include any CLJS test file
living in the `test/cljs` directory in the emitted JS file for each
build.

### Falling in the expression problem again

The first instinct would be now of applying our beloved DRY principle
by just adding the `modern-cljs.shopping.validators-test` namespace to
the `:crossovers` section of the `:cljsbuild` project task, as we
already did for the portable validators we defined in the project

Unfortunately there is an issue. The fact that `clojurescript.test`
lib is a maximal *port* of the `clojure.test` lib on CLJS does not
mean that **it is a portable lib**.

Take a look at the namespace declaration of the [usage sample][12]
included with the `clojurescript.test` lib.

```cljs
(ns cemerick.cljs.test.example
  (:require-macros [cemerick.cljs.test :refer (is deftest with-test run-tests testing)])
  (:require [cemerick.cljs.test :as t]))
```

And compare it with the corresponding declaration of the
`modern-cljs.shopping.validators-test` namespace

```clj
(ns modern-cljs.shopping.validators-test
  (:require [clojure.test :refer [deftest are testing]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))
```

The `:require-macro` keyword is one of the most annoying
[differences between CLJ and CLJS][13].

Because of those requirement differences, the simple addition of the
`modern-cljs.shopping.validators-test` namespace to the `:crossovers`
section of the project it's not sufficient for solving our codebase
duplication issue for the unit testing code.

### Forgetting the DRY principle for a moment

Forget for a moment about the DRY principle and do by hand what the
`lein-cljsbuild` `:crossovers` would have done for us if the
`clojurescript.test` was a portable CLJS/CLJ lib instead of a lib ported
from CLJ to CLJS.

Copy the `validators_test.clj` file from the
`test/clj/modern_cljs/shopping` directory to the corresponding
`test/cljs/modern_cljs/shopping` directory and change its extension form
`.clj` to `cljs`.

```bash
$ cp -R test/clj/modern_cljs/shopping test/cljs/modern_cljs/
$ mv test/cljs/modern_cljs/shopping/validators_test.clj test/cljs/modern_cljs/shopping/validators_test.cljs
```

You'll end up with the following file structure

```bash
$ tree test
test
├── clj
│   └── modern_cljs
│       └── shopping
│           └── validators_test.clj
└── cljs
    └── modern_cljs
        └── shopping
            └── validators_test.cljs

6 directories, 2 files
$
```

Now open the newly copied `validators_test.cljs` file and modify its
namespace declaration by requiring the ported `clojurescript.test` lib
instead of the original `clojure.test` lib.

```clj
(ns modern-cljs.shopping.validators-test
  (:require-macros [cemerick.cljs.test :refer (deftest are testing)])
  (:require [cemerick.cljs.test :as t]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))
```

### Dancing alone on the client-side of the browser

Now cross your fingers and run the following command at the terminal

```bash
$ lein cljsbuild test
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from ["src/brepl" "src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 12.860505 seconds.
Compiling "resources/public/js/modern_dbg.js" from ["src/brepl" "src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern_dbg.js" in 2.31121 seconds.
Compiling "resources/public/js/modern.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 6.609482 seconds.
Running all ClojureScript tests.
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
$
```

Yes, it worked. The `lein cljsbuild test` command compiled all the
builds by including in each emitted JS file the unit tests defined in
the `test/cljs` directory and then it sequentially executed the
`phantomjs-whitespace`, the `phantomjs-simple` and the
`phantomjs-advanced` commands we defined in the `:test-commands`
section of the `:cljsbuild` task. So far so good.

> NOTE 3: If you want to run the tests just for one build do as follows:
> 
> ```bash
> $ lein cljsbuild test phantomjs-whitespace
> Compiling ClojureScript.
> Running ClojureScript test: phantomjs-whitespace
> Testing modern-cljs.shopping.validators-test
> 
> Ran 1 tests containing 13 assertions.
> 
> 0 failures, 0 errors.
> 
> {:fail 0, :pass 13, :test 1, :type :summary, :error 0}
> $
> ```

Now let's see if the the CLJ version of the tests are still working
after the implemented changes.

```bash
$ lein test

lein test modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
$
```

Yes, it's still working. Now force a failure for one of the assertion
in the `validators_test.cljs` file and run again the `lein cljsbuild
test` command to see how it reports the failure.

```clj
(deftest validate-shopping-form-test
  (testing ...
    (testing ...
      (are [... ...] (= ... ...)
           nil (validate-shopping-form "foo" "0" "0" "0") ; force a failure
           ...
           ...))))
```

```bash
$ lein cljsbuild test
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from ["src/brepl" "src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 13.09958 seconds.
Compiling "resources/public/js/modern_dbg.js" from ["src/brepl" "src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern_dbg.js" in 2.344958 seconds.
Compiling "resources/public/js/modern.js" from ["src/cljs" "test/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 6.427312 seconds.
Running all ClojureScript tests.
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)

Shopping Form Validation / Happy Path

expected: (= nil (validate-shopping-form "foo" "0" "0" "0"))

  actual: (not (= nil {:quantity ["Quantity has to be an integer number" "Quantity has to be positive"]}))

Ran 1 tests containing 13 assertions.

1 failures, 0 errors.

{:fail 1, :pass 12, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)

Shopping Form Validation / Happy Path

expected: (= nil (validate-shopping-form "foo" "0" "0" "0"))

  actual: (not (= nil {:quantity ["Quantity has to be an integer number" "Quantity has to be positive"]}))

Ran 1 tests containing 13 assertions.

1 failures, 0 errors.

{:fail 1, :pass 12, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (:)

Shopping Form Validation / Happy Path

expected: (= nil (validate-shopping-form "foo" "0" "0" "0"))

  actual: (not (= nil {:quantity ["Quantity has to be an integer number" "Quantity has to be positive"]}))

Ran 1 tests containing 13 assertions.

1 failures, 0 errors.

{:fail 1, :pass 12, :test 1, :type :summary, :error 0}
Subprocess failed
$
```

It worked again as expected. Because of the code change into the
`validators_test.cljs` file, the `lein cljsbuild test` recompiles all
builds before launching the three `:test-commands` and it finally
reports the assertion error for each of the build.

> NOTE 4: If you followed the suggestion in the previous NOTE 1, I now
> suggest you to commit the changes by issuing the following `git`
> command at the terminal.
>
> ```bash
> $ git add .
> $ git commit -m "step-1 done"
> $ git checkout -b tutorial-16-step-2
> ```

## Don't Repeat Yourself while crossing the border

It's now time to solve the expression problem we worked around by
manually copying and changing the
`test/clj/modern_cljs/shopping/validators_test.clj` file into the
`test/cljs/modern_cljs/shopping/validators_test.cljs` file to test the
`modern-cljs.shopping.validators` namespace on both sides of the
border.

We have two options:

* use the `:crossovers` facility of the `lein-cljsbuild` plugin by
  adding some *Black Magic*;
* use the [cljx][16] lein plugin by [Kevin Lynagh][17].

### No Black Magic

As written by [Evan Mezeske][14] in the [lein-cljsbuild documentation][15]

> In ClojureScript, macros are still written in Clojure, and can not be
> written in the same file as actual ClojureScript code. Also, to use them
> in a ClojureScript namespace, they must be required via :require-macros
> rather than the usual :require.
>
> This makes using the crossover feature to share macros between Clojure
> and ClojureScript a bit difficult, but lein-cljsbuild has some special
> constructs to make it possible.

If you compare again the server-side version of the
`modern-cljs.shopping.validators_test.clj` file with the corresponding
client-side version, you can see that the only detectable variation is
confined in the namespace declaration and it pertains the macros
requirements.

However, if you take a look at the documentation about the [cljx][16]
lein plugin by [Kevin Lynagh][17] plugin, you'll discover that our
scenario perfectly fits with its scope.

> Cljx is a Lein plugin that emits Clojure and ClojureScript code from a
> single metadata-annotated codebase.

My personal opinion about the `:crossovers` option of the
[lein-cljsbuild][18] and the [cljx][16] lein plugin is that the first
is more convenient when you have to deal with **portable** libs
(e.g. [valip][2]), the second is more convenient when you have to deal
with **ported** (e.g. [clojurescript.test][4]).

### Dancing with a chaperone while crossing the border

So, let's dance with [cljx][16]. We start using it by moving the
`modern_cljs` directory from the `test/clj` to a new `test/cljx`
directory and then by renaming the `validators_test.clj` as
`validators_test.cljx` (note the new `.cljx` file extension to denote
annotated-metadata files).

```clj
$ mv test/clj/modern_cljs/ test/cljx
$ mv test/cljx/modern_cljs/shopping/validators_test.clj test/cljx/modern_cljs/shopping/validators_test.cljx
```

Now delete both the `test/clj` and the `test/cljs` directories
because, as we'll see in a moment, we are not going to use them
anymore.

```bash
$ rm -rf test/clj test/cljs
```

We then need to modify the `validators_test.cljx` file by annotating the
namespace declarations with the `^:clj` and the `^:cljs` metadata as
follows:

```clj
^:clj (ns modern-cljs.shopping.validators-test
        (:require [clojure.test :refer [deftest are testing]]
                  [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

^:cljs (ns modern-cljs.shopping.validators-test
         (:require-macros [cemerick.cljs.test :refer (deftest are testing)])
         (:require [cemerick.cljs.test :as t]
                   [modern-cljs.shopping.validators :refer [validate-shopping-form]]))
```

Finally we have to:

* add the [cljx plugin][16] to the project
* configure the `:cljx` task
* consequentely update the `:test-paths` keyword of the project
* consequentely update the `:source-paths` compiler option for each
  CLJS build

Following is the interested code snippet from the `project.clj`

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :test-paths ["target/test/clj"] ;; for CLJ unit test
  ...
  ...
  :plugins [...
            ...
            [com.keminglabs/cljx "0.2.2"]] ;; cljx plugin

  ;; cljx task configuration
  :cljx {:builds [{:source-paths ["test/cljx"] ;; cljx source dir
                   :output-path "target/test/clj" ;; clj output
                   :rules cljx.rules/clj-rules} ;; generation rules

                  {:source-paths ["test/cljx"] ;; cljx source dire
                   :output-path "target/test/cljs" ;; cljs output
                   :extension "cljs" ;; file extension for cljs
                   :rules cljx.rules/cljs-rules}]} ;; generation rules
  ...
  ...
  :cljsbuild {...
              ...
              :builds
              {:dev
               {:source-paths [... ..."target/test/cljs"]
                ...
				...
                }

               :pre-prod
               {:source-paths [... ... "target/test/cljs"]
			    ...
				...
                }

               :prod
               {:source-paths [... "target/test/cljs"]
			    ...
				...
                }}})
```

Let's debrief the newly updated `project.clj` file.

First, we added the `[com.keminglabs/cljx "0.2.2"]` to the `:plugins`
section.

Then, in the `:cljsx` task configuration we decided to save the `cljx`
generated code for CLJ and CLJS respectively under the
`target/test/clj` and `target/test/cljs` directory.

```clj
  :cljx {:builds [{...
                   :output-path "target/test/clj" ;; clj output
                   ...}

                  {...
                   :output-path "target/test/cljs" ;; cljs output
                   ...}]}
```

This way, thanks to the [leiningen][19] `:target-path` option, which
defaults to the `target` directory in the main directory of the
project, the `$ lein clean` command will deleted any `cljx` generated
files.

> NOTE 5: Many thanks to [Chas Emerick][3] for having suggested me this
> smart trick.

Accordingly to the above choice, we had to modify the leiningen
`:test-paths` option with the `["target/text/clj"]` value in such a
way that the `$ lein test` command used to run the CLJ unit tests
knows where to find the generated files.

*Mutatis mutandis*, we had to modify the `:source-paths` option for
each `cljsbuild` build by adding to them the `"target/test/cljs"`
directory. As before, in this way each `cljsbuild` build knows where
to find the CLJS unit testing files generated by `cljx`.

In the `:cljx` task configuration we defined two `cljx` generators
(i.e. `:builds`).

```clj
  :cljx {:builds [{:source-paths ["test/cljx"]
                   :output-path "target/test/clj"
                   :rules cljx.rules/clj-rules}

                  {:source-paths ["test/cljx"]
                   :output-path "target/test/cljs"
                   :extension "cljs"
                   :rules cljx.rules/cljs-rules}]}
```

* The first is configured to generate the CLJ files: the `clj` files
  will be generated into the `target/test/clj` directory starting from
  any `cljx` file in the `test/cljx` directory by applying the
  `cljx.rules/cljs-rules` rule
* The second is configured to generate the CLJS files: the `cljs`
  files will be generated into the `target/test/cljs` directory
  starting from any `cljx` file in the `test/cljx` directory by
  applying the `cljx.rules/cljs-rules` rule

> NOTE 6: It you do not specify the `:extension` option, `cljx` assume the
> `clj` file extension by default.

Both the `cljx.rules/clj-rules` and the `cljx.rules/cljs-rules` rules
have been predefined by `cljx` plugin in the `cljx.rules` namespace
and they remove from the generated code any definition marked
rispectively as `:^cljs` and `^:clj`.

## Let's dance

It's now time to verify that everthing is working as expected.

First, start from a clean codebase.

```bash
$ lein cljsbuild clean
$ lean clean
```

Next we need to generate the `clj` and `cljs` testing files starting
from the shared annotated `validators-test.cljx` file by specifying
the `cljx` task in the `lein` command.

```bash
$ lein cljx once
Rewriting test/cljx to target/test/clj (clj) with 2 rules.
Rewriting test/cljx to target/test/cljs (cljs) with 5 rules.
$
```

> NOTE 7: `cljx` offers both `once` and `auto` subtask (the default is
> `once`) and their behavior is the same of the corresponding `once`
> and `auto` subtask of `cljsbuild`. In `auto` mode any change in any
> `cljx` file will trigger a regeneration of the `clj` and `cljs`
> files.

> NOTE 8: You can even automatically run `cljx` task by adding `:hooks
> [cljx.hooks]` in your `project.clj` file.

You end up with the following structure for the test files

```bash
$ tree target/test
target/test
├── clj
│   └── modern_cljs
│       └── shopping
│           └── validators_test.clj
└── cljs
    └── modern_cljs
        └── shopping
            └── validators_test.cljs

6 directories, 2 files
$
```

We can now launch the CLJS compilation with the usual command.

```bash
$ lein cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from ["src/brepl" "src/cljs" "target/test/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 41.533026 seconds.
Compiling "resources/public/js/modern_dbg.js" from ["src/brepl" "src/cljs" "target/test/cljs"]...
Successfully compiled "resources/public/js/modern_dbg.js" in 9.16067 seconds.
Compiling "resources/public/js/modern.js" from ["src/cljs" "target/test/cljs"]...
Successfully compiled "resources/public/js/modern.js" in 22.609862 seconds.
$
```

### Play and Pray

Finally, cross your finger and issue the commands to run the defined
unit tests for both the sides of the world wide web.

```bash
$ lein test

lein test modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
$
$
$ lein cljsbuild test
Compiling ClojureScript.
Running all ClojureScript tests.
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.

0 failures, 0 errors.

{:fail 0, :pass 13, :test 1, :type :summary, :error 0}
$
```

We reached our goal and we can't be happier! We now have a portable
namespace (i.e. `modern-cljs.shopping.validators`) for the
`shoppingForm` validators and a correponsing portable namespace
(i.e. `modern-cljs.shopping.validators-test`) wich tests it on both
the client and the server side of `modern-cljs` web app.

## Final note

If you decided to follow the suggestion in NOTE 1, you can now commit
your work with the following `git` commands;

```bash
$ git add .
$ git commit -m "step-2 done"
```

Stay tuned for the next tutorial.

# Next - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-15.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/cemerick
[4]: https://github.com/cemerick/clojurescript.test
[5]: https://github.com/cemerick/clojurescript.test#why
[6]: https://github.com/cemerick/clojurescript.test#using-with-lein-cljsbuild
[7]: http://phantomjs.org/
[8]: http://en.wikipedia.org/wiki/WebKit
[9]: http://phantomjs.org/download.html
[10]: https://github.com/cemerick/clojurescript.test/blob/master/runners/phantomjs.js
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
