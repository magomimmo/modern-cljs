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
and added `placeholder`, `title` and `pattern` HTML5 attributes to bot
`email` and `password` HTML5 `input` field elements of the form.

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

NOTE 2: by using a CLJS expression inside the `html` macro from
[hiccups][2] library you're now force to required the `hiccups.runtime`
namespace even if we do explicitly use any function from it.

NOTE 3: We used `re-pattern` function to convert the  read in strings from
`:tile` and `:pattern` attributes as regular expressions to be passed to `re-matches`.

As usual, we have to check our progression by running the application.

```bash
$ lein cljsbuild clean # sometimes it is needed
$ lein cljsbuild auto dev
$ lein ring server-headless # from a new terminal
```

Visit [login-dbg.html][3] and verify the `:blur` and the `:click`
handlers are still working has expected.

Now disable the JavaScript of your browser, reload the page and click
the `Login` button without having typed in anything in the email and
in the password fields. If you have an HTML5 browser you should see a
message saying you to fill out the field and the value of the title
attribute we previously set up in the form. Note the input validator do
not get fired when an input field loses focus, but just when you click
the `login` button to submit the form.

# The HTML5 layer

As you already now I'm really far away from being an HTML designer. So,
don't ask me to be what I'm not and what I don't want to be anyway.

HTML5 has a lot of new stuff. Here we intend just to add the `pattern`
attribute of the `email` and `password` field of the `loginForm`.

This attribute allows to incorporate a regular expression validator with
those input field and when the user type is his `email` and his
`password` the browser takes care of matchig them with the provided
regular expression as soon as the input fields lose the focus
(i.e. blur).

Here is the updated `login-dbg.html` html code.

```html

```


the test in the field, that text is
matched against the provided regular expression. If the match does not
pass, then the browser makes to the user same evidence of that and it
blocks any progression towards the underlying layers, beeing those the
javascript layer, the ajax one or the server-side action script attached
to the form.



rexex,  to verify that it satisfy
Specifically, by accurately used the `prevent-default` function inside
the `submit` button's event handlers, we were able to interrupt the
process esclalation of the event from the `submit` button itself to the
`action` attribute of the `loginForm`.



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
