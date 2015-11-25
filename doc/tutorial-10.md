# Tutorial 10 - A Deeper Understanding of Domina Events

In the [previous tutorial][1] we introduced the Ajax model of
communication between the browser and the server by exploiting the
[shoreleave-remote-ring][2] and [shoreleave-remote][3] libraries.

In this tutorial, prior to extending our comprehension of Ajax in the
CLJS/CLJ context, we're going to get a better and deeper understanding
of a few features of DOM events management provided by
[domina][4].

To fulfill this objective, we're first going to line up the login
example introduced in the [4th Tutorial][5] with the more Clojure-ish
programming style already adopted for the Shopping Calculator example
in the previous tutorials.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][26] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-09
git checkout -b tutorial-10-step-1
```

## Introduction

The following picture shows our old Login Form friend.

![Login Form][6]

As you perhaps remember from the [4th Tutorial][5], we desire to
adhere to the [progressive enhancement][7] strategy which allows any
browser to access our login form, regardless of the browser
capabilities.

The lowest user experience is the one offered by a web application
when the browser does not support JS (or it has been disabled by the
user). The highest user experience is the one offered by a web
application when the the browser supports JS and the application uses
the Ajax communication model.

Generally speaking, you should always start by first supporting the
lowest user experience. Then you step to the next layer by supporting
JS and finally you realize the best user experience enhancement by
introducing the Ajax model of communication between the browser and
the server.

Because this series of tutorials is mostly about CLJS and not about CLJ,
we skipped the layer representating the lowest user experience which is
based on CLJ only. Yet, we promise to fill this gap in successive
tutorials explaining the usage of CLJ libraries on the server side.

# Line up Login Form with Shopping Calculator Form

The [8th tutorial][8] left to the smart user the task of updating
the *Login Form* with the same kind of DOM manipulation used in implementing
the *Shopping Calculator*.

## Start IFDE

As usual we like to work in a live environment. So let's launch IFDE:

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 21.757 sec
```

Then in a new terminal launch the bREPL as usual

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:51016 >>
<< waiting for client to connect ... Connection is ws://localhost:51016
Writing boot_cljs_repl.cljs...
```

and visit the `http://localhost/index.html` URL to activate the bREPL

```clj
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

## index.html

Let's work together on the first step of this task. We start by
reviewing the html code of `index.html` (i.e., the Login Form).

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
    <script src="main.js"></script>
    <script>
      modern_cljs.login.init();
    </script>
</body>
</html>
```

> NOTE 1: The original and non-existent `login.php` server script is
> still attached to the form `action` attribute. In a later
> tutorial we're going to replace it with a corresponding service
> implemented in CLJ.

As you remember, when we reviewed the `Shopping Calculator` code to
make it more Clojure-ish, we started by changing the `type` attribute
of the Shopping Form's button from `type="submit"` to
`type="button"`. But having decided to adhere to a progressive
enhancement strategy, this is not something that we should have done--a
plain [button type][9] is not going anywhere if the browser doesn't support JS.
So we need to stay with the `submit` type of button.

## First try

Start by making the programming style of `login.cljs` more
Clojure-ish. First we want to remove any CLJS/JS interop calls by
using [domina][4]. Open `login.cljs`, update the requirement of the
namespace declaration and change the `init` function to make it more
Clojur-ish

```clj
;;; namespace declaration
(ns modern-cljs.login
  (:require [domina.core :refer [by-id value]]
            [domina.events :refer [listen!]]))
```

```clj
;;; init
(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click validate-form)))
```

> NOTE 2: The [domina.events library][11] contains a robust event
> handling API that wraps the Google Closure event handling code and
> exposing it in an idiomatic functional way for both the `bubbling`
> event propagation phase and the `capture` phase.  In our login
> form example, by having used the `listen!` function, we have also
> implicitly choosen the `bubbling` phase. That said, in domina the
> `submit` event does not bubble up, so we needed to
> attach the listener function (i.e., `validate-form`) to the `:click`
> event of the `submit` button, instead of attaching it to the
> `loginForm`.

As soon as you save the file, it get recompiled and reload. 

Reload the [`index.html`][12] URL to allow the updated `init` function
to be called again. Do not fill any field (or fill just one of them),
and click the "Login" button. The application reacts by showing you
the usual `alert` window reminding you to complete the form.  Click
the `OK` button and be prepared for an unexpected result.

Instead of showing the login form to allow the user to complete it,
the process flows directly to the default `action` attribute of the
form which, by calling a non-existent server-side script (i.e.,
`login.php`), returns the `Page not found` message generated by the
*ring/compojure* web server. That's very bad!

## Prevent the default

Go back to the [index.html][12] URL and require the `domain.events'
namespace at the bREPL and ask for the `Event` protocol docstring:

```clj
cljs.user> (require '[domina.events :as evt])
nil
cljs.user> (doc evt/Event)
-------------------------
domina.events/Event
Protocol
  nil

  prevent-default
  ([evt])
  Prevents the default action, for example a link redirecting to a URL

  stop-propagation
  ([evt])
  Stops event propagation

  target
  ([evt])
  Returns the target of the event

  current-target
  ([evt])
  Returns the object that had the listener attached

  event-type
  ([evt])
  Returns the type of the the event

  raw-event
  ([evt])
  Returns the original GClosure event
nil
```

We now know that the `Event` protocol supports, among others, the
`prevent-default` function, which is what we need to interupt the
process of passing control from the `submit` button to the form
`action` attribute.

The `prevent-default` function requires the fired event (e.g.,
`:click`) be passed to the `validate-form` listener. Let's modify its
definition accordingly to the above information.

First we have to update the `domina.events` requirement by adding
`prevent-default` symbol to the `:refer` option

```clj
(ns modern-cljs.login
  (:require [domina.core :refer [by-id value]]
            [domina.events :refer [listen! prevent-default]]))
```

Then we can go on by updating the `validate-form` defintion as
follows:

```clj
(defn validate-form [e]
  (if (or (empty? (value (by-id "email")))
          (empty? (value (by-id "password"))))
    (do 
      (prevent-default e) 
      (js/alert "Please, complete the form!"))
    true))
```

Here we took advantage of the necessity to update the `validate-form`
function to improve its Clojure-ish style. The semantics of the
`validation-form` is now much more readable than before:

* if the `value` of the `email` or `password` is empty, prevent the
  form action from being fired, raise the alert window asking the user
  to enter the email and the password and finally return control to
  the form;
* otherwise return `true` to pass control to the default action of the
  form.

> NOTE 3: If you carefully watch the `validate-form` implementation you
> should note that the `if` branch traversed when its condition is
> `true` (i.e., when the `email` or the `password` are empty), it does
> not return the `false` value regularly used to block event
> propagation to the `action` attribute of the form. That's because
> `validate-form` is now internally calling `prevent-default`,
> so returning `false` would be redundant.

To make the above mechanics more clear, we also update the `init`
function by wrapping the `validate-forma` listener inside an anonymous
function taking the event `e` as argument. If you want, you can can
safetly leave it as before.

```clj
(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (listen! (by-id "submit") :click (fn [e] (validate-form e)))))
```

Save the file and reload the [index.html][12] page. As you peraphs
remember, the `init` function is called, as a JS script, when the page
is loaded. This is one of the rare case in which the live IFDE is not
able to automate this manual activity.

Now play with the Login Form to verify that it started working again
as expected.

## Catch early react instantly

It's now time to see if we can improve the user experience of the
login form by introducing few more DOM events and DOM manipulation
features of Domina.

One of the first lessons I learned when I started programming was
that any error has to be caught and managed as soon as possible.

In our login form context, *as soon as possible* means that the
syntactical correctness of the email and password typed in by the user
has to be verified as soon as their input fields lose focus (i.e.,
*blur*).

### Email/Password validators

A pretty short specification of our desire could be the following:

1. As soon as the email input field loses focus, check its
syntactical correctness by matching its value against one of the several
[email regex validators][16] available on the net; if the validation
does not pass, make the error evident to help the user;

2. A soon as the password input field loses focus, check its
syntactical correctness by matching its value against one of the
several [password regex validators][24]; if the validation does not
pass, make the error evident to the user.

Although a nice looking implementation of the above specification is
left to you, let's show at least a very crude sample from which to
start.

> NOTE 4: Take a look at the end of [this post][23] for an
> HTML5-compliant approach to password validation.

Open the `login.cljs` source file and start by adding two
[dynamic vars][17] to be used for the `email` and `password` fields
validation:

```clj
;;; 4 to 8, at least one numeric digit.
(def ^:dynamic *password-re* 
  #"^(?=.*\d).{4,8}$")

(def ^:dynamic *email-re* 
  #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")
```

If you add the chance to read the
[differences between CLJ and CLJS](https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure),
you already know that CLJS support for regular-expressions is JS
support.

Now add the `:blur` event listener to both the `email` and `password`
input fields in the `init` function:

```clj
(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt)))
      (listen! email :blur (fn [evt] (validate-email email)))
      (listen! password :blur (fn [evt] (validate-password password))))))
```

We have not passed the event to the two new listeners because, as opposed
to the previous `validate-form` case, it is not needed to
prevent any default action or to stop the propagation of the
event. Instead, we passed them the element on which the blur event
occurred.

Now define the two new validators. Here is a very crude implementation
of them. Remember to define them before the `validate-form` and after
the two newly defined regexs.

```clj
(defn validate-email [email]
  (destroy! (by-class "email"))
  (if (not (re-matches *email-re* (value email)))
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email "Wrong email"]))
      false)
    true))

(defn validate-password [password]
  (destroy! (by-class "password"))
  (if (not (re-matches *password-re* (value password)))
    (do
      (append! (by-id "loginForm") (html [:div.help.password "Wrong password"]))
      false)
    true))
```

I'm very bad both in HTML and CSS. So, don't take this as something to
be proud of. Anyone can do better than me. I just added a few CSS classes
(i.e., `help`, `email` and `password`) using the [hiccups library][21] to
manage the email and password help messages.

Obviously you have to update the namespace declaration as well to be
able to use the the `append!`, `by-class`, `destroy!` and `prepend!`
symbols from the `domina.core` namespace and the `html` symbol from
the `hiccups.core` namespace.

```clj
(ns modern-cljs.login
  (:require [domina.core :refer [append!
                                 by-class
                                 by-id
                                 destroy!
                                 prepend!
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))
```

To complete the coding, review the `validate-form` function as
follows:

```clj
(defn validate-form [evt]
  (let [email (by-id "email")
        password (by-id "password")
        email-val (value email)
        password-val (value password)]
    (if (or (empty? email-val) (empty? password-val))
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") (html [:div.help "Please complete the form"])))
      (if (and (validate-email email)
               (validate-password password))
        true
        (prevent-default evt)))))
```

Note that now the `validate-form` internally calls the two newly-defined
validators and if they do not both return `true`, it calls
`prevent-default` to prevent the `action` attached to
the `loginForm` from being fired.

Following is the complete and final `login.cljs` source code.

```clj
(ns modern-cljs.login
  (:require [domina.core :refer [append!
                                 by-class
                                 by-id
                                 destroy!
                                 prepend!
                                 value]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))

;;; 4 to 8, at least one numeric digit.
(def ^:dynamic *password-re* 
  #"^(?=.*\d).{4,8}$")

(def ^:dynamic *email-re* 
  #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if (not (re-matches *email-re* (value email)))
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email "Wrong email"]))
      false)
    true))

(defn validate-password [password]
  (destroy! (by-class "password"))
  (if (not (re-matches *password-re* (value password)))
    (do
      (append! (by-id "loginForm") (html [:div.help.password "Wrong password"]))
      false)
    true))

(defn validate-form [evt]
  (let [email (by-id "email")
        password (by-id "password")
        email-val (value email)
        password-val (value password)]
    (if (or (empty? email-val) (empty? password-val))
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") 
                 (html [:div.help "Please complete the form"])))
      (if (and (validate-email email)
               (validate-password password))
        true
        (prevent-default evt)))))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt)))
      (listen! email :blur (fn [evt] (validate-email email)))
      (listen! password :blur (fn [evt] (validate-password password))))))
```

To make the help messages more evident to the user, add the following CSS rule
to `styles.css` which resides in the `html/css` directory.

```css
.help { color: red; }
```

Reload again the [`index.html`][12] page to re-attach the lesteners to
the Login Form. Verify the result by playing with the input fields and
the Login button. You should see something like the following
pictures.

![Email Help][18]

![Password Help][19]

![Complete the form][20]

## Event Types

If you're interested in knowing all the event types supported by
[domina][4], here is the native code [goog.events.eventtype.js][15]
which enumerates all events supported by the Google Closure native code on
which `domina` is based.

Another way to know which events are supported by `domina` is to
inspect the Google library `goog.events/EventType` directly.  If we do
this in the `domina.events` namespace it will save some typing.

```clj cljs.user> (in-ns 'domina.events)
nil
domina.events> (map keyword (gobj/getValues events/EventType))
(:click :rightclick :dblclick :mousedown :mouseup :mouseover :mouseout
:mousemove :mouseenter :mouseleave :selectstart :wheel :keypress
:keydown :keyup :blur :focus :deactivate :DOMFocusIn :DOMFocusOut
:change :reset :select :submit :input :propertychange :dragstart :drag
:dragenter :dragover :dragleave :drop :dragend :touchstart :touchmove
:touchend :touchcancel :beforeunload :consolemessage :contextmenu
:DOMContentLoaded :error :help :load :losecapture :orientationchange
:readystatechange :resize :scroll :unload :hashchange :pagehide
:pageshow :popstate :copy :paste :cut :beforecopy :beforecut
:beforepaste :online :offline :message :connect :webkitAnimationStart
:webkitAnimationEnd :webkitAnimationIteration :webkitTransitionEnd
:pointerdown :pointerup :pointercancel :pointermove :pointerover
:pointerout :pointerenter :pointerleave :gotpointercapture
:lostpointercapture :MSGestureChange :MSGestureEnd :MSGestureHold
:MSGestureStart :MSGestureTap :MSGotPointerCapture :MSInertiaStart
:MSLostPointerCapture :MSPointerCancel :MSPointerDown :MSPointerEnter
:MSPointerHover :MSPointerLeave :MSPointerMove :MSPointerOut
:MSPointerOver :MSPointerUp :text :textInput :compositionstart
:compositionupdate :compositionend :exit :loadabort :loadcommit
:loadredirect :loadstart :loadstop :responsive :sizechanged
:unresponsive :visibilitychange :storage :DOMSubtreeModified
:DOMNodeInserted :DOMNodeRemoved :DOMNodeRemovedFromDocument
:DOMNodeInsertedIntoDocument :DOMAttrModified
:DOMCharacterDataModified :beforeprint :afterprint)
```

To complete the application of the progressive enhancement strategy to
the login form, in future tutorials we'll introduce [friend][22]
and line up the login form to the shopping form approach adopted in
the [10th tutorial][1] to allow the browser to communicate with the server
via Ajax.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "deeper understanding"
```

# Next Step - [Tutorial 12: HTML on Top, Clojure on the Bottom][25]

In the [next tutorial][25] we're going to cover the highest and the deepest
layers of the progressive enhancement strategy to the Login Form.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-09.md
[2]: https://github.com/shoreleave/shoreleave-remote-ring
[3]: https://github.com/shoreleave/shoreleave-remote#shoreleave
[4]: https://github.com/levand/domina
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-form.png
[7]: http://en.wikipedia.org/wiki/Progressive_enhancement
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
[9]: http://stackoverflow.com/questions/290215/difference-between-input-type-button-and-input-type-submit
[10]: https://developers.google.com/closure/library/
[11]: https://github.com/levand/domina#event-handling
[12]: http://localhost:3000/index.html
[13]: https://github.com/levand/domina/blob/master/src/cljs/domina/events.cljs
[14]: http://en.wikipedia.org/wiki/Higher-order_function
[15]: https://code.google.com/p/closure-library/source/browse/closure/goog/events/eventtype.js
[16]: http://stackoverflow.com/questions/201323/using-a-regular-expression-to-validate-an-email-address
[17]: http://clojure.org/vars
[18]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/help-01.png
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/completheform-01.png
[20]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/allhelp.png
[21]: https://github.com/teropa/hiccups
[22]: https://github.com/cemerick/friend
[23]: http://www.the-art-of-web.com/javascript/validate-password/#.UPvsHaGjejI
[24]: http://regexlib.com/Search.aspx?k=password&c=-1&m=-1&ps=20
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[26]: https://help.github.com/articles/set-up-git
