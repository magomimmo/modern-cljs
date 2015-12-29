# Tutorial 18 - Live coding with TDD

In the [previous tutorial][1] we integrated the validators for the
Shopping Calculator into the corresponding WUI (Web User Interface) in
such a way that the user will be notified with the corresponding help
messages when she enters invalid values in the form. By first
injecting the validators into the server-side code, we have been
religious about the progressive enhancement strategy. It's now time to
fill the gap by injecting the portable validators into the client-side
WUI as well.

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
the Shopping Calculator into the client-side WUI (Web User Interface).

While there is nothing really new to be learned in this tutorial about
CLJS, it represents a good opportunity to see at work the live coding
TDD environment we setup in the
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
their corresponding documentation been updated as well.

Even if you are a TDD practitioner, which means that you start coding
from a failing test, you still need to know and to understand your
programming language and the libraries you're going to use to fix the
failed tests and to refactor your code to obtain a cleaner and more
maintainable code base. In that regard, Clojure(Script) REPLs are your
best friends as they are for the ones, like myself, not starting to
code from a test that has to fail.

## Start the live coding TDD environment

Start the live coding TDD environment:

```bash
cd /path/to/modern-cljs
boot tdd
...
Elapsed time: 26.573 sec
```

Now launch the client REPL as usual

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

Remember to reactivate the JS engine from the Developer Tools of your
browser and then visit the
[Shopping Calculator](http://localhost:3000/shopping.html) URI to
activate the websocket connection used by `tdd` to reload pages when
you save some changes.

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
`discount` inout fields all together. As you remember, when some
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

## Start from a test that has to fail

On the client side WUI we want to individually validate an input value
as soon as we leave the corresponding input fields (i.e. when the
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
Shopping Calculator:

```clj
(deftest validate-shopping-quantity-test 
  (testing "Shopping Form Quantity Validation"
    (testing "/ Happy Path"
      (are [expected actual] (= expected actual)
        nil (validate-shopping-quantity "1")))))
```

Here the intention is to test an happy path: the validator
`validate-shopping-quantity`, that still does not exist, should return
`nil`, meaning no errors, when called with an integer argument.

As soon as you save the file you'll receive the expected error from
running the test on both CLJS and CLJS.

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
we start fixing the error by adding `validate-shopping-quantity` to
the `:refer` section of the `modern-cljs.shopping.validators`
namespace requirement in the `modern-cljs.shopping.validators-test`
namespace declaration:

> NOTE 1: we could define the individual client-side input validators in
> a `.cljs` source file.

```clj
(ns modern-cljs.shopping.validators-test
  (:require [modern-cljs.shopping.validators :refer [validate-shopping-form
                                                     validate-shopping-quantity]]
            #?(:clj [clojure.test :refer [deftest are testing]]
               :cljs [cljs.test :refer-macros [deftest are testing]])))
```

As soon as you save the file, you'll receive again an expected error:

```bash
...
adzerk.boot_cljs.util.proxy$clojure.lang.ExceptionInfo$ff19274a: Referred var modern-cljs.shopping.validators/validate-shopping-quantity does not exist
    ...    
Elapsed time: 0.275 sec
```

Note that the same error is notified in the browser as well.

## Satisfy the test

To make the assertion to pass, we need to define the
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
same time it's suggesting us that `validate-shopping-price`,
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
not remember quite well how they work to be able to choose the one
that better fit out case. Dont's worry, we have the REPL to experiment
with:

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

and experiment with it

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (cond (= field :quantity) (v/validate-shopping-form val "0" "0" "0")
                   (= field :price) (v/validate-shopping-form "1" val "0" "0")))
nil
````

```clj
cljs.user> (let [field :quantity
                 val "-1"]
             (cond (= field :quantity) (v/validate-shopping-form val "0" "0" "0")
                   (= field :price) (v/validate-shopping-form "1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
nil
```

It works, but it's a little bit verbose. Let's see if `condp` has
something better to offer by first getting its docstring:

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

and then experimenting with it

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (condp = field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "1" val "0" "0")))
nil
```

```clj
cljs.user> (let [field :quantity
                 val "-1"]
             (condp = field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
nil
```

A little bit less verbose. Perhaps `case` form is even better than
`condp`. Get its docstring:

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

`case` macro seems to
[better fit our need](http://insideclojure.org/2015/04/27/poly-perf/). Let's
try it at bREPL:

```clj
cljs.user> (let [field :quantity
                 val "1"]
             (case field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
nil
cljs.user> (let [field :quantity
                 val "-1"]
             (case field
               :quantity (v/validate-shopping-form val "0" "0" "0")
               :price (v/validate-shopping-form "-1" val "0" "0")))
{:quantity ["Quantity can't be negative"]}
```

Now that we understood a little bit better our beloved CLJ/CLJS
programming language by experimenting with it at the REPL, while
following a TDD approach, go back to the
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
still referenced in the test file. Let's fix it:

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
very easy to add few other assertions as well.

As soon as you save the file, the `tdd` environment fires the
recompilation and the re-execution of test tests:

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
interested in.

## Fill the Gap

In the previous paragraph we experimented an augmented TDD workflow by
interleaving few experiments with the language in REPL while
satisfying tests and consequently refactoring some code.

But we still have to attach the newly defined individual field
validators to the Shopping Calculator. Let's do that.





```html
<div>
   <label class="help" for="price">Price has to be a number</label>
   <input type="text"
          name="price"
          id="price"
          value="foo"
          required>
```

Ops, it did not work. What happened?

### Hierarchical and conjunction rules

This unexpected behaiour has to do with the Enlive DSL grammar's
rules. The syntax of the `[:label (e/attr= :for "price")]` selector
says to select any element with a `for` attribute valued to `"price"`
*contained* in a `label` element (i.e. hierarchical rule). In our
scenario there were no other elements contained inside any `label`
element, so the selector did not select any node and the trasformer
does not do anything.

On the other hand, the syntax of the `[[:label (attr= :for "price")]]`
selector is going to select any `label` which has a `for` attribute
valued to `"price"` (i.e. conjunction rule) and this is what we
want. So, to activate the conjunction rule, we need to put the whole
selector in a nested vector. Let's see if it works.

```clj
boot.user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
                                        [:div [:label {:for "tax"} "Tax (%)"]]])
                       [[:label (e/attr= :for "price")]] (e/content "Price has to be a number"))
"<fieldset>
     <div>
         <label for=\"price\">Price has to be a number</label>
     </div>
     <div>
         <label for=\"tax\">Tax (%)</label>
     </div>
</fieldset>"
```

Good. It worked and we're now ready to apply what we just learnt by
REPLing with the `sniptest` macro.

> NOTE 3: Enlive selector syntax offers a disjunction rule too, but
> we're not using it in this tutorial. This rule use the
> `#{[selector 1][selector 2]...[selector n]}` set syntax for meaning
> disjunction between selectors.

### Select and transform

Let's resume the `update-shopping-form` template definition we wrote
in the first refactoring step.

```clj
(deftemplate update-shopping-form "shopping.html"
  [quantity price tax discount errors]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (double (calculate quantity price tax discount)))))
```

Here we defined five pairs of selectors/transformations, one for each
input field of the form. All but the final transformer just set the
corresponding input field value to the value typed in by the user. The
`:#total` input field, instead, is set to the value returned by
the `calculate` function which throws the `NullPointer` exception when
it receives a not stringified number as one of the argument.

First, change the last selector/transformation pair to call the
`calculate` function only when there are no validation errors
(i.e. when `validate-shopping-form` return `nil`).

```clj
(deftemplate update-shopping-form "shopping.html"
  [quantity price tax discount errors]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (if errors
              (set-attr :value "0.00")
              (set-attr :value (format "%.2f" (double (calculate quantity price tax discount))))))
```

Now we have to substitute the content of each `label` relating each
input field with the corresponding error message when its value is
invalid.  As we learned from the previous REPL session with the
`sniptest` macro, to select a single `label` content we can use the
`[[:label (attr= :for <input-name>)]]` selector. But what about the
corresponding transformer? We want to transform the `content` of the
`label` and set its `class ` to `"help"` only when the related
input field value is invalid, that is when there are error messages
for it.

```clj
(deftemplate update-shopping-form "shopping.html"
  [quantity price tax discount errors]
  ...
  ...
  [[:label (attr= :for "quantity")]] (if-let [err (first (:quantity errors))]
                                        (do-> (add-class "help")
					                          (content err))
                                        identity)
  ...
  ...
)
```

Let's analyze the above code. We already discussed the
`[[:label (attr= :for <input-name>)]]` selector. The corresponding
trasformer says:

> **IF** there is an error message pertaining the value for the
> `quantity` input field (i.e. `(first (:quantity errors))`), **THEN**
> add the `"help"` class to the `label` element and set the help
> message as the `content` of the `label`, **ELSE** do nothing
> (i.e. `identity`).

As you can see we are using few more Enlive symbols:

* `do->`: It often happens that you need to apply multiple transformations
  to the same selected HTML node. The `do->` function chains
  (i.e. composes) transformations sequentially from left to right
  (i.e. top to bottom);
* `add-class`: lets you add one or more CSS classes to a selected
  HTML node;
* `content`: replaces the content of a selected HTML node with the
  passed one.

Note that when the value for the input field is valid, we use the CLJ
`identity` predefined function to leave the content of the element as
it was.

### Syntactic sugar

The above transformer is very boring to be repeated for each `label`
of the corresponding `shoppingForm` input fields, but we're coding
with a LISP programming language and we can express the above
transformation with the following `maybe-error` simple macro which
receives an expression and expands into the above convoluted code.

```clj
(defmacro maybe-error [expr]
  `(if-let [x# ~expr]
     (do-> (add-class "help")
           (content x#))
     identity))
```

### Step 2 - Ready to go

We're now ready to finish our `deftemplate` definition. Following is
the entire content of the `shopping.clj` source file.

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate
                                            content
                                            do->
                                            add-class
                                            set-attr
                                            attr=]]
            [modern-cljs.remotes :refer [calculate]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(defmacro maybe-error [expr]
  `(if-let [x# ~expr]
     (do-> (add-class "help")
           (content x#))
     identity))

(deftemplate update-shopping-form "shopping.html"
  [q p t d errors]

  ;; select and transform input label

  [[:label (attr= :for "quantity")]] (maybe-error (first (:quantity errors)))
  [[:label (attr= :for "price")]] (maybe-error (first (:price errors)))
  [[:label (attr= :for "tax")]] (maybe-error (first (:tax errors)))
  [[:label (attr= :for "discount")]] (maybe-error (first (:discount errors)))

  ;; select and transform input value

  [:#quantity] (set-attr :value q)
  [:#price] (set-attr :value p)
  [:#tax] (set-attr :value t)
  [:#discount] (set-attr :value d)

  ;; select and transform total

  [:#total] (if errors
              (set-attr :value "0.00")
              (set-attr :value (format "%.2f" (double (calculate q p t d))))))

(defn shopping [q p t d]
  (update-shopping-form q p t d (validate-shopping-form q p t d)))
```

### Play and Pray

We're now ready to verify if our long refactoring session works.

Then, after having disabled the JavaScript engine of your browser,
visit the [Shopping Calculator](http://localhost:3000/shopping.html)
URI, fill the form with valid values and finally click the `Calculate`
button. Everything should work as expected.

Let's now see what happens if you type any invalid value into the form,
for example `1.2` as the value for the `Quantity` input field, `foo`
as the value for `Price`, `bar` as the value for `Tax` and finally
nothing as the value for `Discount`.

![Shopping with invalid values][12]

You should receive the following feedback

![Shopping  with error messages][13]

It worked as expected. We fixed the server-side code by refactoring it
to inject the form validators into the Enlive template definition for
the `shopping.html` page. And we also learned a little bit more about
the Enlive DSL.

Stop the CLJ REPL and the `boot` processes and reset the branch as
usual:

```bash
git reset --hard
```

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-17.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md

[2]: https://github.com/cgrand/enlive
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md#break-the-shopping-calculator-again-and-again
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ServerNullPointer.png
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/price-error.png
[7]: https://github.com/swannodette/enlive-tutorial/
[8]: https://github.com/weavejester/hiccup
[9]: https://github.com/weavejester
[10]: https://github.com/cgrand
[11]: https://github.com/cgrand/enlive/blob/master/src/net/cgrand/enlive_html.clj#L982
[12]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-invalid-values.png
[13]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-with-invalid-messages.png
