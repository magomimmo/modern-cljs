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

Now [disable the JavaScript engine][15] of your browser, visit or reload the
[shopping URI][4], and click the `Calculate` button. The `Total` field
is filled with the result of the calculation executed on the
server-side via the "/shopping" action associated to the
`shoppingForm`. That's what we reached at the end of the
[previous tutorial][1]. So far so good.

## Break the Shopping Calculator again and again

Now enter an unexpected value in the form, for example `foo` as the
value of the `Price per Unit` field. Finally, click the `Calculate`
button.

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

Now let's see what happens if we [reactivate the JavaScript engine][15] and reload the
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

We know from the above tutorial that we can
[share](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md#crossing-the-border)
the validation rules between the server and the client sides by
creating a portable CLJ/CLJS source file with the `.cljc` extension in
the `src/cljc` source directory's structure.

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

> NOTE 1: you'll get a `java.lang.AssertionError` because there is no
> `ns` form in the newly created file. Don't worry. As soon as you'll
> save the edited file the error will disappear.

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

> NOTE 2: At the moment we don't deal with any issues regarding
> internationalization and we're hard-coding the text messages in the
> source code.

As you can see, we defined the `validate-shopping-form` function by
using the `validate` function from the `valip.core` namespace and a
bunch of predicates that [Chas Emerick][8] was so kind to have defined
for us in the `valip.predicates` namespace.

Considering that `valip` is a portable lib, we can immediately test the
`validate-shopping-form` function at the CLJ REPL and at the CLJS
bREPL as well.

### The server side

Let's start from the server side

```bash
# in a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> 
```

Now exercise the newly defined `validate-shopping-form` function as
follows:

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

The above REPL session testing the `validate-shopping-form`
function is not a substitute for unit testing, since the tests are 
all manual and cannot be re-run without duplicating all of this work. Moreover,
considering that the above `validate-shopping-form` validator is
portable on CLJS as well, you would need to manually repeat them on the
client-side as well. 

## Unit testing

No programmer likes code repetition. Re-typing similar expressions
again and again in the REPL is something that should feel awful to everyone.

Fortunately, both CLJ and CLJS offer a solution for automating
such boring activities. Unfortunately, unit testing on CLJ/JVM is
not exactly the same as unit testing on CLJS/JSVM. The two platforms are
different from one another and they require different namespaces:
the `clojure.test` namespace for CLJ and the `cljs.test` namespace for CLJS.

The `clojure.test` namespace was created long before the
corresponding `cljs.test` namespace and the latter, through a detailed
porting to the JSVM, preserved most of the functionalities provided by
the former. So we are still in a good position for writing unit tests
that are applicable to a portable CLJ/CLJS namespace like
`modern-cljs.shopping.validators`.

That said, most of the `cljs.test` functionalities are provided as
macros, which means that you must use the special CLJS `:require-macros`
option in the `(ns ...)` declaration of a testing namespace, while
you do not need it in the corresponding CLJ namespace declaration.

We have already seen an instance of this. Indeed, while writing the portable
`modern-cljs.login.validators` namespace, we needed to differentiate
between the CLJ/JVM platform and the CLJS/JSVM platform.

For that case we introduced the use of the `#?` reader literal. It
allowed us to make the `email-domain-errors` validator available on
the CLJ/JVM platform only while writing portable code in a `cljc`
source file.

To write a portable testing namespace for the portable
`modern-cljs.shopping.validators` namespace we created above, we have
to use the same `#?` trick to differentiate the different testing
namespace declarations used by CLJ and CLJS.

## Mirroring the project structure

When creating unit tests, we want to host them in a different path
from the application source files. Generally speaking you want to
mimic in a test directory structure the same layout you created for
the application source files.

Under the `src` main directory we currently have three subdirectories,
one for each source file extension: `clj`, `cljs` and `cljc`.

At the moment we'll mimic the same structure for the `cljc` directory
only, because we're going to create the unit tests for that
`modern-cljs.shopping.validators` namespace only.

```bash
mkdir -p test/cljc/modern_cljs/shopping
```

> NOTE 3: Although you are not required to mimic the source directory layout
> in the test directory, doing so will make the project
> structure simpler and easier to understand. 

We now need to create the unit test file for the
`modern-cljs.shopping.validators` namespace. You could name this file
anything, but I like to give it a name resembling the
source namespace file (i.e. `validators`), plus an indicator that it 
defines unit tests. Following the pattern `validators_test.cljc` seems to
be a good name for our needs.

```bash
touch test/cljc/modern_cljs/shopping/validators_test.cljc
```

> NOTE 4: This time you'll not receive any error regarding the file not
> containing an `ns` form. This is because `boot` still does not know
> anything about the `test` directory structure in which the newly
> created file lives.

You'll end up the following test structure

```bash
test
└── cljc
    └── modern_cljs
        └── shopping
            └── validators_test.cljc
```

### Start unit testing at the borders

The reason why we chose to start testing from the
`modern-cljs.shopping.validators` namespace is because it sits at the
border of our application with the external world: be it a user to be
notified about her/his mistyped input, or an attacker trying to
leverage any useful information by breaking the Shopping Calculator.

I prefer to speak about "testing namespaces", rather than "unit testing
functions". Generally speaking you should unit test the API of each
namespace, which means all of the symbols which are not private in the
namespace itself. If you want to unit test a private symbol, you
should write the unit test code in the same file where you defined 
the private symbol.

### The first unit test

At the moment the `modern-cljs.shopping.validators` namespace contains
the `validate-shopping-form` function only, which is the one we're
going to unit test.

The following is the initial unit test code that mimics the short REPL
testing session we did previously for the
`modern-cljs.shopping.validators` namespace.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form]]
            #?(:clj [clojure.test :refer [deftest is]]
               :cljs [cljs.test :refer-macros [deftest is]])))

(deftest validate-shopping-form-test
  (is (= nil (validate-shopping-form "1" "0" "0" "0")))
  (is (= nil (validate-shopping-form "1" "0.0" "0.0" "0.0")))
  (is (= nil (validate-shopping-form "100" "100.25" "8.25" "123.45"))))
```

> NOTE 5: We made the `modern-cljs.shopping.validators-test` namespace
> compatible with both CLJ and CLJS by using the `#?` reader
> literal. Depending on the available feature at compile-time, `:clj`
> of `:cljs`, the reader will cause the correct namespace to be required
> in the `ns` declaration.

We have started very simply.

By using the `deftest` and the `is` macros, we defined our first unit
test named `validate-shopping-form-test`. As mentioned previously, while we are
free to name the tests anything we want, I prefer to adopt some conventions
to facilitate the writing and reading of unit tests. If I want to test a
function named `my-function`, I name the test `my-function-test`.

The `is` macro allows us to make assertions about arbitrary
expressions. The code `(is (= nil (validate-shopping-form "1" "0" "0"
"0")))` means that the evaluation of `(validate-shopping-form "1" "0" "0" "0")` 
must return `nil` for the test to pass. For this test, all the input values 
are valid (at the moment, we're just testing the happy path).

### On getting less bored

You'll quickly get very bored typing `is` forms. That is why
`clojure.test` and `cljs.test` include the `are` macro which allows
the programmer to save some typing. The above group of `is` forms can
be reduced to the following equivalent `are` form:

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form]]
            #?(:clj [clojure.test :refer [deftest are]]
               :cljs [cljs.test :refer-macros [deftest are]])))

(deftest validate-shopping-form-test
  (are [expected actual] (= expected actual)
       nil (validate-shopping-form "1" "0" "0" "0")
       nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
       nil (validate-shopping-form "100" "100.25" "8.25" "123.45")))
```

> NOTE 6: don't forget to substitute `is` with `are` in the
> `:refer`/`:refer-macros` sections of the requirements.

In the above example, each `nil` value represents an expected value,
while the evaluation of each `validate-shopping-form` call represents
the actual value. The `are` macro verifies that for each pair of an
expected and actual values `[expected actual]`, the assertion `(=
expected actual)` is true.

### How to read failure reports

You can even document tests by wrapping your `are` form inside a
`testing` macro, which takes a documentation string followed by any
number of `is/are` assertions.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    (are [expected actual] (= expected actual)
         nil (validate-shopping-form "1" "0" "0" "0")
         nil (validate-shopping-form "1" "0.0" "0.0" "0.0")
         nil (validate-shopping-form "100" "100.25" "8.25" "123.45"))))
```

> NOTE 7: Again, don't forget to add the `testing` symbol in the
> `:refer`/`:refer-macros` sections of the requirements.

The string "Shopping Form Validation" will be included in failure
reports. Calls to `testing` macros may even be nested to allow better
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

## Running the tests

To run the newly defined unit test for the
`modern-cljs.shopping.validators` namespace, we have some options. The
one we're going to use right now does not require killing the running
`boot` process even though we're going to alter the `boot` environment
established by the `build.boot` file when we started IFDE with the
`boot dev` command.

This is the first time in the series that we do not stop `boot` to
alter its runtime environment. Sometimes this is useful,
especially when you're experimenting with something new (as
we are now).

### Dynamically alter the boot environment

In the previous paragraphs we created the
`test/cljc/modern_cljs/shopping` directory. Then we created the
`validators_test.cljc` source file for unit testing the
`modern-cljs.shopping.validators` namespace. At the moment `boot` does
not know anything about this new source file, because the
`:source-paths` environment variable in the `build.boot` file
has been set to `#{"src/clj" "src/cljs" "src/cljc"}`.

There is a pretty easy way to dynamically add a new directory to
`:source-paths` at the REPL:

```clj
boot.user> (set-env! :source-paths #(conj % "test/cljc"))
nil
```

If you're curious about the `set-env!` function, you can examine its docstring:

```clj
boot.user> (doc set-env!)
-------------------------
boot.core/set-env!
([& kvs])
  Update the boot environment atom `this` with the given key-value pairs given
  in `kvs`. See also `post-env!` and `pre-env!`. The values in the env map must
  be both printable by the Clojure printer and readable by its reader. If the
  value for a key is a function, that function will be applied to the current
  value of that key and the result will become the new value (similar to how
  clojure.core/update-in works.
nil
```

As you can see, we fit the last case. The value we passed for the
`:source-paths` key is an anonymous function to `conj` the
`"test/cljc"` source directory to the previous value represented by
the `%` symbol.

In this way you dynamically add the `test/cljc` directory to the
`:source-paths` environment variable of `boot`. Thus,
`boot` now knows about the new `modern-cljs.shopping.validator-test` namespace
we declared above.

## Light the fire on the server side

While we're in the CLJ REPL, we can require the `clojure.test` and the
`modern-cljs.shopping.validators-test` namespaces

```clj
boot.user> (require '[clojure.test :as t]
                    '[modern-cljs.shopping.validators-test])
nil
```

and then run the server-side unit tests for the shopping's fields
validation as follows:

```clj
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
{:test 1, :pass 3, :fail 0, :error 0, :type :summary}
```

So far, so good.

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

Now re-evaluate the above the above `run-tests` expression

```clj
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
{:test 1, :pass 3, :fail 0, :error 0, :type :summary}
```

Oops, nothing new happens. The unit test succeeded again. The problem
is that even if the `validate-shopping-form-test` function got
redefined and recompiled, `clojure.test` still knows about the old
definition. To obtain the expected effect we have to explicitly reload
the `modern-cljs.shopping.validators-test` namespace and re-run
`run-tests`.

```clj
boot.user> (require '[modern-cljs.shopping.validators-test] :reload)
nil
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:8)
Shopping Form Validation / Happy Path
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty" "Quantity has to be an integer number" "Quantity can't be negative"]}))

Ran 1 tests containing 3 assertions.
1 failures, 0 errors.
{:test 1, :pass 2, :fail 1, :error 0, :type :summary}
```

Now we're talking. Even if the failure report does require a bit of
interpretation at first, with some practice you can understand it more quickly.

Now revert the above failing test to its proper form, reload its namespace and
rerun `run-tests` so everything is passing again.

```clj
boot.user> (require '[modern-cljs.shopping.validators-test] :reload)
nil
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 3 assertions.
0 failures, 0 errors.
{:test 1, :pass 3, :fail 0, :error 0, :type :summary}
```

### Do not cover only the working path

The tests we have written so far include only a few assertions about the happy path
in `validate-shopping-form`. Generally speaking you should
also cover the *alternative/exception paths*. Let's add few of them in
our first test.

```clj
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

Even though we could add more assertions, for the moment the coverage of
the `modern-cljs.shopping.validators` namespace is enough to grasp the
idea of the `clojure.test` mechanics.

To run above unit tests do as before. First reload the namespace
containing the test, then run the tests:

```clj
boot.user> (require '[modern-cljs.shopping.validators-test] :reload)
nil
```

```clj
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
```

As you can see, we have one test containing 13 assertions, and all of them
succeed.

### The magic

We now want to repeat the magic we have already seen at work in a previous
tutorial dedicated to the `loginForm`.

Start the CLJS bREPL from the the CLJ REPL as usual

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

## Require the needed namespaces

First require the `cljs.test` namespace

```clj
cljs.user> (require '[cljs.test :as t :include-macros true])
nil
```

As you see we had to use the `:include-macros` option keyword to include
the macros.

Then we have to require `modern-cljs.shopping.validators-test`
namespace containing the assertions associated with the
`validate-shopping-form-test` unit test.

```clj
cljs.user> (require '[modern-cljs.shopping.validators-test :as v])
nil
```

Are you ready for the magic? Evaluate the following expression:

```clj
cljs.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
nil
```

Voila! The magic worked again. Kudos to everyone who put this magic
together.

We are at the end of this tutorial. Stop any `boot` related process
and reset the repository

```bash
git reset --hard
```

Stay tuned!

# Next step - [Tutorial 15: It's better to be safe than sorry (Part 3)][11]

In the [next Tutorial][11] of the series, we're going to do some
housekeeping with `boot`.

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
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
[15]: https://github.com/magomimmo/modern-cljs/blob/master/doc/supplemental-material/enable-disable-js.md
