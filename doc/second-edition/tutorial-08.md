# Tutorial 8 - DOM manipulation

In the [previous tutorial][1] we introduced the `domina.events`
namespace to make our events management a little bit more clojure-ish
than just using CLJS/JS interop. In this tutorial we're going to face
the need to programmatically manipulate DOM elements as a result of
the occurrence of some DOM events (e.g., `mouseover`, `mouseout`,
etc.)

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][12] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-07
```

# Introduction

As we already saw, the [domina library][2] has a lot to offer for
managing the selection of DOM elements and for handling almost any DOM
event. Let's continue by using it to verify how it could help us in
managing the manipulation of DOM elements, one of the most important
feature of any good JS library and/or framework.

To reach this goal, we're going to use again the shopping calculator
example by adding both a `mouseover` and a `mouseout` event handler to
its `Calculate` button.

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

For bREPLing with the Shopping Form we first need to require the
needed namespaces.

```clj
cljs.user> (require '[modern-cljs.shopping :as shop] :reload
                    '[domina.core :as dom] :reload
                    '[domina.events :as evt] :reload)
nil
```

## DOM manipulation

Take a look at the docstring attached to the `append!` function (note
the `!` bang meaning this function mutates the passed arguments)

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
* `by-id` and `by-class` from `domina.core` namespace

Thankfully `append!` accepts, as a first argument, any domina
expression that returns one or more `content` (i.e., one or more DOM
nodes). This means that, for such a simple case, we can safely use
the `by-id` selector to select the parent to be passed to `append!`.

Let's see how `append!` works within the bREPL

```clj
cljs.user> (dom/append! (dom/by-id "shoppingForm")
                        "<div class='help'>Click to calculate</div>")
#object[HTMLFormElement [object HTMLFormElement]]
```

You should now see the `Click to calculate` text in the Shopping Form.

Note that we used the `help` class attribute to be able to remove any
`help` element when later we'll implement the listener for managing
the `mouseout` event for the `calc` button.

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

Here we attached to the `mouseover` event an anonymous function doing
the same thing we tested above.

Go to the form. You'll see a new `Click to calculate` help message be
added to the form each time you enter the button area like in the
following figure.

![Shopping calculator][5]

So far, so good. We now need to remove the help message each time
mouse pointer exits the button area.

## Mouse out event

Thankfully, the `domina.events` namespace supports the `mouseout`
event as well.

The `domina.core` namespace even offers the `destroy!` function to
permanently delete a DOM element and all its children all together.

Go back to the bREPL and ask for the `destroy!` docstring.

```clj
cljs.user> (doc dom/destroy!)
-------------------------
domina.core/destroy!
([content])
  Removes all the nodes in a content from the DOM. Returns nil.
nil
```

We also need a way to select the `div` tag. As you remember we set the
`class` CSS attribute of the added `div` to `help` value. Again, the
`domina.core` namespace exposes a `by-class` function to select all
the elements which are members of a class.

```clj
cljs.user> (doc dom/by-class)
-------------------------
domina.core/by-class
([class-name])
  Returns content containing nodes which have the specified CSS class.
nil
cljs.user> (dom/by-class "help")
(#object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]] #object[HTMLDivElement [object HTMLDivElement]])
```

If you call the `destroy!` function on the sequence returned from the
`by-class` function, you'll see all the `Click to calculate` message
to be deleted.

```clj
cljs.user> (dom/destroy! (dom/by-class "help"))
nil
```

The last experiment we want to do within the bREPL before we start coding
in the `shopping.cljs` file is to attach a listener to the `mouseout`
event for the `calc` button.

```clj
cljs.user> (evt/listen! (dom/by-id "calc")
                        :mouseout (fn []
                                    (dom/destroy! (dom/by-class "help"))))
(#object[Object [object Object]])
```

Go back to the Shopping Form. As soon as you enter the button area
you'll see the message. As soon as you exit the button area the
message will disappear.

## Edit shopping.cljs

Having familiarized a little bit more with the `domina` lib, we are
now ready to code into the `shopping.cljs` file what we learned within
the bREPL.

Here is the updated content.

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append! 
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

1. depending on your taste, there are more ways to *use* or *require*
   a namespace inside a new namespace declaration. Moreover, what you
   like while writing code (e.g., minimizing typing) could be
   different from what you like when reading code (e.g., maximizing
   readability). When your namespace declaration requires more than a
   small number of other namespaces, and each namespace has a lot of
   public symbols, I always prefer to alias the required namespaces,
   because in a short time my role as a code writer changes very
   quickly into a code reader and I don't want to get crazy trying to identify
   which symbol came from which namespace;
2. the original `false` boolean value returned by the `calculate`
   function has been removed, because the `shoppingForm` does not have
   an `action` property any more;
3. the original `if` form has been substituted by the `when` form,
   because we now need to do more things when the predicate returns
   `true` and there is no else path to be followed;

# I hate HTML

I have to admit of being very bad at both HTML and CSS coding and I
always prefer to have a professional designer available to do that
job.

If you're like me, you do not want to code any HTML/CSS fragment as a
string like we did when we manipulated the DOM to add a `div` to the
`shoppingForm` form. Debugging such a code could quickly become a
PITA.

That's why I searched around to see if someone else, having my same
pain, has created a lib to represent those elments as CLJS data structure
instead of strings of HTML.

## hiccups

The first CLJS library I found to relieve my pain was
[hiccups][6]. Even if it's an incomplete port of [hiccup][7] on CLJS,
it's solid and stable enough for the purposes of this tutorial.  It
uses vectors to represent HTML tags and maps to represent a tag's
attributes.

## Stop IFDE and add hiccups

Even if we could add a new dependency to IFDE while it's running, as
soon as we the IFDE exits that dependency is gone. So, to go on with the
next step, stop any `boot` related process and add the `hiccups` lib into
`build.boot` before starting the IFDE again:

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

## Add hiccups namespaces

Open the `src/cljs/modern_cljs/shopping.cljs` file to update the
requirements of the namespace declaration.

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append! 
                                 by-class
                                 by-id 
                                 destroy! 
                                 set-value! 
                                 value]]
            [domina.events :refer [listen!]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))
```

> NOTE 1: As noted in the [previous tutorial][1], due to a bug in the
> `boot-cljs-repl` task, we need to first require a namespace from a
> namespace declaration to be able to require it in the bREPL as well.

> NOTE 2: The `hiccups`runtime namespace has to be required, even if
> we're not going to use its symbols. For this reason we neither
> aliased it or `refer` any symbol.

> NOTE 3: the `hiccups.core` namespace contains macros (e.g. `html`), which are
> written in CLJ. Namespaces containing macros are referenced via the
> `:require-macros` keyword in the namespace declaration and via
> `require-macros` in the bREPL.

As soon as you save the file, the IFDE will recompile and reload it.

## bREPLing with hiccups

Before we start bREPLing with `hiccups`, we need to require its namespace
in the bREPL as well.

```clj
cljs.user> (require '[hiccups.runtime])
nil
cljs.user> (require-macros '[hiccups.core :refer [html]])
nil
```

As mentioned above, we refer only the `html` macro from the `hiccups.core` namespace,
since it's the only one we're going to exercise.

Here are some simple examples of using `hiccups` in the bREPL:

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
passed to `append!` function:

```clj
cljs.user> (html [:div.help "Click to calculate"])
"<div class=\"help\">Click to calculate</div>"
```

We are now ready to substitute the horrific HTML string to be passed
to the `mouseover` anonymous listener in the `shopping.cljs` source
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
to make our shopping calculator sample as clojure-ish as possible. The
only thing that still hurts me is the `.-getElementById` interop call
in the `init` function. It can be very easily removed by just using
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

You can now stop any `boot` related process and reset your git repository.

```bash
git reset --hard
```

# Next step - [Tutorial 9: Introducing Ajax][11]

In the next tutorial we're going to extend our comprehension of CLJS by
introducing Ajax to let the CLJS client-side code communicate with
the CLJ server-side code.

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-07.md
[2]: https://github.com/levand/domina#examples
[3]: https://github.com/magomimmo/domina/blob/master/readme.md#examples
[4]: http://localhost:3000/shopping-dbg.html
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/mouseover.png
[6]: https://github.com/teropa/hiccups
[7]: https://github.com/weavejester/hiccup
[8]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[9]: http://localhost:3000/shopping-pre.html
[10]: http://localhost:3000/shopping.html
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
[12]: https://help.github.com/articles/set-up-git
