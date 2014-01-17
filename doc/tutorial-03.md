# Tutorial 3 - Ring and Compojure

In this tutorial you are going to substitute the external http-server
that we configured in [tutorial 2][1] with [ring][2] and
[compojure][5], the most standard way run a web based CLJ application.

## Introduction

So far we have only played with CLJS code that, once compiled to JS, runs on the
browser side, and have not needed a CLJ-enabled http-server. But we love Clojure
and we want to learn more about it too.

[Ring][2] is one of the fundamental building-blocks of any CLJ-based
stack of libraries to develop web based applications. We're going to use it
instead of any other http-server.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][8] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-02
git checkout -b tutorial-03-step-1
```

## Add lein-ring plugin to our project.clj

We already saw how `lein-cljsbuild` plugin helped us in managing the
build, the configuration and the running of CLJS code. In a similar way,
we're going to use [lein-ring][3] plugin to manage and automate common
[ring][2] tasks.

To install `lein-ring`, add it as a plugin to your `project.clj`. As for
`lein-cljsbuild`, if you're going to use it in every CLJ project, you
can add it to your global profile (i.e. in `~/.lein/profiles.clj`).

Like `lein-cljsbuild`, `lein-ring` plugin requires to be configurated
by adding a `:ring` keyword to `project.clj`. The value of `:ring` has
to contain a map of configuration options, but at the moment just one
of them, the `:handler`, is required and has to refer a function we are
going to define.

Here are the required changes in the `project.clj`

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :plugins [...
            [lein-ring "0.8.8"]]

  :ring {:handler modern-cljs.core/handler}
  ...)
```
## Create the handler

A ring handler is just a function that receives a request as an
argument and produces a response. Both request and response are
regular clojure maps. Instead of using low-level [Ring API][4], we're
going to add another very common library to our `project.clj`:
[compojure][5].

[Compojure][5] is a small routing library for [Ring][2] that allows
web applications to be composed of small and independent parts, using
a concise DSL (Domain Specific Language) to generate a [Ring][2]
handler.

In this tutorial our goal is to set up an http-server able to serve
static html pages (e.g. simple.html) saved in the `resources/public`
directory.

Open the file `core.clj` from `src/clj/modern_cljs` directory and
change its content as follows.

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
to the `project.clj` dependencies section as follows:

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  :dependencies [...
                 [compojure "1.1.6"]]
  ...)
```

## Run the http-server

Now that everything has been set up, we can run the server as follows:

```bash
lein ring server
2012-11-03 19:06:33.178:INFO:oejs.Server:jetty-7.6.1.v20120215
Started server on port 3000
2012-11-03 19:06:33.222:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
```

You should see a page with a paragraph saying
"Hello from compojure".  As you can see, the server started by detault
on port `3000`. Optionally, you can pass it a different port number,
like so: `lein ring server 8888`.

You can also check that the browser-connected repl is still working by
launching the `lein trampoline cljsbuild repl-listen` command on a
new terminal (remember to cd to `/path/to/modern-cljs`) and visiting
[simple.html][6] page.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "added ring and compojure"
```

## Next step - [Tutorial 4: Modern ClojureScript][7]

In the [next tutorial 4][7] we're going to have some fun introducing form validation in CLJS.

# License

Copyright © Mimmo Cosenza, 2012-2014. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[2]: https://github.com/ring-clojure/ring
[3]: https://github.com/weavejester/lein-ring
[4]: http://ring-clojure.github.com/ring/
[5]: https://github.com/weavejester/compojure.git
[6]: http://localhost:3000/simple.html
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-04.md
[8]: https://help.github.com/articles/set-up-git
