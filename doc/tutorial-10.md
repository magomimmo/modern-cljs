# Tutorial 10 - Introducing AJAX

In the [previous tutorial][1] we were happy enough with the results we
achieved in terms of [separation of concerns][2], functional programming
style and elimination of any direct use of CLJS/JS interop.

As we said in [Tutorial 4][3], the very first reason why JS adoption
became so intense had to do with its ability to easily support
client-side validation of HTML forms. This is the same thing we did
from [Tutorial 4][3] through [Tutorial 9][1].

Then, thanks to the introduction of [XmlHttpRequest][4] and
the popularity of Gmail and Google Maps, the terms **[Ajax][31]** and **[Web 2.0][32]**
became the most used web buzzwords all over the place and in a
very short time too.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][30] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-09
git checkout -b tutorial-10-step-1
```

# Introduction

Ajax is not something magic; it's just a kind of client-server
communication, eventually asynchronous, mainly using http/https
protocols. Ajax exploits more [web techniques][5]:

* HTML and CSS for presentation
* DOM for dynamic display and interaction with data
* XML/JSON for the data representation
* XMLHttpRequest for asynchronous communication
* JavaScript to bring all the above techniques together

A sequence diagram visualizing the techniques above
applied to a form page example is shown below:

![Ajax Diagram][6]

As you can see, when a click event is raised, the corresponding event
handler calls the XHR object and registers a callback function, which
will be notified with the response. The XHR object creates the
http request and sends it to the web server which selects and calls the
appropriate server-side handler (i.e., application logic). The server-side
handler then returns the response to the XHR object which parses
the response and notifies the registered callback. The
callback function then manipulates the DOM and/or the CSS of the
page to be shown by the browser to the user.

# No Ajax, no party

If CLJS did not have a way to implement the Ajax interaction model, it would
be dead before it was born. As usual, CLJS could exploit the Google Closure
Library which includes the `goog.net` package to abstract the plain
`XmlHttpRequest` object for supporting Ajax communications with a server
from a web browser.

That said, directly interacting with `XmlHttpRequest` or with
`goog.net.XhrIo` and `goog.net.XhrManager` could easily become
cumbersome. Let's see if the CLJS community has done something to
alleviate a certain PITA.

# Introducing shoreleave

After a brief GitHub search, my collegues [Federico Boniardi][7] and
[Francesco Agozzino][8] found the [shoreleave-remote][9] and
[shoreleave-remote-ring][10] libraries.  They appreared to be
really promising in helping our Ajax experiments, as compared with
the [fetch][11] library, which was the one I was aware of but depends
on the [recently deprecated][13] [noir][12].

As you can see from its [readme][14], shoreleave is a collection of
integrated libraries that focuses on:

* Security
* Idiomatic interfaces
* Common client-side strategies
* HTML5 capabilities
* ClojureScript's advantages

And it builds upon efforts found in other ClojureScript projects, such
as [fetch][11] and [ClojureScriptOne][15]--standing on the shoulders of giants.

## KISS (Keep It Small and Stupid)

To keep things simple enough we're going to stay with our boring
[shopping calculator][20] form as a reference case to be implemented using
`shoreleave`.

What we'd like to do is move the calculation from the client-side code
(i.e., CLJS) to the server-side code (i.e., CLJ), then let the former
ask the latter to produce the result to be manipulated by CLJS.

The following sequence diagram visualizes our requirements.

![Shopping Ajax][16]

The first thing we want to do is implement the server-side function to
calculate the total.

## The server side

Thanks to [Chas Emerick][17], one of the most active and
fruitful Clojurists, we can exploit [shoreleave-remote-ring][10] by
defining a remote `calculate` function which will return a result
(i.e., `total`) based on the input (i.e., `quantity`, `price`, `tax`
and `discount`).

### Update dependencies

As usual we should first add the `shoreleave-remote-ring` library to
`project.clj`. That said, the current `shoreleave-remote-ring "0.3.0"`
release depends on `org.clojure/tools.reader "0.7.0"`,
which is not compatible with the latest
CLJS releases. To overcome this issue I upgraded all the `shoreleave`
libs used in the `modern-cljs` series. So, instead of adding the
canonical `shoreleave` libs you have to use the following forks:

```clj
(defproject ...
  ...
  :dependencies [...
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"]]
  ...)
```

> NOTE 1: We also added  the `shoreleave-remote` library to the
> project dependencies. This lib will be used later for the
> client-side code.

### defremote

The next step is to define the server-side function that implements the
calculation from the `quantity`, `price`, `tax` and `discount` inputs. The
`shoreleave-remote-ring` library offers the `defremote` macro which is
like `defn` plus adding the function to
a registry implemented as a reference type map (e.g., `(def remotes
(atom {}))`).

Create a new CLJ file named `remotes.clj` in the `src/clj/modern_cljs`
directory and write the following code:

```clojure
(ns modern-cljs.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))
```

As you can see we first declared the new `modern-cljs.remotes`
namespace and required the `shoreleave.middleware.rpc` namespace
referring the `defremote` macro.

Then we used `defremote` to define the `calculate` function.

> NOTE 2: If you compare this `calculate` function with the one
> originally defined in the `shopping.cljs` client code in the
> [previous tutorial][1], you should note that the call to the `.toFixed`
> CLJS function interop has been removed.

> NOTE 3: The namespace declaration now uses the `:require` form with
> the `:refer` specification, which I prefer to both
> the `(:use ... :only [...])` and `(:require ... :as ...)` specifications.

### Update the handler

When we introduced [Compojure][18] in [Tutorial 3][19] we defined a
handler which used the `site` wrapper to add a set of standard
*ring-middlewares* suitables for a regular web site. Here is the
content of `core.clj`

```clojure
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  ; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ; to server static pages saved in resources/public directory
  (resources "/")
  ; if page is not found
  (not-found "Page non found"))

;; site function create an handler suitable for a standard website,
;; adding a bunch of standard ring middleware to app-route:
(def handler
  (site app-routes))

```

The `shoreleave-remote-ring` library requires that you add the
`wrap-rpc` wrapper to the top-level handler in such a way that it will
be ready to receive Ajax calls. Open the `remotes.clj` file again,
add the required namespace dependencies, and define the new handler which
wraps the original one with `wrap-rpc`. Here is the complete
`remotes.clj` content.

```clojure
(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))
```

> NOTE 4: We required the `modern-cljs.core` and `compojure.handler`
> namespaces to refer `handler` and `site` symbols. Then we added
> `wrap-rpc` to the `:refer` specification of the already required
> `shoreleave.middleware.rpc` namespace.

The last thing to be done on the server-side is to update the `:ring`
task configuration in `project.clj` by replacing the
`modern-cljs.core/handler` handler with the new one (i.e., `app`).

```clj
(defproject ...
    ...
    :ring {:handler modern-cljs.remotes/app}
	...)
```

Great: the server-side is done. We now have to fix the client-side code,
which means the `shopping.cljs` file.

## The client side

Open the `shopping.cljs` file from the [previous tutorial][1].

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [hiccups.runtime :as hiccupsrt]
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
;;; rest of the code
```

First you need to update the namespace declaration by requiring the
`shoreleave.remotes.http-rpc` namespace to refer `remote-callback`.

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :as h])
  (:require [domina :as dom]
            [hiccups.runtime :as hiccupsrt]
            [domina.events :as ev]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]]))
```

> NOTE 5: We also added `cljs.reader` namespace to refer
> `read-string`. The reason will become clear when we fix the
> client-side `calculate` function.

Now, let's finish the work by modifying the `calculate` function.

```clojure
(defn calculate []
  (let [quantity (read-string (dom/value (dom/by-id "quantity")))
        price (read-string (dom/value (dom/by-id "price")))
        tax (read-string (dom/value (dom/by-id "tax")))
        discount (read-string (dom/value (dom/by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(dom/set-value! (dom/by-id "total") (.toFixed % 2)))))
```

### The arithmetic is not always the same

First, take a look at the `let` form. We wrapped the reading of all the
input field values inside a `read-string` form, which returns the JS
object coded by the given string. That's because CLJS has the same
arithmetic semantics as JS, which is different than
CLJ on the JVM. Try to launch the rhino repl from
`modern-cljs` home directory and evaluate a multiplication
and passing it two stringified numbers:

```clojure
lein trampoline cljsbuild repl-rhino
Running Rhino-based ClojureScript REPL.
"Type: " :cljs/quit " to quit"
ClojureScript:cljs.user> (* "6" "7")
WARNING: cljs.core/*, all arguments must be numbers, got [string string] instead. at line 1
42
ClojureScript:cljs.user>
```

As you can see, CLJS implicitly casts strings to numbers when applies
some arithmetic functions, but not all them. As an example try to add
two stringified numbers and then multiply the result by 2 (stringified
or not, it's the same).

```clojure
ClojureScript:cljs.user> (+ "1" "2")
WARNING: cljs.core/+, all arguments must be numbers, got [string string] instead. at line 2
"12"
ClojureScript:cljs.user> 1
1
ClojureScript:cljs.user> (* "2" (+ "1" "2"))
WARNING: cljs.core/+, all arguments must be numbers, got [string string] instead. at line 4
WARNING: cljs.core/*, all arguments must be numbers, got [string number] instead. at line 4
24
ClojureScript:cljs.user>
```

So, you have been warned. If you start a computation from a sum of
stringified numbers, you are asking for trouble, because you're starting
with string concatenation.

Now try the same thing in a regular CLJ repl:

```clojure
lein repl
nREPL server started on port 53127
REPL-y 0.1.4
Clojure 1.5.1
    Exit: Control+D or (exit) or (quit)
Commands: (user/help)
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
          (user/sourcery function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
Examples from clojuredocs.org: [clojuredocs or cdoc]
          (user/clojuredocs name-here)
          (user/clojuredocs "ns-here" "name-here")
user=> (* "6" "7")

ClassCastException java.lang.String cannot be cast to java.lang.Number  clojure.lang.Numbers.multiply (Numbers.java:146)
user=>
```

As you can see, CLJ throws `ClassCastException` because it
can't cast a `String` to a `Number`.

It should now be clear why we added `cljs.reader` to the
`modern-cljs.shopping` namespace declaration to refer to the CLJS
`read-string` function.

### The remote callback

Take another look at the `calculate` definition:

```clojure
(defn calculate []
  (let [quantity (read-string (dom/value (dom/by-id "quantity")))
        price (read-string (dom/value (dom/by-id "price")))
        tax (read-string (dom/value (dom/by-id "tax")))
        discount (read-string (dom/value (dom/by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(dom/set-value! (dom/by-id "total") (.toFixed % 2)))))
```

After having read the values from the input fields of the shopping
form, the client-site `calculate` function calls the `remote-callback` one,
which accepts:

* the keywordized remote function name (i.e., `:calculate`);
* a vector of arguments to be passed to the remote function
  (i.e., `[quantity price tax discount]`);
* an anonymous function which receives the result (i.e., `%`) from the
  remote calculation through the `remote-callback` call, then formats
  the result (i.e., `.toFixed`) and finally manipulates the DOM by
  setting the value of the `total` input field (i.e., `set-value!`) to
  the formatted one.

# Play&Pray

Now cross your fingers and do as follows:

* clean any previous compilation from `modern-cljs` directory;
* compile the `dev` build;
* run the ring server.

```bash
cd /path/to/modern-cljs
lein do cljsbuild clean, cljsbuild once, ring server-headless
lein trampoline cljsbuild repl-listen # optional - in a new terminal
```

> NOTE 6: As you can see above, we started using the `do` chaining
> feature of the `lein` command to minimize our typing.

Now visit [shopping-dbg.html][20], click the `Calculate` button and
verify that the shopping calculator returns the expected `total`
value.

Congratulations! You implemented a very simple, yet
representative Ajax web application, by using CLJS on the client-side
and CLJ on the server-side.

# Make you a favor

> NOTE 7: This paragraph has been written using a previous
> version of `shoreleave` libs. *Mutatis Mutandis* (e.g., `_shoreleave`
> instead of `_fetch`), everything should be almost the same when
> using the latest available version of `shoreleave`.

Let's finally verify our running Ajax application by using the browser
development tools. I'm using Google Chrome Canary, but you can choose
any browser that provides development tools comparable
with the ones available in Google Chrome Canary.

* If you have stopped the running ring server from the previous
  paragraph, just run it again as explained above;
* Open the development tools (i.e. `Tools->Developer->Developer
  Tools`);
* Select the `Network` pane;
* Visit [shopping-dbg.html][20] page or reload it.

Your browser should look like the following image.

![network-01][21]

Click the `Calculate` button. If you focus on the
`Network` pane, you should now see something similar to the following
image.

![network-02][22]

Now click `_fetch` from the `Name/Path` column. If the `Header`
subpane is not already selected, select it and scroll until you can see
the `Form Data` area. You should now see the following view which
reports `calculate` as the value of the `remote` key, and `[1 1 0 0]`
as the value of `params`.

![network-03][23]

Now select the `Response` subpane. You should see the returned value
from the remote `calculate` function like in the following image.

![network-04][24]

That's it folks.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "introducing ajax"
```

# Next step [Tutorial 11: A Deeper Understanding of Domina Events][25]

In the next tutorial we're going to apply what we just learned and extend its
application to the login form introduced in [Tutorial 4][3].

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[2]: http://en.wikipedia.org/wiki/Separation_of_concerns#HTML.2C_CSS.2C_JavaScript
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[4]: http://en.wikipedia.org/wiki/XMLHttpRequest
[5]: http://en.wikipedia.org/wiki/Ajax_(programming)#Technologies
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ajax.png
[7]: https://github.com/federico-b
[8]: https://github.com/agofilo
[9]: https://github.com/shoreleave/shoreleave-remote
[10]: https://github.com/shoreleave/shoreleave-remote-ring
[11]: https://github.com/ibdknox/fetch
[12]: https://github.com/ibdknox/noir
[13]: https://groups.google.com/forum/#!msg/clj-noir/AbAvQuikjGk/x8lKLKoomM0J
[14]: https://github.com/shoreleave/shoreleave-remote#shoreleave
[15]: https://github.com/brentonashworth/one
[16]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-ajax.png
[17]: https://github.com/cemerick
[18]: https://github.com/weavejester/compojure
[19]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[20]: http://localhost:3000/shopping-dbg.html
[21]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-01.png
[22]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-02.png
[23]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-03.png
[24]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-04.png
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md
[26]: https://github.com/levand/domina
[27]: https://github.com/levand/domina/blob/master/src/cljs/domina.cljs#L125
[28]: https://github.com/levand/domina/pull/43
[29]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md
[30]: https://help.github.com/articles/set-up-git
[31]: http://en.wikipedia.org/wiki/Ajax_%28programming%29
[32]: http://en.wikipedia.org/wiki/Web_2.0
