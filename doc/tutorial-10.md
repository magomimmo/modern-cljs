# Tutorial 10 - Introducing Ajax

In the [last tutorial][1] we were happy enough with the reached results
in terms of [separation of concerns][2], functional programming style
and elimination of any direct use of CLJS/JS interop.

As we said in [Tutorial 4][3], the very first reason why JS adoption
became so intense had to do with its ability to easily support
client-side validation of HTML forms. The same thing we did from
[Tutorial 4][3] to [Tutorial 9][1].

Then, thanks to the introduction of [XmlHttpRequest][4] JS obejct and
the wide spread of gmail and google maps, the terms **ajax** and **web
2.0** became the most used web buzzwords all over the places and in a
very short time too.

# Introduction

Ajax is not something magic, it's just a kind of client-server
communication, eventually asyncronous, mainly using http/htts
protocols. Ajax expoits more [web techniques][5]:

* HTML and CSS for presentation
* DOM for dynamic display and interaction with data
* XML/JSON for the data representation
* XMLHttpRequest object for aynchronous communication
* JavaScript to bring all the above techniques together

A sequence diagram which visualizes the above techniques interaction
applied to a form page example is shown in the next picture.

![Ajax Diagram][6]

As you can see, when a click event is raised, the corresponding event
handler calls the XHR object and registers a callback function, which
will be later notified with the response. The XHR object creates the
http request and sends it to the web server which selects and calls the
appropriate server side handler (i.e. application logic). The server
side handler then returns the response to the XHR object which parses
the response and notifies the result to the registered callback. The
callback function then will manipulate the DOM and/or the CSS of the
page to be shown by the browser to the user.

# No Ajax, no party

If CLJS did not have a way to implement ajax interaction model, it would
be dead before to be born. As usual, CLJS could exploit Google Closure
Library which includes the `goog.net` package to abstract the plain
`XmlHttpRequest` object for supporting ajax communications with a server
from a web browser.

That said, directly interacting with `XmlHttpRequest` or with
`goog.net.XhrIo` and `goog.net.XhrManager` objects could easly become an
hard work to be done. Let's see if CLJS community has done something to
alleviate a sure PITA.

# Introducing shoreleave

After a brief github search, my collegues [Federico Boliardi][7] and
[Francesco Agozzino][8] found [shoreleave-remote][9] and
[shoreleave-remote-ring][10] libraries.  They seem immediatly to be
really promising in helping our ajax experiments if compared with
[fetch][11] library, which was the one I was aware of and that depends
on [noir][12] that has been recently [deprecated][13].

As you can read from its [readme][14], Shoreleave is a collection of
integrated libraries that focuses on

* Security
* Idiomatic interfaces
* Common client-side strategies
* HTML5 capabilities
* ClojureScript's advantages

and it builds upon efforts found in other ClojureScript projects, such
as [fetch][11] and [ClojureScriptOne][15] which is like to say that it stands
on the shoulders of giants.

## KISS (Keep It Small and Stupid)

To keep things simple enough we're going to stay with our boring
[shopping calculator][20] form as a reference case to be implemented by using
shoreleave.

What we'd like to do is to move the calculation from the client side code
(i.e. CLJS) to the server side code (i.e. CLJ) and then let the former
asks the latter to produce the result to be then manipulated by CLJS.

The following sequence diagram visualizes our requirements.

![Shopping Ajax][16]

The first thing we want to do it's to implement the remote function to
calculate the total.

## The server side

Thanks to [Chas Emerick][17], which is one of the most active and
fruitful clojurist, we can exploit [shoreleave-remote-ring][10] by
definig a remote calculate function which will return out the result
(i.e. `total`) from the passed input (i.e. `quantity`, `price`, `tax`
and `discount`).

### Update dependencies

As usual we have first to add `shoreleave-remote-ring` library to
`project.clj`. Here is the corresponging code fragment.

```clojure

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [domina "1.0.0"]
                 [hiccups "0.1.1"]
                 [com.cemerick/shoreleave-remote-ring "0.0.2"]]
```

### defremote

The next step is to define the remote function that implements the
calculation from `quantity`, `price`, `tax` and `discount` input. The
`shoreleave-remote-ring` library offers `defremote` macro which is just
like `defn` macro plus the registration of the definig function in a
registry of implemented as a map.

Here is the definition of the remote `calculate` function. Add it to
`core.clj` file. Remember to add `cemerick.shoreleave.rpc` namespace to
the `modern-cljs.core` namespace declaration.

```clojure
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [cemerick.shoreleave.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))
```

> NOTE 1: If you compare the remote `calculate` function with the one
> originally defined in `shopping.cljs` client code in the [last tutorial][1],
> you should note that the call to `.toFixed` CLJS function interop has
> been removed.

> NOTE 2: The namespace declaration uses now only `:require` and
> `:refer` specifications just to make the code more readable. Generally
> speaking I prefer to use `:require` with `:as` specification, because
> it always allows me to immediatly see in the code in which namespace a
> symbol is defined.

### Update the handler

As you perhaps remember, when we introduced [Compojure][18] in
[Tutorial 3][19] we defined the `handler` symbol by attaching to it the
result of calling the `site` function from the `compojure.handler`
namespace. The `site` function accepts routes defined by `defroutes`
macro included in `compojure.core` namespace and adds a set of *ring
middleware* suitables for a standard site (i.e. `wrap-session`,
`wrap-flash`, `wrap-coockies`, `wrap-multipart-params`, `wrap-params`,
`wrap-nested-params` and `wrap-keyword-params`.

We now need to add to the `handler` a middleware able to receive and
manage an rpc request coming from the browser.

```clojure
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [cemerick.shoreleave.rpc :refer [defremote wrap-rpc]]))
```

You should note that we added the symbol `wrap-rpc` in the list of the
referred symbols from `cemerick.shoreleave.rpc` namespace.
Now define the new handler `app` as follows:

```clojure
(def app (-> #'handler
             wrap-rpc
             site))
```

The last thing to be done for the server-side part of the reworking is
to update the `:ring` task configuration in the `project.clj`.

```clojure
;;; old :ring task configuration
;;; :ring {:handler modern-cljs.core/handler}

;;; new :ring task configuration
:ring {:handler modern-cljs.core/app}
```

The server-side is done. We now have to fix the client side code,
which means the `shopping.cljs` file.

## The client side

Open the `shopping.cljs` file from the [last tutorial][1].

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :as h])
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

(defn add-help []
  (dom/append! (dom/by-id "shoppingForm")
               (h/html [:div.help "Click to calculate"])))

(defn remove-help []
  (dom/destroy! (dom/by-class "help")))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (ev/listen! (dom/by-id "calc") :click calculate)
    (ev/listen! (dom/by-id "calc") :mouseover add-help)
    (ev/listen! (dom/by-id "calc") :mouseout remove-help)))
```

First you need to update the namespace declaration by adding the
`shoreleave.remotes.macros` and `shoreleave.remotes.http-rpc`.

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :as h]
                   [shoreleave.remotes.macros :as macros])
  (:require [domina :as dom]
            [domina.events :as ev]
            [shoreleave.remotes.http-rpc :as rpc]
            [cljs.reader :refer [read-string]]))
```

> NOTE 3: note that we also added `cljs.reader` namespace to
> `modern-cljs.shopping` namespace declaration. The reason why of this
> will became clear when we'll fix the client-side `calculate` function.

Let's now finish the work by modifying the `calculate` function.

```clojure
(defn calculate []
  (let [quantity (read-string (dom/value (dom/by-id "quantity")))
        price (read-string (dom/value (dom/by-id "price")))
        tax (read-string (dom/value (dom/by-id "tax")))
        discount (read-string (dom/value (dom/by-id "discount")))]
    (rpc/remote-callback :calculate
                         [quantity price tax discount]
                         #(dom/set-value! (dom/by-id "total") (.toFixed % 2)))))
```

### The arithmetic is not always the same

First take a look at the `let` form. We wrapped the reading of all the
input field values inside a `read-string` form, which returns the JS
object from the read string. That's because CLJS has the same arithmetic
semantics as JS, which is different from the corresponding one of the
CLJ on the JVM. Try to launch the rhino repl from `modern-cljs` home
directory and then evaluate a multiplication function by passing it two
stringfied numbers:

```clojure
$ lein trampoline cljsbuild repl-rhino
Running Rhino-based ClojureScript REPL.
"Type: " :cljs/quit " to quit"
ClojureScript:cljs.user> (* "6" "7")
42
ClojureScript:cljs.user>
```

As you can see CLJS implicitly casts the strings to numbers and then
applies the multiplication.

Now try the same thing in a regular CLJ repl:

```clojure
$ lein repl
nREPL server started on port 53127
REPL-y 0.1.4
Clojure 1.4.0
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

As you can see, and you should already know, CLJ throws a
`ClassCastException` because it can't cast a `String` to a `Number`.

It should be now clear why we add `cljs.reader` namespace to
`modern-cljs.shopping` namespace declaration for using `read-string`
function.

### The remote callback



# Next step - Tutorial 11 TBD

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
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
