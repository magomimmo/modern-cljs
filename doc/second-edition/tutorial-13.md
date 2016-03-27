# Tutorial 13 - Better Safe Than Sorry (Part 1)

In this part of the tutorial we're going to prepare the field for one
of the main topics in the software development life cycle: *code
testing*. Code testing is a kind of continuum which goes from zero to
almost full test coverage. I'm not going to open one of those endless
discussions about the right amount of testing. Code testing
is a need, full stop. How much coverage? It depends.

I have to admit that I never took a program from a unit test that has
to initially fail before I could proceed to success. When you are as
old as I am, you can't change your habits. So, whoever is religious
about TDD/BDD (Test Driven Development and Behavioural Driven
Development) must forgive me.

Nowadays, by using functional programming languages like CLJ/CLJS, the
unit tests are much easier to implement, as compared with
imperative and object-oriented programming languages, because most of
the time you have to deal with pure functions whose output depends
only on the passed input.

## Preamble

To start working from the end of the previous tutorial, assuming
you've `git` installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-12
```

## Introduction

Before we go ahead with the problem of testing your CLJ/CLJS
code, we have to finish something we left behind in the previous
tutorials. In the [Tutorial 9 - Introducing Ajax - ][1] we
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

## Add Enlive

In this tutorial we're going to use [Enlive][9] by
[Christophe Grand][27], one of the most famous CLJ libs in the Clojure
communities. Even if it will take few paragraphs before you'll see it
in action, we add it to the project before starting the IFDE.

Open the `build.boot` file and add `enlive` to the dependencies
section as usual with any lib:

```clj
(set-env!
 ...
 :dependencies '[..
                 [enlive "1.1.6"]
                 ])
...
```

## Review the Shopping Calculator

> ATTENTION NOTE: all the figures of this tutorials have been taken a
> couple of years ago with old versions of Chrome/Canary that are now
> outdated. That said, Mutatis Mutandis, you should be able to follow
> the tutorial without any problem.

Start the IFDE as usual

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 19.288 sec
```

and visit the [shopping URI][2] for reviewing the Shopping Calculator
program.

Click the `Calculate` button. The `Total` field is valued with the
result of the calculation executed via Ajax on the server-side.

Note that the `localhost:3000/shopping.html` URI shown in the address
bar of the browser does not change when you click the `Calculate`
button. Only the value of the `Total` field will be updated, even if the
calculation was executed by the server. That's one of the beauties of
Ajax.

As you already know from the [Tutorial 9 - Introducing Ajax - ][1] you
can open the `Developer Tools` panel of your browser (I'm using Google
Chrome) and take a look at the Network tab after having reloaded the
[shopping URI][2].

Any time you click the `Calculate` button a new `_shoreleave` POST
asynchronous method request is submitted to the server which responds
with the HTTP/1.1 202 status code (i.e. Accepted).

![AjaxNetwork][3]

But what happens if you disable JavaScript? Let's try.

[Disable JavaScript][33] and then reload the [shopping URI][2]. If you
move the mouse cursor over the `Calculate` button nothing happens and
nothing happens even if you click it.

Take a look at the `shopping.html` file which is under the `html`
directory of the `modern-cljs` main directory.

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

In the [Tutorial 9 - Introducing Ajax][5] we used [Compojure][6] to
define the application's routes as follows:

```clj
(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (POST "/login" [email password] (authenticate-user email password))
  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))
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
from the browser, we have just exercised a kind of TDD (Test Driven
Development) approach.

To fix the failure we just met, we need to add a route for the
"/shopping" request to the `defroutes` macro call.

Open the `src/clj/modern_cljs/core.clj` file and add the "/shopping"
POST route:

```clj
(defroutes handler
  ...
  (POST "/shopping" [quantity price tax discount]
        (str "You enter: "quantity " " price " " tax " and " discount "."))
  ...)
```

> NOTE 1: In the Restful communities, which I respect a lot, that
> would be a blasphemy, because the Shopping Calculator is an
> application resource which, in Restful parlance, is safe and
> idempotent and we should have used the default GET verb/method.

> NOTE 2: we also extract the values of the input parameters of the
> `shoppingForm` by passing the args vector
> `[quantity price tax discount]` to the POST call.

Now click again the `Calculate` button. You should receive a plain
text of the input values of the form.

![FictionShopping][7]

Let's know go back for a while and see what happens if we re-enable the
JavaScript engine of the browser. Open again the Setting of the
Developer Tools and [enable the JavaScript engine][33] by unmarking the
`Disable JavaScript` check-box.

Next, reload the [shopping URI][2] and finally click the `Calculate`
button again.

![FictionShopping2][7]

Ops, it seems that the Ajax version of the Shopping Calculator does not
work anymore. What happened?

### Fix the failed test

Now that we have changed the `Calculate` input from `type="button"` to
`type="submit"`, when the user clicks it, the control passes to the
`action="/shopping"` and submits a POST request to the server. The
server then responds by calling the handler function which is now
associated with the POST "/shopping" route.

We already dealt with this problem in a [previous tutorial][8]
dedicated to the `login` example. We solved it by preventing the
above from happening.

We need to code the same thing in the
`src/cljs/modern_cljs/shopping.cljs` file.

Open the `shopping.cljs` file and modify the function associated with the
`click` event as follows.

```clj
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") 
             :click 
             (fn [evt] (calculate evt)))
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

We wrapped the `calculate` function inside an anonymous function,
which now receives an event as the sole argument.

Now we need to modify the `calculate` function definition to prevent
the `click` event from being passed to the `action` of the Shopping
form.

```clj
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

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append! 
                                 by-class
                                 by-id 
                                 destroy! 
                                 set-value! 
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))
```

As soon as you save the file everything get recompiled. As you
remember, when we modify the exported `init` function we have to
reload the corresponding page which call it from the script tag.

Reload the [shopping URI][2]. You should now see the Ajax version of
the Shopping Calculator working again as expected.

Not bad so far.

## Step 2 - Enliving the server-side

In the previous pragraphs of this tutorial we prepared the stage for
introducing [Enlive][9].

There are already few [Enlive tutorials][10] available online and I'm
not going to add anything beyond the simplest use case to allow us to
implement the server-side only Shopping Calculator in accordance with
the progressive enhancement principle.

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
1. read the parameters typed in by the user from the submitted HTTP
   request;
1. parse the extracted values and calculate the total;
1. update the fields in the HTML form; and
1. send the resulted page to the user.

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
`shopping.html` file from its directory and the step `4.` - update the
input fields of the form.

[Enlive][9] offers a single macro, `deftemplate`, which allows us to
resolve both `1.` and `4.` in a single shot.

`deftemplate` accepts 4 arguments:

* `name`
* `source`
* `args`
* `& forms`.

It implicitly creates a function with the same number of `args` and
the same `name` as the template. The `source` argument can be any HTML
file located in the `classpath` of the application.

Finally, the `&forms` argument is composed of a sequence of pairs. The
left hand of the pair is a vector of CSS-like selectors, used to
select the interested elements/nodes from the parsed HTML source. The
right hand of the pair is a function which is applied to transform
each selected element/node.

As you perhaps remember from the tutorial introducing Ajax, in the
`build.boot` building file of the project we passed the `"target"`
directory as the value of the `:resource-root` option for the `serve`
task.

```clj
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :handler 'modern-cljs.remotes/app            ;; ring hanlder
          :resource-root "target"                      ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

This means that we can pass the `shopping.html` file to
`deftemplate` as the `source` argument.

As the `name` argument, we're going to use the same name of the POST
route (i.e. `shopping`) previously defined inside the `defroutes`
macro.

Then, the `args` to be passed to `deftemplate` are the same we defined
in the `"/shopping"` POST route: `[quantity price tax discount]`.

Finally, regarding the `& forms` arguments, start by instantiating it
with two `nil` values, which means no selectors and no
transformations. We should now expect that the source will be rendered
exactly as the original HTML source.

## Let's code

We have to decide where to create the CLJ file containing the template
definition for the Shopping Calculator page. I prefer to maintain a
directory structure which mimics the logical structure of an
application. So I decided to create a new `templates` directory under
the `src/clj/modern_cljs/` directory.

```bash
# in a new terminal
cd /path/to/modern-cljs
mkdir src/clj/modern_cljs/templates
```

Inside this directory create now the `shopping.clj` file where we'll
create the `deftemplate` macro call.

```bash
touch src/clj/modern_cljs/templates/shopping.clj
```

Following is the content of the newly created `shopping.clj` file to
define the Shopping Calculator template

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate]]))

(deftemplate shopping "shopping.html"
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

Now [disable the JavaScript engine][33] of your browser again and visit the
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

> NOTE 3: I strongly suggest you to read the enlive
> [syntax for selector][20] at least to have a decent understanding of
> it.

But what about the transformation functions? [Enlive][9] offers a lot
of them but this is not a tutorial on [Enlive][9], and I'm going to
use the only function we need in our context: the `(set-attr &kvs)`
function.  It accepts keyword/value pairs where the keywords are the
names of the attributes you want to set. In our sample, the only
attribute we are going to set is the `value` attribute of each `input`
field. So let's start by adding to the `deftemplate` call both the
selector clause and the trasformation function for each input field as
follows:

```clojure
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate set-attr]]))
  
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

In the [Tutorial 9 - Introducing Ajax][1] we defined the remote
`calculate` function by calling the `defremote` macro. The
`defremote` call implicitly define a function with the same name
of the remote function and that's good, because we hate any kind of
code duplication. We could immediately use it to calculate the result
of the Shopping Calculator by just parsing the
`[quantity price tax discount]` passed to the `deftemplate` call.

But wait a minute. We [already parsed those arguments][21] on the CLJS
side of the `calculate` function and we don't want to parse them
again. To reach this DRY objective we need to refactor the code by
moving the parsing code of the fields values from the client side to
the server side.

Let's take a look at the CLJS `shopping.cljs` file where we defined
the client side `calculate` function.

```clj
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
function from the `cljs.reader` lib of CLJS.

> ATTENTION NOTE: parsing a string coming from user input with
> `read-string` is
> [very dangerous](http://stackoverflow.com/questions/2640169/whats-the-easiest-way-to-parse-numbers-in-clojure)
> from a security point of view. Here where are not taking care of
> this issue, but you should. One possible solution is to use regular
> expressions.

Let's now refactor the `calculate` functions we defined in both CLJS
and CLJ source files. Open the `shopping.cljs` file under the
`src/cljs/modern_cljs` directory and modify it by removing the
`cljs.reader` from the namespace requirements and by removing the
calls to `read-string` in the `calculate` function definition as
follows:

```clj
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina.core :refer [by-id value by-class set-value! append! destroy!]]
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
arguments and we should refactor it as well. Open the `remote.clj`
file under the `src/clj/modern_cljs` directory and modify the
`calculate` function as follows:

```clj
(defremote calculate [quantity price tax discount]
  (-> (* (read-string quantity) (read-string price))
      (* (+ 1 (/ (read-string tax) 100)))
      (- (read-string discount))))
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

> NOTE 4: We added the `format` call to format the `Total` value with
> two digits after the decimal point. Note that we [casted][32] the
> `calculate` result to `double`.

Assuming that you have your IFDE running, as soon as you save the file
you'll receive an error.

### FIAT - Fix It Again Tony

Too bad. We just met a cyclic namespaces dependency problem. Cyclic namespace
dependencies are not allowed in CLJ so you need to refactor the code.

The `modern-cljs.templates.shopping` namespace now requires the
`modern-cljs.remotes` namespace to access the `calculate` remote
function. In turn, the `modern-cljs.remotes` namespace requires the
`modern-cljs.core` namespace to access the `handler` function. In
turn, the `modern-cljs.core` namespace requires the
`modern.cljs.templates.shopping` namespace to access the `shopping`
function implicitly defined by the `deftemplate` macro call.

```
modern-cljs.templates.shopping -> modern-cljs.remotes
-> modern-cljs.core -> modern-cljs.templates.shopping
```

Our scenario is simple enough. Remove the `modern-cljs.core` reference
from the `modern-cljs.remotes` namespace declaration. There, we only
referenced the `handler` symbol from the `modern-cljs.core` namespace
in the `app` definition. By moving the `app` definition to the
`modern-cljs.core` namespace we should be able to resolve the cyclic
issue.

We just met another case in which it is easier to stop the IFDE than
altering its runtime environment. This is because we now have to
substitute the `:handler` value in the `serve` task of the
`build.boot` from `modern-cljs.remotes/app` to `modern-cljs.core/app`.

So, stop any `boot` related process and modify both the `remotes.clj`
and the `build.boot` files.

Following is the modified content of the `remotes.clj` file where we
have removed both the reference to the `modern-cljs.core` namespace
and the `app` symbol definition.

```clj
(ns modern-cljs.remotes 
  (:require [modern-cljs.login.validators :as v]
            [shoreleave.middleware.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (-> (* (read-string quantity) (read-string price))
      (* (+ 1 (/ (read-string tax) 100)))
      (- (read-string discount))))

(defremote email-domain-errors [email]
  (v/email-domain-errors email))
```

> NOTE 5: We also removed the references to the `wrap-rpc` symbol and
> to `site`from because they are not used anymore by any functions
> defined in the file.

Next, we need to add the `app` symbol definition in the
`modern-cljs.core` namespace and add both the
`shoreleave.middleware.rpc` and `compojure.handler` requirements to be
able to reference `wrap-rpc` and `site` symbols in the `app`
definition. Following is the modified content of the `core.clj` file.

```clj
(ns modern-cljs.core 
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found files resources]]
            [compojure.handler :refer [site]]
            [modern-cljs.login :refer [authenticate-user]]
            [modern-cljs.templates.shopping :refer [shopping]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]))

(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (POST "/login" [email password] (authenticate-user email password))
  (POST "/shopping" [quantity price tax discount]
        (shopping quantity price tax discount))
  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))         ;; page not found

(def app
  (-> (var handler)
      (wrap-rpc)
      (site)))
```

Last, but not least, we have to modify the `build.boot` file to update
the namespace of the `app` symbol in the `:handler` section of the
`serve` task.

```clj
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :handler 'modern-cljs.core/app               ;; new ring handler
          :resource-root "target"                      ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

We are now ready to rebuild and run everything.

Start the IFDE

```bash
boot dev
...
Elapsed time: 19.405 sec
```

visit [shopping][2] URI and play with the form by enabling and
disabling the JavaScript engine of your browser. Everything should
work as expected in both the scenarios.

When you're done, kill the `boot` process and reset your git
repository.

```bash
git reset --hard
```

# Next Step - [Tutorial 14: It's better to be safe than sorry (Part 2)][28]

In the [next tutorial][28], after having added the validators for the
`shoppingForm`, we're going to introduce unit testing.

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
[2]: http://localhost:3000/shopping.html
[3]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/networkActivities01.png
[4]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/DisableJavaScript.png
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
[6]: https://github.com/weavejester/compojure
[7]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/fictionShopping.png
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-10.md#prevent-the-default
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
[21]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md#the-client-side
[22]: http://dev.clojure.org/display/design/Feature+Expressions
[23]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md#cross-the-border
[24]: https://github.com/cemerick/valip
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md#the-selection-process
[26]: https://github.com/cemerick/valip/blob/master/src/valip/predicates.clj
[27]: https://github.com/cgrand
[28]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md
[29]: https://github.com/emezeske/lein-cljsbuild
[30]: https://github.com/emezeske/lein-cljsbuild#hooks
[31]: https://github.com/technomancy/leiningen
[32]: http://clojuredocs.org/clojure_core/clojure.core/format#example_839
[33]: https://github.com/learningcljs/modern-cljs/blob/se-js-enable-disable/doc/supplemental-material/enable-disable-js.md
