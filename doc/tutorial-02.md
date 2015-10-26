# Tutorial 2 - Immediate Feedback Principle

This tutorial is aimed at configuring `modern-cljs` project to reach
in out development environment a level of interaction that Bret Victor
called Immediate Feedback in his seminal talk
[Inventing on Principle][1]

## Preamble

If you want to start working from the end of the [previous tutorial][2],
assuming you've [git][3] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout tutorial-01
git checkout -b tutorial-02-step-1
```

This way you're cloning the tutorial-01 branch into a new branch to
start working with.


## Introduction

The `boot` building tool is more recent and less mature than the
corresponding `leiningen` building tool, which is a kind of standard
for CLJ developers. Howsoever, the `boot` community is working hard to
progressively enrich it with features, *tasks* in `boot` parlance,
aimed at filling the gap.

If you take a look at the [tasks for `boot`][4] developed by the
community, you'll discover that we already have anything we need to
start approaching the Bred Victor's principle of immediate feedback:

* [`boot-http`][5]: a `boot` task providing a simple CLJS based HTTP
  server;
* [`boot-reload`][6]: a `boot` task providing a live-reload of static
  resources (i.e. CSS, images, etc.);
* [`boot-cljs-repl`][7]: a `boot` task providing a REPL for CLJS
  development;

## CLJ-based HTTP server

Let's start by adding `boot-http` server to our `build.boot` file
located in the `modern-cljs` home directory. 

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 
 :dependencies '[[adzerk/boot-cljs "1.7.48-6"]
                 [pandeiro/boot-http "0.6.3"]]) ;; add http dependency

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]) ;; make serve task visible
```

As you see, we only added the latest available release of `boot-http`
to the project dependencies and made the `serve` task visible to the
`boot` command by referring it in the `require` form.

Note that we're still implicetely exploit few `boot` defaults:

* the use of Clojure 1.7.0, defined in the `boot.properties` file;
* the use of ClojureScript 1.7.48, implicetely imported by the
  `boot-cljs` dependency;
* the `"target"` directory as the default value used as `:target-path`
  by `boot` itself.

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

The `-d` option is used to set the directory to serve and it will be
created if it does not exist. Let's try the following `boot` command
at the terminal:

```bash
boot serve -d target cljs
Directory 'target' was not found. Creating it...2015-10-25 14:33:43.481:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-25 14:33:43.578:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

You'll note that the command will exit after executing the `cljs`
compilation task and if you now type `http://localhost:3000` in your
browser URL bar you'll get an error. This is because the `serve` task
does not wait after been executed.

To solve this problem we have to add the predefined `wait` task
already included with `boot`, between the call to `serve` and `cljs`
tasks. Let's see this solution at work:

```bash
boot serve -d target wait cljs
Directory 'target' was not found. Creating it...2015-10-25 15:01:22.832:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-25 15:01:22.929:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

The `boot` command does not exit anymore and you'll obtaing the
`index.html` page when connecting to http://localhost:3000 URL from
your browser.

Open the Developer Tool of your browser to verify that the `Hello,
World!` string has been printed at the console. Before to proceed with
the next step, kill the current `boot` process (`CTRL-C`).

## CLJS source recompilaton

If we want to approach the Bret Victor Immediate Feeback Principle, we
should be able to recompile any CLJS source code as soon as we modify
and save one of them.

`watch` task is another of the large number of the predefined tasks
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

It seems that just inserting the `watch` task before calling the
`cljs` task we should be able to trigger the source recompilation.

```bash
boot serve -d target wait watch cljs
2015-10-25 17:27:46.467:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-25 17:27:46.507:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>

Starting file watcher (CTRL-C to quit)...

Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Elapsed time: 6.436 sec
```

Visit again the `http://localhost:3000` URL in your browser to confirm
that "Hello, World!" has been printed in the JS console. Then open in
your preferred edtor the `src/cljs/modern_cljs/core.cljs` source file
and modify the message to be printed. Save the file and take a look at
the terminal. You should see that a new CLJS compilation task has been
triggered.

```bash
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Elapsed time: 0.168 sec
```

Finally reload the html page to confirm that the `main.js` file linked
to it has been updated by osserving the message printed at the browser
console. So far, so good.

Before to proceed to the next step in approching the immediate
feddback goal, kill the `boot` process (`CTRL-C`).

## Resources reloading

Anytime you modify a CLJS source file you have to manually reload the
html page pointing to it to verify the effect of your coding. We
desire to approach the immediate feedback principle much more closer
than this.

Luckily, there is a `boot` task developed by the community to automate
the reload of html pages an more general of any static resource:
[`boot-reload`][6]. Again we have to add the new task to the
dependensies of the proejct and make it visibile to the `boot` command
by requiring its main command:

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 
 :dependencies '[[adzerk/boot-cljs "1.7.48-6"]
                 [pandeiro/boot-http "0.6.3"]
                 [adzerk/boot-reload "0.4.1"]]) ;; add boot-realod

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]) ;; make reload visible
```

This task has to be inserted in the `boot` command immediately before
the `cljs` compilation. Give it a try:

```bash
boot serve -d target wait watch reload cljs
Starting reload server on ws://localhost:60865
Writing boot_reload.cljs...
2015-10-25 18:57:15.221:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-25 18:57:15.247:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>

Starting file watcher (CTRL-C to quit)...

Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Elapsed time: 10.283 sec
```

Now reload again the usual URL in your browser and repeat the above
procedure by modifing the message to be printed at the browser
console. As before you'll see that as soon as you save the `core.cljs`
file the CLJS recompilation is triggered. This time, thanks to the
`boot-reload` task, the page is reload as well, as you can confirm by
seeing the new message printed at the browser console.

You can even modify the html source file to obtain an almost immediate
feedback from the browser.

Nice stuff. Kill again the `boot` command (CTRL-C) for advancing to
the next level.

## Browser REPL (bREPL)

One of the main reasons to use a LISP dialect like CLJ is its REPL
(Read Eval Print Loop), which enables a very interactive style of
programming. CLJS communities worked very hard to bring into CLJS the
same REPL-based programming experience available in CLJ, and created a
way to connect a CLJS REPL to almost any JS engine, the one embedded
in the browser as well. This style of programming allows you to
evaluate CLJS forms in the REPL and receive an immediate feedback in
the browser to which the REPL is connected.

`boot` community has a task to offer even in this area. It's name is
`boot-cljs-repl`. As already done for the other tasks not predefined
with `boot`, we need to add it to the dependencies of the `build.boot`
project file and then we have to require its main tasks
(i.e. `cljs-repl` and `start-repl`) to make them visible to the `boot`
command at the terminal.

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 
 :dependencies '[[adzerk/boot-cljs "1.7.48-6"]
                 [pandeiro/boot-http "0.6.3"]
                 [adzerk/boot-reload "0.4.1"]
                 [adzerk/boot-cljs-repl "0.2.0"]]) ;; add REPL

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]) ;; make ti visible
```

Again, issue the `boot cljs-repl -h` command if you want to read the
documentation of its advanced options.

That said, if you lounch the previous `boot serve -d target wait watch
reload cljs cljs-repl` by appending the `cljs-repl` task as the last
argument to the previous command, you'll get an error. This is because
`boot-cljs-repl` task requires you to explicitely specify both the
Clojure and the ClojureScript release into the `build.boot`
dependencies:

```clj
(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}
 
 :dependencies '[[org.clojure/clojure "1.7.0"] ;; add CLJ
                 [org.clojure/clojurescript "1.7.122"] ;; add CLJS
                 [adzerk/boot-cljs "1.7.48-6"]
                 [pandeiro/boot-http "0.6.3"]
                 [adzerk/boot-reload "0.4.1"]
                 [adzerk/boot-cljs-repl "0.2.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])
```

You can now safetely run the `boot` command at the terminal as follow:

```bash
boot serve -d target wait watch reload cljs cljs-repl
Starting reload server on ws://localhost:64717
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2015-10-25 22:30:13.708:INFO:oejs.Server:jetty-7.6.13.v20130916
2015-10-25 22:30:13.739:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
<< started Jetty on http://localhost:3000 >>

Starting file watcher (CTRL-C to quit)...

Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
nREPL server started on port 64719 on host 127.0.0.1 - nrepl://127.0.0.1:64719
Adding :require adzerk.boot-cljs-repl to main.cljs.edn...
Elapsed time: 17.949 sec
```

The command informs you that an [`nrepl server`][8] has been started
on the local host at a port number 64719 (your port number will be
different). If your editor supports `nrepl` you are going to use that
information to connect to the now running `nrepl server` with an
`nrepl client`.

At the moment we'll be happy enough to be able tu run `cljs-repl` from
a second terminal by first launch the predefined `repl` task included
with `boot` by passing it the `-c` (i.e. client) option:

```bash
# in a new terminal
cd modern-cljs
boot repl -c
REPL-y 0.3.5, nREPL 0.2.11
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
namespace. From here we launch a browser based CLJS REPL (bREPL) as
follow:

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

If you're using `git`, update the `.gitignore` content as follows:

```bash
/target
.nrepl-port
.nrepl-history
/out
```

Then commit you changes to the project

```bash
git add .gitignore
git commit -m "add .nrepl-port, .nrepl-history and out"
git commit -am "immediate feedback"
```

## Next step - [Tutorial 3: House Keeping][9]

In the next [tutorial][9] we're going bla bla bla

# License

Copyright © Mimmo Cosenza, 2012-2015. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://vimeo.com/36579366
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[3]: https://git-scm.com/
[4]: https://github.com/boot-clj/boot/wiki/Community-Tasks
[5]: https://github.com/pandeiro/boot-http
[6]: https://github.com/adzerk-oss/boot-reload
[7]: https://github.com/adzerk-oss/boot-cljs-repl
[8]: https://github.com/clojure/tools.nrepl
[9]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
