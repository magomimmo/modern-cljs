# Tutorial 12 - HTML on Top, Clojure on the Bottom

In the [previous tutorial][1], we used DOM events to further the
progressive enhancement strategy of our clojurean web application. By
implementing, via CLJS, the *javascript* layer, in the previous
tutorial we only covered one of the four typical layers of that
stategy. We still need to implement the topmost layer, the HTML5
layer, and the two lowest ones: the Ajax layer and the *pure* server
side layer.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][10] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-11
git checkout -b tutorial-12-step-1
```

# Introduction

In this tutorial we're going to cover the highest and lowest layers
of the `loginForm` example we started to cover in the
[previous tutorial][1]. We leave detailed care of the highest layer
(i.e. HTML5) to anyone who wants to play with it. Here we want only
introduce the new `title` and `pattern` HTML5 attributes, which we are
going to use by pairing the corresponding patterns and help messages we
previously nailed in the `validate-email` and `validate-password`
functions.

# The highest surface

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
> `pre-prod` and the `prod` builds defined in the `project.clj` file (I
> hate this kind of code duplication. But, at the moment it is not something
> I'm trying to solve).

As you can see, we removed the `novalidate` HTML5 attribute from the
form and added the `placeholder`, `title` and `pattern` HTML5 attributes
to both the `email` and the `password` HTML5 `input` field elements of
the form.

Umm, this is again a kind of code duplication and I hate it more than the
previous one. Luckly this time we have a handy solution based on the
[domina library][2]. Let's modify the `validate-email` and
`validate-password` handlers in such a way that, instead of having those
patterns and help messages defined in the source code, we can get them
from the input elements attributes by using the `attr` function defined
in the `domina` namespace. Following is the fragment of the updated code
from `login.cljs`.

```clojure
(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]))

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if (not (re-matches (re-pattern (attr email :pattern)) (value email)))
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (attr email :title)]))
      false)
    true))

(defn validate-password [password]
  (destroy! (by-class "password"))
  (if (not (re-matches (re-pattern (attr password :pattern)) (value password)))
    (do
      (append! (by-id "loginForm") (html [:div.help.password (attr password :title)]))
      false)
    true))
```

> NOTE 2: by using a CLJS expression inside the `html` macro from
> [hiccups][3] library you're now forced to require the `hiccups.runtime`
> namespace even if you do not explicitly use any function from it.

> NOTE 3: We used `re-pattern` function to convert the strings got from
> the `:title` and the `:pattern` attributes in regular expressions to be
> passed to `re-matches`.

As usual, check your progress by running the application.

```bash
lein do cljsbuild clean, cljsbuild auto dev # dev build only
lein ring server-headless # from a new terminal
```

Visit [login-dbg.html][4] and verify that the `:blur` and the `:click`
handlers are still working as in the [previous tutorial][1].

Now disable the JavaScript of your browser, reload the page and click
the `Login` button without having filled the email and the password
fields.

If you're using an HTML5 browser you should see a message telling you to
fill out the email field and the text attached as the value of the
`title` attribute previously set up in the form.

If you don't use an HTML5 browser, the new attributes are just ignored
and the process continues by calling the still non-existent
`login.php` server-side script and you get the usual `Not found page`
from the [compojure][5] routes we defined in the [3rd Tutorial][6].

It's now time to take care of this error by implementing the server-side
login service, which represents the deepest layer of the
progressive enhancement strategy: the one we should have started from in
a real web application development.

# The deepest surface layer

As you already know, this series of tutorials is about CLJS, not CLJ. On
the net there are already some very good tutorials on CLJ web
application develpoment using [ring][7] and [compojure][5]. Despite
this, we think we should at least give you a starting point and a track
to be followed.

First of all, we need to delete the `login.php` value originally
attached to the `action` attribute of the `loginForm` and substitute it
with a call to a compojure route. We'll call this new route
`login`. Following is the updated code fragment.

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

Now, after any HTML5 field validation has been passed, the browser is
going to request the server for the `/login` resource by using the
`POST` method, which passes the `email` and `password` values
provided by the user.

We should now take a look at the `core.clj` source file where the ring
request handler and the compojure routes have been defined. The received
"/login" POST request traverses the various ring middleware layers to finally
reach the `app-routes` which has to provide the response to be sent to
the browser.

At the moment, `app-routes` doesn't yet know how to manage a `POST` method
request. Add to `app-routes` a new `POST` route.

Following is the complete new `core.clj` source code.

```clojure
(ns modern-cljs.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [compojure.handler :refer [site]]
            [modern-cljs.login :refer [authenticate-user]]))

(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  (POST "/login" [email password] (authenticate-user email password))
  (resources "/")
  (not-found "Page non found"))

(def handler
  (site app-routes))
```

Whenever the web server is receiving a POST request with the "/login"
URI, the `email` and `password` parameters are extracted from the body
of the POST request and the `authenticate-user` function will be called
by passing it those parameters.

> NOTE 4: We intend to define the `authenticate-function` in the new
> `modern-cljs.login` namespace wich has to be required in the
> `modern-cljs.core` namespace declaration.

## First try

To keep things simple, at the moment we're not going to be concerned
with user authentication or with any security issues. We also don't
want to return any nice HTML response page. We just want to see if the
working parts have been correctly set up.

Create the file `login.clj` in the `src/clj/modern_cljs/` directory and
copy to it the following code.

```clojure
(ns modern-cljs.login)

(def ^:dynamic *re-email*
  #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$")

(def ^:dynamic *re-password*
  #"^(?=.*\d).{4,8}$")

(declare validate-email validate-password)

(defn authenticate-user [email password]
  (if (or (empty? email) (empty? password))
    (str "Please complete the form")
    (if (and (validate-email email)
             (validate-password password))
      (str email " and " password
           " passed the formal validation, but you still have to be authenticated"))))

(defn validate-email [email]
  (if  (re-matches *re-email* email)
    true))

(defn validate-password [password]
  (if  (re-matches *re-password* password)
    true))
```

There are a lot of bad things in this code. The most relevant is
the code duplication between the client and the server code. For the
moment be forgiving and go on by verifying the code mechanics.

So, be patient and run the application as usual.

```bash
lein cljsbuild clean # at times it is needed
lein cljsbuild auto dev # dev build only
lein ring server-headless # from a new terminal
```

Disable the browser JavaScript and visit the [login-dbg.html][4]
page. Remember that we removed the `novalidate` attribute flag from the
`loginForm`. Now click the `Login` button without having filled any
fields.

If you requested the page from an HTML5 browser, it will ask you to fill
in the email address field with a well-formed email. Do it and click again
on the `Login` button. The browser will now asks you to fill in the password
field by typing a password with length between 4 and 8 digits and with at
least 1 numeric digit. Do it and click the `Login` button again.

If everything has gone well, the browser should now show you a message
saying something like this: *xxxx.yyyy@gmail.com and zzzz1 passed
the formal validation, but you still have to be authenticated*.

Great. We got what we expected.

Now add again the `novalidate` attribute flag to the `loginForm` in the
`login-dbg.html` source code, reload the page and click the `Login`
button without having filled any field. The browser should now show you
the *Please complete the form* message.

Great. We got what we expected.

Now enable the JavaScript, reload the [login-dbg.html][4] and click the
`Login` button again. You should see the *Please complete the form*
message shown at the bottom of the login form. Try to type an invalid
email address and click the button again. You should now see the *Type a
well formed email!* shown at the top of the login form.

Great. We got again what we expected.

We still have a lot of work to be done, but we were able to implement 3
of the 4 layers of the progressive enhancement approach for web
application development. And we used clojure on the server-side and
clojurescript on the client-side. Not bad so far.

We already know from the [10th Tutorial][8] how to implement the ajax
layer. Before doing that by copying the Shopping Calculator sample
already done, in the next couple of tutorials we're going to cover a
couple of entirely new topics.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "highest and deepest"
```

# Next Step - [Tutorial 13: Don't Repeat Yourself while crossing the border][9]

One of our long term objectives is to eliminate any code duplication
from our web applications.  That's like saying we want to stay as
compliant as possible with the Don't Repeat Yourself (DRY)
principle. In the [next tutorial][9] we're going to exercise the DRY
principle while adding a validator library to the project.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md
[2]: https://github.com/levand/domina
[3]: https://github.com/magomimmo/hiccups
[4]: http://localhost:3000/login-dbg.html
[5]: https://github.com/magomimmo/compojure
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[7]: https://github.com/magomimmo/ring
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-13.md
[10]: https://help.github.com/articles/set-up-git
