# Tutorial 11 - HTML on Top, Clojure on the Bottom

In the [previous tutorial][1], we used DOM events to further the
progressive enhancement strategy of our Clojurean web application. By
implementing (via CLJS) the *JavaScript* layer in the previous
tutorial, we only covered one of the four typical layers of that
strategy. We still need to implement the topmost layer, the HTML5
layer, and the two lowest ones: the Ajax layer and the *pure*
server-side layer.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][10] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-10
git checkout -b tutorial-11-step-1
```

# Introduction

In this tutorial we're going to cover the highest and lowest layers
of the login form example we started to cover in the
[previous tutorial][1]. We leave the details of the highest layer
(i.e., HTML5) to anyone who wants to play with it. Here we only want to
introduce the new `title` and `pattern` HTML5 attributes, which we will
use by pairing the corresponding patterns and help messages
previously used by the `validate-email` and `validate-password`
functions.

# Launch IFDE

Launch IFDE as usual

```bash
boot dev
...
Elapsed time: 20.296 sec
```

Visit the [`index.html`](http://localhost:3000/index.html) page and
then run the bREPL

```bash
boot repl -c
...
boot.user> (start-repl)
...
cljs.user> 
```

We are now ready for the frist step.

# The highest surface

Here is the relevant fragment of the updated `index.html` introducing
the above HTML5 new features.

```html
<html>
...
    <form action="login.php" method="post" id="loginForm">
           ...
            <div>
              <label for="email">Email Address</label>
              <input type="email" name="email" id="email"
                     placeholder="email"
                     title="Type a well-formed email!"
                     pattern="^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$"
                     required>
            </div>

            <div>
              <label for="password">Password</label>
              <input type="password" name="password" id="password"
                     placeholder="password"
                     title="Password is from 4 to 8 chars with 1 number!"
                     pattern="^(?=.*\d).{4,8}$"
                     required>
            </div>
...
</html>
```

As you can see, we removed the `novalidate` HTML5 attribute from the
form and added the `placeholder`, `title` and `pattern` HTML5
attributes to both the `email` and the `password` HTML5 input field
elements of the form.

As usual, as you save the file, IFDE will reload it. 

The `pattern` regex values are a kind of code duplication I really
hate. Luckily we have a handy solution based on the
[domina library][2]. Let's modify the `validate-email` and
`validate-password` handlers in such a way that, instead of having
those patterns and help messages defined in the source code, we can
get them from the input elements attributes by using the `attr`
function defined in the `domina.core` namespace.

## bREPLing with HTML5 new attributes

Go the bREPL and familiarize with the `pattern` attributes and the
corresponding regex. Remember that CLJS regex are JS regex.

First require the `domina.core` namespace, ask for the `attr`
docstring and get the value of the `pattern` and `title` attributes:

```clj
cljs.user> (require '[domina.core :as dom])
nil
cljs.user> (doc dom/attr)
-------------------------
domina.core/attr
([content name])
  Gets the value of an HTML attribute. Assumes content will be a single node. Name may be a stirng or keyword. Returns nil if there is no value set for the style.
nil
cljs.user> (dom/attr (dom/by-id "email") :pattern)
"^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$"
cljs.user> (dom/attr (dom/by-id "email") :title)
"Type a well-formed email!"
```

We are now ready to substitute the previous code with the one getting
regexp and help message directly from the Login Form.

## login.cljs

Open the `login.cljs` file and edit it as follows:

```clj
(ns modern-cljs.login
  (:require [domina.core :refer [append!
                                 by-class
                                 by-id
                                 destroy!
                                 prepend!
                                 value
                                 attr]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))

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

> NOTE 1: we added the `attr` symbol to the `:refer` section of the
> `domina.core` requirement and that we deleted both `*email-re*` and
> `*password-re*` previous definitions.

As usual, check that the Login Form is still working as expected.

## HTML5 Validation

Now disable JavaScript in your browser, reload the page and click
the `Login` button without having filled the email and the password
fields.

If you're using an HTML5 browser you should see a message telling you to
fill out the email field and the text specified as the value of the
`title` attribute previously set up in the form.

If you don't use an HTML5 browser, the new attributes are just ignored
and the process continues by calling the still non-existent
`login.php` server-side script, resulting in the usual `Not found page`
from the [compojure][5] routes we defined in the [3rd Tutorial][6].

You can simulate this last effect with an HTML5 compliant browser by
re-adding the `novalidate` attribute to `loginForm`.

It's now time to take care of this error by implementing the
server-side login service, which represents the deepest layer of the
progressive enhancement strategy -- the one we should have started
from in a real web application development when we want to follow the
progressive enhancement strategy.

Kill any `boot` related process and commit the work you did until now.

```bash
git commit -am "get pattern attr from the form"
```

# The deepest layer

As you already know, this series of tutorials is about CLJS, not CLJ. On
the net there are already some very good tutorials on CLJ web
application develpoment using [ring][7] and [compojure][5]. Despite
this, I think we should at least provide a starting point and a track
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

Now, after any HTML5 field validation has succeeded, the browser is
going to request the `/login` resource from the server using the
`POST` method, passing the `email` and `password` values
provided by the user.

We should now take a look at `core.clj` where the ring
request handler and the compojure routes have been defined. The received
`POST /login` request traverses the various ring middleware layers to finally
reach `app-routes` which has to handle the request and provide a response to be returned to
the browser.

At the moment, `app-routes` doesn't yet know how to manage a `POST` method
request. Add a new `POST` route to `app-routes`.

Following is the updated `core.clj` source code.

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
  (not-found "Page not found"))

(def handler
  (site app-routes))
```

Whenever the web server receives a POST request with the "/login"
URI, the `email` and `password` parameters are extracted from the body
of the POST request and the `authenticate-user` function will be called
with those parameters.

> NOTE 4: We intend to define the `authenticate-user` function in the new
> `modern-cljs.login` namespace which has to be required in the
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
  (if (re-matches *re-email* email)
    true))

(defn validate-password [password]
  (if (re-matches *re-password* password)
    true))
```

There are a lot of bad things in this code. The most obvious is
the code duplication between the client and the server code. For the
moment, be forgiving and go on by verifying this code works.

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

If you're using an HTML5 browser, it will ask you to fill
in the email address field with a well-formed email. Do it and click again
on the `Login` button. The browser will now ask you to fill in the password
field by typing a password with length between 4 and 8 digits and with at
least 1 numeric digit. Do it and click the `Login` button again.

If everything has gone well, the browser should now show you a message
saying something like this: `xxxx.yyyy@gmail.com and zzzz1 passed
the formal validation, but you still have to be authenticated`.

Great. We got what we expected.

Now add again the `novalidate` attribute flag to the `loginForm` in the
`login-dbg.html` source code, reload the page and click the `Login`
button without having filled any field. The browser should now show you
the *Please complete the form* message.

Great. We got what we expected.

Now enable JavaScript, reload [login-dbg.html][4] and click the
`Login` button again. You should see the *Please complete the form*
message shown at the bottom of the login form. Try to type an invalid
email address and click the button again. You should now see the *Type a
well-formed email!* shown at the top of the login form.

Great. We again got what we expected.

We still have a lot of work to do, but we were able to implement 3
of the 4 layers of the progressive enhancement approach for web
application development. And we used Clojure on the server-side and
ClojureScript on the client-side. Not bad so far.

We already know from the [10th Tutorial][8] how to implement the Ajax
layer. Before doing that by copying the Shopping Calculator sample
already done, in the next couple of tutorials we're going to cover a
couple of entirely new topics.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "highest and deepest"
```

# Next Step - [Tutorial 13: Don't Repeat Yourself][9]

One of our long-term objectives is to eliminate any code duplication
from our web applications. That's like saying we want to
comply with the Don't Repeat Yourself (DRY)
principle. In the [next tutorial][9] we're going to exercise the DRY
principle while adding a validator library to the project.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-10.md
[2]: https://github.com/levand/domina
[3]: https://github.com/magomimmo/hiccups
[4]: http://localhost:3000/index.html
[5]: https://github.com/magomimmo/compojure
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
[7]: https://github.com/magomimmo/ring
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-12.md
[10]: https://help.github.com/articles/set-up-git
