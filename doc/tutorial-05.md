# Tutorial 5 - Introducing Domina

In this tutorial we're going to introduce [Domina][1] to improve the
idiomaticity of the validation sample of the login form we presented in
the [previous tutorial][2].

## Introduction

In the [previus tutorial][2] we started coding in CLJS directly
translating from JS to CLJS by using [JS interop][7] features of CLJS. Now
it's time to try something better.

> [Domina][1] is a jQuery inspired DOM manipulation library for
> ClojureScript. It provides a functional, idiomatic Clojure interface
> to the DOM manipulation facilities provided by the Google Closure
> library...  While Domina does not provide any innovations, attempts to
> provide a basic functional interface to DOM manipulation that feels
> natural in ClojureScript.

When I first met [clojurescriptone][3] in searching for a CLJS guide,
and read about [Design and templating][4], I found myself very much in
agreement with the following reasoning:

> Many Clojure web applications use [Hiccup][5] for HTML templating. If
> the programmer is also the designer, then Hiccup is ideal. However,
> most developers are bad at design. We need to work with people who are
> good at design and who don't need to care about Clojure. ClojureScript
> One proposes one approach to templating which allows designers to work
> with HTML, CSS and images without having to set an eye on Hiccup data
> structures or those pesky parentheses.

[`login.html`][8] is going to be our pure HTML/CSS template and `domina`
is going to be our CLJS library to interface the DOM of `login.html` in
a more clojure-ish style.

## Add domina to projct dependencies

As usual when using leiningen, to add a library to a CLJ/CLJS project,
you need to add it to the dependencies section of `project.clj`. Here is
the updated version of `project.clj`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ; clojure source code path
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 ; compojure dependency
                 [compojure "1.1.3"]
                 ; domina dependency
                 [domina "1.0.0"]]
  :plugins [; cljsbuild plugin
            [lein-cljsbuild "0.2.9"]
            ; ring plugin
            [lein-ring "0.7.5"]]
  ; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}
  ; cljsbuild tadks configuration
  :cljsbuild {:builds
              [{; clojurescript source code path
                :source-path "src/cljs"
                ; Google Closure Compiler options
                :compiler {; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"
                           ; minimum optimization
                           :optimizations :whitespace
                           ; prettyfying emitted JS
                           :pretty-print true}}]})
```

## Domina selectors

[Domina][1] offers more selector functions: `xpath`, in `domina.xpath`
namespace, and `sel`, in `domina.css` namaspace. But it also features
`by-id`, `value` and `set-value!` functions defined in `domina` core
namespace, which is the one we're going to use.

The nice thing about domina `(by-id id)`, inherited by the unferlying
Googgle Closure library on which `domina` is implemented, is that it
takes care of verifying if the passed argument is a string. As we
anticipated, `domina` core namespace offers other useful functions we're
going to use: `(value el)`, which returns the value of the passed
element, and `(set-value! el value)` which sets its value.

> Note 1: when a function modify an argument passed to it, by clojure
> naming convetion the bang "!" is added at the end of the function
> name.

> Note 2: when you need to :use or :require a namespace, CLJS imposes
> using the :only form of :use and the :as form of :require.

## Modify validate-form

In this step we're going to modify the namespace declaration and
`validate-form` function definition substituting `.getElementById` and
`.-value` JS interop with the corresponding domina `by-id` and `value`
functions.

Open `login.cljs` file from `src/cljs/modern_cljs` directory of the
project and modify both its `namespace` declaration and `validate-form`
function definition as follows:

```clojure
(ns modern-cljs.login
  (:use [domina :only [by-id value]]))

(defn validate-form []
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

As you can see, using `domina` the code is now more fluid than before.
Leave the rest of the file as it was. To check that everything went well
do as follows:

```bash
$ cd /path/to/modern-cljs
$ lein ring server
$ lein cljsbuild once # or auto to trigger automatic recompilation
$ lein trampoline cljsbuild repl-listen
```

Open your browser at `localhost:3000/login.html` and after few moments,
when the CLJS repl becomes responsive having established the connection
with the browser, try the following at the REPL prompt:

```bash
ClojureScript:cljs.user> (in-ns 'modern-cljs.login)

ClojureScript:modern-cljs.login> validate-form
#<function validate_form() {
  var email__6289 = domina.by_id.call(null, "email");
  var password__6290 = domina.by_id.call(null, "password");
  if(function() {
    var and__3822__auto____6291 = cljs.core.count.call(null, domina.value.call(null, email__6289)) > 0;
    if(and__3822__auto____6291) {
      return cljs.core.count.call(null, domina.value.call(null, password__6290)) > 0
    }else {
      return and__3822__auto____6291
    }
  }()) {
    return true
  }else {
    alert("Please, complete the form!");
    return false
  }
}>
ClojureScript:modern-cljs.login>
```

The evalutation of `validate-form` symbol returns the JS function
definition attached by the CLJS compiler to the symbol itself. If you
now try to call the function `(validate-form)`, you should see the
browser alert window asking you to complete the form; click the `ok`
button and you'll see `(validate-form)` returning `false`.

```bash
ClojureScript:modern-cljs.login> (validate-form)
false
ClojureScript:modern-cljs.login>
```

Fill both `Email Address` and `Password` fields of the login form. At
the CLJS repl prompt, call `(validate-form)` again. You should now see
`(validate-form)` returning `true`.

```bash
ClojureScript:modern-cljs.login> (validate-form)
true
ClojureScript:modern-cljs.login>
```

# Next Step

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/levand/domina
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[3]: https://github.com/brentonashworth/one
[4]: https://github.com/brentonashworth/one/wiki/Design-and-templating
[5]: https://github.com/weavejester/hiccup
[6]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[7]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md#porting-to-clojurescript
