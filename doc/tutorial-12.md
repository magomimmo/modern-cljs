# Tutorial 12 - Be friendly with ajax (Part 1)

In the [latest tutorial][] we saw few Domina features about DOM events
which helped us to the desire of adhering to the progressive
enhancement strategy in developing a clojurean web application.  By
implementing, via CLJS, the *javascript* layer, in the previous
tutorial we only covered one the four typical layers of that
stategy. We still need to implement the top most layer, the HTML5
layer, and the two deeper ones: the Ajax layer and the *pure* server
side layer.

# Introduction

In this and the next tutorials we're going to cover the two deeper
missing layers from `loginForm` example, leaving a detailed care of
the *superficial* HTML5 layer to anyone who likes to play with it. The
only two exceptions are the new `title` and `pattern` HTML5 attributes
which we are going to use instead of the corresponding patterns and
help messages we previously screwed in the `validate-email` and
`validate-password` functions.

Here is the interested fragment of the updated `login-dbg.html`.

```html
<html>
...
    <form action="login.php" method="post" id="loginForm">
           ...
            <div>
              <label for="email">Email Address</label>
              <input type="email" name="email" id="email"
                     placeholder="email"
                     title="Type a well formed email!"
                     pattern="^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$"
                     required>
            </div>

            <div>
              <label for="password">Password</label>
              <input type="password" name="password" id="password"
                     placeholder="password"
                     pattern="^(?=.*\d).{4,8}$"
                     title="Password is from 4 to 8 chars with 1 number!"
                     required>
            </div>
...
</html>
```

> NOTE 1: remember to make the same modification to both
> `login-pre.html` and `login.html` which serve respectively the
> `pre-prod` and the `prod` builds defined in the `project.clj`
> file. I hate this kind of code duplication. At the moment it is not
> something I'm trying to solve.

As you can see, we removed the `novalidate` HTML5 attribute from the
form and added `placeholder`, `title` and `pattern` HTML5 attributes
to both `email` and `password` HTML5 `input` field elements of the
form.

Uhm, this is again a kind code duplication and I hate it more than the
previuos one. Luckly this time we have an handly solution based on
[domina library][]. Let's modify the `validate-email` and
`validate-password` handlers in such a way that, instead of having
those patterns and help messages screwed in the source code, we can
get them from the input elements attributes by using the `attr`
function defined in the `domina` namespace. Following is the fragment
of the updated code from `login.cljs`.

```clojure
(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]))

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if (not (re-matches (re-pattern "\\d+" (attr email :pattern)) (value email)))
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (attr email :title)]))
      false)
    true))

(defn validate-password [password]
  (destroy! (by-class "password"))
  (if (not (re-matches (re-pattern "\\d+" (attr password :password)) (value password)))
    (do
      (append! (by-id "loginForm") (html [:div.help.password (attr password :title)]))
      false)
    true))
```

> NOTE 2: by using a CLJS expression inside the `html` macro from
> [hiccups][] library you're now forced to require the `hiccups.runtime`
> namespace even if you do not explicitly use any function from it.

> NOTE 3: We used `re-pattern` function to convert the strings got from
> the `:title` and the `:pattern` attributes in regular expressions to be
> passed to `re-matches`.

As usual, check your progression by running the application.

```bash
$ lein cljsbuild clean # at times it is needed
$ lein cljsbuild auto dev # dev build only
$ lein ring server-headless # from a new terminal
```

Visit [login-dbg.html][] and verify the `:blur` and the `:click`
handlers are still working as in the [previous tutorial][].

Now disable the JavaScript of your browser, reload the page and click
the `Login` button without having filled the email and the password
fields. 

If you're using an HTML5 browser you should see a message telling you
to fill out the email field with the added text of the `title`
attribute previously set up in the form. 

If you don't use an HTML5 browser, the new attributes are just ignored
and the process goes directly on by calling the still inexistent
`login.php` server-side script and you get the usual `Not found page`
from [compojure][]. It's now time to take care of it, that means to
implement the server-side login service. It represents the deepest
layer layer of the progressive enhancement strategy. The onewe sould
have started from.

# The deepest surface layer

As you already know, this series of tutorials is about CLJS, not
CLJ. On the net there are already some very good tutorials on CLJ web
application develpoment using [ring][] and [compojure][]. Despite
this, we think we should at least give you a sound trace from where to
start from.

First of all we have to delete in the `login-dbg.html` file the
`login.php` server-side script originally attached to the `action`
attribute of the `loginForm` and substitute it a call to a compojure
route. We'll call this new route `login`. Following is the interested
and updated code fragment.

```html
<!doctype html>
<html lang="en">
...
    <form action="login" method="post" id="loginForm">
	...
    </form>
...
</html>
```

Now, when the `action` attribute value of the form will be called with
the `POST` method, the browser sends to the server an http request
containing the "/login" URI. Then, the ring server translates the
received request in a request map which, after traversing the various
ring middleware (i.e. `wrap-rcp` and `site`) finally reach the
`app-routes` to produce a response map defined in the `core.clj` via
the `defroutes` macro of the `compojure.core` namespace. 

At the moment `app-routes` doesn't know how to manage a `POST` method
request and we need to tech it by adding it a new `POST`
route. Following is the interested and update code grament from
`core.clj` file.

```clojure
(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to server static pages saved in resources/public directory
  (POST "/login" [email password] (validate-user email password)) 
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))
```

As you can see we only added a new route, `(POST "/login"
[email password] (validate-user email password))` which will be
selected when the received request map has the `/login` URI. Both `GET` and `PUT` are compojure macros which expand in a enriched request map. 

# Next step - TBD

TBD

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

