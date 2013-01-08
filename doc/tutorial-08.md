# Tutorial 8 - Introducing Domina events

Starting from [Tutorial 5][1], we introduced [domina library][2] to
approach CLJS programming in a more clojure-ish way if compared with
using CLJS/JS interop features.

## Introduction

We touched `domina` surface by using `by-id` to select individual
elements from DOM, `value` and `set-value!` to get/set the value of a
form field.

It's now time to see what domina library has to offer for substitutuing
CLJS/JS interop features in managing events.

## Listen to events

Let's go back to the [shopping calculator form][3] we introduce in
Tutorial 5.

First of all, by been cloned from the orginal HTML code of
[Modern JavaScript: Develop and Desing][4], the shopping form used a
`submit` type of button. As the shopping calculator data need not to
be sent to a server-side script to be elaborated, we think it's more
appropriate to use a `button` type and remove both `action` and
`method` attributes from the corresponding `form` tag.

Here is the updated html code.

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Shopping Calculator</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
  <!-- shopping.html -->
  <form id="shoppingForm" novalidate>
    <legend> Shopping Calculator</legend>
    <fieldset>

      <div>
        <label for="quantity">Quantity</label>
        <input type="number"
               name="quantity"
               id="quantity"
               value="1"
               min="1" required>
      </div>

      <div>
        <label for="price">Price Per Unit</label>
        <input type="text"
               name="price"
               id="price"
               value="1.00"
               required>
      </div>

      <div>
        <label for="tax">Tax Rate (%)</label>
        <input type="text"
               name="tax"
               id="tax"
               value="0.0"
               required>
      </div>

      <div>
        <label for="discount">Discount</label>
        <input type="text"
               name="discount"
               id="discount"
               value="0.00" required>
      </div>

      <div>
        <label for="total">Total</label>
        <input type="text"
               name="total"
               id="total"
               value="0.00">
      </div>
      <br><br>
      <div>
        <input type="button"
               value="Calculate"
               id="calc">
      </div>

    </fieldset>
  </form>
  <script src="js/modern_dbg.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

> NOTE 1: In [Tutorial 7][5] we set `:cljsbuild` configuration options
> to generate three different builds: `:dev`, `:pre-prod` and `:prod`
> which emitted three differents JS (i.e. `modern_dbg.js`,
> `modern_pre.js` and `modern.js`). Then we replicated three html file
> (i.e. `shopping-dbg.html`, `shopping-pre.html` and `shopping.html`)
> to include the appropriate JS file emitted by the three different
> builds.  You should replicate the above modification in each
> shopping html file.

Here is the updated shopping calculator form rendered by the browser.

![Shopping calculator][6]

### Domina events

As you perhaps remember, to manage the shopping calculator we defined
the `calculate` function and the `init` function to attach `calculate`
to the `submit` button of the `shoppingForm`.

`domina.events` namespace offers a bunch of functions to manage DOM
events. One of them is the `listen!` function, wich allows to attach
an handling function (e.g. `calculate`) to a DOM event type
(e.g. `click`, `mouseover`, `mouseout`, etc). 

Let's now update `shopping.cljs` by requiring `domina.events`
namespace and by substituting the `.-onsubmit` JS interop with the
`listen!` function as follows:

```clojure
(ns modern-cljs.shopping
  (:require [domina :as dom]
            [domina.events :as ev]))

(defn calculate []
  (let [quantity (dom/value (dom/by-id "quantity"))
        price (dom/value (dom/by-id "price"))
        tax (dom/value (dom/by-id "tax"))
        discount (dom/value (dom/by-id "discount"))]
    (dom/set-value! (dom/by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))))

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (ev/listen! (dom/by-id "calc") :click calculate)))
```

> NOTE 2: We now `:require` `domina` instead of just `:use` it as in
> [previous tutorials][7]. Note that we also deleted the returned
> `false` value from `calculate` definition because, when using
> `button` input type instead of `submit` input type, we need not to
> return the control to the form itself.

> NOTE 3: As usual, the `init` function has been exported to protect its
> name from been changed by Google Closure Compiler aggressive compilation
> used in `:prod` build (i.e. `:advanced`).

You can now compile and run the project as usual:

```bash
$ lein ring server # from modern-cljs home
$ lein cljsbuild auto dev # from modern-cljs home in a new terminal
```

If you want interact with the bREPL, just execute the usual command to
run the bREPL.

```bash
$ lein trampoline cljsbuild repl-listen # from modern-cljs home in a new terminal
```

Verify that everithing is still working as espected by visiting
[`shopping-dbg.html][8] page.

### Bubbling and capture models

Domina library supports both *bubbling* and *capture* event models. In
the above shopping calculator example we used the domina `listen!`
function for handling the mouse `click` event on the *Calculate*
button. `listen!` is member of a group of functions defined by domina
to use the *bubbling* method of handling DOM events. If you want to
experience the *capture* method you have just to substitute in the
`init` definition the `listen!` call with the corresponding `capture!`
call and you're done.

```clojure
;;; the rest as before

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (ev/capture! (dom/by-id "calc") :click calculate)))
```
# [Tutorial 9 - DOM Manipulation][9]

In the next tutorial we'are going to face the need to programmatically
manipulate DOM elements as a result of the occurrance of some DOM
event (e.g. `mouseover`, `mouseout`, etc.).

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[2]: https://github.com/levand/domina
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md#shopping-calculator-sample
[4]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-reviewed.png
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md#modify-validate-form
[8]: http://localhost:3000/shopping-dbg.html
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
