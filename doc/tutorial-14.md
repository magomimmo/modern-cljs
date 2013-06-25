# Tutorial 14 - It's better to be safe than sorry (Part 1)

In this tutorial we're going to prepare the field for one of the main
topic in the software development lifle cycle: code testing.

Code testing is a kind of continuum which goes from zero to almost full
test coverage. I'm not going to open those kind of discussions which
have a never ending story and go nowhere. Code testing is a need, full
stop. How much coverage? It depends.

I started programming almost 30 years ago and I had the fortune of
starting with Prolog and passing many years programming with wonderful
Lisp Machine, which I still miss a lot today. I never departed a program
from a test that has to fail to progress until it succeeds. That's
because I had my Lisp REPL to follow my thoughts and to correct them on
the fly. Nowadays, by using CLJ/CLJS, immutability included, unit tests
are very easy to be implemented because most of the time you manage pure
functions, whose outputs depends only by the passed inputs. That said,
when you're aged like I'm, you can't change your habits. So, who's
religious about TDD/BDD (Test Driven Development and Behavioural Driven
Development), has to be forgiven with me.

## Introduction

In the last [tutorial][1] I hope you had some fun in seeing the DRY
(dont' Repeat Yourself) principle at work, while adhering to the
progressive enhancement strategy. Before to go ahead by affording the
problem of testing your CLJ/CLJS code, we have to fulfill something we
left behind. In the [tutorial 10 - Introducing Ajax][2] we implemented a
Shopping Calculator by using the Ajax style of communication between the
browser and the server: said otherwise, between ClojureScript on the
client side and Clojure on the server side. Obviously, nobody will never
implement that kind of stupid widget by using Ajax, if all the
information he needs to make the calculation from are already in his
hands on the browser side. By moving the calculation from the client
side to the server side, we found an excuse to gently introduce you to
Ajax. After all, this is a series of tutorial on CLJS not on
CLJ. Nonetheless, by omitting to implement also a server-side only
Shopping Calculator, we have broken the first principle of the
progressive enhancement strategy, which dictates:

> NOTE 1: start developing your web application by implementing it as if
> JavaScript would not be available on the browser of the users.

But we own the treasure of having the same language on both sides, so it
will not be a PITA to go from one side to the other and viceversa. Just
be prepared to pay a little attention anytime you cross the border in
any direction. And remember, you can even move the border, if this is
usuful for some reason, which means you can move code from server side
to client side and viceversa. So, let's dance.

## Review the Shopping Calculator

Start by reviewing the Shopping Calculator program. If you take track of
your steps ahead do as follows:

```bash
$ git clone https://github.com/magomimmo/modern-cljs.git
$ cd modern-cljs
$ git checkout tutorial-13
$ git checkout -b tutorial-14-step-1
$ lein cljsbuild auto prod
$ lein ring server-headless # in a new terminal from modern-cljs dir
```

Now visit the [shopping.html][3] page, and click the `Calculate`
button. The `Total` field is updated with the result of the calculation
executed via Ajax on the server-side. As you can see, the
`shopping.html` page showed in the address bar of the browser stays the
same. Only the result of the `Total` field has been updated, even if the
calculation has been executed by the server. That's the beauty of ajax.

As you already know from the [Tutorial 10 - Introducing Ajax][2], by
opening the `Developer Tools` panel of your browser (I'm using Google
Chrome) and by taking a look at the Network tab, after having reloaded
the [shopping.html][3] page, you should see the network activities. Any
time you click the `Calculate` button a new `_shoreleave` POST method
request is submitted to the server which responds with the HTTP/1.1 202
status code (i.e. Accepted, that means asynchronous).

![AjaxNetwork][5]

But what happens if you disable JavaScript? Let's try. Disable the
JavaScript engine on Google Chrome by clicking on the Setting's icon
positioned in the very right bottom of the `Developer tool` window. It
opens a panel from where you can mark the `Disable JavaScript`
check-box.

![DisableJavaScript][6]

Now close the Setting's panel and reload the [shopping.html][3] page.
If you move the mouse cursor over the `Calculate` button nothing happens
and nothing happens even if you click it.

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

As you can see the `form` tag has no `action` and `method` set and the
`type` attribute of the Calculate `input` is set to `"button"` value,
which means that when JavaScript is disabled and the `form` does not
respond to any event.

## Step 1 - Break the Shopping Calculator

Now modify the `form` by adding the `action="/shopping"` and the
`method="POST"` attributes/values. Next change the Calculate `input
type` from `type="button"` to `type="submit"`. Following is the
interested snippet of the modified HTML code.

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

Reload the [shopping.html][3] page and click the `Calculate` button
again. You should receive a `Page not found` message from the server
and see the `localhost:3000/shopping` URI in the address bar of your
browser instead of the previous `localhost:3000/shopping.html` URL.

As you remember from the [Tutorial 3 - CLJ based http-server][4], we
defined the application's routes as follows

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

which explains us why we received the `Page not found` page: our server
does not know anything about the `/shopping` URL now requested by the
the Shopping Calculator form.

Furthermore, by having set the `method` attribute of the `form` to
`POST` and the `type` attribute of the Calculate `input` to `submit`, we
asked the browser to send to the server a POST request with the
`"/shopping"` URL whenever the user clicks the Calculate button.

### A kind of TDD

By modifying the `shopping.html` and disabling the JavaScript from the
browser, we have just exercized a kind of TDD (Test Driver
Development). So the first step is now to fix the failure we just met by
adding a fictional route to the [compojure][7] `defroutes` macro.

Open the `src/clj/modern_cljs/core.clj` file and add a POST route for
the `"/shopping"` URL as follows.

```clojure
(defroutes app-routes
  ...
  (POST "/shopping" [quantity price tax discount]
        (str "You enter: "quantity " " price " " tax " and " discount "."))
  ...
)
```

> NOTE 2: In the Restful community, which I'm starting to appreciated a
> lot, that whould be a blasphemy, because the Shopping Calculator is an
> application resource which is safe and idempotent, in Restful
> parlance, and we should have used the default GET verb/method.

Now reload the `shopping.html` page and click again the `Calculate`
button. You should now receive a message with the values of the input
fields of the Shopping Calculator form.

![FictionShopping][8]

Let's know go back for a while and see what happens if we re-enable the
JavaScript engine of the browser: open again the Setting of the
Developer Tools and enable the JavaScript engine by unmarking the
`Disable JavaScript` check-box.

Next, reload the [shopping.html][3] page and finally click the
`Calculate` button again.

![FictionShopping2][9]

Ops, it seems that the Ajax version of the Shopping Calculator does not
work anymor. What did happen?

### Fix the failed virtual test

By having changed the `Calculate` input from `type="button"` to
`type="submit"`, when now the user clicks it, the control passes to the
`action="/shopping"` which sumits a POST request to the server.

We already afforded this problem in a [previous tutorial][10]
dedicated to the `login` example and we solved it by preventing the
above to happen.

We need to code the same thing in the
`src/cljs/modern_cljs/shopping.cljs` file. Open the `shopping.cljs`
file and modify the function associated to the `click` event as
follows.

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
```

As you can see we wrapped the original `calculate` function inside an
anonymous function, which now receive the event as an argument.

Now we need to modify the `calculate` function definition to be able
to prevenet the `click` event to be passed to the `action` of the
Shopping form as follows.

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

As you can see, after having called the `remote-callback :calculate` we
added the call to the `prevent-default` function, which is defined in
the `domina.events` namespace. The last modification we have to
introduce is to add the `prevent-default` function in the `:refer`
section of the `domina.events` requirement as follows:

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
run you should see the CLJS/Google Closure Compiler running again to
produce the optimized `modern.js` script file.

Reload the [shopping.html][3]. You should now see the Ajax version of
the Shopping Calculator working again as expected.

Not so bad until now. I suggest you to commit now your work by issuing
the following `git` command:

```bash
$ git commit -am "Step 1"
$ git checkout -b tutorial-14-step-2
```

This `git` command clones the step-1 in the step-2 branch and sets the
latter as the active branch in preparation of the next work we have to
do.

## Step 2 - Enliving the server-side

In the Step 1 of this tutorial we prepared the field for introducing
[Enlive][11], one of the most famous CLJ libs in the clojurean
community. There are already [few Enlive tutorials available online][12]
and I'm not going to add anything, but the simplest use case which
allows us to implement the server-side only Shopping Calculator in
accordance with the progressive enhancement principle.

The reasons why I choose Enlive are very well motivated by
[David Nolen][13] in its [nice tutorial][14] on Enlive:

> Enlive gives you the advantages of designer accessible templates
> (since they’re just HTML) without losing the power of function
> composition. As a result, your designer can create all the various
> widgets for your website using only HTML and CSS and you can compose
> your pages from any combination of their designs.

This is similar to [Domina][15] separation of concern which allows the
designer and the programmer to play their roles without too many
impedance mismatches.

Our needs are very easy to be described. We have to:

1. Read from the file system a pure HTML template/page which represents the
Shopping Calculator form;
2. read the parameters typed-in by the user from the HTTP request;
3. parsing the extracted values and make the calculation of the total;
4. update the fields in the HTML form;
5. send the resulted Shopping Calculator page to the user

The following picture shows a sequence diagram of the above description.

![Shopping-server][16]

Obviously we have to validate all inputs, but this is something we'll
take care of later.

### Enter Enlive

The steps `2.` and `5.` are already satisfied by the `defroutes` macro
from `compojure`.  The step `3.` - calculate the total - seems to be
already satisfied by the the `defremote` macro call from `shoreleave`,
which implicitely define a function with the same name.

It seems that we need to implement just the step `1.` - read the
`shopping.html` from the `resources/public` directory and the step
`4.` - update the input fields of the form after the calculation.

Enlive offers a single macro, `deftemplate`, which allows to resolve
both `1.` and `4.` in a single step.

`deftemplate` accepts 4 arguments:
* `name`
* `source`
* `args`
* `& forms`.

It creates a function with the same number of `args` and the same `name`
of the template. The `source` can be any HTML file located in the
`classpath` of the application.

Finally `&forms` is composed by pairs of left hand expressions, which
represent the CSS-like selectors for the parsed source, and a right hand
transformation function which is applied to any selected element/node
from the parsed HTML source.

In you inspect the application classpath by issuing `$ lein classpath`
command from the terminal, you can verify that the `resources` directory
is a member of the application classpath. This means that the HTML
`source` arg could be `public/shopping.html`.

Regarding the `name` arg, just use the same name of the
POST route (i.e. `shopping`) previously defined inside the `defroutes`
macro.

Then, the `args` arg to be passed to `deftemplate` is the same we
already used in the `(POST "/shopping" [quantity price tax discount]
...)` route.

Finally, regarding the `& forms` arg, start by instantiating it with two
`nil`, which means no selectors and no transfomrations. I would expect
that the source will be rendered exactly as the original HTML source.

### Let's code

First, as usual, we need to add the `enlive` lib to the
`project.clj`.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ...
                 ...
                 [com.cemerick/valip "0.3.2"]
                 ;; add enlive dependency
                 [enlive "1.1.1"]]
  ...
  ...
)
```

We then have to decide where to create the CLJ file containing the
template for the Shopping Calculator page. Because I prefer to mantain a
directory structure which mimics the logical structure of an application
I decided to create a new `templates` directory under the
`src/clj/modern_cljs/` directory.

```bash
$ mkdir src/clj/modern_cljs/templates
```

Inside this directory create now the `shopping.clj` file where put the
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

If the `lein ring server-headless` command is still runnin, stop it by
`Ctr-C` and run it again to allow the server to import the new `enlive`
dependencies.

```bash
$ lein ring server-headless`
```

Now disable again the JavaScript engine of your browser and visit the
[shopping.html][3] page.

You should see the Shopping Calculator page showing the default field
values again and again each time you press the `Calculate` button, no
matter what you typed in the value boxes of the fiels. This is exactly
what we should expect, because we did not select any node and any
transformation of the nodes. So far so good.

> NOTE 3: For reasons I'm too lazy to investigate, I needed to add the
> `form="shoppingForm` attribute to the `fieldset` HTML tag, otherwise the
> `enlive` parser of the HTML source would add a second one around the
> input field of the form itself. I hope that some one explains me the
> motivation.

Here is interested snippet of code

```html
<!doctype html>
<html lang="en">
...
...
<body>
  <!-- shopping.html -->
  <form action="/shopping" method="post" id="shoppingForm" novalidate>
    <fieldset form="shoppingForm">
      <legend> Shopping Calculator</legend>
...
...
      <script src="js/modern.js"></script>
      <script>
        modern_cljs.shopping.init();
      </script>
    </fieldset>
  </form>
</body>
</html>

````

### Select and transform

It's now time to fill the gap, the two `nil`, we left behind in the `deftemplate`
definition.

For a depth understanding of the left hand selection clause in the `&
forms` arg of `deftemplate` call, you need to understand CSS
selectors. You should know them even if you want to use [domina][] or
[jquery][]. So, even if we'd like to have an unified language all over
the places, you can't avoid a little bit of HTML and CSS. That's the
life we have to leave with.

A selector in `enlive` is almost identical to the corresponding CSS
selector. Generally speaking you just need to wrap the CSS selector
inside a vector and prepend the seclector with the colon `:`.

For example, if you want to select a tag with an `id="quantity"`, you
just need to write `[:#quantity]` which corresponds to the `#quantity`
CSS selector.

> NOTE 4: I strongly suggest you to read the enlive
> [syntax for selector][] at least to have a decent understanding of its
> differences from CSS selectors.

But what about the transformation functions? `enlive` offers you a lot
of them and this is not a tutorial on `enlive`, which means I'm going to
use the only `enlive` function we really need in our context: the
`(set-attr &kvs)` function which accepts keyword/value pairs where the
keywords are the names of the attroibutes you want to set. In our
context, the only attribute we are going to set is the `value` attribute
of each `input` field. So let's start by adding to the `deftemplate`
both the selector clause and the trasformation function as follows:

```clojure
(deftemplate shopping "public/shopping.html"
  [quantity price tax discount]
  [:#quantity] (set-attr :value quantity)
  [:#price] (set-attr :value price)
  [:#tax] (set-attr :value tax)
  [:#discount] (set-attr :value discount))
```

If you now reload the [shopping.html][3] page and change the values of
the input fields of the Shopping Calculator form, by clicking the
`Calculate` button you'll receive a form with every value
set with the same values you previously typed in.

It's now time to make the calculation and to set the result in the
`Total`.

### Evolve by refactoring the code


# Next step - TBD

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/weavejester/valip
[4]: https://github.com/cemerick
[5]: https://github.com/weavejester
[6]: https://github.com/cemerick/valip/blob/master/README.md
[7]: http:/localhost:3000/login-dbg.html
[8]: https://github.com/cemerick/valip/blob/master/README.md
[9]: http://dev.clojure.org/display/design/Feature+Expressions
[10]: https://github.com/emezeske/lein-cljsbuild
[11]: https://github.com/emezeske
[12]: https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/doc/CROSSOVERS.md
[13]: http://clojuredocs.org/clojure_core/clojure.core/if-let
[14]:https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md#first-try
[15]: https://github.com/emezeske/lein-cljsbuild
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[17]: http://clojuredocs.org/clojure_core/clojure.core/let#example_878
[18]: https://github.com/shoreleave
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/remote-validator.png
