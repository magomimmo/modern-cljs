# Tutorial 14 - Better Safe Than Sorry (Part 2)

To adhere to the progressive enhancement strategy in the
[latest tutorial][1] we introduced [Enlive][2] by
[Christophe Grand][10] and used it to implement the server-side-only
version of the Shopping Calculator. In doing that implementation, we
were forced to refactor the code for two reasons:

* to apply the [DRY principle][3]
* to resolve a cyclic namespaces dependency problem we met on the way.

In this tutorial we're going to introduce *code testing*. 

## Preamble

To start working from the end of the previous tutorial, assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-13
```

## Introduction

Before introducing *code testing* we first want to accomplish a few
other things:

* review the Shopping Form
* break the Shopping Form
* add fields's validations

## Start the IFDE

As usual we first start the IFDE live environment

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 23.261 sec
```

## Review the Shopping Form

Now disable the JavaScript engine of your browser, visit or reload the
[shopping URI][4], and click the `Calculate` button. The `Total` field
is filled with the result of the calculation executed on the
server-side via the "/shopping" action associated to the
`shoppingForm`. That's what we reached at the end of the
[previous tutorial][1]. So far so good.

## Break the Shopping Calculator again and again

Now enter an unexpected value in the form, for example the `foo`, as
the value of the `Price per Unit` field. Finally, click the
`Calculate` button.

You'll receive the infamous `HTTP ERROR: 500` page saying that
`clojure.lang.Symbol` can't be cast to `java.lang.Number`. This is a
`java.lang.ClassCastException` as you can read from the warning
reported at the terminal as well.

```clj
2015-12-05 10:49:28.421:WARN:oejs.HttpChannel:qtp540393068-80: /shopping
java.lang.ClassCastException: clojure.lang.Symbol cannot be cast to java.lang.Number
	at clojure.lang.Numbers.multiply(Numbers.java:148)
	at modern_cljs.remotes$calculate.invoke(remotes.clj:6)
...
	at java.lang.Thread.run(Thread.java:745)
```

That's because the remote `calculate` function accepts only
stringified numbers for its calculation and we're now passing to it a
stringified symbol that can't be casted to a number.

Now let's see what happens if we reactivate the JavaScript engine by
unmarking the `Disable JavaScript` check-box from the Settings of the
`Developer Tools` and reload the
[Shopping Form](http://localhost:3000/shopping.html) URL.

Try again to type `foo` instead of a number in one of the form fields
and click `Calculate` after having reloaded the
[Shopping From](http://localhost:3000/shopping.html) page.

This time, due to the Ajax communication, even though the server
returned the same 500 error code as before, the browser does not show
the same `ERROR PAGE`. The result is not as bad as before, but still
unacceptable for a professional web page.

Before we invest time and effort in unit testing, let's understand
what the Shopping Form lacks?

It needs input validation: both for the server and the client sides,
as usual.

## Shopping Form validation

We already used [Valip][7] lib by [Chas Emerick][8] in the
[Tutorial-12 - Don't Repeat Yourself while crossing the border - ][9]
to validate the `loginForm` fields, and we already know how to apply
the same approach to the `shoppingForm` validation.

We know from the above tutorial that we can share the validation rules
between the server and the client sides by creating a portable
CLJ/CLJS source file with the `.cljc` extension in the `src/cljc`
source directory's structure.

### validators.cljc

Create the directory `shopping` under the `src/cljc/modern_cljs/`
directory to reflect the project structure we already used for the
`login` validation.

```bash
mkdir src/cljc/modern_cljs/shopping
```

In the `shopping` directory now create the file `validators.cljc`
where we're going to define the `validate-shopping-form` function,
which uses the `valip.core` and `valip.predicates` namespaces.

```bash
touch src/cljc/modern_cljs/shopping/validators.cljc
```

To keep things simple, at first we will consider only very basic
validations:

* no input can be empty
* the value of `quantity` has to be a positve integer
* the values of `price`, `tax` and `discount` have to be numbers

Following is the content of the newly created `validators.cljc` file.

```clj
(ns modern-cljs.shopping.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present?
                                      integer-string?
                                      decimal-string?
                                      gt]]))

(defn validate-shopping-form [quantity price tax discount]
  (validate {:quantity quantity :price price :tax tax :discount discount}

            ;; validate presence

            [:quantity present? "Quantity can't be empty"]
            [:price present? "Price can't be empty"]
            [:tax present? "Tax can't be empty"]
            [:discount present? "Discount can't be empty"]

            ;; validate type

            [:quantity integer-string? "Quantity has to be an integer number"]
            [:price decimal-string? "Price has to be a number"]
            [:tax decimal-string? "Tax has to be a number"]
            [:discount decimal-string? "Discount has to be a number"]

            ;; validate range

            [:quantity (gt 0) "Quantity can't be negative"]

            ;; other specific platform validations (not at the moment)

            ))
```

> NOTE 1: At the moment we don't deal with any issues regarding
> internationalization and we're hard-coding the text messages in the
> source code.

As you can see, we defined the `validate-shopping-form` function by
using the `validate` function from the `valip.core` namespace and a
bunch of predicates that [Chas Emerick][8] was so kind to have defined
for us in the `valip.predicates` namespace.

Considering that `valip` is a portable lib, we can immediatly test the
`validate-shopping-form` function at both the CLJ REPL and then at the
CLJS bREPL as well.

### The server side

Let's start the CLJ REPL first:

```bash
# in a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> 
```

and then exercise the newly defined `validate-shopping-form` function
as follows:

```clj
boot.user> (use 'modern-cljs.shopping.validators)
nil
```

```clj
boot.user> (validate-shopping-form "1" "0" "0" "0")
nil
```

```clj
boot.user> (validate-shopping-form "-10" "0" "0" "0")
{:quantity ["Quantity can't be negative"]}
```

```clj
boot.user> (validate-shopping-form "-10" "0" "0" "")
{:discount ["Discount can't be empty" "Discount has to be a number"], :quantity ["Quantity can't be negative"]} 
```

### The magic again

We now want to repeat the magic we already saw at work in a previous
tutorial dedicated to the `loginForm`.

Start the CLJS bREPL from the the CLJ REPL as usual:

```clj
boot.user> (start-repl)
<< started Weasel server on ws://127.0.0.1:49522 >>
<< waiting for client to connect ... Connection is ws://localhost:49522
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user> 
```

and repeat the above manual test procedure:

```clj
cljs.user> (use '[modern-cljs.shopping.validators :only [validate-shopping-form]])
nil
```

> NOTE 2: as you already know, one of the biggest
> [differences](https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces)
> between CLJ and CLJS regards namespaces.

```clj
cljs.user> (validate-shopping-form "1" "0" "0" "0")
nil
```

```clj
cljs.user> (validate-shopping-form "-10" "0" "0" "0")
{:quantity ["Quantity can't be negative"]}
```

```clj cljs.user> (validate-shopping-form "-10" "0" "0" "")
{:discount ["Discount can't be empty" "Discount has to be a number"], :quantity
["Quantity can't be negative"]}
```

WOW, the magic is working again. We defined a single
`validate-shopping-form` function which is able to be used without any
modification on the server and on the client sides as well. Kudos to
everyone who put this magic together.

All the above REPL/bREPL sessions for testing the
`validate-shopping-form` function on both sides of the `Shopping Form`
do not substitute for unit testing, because as soon as we stop
REPL/bREPL all the tests are gone. If we need to repeat them (which we
certainly will), they will have to be manually retyped.

## Unit testing

No programmer likes code repetition. Retyping similar expressions
again and again in the REPL is something that should feel awful to any
of us.

Fortunately, both CLJ and CLJS have to offer a solution for reducing
the above boring activities. Unfortunately, unit testing on CLJ/JVM is
not exactly as unit testing on CLJS/JSVM. The two platforms are
different from one another and they consequently require different
namespaces:

* `clojure.test` namespace for CLJ
* `cljs.test` namespace for CLJS

The `clojure.test` namespace has been implemented well before the
corresponding `cljs.test` namespace and the latter, through a detailed
porting on the JSVM preserved most of the functionalities provided by
the former. So we are still in good position when you want write unit
tests which are applicable to a portable CLJ/CLJS namespace.

That said, most of the `cljs.test` functionalities are provided as
macros, which means that you need the special CLJS `:require-macros`
option in the requirement declaration of a testing namespace, while
you do no need it in the corresponding CLJ namespace declaration.

We already met a case in which, while writing the portable
`modern-cljs.login.validators` namespace, we needed two differentiate
the CLJ/JVM from the CLJS/JSVM platform.

For that case we introduced the use of the `#?` reader literal to be
able to make the `email-domain-errors` validator available on the
CLJ/JVM platform only, even when writing portable code in a `cljc`
source file.

If we want to write a portable testing namespace for the portable
`modern-cljs.shopping.validators` namespace we created above, we have
to use the same `#?` trick to differentiate the different testing
namespace declarations used by CLJ and CLJS.

<ut to here>

So, let's introduce the `clojure.test` and `cljs.test` to automate at
least the need to retype the same tests anytime we change something in
the `validate-shopping-form`.

### Mirroring the project structure

First, we need to prepare the structure of the `test` directory to
reflect the corresponding layout of the project `src` directory.

Replicate the `src` directory structure into the `test` directory and
remove the original `modern_cljs` directory created by the `lein new
modern-cljs` command at the very beginning of the `modern-cljs` series
of tutorials.

```bash
mkdir -p test/{clj,cljs}/modern_cljs
mkdir -p test/clj/modern_cljs/shopping
rm -rf test/modern_cljs
```

You'll end in the following file structure,

```bash
tree test/
test/
├── clj
│   └── modern_cljs
│       └── shopping
└── cljs
    └── modern_cljs
```

### Start testing at the borders

Note that we have created the `shopping` directory where we're going
to add our first unit test: the one aimed at testing the
`modern-cljs.shopping.validators` namespace. The reason why we choose
this namespace to introduce unit testing is because it sits at the
interface of our application to the external world; be it a user to be
notified about her/his mistyped input, or an attacker trying to
leverage any useful information by breaking the Shopping Calculator.

I prefer to talk about testing of namespaces instead of unit testing
of functions. Generally speaking you should unit test the API of each
namespace, which means all the symbols which are not private in the
namespace itself. If you want to unit test a private symbol, you
should write the unit test code in the same file where you privatized
the symbol itself.

### The first unit test

At the moment the `modern-cljs.shopping.validators` namespace contains
only the `validate-shopping-form` function, which is then the one
we're going to unit test.

Create the `validators_test.clj` file in the
`test/clj/modern_cljs/shopping` directory. We could name this file as
we want, but I prefer to adhere to some naming convention to
simplifying the writing and the reading of the tests. When I write a
file for testing a namespace (e.g. `validators`), I name it by
appending `_test` (e.g. `validators_test.clj`) to the file name of the
namespace to be tested (e.g. `validators.clj`).

Following is the initial unit test code that mimics the short REPL
testing session we did previously with the
`modern-cljs.shopping.validators` namespace.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [clojure.test :refer [deftest is]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftest validate-shopping-form-test
  (is (= nil (validate-shopping-form "1" "0" "0" "0")))
  (is (= nil (validate-shopping-form "1" "0.0" "0.0" "0.0")))
  (is (= nil (validate-shopping-form "100" "100.25" "8.25" "123.45"))))
```

We started very simple. First in the
`modern-cljs.shopping.validator-test` namespace declaration we
required the `clojure.test` and the `modern-cljs.shopping.validators`
namespaces.

Then, by using the `deftest` and the `is` macros, we defined our first
unit test named `validate-shopping-form-test`. As before, even if we
are free to name anything as we want, I prefer to adopt some
conventions for facilitating unit test writing and reading. If I want
to test a function named `my-function`, I name the test
`my-function-test`.

The `is` macro allows to make assertions of any arbitrary
expression. The code `(is (= nil (validate-shopping-form "1" "0" "0"
"0")))` is saying that the acutal evaluation of the
`(validate-shopping-form "1" "0" "0" "0")` form is expected to be equal
to `nil` because all the input are valid. At the moment, we're just
testing the working path.

### On getting less bored

You'll get bored very quickly in typing `is` forms. That's way
`clojure.test` includes the `are` macro which allows the programmer to
save some typing. The above group of `is` forms can be reduced to the
following equivalent `are` form:

```clj
(ns modern-cljs.shopping.validators-test
  (:require [clojure.test :refer [deftest are]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftest validate-shopping-form-test
  (are [expected actual] (= expected actual)
       nil (validate-shopping-form "1" "0" "0" "0")
       nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
       nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))
```

In the above example, each `nil` value represents an expected value,
while the evaluation of each `(validate-shopping-form ...)` call
represents the actual value. The `are` macro verifies that for each
pair of an expected and actual values `[expected actual]`, the
assertion `(= expected actual)` is true.

### How to read failure reports

You can even document tests by wrapping your `are` form inside a
`testing` macro, which takes a documentation string followed by any
number of `is/are` assertions.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [clojure.test :refer [deftest are testing]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (are [expected actual] (= expected actual)
         nil (validate-shopping-form "1" "0" "0" "0")
         nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
         nil (validate-shopping-form "100" "100.25" "8.25" "123.45"))))
```

The string "Shopping Form Validation" will be included in failure
reports. Calls to `testing` macros may be nested too to allow better
reporting of failure reports.

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "1" "0" "0" "0")
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))))
```

### Running the test

The easiest way to run the newly-defined tests for the
`modern-cljs.shopping.validators` namespace is to use the `test` task
of leiningen

```bash
lein test
Exception in thread "main" java.io.FileNotFoundException: Could not locate modern_cljs/shopping/validators_test__init.class or modern_cljs/shopping/validators_test.clj on classpath:
...
...
Tests failed.
```

As you can see, the `lein test` command failed because was not able to
find the `validators_test.clj` file in the project `classpath`. To fix
this problem we have to add a `:test-paths` section into the
`project.clj` to reflect the file structure for unit testing we defined
above.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :test-paths ["test/clj"]
  ...
  ...
)
```

Now run again the test and everything should work nicely.

```bash
lein test
Compiling ClojureScript.

lein test modern-cljs.shopping.validators-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
Could not locate test command .
```

> NOTE 2: By having hooked the `cljsbuild` tasks to `lein` task with the
> `:hooks` configuration option, when you run the `lein test` command
> you will receive a `Could not locate test command .` message. This is
> because at the moment there are no unit tests defined for CLJS in the
> `test/cljs` path.

By  default, the `lein test` command executes all the defined tests (at
the moment just one test containing 3 assertions). If you want to run
a specific test you have to pass its namespace to the `lein test`
command as follows:

```bash
lein test modern-cljs.shopping.validators-test
Compiling ClojureScript.

lein test modern-cljs.shopping.validators-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
Could not locate test command .
```

### Break the test

To see how `clojure.test` reports failures, let's now try to modify
an assertion to produce a failure.

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "" "0" "0" "0") ;; produce a failure
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))))
```

Now run the `lein test` command again, and take a look at the failure
report produced by the `calojure.test` framework.

```bash
lein test

lein test modern-cljs.shopping.validators-test

lein test :only modern-cljs.shopping.validators-test/validate-shopping-form-test

FAIL in (validate-shopping-form-test) (validators_test.clj:8)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty" "Quantity has to be an integer number" "Quantity has to be positive"]}))

Ran 1 tests containing 3 assertions.
1 failures, 0 errors.
Tests failed.
Could not locate test command .
```

The failure report says that on the line 8 of `validators_test` source
file there is a failed assertion because the actual value of the
`(validate-shopping-form "" "0" "0" "0")` call is not equal to
`nil`. Indeed, the actual value of this `validate-shop-form` call is
equal to the `{:quantity [...]}` map.

The failure report does require a bit of interpretation at first, but
after a while you can grasp it quicker.

Now correct the failed assertion by rolling back to the previous version.
Rerun the `lein test` command from the terminal to verify that all
the assertions succeed.

### Do not cover only the working path

The coded test includes only few assertions regarding the working path
of the `validate-shopping-form` calls. Generally speaking you should
cover also the *alternative/exception paths*. Let's add few of them in
our first test.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [clojure.test :refer [deftest are testing]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
           nil (validate-shopping-form "1" "0" "0" "0")
           nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
           nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))

    (testing "/ No presence"
      (are [expected actual] (= expected actual)

           "Quantity can't be empty"
           (first (:quantity (validate-shopping-form "" "0" "0" "0")))

           "Price can't be empty"
           (first (:price (validate-shopping-form "1" "" "0" "0")))

           "Tax can't be empty"
           (first (:tax (validate-shopping-form "1" "0" "" "0")))

           "Discount can't be empty"
           (first (:discount (validate-shopping-form "1" "0" "0" "")))))

    (testing "/ Value Type"
      (are [expected actual] (= expected actual)

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "foo" "0" "0" "0")))

           "Quantity has to be an integer number"
           (first (:quantity (validate-shopping-form "1.1" "0" "0" "0")))

           "Price has to be a number"
           (first (:price (validate-shopping-form "1" "foo" "0" "0")))

           "Tax has to be a number"
           (first (:tax (validate-shopping-form "1" "0" "foo" "0")))

           "Discount has to be a number"
           (first (:discount (validate-shopping-form "1" "0" "0" "foo")))))

    (testing "/ Value Range"
      (are [expected actual] (= expected actual)

           "Quantity can't be negative"
           (first (:quantity (validate-shopping-form "-1" "0" "0" "0")))))))
```

We organized the assertions by using the nesting feature of the
`testing` macro to reflect the kind of validations implemented in the
`validate-shopping-form` function.

Even if we could add more assertions, at the moment the coverage for
the `modern-cljs.shopping.validators` namespace is enough to grasp the
idea of the `clojure.test` mechanics.

If you decided to keep track of steps, issue the following `git`
command at the terminal.

```bash
git commit -am "Step 1"
```

Stay tuned!

# Next step - [Turorial 15: It's better to be safe than sorry (Part 3)][11]

In the [next Tutorial][11] of the series we're going to make the
`modern-cljs.shopping.validators-test` runnable from CLJS too.  To
reach this objective, we'll introduce the [clojurescript.test][12] lib
by [Chas Emerick][8] and the [cljx][13] lein plugin by [Kevin Lynagh][14].

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md
[2]: https://github.com/cgrand/enlive
[3]: http://en.wikipedia.org/wiki/Don%27t_repeat_yourself
[4]: http://localhost:3000/shopping.html
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ServerNullPointer.png
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/AjaxServerError.png
[7]: https://github.com/cemerick/valip
[8]: https://github.com/cemerick
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md
[10]: https://github.com/cgrand
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-15.md
[12]: https://github.com/cemerick/clojurescript.test
[13]: https://github.com/lynaghk/cljx
[14]: https://github.com/lynaghk
