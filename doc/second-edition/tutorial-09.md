# Tutorial 9 - Introducing Ajax

In the [previous tutorial][1] we were happy enough with the results we
achieved in terms of [separation of concerns][2], functional programming
style and elimination of any direct use of CLJS/JS interop.

As we said in [Tutorial 4][3], the very first reason why JS adoption
became so intense had to do with its ability to easily support
client-side validation of HTML forms.

Then, thanks to the introduction of [XmlHttpRequest][4] and the
popularity of Gmail and Google Maps, the terms **[Ajax][31]** and
**[Web 2.0][32]** became the most used web buzzwords all over the
places and in a very short time too.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][30] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-08
```

# Introduction

Ajax is not something magic; it's just a kind of client-server
communication, eventually asynchronous, mainly using http/https
protocols. Ajax exploits more [web techniques][5]:

* HTML and CSS for presentation
* DOM for dynamic display and interaction with data
* XML/JSON for the data representation
* XMLHttpRequest for asynchronous communication
* JavaScript to bring all the above techniques together

A sequence diagram visualizing the techniques above
applied to a form page example is shown below:

![Ajax Diagram][6]

As you can see, when a click event is raised, the corresponding event
handler calls the XHR object and registers a callback function, which
will be later notified with the response. The XHR object creates the
http request and sends it to the web server which selects and calls
the appropriate server-side handler (i.e., application logic). The
server-side handler then returns the response to the XHR object which
parses the response and notifies the registered callback. The callback
function then manipulates the DOM and/or the CSS of the page to be
shown by the browser to the user.

# No Ajax, no party

If CLJS did not have a way to implement the Ajax interaction model, it would
be dead before it was born. As usual, CLJS could exploit the Google Closure
Library which includes the `goog.net` package to abstract the plain
`XmlHttpRequest` object for supporting Ajax communications with a server
from a web browser.

That said, directly interacting with `XmlHttpRequest` or with
`goog.net.XhrIo` and `goog.net.XhrManager` could easily become
cumbersome. Let's see if the CLJS community has done something to
alleviate a certain PITA.

# Introducing shoreleave

After a brief GitHub search, my collegues [Federico Boniardi][7] and
[Francesco Agozzino][8] found the [shoreleave-remote][9] and
[shoreleave-remote-ring][10] libraries.  They appeared to be promising
in simplifying our Ajax experiments.

As you can see from its [readme][14], shoreleave is a collection of
integrated libraries that focuses on:

* Security
* Idiomatic interfaces
* Common client-side strategies
* HTML5 capabilities
* ClojureScript's advantages

And it builds upon efforts found in other ClojureScript projects, such
as [fetch][11] and ClojureScriptOne (the link is no more available).

# KISS (Keep It Small and Stupid)

To keep things simple enough we're going to stay with our boring
[shopping calculator][20] form as a reference case to be implemented using
`shoreleave`.

What we'd like to do is move the calculation from the client-side code
(i.e., CLJS) to the server-side code (i.e., CLJ), then let the former
ask the latter to produce the result to be manipulated by CLJS.

The following sequence diagram visualizes our requirements.

![Shopping Ajax][16]

The first thing we want to do is implement the server-side function to
calculate the total. Where we start from?

## The server side

First consider that the Immediate Feedback Development Environment
(IFDE) we setup on [Tutorial 3][19] already has an internal web server
offered by the [`boot-http`][32] task.

`boot-http` is configured to run [`Jetty`][34] by default, but you can
eventually use [`http-kit`][35].

Whichever web server you want to run, `boot-http` adopted the
[`ring`][36] CLJS library for serving web pages you created while
developing your web application.

This series of tutorials is not about CLJ, but as soon as you need to
make an ajax call, we need a server side endpoint as well.

This is why `boot-http` allows us to pass a [`ring handler`][37] to the
`serve` task as you can verify by taking a look at its documentation
at the terminal:

```bash
boot serve -h
Start a web server on localhost, serving resources and optionally a directory.
Listens on port 3000 by default.

Options:
  -h, --help                Print this help info.
  -d, --dir PATH            Set the directory to serve; created if doesn't exist to PATH.
  -H, --handler SYM         Set the ring handler to serve to SYM.
  -i, --init SYM            Set a function to run prior to starting the server to SYM.
  -c, --cleanup SYM         Set a function to run after the server stops to SYM.
  -r, --resource-root ROOT  Set the root prefix when serving resources from classpath to ROOT.
  -p, --port PORT           Set the port to listen on. (Default: 3000) to PORT.
  -k, --httpkit             Use Http-kit server instead of Jetty
  -s, --silent              Silent-mode (don't output anything)
  -R, --reload              Reload modified namespaces on each request.
  -n, --nrepl REPL          Set nREPL server parameters e.g. "{:port 3001, :bind "0.0.0.0"}" to REPL.
```

Aside from the `handler` option, we are interested at the
`resource-root` option as well. At the moment we're not interested in
configuring the `init` and the `cleanup` options. While developing, we
also want to set the `reload` option to `true` to reload any modified
server side namespace on each incoming request.

### build.boot

Let's start by modifying the `build.boot` to configure the `serve`
task accordingly to our `target-path`.

```clj
...
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :handler 'modern-cljs.core/handler ;; add ring handler
          :resource-root "target"            ;; add resource-path
          :reload true)                      ;; reload server side ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

Here we set the `ring handler` option to the `handler` symbol in the
`modern-cljs.core` namespace. Both the namespace and the symbol have
still to be defined. We want to keep any CLJ source file separated
from any CLJS source file. For this reason we're going to create a new
`src/clj/modern_cljs` directory to host any server side CLJ source
file (e.g. `core.clj`).

```clj
cd /path/to/modern-cljs
mkdir -p src/clj/modern_cljs
touch src/clj/modern_cljs/core.clj
```

Now we have to add the newly created `src/clj` directory to the
`source-paths` environment variable in the `build.boot` file as well.

```bash
(set-env!
 :source-paths #{"src/clj" "src/cljs"}  ;; add CLJ source dir
 :resource-paths #{"html"}
 ...
```

### Compojure

There is one more thing we want to do. Instead of directly writing
the routes of our web application as `ring` handlers, we are going to
use [`compojure`][38], a small CLJ routing library that will simplify
our job.

Let's add it to the `dependencies` section of the `build.boot` file.

```clj
 ...
 :dependencies '[
                 ...
                 [compojure "1.4.0"]                   ;; routing lib
                 ...
```

Here is the complete `build.boot` file

```clj
(set-env!
 :source-paths #{"src/clj" "src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]        ;; CLJS compiler
                 [pandeiro/boot-http "0.7.0"]          ;; web server
                 [adzerk/boot-reload "0.4.2"]          ;; live reload
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; CLJS bREPL
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL 
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"] ;; DOM manip
                 [hiccups "0.3.0"]
                 [compojure "1.4.0"]                   ;; routing lib
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

;;; add dev task
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :handler 'modern-cljs.core/handler           ;; ring hanlder
          :resource-root "target"                      ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

### The handler

We're now ready to write our `ring` handler in the
`src/clj/modern_cljs/core.clj` file we previously created.

```clj
(ns modern-cljs.core 
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found files resources]]))

(defroutes handler
  (GET "/" [] "Hello from Compojure!")  ;; for testing only
  (files "/" {:root "target"})          ;; to serve static resources
  (resources "/" {:root "target"})      ;; to serve anything else
  (not-found "Page Not Found"))         ;; page not found
```

There are few things to be noted here:

* we used the `defroutes` macro from the `compojure.core` namespace
  for defining the `handler` we set for the `serve` task in the
  `build.boot` build file;
* the `handler` is just a list of routes;
* we defined four routes:
  * the `GET` macro from the `compojure.core` namespace has been used
    to return the `Hello from Compojure` text when a client ask for
    the `http://localhost:3000/` URL;
  * the `files` function, defined in the `compojure.route` namespace,
    has been used for serving static file from the `target` directory,
    accordingly to the directory we previously set for the
    `target-path`;
  * the `resources` function, defined in the `compojure.route`
    namespace as well, has been used for serving resources living in
    the classpath accordingly to the `target-path` we previously set
    in the `build.boot` file;
  * finally we used the `not-found` function to return a `Page Not
    Found` page for any request which has not been found neither by
    the `files` or the `resources` functions.
  
We're now ready to verify if our set up is still able to serve the
`shopping.html` and the `index.html` pages.

Start the IFDE as usual:

```bash
boot dev
Starting reload server on ws://localhost:56118
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2015-11-24 15:58:29.432:INFO::clojure-agent-send-off-pool-0: Logging initialized @9231ms
2015-11-24 15:58:30.948:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2015-11-24 15:58:30.972:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@6fc50114{HTTP/1.1}{0.0.0.0:3000}
2015-11-24 15:58:30.973:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @10772ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

nREPL server started on port 56119 on host 127.0.0.1 - nrepl://127.0.0.1:56119
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Elapsed time: 17.941 sec
```

Now visit the `http://localhost:3000/index.html` and the
`http://localhost:3000/shopping.html` URLs. Everything should still
work as expected. Then visit the `http://localhost:3000` URL. You
should receive the `Hello from Compojure!` message in the
browser. Finally, if you visit any other URL
(e.g. `http://localhost:3000/foo`) you should receive the `Page Not
Found` message.

If you're complaining about the amount of work you have to do just for
reaching a behavior similar to the one obtainable for free by using
the default `serve` task configuration, I'm with you too.

That said, all that work has been done to be prepared for implementing
the server side ajax endpoint we started from as the goal of this
tutorial.

## Back to shoreleave

When thinking about Ajax, most of us instinctively associate it with
the asynchronous programming model and the way callbacks support it.

Even if this association is not wrong *per se*, it does not mean that
there are no other ways to implement ajax calls.

What is easier than RPC (Remote Procedure Call) to bring with us the
familiarity we all have with the standard way to locally call a
function?  Someone could convince you that the RPC model is
synchronous. This has not to be true. You can easily have an RPC model
which is implemented using asynchronicity. This is exactly the way the
`shoreleave` lib works: it internally uses ajax asynchronicity to
offer you a very familiar RPC programming model.

Thanks to [Chas Emerick][17], one of the most active and fruitful
Clojurists, we can exploit [shoreleave-remote-ring][10] to reach that
objective: it allows to define a remote functions returning a result
calculated server-side based on the input values got on the
client-side. Does this remember you the `Shopping Form` sample?

### Update dependencies

As usual we should first add the `shoreleave-remote-ring` library to
the `dependencies` section of the `build.boot`. That said, the
[canonical][39] `shoreleave-remote-ring "0.3.0"` release depends on
outdated `org.clojure/tools.reader "0.7.0"` and its
[`shoreleave-remote`][9] counterpart internally depends on
[`shorelave-browser`][40] which is affected by a bugged implementation
of the `ITransientAssociative` and the `ITransientMap` CLJS protocols.

To overcome these issues I upgraded all the `shoreleave` libs used in
the `modern-cljs` series. So, instead of adding the canonical
`shoreleave` libs you have to use the following forks:

```clj
  ...
  :dependencies '[...
                   [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1"]
                   [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 ]
```

> NOTE 1: the `shoreleave-remote` library to the vector will be used
> later for the client-side code.

### defremote

The next step is to define the server-side function that implements
the calculation ot the `total` from the `quantity`, `price`, `tax` and
`discount` inputs of our boring Shopping Form.

The `shoreleave-remote-ring` library offers the `defremote` macro
which is like `defn` plus adding the defining function to a registry
implemented as a reference type map (e.g., `(def remotes (atom {}))`).

We like to keep the things separated. Considering we're probably going
to create more `remote` functions, let's create a new CLJ file named
`remotes.clj` in the `src/clj/modern_cljs` directory to host all of
them and start writing the first one:

```clj
(ns modern-cljs.remotes
  (:require [shoreleave.middleware.rpc :refer [defremote]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))
```

As you see, we first declared the new `modern-cljs.remotes` namespace
and required the `shoreleave.middleware.rpc` namespace referring the
`defremote` macro.

Then we used `defremote` to define the `calculate` function.

> NOTE 2: If you compare this `calculate` function with the one
> originally defined in the `shopping.cljs` client code in the
> [previous tutorial][1], you should note that the call to the `.toFixed`
> CLJS function interop has been removed.

### Update the handler

When we introduced [Compojure][18] in the previous section, we defined
an handler to manage the essential routes for serving our
`shopping.html` and `index.html` pages.

That said the `shoreleave-remote-ring` library requires that you add
the `wrap-rpc` wrapper to the top-level handler in such a way that it
will be ready to receive Ajax calls.

Open the `remotes.clj` file again, add the required namespace
dependencies, and define the new handler which wraps the original one
with `wrap-rpc`. Here is the complete `remotes.clj` content.

```clj
(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(def app (-> (var handler)
             (wrap-rpc)))
```

As you see, in the namespace declaration we added a requirement
relative to the `modern-cljs.core` namespace to be able to intern the
`handler` symbol we previously defined.

We also added the `wrap-rpc` symbol to the `:refer` option of the
`shoreleave.middleware.rpc` namespace to be able to call it in the
threading first macro `->` used to wrap the original handler with the
things needed to manage an ajax request.

This is not not enough. To be able to parse the request received from
an ajax client we also need to enrich the handler with the
[`site`][41] middleware which adds some standard features to the
received request map:

Here is the complete `remotes.clj` source file.

```clj
(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [shoreleave.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))
```

The last thing to be done to finally enable the server-side ajax
endpoint to serve an ajax client-side call is to update the `:handler`
option of the `serve` task in the `build.boot` file by replacing the
`modern-cljs.core/handler` handler with the new one (i.e., `app`).

```clj
(deftask dev 
  ...
   (serve ...
          :handler 'modern-cljs.remotes/app            ;; ring hanlder
          ...)
   ...
```

Even if what we did is seems to be all we had to do on the server
side, there is a subtle issue we could run up against during
development as reported into the
[`ring` readme](https://github.com/ring-clojure/ring#upgrade-notice):

> From version 1.2.1 onward, the ring/ring-core package no longer comes
> with the javax.servlet/servlet-api package as a dependency.
> 
> If you are using the ring/ring-core namespace on its own, you may run
> into errors when executing tests or running alternative adapters. To
> resolve this, include the following dependency in your dev profile:
> 
> `[javax.servlet/servlet-api "2.5"]`

In a next tutorial we'll further investigate the `boot` way to manage
such things as [leiningen profiles][42]. At the moment we just want to
add the above lib to the `boot dependencies` section.

Here is the complete and final `build.boot` file.

```clj
(set-env!
 :source-paths #{"src/clj" "src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.2"]
                 [adzerk/boot-cljs-repl "0.3.0"]       ;; add bREPL
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL 
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 [org.clojars.magomimmo/domina "2.0.0-SNAPSHOT"]
                 [hiccups "0.3.0"]
                 [compojure "1.4.0"]                   ;; for routing
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1"]
                 [javax.servlet/servlet-api "2.5"]     ;; for dev only
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

;;; add dev task
(deftask dev 
  "Launch immediate feedback dev environment"
  []
  (comp
   (serve :dir "target"                                
          :handler 'modern-cljs.remotes/app            ;; ring hanlder
          :resource-root "target"                      ;; root classpath
          :reload true)                                ;; reload ns
   (watch)
   (reload)
   (cljs-repl) ;; before cljs
   (cljs)
   (target :dir #{"target"})))
```

Great: the server-side is done. We are ready to accordingly update the
client-side code, which means the `shopping.cljs` file.

Will do this in the IFDE live environment.

## Launch IFDE

Start the IFDE as usual

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 18.859 sec
```

Then launch the standard CLJ REPL

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

If you want to test the remote `calculate` function, require the
`modern-cljs.remotes` namespace and call it at the CLJS REPL prompt.

```clj
boot.user=> (require '[modern-cljs.remotes :as r])
nil
boot.user=> (r/calculate 10 12.25 8.25 5)
127.60624999999999
```

Not so impressive, but it works. We can procede with the client-side
code. Before doing that visit the
`http://localhost:3000/shopping.html` URL to verify that the Shopping
Form is still working as in the previous tutorial. 

## The client side

Open the `shopping.cljs` file from the [previous tutorial][1].

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append! 
                                 by-class
                                 by-id 
                                 destroy! 
                                 set-value! 
                                 value]]
            [domina.events :refer [listen!]]
            [hiccups.runtime])
  (:require-macros [hiccups.core :refer [html]]))

(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (set-value! (by-id "total") (-> (* quantity price)
                                    (* (+ 1 (/ tax 100)))
                                    (- discount)
                                    (.toFixed 2)))))
;;; rest of the code
```

First we need to update the namespace declaration by requiring the
`shoreleave.remotes.http-rpc` namespace and its macros as well.

```clj
(ns modern-cljs.shopping
  (:require [domina.core :refer [append! 
                                 by-class
                                 by-id 
                                 destroy! 
                                 set-value! 
                                 value]]
            [domina.events :refer [listen!]]
            [hiccups.runtime]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [cljs.reader :refer [read-string]])
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as macros]))
```

> NOTE 3: We also added the `cljs.reader` namespace to refer
> `read-string`. The reason will become clear in the following
> section, after we'll update the client-side `calculate` function.

Let's finish the work by modifying the `calculate` function.

```clj
(defn calculate []
  (let [quantity (read-string (value (by-id "quantity")))
        price (read-string (value (by-id "price")))
        tax (read-string (value (by-id "tax")))
        discount (read-string (value (by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))))
```

As soon as you save the file, IFDE will recompile it and reload the
`shopping.html` page as well.

If you now click the `Calculate` button of the Shopping Form you'll
see that is still working, but this time the `Total` value has been
calculated via ajax by the server-side.

You can confirm it by selecting the Network Panel of the Developer
Tool of your browser. If you then select the XHR type of network
traffic, any time you hit the `Calculate` button you'll see a new
`_shoreleave` raw been shown in the panel.

### The arithmetic is not always the same

First, take a look at the above `let` form of the `calculate`
function.

We wrapped the reading of all the input field values inside a
`read-string` form, which returns the JS object coded by the given
string. That's because CLJS has the same arithmetic semantics as JS,
which is different than CLJ on the JVM.

Launch the bREPL from the CLJ REPL we previously launched.

```clj
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:58939 >>
<< waiting for client to connect ... Connection is ws://localhost:58939
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

Evaluate a multiplication by passing to it two stringified numbers:

```clj
cljs.user=> (* "6" "7")
WARNING: cljs.core/*, all arguments must be numbers, got [string string] instead. at line 1 <cljs repl>
42
```

As you can see, CLJS implicitly casts strings to numbers when applies
some arithmetic functions, but not all them. As an example try to add
two stringified numbers and then multiply the result by 2 (stringified
or not, it's the same).

```clj
cljs.user=> (+ "1" "2")
WARNING: cljs.core/+, all arguments must be numbers, got [string string] instead. at line 1 <cljs repl>
"12"
cljs.user=> (* "2" (+ "1" "2"))
WARNING: cljs.core/+, all arguments must be numbers, got [string string] instead. at line 1 <cljs repl>
WARNING: cljs.core/*, all arguments must be numbers, got [string number] instead. at line 1 <cljs repl>
24
```

So, you have been warned. If you start a computation from a sum of
stringified numbers, you are asking for trouble, because you're
starting with string concatenation.

Now try the same thing in a regular CLJ repl:

```clJ
cljs.user=> :cljs/quit
nil
boot.user=> (+ "1" "2")

java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Number
```

As you can see, CLJ throws a `ClassCastException` because it can't
cast a `String` to a `Number`.

It should now be clear why we added `cljs.reader` to the
`modern-cljs.shopping` namespace declaration to refer to the CLJS
`read-string` function: we never want to get in trouble by using
stringified number in numeric calculations.

### The remote callback

Take another look at the `calculate` definition:

```clj
(defn calculate []
  (let [quantity (read-string (value (by-id "quantity")))
        price (read-string (value (by-id "price")))
        tax (read-string (value (by-id "tax")))
        discount (read-string (value (by-id "discount")))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))))
```

After having read the values from the input fields of the shopping
form, the client-site `calculate` function calls the `remote-callback` one,
which accepts:

* the keywordize remote function name (i.e., `:calculate`);
* a vector of arguments to be passed to the remote function (i.e.,
  `[quantity price tax discount]`);
* an anonymous function which receives the result (i.e., `%`) from the
  remote calculation through the `remote-callback` call, then formats
  the result (i.e., `.toFixed`) and finally manipulates the DOM by
  setting the value of the `total` input field (i.e., `set-value!`) to
  the formatted one.

Congratulations! You implemented a very simple, yet representative
Ajax web application by using CLJS on the client-side and CLJ on the
server-side.

# Make you a favor

> NOTE 4: The images of this paragraph have been generated with a
> previous version of `shoreleave` libs. *Mutatis Mutandis* (e.g.,
> `_shoreleave` instead of `_fetch`) and the actual Google Chrome
> Developer Tools interface, everything should be almost the same.

Let's finally verify our running Ajax application by using the browser
Developer Tools. I'm using Google Chrome Canary, but you can choose
any browser that provides Developer Tools comparable with the ones
available in Google Chrome Canary.

* If you have stopped the running `boot dev` process, restart it
* Open the Developer Tools (i.e. `More Tools->Developer Tools`);
* Select the `Network` pane;
* Check the `XHR` filter;
* Visit the `http://localhost:3000/shopping.html` URL or reload it.

Your browser should look like the following image.

![network-01][21]

Click the `Calculate` button. If you focus on the
`Network` pane, you should now see something similar to the following
image.

![network-02][22]

Now click `_shoreleave` (`_fetch`) from the `Name` column. If the
`Header` subpane is not already selected, select it and scroll until
you can see the `Form Data` area. You should now see the following
view which reports `calculate` as the value of the `remote` key, and
`[1 1 0 0]` as the value of `params`.

![network-03][23]

Now select the `Response` sub-pane. You should see the returned value
from the remote `calculate` function like in the following image.

![network-04][24]

That's it folks.

You can now stop any `boot` related process and reset your git repository.

```bash
git reset --hard
```

# Next step [Tutorial 10: A Deeper Understanding of Domina Events][25]

In the next tutorial we're going to apply what we just learned and extend its
application to the login form introduced in [Tutorial 4][3].

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-08.md
[2]: http://en.wikipedia.org/wiki/Separation_of_concerns#HTML.2C_CSS.2C_JavaScript
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-04.md
[4]: http://en.wikipedia.org/wiki/XMLHttpRequest
[5]: http://en.wikipedia.org/wiki/Ajax_(programming)#Technologies
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ajax.png
[7]: https://github.com/federico-b
[8]: https://github.com/agofilo
[9]: https://github.com/shoreleave/shoreleave-remote
[10]: https://github.com/shoreleave/shoreleave-remote-ring
[11]: https://github.com/ibdknox/fetch
[12]: https://github.com/ibdknox/noir
[13]: https://groups.google.com/forum/#!msg/clj-noir/AbAvQuikjGk/x8lKLKoomM0J
[14]: https://github.com/shoreleave/shoreleave-remote#shoreleave
[15]: https://github.com/brentonashworth/one
[16]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/shopping-ajax.png
[17]: https://github.com/cemerick
[18]: https://github.com/weavejester/compojure
[19]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
[20]: http://localhost:3000/shopping-dbg.html
[21]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-01.png
[22]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-02.png
[23]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-03.png
[24]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/network-04.png
[25]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-10.md
[26]: https://github.com/levand/domina
[27]: https://github.com/levand/domina/blob/master/src/cljs/domina.cljs#L125
[28]: https://github.com/levand/domina/pull/43
[29]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-11.md
[30]: https://help.github.com/articles/set-up-git
[31]: http://en.wikipedia.org/wiki/Ajax_%28programming%29
[32]: http://en.wikipedia.org/wiki/Web_2.0
[33]: https://github.com/pandeiro/boot-http
[34]: http://www.eclipse.org/jetty/
[35]: http://www.http-kit.org/
[36]: https://github.com/ring-clojure/ring
[37]: https://github.com/ring-clojure/ring/wiki/Concepts
[38]: https://github.com/weavejester/compojure
[39]: https://github.com/clojars/clojars-web/wiki/Groups#canonical-group
[40]: https://github.com/shoreleave/shoreleave-browser
[41]: https://weavejester.github.io/compojure/compojure.handler.html#var-site
[42]: https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md
