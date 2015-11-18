# Tutorial 7 - Introducing Domina Events

Starting from [Tutorial 5][1], we introduced the [domina library][2] to
approach CLJS programming in a more Clojure-ish way, rather than
using CLJS/JS interop features.

## Preamble

If you want to start working from the end of the [previous tutorial][5],
assuming you've [git][10] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-06
git checkout -b tutorial-07-step-1
```

## Introduction

We touched `domina` superficially by using `by-id` to select individual
elements from the DOM, and `value` and `set-value!` to get/set the value of a
form field.

It's now time to see what the domina library has to offer for substituting
CLJS/JS interop features in managing events.

## Listen to events

Let's go back to the [shopping calculator form][3] we introduced in
Tutorial 5.

First of all, by having been cloned from the orginal HTML code of
[Modern JavaScript: Develop and Design][4], the shopping form used a
`submit` type of button. At the moment, the shopping calculator data
are not sent to a server-side script to be validated. Until we'll
introduce a server-side script we are going to use a `button` type
with the `calc` id. We also removed both the `action` and `method`
attributes from the corresponding `form` tag.

> NOTE 1: We know that, by substituting `submit` with `button` type,
> we're breaking the progressive enhancement strategy, but for now
> we're focusing on the [domina events][2] machinery. Will fix this in
> a subsequente tutorial.

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

Here is the updated shopping calculator form as rendered by the browser.

![Shopping calculator][6]

### Domina events

As you perhaps remember, to manage the shopping calculator we defined
the `calculate` function and the `init` function to attach the former
to the `submit` button of the `shoppingForm`.

The `domina.events` namespace offers a bunch of functions to manage
DOM events. One of them is the `listen!`, which allows us to attach a
handling function (e.g. `calculate`) to a DOM event type (e.g.
`click`, `mouseover`, `mouseout`, etc).

### Launch IFDE

As usual we like to do our work in a live enviroment. So, launch the
IFDE as usual:

```bash
boot dev
...
Compiling ClojureScript...
• main.js
WARNING: domina is a single segment namespace at line 1 /Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/2sf/r3n3mb/main.out/domina.cljs
Elapsed time: 22.270 sec
```

Then, from a new terminal launch the bREPL from the project home page:

```clj
boot repl -c
...
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:49835 >>
<< waiting for client to connect ... Connection is ws://localhost:49835
Writing boot_cljs_repl.cljs...
```

and finally visit the `http://localhost:3000/shopping.html` URL. As
usual you'll receive the connected notification at the bREPL terminal.

```clj
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

Now, before starting bREPLing with our ridicolous webapp, let's add
the `domina.events` namespace to the requirement section of the
`modern-cljs.shopping` namespace declaration in the
`src/cljs/modern-cljs` directory.

```clj
(ns modern-cljs.shopping
  (:require [domina :refer [by-id value set-value!]]
            [domina.events :refer [listen!]]))
```

As soon as you save the file it gets reloaded by the IFDE.

## bREPLing 

As you remember to shorten the typing at the bREPL for familiarizing
ourselves with the functionalities of a new lib, you first have to
require the namespaces you're interested in.

```clj
cljs.user> (require '[modern-cljs.shopping :as shop] :reload
                    '[domina :as dom] :reload
                    '[domina.events :as evt] :reload)
nil
```

Note that this time we required the two `domina` namespace we're
interested in by aliasing their symbols (i.e. `shop`, `dom` and
`evt`).

Even if by aliasing a namespace you are forced to prefix any referece
to its public symbols with the choosen alias, it makes it very clear
from which namespace the used symbols come from. Obviously it protect
yourself from name clashes as well in the hosting namespace.

Let's now see the `evt/listen!` function at work.

```clj
cljs.user> (doc evt/listen!)
-------------------------
domina.events/listen!
([type listener] [content type listener])
  Add an event listener to each node in a DomContent. Listens for events during the bubble phase. Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element.
nil
```

`listen!` listens for event during the bubble phase of events. Let's
see if `domina` offers a corresponding function listening for events during
the capturing phase as well.

> NOTE 2: if you're interested in the differences between the bubbling
> and the captugin phase od DOM events, look at
> [this document by W3C](http://www.w3.org/TR/DOM-Level-3-Events/#event-flow).

```clj
cljs.user> (apropos "capture")
(domina.events/capture! domina.events/capture-once!)
cljs.user> (doc evt/capture!)
-------------------------
domina.events/capture!
([type listener] [content type listener])
  Add an event listener to each node in a DomContent. Listens for events during the capture phase.  Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element.
nil
```

Here we first used the `apropos` macro by passing to it a string (it
could have been a regular-expression) to search for all the public
definitions in all currently-loaded namespaces matching the passed
string or a regular-expression pattern.

`apropos` returned a sequence of two symbols from the `domina.events`
namespace: `capture!` and `capture-once!`. We then asked for the
`capture!` documentation.

Both `listen!` and `capture!` are multi-arity functions. You can call
them with 2 or with 3 arguments. We're interested in the 3-arity
versions. Note the bang `!` char at the end. It informs you that those
functions mutate the element you're passing to.

Remember that we now want to attach the `init` function from the
`shopping.cljs` namespace to the `click` event generated by the user
when she clicks the `calc` button of the `shoppingForm`.

```clj
cljs.user> (evt/listen! (dom/by-id "calc") :click shop/calculate)
(#object[Object [object Object]])
```

> NOTE 3: `domina` keywordize any event name. This is the reason why we
> can use the `:click` keyword to identify the `click` event.

Go to the browser and test the shopping form to verify that it still
working.

Verify again that the shopping form is still working.

### Edit shopping.cljs 

Let's update the `init` definition in the `shopping.cljs` file
according to the above bREPL experiment. 

```clj
(ns modern-cljs.shopping
  (:require [domina :refer [by-id value set-value!]]
            [domina.events :refer [listen!]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))))

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (listen! (by-id "calc") :click calculate)))
```

> NOTE 4: Note that we deleted the returned `false` value from
> `calculate` because, when using `button` input type instead of
> `submit` input type, we do not need to return the control to the
> form itself.

> NOTE 5: the `init` function has been exported to protect its name
> from being changed by Google Closure Compiler aggressive compilation
> eventually used by the `advanced` optimization.

You can now compile and run the project as usual:

```bash
lein ring server # from modern-cljs home
lein cljsbuild auto dev # from modern-cljs home in a new terminal
```

If you want to interact with the bREPL, just execute the usual command to
run the bREPL.

```bash
lein trampoline cljsbuild repl-listen # from modern-cljs home in a new terminal
```

Verify that everything is still working as expected by visiting the
[shopping.html](http://localhost:3000/shopping.htm) page.

### Bubbling and capture models

The Domina library supports both *bubbling* and *capture* event models. In
the above shopping calculator example we used the domina `listen!`
function for handling the mouse `click` event on the *Calculate*
button. `listen!` is a member of a group of functions defined by domina
to use the *bubbling* method of handling DOM events. If you want to
experience the *capture* method you have to simply substitute the `listen!`
call with the corresponding `capture!` call and you're done.

```clojure
;;; the rest as before

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (ev/capture! (dom/by-id "calc") :click calculate)))
```

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "introducing domina events"
```

# Next Step - [Tutorial 9: DOM manipulation][9]

In the next tutorial we're going to face the need to programmatically
manipulate DOM elements as a result of the occurrance of some DOM
events (e.g., `mouseover`, `mouseout`, etc.)

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
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
[10]: https://help.github.com/articles/set-up-git
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md
