# Tutorial 17 - REPLing with Enlive

In the [previous tutorial][1] we reached an important milestone on our
path of reducing code duplication. We were able to share the same form
validation and corresponding unit testing codebase between the client
and server. Anytime in the future that we need to update the
validation rules and the corresponding unit testing, we'll be able to
do it in one shared location, which is a big plus in terms of
maintenance time and costs.

## Preamble

To start working from the end of the [previous tutorial][1], assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-16
```

## Introduction

In this tutorial we're going to integrate the validators for the
Shopping Calculator into the corresponding WUI (Web User Interface) in
such a way that the user will be notified with the corresponding help
messages when the she/he enters invalid values in the form.

We have two options. We can be religious about the progressive
enhancement strategy and then start injecting the validators into the
server-side code. Or we can be more agnostic and start injecting the
validators into the client-side code. Although this series of
tutorials is mainly dedicated to CLJS, we're going to start from the
CLJ code first, and forget about the CLJS code for a while.

We already used the [Enlive][2] lib in the
[Tutorial 13 - Better Safe Than Sorry (Part 1)][3] to implement the
server-side only Shopping Calculator. Even if we were a little bit
stingy in explaining the [Enlive][2] mechanics, we were able to
directly connect the `/shopping` action coming from the `shoppingForm`
submission to the `shopping` template by exploiting the fact that the
`deftemplate` macro implicitly defined a function with the same name
as the defining template.

```clj
;;; src/clj/modern_cljs/core.clj
(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (POST "/login" [email password] (authenticate-user email password))

  ;; the `/shopping` URI is linked to the `shopping` function
  (POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))

  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))
  
```

```clj
;; src/clj/modern_cljs/templates/shopping.clj
;; deftemplate implicitly define the `shopping` function
(deftemplate shopping "shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value (format "%.2f" (calculate 
                                             quantity
                                             price
                                             tax
                                             discount))))
```

However, as we saw in the
[Tutorial 14 - Better safe than sorry (Part 2)][4], we can easily
break the `shoppingForm` by just entering a value that is not a
number, because the `calculate` function is only able to deal with
stringified numbers.

![ServerNullPointer][5]

To start fixing this bug, we introduced form validators and we made
them portable from CLJ to CLJS by simply using the
[Reader Conditionals](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
new features introduced with the `1.7.0` release if CLJ/CLJS
compilers.

With the intent of covering as many as possible usages for the
Shopping Form, we then introduced unit testing and, thanks again to
the Reader Conditionals feature, we made them portable as well.

## Code Refactoring again

All this work it's not useful at all without injecting the form
validators in the form they are intended to validate. To reach this
goal we need to refactor the code again.

### Step One - The middle man

Instead of directly associating the `POST "/shopping` request with the
corresponding `shopping` Enlive template, we are going to intermediate
the latter with a new function which passes the result of the
validators to it.

### Start the TDD environment

As usual we like to work in a live environment. This time we can even
exploit the result of the [previous tutorial][1] by launching the
`tdd` task which offers an auto-running test's suite for both CLJ and
CLJS as well.

```bash
cd /path/to/modern-cljs
boot tdd
...
Elapsed time: 26.573 sec
```

Launch the client REPL as well

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

and visit the
[Shopping Calculator](http://localhost:3000/shopping.html) URI. Then
disable JavaScript from the setting of the browser's developer tools
and reload the page.

Open the `shopping.clj` source file from the
`src/clj/modern_cljs/templates` directory and modify it as follows.

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]
            ;; added the requirement for the form validators
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

;; template renamed
(deftemplate update-shopping-form "public/shopping.html"
  [quantity price tax discount errors] ; added errors argument
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (double (calculate quantity price tax discount)))))

;; new intermediate function
(defn shopping [q p t d]
  (update-shopping-form q p t d (validate-shopping-form q p t d)))
```

> NOTE 1: By defining the new intermediate function with the same name
> (i.e. `shopping`) previously associate with the `POST "/shopping"`
> request, we do not need to modify the `defroutes` macro call in the
> `modern-cljs.core` namespace. Obviously we had to rename the Enlive
> template too.

The first code refactoring step needed for injecting the validators in
the form has been very easy.

As soon as you save the file, the TDD environment re-executes the
validators tests for both CLJ and CLJS. 

## Don't panic with Enlive

We now need to manipulate the HTML source to inject the eventual
error/help message in the right place for each invalid input value
typed in by the user.

For example, if the user typed in "foo" as the value of the the
`price` input field we'd like to show to her/him the following
notification.

![priceError][6]

The original HTML fragment

```html
<div>
   <label for="price">Price Per Unit</label>
   <input type="text"
          name="price"
          id="price"
          value="1.00"
          required>
</div>
```

has to be transformed in the following HTML fragment

```html
<div>
   <label class="help" for="price">Price has to be a number</label>
   <input type="text"
          name="price"
          id="price"
          value="foo"
          required>
```

As we quickly learned in a [previous tutorial][3], [Enlive][2] is very
powerful. By adopting a superset of CSS-like selectors and predefining
a rich set of transformers it should allow us to make the needed HTML
transformation.

That said, at the beginning [Enlive][2] is not so easy to work with,
even by following some [good tutorials][7] available online. Enlive is
full of very clever macros and HOFs definitions which constitute a DSL
(Domain Specific Language) for HTML/XML scraping and templating. You
need to take your time getting familiar with the Enlive lib. Generally
speaking, the best way to learn a new library in CLJ is by REPLing
with it.

### REPLing with Hiccup

Before to start REPLing around, do yourself a favor: do your REPLing
by using the [hiccup][8] lib by [James Reeves][9], because it will
save you a headache writing stringified HTML at the REPL.

One of the nice features of `boot` build tool is that it allows you to
add dependencies at runtime when you need to make some experiments
with a lib that you still don't know if you're going to include in
your project.

From the CLJS REPL that we previously launched you can temporarily add
a dependency and then require the needed namespaces as  follows:

```clj
boot.user=> (set-env! :dependencies #(conj % '[hiccup "1.0.5"]))
nil
```

```clj
boot.user=> (require '[hiccup.core :refer [html]])
nil
```

We are ready to start REPLing with [hiccup][8].

[Hiccup][8] is a very simple library to use. It allows us to emit
stringified HTML code from CLJ data structures. It uses vectors to
represent HTML elements, and maps to represent the elements'
attributes.

For example, if we want to create the HTML fragment for the `price`
input field when the user typed in an invalid value (e.g. `foo`), we
could issue the following Hiccup form

```clj
boot.user> (html [:div
                  [:label.help {:for "price"} "Price has to be a number"]
                  [:input#price {:name "price"
                                 :min "1"
                                 :value "foo"
                                 :required "true"}]])
"<div>
   <label class=\"help\" for=\"price\">Price has to be a number</label>
   <input id=\"price\" min=\"1\" name=\"price\" required=\"true\" value=\"foo\" />
</div>"
```

> NOTE 2: As you can see, Hiccup also provides a CSS-like shortcut for
> denoting the `id` and `class` attributes (e.g. `:input#price` and
> `:label.help`)

### REPLing with Enlive

[Christophe Grand][10], the author of [Enlive][2], was aware of the
need to experiment with his powerful and complex DSL in the REPL and
kindly defined a [sniptest][11] macro just for that. The `sniptest`
receives a stringified HTML as a first argument and optionally one or
more pairs of selectors/transformations. This allows it to mimic the
`deftemplate` macro behaviour in the REPL.

In the active REPL, require the Enlive namaspace as follows,

```clj
boot.user> (require '[net.cgrand.enlive-html :as e])
nil
```

and call the `sniptest` macro by passing it, as a single argument, the
call of the Hiccup `html` function, which emits a stringified HTML
code.

```clj
boot.user> (e/sniptest (html [:div [:label {:for "price"} "Price"]]))
"<div>
   <label for=\"price\">Price</label>
</div>"
```

Here we used the `sniptest` macro without any selector/transformation
form and it just returned the passed argument (i.e.  the stringified
HTML fragment built by the Hiccup `html` function)

Let's now add a selector/transformation pair for selecting the `label`
and changing its content from `Price per Unit` to `Price has to be a
number`.

```clj
boot.user> (e/sniptest (html [:div [:label {:for "price"} "Price per Unit"]])
                             [:label] (e/content "Price has to be a number"))
"<div>
     <label for=\"price\">Price has to be a number</label>
 </div>"
```

We obtained what we were expecting. So far so good. But what if there
are, as in our `shopping.html` source, more `label` elements
contained in the `fieldset` element? Let's REPL this scenario.

```clj
boot.user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
                                        [:div [:label {:for "tax"} "Tax (%)"]]])
                       [:label] (e/content "Price has to be a number"))
"<fieldset>
     <div>
         <label for=\"price\">Price has to be a number</label>
     </div>
     <div>
         <label for=\"tax\">Price has to be a number</label>
     </div>
</fieldset>"
```

The `[:label]` selector selected both `label` elements inside the
`fieldset` element. The corresponding transformer consequentely
changed the content of both of them. Luckly, Enlive offers a rich set
of predicates which can be applied to be more specific within the
selectors. One of them is `attr=`, which tests if an attribute has a
specified value. Let's see how it works in our scenarion.

```clj
boot.user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
                                        [:div [:label {:for "tax"} "Tax (%)"]]])
                       [:label (e/attr= :for "price")]
                       (e/content "Price has to be a number"))
"<fieldset>
     <div>
         <label for=\"price\">Price per Unit</label>
     </div>
     <div>
         <label for=\"tax\">Tax (%)</label>
     </div>
 </fieldset>"
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
     (do-> (add-class "error")
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

You should receive the following feedback.

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

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
[2]: https://github.com/cgrand/enlive
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md#break-the-shopping-calculator-again-and-again
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ServerNullPointer.png
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/price-error.png
[7]: https://github.com/swannodette/enlive-tutorial/
[8]: https://github.com/weavejester/hiccup
[9]: https://github.com/weavejester
[10]: https://github.com/cgrand
[11]: https://github.com/cgrand/enlive/blob/master/src/net/cgrand/enlive_html.clj#L959
[12]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-invalid-values.png
[13]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-with-invalid-messages.png
