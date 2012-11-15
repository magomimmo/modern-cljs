# Tutorial 6 - Easy made Complex and Simple made Easy

In this tutorial we are going to investigate the issue we met in the
[last tutorial][1] and try to solve it.

## Introduction

Last tutorial ended with a not so nice issue. Just to recap we did as
follows:

* created the `login.html` page and the corresponding `login.cljs`
  source file;
* created the `shopping.html` page and the corresponding
  `shopping.cljs` source file;
* launched the app in the usual way

```bash
$ lein ring server # from the project home directory
$ lein cljsbuild auto # from the project home directory in a new terminal
$ lein trampoline cljsbuild repl-listen # from the project home directory in a new terminal
```

We than visited the [shopping][2] page in the browser and discovered
that the `init` function we set for the `onload` property of the JS
`window` object was not the one we defined in `shopping.cljs`, but the
one we defined in `login.cljs`.

As we anticipated in the [previous tutorial][1], this behaviour
depends from Google Closure Compiler driven by `lein-cljsbuild`
plugin. 

## Introducing Google Closure Compiler (CLS)

In the [first tutorial][3], we set the `:cljsbuild` keyword of
`project.clj` to configure Google Closure Compiler with the following
options:

```clojure
(defproject ....  
  ...
  :cljsbuild {:builds
              [{:source-path "src/cljs"
                :compiler {:output-to "resources/public/js/modern.js"
                           :optimizations :whitespace
                            :pretty-print true}}]})

```

The `:source-path` option instructs CLS to look for any CLJS source
code in the `src/cljs` directory structure. The `:output-to` option of
the `:compiler` keyword instructs CLS to save the compilation result
in the `"resources/public/js/modern.js` file. 

I'm not going to explain every single detail of CLJS/CLS pair of
compilers. The only detail it is useful to recap to investigate and
eventually to solve the above issue is that the CLSJ/CLS pair of
compilers generates a **single** JS file
(e.g. `"resources/public/js/modern.js"`) from **all** the CLJS files
it finds in `"src/cljs"` directory/subdirectories
(e.g. `connect.cljs`, `login.cljs`, `modern.cljs` and
`shopping.cljs`).

## Is mutability evil?

Both `login.cljs` and `shopping.cljs` had a final call to `(set!
(.-onload js/window) init)`, which is then called two times: once from
`login.cljs` and once from `shopping.cljs`. The order of these calls
doesn't matter, because whichever comes first, the other is going to
mutate its previous value: a clear case against JS mutable data
structure?

## Easy made complex

From the above discussion the reader could start bilieving in the
statement that CLJS is good only for *single page browser
application*. Indeed, there is a very modest solution to the above
conflict between more calls setting the same `onload` property of the
JS `window` object: duplication! 

You have to duplicate the directory structure and the corresponding
build options for each html page is going to include that single
generated JS file.

Here are the bash commands you should enter in the terminal.

```bash
$ mkdir -p src/cljs/{login/modern_cljs,shopping/modern_cljs}
$ mv src/cljs/modern_cljs/login.cljs src/cljs/login/modern_cljs/
$ mv src/cljs/modern_clsj/shopping.cljs src/cljs/shopping/modern_cljs/
$ cp src/cljs/modern_cljs/connect.cljs src/cljs/login/modern_cljs/
$ cp src/cljs/modern_cljs/connect.cljs src/cljs/shopping/modern_cljs/
$ rm -rf src/cljs/modern_cljs
```

And here is the modified fragment of `project.clj`

```clojure
(defproject ...
  ...
  
  :cljsbuild
  {:builds
   
   ;; login.js build
   {:login
    {:source-path "src/cljs/login"
     :compiler
     {:output-to "resources/public/js/login.js"
      :optmizations :whitespace
      :pretty-print true}}
    ;; shopping.js build
    :shopping
    {:source-path "src/cljs/shopping"
     :compiler
     {:output-to "resources/public/js/shopping.js"
      :optmizations :whitespace
      :pretty-print true}}}})
```

> NOTE 1: To understand the details of the `:cljsbuild` configurations,
> I strongly recommend to read the [advanced project.clj example][4]
> from [lein-cljsbuild][5] plugin.

Finally you have to include the right JS file (e.g. `"js/login.js"`
and `"js/shopping.js"` in the script tag of each html page
(e.g. `login.html` and `shopping.html`).

Rich would call the above solution a kind of **incidental
complexity**. What's worst is the fact that each emitted JS file, no
matter how smart is the CLS compiler in reducing the total size of
each generated JS file, is different from the others: there is no way
for the browser to cache the first downloaded one to locally serve all
the others from the cache.

## Simple made easy

Now the simple made easy way:

* remove the call `(set! (.-onsubmit js/document) init)` from both
  `login.cljs` and `shopping.cljs` files;
* add the `:export` tag to the `init` function in both `login.cljs` and
  `shopping.cljs` files;
* add a `script` tag calling the correponding `init` function in both
  `login.html` and `shopping.html` files;
* you're done. 

> NOTE 2: if you do not `^:export` a CLJS function, it will be subject
> to Google Closure Compiler `:optimizations` strategies. When set to
> `:simple` optimizations, the CLS compiler will minified the emitted
> JS file and any local variable or function name will be shorten and
> won't be available from external JS code. If a variable or function
> name is annotated with `:export` metadata, its name is going to be
> preserved and can be called by standard JS code. In our example the
> two functions will be available as: `modern_cljs.login.init()` and
> `modern_cljs.shopping.init()`. 

Here is the interested fragment of `login.cljs`

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
Here is the interested fragment of `login.html`

```html
    <script src="js/modern.js"></script>
    <script>modern_cljs.login.init();</script>
```

And here is the interested fragment of `shopping.html`

```html
  <script src="js/modern.js"></script>
  <script>modern_cljs.shopping.init();</script>
```

> NOTE 3: See NOTE 2 above for an explanation of the exposed function
> names from CLJS to JS.

You can now run everything as usual:

```bash
$ lein ring server # from the project home directory
$ lein cljsbuild auto # from the project home directory in a new terminal
$ lein trampoline cljsbuild repl-listen # from the project home directory in a new terminal
```

## A litle bit of abstrabction

The careful reader will have notice that the `init` function defined
in the `modern-cljs.login` namespace and the one defined in the
`modern-cljs.shopping` namespace are almost identical. They differ
only for the form `id` and the function assigned to `onsubmit`
event. We can then define a more general (i.e. abstract) `init`
function which, receiving both a form `id` and a function as argments,
factorizes implementation of both `init` function, by further reducing
code duplication.

The factorized `init` function, being common to both `login.cljs` and
`shopping.cljs`, will be created in a new namespace, named
`modern-cljs.common`.

Create the `common.cljs` file in `src/cljs/modern-cljs` directory and
write into it the following code.

```clojure
(ns modern-cljs.common)

(defn ^:export init [form-id onload-fn]
  ;; verity that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; validate-form function
    (let [form (.getElementById js/document form-id)]
      (set! (.-onsubmit form) onload-fn))))
```

Now we need to consequently update each pair of html page and CLJS
code to reflect this change.

Here is the interested `login.html` fragment

```html
    <script src="js/modern.js"></script>
    <script>
	  modern_cljs.common.init('loginForm', modern_cljs.login.validate_form);
	</script>
```

> NOTE 4: CLS compiler translates "-" in "_". So
> `modern-cljs.common.init` becomes `mondern_cljs.common.init` and
> `modern-cljs.login.validate-form` becomes
> `modern_cljs.login.validate_form`


And here is the interested `login.cljs` fragment

```clojure
(defn ^:export validate-form []
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))
```

> NOTE 5: You need to `:export` the `validate-form` function name to
> protect it from eventual CLS compiler renaming caused by the `:simple` or
> the more aggressive `:advanced` optimization.

Here is the interested `shopping.html` fragment

```html
    <script src="js/modern.js"></script>
    <script>
		modern_cljs.common.init('shoppingForm', modern_cljs.shopping.validate_form);
	</script>
```

> NOTE 6: See NOTE 4.

And here is the interested `shopping.cljs` fragment

```clojure
(defn ^:export calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))
    false))
```

> NOTE 7: See NOTE 5

If you have not kept everything running in the terminals from the last
work, you can re-start everything as usual:

```bash
$ lein ring server # from the project home directory
$ lein cljsbuild auto # from the project home directory in a new terminal
$ lein trampoline cljsbuild repl-listen # from the project home directory in a new terminal
```

In a subsequent tutorial we'll introduce [domina event][6] management
to further improve our functional style in porting
[Modern JavaScript samples][7] to CLJS.

# Next step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[2]: http://localhost:3000/shopping.html
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[4]: https://github.com/emezeske/lein-cljsbuild/blob/master/example-projects/advanced/project.clj
[5]: https://github.com/emezeske/lein-cljsbuild
[6]: https://github.com/levand/domina#event-handling
[7]: http://www.larryullman.com/books/modern-javascript-develop-and-design/

