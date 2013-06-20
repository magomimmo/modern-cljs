# Tutorial 14 - It's better to be safe than sorry (Part 1)

In this tutorial we're going to prepare the field for one of the main
topic in software development methodologies: code testing.

Code testing is a kind of continuum which goes from zero to almost full
test coverage. I'm not going to open those kind of discussions which
have a never ending story and go nowhere. Code testing is a need, full
stop. How much coverage? It depends.

I started programming almost 30 years ago and I had the luckiness of
starting with Prolog and then on a wonderful Lisp Machine, which I
still miss a lot today. I never started a program from a test that has
to fail to progress until it succeeds. That's because I had my Lisp
REPL to follow my thoughs and to correct them on the fly. Nowaday, by
using CLJ/CLJS, with immutability included, unit tests are very easy
to be implemented because most of the time you have to do with pure
functions, whose outputs depends only by the passed inputs. That said,
when you're aged like I'm, you can't change your habits. So, who's
religious about TDD/BDD (Test Driven Development and Behavioural
Driven Development), has to be forgiven with me.

## Introduction

In the last [tutorial][1] I hope you had some fun in seeing the DRY
principle at work, while adhering to the progressive enhancement
strategy. Before to go ahead by affording the problem of testing your
CLJ/CLJS code, we have to fullfill something we left behind. In the
[tutorial 10 - Introducing Ajax][2] we implemented a Shopping
Calculator by using the Ajax style of communication between the
browser and the server: said otherwise, between ClojureScript on the
client side and Clojure on the server side. Obviously, nobody will
never implement that kind of stupid widget by using Ajax, if all the
information he needs to make the calculation are already in his hands
on the browser side. By moving the calculation from the client side to
the server side, we found an excuse to gently introduce you to
Ajax. After all this is a series of tutorial on CLJS not on CLJ, but
by doing this, we have broken the first principle of the progressive
enhancement strategy, which dictates to start developing your web
application by implementing it as if JavaScript would not be available
on the browser of the users. 

The main advantage of CLJ and CLJS is that you have an unified
language for both sides of the world, and this language is, in my
humble opinion, much better and fun than the JS/Node.js pair. So, we
need to fill the gap, even if this means to move ourself from CLJS to
CLJ for a while.

## Review the Shopping Calculator

Let's start by reviewing the Shopping Calculator program. Do as follows:

```bash
$ git clone https://github.com/magomimmo/modern-cljs.git
$ cd modern-cljs
$ git checkout tutorial-13
$ git checkout -b tutorial-14-step-1  # to clone the branch
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

But what happens if you disable JavaScript? Disable the JavaScript
engine on Google Chrome by clicking on the Setting's icon positioned
in the very right bottom of the `Developer tool` window. It opens a
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

As you can see the `form` tag has no `action` and `method` setted and
the `type` attribute of the Calculate `input` is setted to `"button"`
value, which means that when JavaScript is disabled and the `form`
does not respond to any event.

## Step 1 - Break the Shopping Calculator

Now modify the `form` by adding the `action="/shopping"` and the
`method="post"` attributes/values. Next change the Calculate `input
type` from `type="button"` to `type="submit"`. Following is the
interested snippet of the modified HTML code.

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
  ...
  ...
</html>
```

Reload the [shopping.html][3] page and click the `Calculate` button
again. You should receive a `Page not found` message from the server
and see the `localhost:3000/shopping` URL in the address bar of your
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

which explains us why we received the `Page not found` page: our
server does not know anything about the `/shopping` URL we are now
passing as the value of the `action` of the Shopping Calculator form.

Furthermore, by having setted the `method` attribute of the `form` to
`post` and the `type` attribute of the Calculate `input` to `submit`, we
asked the browser to send to the server a POST request with the
`"/shopping"` URL whenever the user clicks the Calculate button.

### A kind of TDD

By modifying the `shopping.html` and disabling the JavaScript from the
browser, we have just exercized a kind of TDD (Test Driver
Development). So the first step is to fix the problem we just met by
adding a fictious route to the [compojure][7] `defroutes` macro.

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

Now reload the `shopping.html` page and click again the `Calculate`
button. You should now receive a message with the values of the input
fields of the Shopping Calculator form.

![FictionShopping][8]

Let's know go back for a while and see what happens if we enable again
the JavaScript engine of the browser: open again the Setting of the
Developer Tools and enable the JavaScript engine by unmarking the
Disable JavaScript check-box.

Next reload the [shopping.html][3] URL and finally click the `Calculate`
button again.

![FictionShopping2][9]

Ops, it seems that the Ajax version of the Shopping Calculator does not
work anymore as expected. What's happening?

### Fix the failed virtual test

By having changed the `Calculate` input from `type="button"` to
`type="submit"`, when now the user clicks it, the control passes to
the `action="/shopping"` which is called as a POST request to the server.

We already afforded this problem in a [previuos tutorial][10]
dedicated to the `login` example and we solved it by preventing the
click event to be passes to the action associated to the form.

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

If you had not stop the `cljsbuild` auto compilation from the previuos
run you should see the CLJS/Google Closure Compiler running again to
produce the optimized `modern.js` script file.

Reload the [shopping.html][3] URL. You should now see the Ajax version
of the Shopping Calculator working again as expected.

Not so bad until now. I suggest you to commit now your work by issuing
the following `git` command:

```bash $ git commit -am "Step 1" $ git checkout -b tutorial-14-step-2
``` The last `git` command is to clone the step-1 in the step-2 branch
and set the last as the active branch in preparation of the next work.

## Step 2 - Enliving the server-side

In the Step 1 of this tutorial we prepared the field for introducing
[Enlive][11], one of the most famous CLJ libs in the clojurean
community. There are already
[few Enlive tutorials availbale online][12] and I'm not going to
compete with who is more knowledgeable than me on this topic. In this
step of the tutorial I'm only going to introduce the few things that
will allow us to implement a very simple server-side only Shopping
Calculator in accordance with the progressive enhamvement principle.

The reasons why I choose Enlive are very well motivated by
[David Nolen][13] in its [bestseller tutorial][14] on Enlive:

> Enlive gives you the advantages of designer accessible templates
> (since they’re just HTML) without losing the power of function
> composition. As a result, your designer can create all the various
> widgets for your website using only HTML and CSS and you can compose
> your pages from any combination of their designs.

This is similar to [Domina][15] separation of concern which allows the
designer to play its role and the programmer too.

Our needs are very easy to be described. We have to:

* Read a pure HTML page which represents the Shopping Calculator 
* Extract from the HTTP request the parameters typed-in by the user in
the Shopping form
* make the calculation of the total 
* update the total field with the calcultated result
* show the updated Shopping Calculator to the user

The following picture shows a sequence diagram of the above descriptio.

![Shopping-server][16]

Obiouvsly we have to validate all inputs, but this is something we'll
take care of later.




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
