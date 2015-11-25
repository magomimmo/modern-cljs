# Tutorial 2 - Browser CLJS REPL (bREPL)

In this tutorial you are going to set up a browser-connected CLJS REPL
(bRepl) using an external http-server.

## Introduction

One of the main reasons to use a LISP dialect like CLJ is its REPL
(Read Eval Print Loop), which enables a very interactive style of
programming. CLJS communities are still working very hard to bring
into CLJS the same REPL-based programming experience available in CLJ,
and created a way to connect a CLJS REPL to the JS engine embedded in
the browser. This style of programming allows you to evaluate CLJS
forms in the REPL and have immediate feedback in the browser to which
the REPL is connected.

Due to browser-imposed limitations to prevent [cross site scripting][1]
attacks, the REPL connection with the browser's embedded JS engine has to
respect the [Same Origin Policy][2]. This means that, if we
want to enable a browser-connected CLJS REPL (brepl), we need to set up
a local http-server.

You can use any http-server. In this tutorial we're going to use the
[apache http-server][3], which is included in [MAMP][4] for Mac OS X
operating system, because it's very easy to configure and run. There
should be similar options for other OSs.

> NOTE 1: A very handy and portable http-server is python module
> `SimpleHTTPServer`. If you have python installed on your operating
> system, just launch it as follows:
>
> ```bash
> cd /path/to/modern-cljs/resources/public
> python -m SimpleHTTPServer 8888
> Serving HTTP on 0.0.0.0 port 8888 ...
> ```
>
> Thanks to [Max Penet][5] for the suggestion.
>
 
> NOTE 2: As we'll see in the [next tutorial][11], the clojurian way to
> start an HTTP server is to use [Ring][17] and [Compojure][18].

## Preamble

If you want to start working from the end of the [previous tutorial][8],
assuming you've [git][16] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-01
git checkout -b tutorial-02-step-1
```

This way you're cloning the tutorial-01 branch into a new branch to
start working with.

## Install, configure and run MAMP

Follow MAMP documentation to install MAMP. Start MAMP and click the
Preferences button of its Admin GUI.

![MAMP Admin Panel][6]

Then click the Apache tab to choose
`/path/to/modern-cljs/resources/public` as root directory of your local
Apache http server. Click ok. Finally click the `Start Servers` button to
start everything. Now you have a local web server running on your
machine at port `8888`. Visit [simple.html][7] you created in
[Tutorial 1 - The Basics][8] using `http://localhost:8888/simple.html`
to verify that everything is ok.

## Setting up a browser connected CLJS REPL (brepl)

To set up a brepl, we need to follow a few steps:

* create a CLJS file to predispose the connection between the browser
  and the brepl
* compile the CLJS file
* start the brepl server
* enable the connection

### Create the connection

Create a CLJS source file in the `src/cljs/modern_cljs` with the
following content:

```clojure
(ns modern-cljs.connect
  (:require [clojure.browser.repl :as repl]))

(repl/connect "http://localhost:9000/repl")
```

Save the file as `connect.cljs`.

As you can see, to connect from the browser to the brepl we have to call
the `connect` function defined in the `clojure.browser.repl`
namespace. We set `9000` as the port for the brepl to connect to,
because this is the default port used by the brepl server when we
start it.

### Compile the CLJS file

Now we need to compile the new CLJS file. [Google Closure Compiler][9]
(CLS) has a few compilation options we already set up in our
`project.clj` during [Tutorial 1][8], and we can leave those options
as we have already configured. Now call the CLJS compilation task:

```bash
lein do clean, cljsbuild once
Compiling ClojureScript.
Compiling "resources/public/js/modern.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern.js" in 4.904672 seconds.
```

> NOTE 3: We chained the clean and cljsbuild tasks together in a single
> command by using the do task with comma-separated tasks.

> NOTE 4: By using CLJS 1.7.145 release you'll get an annoying
> compilation warning regarding the Google Closure Library (GCL). At
> the moment it does not hurt. You could eventually downgrade to CLJS
> 1.7.122 or waiting for a new release.

### Start a brepl

To enable the connection between the repl and the browser, we need
to start a repl that acts as a server waiting for a connection from the
browser. To do that we're going to use a `repl-listen` task already
set up by `lein-cljsbuild` for us.

```bash
lein trampoline cljsbuild repl-listen
Running ClojureScript REPL, listening on port 9000.
Compiling client js ...
Waiting for browser to connect ...
```

Do not yet type anything at the brepl prompt. It's waiting for a
connection from the browser and it's not yet responsive.

### Enable the connection

To enable the browser connection with the running brepl we just need
to visit the [simple.html][7] we created in the previous
[Tutorial 1][8].  Make sure to visit it using
`http://localhost:8888/simple.html` rather than through
`file:///.../simple.html`, otherwise, the brepl will not connect.

Obviously, the http-server has to be running. By visiting
[simple.html][7], the included `modern.js` script generated by CLS
compilation connects to the listening brepl on port `9000` started by
`repl-listen` task of `lein-cljsbuild` plugin.

As soon as the connection has been activated you'll get the
`cljs.user=>` prompt ready to evaluate CLJS expressions.

```bash
Running ClojureScript REPL, listening on port 9000.
Compiling client js ...
Waiting for browser to connect ...
To quit, type: :cljs/quit
cljs.user=>
```

Now you can evaluate CLJS forms in the brepl.

```clojure
cljs.user=> (+ 41 1)
42
cljs.user=>
```
Best of all, you can start evaluting CLJS forms interacting with the browser
and see immediate feedback in the browser itself.

```clojure
cljs.user=> (js/alert "Hello from a browser connected repl")
```
![Alert Window][10]

You will note that there is no command history or editing ability in
this REPL.  You can add it if you wish by installing [rlwrap][12] and
following the instructions [here][13]. 

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "brepl enabled"
```

## Next step - [Tutorial 3: Ring and Compojure][11]

In the next [tutorial][11] we're going to substitute the external
http-server with a CLJ-based http-server, enabling direct
communications between CLJS client code and CLJ server code in
subsequent tutorials.

# License

Copyright © Mimmo Cosenza, 2012-2015. Released under the Eclipse Public
License, the same as Clojure.

[1]: http://en.wikipedia.org/wiki/Cross-site_scripting
[2]: http://en.wikipedia.org/wiki/Same_origin_policy
[3]: http://httpd.apache.org/
[4]: http://www.mamp.info/en/index.html
[5]: https://github.com/mpenet
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/mamp-01.png
[7]: http://localhost:8888/simple.html
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/first-edition/tutorial-01.md
[9]: https://developers.google.com/closure/compiler/
[10]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/alert.png
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/first-edition/tutorial-03.md
[12]: http://utopia.knoware.nl/~hlub/rlwrap/#rlwrap
[13]: https://github.com/emezeske/lein-cljsbuild/wiki/Using-Readline-with-REPLs-for-Better-Editing
[14]: https://github.com/emezeske/lein-cljsbuild/issues/186
[15]: https://github.com/emezeske/lein-cljsbuild
[16]: https://help.github.com/articles/set-up-git
[17]: https://github.com/mmcgrana/ring
[18]: https://github.com/weavejester/compojure
