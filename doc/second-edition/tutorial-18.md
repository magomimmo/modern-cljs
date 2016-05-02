# Tutorial 18 - Augmented TDD Session

In the [previous tutorial][1] we integrated the validators for the
Shopping Calculator into the corresponding [WUI][5] in
such a way that the user will be notified with the corresponding help
messages when she enters invalid values in the form. By first
injecting the validators into the server-side code, we have been
religious about the progressive enhancement strategy. It's now time to
fill the gap by injecting the portable validators into the client-side
[WUI][5] as well.

## Preamble

To start working from the end of the [previous tutorial][1], assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-17
```

## Introduction

In this tutorial we're going to integrate the portable validators for
the Shopping Calculator into the client-side [WUI][5].

While there is nothing really new to be learned in this tutorial about
CLJS, it represents a good opportunity to see at work the live coding
[TDD][6] environment we setup in the
[Tutorial 16 - On pleasing TDD practitioners][2]

As said by [Fogus](http://www.fogus.me/), the author of
[The Joy of Clojure](https://www.manning.com/books/the-joy-of-clojure-second-edition)
book:

> Most software development projects include a stage where you’re not
> sure what needs to happen next. Perhaps you need to use a library or
> part of a library you’ve never touched before. Or perhaps you know
> what your input to a particular function will be, and what the output
> should be, but you aren’t sure how to get from one to other. In some
> programming languages, this can be time-consuming and frustrating; but
> by leveraging the power of the Clojure REPL, the interactive command
> prompt, it can actually be fun.

This statement is particularly true when you deal with OSS (Open
Source Software) libraries which are frequently updated without
their corresponding documentation being updated as well.

Even if you are a [TDD][6] practitioner, which means that you start coding
from a failing test, you still need to know and to understand your
programming language and the libraries you're going to use to fix the
failed tests and to refactor your code to obtain a cleaner and more
maintainable code base. In that regard, Clojure(Script) REPLs are your
best friends as they are for the ones, like myself, not starting to
code from a test that has to fail.

## Start TDD

Start [TDD][6] environment:

```bash
cd /path/to/modern-cljs
boot tdd
...
Elapsed time: 26.573 sec
```

### Start CLJ REPL

Now launch the client REPL as usual

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

[Turn on the JS engine][4] in your browser and then visit the
[Shopping Calculator](http://localhost:3000/shopping.html) URI to
activate the websocket connection used by `tdd` to reload pages when
you save some changes.

### Start CLJS bREPL

Finally, launch the CLJS bREPL from the CLJ REPL

```bash
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:49974 >>
<< waiting for client to connect ... Connection is ws://localhost:49974
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

and you're ready to go.

## Server side validators review

In the [previous tutorial][1] we injected the portable
`validate-shopping-form` validator in the `shopping` function defined
in the `modern-cljs.templates.shopping` namespace:

```clj
(defn shopping [q p t d]
  (update-shopping-form q p t d (validate-shopping-form q p t d)))
```

The `validate-shopping-form` validator has been defined in the
portable `src/cljc/modern_cljs/shopping/validators.cljc` source file:

```clj
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

`validate-shopping-form` validates the `quantity`, `price`, `tax` and
`discount` input fields all together. As you remember, when some
values do not pass the validation rules, the validator returns a map
of the corresponding error messages. Something like the following

```clj
;;; a sample call like the following
(validate {:email "zzzz" :password nil}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid password format"])

;;; returns

{:email ["Invalid email format"]
 :password ["Password can't be empty" "Invalid password format"]}
```

For that reason the assertions of the `validate-shopping-form-test`
test have been implemented by getting the `first` item of the vector
of messages returned by the `validate-shopping-form` when a value of
an input field does not pass the validation:

```clj
(deftest validate-shopping-form-test
  (testing "Shopping Form Validation"
    ...
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
    ...))
```

## TDD Workflow: start from a test that has to fail

On the client side [WUI][5] we want to individually validate any input value
as soon as we leave the corresponding field (i.e. when the
`blur` event is fired) as we already did in a
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md)
with the `email` and `password` input of the `Login Form`:

```clj
;; individually validate email input field
(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [errors (:email (user-credential-errors (value email) nil))]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    (validate-email-domain (value email))))

;; individually validate password input field
(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [errors (:password (user-credential-errors nil (value password)))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))

(defn ^:export init []
  (if (and ...)
    (let [email (by-id "email")
          password (by-id "password")]
      ...
      ;; validate email input field on blur event
      (listen! email :blur (fn [evt] (validate-email email)))
      l
      ;; validate password input field on blur event
      (listen! password :blur (fn [evt] (validate-password password))))))
```

Open the `test/cljc/modern_cljs/shopping/validators_test.cljc` file to
start adding new test assertions for the `quantity` input of the
Shopping Calculator form:

```clj
(deftest validate-shopping-quantity-test
  (testing "Shopping Form Quantity Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-quantity "1")))))
```

Here the intention is to test the happy path: the validator
`validate-shopping-quantity`, that still does not exist, should return
`nil`, meaning no errors, when called with an integer argument.

As soon as you save the file you'll receive an expected error from
the running CLJS/CLJ auto-test processes:

```bash
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
WARNING: Use of undeclared Var modern-cljs.shopping.validators-test/validate-shopping-quantity at line 57 test/cljc/modern_cljs/shopping/validators_test.cljc
...
Ran 3 tests containing 39 assertions.
0 failures, 1 errors.
clojure.lang.Compiler$CompilerException: java.lang.RuntimeException: Unable to resolve symbol: validate-shopping-quantity in this context, compiling:(modern_cljs/shopping/validators_test.cljc:56:7)
             java.lang.RuntimeException: Unable to resolve symbol: validate-shopping-quantity in this context
...
Elapsed time: 2.487 sec
```

Considering we intend to define the `validate-shopping-quantity`
validator in the portable `modern-cljs.shopping.validators` namespace,
we start fixing the error by adding the `validate-shopping-quantity`
symbol to the `:refer` section of the
`modern-cljs.shopping.validators` namespace requirement in the
`modern-cljs.shopping.validators-test` namespace declaration:

> NOTE 1: we could define the individual client-side input validators
> in a `.cljs` source file as well, but it does not hurt if we define
> them in the portable namespace.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form
                                                     validate-shopping-quantity]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))
```

As soon as you save the file, you'll again receive an expected error:

```bash
...
adzerk.boot_cljs.util.proxy$clojure.lang.ExceptionInfo$ff19274a: Referred var modern-cljs.shopping.validators/validate-shopping-quantity does not exist
    ...
Elapsed time: 0.275 sec
```

Note that the same error is notified in the browser as well.

## Satisfy the test

To make the assertion to pass, we need now to define the
`validate-shopping-quantity` validator in the
`modern-cljs.shopping.validators` namespace.

```clj
(defn validate-shopping-quantity [quantity]
  (first (:quantity (validate-shopping-form quantity "0" "0" "0"))))
```

Here we're reusing the previously defined `validate-shopping-form`
validator by passing to it acceptable values for `price`, `tax` and
`discount` because the newly defined `validate-shopping-quantity`
validator is for the `quantity` input field only.

Also note that `validate-shopping-quantity` returns the `first` error
message, if any, associated with the `:quantity` keyword.

As soon as you save the file you'll see that the newly defined
assertion for the `validate-shopping-validate-test` unit test passed.

```bash
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 39 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 4 tests containing 40 assertions.
0 failures, 0 errors.
Elapsed time: 7.119 sec
```

## Code Refactoring

The `validate-shopping-quantity` validator is very simple, but at the
same time it's suggesting to us that `validate-shopping-price`,
`validate-shopping-tax` and `validate-shopping-discount` validators
would be almost identical:

```clj
(defn validate-shopping-price [price]
  (validate-shopping-form "1" price "0" "0"))

(defn validate-shopping-tax [tax]
  (validate-shopping-form "1" "0" price "0"))

(defn validate-shopping-discount [discount]
  (validate-shopping-form "1" "0" "0" discount))
```

A clear case for code refactoring, because we could implement a more
general `validate-shopping-field` receiving a field (e.g.,
`:quantity`, `:price`, etc.) and the value to be validated as
arguments.

To implement `validate-shopping-field` we could use one of the
CLJ/CLJS conditional forms: `cond`, `condp` or`case`. Perhaps we do
not remember how they work well enough to choose the one that best
fits our case. Don't worry, we have the REPL to experiment
with them:

First require the needed namespace:

```clj
cljs.user> (require '[modern-cljs.shopping.validators :as v])
nil
```

Then get the `cond` docstring:

```clj
cljs.user> (doc cond)
-------------------------
cljs.core/cond
([& clauses])
Macro
  Takes a set of test/expr pairs. It evaluates each test one at a
  time.  If a test returns logical true, cond evaluates and returns
  the value of the corresponding expr and doesn't evaluate any of the
  other tests or exprs. (cond) returns nil.
nil
```

and experiment with the `cond` form in the context of our case, by
starting with the happy path

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (cond (= field :quantity) (v/validate-shopping-form val "0" "0" "0")
                   (= field :price) (v/validate-shopping-form "1" val "0" "0")))
nil
````

and going on with an invalid value:

```clj
cljs.user> (let [field :quantity
                 val "-1"]
             (cond (= field :quantity) (v/validate-shopping-form val "0" "0" "0")
                   (= field :price) (v/validate-shopping-form "1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
```

It works, but it's a little bit verbose. Let's see if the `condp` form
has something better to offer by first getting its docstring

```clj
cljs.user> (doc condp)
-------------------------
cljs.core/condp
([pred expr & clauses])
Macro
  Takes a binary predicate, an expression, and a set of clauses.
  Each clause can take the form of either:

  test-expr result-expr

  test-expr :>> result-fn

  Note :>> is an ordinary keyword.

  For each clause, (pred test-expr expr) is evaluated. If it returns
  logical true, the clause is a match. If a binary clause matches, the
  result-expr is returned, if a ternary clause matches, its result-fn,
  which must be a unary function, is called with the result of the
  predicate as its argument, the result of that call being the return
  value of condp. A single default expression can follow the clauses,
  and its value will be returned if no clause matches. If no default
  expression is provided and no clause matches, an
  IllegalArgumentException is thrown.
nil
```

and then experimenting with it, by starting again from an happy path

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (condp = field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "1" val "0" "0")))
nil
```

and continuing with an invalid input value

```clj
cljs.user> (let [field :quantity
                 val "-1"]
             (condp = field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
```

A little bit less verbose. Perhaps `case` form is even better than
`condp`. Get its docstring

```cljs
cljs.user=> (doc case)
-------------------------
cljs.core/case
([e & clauses])
Macro
  Takes an expression, and a set of clauses.

  Each clause can take the form of either:

  test-constant result-expr

  (test-constant1 ... test-constantN)  result-expr

  The test-constants are not evaluated. They must be compile-time
  literals, and need not be quoted.  If the expression is equal to a
  test-constant, the corresponding result-expr is returned. A single
  default expression can follow the clauses, and its value will be
  returned if no clause matches. If no default expression is provided
  and no clause matches, an Error is thrown.

  Unlike cond and condp, case does a constant-time dispatch, the
  clauses are not considered sequentially.  All manner of constant
  expressions are acceptable in case, including numbers, strings,
  symbols, keywords, and (ClojureScript) composites thereof. Note that since
  lists are used to group multiple constants that map to the same
  expression, a vector can be used to match a list if needed. The
  test-constants need not be all of the same type.
nil
```

The `case` form seems to
[better fit our need](http://insideclojure.org/2015/04/27/poly-perf/). Let's
try it at bREPL:

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (case field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
nil
```

```clj
cljs.user> (let [field :quantity
                 val "-1"]
             (case field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
```

Now that we understand a little bit better our beloved CLJ/CLJS
programming language by experimenting with it at the REPL while
following a [TDD][6] approach, go back to the
`modern-cljs.shopping.validators` namespace and substitute the
previously defined `validate-shopping-quantity` validator with a more
general one as follows:

```clj
(defn validate-shopping-field [field value]
  (case field
    :quantity (first (:quantity (validate-shopping-form value "0" "0" "0")))
    :price (first (:price (validate-shopping-form "1" value "0" "0")))
    :tax (first (:tax (validate-shopping-form "1" "0" value "0")))
    :discount (first (:discount (validate-shopping-form "1" "0" "0" value)))))
```

> NOTE 2: OOP (Object Oriented Programming) practitioners hate
> conditionals forms. Some of them even launched an
> [Anti-IF Campaign](http://antiifcampaign.com/). I'm not religious in
> anyway about anything, because I'm a Philosopher, but sometime it
> happens that a smart use of `defprotocol` or `defmulti` could
> improve the abstraction and the extendibility of your code (i.e.,
> polymorphism).

As soon as you save the file you'll receive again an expected error

```bash
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
adzerk.boot_cljs.util.proxy$clojure.lang.ExceptionInfo$ff19274a: Referred var modern-cljs.shopping.validators/validate-shopping-quantity does not exist
...
Elapsed time: 1.477 sec
```

because `validate-shopping-quantity` does not exist anymore, but it is
still referenced in the test file. Let's fix it

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form
                                                     validate-shopping-field]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))

;;; ...

(deftest validate-shopping-field-test
  (testing "Shopping Form Fields Validation"
    ;; happy path
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-field :quantity "1")
        nil (validate-shopping-field :price "1.0")
        nil (validate-shopping-field :tax "8.25")
        nil (validate-shopping-field :discount "0.0")))
    ;; presence
    (testing "/ Presence"
      (are [expected actual] (= expected actual)
        "Quantity can't be empty" (validate-shopping-field :quantity "")
        "Quantity can't be empty" (validate-shopping-field :quantity nil)
        "Price can't be empty" (validate-shopping-field :price "")
        "Price can't be empty" (validate-shopping-field :price nil)
        "Tax can't be empty" (validate-shopping-field :tax "")
        "Tax can't be empty" (validate-shopping-field :tax nil)
        "Discount can't be empty" (validate-shopping-field :discount "")
        "Discount can't be empty" (validate-shopping-field :discount "")))
    ;; type
    (testing "/ Type"
      (are [expected actual] (= expected actual)
        "Quantity has to be an integer number" (validate-shopping-field :quantity "1.1")
        "Price has to be a number" (validate-shopping-field :price "foo")
        "Tax has to be a number" (validate-shopping-field :tax "foo")
        "Discount has to be a number" (validate-shopping-field :discount "foo")))
    ;; range
    (testing "/ Range"
      (are [expected actual] (= expected actual)
        "Quantity can't be negative" (validate-shopping-field :quantity "-1")))))
```

While we were updating the test file to fix the above error, it was
very easy to add some other assertions as well.

As soon as you save the file, the `tdd` environment fires the
recompilation and the re-execution of all defined tests:

```bash
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 55 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 4 tests containing 56 assertions.
0 failures, 0 errors.
Elapsed time: 2.513 sec
```

I leave to you the addition of any other assertion you could be
interested in for strengthening your confidence of the correctness of
the newly defined fields validators.

## Fill the Gap

In the previous paragraph we experimented with an augmented [TDD][6] workflow by
interleaving a few experiments with the language in the REPL while
satisfying a few tests and consequently refactoring some code. But we
still have to attach the newly defined individual field validators to
the Shopping Calculator. Let's do that.

## Inject the validators

To inject the individual validators for the input fields of the
Shopping Calculator I'm not going to use a [TDD][6] workflow, but you could.

Let's start from the `quantity` input field. We'd like to mimic the
same effect we reached on the server-side [WUI][5]. For example, if the
user eventually typed a decimal number in the `quantity` input field,
when the field looses the focus we'd like to transform the following
HTML fragment

```html
<div>
  <label for="quantity">Quantity</label>
  <input type="number"
         name="quantity"
         id="quantity"
         value="1"
         min="1" required>
</div>
```

into something like the following fragment

```html
<div>
  <label class="help" for="quantity">Quantity has to be an integer number</label>
  <input type="number"
         name="quantity"
         id="quantity"
         value="1.2"
         min="1" required>
</div>
```

## CSS selectors

First note that the `label` element does not have an `id` attribute to
be used with the `by-id` function we already used more times in
previous tutorials on `domina` library usage.

Likely `domina` offers both `css` and `xpath` selectors for such a
case. This is the first time we've dealt with `css` and `xpath` selectors
from the `domina` lib and before we start coding we want to
familiarize ourselves a little bit with at least one of them in the bREPL,
namely with the `css` selector.

Perhaps you remember that due to a bug in the `boot-cljs-repl` task,
before requiring a namespace of a dependency from the bREPL, you need
to require it in a source file.

Open the `src/cljs/modern_cljs/shopping.cljs` source file and add the
`domina.css` namespace to the namespace declaration as follows:

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append!
                                 by-class
                                 by-id
                                 destroy!
                                 set-value!
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [domina.css :refer [sel]] ;; domina css selector
            [hiccups.runtime]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))
```

Note that the only referred symbol in the `domina.css` requirement is
`sel`, for selector. We can now start playing with it at the bREPL. First we have to require the needed namespaces from `domina` library


```clj
cljs.user> (require '[domina.core :as dom]
                    '[domina.css :as css])
nil
```

and then let's see if we're able to select the label for the `quantity`
input field by using the
[`label[for=quantity]`](http://stackoverflow.com/questions/2599627/how-to-select-label-for-email-in-css)
CSS attribute selector:

```clj
cljs.user> (css/sel "label[for=quantity]")
#object[domina.css.t_domina$css8251]
```

So far, so good. Let's see if we are able to get the text associated
with that label:

```clj
cljs.user> (dom/text (css/sel "label[for=quantity]"))
"Quantity"
```

It worked. Our goal is now to replace that text with a new one and to
add the `help` class to the label in such a way that the `styles.ccs`
included in the project will render that text in red color

```clj
cljs.user> (dom/set-text! (css/sel "label[for=quantity]") "Quantity has to be an integer number")
#object[domina.css.t_domina$css8251]
```

If you now take a look at the Shopping Calculator form in the browser,
you should see the new text instead of the original `Quantity`
label. Now let's try to add the `help` class to the label:

```clj
cljs.user> (dom/add-class! (css/sel "label[for=quantity]") "help")
#object[domina.css.t_domina$css8251]
```

But we'll need also to remove a class from an element

```clj
cljs.user> (dom/remove-class! (css/sel "label[for=quantity]") "help")
#object[domina.css.t_domina$css8251]
```

We got it. We learned enough at the bREPL about `domina.core` and
`domina.css` namespaces and we can start coding in the
`src/cljs/modern_cljs/shopping.cljs` source file.

First, we start by adding `add-class`, `remove-class!`, `set-text!`
and `text` symbols to the `refer` section of `domina.core`
requirement:


```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [add-class!    ;; to add a class
                                 remove-class! ;; to remove a class
                                 set-text!     ;; to set text
                                 text          ;; to get text
                                 ...]]
            ...
  (:require-macros ...))
```

Then we need to add the `modern-cljs.shopping.validators` to the
`require` section of the namespace declaration

```clj
(ns modern-cljs.shopping
  (:require ...
            [modern-cljs.shopping.validators :refer [validate-shopping-field
                                                     validate-shopping-form]])
  (:require-macros ...))
```

Note that we're referring both the newly defined individual
`validate-shopping-field` validator and the aggregate
`validate-shopping-form` validator. This choice will become clearer
later when we'll update the `calculate` function.

Let's now attach a listener for the `blur` event to each field of the form:

```clj
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    ;; get original labels' texts
    (let [quantity-text (text (sel "label[for=quantity]"))
          price-text (text (sel "label[for=price]"))
          tax-text (text (sel "label[for=tax]"))
          discount-text (text (sel "label[for=discount]"))]
      ;; quantity validation
      (listen! (by-id "quantity")
               :blur
               (fn [_] (validate-shopping-field! :quantity quantity-text)))
      ;; price validation
      (listen! (by-id "price")
               :blur
               (fn [_] (validate-shopping-field! :price price-text)))
      ;; tax validation
      (listen! (by-id "tax")
               :blur
               (fn [_] (validate-shopping-field! :tax tax-text)))
      ;; discount validation
      (listen! (by-id "discount")
               :blur
               (fn [_] (validate-shopping-field! :discount discount-text))))

    ;; calculate
    (listen! (by-id "calc")
             :click
             (fn [evt] (calculate! evt)))
    ;; show help
    (listen! (by-id "calc")
             :mouseover
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div#help.help "Click to calculate"]))))  ;; hiccups
    ;; remove help
    (listen! (by-id "calc")
             :mouseout
             (fn []
               (destroy! (by-id "help"))))))
```

There are few important things to be noted here:

1. we wrapped the field listeners for `blur` events inside a `let`
   form to keep memory of their original text labels. This is because
   we need to set them again when the input values are valid;
2. each `blur`listener uses the same `validate-shopping-field!`
   function and passes to it the input field it is listening to and the
   original text label captured by the `let` form; note that the name
   of the listener is almost the same as the name of the validator
   (i.e., `validate-shopping-field`). We only added the bang `!` to
   underline that it has side-effect on the DOM;
3. we also modified the button listeners to `mouseover` and `mouseout`
   events. This is because we want to protect the removal of the
   `help` class from the invalid field when the user move the mouse
   out of the button area;
4. we changes the name of the button click listener from `calculate`
   to `calculate!`, because this function is going to have side-effect
   on the DOM as well.

Let's now define the `validate-shopping-field!` listener  above `init` in `shopping.cljs`:

```clj
(defn validate-shopping-field! [field text]
  (let [attr (name field)
        label (sel (str "label[for=" attr "]"))]
    (remove-class! label "help")
    (if-let [error (validate-shopping-field field (value (by-id attr)))]
      (do
        (add-class! label "help")
        (set-text! label error))
      (set-text! label text))))
```

As you see the definition of the `validate-shopping-field!` listener
is quite easy:

* first we get the label for the input field in the `let` form;
* then we remove the `help` class, even when it's not present
  because it does not hurt;
* next, if there is any error resulting from the individual input
  validator, we add the `help` class to the label and set its text to
  the returned error;
* if the input value is valid, we just set the text of the label to
  its original value.

We are not finished yet. What happens when some input values are
invalid and the user clicks the Calculate button? At a minimum we should
prevent the remote calculate function from being called as follows:

```clj
(defn calculate! [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))
        errors (validate-shopping-form quantity price tax discount)]
    (if-not errors
      (remote-callback :calculate
                       [quantity price tax discount]
                       #(set-value! (by-id "total") (.toFixed % 2))))
    (prevent-default evt)))
```

As soon as you save the file, the `tdd` environment recompiles the
source files and re-executes both the CLJS and the CLJ tests.

```bash
Writing clj_test/suite.cljs...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Running cljs tests...
Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 3 tests containing 55 assertions.
0 failures, 0 errors.

Testing modern-cljs.login.validators-test

Testing modern-cljs.shopping.validators-test

Ran 4 tests containing 56 assertions.
0 failures, 0 errors.
Elapsed time: 2.347 sec
```

As you remember, anytime you modify the exported `init` function that
is attached to the `onload` event of the corresponding HTML page, you
need to manually reload the page itself to appreciate the effects of
your changes: reload the page and see the individual fields validators at
work by playing with the Shopping Form.

## On Improving UX (User eXperience)

One more thing: while playing with the Shopping Calculator, you need to click
the `Calculate` button to calculate the total anytime you change an input value.
We could improve the user experience of the Shopping Calculator and it's
very easy too. I leave to you to understand the following final code
for improving the Shopping Calculator UX.


```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [add-class!
                                 append!
                                 by-class
                                 by-id
                                 destroy!
                                 remove-class!
                                 set-value!
                                 set-text!
                                 text
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [domina.css :refer [sel]]
            [hiccups.runtime]
            [modern-cljs.shopping.validators :refer [validate-shopping-field
                                                     validate-shopping-form]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))

(defn calculate! []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))
        errors (validate-shopping-form quantity price tax discount)]
    (if-not errors
      (remote-callback :calculate
                       [quantity price tax discount]
                       #(set-value! (by-id "total") (.toFixed % 2))))))

;;; validate-shopping-filed now takes an event argument too
(defn validate-shopping-field! [evt field text]
  (let [attr (name field)
        label (sel (str "label[for=" attr "]"))]
    (remove-class! label "help")
    (if-let [error (validate-shopping-field field (value (by-id attr)))]
      (do
        (add-class! label "help")
        (set-text! label error))
      (do
        (set-text! label text)
        (calculate!)               ;; trigger the calculation
        (prevent-default evt)))))  ;; and prevent default submission

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    ;; get original labels' texts
    (let [quantity-text (text (sel "label[for=quantity]"))
          price-text (text (sel "label[for=price]"))
          tax-text (text (sel "label[for=tax]"))
          discount-text (text (sel "label[for=discount]"))]
      ;; quantity validation
      (listen! (by-id "quantity")
               :input
               (fn [evt] (validate-shopping-field! evt :quantity quantity-text)))
      ;; price validation
      (listen! (by-id "price")
               :input
               (fn [evt] (validate-shopping-field! evt :price price-text)))
      ;; tax validation
      (listen! (by-id "tax")
               :input
               (fn [evt] (validate-shopping-field! evt :tax tax-text)))
      ;; discount validation
      (listen! (by-id "discount")
               :input
               (fn [evt] (validate-shopping-field! evt :discount discount-text))))
    (listen! (by-id "calc")
             :click
             (fn [evt]
               (calculate!)
               (prevent-default evt)))
    (listen! (by-id "calc")
             :mouseover
             (fn [_]
               (append! (by-id "shoppingForm")
                        (html [:div#help.help "Click to calculate"]))))  ;; hiccups
    (listen! (by-id "calc")
             :mouseout
             (fn [_]
               (destroy! (by-id "help"))))
    (calculate!)))
```

As usual, when you save the above changes, the [TDD][6] environment
triggers the recompilation and re-executes the tests. By having
changed the `init` function, you need to manually reload the
[Shopping Calculator page](localhost:3000/shopping.html). Now, as soon
as you type a value in any input field, the Shopping Calculator will
show you in red the calculated Total.

I'm really bad with HTML/CSS and I'm pretty sure that most of you can
make the UX even better than this.

You can now stop the CLJ REPL and the boot process, and then reset the
branch as usual:

```bash
git reset --hard
```

## Next Step - [Tutorial 19 - Living' on the edge][3]

In the [next tutorial][3] we're going to explain how to make a library
compliant with the new Reader Conditionals extension on CLJ/CLJS
compilers.

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-17.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-19.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/supplemental-material/enable-disable-js.md
[5]: https://en.wikipedia.org/wiki/User_interface#Types
[6]: https://en.wikipedia.org/wiki/Test-driven_development
