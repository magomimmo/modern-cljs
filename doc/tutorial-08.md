# Tutorial 8 - Handling events using Domina

Starting from [Tutorial 5][1], we introduced [domina library][2] to
approach CLJS programming in a more closjre-ish way if compared with
just using CLJS/JS interop features.

We touched `domina` surface by using `by-id` to select
individual elements from DOM, `value` and `set-value!` to get/set the
value of a form field.

It's now time to see what domina library has to offer for substitutuing
CLJS/JS interop features in managing events.

## Introduction

Let's go back to the [shopping calculator form][3] we introduce in
Tutorial 5.

First of all, by been cloned from the orginal HTML code of
[Modern JavaScript: Develop and Desing][4], the shopping form used a
`submit` type of button instead of a `button` type. As the shopping
calculator data need not to be sent to a server-side script to be
elaborated, we think it's more appropriate to use a `button` type and
remove both `action` and `method` attributes from the corresponding
`form` tag.

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

> NOTE 1: In [Tutorial 7][5] we set `:cljsbuild` configuration options to
> generate three different builds: `:dev`, `:pre-prod` and `:prod` which
> emitted three differents JS (i.e. `modern_dbg.js`, `modern_pre.js` and
> `modern.js`). Then we had to replicate three html
> (i.e. `shopping-dbg.html`, `shopping-pre.html` and `shopping.html`) to
> include the appropriate JS file emitted by the three different builds.
>
> You should replicate the above modification in each shopping html file.

Here is the updated shopping calculator form rendered by the browser.

[!Shopping calculator][6]

## Domina events

As you perhaps remember, to manage the shopping calculator we defined
the `calculate` function and the `init` function to attach it to the
`submit` button of the `shoppingForm`.

domina.events namespace offers a `listen!` function to attach an
handling function (e.g. `calculate`) to a DOM event type (e.g. `click`,
`mouseover`, `mouseout`, etc).

Let's update `shopping.cljs` by requiring `domina.events` namespace and
by substituting `.-onsubmit` JS interop with `listen!` function as
follows:

```clojure-mode
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
    (let [theButton (dom/by-id "calc")]
      (ev/listen! theButton :click calculate))))
```

> NOTE 2: We now `:require` `domina` instead of just `:use` it as in previous
> tutorials. Note that we also deleted the returned `false` value from `calculate`
> because, when using `button` input type instead of `submit`
> input type, it's not needed any more to return the control to the form
> itself.

> NOTE 3: A usual, the `init` function has been exported to protect its
> name from been changed by Google Closure Compiler aggressive compilation
> used in `:dev` build.

You can now run the project as usual:

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
[`shopping-dbg.html][7] page.

Domina library supports both `bubbling` and `capture` event models. The
`listen!` function supports `bubbling` method. If you want to use the
`capture` method, you just need to substitute `capture!` function to
`listen!` one in the `init` function definition and you're done.

## Reset button

To improve our shopping calculator we're now going to add it a second
button which allows the user to reset the input values to their
defaults.

You first need to add a new button to `shopping-dbg.html` file (and to
`shopping-pre.html` and `shopping.html` too), like so:

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
      ...
      ...
      <div>
        <input type="button"
               value="Calculate"
               id="calc">               id="calc">
      </div>

      <div>
        <input type="button"
               value="Reset"
               id="reset">
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

Here is the updated shopping calculator rendered by the browser.

[!Shopping calculator][7]

We than need to define a function which reset any input field to its
default.

```clojure

```

## A mouseover/mouseout event

Together with the calculate and reset feature associated with the
click, we want to add a mouseover/mousout event which print on the
form the behavior of the buttons *Calcuate* and *Reset*, that is

![Shopping events][7]

To do this, we define the functions `addcalclauncher` and
`addresetlauncher`, that handle the events associated with the
calcuate and reset events of *Calcuate* and *Reset* buttons. The
following code handles within a unique function the three
behaviors concerning the *Calcuate* button.

```clj
(defn addcalclauncher []
  (doall
   [(evts/listen! (dom/by-id "calculate") :mouseover (fn [evt] (appendinfo)))
    (evts/listen! (dom/by-id "calculate") :mouseout (fn [evt] (removeinfo)))
    (evts/listen! (dom/by-id "calculate") :click  (fn [evt] (calculate)))]))

(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
     (addcalclauncher)))
```

provided that we have included the domina library running

```clj
(ns event-ex-one.reset
        (:require [domina :as dom]
                  [domina.events :as evts]))
```

Here `calculate` is the "calculator" routine specified in the previous
tutorials. The functions `appendinfo`, `removeinfo` are responsible to
the information printing on the form. To manipulate the underlining
HTML we proceed as follows

```cljs
(defn appendinfo []
  (dom/append! (xdom/xpath "//body/form") (hsc/html [:div {:id "txtcalc"} "Click to calculate"])))

(defn removeinfo []
  (dom/destroy! (dom/by-id "txtcalc")))
```

The `dom/append!` and `dom/destroy!` functions respectively add and
delete a specified DOM element. A review of the specifications which
can be passed to these function can be found in the
[domina readme][1]. Since `dom/append!` receives as second argument a
string which contains the HTML code to be appended, we want to have a
more "clojurish" approach to generate HTML code. To this aim, we use
[hiccups library][2]. It provides the function `hsc/html` which
return a string containing an HTML source code defined by a standard
"hiccups" syntax (see [hiccups readme][2] for a complete review).

Similary, we hook to the *Reset* buttons the similar events.

```clj
(defn addresetlauncher []
  (doall
   [(evts/listen! (dom/by-id "reset") :mouseover (fn [evt] (appendinfor)))
    (evts/listen! (dom/by-id "reset") :mouseout (fn [evt] (removeinfor)))
    (evts/listen! (dom/by-id "reset") :click  (fn [evt] (resetform)))]))

;; the same as the previous sample
(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
     (addresetlauncher)))
```

Here `reset` is the function that reset the form.

```clj
(defn resetform []
  (dom/set-value! (dom/by-id "total") "0.00")
  (dom/set-value! (dom/by-id "price") "0.00")
  (dom/set-value! (dom/by-id "tax") "0.0")
  (dom/set-value! (dom/by-id "discount") "0.00")
  (dom/set-value! (dom/by-id "quantity") "1")
  false)
```

As shown in [tutorial 6][8], to make the produced JavaScript actually
runnable, we need to add in the HTML `shopping.html` the following
lines

```HTML
<script>modern_cljs.shopping.init();</script>
<script>modern_cljs.reset.init();</script>
```

> Since the events are hooked both to the DOM element and its
> response, there is no behavior difference between the bubble-phase
> and the capture-phase, anyway domina allows the user follow both the
> approaches.

## Another approach for building the page

We have seen above how [domina][1] and [hiccups][2] can be expolited
for HTML pages manipulations. Anyway a different approach is possible,
that is we can build an html page entirely in the ClojureScript code
and declaring only a minimal skeleton in our HTML. To do so we employ
the [c2 library][9].

The HTML page is now the following.

```HTML
<!DOCTYPE html>
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
  <form action="" method="post" id="shoppingForm" novalidate></form>
  <script src="js/modern_dbg.js"></script>
</body>
</html>
```

and the shopping form can be initialized using the `bind!` macro of
`c2.util`, which must be imported by the usual `use-macro`
declaration.

```clj
(bind! "#shoppingForm"
       [:form
        [:legend "Shopping Calculator"]
        [:fieldset
         [:div [:label {:for "quantity"} "Quantity"
                [:input#quantity {:type "number"
                                  :name "quantity"
                                  :value "1"
                                  :min "1"
                                  :required true}]]]
         [:div [:label {:for "price"} "Price Per Unit"
                [:input#price {:type "text"
                               :name "price"
                               :value "1.00"
                               :required true}]]]
         [:div [:label {:for "tax"} "Tax Rate (%)"
                [:input#tax {:type "text"
                             :name "tax"
                             :value "0.0"
                             :requried true}]]]
         [:div [:label {:for "discount"} "Discount"
                [:input#discount {:type "text"
                                  :name "discount"
                                  :value "0.0"
                                  :required true}]]]
         [:div [:label {:for "total"} "Total"
                [:input#total {:type "text"
                               :name "total"
                               :value "0.00"
                               :required true}]]]
         [:div [:input#calculateButton {:type "button"
                                         :value "Calculate"}]]
         [:div [:input#resetButton {:type "button"
                               :value "Reset"}]]]])

```

> Observe that the basic difference between this approach goes beyond
> the language we use to build a HTML page. Following this approach
> the actions associated with the DOM elements don't require to be
> initialized (see the *init* functions in the previous tutorials) and
> so, no CLJS functions must be exported, as we shall see below.

We see now how the events discussed in the previous section can be
handled with *c2*.

## The mouseover/mouseout event with c2

We recall that we want to attach to the *Calculate* button a mouseover
event which prints on the form some information about the behavior of
the button, which must disappear when the mouse moves out the
button. Similary for the *Reset* button.

Here the code for the calculation

```clj
        (c2event/on-raw "#calculateButton" :click calculate)
        (c2event/on-raw "#calculateButton" :mouseover (fn [] (add-info "#shoppingForm" "calculate")))
        (c2event/on-raw "#calculateButton" :mouseout (fn [] (remove-info "#calculate")))
```

and the code for the reset action

```clj
        (c2event/on-raw "#resetButton" :click reset-form)
        (c2event/on-raw "#resetButton" :mouseover (fn [] (add-info "#shoppingForm" "reset")))
        (c2event/on-raw "#resetButton" :mouseout (fn [] (remove-info "#reset")))
```

where `calculate`, `reset`, `add-info` and `remove-info` are now defined as follows

```clj
        (defn calculate []
                (let [quantity (c2dom/val "#quantity")
                        price (c2dom/val "#price")
                        tax (c2dom/val "#tax")
                        discount (dom/val "#discount")]
                (c2dom/val "#total" (-> (* quantity price)
                                        (* (+ 1 (/ tax 100)))
                                                        (- discount)
                                                        (.toFixed 2)))))

        (defn reset-form []
                (let [fields ["#quantity" "#price" "#tax" "#discount" "#total"]
                          init ["1" "1.00" "0.0" "0.0" "0.00"]]
                  (dorun (map c2dom/val fields init))))

        (defn add-info [el name]
                (c2dom/append! el [:div {:id name} (str "Click to " name)]))

        (defn remove-info [el]
                (c2dom/remove! el))
```

which are slightly different since they use the *c2 library* functions
(actually those are not the only differences, we wanted to show other
possible implementations).

> As mentioned above, no ^:export tags must be provided.


[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[2]: https://github.com/levand/domina
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md#shopping-calculator-sample

[4]: https://github.com/magomimmo/domina/blob/master/src/cljs/domina/events.cljs

[2]: https://github.com/teropa/hiccups
[4]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[5]: http://www.larryullman.com/
[6]: https://raw.github.com/magomimmo/modern-cljs/tut-11/doc/images/form-idle.png
[7]: https://raw.github.com/magomimmo/modern-cljs/tut-11/doc/images/form-events.png
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[9]: https://github.com/lynaghk/c2.git
[10]: https://github.com/lynaghk/c2/blob/master/README.markdown
