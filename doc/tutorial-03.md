# Tutorial 3 - CLJ based http-server

In this tutorial you are going to substitute the external http-server
that we configured in [tutorial 2][1] with [ring][2], a CLJ based
http-server.

## Introduction

So far we have only played with CLJS code that, once compiled to JS, runs on the
browser side, and have not needed a CLJ enabled http-server. But we love clojure
and we want to learn more about it too.

[Ring][2] is one of the fundamental building-blocks of any CLJ based
stack of libraries to develop web based application in CLJ programming
language and we're going to use it instead of any other http-server
based.

## Add lein-ring plugin to our project.clj

We already saw how `lein-cljsbuild` plugin helped us in managing the
build, the configuration and the running of CLJS code. In a similar way,
we're going to use [lein-ring][3] plugin to manage and automate common
[ring][2] tasks.

To install `lein-ring`, add it as a plugin to your `project.clj`. As for
`lein-cljsbuild`, if you're going to use it in every CLJ project, you
can add it to your global profile (i.e. in `~/.lein/profiles.clj`).

Like `lein-cljsbuild`, `lein-ring` plugin require to be configurated by
adding a `:ring` keyword to `project.clj`. The value of `:ring` has to
contain a map of configuration options, but just one of them,
`:handler`, is required. It has to refer a function we are going to
define.

Here is the modified version of `project.clj` with the required
configuration we talked about.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; clojure source code pathname
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.5.1"]]

  :plugins [;; cljsbuild plugin
            [lein-cljsbuild "0.3.0"]

            ;; ring plugin
            [lein-ring "0.8.3"]]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              [{;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of the emitted JS file
                           :output-to "resources/public/js/modern.js"

                           ;; use minimal optimization CLS directive
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}]})
```

## Create the handler

A ring handler is just a function that receives a request as an
argument and produces a response. Both request and response are
regular clojure map. Instead of using low-level [Ring API][4], we're
going to add another very common library to our `project.clj`:
[compojure][5].

[Compojure][5] is a small routing library for [Ring][2] that allows
web applications to be composed of small and independent parts, using
a concise DSL (Domain Specific Language) to generate [Ring][2]
handler.

In this tutorial our goal is to set up an http-server able to serve
static html pages (e.g. simple.html) saved in the `resources/public`
directory.

Open the file `core.clj` from `src/clj/modern_cljs` directory and
change it's content as follows.

```clojure
(ns modern-cljs.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

;; defroutes macro defines a function that chains individual route
;; functions together. The request map is passed to each function in
;; turn, until a non-nil response is returned.
(defroutes app-routes
  ; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ; to serve static pages saved in resources/public directory
  (route/resources "/")
  ; if page is not found
  (route/not-found "Page not found"))

;; site function creates a handler suitable for a standard website,
;; adding a bunch of standard ring middleware to app-route:
(def handler
  (handler/site app-routes))
```

## Add compojure to project.clj

Before running our new CLJ based http-server, we need to add `compojure`
to `project.clj` dependencies section. The new `project.clj` is as
follows:

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; clojure source code pathname
  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]]

  :plugins [;; cljsbuild plugin
            [lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.3"]]

  ;; ring tasks configuration
  :ring {:handler modern-cljs.core/handler}

  ;; cljsbuild tasks configuration
  :cljsbuild {:builds
              [{;; clojurescript source code path
                :source-paths ["src/cljs"]

                ;; Google Closure Compiler options
                :compiler {;; the name of the emitted JS file
                           :output-to "resources/public/js/modern.js"

                           ;; minimum optimization
                           :optimizations :whitespace

                           ;; prettyfying emitted JS
                           :pretty-print true}}]})
```

## Run the http-server

Now that everything has been set up, we can run the server as follows:

```bash
$ lein ring server
2012-11-03 19:06:33.178:INFO:oejs.Server:jetty-7.6.1.v20120215
Started server on port 3000
2012-11-03 19:06:33.222:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
```

You should see a page with a paragraph saying
"Hello from compojure".  As you can see, the server started by detault
on port `3000`. Optionally, you can pass it a different port number,
like so: `$ lein ring server 8888`.

You can also check that the browser connected repl is still working by
launching again `$ lein trampoline cljsbuild repl-listen` command on a
new terminal (remember to cd to `/path/to/modern-cljs`) and visiting
[simple.html][6] page.

## Next step

In the [next tutorial 4][7] we're going to have some fun introducing form validation in CLJS.

# License

Copyright © Mimmo Cosenza, 2012-2013. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[2]: https://github.com/ring-clojure/ring
[3]: https://github.com/weavejester/lein-ring
[4]: http://ring-clojure.github.com/ring/
[5]: https://github.com/weavejester/compojure.git
[6]: http://localhost:3000/simple.html
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
