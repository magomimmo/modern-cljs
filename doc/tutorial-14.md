# Tutorial 14 - It's better to be safe than sorry (part 1)

In this tutorial we're going to prepare the field for one of the main
topic in software development and methodologies: code testing.

Code testing is a kind of continuum which goes from zero to almost full
test coverage.

I'm not going to open those kind of discussions which have a never
ending story and go nowhere. Code test is needed, full stop. How much
coverage? It depends.

I started programming almost 30 years ago and I had the luckiness of
starting with Prolog and then on a wonderful Lisp Machine, which I still
miss a lot today. I never started a program from a test that has to fail
to progress until it succeeds. That's because I had my Lisp REPL to
follow my thoughs and to correct them on the fly. But when you're aged
like I'm, you can't change anymore your habits. So, who's religious
about TDD/BDD (Test Driven Development and Behavioural Driven
Development), has to be forgiven with me.

## Introduction

In the last [tutorial][1] I hope you had some fun in seeing the DRY
principle at work while adhering to the progressive enhancement
strategy. Before to go on by affording the problem of testing your
CLJ/CLJS code, we have to fullfill something we left behind. In the
[tutorial 10 - Introducing Ajax][2] we implemented a Shopping Calculator
by using the Ajax style of communication between the browser and the
server, said otherwise, between ClojureScript on the client side and
Clojure on the server side. Obviously, nobody will ever implement that
kind of widget by using Ajax, if all the information he needs to make
the calculation are already in his hands on the browser side. By moving
the calculation from the client side to the server side, we found an
excuse to gently introduce you to Ajax. After all this is a series of
tutorial on CLJS not on CLJ, but we still have broken the first
principle of the progressive enhancement strategy which dictates to
start developing your web application by implementing it as if
JavaScript would not be available on the browser of the users. That
said, the main advantage of CLJ and CLJS is that you have an unified
language for both sides of the world, and this language is, in my humble
opinion, much better and fun than the JS/Node.js pair. So, we need to
fill the gap, even if this means to move ourself from CLJS to CLJ.

## Review the Shopping Calculator

To review the Shopping Calculator program do as follows:

```bash
$ git clone https://github.com/magomimmo/modern-cljs.git
$ cd modern-cljs
$ git checkout tutorial-13
$ lein cljsbuild once prod
$ lein ring server-headless
```

Now visit the [shopping.html][3] page, and click the `Calculate`
button. The `Total` field is updated with the result of the calculation
executed via Ajax on the server-side. As you can see the `shopping.html`
page showed by the address bar of the browser does not change. Only the
result of the `Total` field has been updated, even if the calculation
has been executed by the server.

But what happens if you disable JavaScript? Try it. To disable the
JavaScript engine on Google Chrome, open the `Developer Tools` and click
on the setting's icon positioned in the very right bottom of the
browser. It opens a panel from where you can check the `Disable
JavaScript` check-box. Now close the Setting's panel and reload the
[shopping.html][3] page. First if you move the mouse cursor on the
`Calculate` button nothing happens and nothing happens even if you click
it.

Take a look at the `shopping.html` file which is under the
`resources/public` directory of the `modern-cljs` main directory.

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Shopping Calculator</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
  <!-- shopping.html -->
  <form id="shoppingForm" novalidate>
    <legend> Shopping Calculator</legend>
    <fieldset>
    ...
    ...
      <div>
        <input type="button"
               value="Calculate"
               id="calc">
      </div>
    </fieldset>
  </form>
  <script src="js/modern.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

As you can see the `form` tag has no `action` and `method` setted and
`type` attribute of the Calculate `input` is `button`.

## Break the Shopping Calculator

Now modify the `form` by adding the `action` and the `method` attribute
and change the Calculate `input type` from `button` su `submit` as
follows.

```html
...
  <form action="shopping" id="shoppingForm" method="post" novalidate>
    <legend> Shopping Calculator</legend>
    <fieldset>
    ...
    ...
      <div>
        <input type="submit"
               value="Calculate"
               id="calc">
      </div>
    </fieldset>
  </form>
  <script src="js/modern.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

Reload the [shopping.html][3] page and click the `Calculate` button. You
should receive a `Page not found` message from the server and see the
`localhost:3000/shopping` URL in the location bar of your browser
instead of the previous  `localhost:3000/shopping.html` URL.

As you remember from the [Tutorial 3 - CLJ based http-server][4], we
defined the application routes as follows

```clojure
(defroutes app-routes
  ;; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ;; to authenticate the user
  (POST "/login" [email password] (authenticate-user email password))
  ;; to server static pages saved in resources/public directory
  (resources "/")
  ;; if page is not found
  (not-found "Page non found"))
```

which explains us why we received the `Page not found` page: our server
does not know anything about the `/shopping` route we added as the value
of the `action` of the Shopping Calculator form.

Furthermore, by having setted the `method` attribute of the `form` to
`post` and the `type` attribute of the Calculate `input` to `submit`, we
asked the browser to send to the server a POST request to the
`/shopping` URL whenever the user click the Calculate button.


# Next step - TBD

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/weavejester/valip
[4]: https://github.com/cemerick
[5]: https://github.com/weavejester
[6]: https://github.com/cemerick/valip/blob/master/README.md
[7]: http:/localhost:3000/login-dbg.html
[8]: https://github.com/cemerick/valip/blob/master/README.md
[9]: http://dev.clojure.org/display/design/Feature+Expressions
[10]: https://github.com/emezeske/lein-cljsbuild
[11]: https://github.com/emezeske
[12]: https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/doc/CROSSOVERS.md
[13]: http://clojuredocs.org/clojure_core/clojure.core/if-let
[14]:https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md#first-try
[15]: https://github.com/emezeske/lein-cljsbuild
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[17]: http://clojuredocs.org/clojure_core/clojure.core/let#example_878
[18]: https://github.com/shoreleave
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/remote-validator.png
