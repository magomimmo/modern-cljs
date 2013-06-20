# Tutorial 14 - It's better to be safe than sorry (part 1)

In this tutorial we're going to prepare the field for one of the main
topic in software development and methodologies: code testing.

Code testing is a kind of continuum which goes from zero to almost full
test coverage. I'm not going to open those kind of discussions which
have a never ending story and go nowhere. Code test is needed, full
stop. How much coverage? It depends.

I started programming almost 30 years ago and I had the luckiness of
starting with Prolog and then on a wonderful Lisp Machine, which I still
miss a lot today. I never started a program from a test that has to fail
to progress until it succeeds. That's because I had my Lisp REPL to
follow my thoughs and to correct them on the fly. When you're aged like
I'm, you can't change your habits. So, who's religious about TDD/BDD
(Test Driven Development and Behavioural Driven Development), has to be
forgiven with me.

## Introduction

In the last [tutorial][1] I hope you had some fun in seeing the DRY
principle at work while adhering to the progressive enhancement
strategy. Before to go ahead by affording the problem of testing your
CLJ/CLJS code, we have to fullfill something we left behind. In the
[tutorial 10 - Introducing Ajax][2] we implemented a Shopping Calculator
by using the Ajax style of communication between the browser and the
server, said otherwise, between ClojureScript on the client side and
Clojure on the server side. Obviously, nobody will never implement that
kind of widget by using Ajax, if all the information he needs to make
the calculation are already in his hands on the browser side. By moving
the calculation from the client side to the server side, we found an
excuse to gently introduce you to Ajax. After all this is a series of
tutorial on CLJS not on CLJ, but we still have broken the first
principle of the progressive enhancement strategy which dictates to
start developing your web application by implementing it as if
JavaScript would not be available on the browser of the users. That
said, the main advantage of CLJ and CLJS is that you have an unified
language for both sides of the world, and this language is, in my humble
opinion, much better and fun than the JS/Node.js pair. So, we need to
fill the gap, even if this means to move ourself from CLJS to CLJ for a
while.

## Review the Shopping Calculator

To review the Shopping Calculator program do as follows:

```bash
$ git clone https://github.com/magomimmo/modern-cljs.git
$ cd modern-cljs
$ git checkout tutorial-13
$ lein cljsbuild auto prod
$ lein ring server-headless # in a new terminal from modern-cljs dir
```

Now visit the [shopping.html][3] page, and click the `Calculate`
button. The `Total` field is updated with the result of the calculation
executed via Ajax on the server-side. As you can see the `shopping.html`
page showed in the address bar of the browser does not change. Only the
result of the `Total` field has been updated, even if the calculation
has been executed by the server.

As you already know from the [Tutorial 10 - Introducing Ajax][2], by
opening the `Developer Tools` panel of your browser (I'm using Google
Chrome) and by taking a look at the Network tab after having reload the
[shopping.html][3] page, you should see the network activities. Any time
you click the `Calculate` button a new `_shoreleave` POST method request
is submited to the server which responds with the HTTP/1.1 202 status
code (i.e. Accepted).

![AjaxNetwork][5]

But what happens if you disable JavaScript? Try it. To disable the
JavaScript engine on Google Chrome, click on the setting's icon
positioned in the very right bottom of the `Developer tool`. It opens a
panel from where you can mark the `Disable JavaScript` check-box.

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
               id="calc">
      </div>
    </fieldset>
  </form>
  <script src="js/modern.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

As you can see the `form` tag has no `action` and `method` setted and
the `type` attribute of the Calculate `input` is `button`, which means
that when JavaScript is disabled the `form` does not respond to any
event.

## Break the Shopping Calculator

Now modify the `form` by adding the `action="/shopping"` and the
`method="post"` attributes. Then change the Calculate `input type` from `type="button"` to `type="submit"` as
follows.

```html
...
  <form action="/shopping" id="shoppingForm" method="post" novalidate>
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
  <script src="js/modern.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

Reload the [shopping.html][3] page and click the `Calculate` button
again. You should receive a `Page not found` message from the server and
see the `localhost:3000/shopping` URL in the location bar of your
browser instead of the previous `localhost:3000/shopping.html` URL.

As you remember from the [Tutorial 3 - CLJ based http-server][4], we
defined the application routes as follows

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
does not know anything about the `/shopping` route we just added as a
value of the `action` of the Shopping Calculator form.

Furthermore, by having setted the `method` attribute of the `form` to
`post` and the `type` attribute of the Calculate `input` to `submit`, we
asked the browser to send to the server a POST request with the
`/shopping` URI whenever the user clicks the Calculate button.

## A kind of TDD

By modifying the `shopping.html` and disabling the JavaScript from the
browser, we have just exercized a kind of TDD (Test Driver
Development). So the first step is to fix the problem we met by adding a
fictious route to the [compojure][7] `defroutes` macro.

Open the `src/clj/modern_cljs/core.clj` file and add a POST route for
the `"/shopping"` URI as follows.

```clojure
(defroutes app-routes
  ...
  (POST "/shopping" [quantity price tax discount]
        (str "You enter: "quantity " " price " " tax " and " discount "."))
  ...
)
```

Now reload the `shopping.html` page and click again the `Calculate`
button. Ypou should receive a message with the values of the input of
the Shopping Calculator form.

![FictionShopping][8]

Let's know rollback for a while and see what happens if we enable again
the JavaScript engine of the browser. Open again the Setting of the
Developer Tools and enable the JavaScript engine by unmarking the
Disable JavaScript check-box.

Then reload the [shopping.html][3] URL and finally click the `Calculate`
button again.

![FictionShopping2][9]

Ops, it seems that the Ajax version of the Shopping Calculator does not
work anymore as expected. What's happening?

## FIAT - Fix It Again Tony

By having changed the `Calculate` input from `type="button"` to
`type="submit"` now when the user click it, the control passes for
default to the `action="/shopping"` URI of the `method="post"` associated
to the Shopping form.

We already afforded this problem for the `login` context in a
[previuos tutorial][10] and solved it just by preventing the click event
to be passes to the default, which is the
`action="/shopping"`/`method="post"` pair associated to the form. So,
just code the same thing in the
`src/cljs/modern_cljs/shopping.cljs`. Open the `shopping.cljs` file and
modify the function associated to the `click` event as follows.

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
```

As you can see we have to wrap in an anounymous funtion the original
`calcualte` function, which now receive the event as an argument.

Now we need to modify the `calculate` function definition to be able to
prevenet the `click` event to be passed on to the `action` of the
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

As you can see, after having called the `remote-callback` we added the
call to the `prevent-default` function, which is defined in the
`domina.events` namespace. The last modification we have to introduce is
to add the `prevent-default` function in the `:refer` section of the
`domina.events` requirement in the `modern-cljs.shopping` namespace as
follows.

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]]))
```

If you did not stop the `cljsbuild` auto compilation of the `dev` build
from the previous build

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
