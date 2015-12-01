# Tutorial 6 - The Easy Made Complex and the Simple Made Easy

In this tutorial we are going to investigate the issue we met in the
[previous tutorial][1] and try to solve it.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][9] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-05
```

## Introduction

Our latest tutorial ended with a not so nice error.  We discovered
that as soon as we have two HTML pages linking the same `js/main.js`
generated JS file, the `init` function we set for the `onload`
property of the JS `window` object for the `login.html` page was not
the one we defined in `login.cljs`, but the one we defined in
`shopping.cljs`.

As we anticipated in the [previous tutorial][1], this behavior
depends on the Google Closure/CLJS pair of compilers driven by the
`boot-cljs` task.

## Introducing Google Closure Compiler (GCSL)

In the [first tutorial][3], we set `:source-paths` in the `build.boot`
file to the `#{src/cljs}` path.

The `:source-paths` directive instructs Google Closure/CLJS pair of
compilers to look for any CLJS source code in the `src/cljs` directory
structure for doing its job.

I'm not going to explain every single detail of the CLJS/GCSL pair of
compilers. The only detail that is useful for investigating and
eventually solving the above issue is that the pair of compilers
generates a **single** JS file (i.e., `js/main.js`) from **all** of
the CLJS files it finds in the `src/cljs` directory and subdirectories
(i.e., `core.cljs`, `login.cljs`, and `shopping.cljs`).

## Is mutability evil?

Both `login.cljs` and `shopping.cljs` have a final call to `(set!
(.-onload js/window) init)`, which is therefore called twice: once
from `login.cljs` and once from `shopping.cljs`. The order of these
calls is critical, because whichever comes first, the other is going
to overwrite the previous value: a clear case against JS mutable data
structures?

## Easy made complex

From the above discussion the reader could infer that that CLJS is
good only for *Single Page Application* (SPA). Indeed, there is a very
modest solution to the above conflict between more calls setting the
same `onload` property of the JS `window` object: code duplication!

You have to duplicate the directory structure and the corresponding
build options for each html page that is going to include the single
generated JS file.

I don't know about you, but if there is a things that I hate more than
a WARNING notification by a compiler is code duplication. So, I'm not
even going to explain how to duplicate your code to modestly solve the
above error.

## Simple made easy

Now the simple made easy way:

* remove the call `(set! (.-onload js/window) init)` from both
  `login.cljs` and `shopping.cljs` files;
* add the `:export` tag (metadata) to the `init` function in both
  `login.cljs` and `shopping.cljs` files;
* add a `script` tag calling the correponding `init` function in both
  `login.html` and `shopping.html` files;
* you're done.

> NOTE 2: If you do not `^:export` a CLJS function, it will be subject
> to Google Closure Compiler `optimizations` strategies. When set to
> `simple` or `advanced`, the GCSL compiler will minify the emitted JS
> file and any local variable or function name will be
> shortened/obfuscated and won't be available from external JS
> code. If a variable or function name is annotated with `:export`
> metadata, its name will be preserved and can be called by standard
> JS code. In our example the two functions will be available as:
> `modern_cljs.login.init()` and `modern_cljs.shopping.init()`.

Here is the related fragment of `login.cljs`

```clj
;; the rest as before
(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; validate-form function
    (let [login-form (.getElementById js/document "loginForm")]
      (set! (.-onsubmit login-form) validate-form))))

;; (set! (.-onload js/window) init)
```

And here is the related fragment of `shopping.cljs`

```clj
;; the rest as before
(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (let [the-form (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit the-form) calculate))))

;; (set! (.-onload js/window) init)

```
Here is the related fragment of `login.html`

```html
    <script src="js/main.js"></script>
    <script>modern_cljs.login.init();</script>
```

And here is the related fragment of `shopping.html`

```html
  <script src="js/main.js"></script>
  <script>modern_cljs.shopping.init();</script>
```

## Tradeoffs at work

As you see, by inserting a script snippet in the HTML pages, we're
violating the unobtrusive principle expressed by a lot of webapps
designers. The life is full of compromises and this is one of those
tradeoffs.

All those changes could have been done while the IFDE is running. But
there is one more thing we want to take care of and we can't do it
while the IFDE is running.

As you noted, to adhere to the convention of keeping any JS resourses
confined in a `js` subdirectory of the directory serving HTML pages, in
[Tutorial-03][4] we had to create the `html/js/main.cljs.edn`
file.

Moreover, anytime we create/delete a CLJS namespace, we have to
maintain the `require` section of that file. This is a clear case of
*incidental complexity* introduced by the `boot.cljs` task.

Hopefully some day the `boot-cljs` maintainers will solve this issue
in a less convoluted way. In the meantime, to bypass that incidental
complexity, we are going to violate the above convention. A second
tradeoff. Keep this two tradeoffs in your memory, because you got two
debits that one day or the other you're going to pay for.

Let's apply this second tradeoff.

First, delete the `html/js/main.cljs.edn` file.

```bash
cd /path/to/modern-cljs
rm -rf html/js
```

Now edit both the `html/index.html` and the `html/shopping.html` files
to reset the `src` attribute of their `<script>` tag from `js/main.js`
to `main.js`.

```html
<!doctype html>
<html lang="en">
...
<body>
...
    <script src="main.js"></script>
    <script>modern_cljs.login.init();</script>
</body>
</html>
```

```html
<!doctype html>
<html lang="en">
...
<body>
...
    <script src="main.js"></script>
    <script>modern_cljs.shopping.init();</script>
</body>
</html>
```

One last thing. In the [first tutorial][3] of this series we created
the `core.cljs` source file in the `src/cljs/modern_cljs`
directory. It only prints `Hello, world!` at the console of the
browser and it was created just as a kind of a placeholder for making
our IFDE working. We do not need it anymore and you can safely delete
it.

```bash
rm src/cljs/modern_cljs/core.cljs
```

## Launch IFDE

You can now start the IFDE as usual:

```bash
boot dev
...
Compiling ClojureScript...
• main.js
Elapsed time: 35.941 sec
```

Then visit the http://localhost:3000 and the
http://localhost:3000/shopping.html URLs for verifying the two forms
are now working as expected.

As usual, if you want to play with the bREPL, launch it as usual and
then reload on of the above URLs.

```clj
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> (start-repl)
...
cljs.user=>
```

You can now stop any `boot` related process and reset your git repository.

```bash
git reset --hard
```

# Next step - [Tutorial 7: Introducing Domina Events][8]

In the [next tutorial][8] we'll introduce [domina event][6] management
to further improve our functional style in porting
[Modern JavaScript samples][7] to CLJS.

# License

Copyright © Mimmo Cosenza, 2012-2015. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-05.md
[2]: http://localhost:3000/shopping.html
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-01.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
[5]: https://github.com/emezeske/lein-cljsbuild
[6]: https://github.com/levand/domina#event-handling
[7]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-07.md
[9]: https://help.github.com/articles/set-up-git
