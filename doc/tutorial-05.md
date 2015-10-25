# Tutorial 5 - Introducing Domina

In this tutorial we're going to introduce [Domina][1] to improve the
idiomaticity of the login form validation we presented in
the [previous tutorial][2].

## Preamble

If you want to start working from the end of the [previous tutorial][2],
assuming you've [git][13] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-04
git checkout -b tutorial-05-step-1
```

## Introduction

In the [previous tutorial][2] we started coding in CLJS, directly
translating from JS to CLJS by using the [JS interop][7] features of CLJS. Now
it's time to try something better.

> [Domina][1] is a jQuery inspired DOM manipulation library for
> ClojureScript. It provides a functional, idiomatic Clojure interface
> to the DOM manipulation facilities provided by the Google Closure
> library...  While Domina does not provide any innovations, it attempts to
> provide a basic functional interface to DOM manipulation that feels
> natural in ClojureScript.

When I first met [clojurescriptone][3] in searching for a CLJS guide,
and read about [Design and templating][4], I found myself very much in
agreement with the following reasoning:

> Many Clojure web applications use [Hiccup][5] for HTML templating. If
> the programmer is also the designer, then Hiccup is ideal. However,
> most developers are bad at design. We need to work with people who are
> good at design and who don't need to care about Clojure. ClojureScript
> One proposes one approach to templating which allows designers to work
> with HTML, CSS and images without having to set an eye on Hiccup data
> structures or those pesky parentheses.

Our old `login.html` friend is going to be our pure HTML/CSS template
and domina is going to be our CLJS library to interface the DOM of the
page in more idiomatic CLJ/CLJS.

Here is the content of `login.html` we already used in [tutorial 4][2].

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Login</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <!-- login.html -->
    <form action="login.php" method="post" id="loginForm" novalidate>
        <fieldset>
            <legend>Login</legend>

            <div>
              <label for="email">Email Address</label>
              <input type="email" name="email" id="email" required>
            </div>

            <div>
              <label for="password">Password</label>
              <input type="password" name="password" id="password" required>
            </div>

            <div>
              <label for="submit"></label>
              <input type="submit" value="Login &rarr;" id="submit">
            </div>

        </fieldset>
    </form>
    <script src="js/modern.js"></script>
</body>
</html>
```

## Add domina to project dependencies

As usual when using leiningen, to add a library to a CLJ/CLJS project,
you need to add it to the dependencies section of `project.clj`.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [...
                 [domina "1.0.3"]]
  ...)
```

> ATTENTION NOTE: `domina` was one of the first DOM library written in
> CLJS and it has not been updated to follow the evolution of CLJS
> compiler. You'll get few warnings during its compilation. Those
> warnings do not affect the behaviour of the lib in the contest of
> this tutorial. That said, even if you're not going to use `domina`
> in your project, this tutorial could be still useful to undertstand
> the way CLJS works.

## Domina selectors

[Domina][1] offers several selector functions: `xpath`, in the `domina.xpath`
namespace, and `sel`, in the `domina.css` namespace. But it also features the
`by-id`, `value` and `set-value!` functions defined in the `domina` core
namespace, which is the one we're going to use.

The nice thing about domina `(by-id id)`, inherited from the
underlying Google Closure Library (GCL) on which domina is
implemented, is that it takes care of verifying if the passed argument
is a string. As we anticipated, the `domina` core namespace offers
other useful functions we're going to use: `(value el)`, which returns
the value of the passed element, and `(set-value! el value)` which
sets its value.

> NOTE 2: When a function modifies an argument passed to it, by Clojure
> naming convention a bang "!" is added at the end of the function
> name.

> NOTE 3: To `:use` a namespace in CLJS, you must specify vars
> using the `(:use ... :only [...])` form. For further
> differences see [the ClojureScript Wiki][8]

## Modify validate-form

In this step we're going to modify the namespace declaration and
`validate-form` function definition, substituting `.getElementById` and
`.-value` JS interop with the corresponding domina `by-id` and `value`
functions.

Open `login.cljs` file from `src/cljs/modern_cljs` directory of the
project and modify both its `namespace` declaration and `validate-form`
function definition as follows:

```clojure
(ns modern-cljs.login
  (:use [domina :only [by-id value]]))

(defn validate-form []
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))
```

As you can see, using `domina` the code is now more fluid than before.
Leave the rest of the file as is. To check that everything still works
do the following:

```bash
cd /path/to/modern-cljs
lein ring server-headless
lein cljsbuild once # in a new terminal and after having cd in modern-cljs
lein trampoline cljsbuild repl-listen
```

> NOTE 4: Be sure to `cd` to the home directory of the project in each
> terminal you open.

Open <http://localhost:3000/login.html>, and when the CLJS repl
becomes responsive, having established a connection with the browser,
try the following at the REPL prompt:

```bash
cljs.user=> (require '[modern-cljs.login :as l])
cljs.user=> l/validate-form
#object[modern_cljs$login$validate_form "function modern_cljs$login$validate_form() {
  var email = domina.by_id.call(null, "email");
  var password = domina.by_id.call(null, "password");
  if (cljs.core.count.call(null, domina.value.call(null, email)) > 0 && cljs.core.count.call(null, domina.value.call(null, password)) > 0) {
    return true;
  } else {
    alert("Please, complete the form!");
    return false;
  }
}"]
cljs.user=>
```

The evaluation of the `l/validate-form` symbol returns the JS function
definition attached by the CLJS compiler to the symbol itself. If you
now try to call the function `(l/validate-form)`, you should see the
browser alert window asking you to complete the form; click the `ok`
button and you'll see that `(l/validate-form)` returns `false`.

```bash
cljs.user=> (l/validate-form)
false
cljs.user=>
```

Fill both the `Email Address` and `Password` fields of the login
form. At the CLJS repl prompt, call `(l/validate-form)` again. You
should now see that `(l/validate-form)` returns `true`, passing
control to the [original][6] server-side script which we're going to
implement in CLJ in a subsequent tutorial.

```bash
cljs.user=> (l/validate-form)
true
cljs.user=>
```

## Shopping calculator sample

Now let's try to port to CLJS a second example from Larry Ullman
[Modern JavaScript][6] book: a kind of e-commerce tool that will
calculate the total of an order, including tax, minus any discount.

### Pure HTML/CSS page

Here is the `shopping.html` content which is in line with
[clojurescriptone approach][4] and [Larry Ullman][6] to keep the design
of the HTML/CSS/images separated from the code which is going to implement
its behaviour. Save it in `resources/public` directory.

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
  <form action="" method="post" id="shoppingForm" novalidate>
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
      <div>
        <input type="submit"
               value="Calculate"
               id="submit">
      </div>
    </fieldset>
  </form>
  <script src="js/modern.js"></script>
</body>
</html>
```

As before, we included the link to `js/modern.js` external JS file
which will be generated by the CLJS compilation. Note that this time
we have not attached any value to the `action` attribute of the
form. That's because in this new example there is no server-side form
submission.

The following picture shows the rendered `shopping.html` page.

![Shopping Page][9]

### Shopping calculator CLJS code

Now it's time to code the implementation of the shopping calculator. We
need to read a few values from the calculator form:

* quantity
* price per unit
* tax rate
* discount

We then have to calculate the total, write back the result in the form
and return `false` to prevent the browser from attempting to submit the
data to a server-side script.

Create the `shopping.cljs` file in the `src/cljs/modern_cljs` directory and
enter the following code:

```clojure
(ns modern-cljs.shopping
  (:use [domina :only [by-id value set-value!]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))
    false))

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [the-form (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit the-form) calculate))))

(set! (.-onload js/window) init)
```

Let's now try our little shopping calculator as usual:

```bash
lein ring server-headless # in the modern-cljs home directory
lein cljsbuild auto # in the modern-cljs directory in a new terminal
lein trampoline cljsbuild repl-listen # in the modern_cljs directory in a new terminal
```

### A short troubleshooting session

Now visit `localhost:3000/shopping.html` and run the calculator by
clicking the `Calculate` button. You'll receive a "Page not
found" error. What happened?

The error is not very informative. We have not yet introduced any
debugging tools to be used in such a case, so we try troubleshooting
with what we have in our hands: the CLJS repl connected to the browser
(i.e., brepl). In the previous login form sample we ran the
`(l/validate-form)` function from the brepl to test its
behaviour. Let's try the same thing by evaluating the `(calculate)`
function we just defined in `modern-cljs.shopping` namespace.

First, click the back button of your browser to return to the
`shopping.html` page and then evaluate the following CLJS expressions
in the brepl.

```bash
cljs.user=> (require '[modern-cljs.shopping :as s])

cljs.user=> (s/calculate)
false
```

The `calculate` functions correctly returns `false` and the `Total`
shown by the calculator form is correct too.

![Total][11]

This means that the `calculate` function is correctly called in the
brepl, but not by the `Calculate` button of the form. Let's see if the
brepl may help us in investigating the problem we have.

```bash
cljs.user=> (.-onsubmit (.getElementById js/document "shoppingForm"))
nil
cljs.user=>
```

Oops, the `onsubmit` property of `shoppingForm` form element has no
value. `calculate` should have been set as its value by `init`
function which, in turn, should have been set as the value of the
`onload` property of the `window` object. Let's now see what's the
value of the `onload` property of the `window` object.

```bash
cljs.user=> (.-onload js/window)
#object[modern_cljs$login$init "function modern_cljs$login$init() {
  if (cljs.core.truth_(function() {
    var and__4557__auto__ = document;
    if (cljs.core.truth_(and__4557__auto__)) {
      return document.getElementById;
    } else {
      return and__4557__auto__;
    }
  }())) {
    var login_form = document.getElementById("loginForm");
    return login_form.onsubmit = modern_cljs.login.validate_form;
  } else {
    return null;
  }
}"]
cljs.user=>
```

Oops, the `init` function assigned as the value for the `window` `onload`
property is not the one we just defined, but the `init` function we
defined for `loginForm`.

What just happened has to do with the Google Closure Compiler
(i.e., cljsbuild). It gets every CLJS file from `:source-paths`
we set in the very [first tutorial][10] and compiles all of them in
the `js/modern.js` file we set in the same tutorial as the value of
the `:output-to` option of `lein-cljsbuild` plugin.

To temporarily solve this problem, evaluate the `(init)` function in
the brepl as follows:

```bash
cljs.user=> (s/init)
#object[modern_cljs$shopping$calculate "function modern_cljs$shopping$calculate() {
  var quantity = domina.value.call(null, domina.by_id.call(null, "quantity"));
  var price = domina.value.call(null, domina.by_id.call(null, "price"));
  var tax = domina.value.call(null, domina.by_id.call(null, "tax"));
  var discount = domina.value.call(null, domina.by_id.call(null, "discount"));
  domina.set_value_BANG_.call(null, domina.by_id.call(null, "total"), (quantity * price * (1 + tax / 100) - discount).toFixed(2));
  return false;
}"]
cljs.user=>
```

You can now use the *Shopping Calculator* form by clicking its
*Calculate* button.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "introducing domina"
```

# Next Step [Tutorial 6: The Easy Made Complex and Simple Made Easy][12]

In the [next tutorial][12] we're going to investigate and solve in two
different ways the problem we just met.

# License

Copyright © Mimmo Cosenza, 2012-2014. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/levand/domina
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[3]: https://github.com/brentonashworth/one
[4]: https://github.com/brentonashworth/one/wiki/Design-and-templating
[5]: https://github.com/weavejester/hiccup
[6]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[7]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#host-interop
[8]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
[9]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping.png
[10]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[11]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/total.png
[12]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[13]: https://help.github.com/articles/set-up-git
