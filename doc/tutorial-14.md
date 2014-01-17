# Tutorial 14 - Better Safe Than Sorry (Part 1)

In this part of the tutorial we're going to prepare the field for one
of the main topics in the software development life cycle: *code
testing*. Code testing is a kind of continuum which goes from zero to
almost full test coverage. I'm not going to open one of those endless
discussions about the right amount of testing. Code testing
is a need, full stop. How much coverage? It depends.

I have to admit that I never took a program from a unit test that
has to initially fail before I could proceed to success. When you are as
old as I am,
you can't change your habits. So, whoever is religious about TDD/BDD (Test
Driven Development and Behavioural Driven Development) must forgive me.

Nowadays, by using functional programming languages like CLJ/CLJS, the
unit tests are much easier to implement, as compared with
imperative and object-oriented programming languages, because most of
the time you have to deal with pure functions whose output depend
only on the passed input.

## Introduction

Before we go ahead with the problem of testing your CLJ/CLJS
code, we have to finish something we left behind in the previous
tutorials. In the [Tutorial 10 - Introducing Ajax - ][1] we
implemented a Shopping Calculator by using the Ajax style of
communication between the browser (i.e. ClojureScript) and the server
(i.e. Clojure).

Obviously, nobody will ever implement that kind of stupid widget by
using Ajax, because all the information needed to make the
calculation from the input is already present on the browser
side. By moving the calculation of the `Total` from the client side to
the server side, we found an excuse to gently introduce a little bit
of Ajax in CLJS/CLJ.

Nonetheless, by failing to implement the server-side only Shopping
Calculator, we have broken the first principle of the progressive
enhancement strategy, which dictates:

> Start to develop the front-end by ignoring the existence of JavaScript
> for a while.

But we're in a good position to cover the missing step, because we
have the same language on both sides. It should not be a PITA to go
from one side to the other and vice-versa. Just be prepared to pay a
little attention anytime you cross the border in either direction.

And remember, you can even move the border, if this is useful for any
reason. By moving the border I mean that you can enable pieces
of your code to be movable at will from one side to the other side of the
border.

## Review the Shopping Calculator

Start by reviewing the Shopping Calculator program. If you want to keep
track of your next steps do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-13
git checkout -b tutorial-14-step-1
```

Then compile and run `modern-cljs` as usual:

```bash
lein cljsbuild auto prod
lein ring server-headless # in a new terminal from modern-cljs dir
```

Now visit the [shopping URI][2], and click the `Calculate` button. The
`Total` field is valued with the result of the calculation executed via
Ajax on the server-side.

Note that the `localhost:3000/shopping.html` URI shown in the address
bar of the browser does not change when you click the `Calculate`
button. Only the value of the `Total` field will be updated, even if the
calculation was executed by the server. That's one of the beauties of
Ajax.

As you already know from the [Tutorial 10 - Introducing Ajax - ][1] you
can open the `Developer Tools` panel of your browser (I'm using Google
Chrome) and take a look at the Network tab after having reloaded the
[shopping URI][2].

Any time you click the `Calculate` button a new `_shoreleave` POST
asynchronous method request is submitted to the server which responds
with the HTTP/1.1 202 status code (i.e. Accepted).

![AjaxNetwork][3]

But what happens if you disable JavaScript? Let's try. On Google
Chrome you disable the JavaScript engine by clicking on the Setting's
icon positioned in the very right bottom of the `Developer tool`
window. It opens a panel from where you can mark the `Disable
JavaScript` check-box.

![DisableJavaScript][4]

Now close the Setting's panel and reload the [shopping URI][2]. If you
move the mouse cursor over the `Calculate` button nothing happens and
nothing happens even if you click it.

Take a look at the `shopping.html` file which is under the
`resources/public` directory of the `modern-cljs` main directory.

```html
<!doctype html>
<html lang="en">
<head>
        ...
        ...
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
               id="calc">
      </div>
    </fieldset>
  </form>
    ...
        ...
</body>
</html>
```

As you can see, the `form` tag has no `action` or `method` attributes
and the `type` attribute of the Calculate input is set to
`button`. This means that, when JavaScript is disabled, the `form`
does not respond to any event.

## Step 1 - Break the Shopping Calculator

Now modify the `shoppingForm` by adding the `action="/shopping"` and
the `method="POST"` attribute/value pairs. Next, change the Calculate
input from `type="button"` to `type="submit"` attribute/value
pair. Following is the interested snippet of the modified HTML code.

```html
  ...
  <form action="/shopping" id="shoppingForm" method="POST" novalidate>
    <legend> Shopping Calculator</legend>
    <fieldset>
    ...
    ...
      <div>
        <input type="submit"
               value="Calculate"
               id="calc">
      </div>
    </fieldset>
  </form>
  ...
  ...
</html>
```

Reload the [shopping URI][2] and click the `Calculate` button
again. You receive a plain `Page not found` text from the server. You
also see the `localhost:3000/shopping` URI in the address bar of your
browser instead of the previous `localhost:3000/shopping.html` URI.

In the [Tutorial 3 - CLJ based http-server - ][5] we used
[Compojure][6] to define the application's routes as follows:

```clojure
(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to authenticate the user
  (POST "/login" [email password] (authenticate-user email password))
  ;; to server static pages saved in resources/public directory
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))
```

It explains why we received the `Page not found` page: we did not
define any route for the `localhost:3000/shopping` URI requested by
the `Calculate` button.

By setting the `method` attribute of the `shoppingForm` to `POST` and
the `type` attribute of its `calc` input field to `submit`, we are
asking the browser to send a POST request with the
`localhost:3000/shopping` URI whenever the user clicks the Calculate
button and this URI does not exist.

### A kind of TDD

By modifying the `shopping.html` file and disabling the JavaScript
from the browser, we have just exercized a kind of virtual TDD (Test
Driven Development) simulation.

To fix the failure we just met, we need to add a route for the
"/shopping" request to the `defroutes` macro call. Open the
`src/clj/modern_cljs/core.clj` file and add the "/shopping" POST route
as follows.

```clojure
(defroutes app-routes
  ...
  (POST "/shopping" [quantity price tax discount]
        (str "You enter: "quantity " " price " " tax " and " discount "."))
  ...
)
```

> NOTE 1: In the Restful communities, which I respect a lot, that
> whould be a blasphemy, because the Shopping Calculator is an
> application resource which, in Restful parlance, is safe and
> idempotent and we should have used the default GET verb/method.

> NOTE 2: we also extract the values of the input parameters of the
> `shoppingForm` by passing the args vector
> `[quantity price tax discount]` to the POST call.

Now reload the [shopping URI][2] and click again the `Calculate`
button. You should receive a plain text of the input values of the
form.

![FictionShopping][7]

Let's know go back for a while and see what happens if we re-enable the
JavaScript engine of the browser. Open again the Setting of the
Developer Tools and enable the JavaScript engine by unmarking the
`Disable JavaScript` check-box.

Next, reload the [shopping URI][2] and finally click the `Calculate`
button again.

![FictionShopping2][7]

Ops, it seems that the Ajax version of the Shopping Calculator does not
work anymore. What happened?

### Fix the failed test

Now that we have changed the `Calculate` input from `type="button"` to
`type="submit"`, when the user clicks it then control passes to
the `action="/shopping"` and submits a POST request to the
server. The server then responds by calling the handler function which
is now associated with the POST "/shopping" route.

We already dealt with this problem in a [previous tutorial][8]
dedicated to the `login` example. We solved it by preventing the
above from happening.

We need to code the same thing in the
`src/cljs/modern_cljs/shopping.cljs` file.

Open the `shopping.cljs` file and modify the function associated with the
`click` event as follows.

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
```

We wrapped the `calculate` function inside an anonymous function,
which now receives an event as the sole argument.

Now we need to modify the `calculate` function definition as follows, to
prevent the `click` event from being passed to the `action` of the
Shopping form.

```clojure
(defn calculate [evt]
  (let [quantity (read-string (value (by-id "quantity")))
        price (read-string (value (by-id "price")))
        tax (read-string (value (by-id "tax")))
        discount (read-string (value (by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))
```

We updated the signature of the `calculate` function to accept the
event and added the `(prevent-default evt)` as the last call in its
definition, which interrupts the flow.

The last modification we have to introduce is to add the
`prevent-default` symbol to the `:refer` section of the `domina.events`
requirement as follows:

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]]))
```

If you did not stop the `cljsbuild` auto compilation from the previous
run, as soon as you save the file you should see the CLJS compiler
producing an updated version of `modern.js` script file.

Reload the [shopping URI][2]. You should now see the Ajax version of
the Shopping Calculator working again as expected.

Not bad so far. I suggest you to commit your work now by issuing
the following `git` command:

```bash
git commit -am "Step 1"
git checkout -b tutorial-14-step-2
```

The second `git` command clones the `tutorial-14-step-1` branch into
the `tutorial-14-step-2` branch and sets the latter as the active
branch in preparation for the next sage of our work.

## Step 2 - Enliving the server-side

In the previous pragraphs of this tutorial we prepared the stage for
introducing [Enlive][9] by [Christophe Grand][27], one of the most
famous CLJ libs in the Clojure communities. There are already few
[Enlive tutorials][10] available online and I'm not going to add
anything beyond the simplest use case to allow us to implement the
server-side only Shopping Calculator in accordance with the
progressive enhancement principle.

The reasons why I chose [Enlive][9] are very well motivated by
[David Nolen][11] in his [nice tutorial][12] on Enlive:

> Enlive gives you the advantages of designer accessible templates
> (since they’re just HTML) without losing the power of function
> composition. As a result, your designer can create all the various
> widgets for your website using only HTML and CSS and you can compose
> your pages from any combination of their designs.

This is similar to [Domina][13] separation of concern which allows the
designer and the programmer to play their roles without too many
impedance mismatches.

Our needs are very easy to describe. We have to:

1. Read a pure HTML template/page from the file system representing
the Shopping Calculator;
2. read the parameters typed in by the user from the submitted HTTP
   request;
3. parse the extracted values and calculate the total;
4. update the fields in the HTML form; and
5. send the resulted page to the user.

The following picture shows a sequence diagram of the above description.

![Shopping-server][14]

Obviously we should also validate all inputs, but this is something
we'll take care of later in the next tutorial.

### Enter Enlive

The steps `2.` and `5.` are already satisfied by the `defroutes` macro
from [Compojure][6]. The step `3.` - calculate the total - seems to be
already satisfied by the the `defremote` macro call from
[Shoreleave][15], which implicitly defines a function with the same
name.

It seems that we just need to implement the step `1.` - read the
`shopping.html` file from the `resources/public` directory and the
step `4.` - update the input fields of the form.

[Enlive][9] offers a single macro, `deftemplate`, which allows us to
resolve both `1.` and `4.` in a single shot.

`deftemplate` accepts 4 arguments:
* `name`
* `source`
* `args`
* `& forms`.

It creates a function with the same number of `args` and the same `name`
as the template. The `source` can be any HTML file located in the
`classpath` of the application.

Finally `&forms` is composed of a sequence of pairs. The left hand of
the pair is a vector of CSS-like selectors, used to select the
interested elements/nodes from the parsed HTML source. The right hand
of the pair is a function which is applied to transform each selected
element/node.

If you issue the `lein classpath` command from the terminal, you can
verify that the `resources` directory is a member of the application
`classpath`. This means that we can pass the
`public/shopping.html` file to `deftemplate` as the `source` arg.

As the `name` arg, we're going to use the same name of the POST route
(i.e. `shopping`) previously defined inside the `defroutes` macro.

Then, the `args` to be passed to `deftemplate` are the same we defined
in the `(POST "/shopping" [quantity price tax discount] ...)` route.

Finally, regarding the `& forms` arg, start by instantiating it with
two `nil` values, which means no selectors and no transformations. I
now expect that the source will be rendered exactly as the original
HTML source.

### Let's code

First, as usual, we need to add the [Enlive][9] lib to the
`project.clj`.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [...
                 [enlive "1.1.4"]]
  ...)
```

We then have to decide where to create the CLJ file containing the
template definition for the Shopping Calculator page. Because I prefer
to mantain a directory structure which mimics the logical structure of
an application I decided to create a new `templates` directory under
the `src/clj/modern_cljs/` directory.

```bash
mkdir src/clj/modern_cljs/templates
```

Inside this directory create the `shopping.clj` file where we can put the
`deftemplate` macro call.

Following is the content of the `shopping.clj` file which contains the
definition of the Shopping Calculator template

```clojure
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]))

(deftemplate shopping "public/shopping.html"
  [quantity price tax discount]
  nil nil)
```

Now that we have defined the `shopping` template, which implicitly
defines the `shopping` function, we can go back to the `core.clj` to
update its namespace declaration and substitute the `(str "You enter:
" quantity " " price " " tax " and " discount ".")` call with the call
to the newly defined `shopping` function.

```clj
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [modern-cljs.login :refer [authenticate-user]]
            [modern-cljs.templates.shopping :refer [shopping]]))

(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to authenticate the user
  (POST "/login" [email password] (authenticate-user email password))
  ;; to server static pages saved in resources/public directory
  (POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))
```

If the `lein ring server-headless` command is still running, stop it
by `Ctr-C` and run it again to allow the server to import the new
`enlive` dependencies.

```bash
lein ring server-headless`
```

> NOTE 3: We need to stop the running ring server because we added the
> [Enlive][9] lib to the project dependencies. Thanks to
> [Chas Emerick][16] we can now add new dependencies in the REPL to a
> running project by using the [Pomegranate][17] lib.

Now disable the JavaScript engine of your browser again and visit the
[shopping][2] URI.

You should see the Shopping Calculator page showing the default field
values again and again each time you press the `Calculate` button, no
matter what you typed in the value boxes of the fields. This is exactly
what we expected, because we did not select any node and any
transformation of the nodes. So far so good.

### Select and transform

It's now time to fill the gap in the `deftemplate` call by adding the
appropriate selector/transformation pairs.

For a deeper understanding of the CSS-like selectors accepted by
`deftemplate`, you need to understand CSS selectors. You should know
them even if you want to use [Domina][18] or [JQuery][19]. So, even if
we'd like to have a unified language all over the places, you can't
avoid to learn a little bit of HTML, CSS and JS to use
CLJ/CLJS. That's the life we have to live with.

A selector in [Enlive][9] is almost identical to the corresponding CSS
selector. Generally speaking you just need to wrap the CSS selector
inside a CLJ vector and prefix it with the colon `:` (i.e. keywordize
the CSS selectors).

For example, if you want to select a tag with an `id="quantity"`
attribute, you need to write `[:#quantity]` which corresponds to the
`#quantity` CSS selector.

> NOTE 4: I strongly suggest you to read the enlive
> [syntax for selector][20] at least to have a decent understanding of
> it.

But what about the transformation functions? [Enlive][9]
offers a lot of them but this is not a tutorial on [Enlive][9], and I'm
going to use the only function we need in our context: the `(set-attr
&kvs)` function.  It accepts keyword/value pairs where the keywords
are the names of the attributes you want to set. In our sample, the
only attribute we are going to set is the `value` attribute of each
`input` field. So let's start by adding to the `deftemplate` call both
the selector clause and the trasformation function as follows:

```clojure
(deftemplate shopping "public/shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount))
```

Reload the [shopping][2] URI and change the values of the input fields
of the Shopping Calculator form. By clicking the `Calculate` button
you'll receive the form with the same values you previously typed
in. So far, so good.

It's now time to make the calculation and to set the result in the
`Total` field of the `shoppingForm`.

### Evolve by refactoring the code

> By continuously improving the design of code, we make it easier and
> easier to work with. This is in sharp contrast to what typically
> happens: little refactoring and a great deal of attention paid to
> expediently adding new features. If you get into the hygienic habit
> of refactoring continuously, you'll find that it is easier to extend
> and maintain code.   - Joshua Kerievsky, Refactoring to Patterns

In the [Tutorial 10 - Introducing Ajax][1] we defined the remote
`calculate` function by calling the `defremote` macro. The
`defremote` call implicitly define a function with the same name
of the remote function and that's good, because we hate any kind of
code duplication. We could immediately use it to calculate the result
of the Shopping Calculator by just parsing the
`[quantity price tax discount]` passed to the `deftemplate` call. But
wait a minute. We [already parsed those arguments][21] on the CLJS
side of the `calculate` function and we don't want to parse them
again. To reach this DRY objective we need to refactor the code by
moving the parsing code of the fields values from the client side to
the server side.

Let's take a look at the CLJS `shopping.cljs` file where we defined
the client side `calculate` function.

```clojure
(defn calculate []
  (let [quantity (read-string (value (by-id "quantity")))
        price (read-string (value (by-id "price")))
        tax (read-string (value (by-id "tax")))
        discount (read-string (value (by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))))

```

As you can see, to parse the input string, we used the `read-string`
function from the `cljs.reader` lib of CLJS. We're again tripping over
the [Features Expression Problem][22] we met in the previous
[Tutorial 13 - Don't Repeat Yourself while crossing the border][23].

CLJ and CLJS have different ways to parse a string and different ways
to convert a number, a stringfied integer, or double number. As usual
[Chas Emerick][16] is going to help us a lot again. In the context of
his [valip][24] lib we already used in the [Tutorial 13][25], he defined
the following portable functions in the [valip.predicates][26]
namespace:

```clojure
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms.
All predicates in this namespace are considered portable between different
Clojure implementations."
  (:require [clojure.string :as str]
            [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))

(defn integer-string?
  "Returns true if the string represents an integer."
  [s]
  (boolean (re-matches #"\s*[+-]?\d+\s*" s)))

(defn decimal-string?
  "Returns true if the string represents a decimal number."
  [s]
  (boolean (re-matches #"\s*[+-]?\d+(\.\d+(M|M|N)?)?\s*" s)))

;; private
(defn- parse-number [x]
  (if (and (string? x) (re-matches #"\s*[+-]?\d+(\.\d+M|M|N)?\s*" x))
    (read-string x)))
```

### Portable functions

By taking inspiration from there, we are going to create an
`utils.clj` file containing the definition of few useful and portable
functions to help us in parsing the input of the `shoppingForm`.

Create the `utils.clj` file in the `src/clj/modern_cljs`
directory and write the following content:

```clojure
(ns modern-cljs.utils
  (:require [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))

(defn parse-integer [s]
  (if (and (string? s) (re-matches #"\s*[+-]?\d+\s*" s))
    (read-string s)))

(defn parse-double [s]
  (if (and (string? s) (re-matches #"\s*[+-]?\d+(\.\d+(M|M|N)?)?\s*" s))
    (read-string s)))

(defn parse-number [x]
  (if (and (string? x) (re-matches #"\s*[+-]?\d+(\.\d+M|M|N)?\s*" x))
    (read-string x)))
```

> NOTE 5: This is the first time we see the `:refer-clojure` section
> in a namespace declaration. Its objective is to prevent namespace
> conflicts. In our scenario, by using the `:exclude` keyword, we
> prevent the CLJS `read-string` function from conflicting with the
> corresponding CLJ `read-string` function.

We now have three portable functions to parse a generic number, an
integer and a double, which means we'll be free to use them both in
the CLJS side and the CLJ side application code. Not bad.

Let's now refactor the `calculate` functions we defined in both CLJS
and CLJ source files. Open the `shopping.cljs` file under the
`src/cljs/modern_cljs` directory and modify it by removing the
`cljs.reader` from the namespace requirements and by removing the
calls to `read-string` in the `calculate` function definition as
follows:

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))

(defn calculate [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))

;;; the rest as before
```

Now the `:calculate` remote-callback function accepts strings as
arguments and we need to refactor it as well. Open the `remote.clj`
file under the `src/clj/modern_cljs` directory and modify it by
requiring `modern-cljs.utils` in the namespace declaration and by
adding the `parse-integer` and `parse-double` calls to the `defremote`
definition of `calculate`.

```clojure
(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [modern-cljs.login.java.validators :as v]
            [modern-cljs.utils :refer [parse-integer parse-double]]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (let [q (parse-integer quantity)
        p (parse-double price)
        t (parse-double tax)
        d (parse-double discount)]
  (-> (* q p)
      (* (+ 1 (/ t 100)))
      (- d))))

;;; the rest as before
```

We're now ready to add the `calculate` function to the template
definition in the `shopping.clj` file under the
`src/clj/modern_clj/templates` directory.

Open and modify the above file as follows:

```clojure
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]
            [modern-cljs.remotes :refer [calculate]]))

(deftemplate shopping "public/shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount)
  [:#total] (set-attr :value
                      (format "%.2f" (double (calculate quantity price tax discount)))))
```

> NOTE 6: We added the `format` call to format the `Total` value with
> two digits after the decimal point. Note that we [casted][32] the
> `calculate` result to `double`.

Assuming that you have stopped the `lein ring server-headless`
command from the terminal, if you now try to launch the command again
you receive a compilation error:

```bash
lein ring server-headless
Exception in thread "main" java.lang.Exception: Cyclic load dependency: [ /modern_cljs/remotes ]->/modern_cljs/templates/shopping->/modern_cljs/core->[ /modern_cljs/remotes ]
...
...
Subprocess failed
```

### FIAT - Fix It Again Tony

Too bad. We just met a cyclic namespaces dependency problem. Cyclic namespace
dependencies are not allowed in CLJ so you need to refactor the code.

Our scenario is simple enough. Remove the `modern-cljs.core` reference
from the `modern-cljs.remotes` namespace declaration. There, we only
referenced the `handler` symbol from the `modern-cljs.core` namespace
in the `app` definition. By moving the `app` definition to the
`modern-cljs.core` namespace we should be able to resolve the cyclic issue.

Following is the modified content of the `remotes.clj` file where we
have removed both the reference to the `modern-cljs.core` namespace
and the `app` symbol definition.

```clojure
(ns modern-cljs.remotes
  (:require
   ;;       [modern-cljs.core :refer [handler]]
            [modern-cljs.login.java.validators :as v]
            [modern-cljs.utils :refer [parse-integer parse-double]]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (let [q (parse-integer quantity)
        p (parse-double price)
        t (parse-double tax)
        d (parse-double discount)]
  (-> (* q p)
      (* (+ 1 (/ t 100)))
      (- d))))

(defremote email-domain-errors [email]
  (v/email-domain-errors email))
```

> NOTE 7: We also removed the reference to the `wrap-rpc` symbol from
> the `shoreleave.middleware.rpc` requirement because it is not used
> anymore by any functions defined in the file.

Next, we need to add the `app` symbol definition in the
`modern-cljs.core` namespace and add the `shoreleave.middleware.rpc`
requirement to be able to reference the `wrap-rpc` symbol in the `app`
definition. Following is the modified content of the `core.clj` file.

```clojure
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [modern-cljs.login :refer [authenticate-user]]
            [modern-cljs.templates.shopping :refer [shopping]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to authenticate the user
  (POST "/login" [email password] (authenticate-user email password))
  ;; to server shopping command
  (POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))
  ;; to server static pages saved in resources/public directory
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))

;;; site function create an handler suitable for a standard website,
;;; adding a bunch of standard ring middleware to app-route:
(def handler
  (site app-routes))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))
```

Last, but not least, we have to modified the `project.clj` file to
update the namespace of the `app` symbol in the `:ring` section.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :ring {:handler modern-cljs.core/app}
  ...)
```

We are now ready to rebuild and run everything as follows:

```bash
lein do clean, cljsbuild clean, cljsbuild once prod, ring server-headless
```

Now visit [shopping][2] URI and play with the form by enabling and
disabling the JavaScript engine of your browser. Everything should
work as expected in both the scenarios.

## Housekeeping

As you have seen in all the previous tutorials concerning
[lein-cljsbuild][29], most of the times you need to run both a `lein
clean` and `lein cljsbuild clean` command to clean the entire
project. Next you have to issue the `lein cljsbuild once` command to
compile down the cljs files. Until now we use the `do` chaining
feature of `lein` as a little workaround to those repetitions.

[lein-cljsbuild][30] can hook into few Leiningen tasks to enable CLJS
support in each of them. The following tasks are supported:

```bash
lein clean
lein compile
lein test
lein jar
```

Add the following option to your project configuration:

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :hooks [leiningen.cljsbuild]
  ...)
```

You can now run the following commands:

```bash
lein clean # it call lein cljsbuild
lein compile # it call lein cljsbuild once
```

Note that you can't add a build-id to `lein compile` command as you
can do with `lein cljsbuild once` command. So, if you just need to
compile a single build-id you're forced to use the `lein cljsbuild
once <build-id>` command.

[Leiningen][31] even support project-specific task aliases. We'll
introduce this feature in a next tutorial.

## ATTENTION - FINAL NOTES

To be able to run all the `modern-cljs` builds (i.e. `:dev`, `:prod`
and `:dev`), you have to update the `shopping-dbg.html` and
`shopping-prod.html` files with the same modification we did in the
`shopping.html` file.

Then, assuming you added the `:hooks` option to the `project.clj` as
documented above, submit the following commands in the terminal from
the main `modern-cljs` directory.

```bash
lein do clean, compile, ring server-headless
```

Now visit any version of the Shopping Calculator
(i.e. `shopping-dbg.html`, `shopping-pre.html` or `shopping.html`) to
see all of them still working equally, even if the Enlive template has
been defined starting from the `shopping.html` file. This is because
the different script tags in each version of the page are read only
when the JavaScript engine is active.

As a very last step, if everything is working as expected, I suggest you
to commit the changes as follows:

```bash
git add .
git commit -m "finished step-2"
```

# Next Step - [Tutorial 15: It's better to be safe than sorry (Part 2)][28]

In the [next tutorial][28], after having added the validators for the
`shoppingForm`, we're going to introduce unit testing.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[2]: http://localhost:3000/shopping.html
[3]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/networkActivities01.png
[4]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/DisableJavaScript.png
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[6]: https://github.com/weavejester/compojure
[7]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/fictionShopping.png
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md#prevent-the-default
[9]: https://github.com/cgrand/enlive
[10]: https://github.com/cgrand/enlive#enlive-
[11]: https://github.com/swannodette
[12]: https://github.com/swannodette/enlive-tutorial
[13]: https://github.com/levand/domina
[14]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-server.png
[15]: https://github.com/shoreleave/shoreleave-remote-ring
[16]: https://github.com/cemerick
[17]: https://github.com/cemerick/pomegranate
[18]: https://github.com/levand/domina#selectors
[19]: http://jquery.com/
[20]: http://cgrand.github.io/enlive/syntax.html
[21]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md#the-client-side
[22]: http://dev.clojure.org/display/design/Feature+Expressions
[23]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-13.md#cross-the-border
[24]: https://github.com/cemerick/valip
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-13.md#the-selection-process
[26]: https://github.com/cemerick/valip/blob/master/src/valip/predicates.clj
[27]: https://github.com/cgrand
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-15.md
[29]: https://github.com/emezeske/lein-cljsbuild
[30]: https://github.com/emezeske/lein-cljsbuild#hooks
[31]: https://github.com/technomancy/leiningen
[32]: http://clojuredocs.org/clojure_core/clojure.core/format#example_839
