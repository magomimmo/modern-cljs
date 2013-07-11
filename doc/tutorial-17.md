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
enhancement strategy or be more agnostic. Although this series of
tutorials is mainly dedicated to CLJS, we decided to be less agnostic
than usual and we're going to start by integrating the validators in
the server-side code first, and forget for a while about CLJS/JS code.

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
little bit stingy in explaining the [Enlive][2] mechanics, we were able
to directly connect the `/shopping` action coming from the
`shoppingForm` submission to the `shopping` template by exploiting the
fact the `deftemplate` macro implicitly define a function with the same
name of the defining template.

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
[Tutorial 15 - It's better to be safe than sorry (Part 2) - ][4], we can
break the `shoppingForm` by just typing in the form a value that's not a
number, because the `calculate` function is able to deal with
stringified numbers only.

![ServerNullPointer][5]

### Refatoring to inject the form validators

We now need to refactor again the code. We want to insert a call to
the `validate-shopping-form` validation function between the received
request (i.e. `POST "/shopping"`) and the `shopping` function call, in
such a way that when any typed in value is invalid we can notify the
corresponding error message to the user by transforming/manipulting
the `shoppingForm` form.

#### Step 1

So, instead of directly associate the `POST "/shopping` request with the
corresponding function implicitly defined by the `deftemplate` macro, we
are going to associate it with a new function, which intermediates the
call for rendering HTML page by first getting the result from the
`validate-shopping-form` validation function.

Open the `shopping.clj` source file from the
`src/clj/modern-cljs/templates` directory and modify it as follows.

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(deftemplate shopping-template "public/shopping.html"
  [quantity price tax discount errors]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (calculate quantity price tax discount))))

(defn shopping [q p t d]
  (shopping-template q p t d (validate-shopping-form q p t d)))
```

First we added the `modern-cljs.shopping.validators`
namespace requirement to be able to refer the `validate-shopping-form`
function.

Then we renamed the Enlive template definition and added to it a fifth
errors argument feeded by a new `shopping` function which directly
receives the arguments from the `POST "/shopping"` request.

> NOTE 1: By defining the new intermediate function with the same name
> (i.e. `shopping`) previoulsly associate with the `POST "/shopping"`
> request, we needn't to modify the `defroutes` macro call in the
> `modern-cljs.core` namespace.

#### Step 2

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

At the moment the `update-attr` is just calling the `set-attr`
function and substitutes all its occurrences, but the last one, in the
`shopping-template` definition. This is because we want to verify that
the code refactoring done until now does not break the application.

Now run the server as usual.

```bash
$ lein ring server-headless
```

Disable the JavaScript engine of your browser and visit the
[shopping.html][10] URL. Fill the form with valid values and click the
`Calculate` function. Everything should still work as before the code
refactoring. So far so good.

## HTML transformation

It's now time to take care of the error messages notification to the
user by adding the needed HTML transformation to the `update-attr`
function which at the moment does only set the value of the selected
input node.

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

![priceError][9]

Here we substituted the `content` of the `label` associated to the
`price` input filed with the corresponding error massage received from
the `validate-shopping-form`.

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
