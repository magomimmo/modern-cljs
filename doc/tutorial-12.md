# Tutorial 12 - Be friendly with ajax (Part 1)

In the [latest tutorial][1] we saw few Domina features about DOM events
which helped us to the desire of adhering to the progressive enhancement
strategy in developing a clojurean web application.  By implementing,
via CLJS, the *javascript* layer, in the previous tutorial we only
covered one the four typical layers of that stategy. We still need to
implement the top most layer, the HTML5 layer, and the two deeper ones:
the Ajax layer and the *pure* server side layer.

# Introduction

In this and the next tutorials we're going to cover the two deeper
missing layers from `loginForm` example, leaving a detailed care of the
*superficial* HTML5 layer to anyone who likes to play with it. The only
two exceptions are the new `title` and `pattern` HTML5 attributes which
we are going to use instead of the corresponding patterns and help
messages we previously screwed in the `validate-email` and
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
> `pre-prod` and the `prod` builds defined in the `project.clj` file.

As you can see we removed the `novalidate` HTML5 attribute from the form
and added `placeholder`, `title` and `pattern` HTML5 attributes to both
`email` and `password` HTML5 `input` field elements of the
form.

Uhm, the kind of code duplication we don't like at all. So, let's modify
the `validate-email` and `validate-password` handlers in such a way that
instead of having those patterns and help messages screwed in the source
code, we can get them from the input elements attributes by using the
`attr` function defined in the `domina` namespace. Following is the
fragment of the updated code from `login.cljs`.

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
> [hiccups][2] library you're now forced to require the `hiccups.runtime`
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

Visit [login-dbg.html][3] and verify the `:blur` and the `:click`
handlers are still working as in the [previous tutorial][4].

Now disable the JavaScript of your browser, reload the page and click
the `Login` button without having filled the email and the password
fields. If you're using an HTML5 browser you should see a message saying
you to fill out the email field with the added text of the `title`
attribute previously set up in the form. If you don't use an HTML5
browser, the new attributes are just ignored and the process goes on by
calling the still inexistent `login.php` server-side script and you get
the usual `not found page`.

It's now time to take care of it by implementing the server-side login
service which represents the deepest and default layer of the
progressive enhancement strategy.

# The deepest layer

As you already know, this is a CLJS tutorial, not a CLJ tutorial. There
are already a quite large numbers of very good tutorials on CLJ web
application develpment.



Friend security

- channel security
- user agent Authentication (form hhtp basic and openid
- role-base Authentication
- su capability


Spring security

- web layer (Spring MVC)
- Service Layer (Spring Core)
- DataAccess Layer (Spring ORM(JPA)

- Authentication
- Authorization
- DB credential security

1. Map from authitecicated users to one or more roles
2. Association of resourse to roles

Spring secuirty

1. Segment users into urser classes
2. assign levels of autohirzation to roles
3. Assign user roles to user classes


bla bla

# Introduction

bla bla

# Next step - TBD

TBD

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-11.md


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
[17]: http://clojure.org/vars
[18]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/help-01.png
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/completheform-01.png
[20]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/allhelp.png
[21]: https://github.com/teropa/hiccups
[22]: https://github.com/cemerick/friend
[23]: http://www.the-art-of-web.com/javascript/validate-password/#.UPvsHaGjejI
[24]: http://regexlib.com/Search.aspx?k=password&c=-1&m=-1&ps=20
