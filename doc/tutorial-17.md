# Tutorial 17 - Less is more

In the [previous tutorial][1] we reached an important milestone on our
path towards the elimination of as much as possibile code
duplication. Indeed, we were able to share the same form validation and
corresponding unit testing codebase between the client and
server. Anytime in the future we should need to update the validation
rules and the corresponding unit testing, we'll do it in one shared
place only, which is a big plus in terms of maintenance time and costs.

## Introduction

In this tutorial we're going to integrate the validators for the
Shopping Calculator into the corresponding WUI (Web User Interface) in
such a way that the user will be notified with some error messages when
the she/he types in invalid values in the form.

We have two options. We can be religious about the progressive
enhancement strategy and then start from the server-side. Or be more
agnostic and then start from the client-side. Although this series of
tutorials is mainly dedicated to CLJS, we decided, at least for this
time, to be more religious than usual and start by integrating the
validators in the server-side code first, and forget for a while about
CLJS code.

## Prepare the field for the HTML transformation

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> $ git clone https://github.com/magomimmo/modern-cljs.git
> $ cd modern-cljs
> $ git checkout tutorial-16
> $ git checkout -b tutorial-17-step-1
> ```

We already used the [Enlive][2] lib in the
[Tutorial 14 - It's better to be safe than sorry (Part 1) - ][3] to
implement the server-side only Shopping Calculator. Even if we were a
little bit stingy in explaining the [Enlive][2] mechanics, we were
able to directly connect the `/shopping` action coming from the
`shoppingForm` submission to the `shopping` template by exploiting the
fact the `deftemplate` macro implicitly define a function with the
same name of the defining template.

```clj
;; src/clj/modern_cljs/core.clj
;; the `/shopping` URI is linked to the `shopping` function
(POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))
```

```clj
;; src/clj/modern_cljs/templates/shopping.clj
;; deftemplate implicitly define the `shopping` function
(deftemplate shopping "public/shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (calculate quantity price tax discount))))
```

However, as we saw in the
[Tutorial 15 - It's better to be safe than sorry (Part 2) - ][4], we
can break the `shoppingForm` by just typing in the form a value that's
not a number, because the `calculate` function is able to deal with
stringified numbers only.

![ServerNullPointer][5]

### Code Refactoring again

To be able to inject the form validators into the the current
server-side code, we need to refactor it again.

#### Step 1

Instead of directly associate the `POST "/shopping` request with the
corresponding `shopping` function, implicitly defined by the
`deftemplate` macro, we are going to rename the template
(e.g. `shopping-form-template`) and define a new `shopping` function
which calls the new template by passing to it, as an added argument,
the result of calling the `validate-shopping-form` validation
function.

Open the `shopping.clj` source file from the
`src/clj/modern-cljs/templates` directory and modify it as follows.

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]
            ;; added the requirement for the form validators
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftemplate shopping-form-template "public/shopping.html"
  [quantity price tax discount errors] ; added errors argument
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (calculate quantity price tax discount))))

(defn shopping [q p t d]
  (shopping-form-template q p t d (validate-shopping-form q p t d)))
```

> NOTE 1: By defining the new intermediate function with the same name
> (i.e. `shopping`) previoulsly associate with the `POST "/shopping"`
> request, we do not need to modify the `defroutes` macro call in the
> `modern-cljs.core` namespace.

The first code refactoring step has been very easy.

#### Step 2

We now need to manipulate/transform the HTML source to inject the
eventual error message in the right place for each invalid input value
typed in by the user.

To make an example, if the user typed in "foo" as the value of the the
`price` input field we'd like to show to her/him the following
notification.

![priceError][9]

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
   <label class="error" for="price">Price has to be a numebr</label>
   <input type="text"
          name="price"
          id="price"
          value="foo"
          required>
```

As we quickly learnt in a [previous tutorial][4], [Enlive][2] is very
powerful. By adopting a superset of CSS-like selectors and predefining
a rich set of transformers it should allow us to make the needed HTML
manipulation.

##### Don't panic

That said, [Enlive][2] is not so easy to work with at the beginning,
even by following some [good tutorials][] available online. It's full
of very smart and nested macros and HOFs. You need to take your time
for learning them. Generally speacking, the best way to learn a new
library in CLJ is to REPLing around.

##### Learn bt REPLing

Before doing that, make you a favor: add the the [hiccup][11] lib by
[James Reeves][12] to the project dependencies. It will allow you to
save an headache in writing string of HTML code to be used with
Enlive.

> NOTE 2: Even better, add the [Pomgranate][13], again by
> [Chas Emerick][14], which allows you to dynamically modify the project
> `classpath` in the REPL.

Open your `project.clj` and add the [hiccup][11] dependency.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :dependencies [...
                 ...
                 [hiccup "1.0.3"]]
  ...
  ...
)
```

Now run the REPL as usual

```bash
$ lein repl
nREPL server started on port 49623
REPL-y 0.2.0
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)

user=>
```

and start REPLing with [hiccup][11] and [Enlive][2].

```clj
user=> (require '[hiccup.core :refer [html]])
nil
user=>
```

Hiccup is a very simple library to be used. It allows to represent
HTML as Clojure data structure. It uses vectors to represent elements,
and maps to represent an element's attributes.

For example, if we want to create an HTML fragment representing the
destination HTML for the `price` input field when the user typed in an
invalid value (e.g. `foo`), we could issue the following Hiccup form

```clj
user=> (html [:div [:label.error {:for "price"} "Price has to be a number"]
                   [:input#price {:name "price"
				                  :min "1"
								  :value "foo"
								  :required "true"}]])
"<div>
   <label class=\"error\" for=\"price\">Price has to be a number</label>
   <input id=\"price\"
          min=\"1\"
		  name=\"price\"
		  required=\"true\"
		  value=\"foo\" />
</div>"
user=>
```

> NOTE 3: As you can see, Hiccup also provides a CSS-like shortcut for
> denoting the `id` and `class` attributes.

##### REPLing with Enlive

[Christophe Grand][15], the author of [Enlive][2], was aware of the
need of experimenting with his powerful and complex lib in the REPL
and kindly defined a [sniptest][16] macro just for that.

In the active REPL, now require the Enlive namaspace as follows,

```clj
user=> (require '[net.cgrand.enlive-html :as e])
nil
user=>
```

and call the `sniptest` macro by passing it as a single argument the
call of the Hiccup `html` function as follows

```clj
user> (e/sniptest (html [:div [:label {:for "price"} "Price"]]))
"<div><label for=\"price\">Price</label></div>"
user>
```

Here we used the `sniptest` macro without any selector/transformation
form and it just returned the passed argument (i.e.  the stringified
HTML fragment built by the Hiccup `html` function)

Let's now try to select the `label` and change its content from `Price
per Unit` to `Price has to be a number`.

```clj
user> (e/sniptest (html [:div [:label {:for "price"} "Price per Unit"]])
		          [:label] (e/content "Price has to be a number"))
"<div>
     <label for=\"price\">Price has to be a number</label>
 </div>"
user>
```

We obtained what we were expecting. So far so good. But what if there
are more `label` elements? Let's REPLing this scenario.

```clj
user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
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
user> 
```

The `[:label]` selector selected both `label` elements inside the
`fieldset` element. The corresponding transformer consequentely
changed the content of both of them. Luckly, Enlive offers a rich set
of predicates which can be applied to be more specific within the
selectors. One of them is `attr=`, which tests if an attribute has a
specified value. Let's see how it works in our scenarion.

```clj
user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
				                   [:div [:label {:for "tax"} "Tax (%)"]]])
		          [:label (e/attr= :for "price")] (e/content "Price has to be a number"))
"<fieldset>
     <div>
	     <label for=\"price\">Price per Unit</label>
	 </div>
	 <div>
	     <label for=\"tax\">Tax (%)</label>
	 </div>
 </fieldset>"
user> 
```

Ops, it did not work. What happened?

##### Pay attention when usign predicates

This unexpected behaiour has to do with an Enlive selectors' rule to
be used when applied to predicates. The
`[:label (e/attr= :for "price")]` selector is going to match any
element with a `for` attribute valued to `"price"` inside a `label`
element.  In our scenario there are no other elements inside any
`label` element, so the selector did not selected any node.  On the
other hand, `[[:label (attr= :for "price")]]` is going to match any
`label` with a `for` attribute valued to `"price"`, which is what we
want.

So, we need to put the whole selector in a nested vector. Let's see if
it works.

```clj
user> (e/sniptest (html [:fieldset [:div [:label {:for "price"} "Price per Unit"]]
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
user> 
```
Ok, now it worked. 

#### Ready to go


As you probably know, there is no way in
CSS to select a parent and neither an antecedent sibling.
We should the fact the there is no a parent selector in CSS and
neither a previous sibling selector. Probably, the best thing to do
would be to move in the HTML source `the `id` attribute from the `input`
element to the `div` element (i.e. its parent), but we don't want to
bother with the designer of the page. So, let's stay as we are and try
to find a workaround.






input filed with the `id="price`"
 selectors do not allow to select a parent of
a node (i.e. CSS is not ascendent)
Now the things are going to be more complicated, becasue there are more
way to rea

For each input field we now need to substitute in the `deftemplate`
macro call the original transformer (i.e. `set-attr`) with a new one
which receives both the value typed in by the user and, if the value
was invalid, the corresponding error message produced by the
`validate-shopping-form` validation function.

Modify again the source file as follows.

```clj
(defn update-attr [value error]
  (set-attr :value value))

(deftemplate shopping-template "public/shopping.html"
  [quantity price tax discount errors]
  [:#quantity] (update-attr quantity (first (:quantity errors)))
  [:#price] (update-attr price (first (:price errors)))
  [:#tax] (update-attr tax (first (:tax errors)))
  [:#discount] (update-attr discount (first (:discount errors)))
  [:#total] (set-attr :value
                      (format "%.2f" (calculate quantity price tax discount))))
```

> NOTE 2: As you should remember, the `validate-shopping-form` function
> returs `nil` when all the input values of the `shoppingForm` are valid
> and return a map of errors messages (e.g. `{:quantity
> ["Quantity can't be negative"] :price ["Price ha to be a number"]}` when
> any value is invalid. The value of each key of the returned map is
> always a vector of messages, even when there is just one of them, and we
> need to take the `first`.

At the moment the `update-attr` is just calling the `set-attr` function
and substitutes all its occurrences, but the last one, in the
`shopping-template` definition. This is because we want to verify that
the code refactoring done until now does not break the `calculate` happy
path.

Now run the server as usual.

```bash
$ lein ring server-headless
```

Disable the JavaScript engine of your browser and visit the
[shopping.html][10] URL. Fill the form with valid values and click the
`Calculate` function. Everything should still work as before the code
refactoring. The `calculate` happy path is still working. So far so
good.

## HTML transformation

It's now time to take care of the error messages notification to the
user by adding the needed HTML transformation into the `update-attr`
function definition which, at the moment, does only set the value of the
selected input node.

First we should clarify to ourself what kind of transformation we want
to operate on the HTML page when there is any input value that is
invalid.

We start from the following HTML snippet of code from the
`shopping.html` file which resides in the `resources/public` directory.

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

Even if I'm very bad both in HTML and CSS, I think that the following
could be an acceptable transformation of the original HTML snippet to
notify the user of the invalid `price` value.

```html
<div>
   <label for="price" class="error">Price has to be a number</label>
   <input type="text"
          name="price"
          id="price"
          value="foo"
          required>
</div>
```



Here we substituted the `content` of the `label` associated to the
`price` input field with the corresponding error massage received from
the `validate-shopping-form` and kept the value typed in by user.



# Next - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md
[2]: https://github.com/cgrand/enlive
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-15.md#break-the-shopping-calculator-again-and-again
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ServerNullPointer.png
[6]: http://en.wikipedia.org/wiki/Syntactic_sugar
[7]: http://en.wikipedia.org/wiki/Closure_(computer_science)
[8]: http://en.wikipedia.org/wiki/Higher-order_function
[9]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/price-error.png
[10]: http://localhost:3000/shopping.html
[11]: https://github.com/weavejester/hiccup
[12]: https://github.com/weavejester
[13]: https://github.com/cemerick/pomegranate
[14]: https://github.com/cemerick
[15]: https://github.com/cgrand
[16]: https://github.com/cgrand/enlive/blob/master/src/net/cgrand/enlive_html.clj#L959
[17] :https://github.com/cgrand/enlive/blob/master/src/net/cgrand/enlive_html.clj#L538
[18]: http://www.clojurebook.com/



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
