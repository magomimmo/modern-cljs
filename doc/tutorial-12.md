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
missing layers of our `loginForm` example, leaving the *superficial*
HTML5 layer to anyone who likes to play with it. The only two exceptions
regard the new `title` and `pattern` HTML5 attributes which are going to
be used instead of the corresponding patterns and help messages we
previously screwed in the `validate-email` and `validate-passord`
functions.

Here ithe update `login-dbg` html.

```html

```
We're going to
use the to
extract from the email and password validator functions both the email
and password regular expressions and the corresponding help messages to
be communicated to the user when the provided email and password do not
match them.


adding few attributes to the input fields of the `loginForm`, then we'll
step over the already implemented javascript layer to add the third
layer, the Ajax one. Finally, we're going to implement the server side
the, the one that we all started from in the 90's.

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
