# Tutorial 7 - Introducing Domina Events

Starting from [Tutorial 5][1] we introduced the [domina library][2] to
approach CLJS programming in a more Clojure-ish way by substituting
CLJS/JS interop features. In this tutorial we're going to introduce
`domina` events.

## Preamble

If you want to start working from the end of the [previous tutorial][5],
assuming you've [git][10] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-06
```

## Introduction

We superficially touched `domina` by using `by-id` to select
individual elements from the DOM, and `value` and `set-value!` to
get/set the value of a form field.

It's now time to see what the domina library has to offer for substituting
CLJS/JS interop features in managing events.

## Listen to events

Let's go back to the [shopping calculator form][3] we introduced in
Tutorial 5.

First of all, by having been cloned from the original HTML code of
[Modern JavaScript: Develop and Design][4], the shopping form used a
`submit` type of button. At the moment, the shopping calculator data
are not sent to a server-side script to be validated. Until we
introduce a server-side script we are going to use a `button` type
with the `calc` id. For the same reason we are also removing both the
`action` and `method` attributes from the corresponding `form` tag.

> NOTE 1: By replacing `submit` with `button`, we're breaking
> the progressive enhancement strategy. We will fix this issue in a subsequent
> tutorial. Here we're focusing on the [domina events][2] machinery. 

## Launch the IFDE

As in previous tutorials, we like to progress step by step by using a
live development environment. So, let's start by launching the IFDE.

```bash
boot dev
...
Compiling ClojureScript...
• main.js
Elapsed time: 19.122 sec
```

## Launch the bREPL

Then launch the bREPL from a new terminal

```clj
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> (start-repl)
```

Then visit the [shopping](http://localhost:3000/shopping.html) URL to
activate the bREPL.

Edit the `html/shopping.html` file to use a button input, as discussed above.

```html
<!doctype html>
...
  <form id="shoppingForm" novalidate>
    ...
      <br><br>
      <div>
        <input type="button"
               value="Calculate"
               id="calc">
      </div>
...
```

Here is the updated shopping calculator form as rendered by the
browser.

![Shopping calculator][6]

Note that if you click the `Calculate` button nothing happens,
because there is no listener attached.

### bREPLing with domina

We want to invoke the `calculate` function whenever the `click` event
is triggered by the `calc` button.

The `domina.events` namespace offers a bunch of functions to manage
DOM events. One of them is `listen!`, which allows us to attach a
handling function (e.g. `calculate`) to a DOM event type (e.g.
`click`, `mouseover`, `mouseout`, etc).

Let's start playing with `domina` events by first using a namespace declaration to require the new
domina namespace we're interested in using: `modern-cljs.shopping`.

Open the `src/cljs/modern_cljs/shopping.cljs` file to update its
requirements.

```cljs
(ns modern-cljs.shopping
  (:require [domina.core :refer [by-id value set-value!]]
            [domina.events :refer [listen!]]))
```

> NOTE 2: Due to a bug of the `boot-cljs-repl` task, we need to first
> require a namespace from a namespace declaration to be able to
> require it in the bREPL as well.

Now repeat the above requirements in the bREPL. Remember that at the
bREPL prompt you need to quote (i.e. `'`) the namespace symbols in the
requirment form.

```clj
cljs.user> (require '[modern-cljs.shopping :as shop] :reload
                    '[domina.core :as dom] :reload
                    '[domina.events :as evt] :reload)
nil
```

This time we required the namespaces we're interested in by aliasing
their symbols (i.e. `shop`, `dom` and `evt`), just to make clear from
what namespace the symbols come from.

Let's now see the `evt/listen!` function at work by first reading its
internal documentation (i.e. `docstring`).

```clj
cljs.user> (doc evt/listen!)
-------------------------
domina.events/listen!
([type listener] [content type listener])
  Add an event listener to each node in a DomContent. Listens for events
  during the bubble phase. Returns a sequence of listener keys (one for
  each item in the content). If content is omitted, binds a listener to
  the document's root element.
nil
```

It says that `listen!` listens for event during the bubble phase of
events. Just for curiosity, let's see if `domina` offers a
corresponding function listening for events during the capturing phase
as well.

> NOTE 3: if you're interested in the differences between the bubbling
> and the capturing phase of DOM events, look at
> [this document by W3C](http://www.w3.org/TR/DOM-Level-3-Events/#event-flow).

```clj
cljs.user> (apropos "capture")
(domina.events/capture! domina.events/capture-once!)
cljs.user> (doc evt/capture!)
-------------------------
domina.events/capture!
([type listener] [content type listener])
  Add an event listener to each node in a DomContent. Listens for events
  during the capture phase.  Returns a sequence of listener keys (one for
  each item in the content). If content is omitted, binds a listener to
  the document's root element.
nil
```

Here we used the `apropos` macro to find all public method definitions
that match the string "capture" in all currently-loaded namespaces.

Our search returned two definitions from the `domina.events`
namespace: `capture!` and `capture-once!`. We then asked for the
`capture!` docstring.

Both `listen!` and `capture!` are multi-arity functions. You can call
them with 2 or with 3 arguments. We're interested in the 3-arity
versions because we want to add a listener (i.e. `calculate`) to the
`click` event of the `calc` button. Note the bang `!` char at the
end. It informs you that those functions mutate the argument/element
you're passing to.

```clj
cljs.user> (evt/listen! (dom/by-id "calc") :click shop/calculate)
(#object[Object [object Object]])
```

> NOTE 4: `domina` uses the "keywordized" version of event names. This is the reason we
> can use the `:click` keyword to identify the `click` event.

Go to the browser and test the shopping form to verify that it works as expected.

### Edit shopping.cljs 

We're now ready to update the `shopping.cljs` source code according to
the above bREPL experiment.

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [by-id value set-value!]]
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

> NOTE 5: Note that we deleted the returned `false` value from
> `calculate`. When using a `button` input type instead of a
> `submit` input type, we do not need to return the control to the
> form itself.

> NOTE 6: the `init` function has been exported to protect its name
> from being changed by the `advanced` optimization that we still have
> to introduce.

As usual, as soon as you save the file the IFDE takes care of its
recompilation and reloading.

Play with the Shopping Form to verify it works as expected. 

### Bubbling and capture models

As we saw, `domina` supports both *bubbling* and *capture* event
models. In the above shopping calculator example we used the domina
`listen!` function for handling the mouse `click` event on the
*Calculate* button. `listen!` is a member of a group of functions
defined by `domina` to use the *bubbling* method of handling DOM
events. If you want to experience the *capture* method you have to
simply substitute the `listen!` call with the corresponding `capture!`
call and you're done.

```clj
cljs.user> (evt/unlisten! (dom/by-id "calc") :click)
nil
cljs.user> (evt/get-listeners (dom/by-id "calc") :click)
()
```

Here we detached the `calculate` listener of the `click` event from
the `calc` button. Then we verified via the `get-listeners` function
that there are no more listeners for `calc` attached to that event.

If you click the `Calculate` button nothing happens.

Now call the `capture!` function to trigger the `calculate` function
during the *capture* phase of the click event. 

```clj
cljs.user> (evt/capture! (dom/by-id "calc") :click shop/calculate)
(#object[Object [object Object]])
```

The `Calculate` button starts working again.

You can now stop any `boot` related process and reset your git repository.

```bash
git reset --hard
```

# Next Step - [Tutorial 8: DOM manipulation][9]

In the [next tutorial][9] we're going to face the need to
programmatically manipulate DOM elements as a result of the occurrence
of some DOM events (e.g., `mouseover`, `mouseout`, etc.)

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-05.md
[2]: https://github.com/levand/domina
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-05.md#shopping-calculator-sample
[4]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-06.md
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-reviewed.png
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-05.md#modify-validate-form
[8]: http://localhost:3000/shopping-dbg.html
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-08.md
[10]: https://help.github.com/articles/set-up-git
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-17.md
