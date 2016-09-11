# Tutorial 2 - Immediate Feedback Principle

This tutorial is aimed at configuring a ClojureScript project to approach
the Immediate Feedback principle, as described by Bret Victor in his
[seminal talk][1].

## Preamble

If you want to start working from the end of the [previous tutorial][2],
assuming you have [git][3] installed, do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-01
```

This clones the tutorial repo and starts you at the end
of the first tutorial.

> NOTE 1: the `se-` prefix means "second edition".

## Introduction

The `boot` building tool is more recent and less mature than the
corresponding `leiningen` building tool, which is a kind of standard
for CLJ developers. However, the `boot` community is working hard to
progressively enrich it with features, *tasks* in `boot` parlance,
aimed at filling the gaps and, perhaps, even overtake.

If you take a look at the [tasks for `boot`][4] developed by the
community, you'll discover that we already have everything we need to
start approaching Bret Victor's principle of Immediate Feedback:

* [`boot-http`][5]: a `boot` task providing a simple CLJ based HTTP
  server;
* [`boot-reload`][6]: a `boot` task providing a live-reload of static
  resources (i.e. CSS, images, etc.);
* [`boot-cljs-repl`][7]: a `boot` task providing a REPL for CLJS
  development;

    > NOTE 2: we already used the `boot-cljs` task in the previous tutorial.

## CLJ-based HTTP server

Let's start by adding the `boot-http` server to our `build.boot` file
located in the `modern-cljs` home directory.

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 
 :dependencies '[[adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]]) ;; add http dependency

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]) ;; make serve task visible
```

As you see, we added the latest available release of `boot-http` to
the project dependencies and made the `serve` task visible to the
`boot` command by referring it in the `require` form.

Note that we're still implicitly exploiting a few `boot` defaults:

* the use of Clojure 1.7.0, defined in the `boot.properties` file;
* the use of ClojureScript 1.7.170, implicitly imported by the
  `boot-cljs` dependency;

As usual let's take a look at the help documentation of the newly
added `serve` task:

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
```

The `-d` option is used to set the directory to be served. It will
be created if it does not exist. Let's try the following `boot`
command at the terminal:

```bash
boot serve -d target
2015-10-26 21:38:48.489:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-26 21:38:48.549:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>
```

You'll note that the command will exit after starting the http server
(i.e. Jetty). If you now type `http://localhost:3000` in your browser
URL bar you'll get an error. This is because the `serve` task does not
block.

To solve this problem we have to add the predefined `wait` task
already included with `boot`.

```bash
boot wait -h
Wait before calling the next handler.

Waits forever if the --time option is not specified.

Options:
  -h, --help       Print this help info.
  -t, --time MSEC  Set the interval in milliseconds to MSEC.
```

Let's see this solution at work:

```bash
boot wait serve -d target
2015-10-26 21:40:54.695:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-26 21:40:54.772:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>
```

The `boot` command does not exit anymore and you'll obtain the
`index.html` page when connecting to `http://localhost:3000` from
your browser. Now kill the server (`CTRL-C`).

`boot` tasks can be easily chained:

```bash
boot wait serve -d target cljs target -d target
2016-01-03 11:14:09.949:INFO::clojure-agent-send-off-pool-0: Logging initialized @7356ms
Directory 'target' was not found. Creating it...2016-01-03 11:14:10.011:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2016-01-03 11:14:10.048:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@31c694ca{HTTP/1.1}{0.0.0.0:3000}
2016-01-03 11:14:10.049:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @7455ms
Started Jetty on http://localhost:3000
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
```

Visit the `http://localhost:3000` URL and open the Developer Tool of
your browser to verify that the `Hello, World!` string has been
printed at the console. Before proceeding with the next step, kill the
current `boot` process (`CTRL-C`).

## CLJS source recompilation

If we want to approach the Immediate Feeback principle,
any changed CLJS source code should be recompiled as soon as we
modify and save a `.cljs` file.

The `watch` task is another of the large number of the predefined tasks
already included with `boot`.

```bash
boot watch -h
Call the next handler when source files change.

Debouncing time is 10ms by default.

Options:
  -h, --help     Print this help info.
  -q, --quiet    Suppress all output from running jobs.
  -v, --verbose  Print which files have changed.
  -M, --manual   Use a manual trigger instead of a file watcher.
```

Aside from triggering the execution of the CLJS recompilation whenever a
change in the CLJS source code is saved, the `watch` task can even
substitute the `wait` tasks, because it is not blocking either.

It seems that just inserting the `watch` task before calling the
`cljs` task we should be able to trigger the source recompilation.

```bash
boot serve -d target watch cljs target -d target
2016-01-03 11:17:19.733:INFO::clojure-agent-send-off-pool-0: Logging initialized @7494ms
2016-01-03 11:17:19.796:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2016-01-03 11:17:19.834:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@523e2274{HTTP/1.1}{0.0.0.0:3000}
2016-01-03 11:17:19.836:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @7596ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 6.197 sec
```

Visit `http://localhost:3000` again in your browser to confirm
that "Hello, World!" has been printed in the JS console. Then open the
`src/cljs/modern_cljs/core.cljs` source file in your preferred editor
and modify the message to be printed. Save the file. You should see that
a new CLJS compilation task has been triggered in the terminal.

```bash
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 0.174 sec
```

Finally reload the html page to confirm that the `main.js` file linked
to it has been updated by observing the message printed at the browser
console. So far, so good.

Before proceeding to the next step, kill the `boot` process (`CTRL-C`).

## Resources reloading

Anytime you modify a CLJS source file you have to manually reload the
html page pointing to it to verify the effect of your coding and we
want to get much closer to the Immediate Feedback principle.

Luckily, there is a `boot` task developed by the community to automate
reloading of any static resource: [`boot-reload`][6]. Again we have
to add the new task to the dependencies of the project and make it
visible to `boot` by requiring its primary command:

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.9"]]) ;; add boot-reload

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]) ;; make reload visible
```

This task has to be inserted in the `boot` command immediately before
the `cljs` compilation. Give it a try:

```bash
boot serve -d target watch reload cljs target -d target
Starting reload server on ws://localhost:58020
Writing boot_reload.cljs...
2016-01-03 11:22:21.323:INFO::clojure-agent-send-off-pool-0: Logging initialized @9526ms
2016-01-03 11:22:21.387:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2016-01-03 11:22:21.410:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@3117f174{HTTP/1.1}{0.0.0.0:3000}
2016-01-03 11:22:21.411:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @9614ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 8.281 sec
```

Now reload the usual URL in your browser and repeat the above
procedure by modifying the message to be printed in the browser
console. As before you'll see that as soon as you save the `core.cljs`
file the CLJS recompilation is triggered. This time, thanks to the
`boot-reload` task, the page is reloaded as well. You can confirm this
by seeing if the new message is printed in the browser's console.

You can even modify the html source file to obtain an almost immediate
feedback from the browser.

Nice stuff. Kill the `boot` command again (`CTRL-C`) before advancing to
the next level.

## Browser REPL (bREPL)

One of the main reasons to use a LISP dialect like CLJ is its REPL
(Read Eval Print Loop), which enables a very interactive style of
programming. CLJS communities worked very hard to bring the
same REPL-based programming experience to CLJS available in CLJ, and created a
way to connect a CLJS REPL to almost any JS engine, including browser-embedded
engines. This style of programming allows you to
evaluate CLJS forms in the REPL and receive an immediate feedback in
the browser to which the REPL is connected.

The `boot` community has a task to offer in this area, too. Its name is
`boot-cljs-repl`. As we have already done for the other tasks that `boot`
does not include, we need to add `boot-cljs-repl` to the dependencies of the
`build.boot` project file. Then, as usual, we have to require its primary tasks
(i.e. `cljs-repl` and `start-repl`) to make them visible to the `boot`
command at the terminal.

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.9"]
                 [adzerk/boot-cljs-repl "0.3.0"]]) ;; add bREPL

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]) ;; make it visible
```

Again, issue the `boot cljs-repl -h` command if you want to read the
documentation on its advanced options.

```bash
boot cljs-repl -h
Start a ClojureScript REPL server.

The default configuration starts a websocket server on a random available
port on localhost.

Options:
  -h, --help                   Print this help info.
  -b, --ids BUILD_IDS          Conj [BUILD IDS] onto only inject reloading into these builds (= .cljs.edn files)
  -i, --ip ADDR                Set the IP address for the server to listen on to ADDR.
  -n, --nrepl-opts NREPL_OPTS  Set options passed to the `repl` task to NREPL_OPTS.
  -p, --port PORT              Set the port the websocket server listens on to PORT.
  -w, --ws-host WSADDR         Set the (optional) websocket host address to pass to clients to WSADDR.
  -s, --secure                 Flag to indicate whether the client should connect via wss. Defaults to false.
```

The `cljs-repl` task has to be positioned just before the `cljs` task.
The `cljs-repl` author also suggests being explicit about the `Clojure`
and `ClojureScript` releases to be added in the dependencies section of
the `build.boot` build file.

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[org.clojure/clojure "1.7.0"]         ;; add CLJ
                 [org.clojure/clojurescript "1.7.170"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.9"]
                 [adzerk/boot-cljs-repl "0.3.0"]
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])
```

If you now launch the following `boot` command you'll receive a
warning and an error:

```bash
boot serve -d target watch reload cljs-repl cljs target -d target
Starting reload server on ws://localhost:55540
Writing boot_reload.cljs...
You are missing necessary dependencies for boot-cljs-repl.
Please add the following dependencies to your project:
[com.cemerick/piggieback "0.2.1" :scope "test"]
[weasel "0.7.0" :scope "test"]
[org.clojure/tools.nrepl "0.2.12" :scope "test"]
...
java.io.FileNotFoundException: Could not locate cemerick/piggieback__init.class or cemerick/piggieback.clj on classpath.
...
Elapsed time: 3.720 sec
```

This is because `boot-cljs-repl` does not transitively include its
dependencies and you have to explicitly add them in the
`:dependencies` section of the `build.boot` file:

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [adzerk/boot-cljs "1.7.170-3"]
                 [pandeiro/boot-http "0.7.0"]
                 [adzerk/boot-reload "0.4.9"]
                 [adzerk/boot-cljs-repl "0.3.0"]
                 [com.cemerick/piggieback "0.2.1"]     ;; needed by bREPL
                 [weasel "0.7.0"]                      ;; needed by bREPL
                 [org.clojure/tools.nrepl "0.2.12"]    ;; needed by bREPL
                 ])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])
```

> NOTE 3: At the moment we don't take care of the `:scope` of the
> dependencies. We'll come back to this directive in a later tutorial.

After having quit the previous process, you can safety run the
`boot` command in the terminal as follows:

```bash
boot serve -d target watch reload cljs-repl cljs target -d target
Starting reload server on ws://localhost:58051
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2016-01-03 11:32:46.553:INFO::clojure-agent-send-off-pool-0: Logging initialized @9256ms
2016-01-03 11:32:46.613:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2016-01-03 11:32:46.636:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@4a94a06{HTTP/1.1}{0.0.0.0:3000}
2016-01-03 11:32:46.637:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @9340ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

nREPL server started on port 58053 on host 127.0.0.1 - nrepl://127.0.0.1:58053
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 18.949 sec
```

The command output informs you that [`nrepl server`][8] has been
started on the local host at a port number (your port number will be
different). If your editor supports `nrepl` you are going to use that
information to connect to the now running `nrepl server` with an
`nrepl client`.

> NOTE: Emacs and CIDER support this. You can learn more about them with these [resources]
(https://github.com/magomimmo/modern-cljs/blob/master/doc/supplemental-material/emacs-cider-references.md).

At the moment we're happy enough to be able to run `cljs-repl` from a
second terminal by first launching the predefined `repl` task included
with `boot` and passing it the `-c` (i.e. client) option:

```bash
# in a new terminal
cd /path/to/modern-cljs
boot repl -c
REPL-y 0.3.5, nREPL 0.2.12
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_25-b17
        Exit: Control+D or (exit) or (quit)
    Commands: (user/help)
        Docs: (doc function-name-here)
              (find-doc "part-of-name-here")
Find by Name: (find-name "part-of-name-here")
      Source: (source function-name-here)
     Javadoc: (javadoc java-object-or-class-here)
    Examples from clojuredocs.org: [clojuredocs or cdoc]
              (user/clojuredocs name-here)
              (user/clojuredocs "ns-here" "name-here")
boot.user=>
```

This is a standard CLJ REPL defaulted to the `boot.user`
namespace. From here we can launch a browser based CLJS REPL (bREPL) as
follows:

```cljs
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:49358 >>
<< waiting for client to connect ... Connection is ws://localhost:49358
Writing boot_cljs_repl.cljs...
```

The terminal is now waiting for a client connection from the
browser. Visit the usual http://localhost:3000 URL to activate the
bREPL connection.

```cljs
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

To confirm that you can evaluate CLJS forms from the bREPL,
submit the alert function to the browser:

```cljs
(js/alert "Hello, ClojureScript")
nil
```

To stop the bREPL, submit the `:cljs/quit` expression. Then stop the
CLJ REPL (CTRL-D or `(exit)` or `(quit)`). Finally stop `boot`
(CTRL-C).

Before stepping to the next tutorial reset your git repository:

```bash
git reset --hard
```

## Next step - [Tutorial 3: House Keeping][9]

In the next [tutorial][9] we're going to automate the launching of the
`boot` command to approach the Immediate Feedback Development
Environment (IFDE).

# License

Copyright © Mimmo Cosenza, 2012-2015. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://vimeo.com/36579366
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-01.md
[3]: https://git-scm.com/
[4]: https://github.com/boot-clj/boot/wiki/Community-Tasks
[5]: https://github.com/pandeiro/boot-http
[6]: https://github.com/adzerk-oss/boot-reload
[7]: https://github.com/adzerk-oss/boot-cljs-repl
[8]: https://github.com/clojure/tools.nrepl
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-03.md
