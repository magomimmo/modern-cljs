# Tutorial 11 - A deeper understanding of Domina Events

> LATEST NEWS: Starting from this tutorial on, we dicided to directly
> include the `1.0.2-SNAPSHOT` of [domina][4] source code in the
> `modern-cljs` code. Consequently, we removed
> `[domina "1.0.2-SNAPSHOT"]` from the project dependencies. We also
> upgraded `lein-cljsbuild` to `0.2.10` version, even if we know that
> there is an open issue about a very boring and apparently useless
> waiting time after cljsbuild complete any CLJS compilation.

In the [latest tutorial][1] we introduced the ajax model of
communication between the browser and the server by exploiting the
[shoreleave-remote-ring][2] and [shoreleave-remote][3] libraries.

In this tutorial, prior to extend our comprehension of ajax in the
CLJS/CLJ context, we're going to get a better and deeper understanding
of few features of DOM events management provided by
[domina library][4].

To fullfill this objective, we're first going to line up the login
example introduced in the [4th Tutorial][5] with the more clojure-ish
programming style already adopted in the previous tutorials about the
Shopping Calculator example.

# Introduction

The following picture shows our old `Login Form` friend.

![Login Form][6]

As you perhaps remember from the [4th Tutorial][5], we desire to
adhere to the [progressive enhancement][7] strategy which allows any
browser to access our login form, regardless of the browser
capabilities.

The lowest user experience is the one offered by a web application
when the browser does not support JS (or it has been disabled by the
user). The highest user experience is the one offered by a web
application when the the browser support JS and the application uses
the Ajax communication model.

Generally speaking, you should always start by supporting the lowest
user experience. Then you step to the next layer by supporting JS and
finally you realize your last user experience enhancement by introducing
the ajax model of communication between the browser and the server.

Being this series of tutorials mostly about CLJS and not about CLJ, we
skypped the layer representating the lowest user experience wich is
based on CLJ only. Yet, we promise to fill this gap in successives
tutorials explaining the usage of CLJ libraries on the server-side.

# Line up Login Form with Shopping Calculator Form

The [9th tutorial][8] left to the smart user the charge of applying to
the *Login Form* the same kind of DOM manipulation used in implementing
the *Shopping Calculator*.

Let's now work together on the first step of this not so easy task we
previously left to you. We start by reviewing the html code of the
`login-dbg.html`.

```html
<!doctype html>
<html lang="en">
<head>
...
...
</head>
<body>
    <form action="login.php" method="post" id="loginForm" novalidate>
        <fieldset>
            <legend>Login</legend>
            ...
            ...
            <div>
              <label for="submit"></label>
              <input type="submit" value="Login &rarr;" id="submit">
            </div>
        </fieldset>
    </form>
    <script src="js/modern_dbg.js"></script>
    <script>
      modern_cljs.login.init();
    </script>
</body>
</html>
```

As you remember, when we reviewed the `Shopping Calculator` code to
make it more clojure-ish, we started by changing the `type` attribute
of the Shopping form's button from `type="submit"` to
`type="button"`. By having decided to adhere to progressive
enhancement strategy, this is not something that we should have done,
because a plain [button type][9] is not going anywhere without JS been
enabled in the browser. So we need to stay with the `submit` type of
botton.

## First try

Start by making the programming style of `login.cljs` more
clojure-ish. First we want to remove any CLJS/JS interop call by using
[domina library][4]. Open the `login.cljs` file and change the `init`
function as follows.

```clojure
(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click validate-form)))
```

> NOTE 1: The [domina.events library][11] contains a robust event
> handling API that wraps the Google Closure event handling code, while
> exposing it in a idiomatic functional way for both the `bubbling`
> event propagation phase and the `capture` phase one.  In our login
> form example, by having used the `listen!` function, we have also
> implicitly choosen the `bubbling` phase. That said, in domina the
> `submit` type of event does not bubble up, and we then needed to
> attach the listener function (i.e. `validate-form`) to the `:click`
> event of the `submit` button, instead of attaching it to the
> `loginForm` as it was before.

Now compile and run the application as usual.

```bash
$ lein cljsbuild clean # clean any previous CLJS compilation
$ lein cljsbuild auto dev # compile just the `dev` build
$ lein ring server-headless # lunch the server from a new terminal
```

Then visit [login-dbg.html][12] and do not fill any field (or fill
just one of them). The application reacts by showing you the usual
`alert` window that remember you to complete the form
compilation. Click the `OK` button and be prepared to an unexpected
result.

Instead of showing the `loginForm` to allow the user to complete it,
the process flows directly to the default `action` attribute of the
form which, by calling an inesitent server-side script
(i.e. `login.php`) return the `Page not found` message generated by
the ring/compjure server.

## Prevent the default

Take a look at the [domina/events.cljs][13] source code and direct your
attention to `Event` protocol and to the private [HOF][14]
`create-listener-function`.

```clojure
(defprotocol Event
  (prevent-default [evt] "Prevents the default action, for example a link redirecting to a URL")
  (stop-propagation [evt] "Stops event propagation")
  (target [evt] "Returns the target of the event")
  (current-target [evt] "Returns the object that had the listener attached")
  (event-type [evt] "Returns the type of the the event")
  (raw-event [evt] "Returns the original GClosure event"))

(defn- create-listener-function
  [f]
  (fn [evt]
    (f (reify

         Event

         (prevent-default [_] (.preventDefault evt))
         (stop-propagation [_] (.stopPropagation evt))
         (target [_] (.-target evt))
         (current-target [_] (.-currentTarget evt))
         (event-type [_] (.-type evt))
         (raw-event [_] evt)

         ILookup

         (-lookup [o k]
           (if-let [val (aget evt k)]
             val
             (aget evt (name k))))
         (-lookup [o k not-found] (or (-lookup o k)
                                      not-found))))
    true))

(defn listen!
  ([type listener] (listen! root-element type listener))
  ([content type listener]
     (listen-internal! content type listener false false)))
```

This is were you can find a beautiful clojure-ish programming style.  It
uses the anonymous reification idiom to attach predefined protocols
(i.e. `Event` and `ILookup`) to any data/structured data you want. Take
your time to study it. I promise you will be rewarded.

Anyway, we're not here to discuss programming elegance, but to solve the
problem of preventing the `action` login form from being fired when the
user has not filled the required fields.

Thanks to the programming idiom cited above, we now know that the
`Event` protocol supports, among others, the `prevent-default`
function which is what we need to interupt the process of passing the
control from the `submit` button to the form `action` attribute.

This application of the anonymous reification idiom requires that the
fired event (i.e. `:click`) is passed to the `validate-form` listener as
follows:

```clojure
(ns modern-cljs.login
  (:require [domina :refer [by-id value]]
            [domina.events :refer [listen! prevent-default]]))

(defn validate-form [e]
  (let [email (value (by-id "email"))
        password (value (by-id "password"))]
    (if (or (empty? email) (empty? password))
      (do
        (prevent-default e)
        (js/alert "Please insert your email and password"))
      true)))
```

> NOTE 2: Remeber to add `prevent-default` symbol to the `:refer`
> section for `domina.events` in the namespace declaration.

> NOTE 3: We took adantage of the necessity to update the `validate-form`
> function for improving its clojure-ish style. The semantic of the
> `validation-form` is now much more clear than before:
>
> * get the values of email and password
> * if one of the two is empty, prevent the form action form being
>   fired, raise the alert window asking the user to compile the
>   email and the password and finally return the control to the form;
> * otherwise return `true` to pass the control to the `default`
>   action of the form.

> NOTE 4: If you carefully watch the `validate-form` implementation you
> should note that the `if` branch traversed when its condition is
> `true` (i.e. when or the `email` or the `password` are empty), it does
> not return the `false` value regularly used to block the event
> propagation to the `action` attribute of the form. That's because
> `validate-form` is now internally calling `prevent-default` function
> and a returnig `false` would become reduntant.

One last code modification in the `init` function and we're done.

```clojure
(defn ^:export init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click (fn [e] (validate-form e)))))
```

Here we just wrap inside an anonymous function the `validate-form`
listener by passing it the fired `event` (i.e.`:click`).

As usual, let's now verify our work by visiting the
[login-dbg.html][12] page. If you have stopped the previous
application run, execute again the usual commands from the terminal
prior to visit the [login page][12].

```bash
$ lein cljsbuild auto dev
$ lein ring server-headless
```

## Catch early react istantly

It's now time to see how we can improve the user experience of the
`loginForm` by using some more DOM events and DOM manipulations features
of Domina.

One of the first lesson I learnt 30 years ago, when I first started
programming, it was that any error has to be catch and managed as soon
as it manifests itself. For the `loginForm` example, *as soon as
possible* it means as soon as the email input field loses the focus
(i.e. *blur*).

Open the `login.cljs` source file and update it as follow:



## Event Types

If you're interested in knowing all the event types supported by
[Domina][4], here is the native code [goog.events.eventtypes.js][15]
which enumerates all supported events. Take into account that Google
Closure uses string as the event keys and Domina makes its best to
convert their keywords representation to strings for lookup purposes
using the following code:

```clojure
;; goog.events native namespace required as `events`
;; goog.object native namespace required as `gobj`

(def builtin-events (set (map keyword (gobj/getValues events/EventType))))


(defn- find-builtin-type
  [evt-type]
  (if (contains? builtin-events evt-type)
    (name evt-type)
    evt-type))
```

That means that if you run the brepl and evaluates `builtin-events`
symbol you get the `set` of all the supported events.

```bash
$ lein ring server-headless
$ lein cljs-build auto dev # from a new terminal
$ lein trampoline cljs-build repl-listen # from a new terminal
```
```clojure
Running ClojureScript REPL, listening on port 9000.
"Type: " :cljs/quit " to quit"
ClojureScript:cljs.user> domina.events/builtin-events
#{:submit :unload :DOMFocusOut :help :dragstart :cut :losecapture
 :mousedown :touchmove :touchcancel :keypress :paste :mouseover
 :propertychange :pageshow :popstate :contextmenu :offline :beforecut
 :resize :mouseout :dragover :click :error :selectstart :load :touchend
 :blur :change :hashchange :webkitTransitionEnd :focus :keydown :connect
 :mouseup :touchstart :dragleave :drop :pagehide :message :keyup :online
 :mousemove :scroll :input :deactivate :beforecopy :beforepaste :copy
 :DOMFocusIn :select :dblclick :dragenter :readystatechange}
ClojureScript:cljs.user>
```

Remeber to visit [login-dbg.html][12] to activate the brepl before to
evaluate any expression in the brepl.

# Conlusions

The approach followed in this tutorial to manage `submit` events can
be easly applied when you want to be compliant with the progressive
enhancement strategy. In the next tutorials we're going to adorn the
login form with more DOM events and manipulation to be prepared for
*ajaxinig* it.


# Next step - TBD

TBD

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[2]: https://github.com/shoreleave/shoreleave-remote-ring
[3]: https://github.com/shoreleave/shoreleave-remote#shoreleave
[4]: https://github.com/levand/domina
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-form.png
[7]: http://en.wikipedia.org/wiki/Progressive_enhancement
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[9]: http://stackoverflow.com/questions/290215/difference-between-input-type-button-and-input-type-submit
[10]: https://developers.google.com/closure/library/
[11]: https://github.com/levand/domina#event-handling
[12]: http://localhost:3000/login-dbg.html
[13]: https://github.com/levand/domina/blob/master/src/cljs/domina/events.cljs
[14]: http://en.wikipedia.org/wiki/Higher-order_function
[15]: https://code.google.com/p/closure-library/source/browse/trunk/closure/goog/events/eventtype.js?r=469
[16]: http://stackoverflow.com/questions/201323/using-a-regular-expression-to-validate-an-email-address
