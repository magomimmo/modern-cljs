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

> NOTE 1: you'll get a `java.lang.AssertionError` because there is no an
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
cljs.user> (require '[modern-cljs.shopping.validators :refer [validate-shopping-form]])
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

```clj
cljs.user> (validate-shopping-form "-10" "0" "0" "")
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
different from one another and they require different namespaces:
`clojure.test` namespace for CLJ and `cljs.test` namespace for CLJS.

The `clojure.test` namespace has been implemented well before the
corresponding `cljs.test` namespace and the latter, through a detailed
porting on the JSVM preserved most of the functionalities provided by
the former. So we are still in good position for writing unit tests
that are applicable to a portable CLJ/CLJS namespace.

That said, most of the `cljs.test` functionalities are provided as
macros, which means that you need the special CLJS `:require-macros`
option in the requirement declaration of a testing namespace, while
you do no need it in the corresponding CLJ namespace declaration.

We already met a case like that. Indeed, while writing the portable
`modern-cljs.login.validators` namespace, we needed to differentiate
the CLJ/JVM platform from the CLJS/JSVM one.

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
only

```bash
mkdir -p test/cljc/modern_cljs/shopping
```

because we're going to create the unit tests for the
`modern-cljs.shopping.validators` namespace only.

> NOTE 3: even if you do not need to mimic the source directory layout
> in a corresponding test directory layout, by doing it the project
> structure will be more readable.

We now need to create the unit test file for the
`modern-cljs.shopping.validators` namespace. You could name this file
as you like, but I like to give it a name resembling the name of the
file defining the source namespace (i.e. `validators`) and remembering
me I'm taking care of its unit tests.

```bash
touch test/cljc/modern_cljs/shopping/validators_test.cljc
```

> NOTE 4: this time you'll not receive any error regarding the file not
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

### Start testing at the borders

The reason why we chose to start testing from the
`modern-cljs.shopping.validators` namespace is because it sits at the
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
only the `validate-shopping-form` function, which is the one we're
going to unit test.

Following is the initial unit test code that mimics the short REPL
testing session we did previously with the
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

> NOTE 5: we made the `modern-cljs.shopping.validators-test` namespace
> compatible with both CLJ and CLJS by using the `#?` reader
> literal. Depending on the available feature at runtime, `:clj` of
> `:cljs`, the reader will get the right namespace to be required in the
> `ns` declaration.

We started very simple.

By using the `deftest` and the `is` macros, we defined our first unit
test named `validate-shopping-form-test`. As before, even if we are
free to name anything as we want, I prefer to adopt some conventions
for facilitating unit test writing and reading. If I want to test a
function named `my-function`, I name the test `my-function-test`.

The `is` macro allows to make assertions of any arbitrary
expression. The code `(is (= nil (validate-shopping-form "1" "0" "0"
"0")))` is saying that the acutal evaluation of the
`(validate-shopping-form "1" "0" "0" "0")` form is expected to be equal
to `nil` because all the input are valid. At the moment, we're just
testing the happy path.

### On getting less bored

You'll get bored very quickly in typing `is` forms. That's way
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

> NOTE 7: again, don't forget to add the `testing` symbol in the
> `:refer`/`:refer-macros` sections of the requirements.

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

## Running the test

To run the newly defined unit test for the
`modern-cljs.shopping.validators` namespace we have more options. The
one we're going to use right now does not require to kill the running
`boot` process even if we're going to alter the `boot` environment
established by the `build.boot` file when we started IFDE with the
`boot dev` command.

This is the first time in the series that we do no stop `boot` to
alter its runtime environment. Sometimes it could be useful,
especially when you're experimenting something new and this is our
case.

### Dynamically alter the boot environment

In the previous paragraphs we created the
`test/cljc/modern_cljs/shopping` directory. Then we created the
`validators_test.cljc` source file for unit testing the
`modern-cljs.shopping.validators` namespace. At the moment `boot` does
not know anything about this new source file, because the
`:source-paths` environment variable of the `build.boot` building file
has been set to `#{"src/clj" "src/cljs" "src/cljc"}`.

There is a pretty easy way to add a new directory to
`:source-paths`. Stop the CLJS bREPL session (i.e. `:cljs/quit`) you
previously ran on top of the CLJ REPL session. You should now see the
CLJ REPL `boot.user=>` prompt. Now evaluate the following expression
at the REPL prompt:

```clj
boot.user> (set-env! :source-paths #(conj % "test/cljc"))
nil
```

If you're curious about the `set-env!` function, you can ask for its docstring

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

As you see we hit last case. The value we passed for the
`:source-paths` key is an anonymous function conjoining the
`"test/cljc"` source directory to the previous value represented by
the `%` symbol.

This way you are dynamically adding the `test/cljc` directory to the
`:source-paths` environment variable of `boot`. It means that now
`boot` knows about the `modern-cljs.shopping.validator-test` namespace
we declared above.

## Light the fire on the server side

While we're in the CLJ REPL, we can require the `clojure.test` and the
`modern-cljs.shopping.validators-test` namespace

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
redefined and recompiled, `cojure.test` still knows about the old
definition. To obtain the expected effect we have to explicitly reload
the `modern-cljs.shopping.validators-test` namespace and re-run
`run-tests`.

```clj
boot.user> (require '[modern-cljs.shopping.validators-test] :reload)
nil
boot.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

FAIL in (validate-shopping-form-test) (validators_test.cljc:8)
Shopping Form Validation
expected: (= nil (validate-shopping-form "" "0" "0" "0"))
  actual: (not (= nil {:quantity ["Quantity can't be empty" "Quantity has to be an integer number" "Quantity can't be negative"]}))

Ran 1 tests containing 3 assertions.
1 failures, 0 errors.
{:test 1, :pass 2, :fail 1, :error 0, :type :summary}
```

Now we talk. Even if the failure report does require a bit of
interpretation at first, after a while you can grasp it quicker.

Now revert the above test to the good form, reload its namespace and
rerun `run-tests to got back to the right test result.

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

The coded test includes only few assertions regarding the working path
of the `validate-shopping-form` calls. Generally speaking you should
cover also the *alternative/exception paths*. Let's add few of them in
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

Even if we could add more assertions, at the moment the coverage for
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

As you now have one test running 13 assertions and all of them
succeeded.

## Light the fire on the client side

We used a portable validation lib (i.e. `valip`). We implemented
portable validation rules (i.e. `validators.cljc`). We wrote a
portable unit tests file and finally we even ran those tests on the
server side. All of that without stopping the running IFDE.

The minimum we can now ask for is to run the same unit
tests on the client side as well.

But you'll met a problem. Once you stop a CLJS bREPL
(i.e. `:cljs/quit`) started on top of a CLJ REPL, if you try to
restart the bREPL, it hangs forever.

> NOTE 8: if you use a smart nrepl editor able to create more
> nrepl-client connections with the nrepl-server created by `boot`, this
> problem will not affect you.

This seems to be a bug of the `boot-cljs-repl` task. One way you have
to solve this problem is to stop the CLJ REPL, stop the IFDE and
restart all the stuff again

```clj
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 24.569 sec
```

visit the [Shopping URL](http://localhost:3000/shopping.html) and run
the CLJ REPL

```bash
# from another terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

Do you remember that we altered the `:source-paths` key by adding the
newly created `test/cljc` test directory? Do that again.

```clj
boot.user> (set-env! :source-paths #(conj % "test/cljc"))
nil
```

We can now start the CLJS bREPL on top of the CLJ REPL again

```clj
boot.user> (start-repl)
<< started Weasel server on ws://127.0.0.1:53672 >>
<< waiting for client to connect ... Connection is ws://localhost:53672
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
```

## Require the needed namespaces

It's now time to see the magic at work one more time.

First require the `cljs.test` namespace

```clj
cljs.user> (require '[cljs.test :as t :refer-macros [run-tests]])
nil
```

As you see we had to use the `:refer-macros` option keyword to include
the macros.

Then we have to require `modern-cljs.shopping.validators-test`
namespace containing the assertions of the associated with the
`validate-shopping-form-test` unit test

```clj
cljs.user> (require '[modern-cljs.shopping.validators-test :as v])
nil
```

Are you ready for the country? Evaluate the following expression:

```clj
cljs.user> (t/run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
nil
cljs.user> (run-tests 'modern-cljs.shopping.validators-test)

Testing modern-cljs.shopping.validators-test

Ran 1 tests containing 13 assertions.
0 failures, 0 errors.
nil
```

Boom. For this tutorial we're done. Stop any `boot`
related process and reset you repository

```bash
git reset --hard
```

Stay tuned!

# Next step - [Turorial 15: It's better to be safe than sorry (Part 3)][11]

In the [next Tutorial][11] of the series we're going to make some
housekeeping with `boot`.

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
