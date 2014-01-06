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
and `domina` is going to be our CLJS library to interface the DOM of the
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
                 [domina "1.0.3-SNAPSHOT"]]
  ...)
```

> NOTE 1: Due to few bugs of the `domina 1.0.2` release pertaining the
> access to JS object properties when used with recent  CLJS
> releases, I added the `1.0.3-SNAPSHOT` release which fixed those
> bugs.

## Domina selectors

[Domina][1] offers several selector functions: `xpath`, in the `domina.xpath`
namespace, and `sel`, in the `domina.css` namespace. But it also features the
`by-id`, `value` and `set-value!` functions defined in the `domina` core
namespace, which is the one we're going to use.

The nice thing about domina `(by-id id)`, inherited from the underlying
Google Closure library on which `domina` is implemented, is that it
takes care of verifying if the passed argument is a string. As we
anticipated, the `domina` core namespace offers other useful functions we're
going to use: `(value el)`, which returns the value of the passed
element, and `(set-value! el value)` which sets its value.

> NOTE 2: when a function modifies an argument passed to it, by Clojure
> naming convention a bang "!" is added at the end of the function
> name.

> NOTE 3: when you need to :use or :require a namespace, CLJS imposes
> using the :only form of :use and the :as form of :require. For further
> differences see [the ClojureScript Wiki][8]

## Modify validate-form

In this step we're going to modify the namespace declaration and
`validate-form` function definition substituting `.getElementById` and
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
lein ring server
lein cljsbuild once # in a new terminal and after having cd in modern-cljs
lein trampoline cljsbuild repl-listen
```

> NOTE 4: be sure to `cd` to the home directory of the project in each
> terminal you open.

Open <http://localhost:3000/login.html>, and when the CLJS repl becomes responsive,
having established a connection with the browser, try the following at the REPL prompt:

```bash
ClojureScript:cljs.user> (in-ns 'modern-cljs.login)

ClojureScript:modern-cljs.login> validate-form
#<function validate_form() {
  var email__6289 = domina.by_id.call(null, "email");
  var password__6290 = domina.by_id.call(null, "password");
  if(function() {
    var and__3822__auto____6291 = cljs.core.count.call(null, domina.value.call(null, email__6289)) > 0;
    if(and__3822__auto____6291) {
      return cljs.core.count.call(null, domina.value.call(null, password__6290)) > 0
    }else {
      return and__3822__auto____6291
    }
  }()) {
    return true
  }else {
    alert("Please, complete the form!");
    return false
  }
}>
ClojureScript:modern-cljs.login>
```

The evaluation of the `validate-form` symbol returns the JS function
definition attached by the CLJS compiler to the symbol itself. If you
now try to call the function `(validate-form)`, you should see the
browser alert window asking you to complete the form; click the `ok`
button and you'll see `(validate-form)` returning `false`.

```bash
ClojureScript:modern-cljs.login> (validate-form)
false
ClojureScript:modern-cljs.login>
```

Fill both the `Email Address` and `Password` fields of the login
form. At the CLJS repl prompt, call `(validate-form)` again. You should
now see `(validate-form)` returning `true`, passing the control to the
[original][6] server-side script which we're going to implement in a
subsequent tutorial using CLJ.

```bash
ClojureScript:modern-cljs.login> (validate-form)
true
ClojureScript:modern-cljs.login>
```

## Shopping calculator sample

Now let's try to port to CLJS a second example from Larry Ullman
[Modern JavaScript][6] book: a kind of an e-commerce tool that will
calculate the total of an order, including tax, and minus any discount.

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

As before, we included the link to `js/modern.js` external JS file which
will be generated by the CLJS compilation. Note that this time we have
not attached any value to the `action` attribute of the form. That's
because in this new example there is no server-side form submission.

The following picture show the rendered `shopping.html` page.

![Shopping Page][9]

### Shopping calculator CLJS code

Now it's time to code the implementation of the shopping calculator. We
need to read few values from the calculator form:

* quantity
* price per unit
* tax rate
* discount

We then have to calculate the total, write back the result in the form
and return the `false` value because there is no a server-side script to
which submit any data.

Create the `shopping.cljs` file in `src/cljs/modern_cljs` directory and
type into it the following code

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
    (let [theForm (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit theForm) calculate))))

(set! (.-onload js/window) init)
```

Let's now try our little shopping calculator as usual:

```bash
lein ring server # in the modern-cljs home directory
lein cljsbuild auto # in the modern-cljs directory in a new terminal
lein trampoline cljsbuild repl-listen # in a the modern_cljs directly in a new terminal
```

### A short trouble shooting session

Now visit `localhost:3000/shopping.html` and run the calculator by
clicking the `Calculate` button. You'll receive a "Page not
found". What's happened?

The received error is not so informative. We have not yet introduced
any debugging tool to be used in such a case, so we try shooting the
trouble with what we have in our hands: the CLJS repl connected to the
browser (i.e. brepl). In the previous login form sample we ran the
`(validate-form)` function from the brepl to test its behaviour. Let's
try the same thing by evaluating the `(calculate)` function we just
defined in `modern-cljs.shopping` namespace.

First, click the back button of your browser to show again
`shopping.html` page and then evaluate the following CLJS expressions
in the brepl.

```bash
ClojureScript:cljs.user> (in-ns 'modern-cljs.shopping)

ClojureScript:modern-cljs.shopping> (calculate)
false
ClojureScript:modern-cljs.shopping>
```

The `calculate` functions correctly returns `false` and the `Total`
shown by the calculator form is correct too.

![Total][11]

This means that the `calculate` function is correctly called in the
brepl, but not by the `Calculate` button of the form. Let's see if the
brepl may help us in investigating the problem we have.

```bash
ClojureScript:modern-cljs.shopping> (.-onsubmit (.getElementById js/document "shoppingForm"))
nil
ClojureScript:modern-cljs.shopping>
```

Oops, the `onsubmit` property of `shoppingForm` form element has no
value. `calculate` should have been set as its value by `init`
function which, in turn, should have been set as the value of the
`onload` property of the `window` object. Let's now see what's the
value of the `onload` property of the `window` object.

```bash
ClojureScript:modern-cljs.shopping> (.-onload js/window)
#<function init() {
  if(cljs.core.truth_(function() {
    var and__3822__auto____6439 = document;
    if(cljs.core.truth_(and__3822__auto____6439)) {
      return document.getElementById
    }else {
      return and__3822__auto____6439
    }
  }())) {
    var login_form__6440 = document.getElementById("loginForm");
    return login_form__6440.onsubmit = modern_cljs.login.validate_form
  }else {
    return null
  }
}>
ClojureScript:modern-cljs.shopping>
```

Oops, the `init` function assigned as value for the `window` `onload`
property is not the one we just defined, but the `init` function we
defined to initialize the previous `loginForm`.

What just happened has to do with the Google Closure Compiler
(i.e. cljsbuild). It gets every CLJS file from `:source-paths` keyword
we set in the very first [tutorial][10] and compiles all of them in
the "js/modern.js" file we set in the same tutorial as the value of
the `:output-to` option of `lein-cljsbuild` plugin.

To temporarily solve this problem, evaluate the `(init)` function in
the brepl as follows:

```bash
ClojureScript:modern-cljs.shopping> (init)
#<function calculate() {
  var quantity__18253 = domina.value.call(null, domina.by_id.call(null, "quantity"));
  var price__18254 = domina.value.call(null, domina.by_id.call(null, "price"));
  var tax__18255 = domina.value.call(null, domina.by_id.call(null, "tax"));
  var discount__18256 = domina.value.call(null, domina.by_id.call(null, "discount"));
  domina.set_value_BANG_.call(null, domina.by_id.call(null, "total"), (quantity__18253 * price__18254 * (1 + tax__18255 / 100) - discount__18256).toFixed(2));
  return false
}>
ClojureScript:modern-cljs.shopping>
```

You can now use the *Shopping Calculator* form clicking its
*Calculate* button.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "introducing domina"
```

# Next Step [Tutorial 6: Easy made Complex and Simple made Easy][12]

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
[7]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[8]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[9]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping.png
[10]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[11]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/total.png
[12]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[13]: https://help.github.com/articles/set-up-git
