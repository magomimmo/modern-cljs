# Tutorial 7 - Compilation Modes

In this tutorial we're going to explore CLS compiler optimizations by
using the usual `lein-cljsbuild` plugin of `leiningen` and we'll
discover a trouble which we will solve by using the latest release of
lein-cljsbuild (i.e. lein-cljsbuild 0.3.2).

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][12] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-06
git checkout -b tutorial-07-step-1
```

## Introduction

In the [previous tutorial][1] we came in contact with `:export` directive
been attached to `init` function. That directive had the role of
protecting the function from being eventually renamed by the Google
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
                :source-paths ["src/cljs"]

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
development phase, because it prettifies the emitted JS code in such a
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
  ...
  :cljsbuild {:builds
              {:dev
               {;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               {;; clojurescript source code path
                :source-paths ["src/cljs"]

                :compiler {;; different output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optmization
                           :optimizations :simple

                           ;; no need prettyfication
                           :pretty-print false}}}})
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
cd /path/to/modern-cljs
lein cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 15.169253 seconds.
Compiling "resources/public/js/modern_dbg.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern_dbg.js" in 3.202557 seconds.
```

Or you can compile just one of them by passing the corresponding
de-keywordized build name as follows:

```bash
lein cljsbuild once pre-prod
Compiling ClojureScript.
Compiling "resources/public/js/modern_pre.js" from ["src/cljs"]...
Successfully compiled "resources/public/js/modern_pre.js" in 14.51913 seconds.
```

If you now list your `resources/public/js` directory you can immediately
see the size difference of `modern_dbg.js` and `modern_pre.js`, the latter being
30% less than the former, but still more than 700KB.

```bash
ls -lah resources/public/js/
total 3520
drwxr-xr-x  4 mimmo  staff   136B Nov 17 20:23 .
drwxr-xr-x  7 mimmo  staff   238B Nov 17 00:39 ..
-rw-r--r--  1 mimmo  staff   1.0M Nov 17 20:23 modern_dbg.js
-rw-r--r--  1 mimmo  staff   705K Nov 17 20:23 modern_pre.js
```

## Being much more aggressive than the others

It's now time to be much more aggressive by enabling the so called `dead
code elimination` through `:advanced` CLS compilation mode.  Here is the
code snippet.

```clojure
(defproject ...
  ...

  :cljsbuild {:builds
              {:dev
               {;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :prod
               {;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced

                           ;; no need prettyfication
                           :pretty-print false}}
               :pre-prod
               {;; clojurescript source code path
                :source-paths ["src/cljs"]
                :compiler {;; different output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optmization
                           :optimizations :simple

                           ;; no need prettyfication
                           :pretty-print false}}}})
```

Now compile the new build as usual by launching `lein cljsbuild once
prod` and then list the content of `resources/public/js`.

```bash
ls -lah resources/public/js/
total 3888
drwxr-xr-x  5 mimmo  staff   170B Nov 17 20:46 .
drwxr-xr-x  7 mimmo  staff   238B Nov 17 00:39 ..
-rw-r--r--  1 mimmo  staff   115K Nov 17 20:46 modern.js
-rw-r--r--  1 mimmo  staff   1.1M Nov 17 20:46 modern_dbg.js
-rw-r--r--  1 mimmo  staff   622K Nov 17 20:46 modern_pre.js
```

We reached 115KB and, if you gzip it, you'll reach 37KB, almost the same
size of the jquery minified and gzipped version, which is 32KB.

> NOTE 2: Serving gzipped files is outside the scope of this tutorial. You can
> read about this topic [here][10] and [here][11].

## Housekeeping

We now have three different JS generated files, which means we should have
three different versions for each html page of our small CLJS samples
and each of them should have a `script` tag pointing to the right JS
version. Not a big deal, but still something to take care of.

```bash
cp resources/public/login.html resources/public/login-dbg.html
cp resources/public/login.html resources/public/login-pre.html
cp resources/public/shopping.html resources/public/shopping-dbg.html
cp resources/public/shopping.html resources/public/shopping-pre.html
```
Now edit `login-dbg.html`, `login-pre.html`, `shopping-dbg.html` and
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
usual (e.g. `lein ring server`, `lein cljsbuild auto` and `lein
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
connected repl (brepl) as a CLJS evalutation environment enabling a
more productive and interactive style of programming. To reach this
objective we created a `connect.cljs` file where we called
`(repl/connect "http://localhost:9000/repl")` to establish the server
side of the brepl connection: the JavaScript virtual machine of the
browser client itself.

Having an active brepl connection is a great thing during development
and testing phases, but for security reasons it would be better not to
have it in a production environment. Aside from the fact that, as we
have just seen, the `:advanced` compilation mode left hanging the brepl
connection, it would be nice to have a way to explicitly **exclude** the
`connect.cljs` file containing the connection call from the build
(i.e. `:prod`) dedicated to the production environment.

## Solve the problem

Starting from the `0.3.2` release, the [lein-cljsbuild][6] plugin has
a new feature which can be used to easily solve the above trouble by
allowing to specify more than one CLJS source directory in the
`:source-paths` compilation option. To **exclude** the `connect.cljs`
file from the production build we have to move it from the `src/cljs`
directory to a new one and add the newly created directory only to the
development and pre-production builds.

Create a new directory/subdirectory in the `src` directory of
modern-cljs project

```bash
mkdir -p src/brepl/modern_cljs
```

Now move the `connect.cljs` file from `src/cljs/modern_cljs` to the new
`src/brepl/modern_cljs` directory.

```bash
mv src/cljs/modern_cljs/connect.cljs src/brepl/modern_cljs/
```

Next update the `project.clj` file by adding the `"src/brepl"` directory
to the `:source-paths` of the `:dev` and the `:pre-prod` builds, leaving
the `:prod` build as it was.

> NOTE 3: remember to add the `"src/brepl"` pathname to the main
> `:source-paths` setting as well (cf. [Tutorial 1][8])

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :source-paths ["src/clj" "src/cljs" "src/brepl"] ; added src/brepl
  ...
  :cljsbuild {:builds
              {:dev
               {;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs"] ; "src/brepl"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
               :pre-prod
               {;; clojurescript source code path
                :source-paths ["src/brepl" "src/cljs"] ; added "src/brepl"

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern_pre.js"

                           ;; simple optimization
                           :optimizations :simple

						   ;; no need prettyfication
                           :pretty-print false}}
               :prod
               {;; clojurescript source code path
                :source-paths ["src/cljs"] ;; no "src/brepl"

                :compiler {;; different JS output name
                           :output-to "resources/public/js/modern.js"

                           ;; advanced optimization
                           :optimizations :advanced

						   ;; no need prettyfication
                           :pretty-print false}}}})
```

You can run the usual commands to recompile all the builds and run the
`modern-cljs` project.

```bash
lein clean
lein cljsbuild clean
lein cljsbuild once
```

> NOTE 4: Instead of sequencially running the above tasks, you can chain
> them at the terminal as follows:
>
> ```bash
> lein do clean, cljsbuild clean, cljsbuild once
> ```
>
> Leiningen offers more options to automate composite tasks. We'll see
> them in subsequent tutorials.

One very nice consequence of the `connect.cljs` exclusion from the
`:prod` build is that now the size of the generated `modern.js` is even
smaller than before.

```bash
ls -lah resources/public/js/
total 3880
drwxr-xr-x   5 mimmo  staff   170B Mar  3 19:50 .
drwxr-xr-x  11 mimmo  staff   374B Mar  3 19:48 ..
-rw-r--r--   1 mimmo  staff    62K Mar  3 19:50 modern.js
-rw-r--r--   1 mimmo  staff   1.1M Mar  3 19:49 modern_dbg.js
-rw-r--r--   1 mimmo  staff   626K Mar  3 19:49 modern_pre.js
```

If you zip it you reach an amazing size of 18K. Finally you can run as
usual the `modern-cljs` project.

```bash
lein ring server
```

Now visit the `login.html` or the `shopping.html` pages, which include
the `modern.js` file emitted by the CLJS compiler and test them to see
if they are still working as expected.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "being doubly aggressive"
```

# Next step - [Tutorial 8: Introducing Domina events][9]

In the next [Tutorial 8][9] we're going to introduce domina events
which, by wrapping Google Closure Library event management, allows to
follow a more clojure-ish approach in handing DOM events.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
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
[10]: http://cemerick.com/2011/04/22/adding-gzip-compression-to-a-clojure-webapp-in-30-seconds/
[11]: http://docs.codehaus.org/display/JETTY/GZIP+Compression
[12]: https://help.github.com/articles/set-up-git
