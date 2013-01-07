# Tutorial 10 - DOM manipulation

In the [last tutorial][1] we introduced `domina.events` namespace to
make our events management a little bit more clojure-ish than just
using CLJS/JS interop. In this tutorial we'are going to face the need
to programmatically manipulate DOM elements as a result of the
occurrance of some DOM event (e.g. `mouseover`, `mouseout`, etc.).

# Introduction

As we already saw, [domina library][2] has a lot to offer for managing
the selection of DOM elements and for handling almost any DOM
event. Let's go on by using it to verify how it could help us in
managing the manipulation of DOM elements which is one of the most
important features of any good JS library and/or framework.

To follow that goal we're going to use again our old shopping
calculator example by adding to its `Calculate` button both a
`mouseover` and a `mouseout` event handlers.  The `mouseover` handler
reacts by adding a simple paragraph text, saying "Click to calculate",
to the form itself. The `mouseout` handler, instead, reacts by
deleting that paragraph.  The requirement is very simple but, as you
will see, pretty representative of a kind of problem you're going to
face again and again in yuor CLJS programming.

# Mouseover event

Let's start by adding a `mouseover` handler to the `Calculate`
button. The first step is to write a function, named `add-help`, wich
append a text paragraph to the shopping calculator DOM. To do that
we're are going to experience the domana library again by using its `append!` function from `domina` namespace. The documentation attached to `append!` functionssays that:

> Given a parent and child contents, appends each of the children to all
> of the parents. If there is more than one node in the parent content,
> clones the children for the additional parents. Returns the parent
> content.

And here is a simple example of `append!` usage from its [readme][3].

```clojure
;;; from domina readme.md
(append! (xpath "//body") "<div>Hello world!</div>")

;;; from domina unit tests
(append! (sel "body")
         "<div><p><span>some text</span></p>
		       <p><span>more text</span></p>
		  </div>")

(append! (sel "body")
g         "<div><p><span>some text</span></p>
		       <p><span>more text</span></p>
		  </div>")
```

# Next step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[2]: https://github.com/levand/domina#examples
[3]: 
