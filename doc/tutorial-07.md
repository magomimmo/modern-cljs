# Tutorial 7 - Being doubly aggressive

In this tutorial we're going to explore CLS compiler optimizations by
using the usual `lein-cljsbuild` plugin of `leiningen`, but we'll
discover a trouble we do not know how to manage yet.

## Introduction

In the [last tutorial][1] we came in contact with `:export` directive
been attached to `init` function. That directive had the scope to
protect that function from being evantually renamed by the Google
Closure (CLS) compiler when used with more aggressive compilation mode
than `:whitespace`, namely `:simple` and `:advanced`.

## Being aggressive as all the others

As we already saw in the very [first tutorial][8] of this series, we have
been using the `lein-cljsbuild` plugin to configure the CLJS compilation
process, by instructing `:cljsbuild` keyword with the following value:

```clojure
(defproject ....
...
...

:cljsbuild {:builds
              [{;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}]})
```

The `:whitespace` compilation mode of the CLS compiler removes all
comments and whitespaces from the JS code emitted by the CLJS
compiler. When paired with `:pretty-print true` directive, `:whitespace`
compilation mode is very effective in supporting programmers during the
development phase, because it prettyfies the emitted JS code in such a
way that you can read it and eventually set breakpoints during debugging
sessions with a browser development tool activated.

The `:simple` compilation mode is a little more aggressive with the
emitted JS code by the CLJS compiler. As a lot of others minifiers, it
basically produces a minified JS code by simplifying expressions and
renaming local variables within functions. Nothing very new.

To activate `:simple` compilation mode all you have to do is to change
`:optmizations` value from `:whitespace` to `:simple`.

Instead of just substituting `:simple` directive to the `:whitespace`
one, as documentated in [sample.project.clj][2], `lein-cljsbuild` is so
nice to allow us to declare more than one build configuration. Here is
our new `project.clj` declaration with two `:builds`, the first one,
named `:dev`, which uses `:whitespace` compilation mode, the second one,
named `:pre-prod`, which uses `:simple` compilation mode.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  ;; clojure source code path
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [domina "1.0.0"]]

  :plugins [; cljsbuild plugin
            [lein-cljsbuild "0.2.10"]
            [lein-ring "0.7.5"]]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              {
               :dev
               {;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               {;; same path as above
                :source-path "src/cljs"

                :compiler {;; different output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optmization
                           :optimizations :simple

                           ;; no need prettyfication
                           }}}})

```

> NOTE 1: When you have more named builds like above, the `:builds`
> value changes from a vector to a map.

As you can see, we now have two build configurations sharing the same
CLJS code base (i.e. "src/cljs"):
* `:dev`, which uses `:whitespace` compilation mode and `:pretty-print`
  option set to `true`. The JS code emitted by CLJS/CLS compilers will be
  saved as `modern_dbg.js` in `resources/public/js` directory;
* `:pre-prod`, which uses `:simple` compilation mode and no
  prettyfication. The JS code emitted by CLJS/CLS compilers will be
  saved as `modern_pre.js` in `resources/public/js` directory;

You can now run both builds togheter by launching the usual `lein
cljsbuild once` or `lein cljsbuild auto` commands from the terminal.

```bash
$ cd /path/to/modern-cljs
$ lein cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern_pre.js" in 15.169253 seconds.
Compiling "resources/public/js/modern_dbg.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern_dbg.js" in 3.202557 seconds.
$
```

Or you can compile just one of them by passing the corresponding
de-keywordized build name as follows:

```bash
$ lein cljsbuild once pre-prod
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern_pre.js" in 14.51913 seconds.
$
```

If you now list your `resources/public/js` directory you can immediately
see the size difference of `modern_dbg.js` and `modern_pre.js`, being the
last 30% less than the former, but still more than 700KB.

```bash
$ ls -lah resources/public/js/
total 3520
drwxr-xr-x  4 mimmo  staff   136B Nov 17 20:23 .
drwxr-xr-x  7 mimmo  staff   238B Nov 17 00:39 ..
-rw-r--r--  1 mimmo  staff   1.0M Nov 17 20:23 modern_dbg.js
-rw-r--r--  1 mimmo  staff   705K Nov 17 20:23 modern_pre.js
$
```

## Being much more aggressive than the others

It's now time to be much more aggressive by enabling the so called `dead
code elimination` through `:advanced` CLS compilation mode.  Here is the
code snippet.

```clojure
(defproject ...
  ...

  :cljsbuild {:builds
              {
               :dev
               {;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :prod
               {;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced}}
               :pre-prod
               {;; some path as above
                :source-path "src/cljs"
                :compiler {;; different output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optmization
                           :optimizations :simple
                           ;; no need prettyfication
                           }}}})

```

Now compile the new build as usual by launching `$ lein cljsbuild once
prod` and then list the content of `resources/public/js`.

```bash
$ ls -lah resources/public/js/
total 3888
drwxr-xr-x  5 mimmo  staff   170B Nov 17 20:46 .
drwxr-xr-x  7 mimmo  staff   238B Nov 17 00:39 ..
-rw-r--r--  1 mimmo  staff   183K Nov 17 20:46 modern.js
-rw-r--r--  1 mimmo  staff   1.0M Nov 17 20:46 modern_dbg.js
-rw-r--r--  1 mimmo  staff   705K Nov 17 20:46 modern_pre.js
$
```

We reached 183KB and, if you gzip it, you'll reach 41KB, almost the same
size of the jquery minified and gzipped version, which is 32KB.

## Housekeeping

We now have three different JS generated files, which means we should have
three different versions for each html page of our small CLJS samples
and each of them should have a `script` tag pointing to the right JS
version. Not a big deal, but still something to care of.

```bash
$ cp resources/public/login.html resources/public/login-dbg.html
$ cp resources/public/login.html resources/public/login-pre.html
$ cp resources/public/shopping.html resources/public/shopping-dbg.html
$ cp resources/public/shopping.html resources/public/shopping-pre.html
```
Now edit `login-dbg.htnl`, `login-pre.html`, `shopping-dbg.html` and
`shopping-pre.html` to update the corresponding `script` tag as follows:

`login-dbg.html`

```html
<!doctype html>
<html lang="en">
<head>
...
...
</head>
<body>
...
...
    <script src="js/modern_dbg.js"></script>
    <script>
      modern_cljs.login.init();
    </script>
</body>
</html>
```

`login-pre.html`

```html
<!doctype html>
<html lang="en">
<head>
...
...
</head>
<body>
...
...

    <script src="js/modern_pre.js"></script>
    <script>
      modern_cljs.login.init();
    </script>
</body>
</html>
```

`shopping-dbg.html`

```html
<!doctype html>
<html lang="en">
<head>
...
...
</head>
<body>
...
...

  <script src="js/modern_dbg.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

`shopping-pre.html`

```html
<!doctype html>
<html lang="en">
<head>
...
...
</head>
<body>
...
...

  <script src="js/modern_pre.js"></script>
  <script>
    modern_cljs.shopping.init();
  </script>
</body>
</html>
```

You're now ready to launch your samples from `modern-cljs` directory as
usual (e.g. `$ lein ring server`, `lein cljsbuild auto` and `lein
trampoline cljsbuild repl-listen`) and then to visit the debugging,
pre-production and production versions of the above pages.

Take into account that using the browser as an
[evaluation environment][3] is discouraged with `:advanced` mode. If you
try to evalutate a CLJS expression from the brepl when you're visiting
the production versions of the pages (i.e. `login.html` and
`shopping.html`) the brepl will hang up and this brings us to the next
paragraph.

## Get in trouble

In [tutorial 2][4] and [tutorial 3][5] we introduced the browser
connected repl (brepl) as a CLJS evalutation environment enabling a very
productive and interactive style of programming. To reach this objective
we created a `connect.cljs` file where we called `(repl/connect
"http://localhost:9000/repl")` to establish the server side of the
brepl connection, being the JavaScript virtual machine of the browser
the client side of the connection itself.

Having an active brepl connection is a great thing during development
and testing phases, but for security reasons it would be better not to
have it in a production envirinoment. Aside from the fact that, as we
have just seen, the `:advanced` compilation mode left hanging the brepl
connection, it would be nice to have a way to explicitly **exclude** the
`connect.cljs` file containing the connection call from the build
(i.e. `:prod`) dedicated to the production environment. Sadly, both
[`lein-cljsbuild`][6] and [`clojurescript`][7] do not offer this kind of
feature and, more generally, the eventuality to exclude any CLJS file
from a build driven by `lein-cljsbuild`.

The main consequence of this missing feature is that you can't share the
same CLJS code base (i.e. `:source-path "src/cljs"`) and simultaneously
have more builds that differently filter the code base istself, causing
a maintenance headache due to code duplication.

> FINAL NOTE: Me and my students Federico Boniardi and Francesco Agozzino
> patched `lein-cljsbuild` to extend its build options in such a way that
> it's able to exclude CLJS files-or-dirs from been compiled. As soon as
> we have time to submit the patch, we hope it will be merged in a next
> `lein-cljsbuild` release.

# Next step - Introducing Domina events.

In the next [Tutorial 8][9] we're going to introduce domina events
which, by wrapping Google Closure Library event management, allows to
follow a more clojure-ish approach in handing DOM events.

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-06.md
[2]: https://github.com/emezeske/lein-cljsbuild/blob/0.2.9/sample.project.clj
[3]: https://github.com/clojure/clojurescript/wiki/The-REPL-and-Evaluation-Environments
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[5]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[6]: https://github.com/emezeske/lein-cljsbuild/issues/157
[7]: http://dev.clojure.org/jira/browse/CLJS-419
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-08.md
