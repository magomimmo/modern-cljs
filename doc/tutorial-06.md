# Tutorial 6 - The Easy Made Complex and the Simple Made Easy

In this tutorial we are going to investigate the issue we met in the
[latest tutorial][1] and try to solve it.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][9] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-05
git checkout -b tutorial-06-step-1
```

## Introduction

Our latest tutorial ended with a not so nice issue. Just to recap we did as
follows:

* created the `login.html` page and the corresponding `login.cljs`
  source file;
* created the `shopping.html` page and the corresponding
  `shopping.cljs` source file;
* launched the app in the usual way

```bash
lein ring server # from the project home directory
lein cljsbuild auto # from the project home directory in a new terminal
lein trampoline cljsbuild repl-listen # from the project home directory in a new terminal
```

We than visited the [shopping][2] page in the browser and discovered
that the `init` function we set for the `onload` property of the JS
`window` object was not the one we defined in `shopping.cljs`, but the
one we defined in `login.cljs`.

As we anticipated in the [previous tutorial][1], this behaviour
depends on the Google Closure Compiler driven by the `lein-cljsbuild`
plugin.

## Introducing Google Closure Compiler (CLS)

In the [first tutorial][3], we set the `:cljsbuild` keyword of
`project.clj` to configure the Google Closure Compiler with the following
options:

```clojure
(defproject ....
  ...
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/modern.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})

```

The `:source-paths` option instructs CLS to look for any CLJS source
code in the `src/cljs` directory structure. The `:output-to` option of
the `:compiler` keyword instructs CLS to save the compilation result
in `"resources/public/js/modern.js`.

I'm not going to explain every single detail of the CLJS/CLS pair of
compilers. The only detail that is useful for investigating and
eventually solving the above issue is that the pair of
compilers generates a **single** JS file
(e.g. `"resources/public/js/modern.js"`) from **all** of the CLJS files
it finds in the `"src/cljs"` directory and subdirectories
(e.g. `connect.cljs`, `login.cljs`, and `shopping.cljs`).

## Is mutability evil?

Both `login.cljs` and `shopping.cljs` had a final call to `(set!
(.-onload js/window) init)`, which is therefore called twice: once from
`login.cljs` and once from `shopping.cljs`. The order of these calls
doesn't matter, because whichever comes first, the other is going to
mutate its previous value: a clear case against JS mutable data
structures?

## Easy made complex

From the above discussion the reader could infer that that CLJS is good only
for a *single page browser application*. Indeed, there is a very modest solution
to the above conflict between more calls setting the same `onload` property of
the JS `window` object: code duplication!

You have to duplicate the directory structure and the corresponding
build options for each html page that is going to include the single
generated JS file.

Here are the bash commands you should enter in the terminal.

```bash
mkdir -p src/cljs/{login/modern_cljs,shopping/modern_cljs}
mv src/cljs/modern_cljs/login.cljs src/cljs/login/modern_cljs/
mv src/cljs/modern_cljs/shopping.cljs src/cljs/shopping/modern_cljs/
cp src/cljs/modern_cljs/connect.cljs src/cljs/login/modern_cljs/
cp src/cljs/modern_cljs/connect.cljs src/cljs/shopping/modern_cljs/
rm -rf src/cljs/modern_cljs
```

And here is the modified fragment of `project.clj`

```clojure
(defproject ...
  ...

  :cljsbuild
  {:builds

   ;; login.js build
   {:login
    {:source-paths ["src/cljs/login"]
     :compiler
     {:output-to "resources/public/js/login.js"
      :optimizations :whitespace
      :pretty-print true}}
    ;; shopping.js build
    :shopping
    {:source-paths ["src/cljs/shopping"]
     :compiler
     {:output-to "resources/public/js/shopping.js"
      :optimizations :whitespace
      :pretty-print true}}}})
```

> NOTE 1: To understand the details of the `:cljsbuild` configurations,
> I strongly recommend you read the [advanced project.clj example][4]
> from the [lein-cljsbuild][5] plugin.

Finally you have to include the right JS file (e.g. `"js/login.js"`
and `"js/shopping.js"` in the script tag of each html page
(e.g. `login.html` and `shopping.html`).

Most would call the above solution a kind of **incidental
complexity**. What's worst is the fact that each emitted JS file, no
matter how smart is the CLS compiler is in reducing the total size, is
different from the others: there is no way for the browser to cache the first
downloaded one to locally serve all the others from the cache.

## Simple made easy

Now the simple made easy way:

* remove the call `(set! (.-onload js/window) init)` from both
  `login.cljs` and `shopping.cljs` files;
* add the `:export` tag to the `init` function in both `login.cljs` and
  `shopping.cljs` files;
* add a `script` tag calling the correponding `init` function in both
  `login.html` and `shopping.html` files;
* you're done.

> NOTE 2: if you do not `^:export` a CLJS function, it will be subject
> to Google Closure Compiler `:optimizations` strategies. When set to
> `:simple` optimizations, the CLS compiler will minify the emitted
> JS file and any local variable or function name will be shortened/obfuscated and
> won't be available from external JS code. If a variable or function
> name is annotated with `:export` metadata, its name is going to be
> preserved and can be called by standard JS code. In our example the
> two functions will be available as: `modern_cljs.login.init()` and
> `modern_cljs.shopping.init()`.

Here is the related fragment of `login.cljs`

```clojure
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

And here is the interested fragment of `shopping.cljs`

```clojure
;; the rest as before
(defn ^:export init []
  (if (and js/document
           (.-getElementById js/document))
    (let [theForm (.getElementById js/document "shoppingForm")]
      (set! (.-onsubmit theForm) calculate))))

;; (set! (.-onload js/window) init)

```
Here is the related fragment of `login.html`

```html
    <script src="js/modern.js"></script>
    <script>modern_cljs.login.init();</script>
```

And here is the related fragment of `shopping.html`

```html
  <script src="js/modern.js"></script>
  <script>modern_cljs.shopping.init();</script>
```

You can now run everything as usual:

```bash
lein ring server # from the project home directory
lein cljsbuild auto # from the project home directory in a new terminal
lein trampoline cljsbuild repl-listen # from the project home directory in a new terminal
```

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "Introducing domina"
```

In a subsequent tutorial we'll introduce [domina event][6] management
to further improve our functional style in porting
[Modern JavaScript samples][7] to CLJS.

# Next step - [Tutorial 7: Being doubly aggressive][8]

In the [next tutorial][8] we're going to explore CLJS/CLS compilation modes by
using the usual `lein-cljsbuild` plugin of `leiningen`.

# License

Copyright © Mimmo Cosenza, 2012-2014. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[2]: http://localhost:3000/shopping.html
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[4]: https://github.com/emezeske/lein-cljsbuild/blob/master/example-projects/advanced/project.clj
[5]: https://github.com/emezeske/lein-cljsbuild
[6]: https://github.com/levand/domina#event-handling
[7]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[9]: https://help.github.com/articles/set-up-git
