# Tutorial 3 - CLJ based http-server

In this tutorial you are going to substitute the external http-server
that we configured in [tutorial 2][2] with [ring][1], a CLJ based
http-server.

## Introduction

Until we only play with CLJS code that, once compiled to JS, runs on the
browser side, we needn't a CLJ enabled http-server. But we love clojure
and we want to learn more about it too.

[Ring][1] is one of the foundamental building-blocks of any CLJ based
stack of libraries to develop web based application in CLJ programming
language and we're going to use it instead of any other http-server
based.

## Add lein-ring plugin to our project.clj

We already saw how `lein-cljsbuild` plugin helped us in managing the
build, the configuration and the running of CLJS code. In a similar way,
we're going to use [lein-ring][3] plugin to manage and automate common
[ring][1] tasks.

To install `lein-ring`, add it as a plugin to your `project.clj`. As for
`lein-cljsbuild`, if you're going to use it in every CLJ project, you
can add it to your global profile (i.e. in `~/.lein/profiles.clj`).

Like `lein-cljsbuild`, `lein-ring` plugin require to be configurated by
adding a `:ring` keyword to `project.clj`. The value of `:ring` has to
contain a map of configuration options, but just one of them, `:handler`
is required. It has to refer a function (i.e. handler) we are going to
define.

Here is the modified version of `project.clj` with the required
configuration we talked about.

```clojure
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ; clojure source code path
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [; cljsbuild plugin
            [lein-cljsbuild "0.2.9"]
            ; ring plugin
            [lein-ring "0.7.5"]]
  ; ring tasks configuration
  :ring {:handler modern-cljs.core/hanlder}
  ; cljsbuild tadks configuration
  :cljsbuild {:builds
              [{; clojurescript source code path
                :source-path "src/cljs"
                ; Google Closure Compiler options
                :compiler {; the name of the emitted JS script file
                           :output-to "resources/public/js/modern.js"
                           ; use minimal optimization CLS directive
                           :optimizations :whitespace
                           ; prettyfying emitted JS
                           :pretty-print true}}]})
```

## Create the handler

A ring handler is just a function that receives a request as an
argument and produces a response. Both request and response are regular
clojure map. Instead of using low-level [Ring API][4], we're going to add
another very common component to our `project.clj`:
[compojure][3].

[Compojure][3] is a small routing library for [Ring][1] that
allows web applications to be composed of small, independent parts,
using a concise DSL (Domain Specific Language) to generate [Ring][1]
handler.

In this tutorial our goal is to set up an http-server able to serve
static html pages (e.g. simple.html) saved in the `resources/public`
directory.

Open the file `core.clj` from `src/clj/modern_cljs` directory and change
it's content as follows.

```clojure
(ns modern-cljs.core
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defroutes app-routes
  ; to serve document root address
  (GET "/" [] "<p>Hello from compojure</p>")
  ; to server static pages saved in resources/public directory
  (route/resources "/")
  ; if page is not found
  (route/not-found "Page non found"))

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
  ; clojure source code path
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 ; compojure dependency
                 [compojure "1.1.3"]]
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
                :compiler {; the name of the emitted JS script file
                           :output-to "resources/public/js/modern.js"
                           ; minimum optimization
                           :optimizations :whitespace
                           ; prettyfying emitted JS
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
new terminal (remember to cd to `/path/to/modern-cljs`)

## Next step

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/mmcgrana/ring.git
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[3]: https://github.com/weavejester/compojure.git
[4]: http://ring-clojure.github.com/ring/
