# Tutorial 8 - DOM manipulation

In the [previous tutorial][1] we introduced the `domina.events` namespace to
make our event management a little bit more Clojure-ish than just
using CLJS/JS interop. In this tutorial we're going to face the need
to programmatically manipulate DOM elements as a result of the
occurrence of some DOM events (e.g., `mouseover`, `mouseout`, etc.)

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][12] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-07
git checkout -b tutorial-08-step-1
```

# Introduction

As we already saw, the [domina library][2] has a lot to offer for
managing the selection of DOM elements and for handling almost any DOM
event. Let's continue by using it to verify how it could help us in
managing the manipulation of DOM elements, one of the most important
feature of any good JS library and/or framework.

To reach this goal, we're going to use the shopping calculator example
again, adding both a `mouseover` and a `mouseout` event handler to its
`Calculate` button.

The `mouseover` handler reacts by adding "Click to calculate" to the
form itself. The `mouseout` handler reacts by deleting that text.
Yes, I know, the requirement is very simple but, as you will see,
pretty representative of a kind of problem you're going to face again
and again in your CLJS programming.

# Start IFDE and bREPL

As usual we like to work in the IFDE/bREPL live environment.

Start IFDE

```bash
cd /path/to/modern-cljs
boot dev
...
Compiling ClojureScript...
• main.js
WARNING: domina is a single segment namespace at line 1 /Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/2vm/hys6xj/main.out/domina.cljs
Elapsed time: 23.931 sec
```

Then start the bREPL

```clj
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> (start-repl)
...
```

and finally visit the
[shopping URL](http://localhost:3000/shopping.html) to activate the
bREPL.

## bREPLing with DOM manipulation

For bREPling with the Shopping Form we first need to require the
needed namespaces.

```clj
cljs.user> (require '[modern-cljs.shopping :as shop] :reload
                    '[domina :as dom] :reload
                    '[domina.events :as evt] :reload)
nil
```

## DOM manipulation

Take a look at the docstring attached to the `append!` function
(note the `!` bang)

```clj
cljs.user> (doc dom/append!)
-------------------------
domina/append!
([parent-content child-content])
  Given a parent and child contents, appends each of the children to all
  of the parents. If there is more than one node in the parent content,
  clones the children for the additional parents. Returns the parent content.
nil
```

Here is a simple example of `append!` usage from [domina readme][3].

```clj
;;; from domina readme.md
(append! (xpath "//body") "<div>Hello world!</div>")
```

It appends a `<div>` node to the end of the `<body>` node. It uses
`xpath` to select a single parent (i.e., `<body>`) and a `string` to
represent a single `<div>` child to be added to the parent.

I don't know about you, but I don't feel comfortable with `xpath`, and
I only use it when no equivalent CSS selector is available (e.g.,
ancestor selection) or when the selection is too complex to be managed
and/or maintained.

Anyway, domina offers you three options for nodes selection:

* `xpath` from `domina.xpath` namespace
* `sel` from `domina.css` namespace
* `by-id` and `by-class` from `domina` namespace

Thankfully `append!` accepts, as a first argument, any domina
expression that returns one or more `content` (i.e., one or more DOM
nodes). This means that, for such a simple case, we can safely use
the `by-id` selector to select the parent to be passed to `append!`.

Let's see how `append!` works with the bREPL

```clj
cljs.user> (dom/append! (dom/by-id "shoppingForm")
                        "<div class='help'>Click to calculate</div>")
#object[HTMLFormElement [object HTMLFormElement]]
```

You should now see the `Click to calculate` text in the Shopping Form.

Note that we used the `help` class attribute to be able to remove any
`help` element when later we'll implement the listener for managing
the `mouseover` event for the `calc` button.

## Mouseover event

We can now start to add a `mouseover` handler to the `Calculate`
button by using the same `listen!` function we already used for
triggering the `calculate` listener.

Go back to your bREPL and enter the following expression:

```clj
cljs.user> (evt/listen! (dom/by-id "calc")
                        :mouseover
                        (fn []
                          (dom/append!
                           (dom/by-id "shoppingForm")
                           "<div class='help'>Click to calculate</div>")))
(#object[Object [object Object]])
```

Here we attached to the `mouseover` event an anonymous function
defined inline which does the same thing we tested above.

Go to the form. You'll see a new `Click to calculate` help message be
added to the form each time you enter the button area like in the
following figure.

![Shopping calculator][5]

So far, so good. We now need to remove the help message each time
mouse pointer exits the button area.

## Mouseout event

Thankfully, the `domina.events` namespace supports the `mouseout`
event as well.

The `domina` core namespace even offers the `destroy!` function to
permanentely delete a DOM element and all its children all together.

Go back to the bREPL and ask for the `detroy!` docstring.

```clj
cljs.user> (doc dom/destroy!)
-------------------------
domina/destroy!
([content])
  Removes all the nodes in a content from the DOM. Returns nil.
nil
```

We also need a way to select the `div` tag. As you remember we set the
`class` CSS attribute of the added `div` to `help` value. Again, the
`domina` core namespace exposes the `by-class` function to select all
the elements which are member of a class.

```clj
cljs.user> (doc dom/by-class)
-------------------------
domina/by-class
([class-name])
  Returns content containing nodes which have the specified CSS class.
nil
cljs.user> (dom/by-class "help")
(#object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]])
```

If you now call the `destroy!` function on the sequence returned from
the `by-class` function you'll see all the `Click to calculate`
message to be deleted.

```clj
cljs.user> (dom/destroy! (dom/by-class "help"))
nil
```

The last experiment we want to do at the bREPL before starting coding
in the `shoppinc.cljs` file is to attach the listener to the
`mouseout` event for the `calc` button.

```clj
cljs.user> (evt/listen! (dom/by-id "calc")
                        :mouseout (fn []
                                    (dom/destroy! (dom/by-class "help"))))
(#object[Object [object Object]])
```

Go back to the Shopping Form. As soon as you enter the button area
you'll see the message. As soon as you exit the button area the
message will disapear.

## Edit shopping.cljs

Having familiarized a little bit more with the `domina` lib, we are
now ready to code into the `shopping.cljs` file what we learnt.

Here is the update content.

```clj
(ns modern-cljs.shopping
  (:require [domina :refer [append! 
                            by-class
                            by-id 
                            destroy! 
                            set-value! 
                            value]]
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
  (when (and js/document
           (.-getElementById js/document))
    (listen! (by-id "calc") 
             :click 
             calculate)
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        "<div class='help'>Click to calculate</div>")))
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))
```

Few things to be noted about the above code:

1. depending on your taste, there are more way to *use* or *require* a
   namespace inside a new namespace declaration. Moreover, what you
   like while writing code (minimizing typing) could be different from
   what you like when reading code (maximazing readability). When your
   namespace declaration require a not so small number of other
   namespaces, and each namespace has a lot of public symbols, I
   always prefer to alias the required namespaces, because in a short
   time my role as a code writer changes very quickly into a reader.
2. the origianl `false` boolean value returned by the `calculate`
   function has been removed, because the `shoppingForm` does not have
   an `action` property any more;
3. the origianl `if` form has been substituted by the `when` forms,
   because we now need to do more things whe the predicate is `true`
   and there is no an else path.
4. you could substitute the anonymous functions definitions with a the
   corresponding `#()` short form.

# I hate HTML

I have to admit to being very bad at both HTML and CSS coding and I
always prefer to have a professional designer available to do that
job.

If you're like me, you would not want to code any HTML/CSS fragment as
a string like we did when we manipulated the DOM to add a `div` to the
`shoppingForm` form. Debugging such a code could quickly become a
PITA.

That's why I searched around to see if someone else, having my same
pain, created a lib to represents those elments as CLJS data structure
instead of strings of HTML.

## hiccups

The first CLJS library I found to relieve my pain was
[hiccups][6]. It's just an incomplete port of [hiccup][7] on CLJS. It
uses vectors to represent tags and maps to represent a tag's
attrbutes.

## Stop IFDE and add hiccups

We can't use the IFDE to require a new lib that is not already listed
in the `:dependencies` section of the `build.boot` file. So, to go on
with the next step you need to stop any `boot` related process and
first add `hiccups` lib into the `build.boot` before restarting the
IFDE as usual.

```clj
(set-env!
 ...
 :dependencies '[
                  ...
                  [hiccups "0.3.0"]
                 ])
```

## Restart IFDE

Restart IFDE as usual

```bash
cd /path/to/modern-cljs
boot dev
...
Compiling ClojureScript...
• main.js
WARNING: domina is a single segment namespace at line 1 /Users/mimmo/.boot/cache/tmp/Users/mimmo/tmp/modern-cljs/2vm/hys6xj/main.out/domina.cljs
Elapsed time: 23.931 sec
```

##  Restart bREPL

Restart bREPL as usual

```clj
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> (start-repl)
...
```

and finally visit the
[shopping URL](http://localhost:3000/shopping.html) to activate the
bREPL.

## Add hiccups namespace

Open the `src/cljs/modern_cljs/shopping.cljs` file to update its
requirements.

```clj
(ns modern-cljs.shopping
  (:require [domina :refer [append! 
                            by-class
                            by-id 
                            destroy! 
                            set-value! 
                            value]]
            [domina.events :refer [listen!]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))
```

> NOTE 1: As note in the [previous tutorial][], due to a bug of the
> `boot-cljs-repl` task, we need to first require a namespace from a
> namespace declaration to be able to require it in the bREPL as
> well. The `hiccups`runtime` namesapce has to be required, even if
> we're not going to use its symbols. This is why it has not been
> aliased.

NOTE 2: the `hiccups.core` contains macros (e.g. `html`), which are
written in CLJ. Namespaces containing macros are referenced via the
`:require-macros` keyword in namespace declaration and via
`require-macros` in the bREPL.

As soon as you save the file, IFDE recompile and reload it.

## bREPLing with hiccups

Before start bREPLing with `hiccups`, we need to require its namespace
in the bREPL as well.

```clj
cljs.user> (require '[hiccups.runtime])
nil
cljs.user> (require-macros '[hiccups.core :refer [html]])
nil
```

As above, we did not alias any namespace and we interned the `html`
symbol only from `hiccups.core` namespace, because it's the only one
we're going to exercise.

Here are some basic documented examples of hiccups usage we're going
to experiment with at the bREPL.


```clj
cljs.user> (html [:span {:class "foo"} "bar"])
"<span class=\"foo\">bar</span>"
cljs.user> (html [:script])
"<script></script>"
cljs.user> (html [:p])
"<p />"
```

`hiccups` also provides a CSS-like shortcut for denoting `id` and
`class` attributes

```clj
cljs.user> (html [:div#foo.bar.baz "bang"])
"<div class=\"bar baz\" id=\"foo\">bang</div>"
```

which brings us to our problem of representing the string `"<div
class='help'>Click to calculate</div>"` as CLJ data structures to be
passed to `append!` function

```clj
cljs.user> (html [:div.help "Click to calculate"])
"<div class=\"help\">Click to calculate</div>"
```

We are now ready to substitute the horrific HTML string to be passed
in the `mouseover` anonymous listener in the `shopping.cljs` source
file.

```clj
(defn ^:export init []
  (when (and js/document
           (.-getElementById js/document))
    (listen! (by-id "calc") 
             :click 
             calculate)
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div.help "Click to calculate"]))))
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))
```

We're now happy with what we achieved by using `domina` and `hiccups`
to make our shopping calculator sample as Clojure-ish as possible. The
only thing that still hurts me is the `.-getElementById` interop call
in the `init` function which can be very easily removed by just using
`aget` like so:

```clj
(defn ^:export init []
  (when (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "calc") 
             :click 
             calculate)
    (listen! (by-id "calc") 
             :mouseover 
             (fn []
               (append! (by-id "shoppingForm")
                        (html [:div.help "Click to calculate"]))))  ;; hiccups
    (listen! (by-id "calc") 
             :mouseout 
             (fn []
               (destroy! (by-class "help"))))))
```

As homework, I suggest you to modify `login.cljs` according to
the approach used for `shopping.cljs` in this and in the
[previous tutorial][1].

If you created a new git branch as suggested in the preamble of this
tutorial, stop any `boot` releated process and commit your changes.

```bash
git commit -am "DOM manipulation"
```

# Next step - [Tutorial 9: Introducing Ajax][11]

In the next tutorial we're going to extend our comprehension of CLJS by
introducing Ajax to let the CLJS client-side code communicate with
the CLJ server-side code.

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[2]: https://github.com/levand/domina#examples
[3]: https://github.com/magomimmo/domina/blob/master/readme.md#examples
[4]: http://localhost:3000/shopping-dbg.html
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/mouseover.png
[6]: https://github.com/teropa/hiccups
[7]: https://github.com/weavejester/hiccup
[8]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[9]: http://localhost:3000/shopping-pre.html
[10]: http://localhost:3000/shopping.html
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[12]: https://help.github.com/articles/set-up-git
