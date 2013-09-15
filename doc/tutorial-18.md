# Tutorial 18 - Housekeeping 

In the [previous tutorial][1] we injected form validators into the
WUI (Web User Interface) in such a way that the user will be
notified with the corresponding error messages when she/he types in
invalid values. Continuing with our progressive enhancement
strategy, we started to inject the form validators into the
server-side-only code first.

## Introduction

Instead of immediately going back to the client-side to inject the
same validators into the CLJS code, in this tutorial we're going to
digress about two topics:

* the setup of a more comfortable browser REPL, if compared with the
  one covered in previous tutorials (i.e. [Tutorial 2][2]
  [Tutorial 3][3] and [Tutorial 7][4])
* the setup of a more comfortable project structure obtained by using
  the [Leiningen][5] `profiles`.

> NOTE 1: This tutorial requires a leiningen version `>=
> 2.2.0`. You can upgrade leiningen to the latest available
> version by issuing the following command at the terminal:
>
> ```bash
> lein upgrade
> ```

> To verify your leiningen installed version, submit the following
> command at the terminal:
> 
> ```bash
> lein version
> ```

> NOTE 2: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-17
> git checkout -b tutorial-18-step-1
> ```

> NOTE 3: I also suggest that you quickly review the above tutorials if
> you did not read them, or you don't remember the covered topics.

## The need of a more comfortable bREPL experience

In the [Tutorial 2][2], [Tutorial 3][2] and [Tutorial 7][4] we
explained how to setup and use a browser connected REPL (i.e. bREPL)
to enable a style of programming as dynamic as possible.

That said, the above bREPL configuration is subject to a few
limitations inherited from the underlaying default CLJS REPL,
internally used by the [lein-cljsbuild][6] plugin.

> NOTE 4: See [Piggieback README][7] for a brief list of the
> limitations of the default CLJS REPL. 

In this tutorial we're going to introduce the setting up of a less
limited bREPL to be run from an [nREPL][8] launched by [Leiningen][5]
via the `lein repl` task.

As pointed out by [Ian Eslick][9] in his very useful [tutorial][10] on
setting up a Clojure debugging environment.

> nREPL is a tooling framework for allowing editors (clients) to
> connect to a running Clojure instances (servers) to utilize
> information in the environment to navigate code, complete symbols,
> and dynamically evaluate code...It also defines a middleware
> framework to add functionality on top of the basic definitions of
> transport and minimal methods to support a REPL.

As usual, we again the works done by [Chas Emerick][11]
who created [Piggieback][12], a bREPL implemented as an
[nREPL middleware][19] that allows to launch a CLJS REPL session on
top of an [nREPL][8] session to overcome the limitations of the
default CLJS REPL.

## bREPL setup with Piggieback

The setup of a bREPL based on [Piggieback][12] nREPL middleware is not
cumbersome at all, but does require us to scrupulously follow a few steps.

The first of them, the creation of a pair of CLJS/HTML files which
enable the connection between the JS engine of the browser and a REPL,
has been already described in the [Tutorial 2][2] and updated in the
tutorial [3][2] and [7][4] of this series. Be happy; it stays
the same for the nREPL-based bREPL too.

The second step consists of:

* adding the [Piggieback][12] nREPL middleware to the project
  dependencies, and
* configuring the `:nrepl-options` of the project.

The third and latest step requires:

* to launch the nREPL via the `lein repl` task, and
* to run the bREPL from the active nREPL session.

### STEP 1

Already done in the cited tutorials.

### Step 2

Open the `project.clj` file and update its dependencies as follows:

```clj
(defproject
  ...
  ...
  :dependencies [...
                 ...
                 [com.cemerick/piggieback "0.1.0"]
                 ...
                 ...]
  ...
  ...)
```

Then we have to configure the project `:nrepl-options` as follows:

```clj
(defproject
  ...
  ...
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  ...
  ...)
```

Here we added the `cemerick.piggipack/wrap-cljs-repl` middleware to
the stack of nREPL middleware used by `lein repl` when it starts an
nREPL session.

### Step 3 - Run the bREPL

We can now run a bREPL session on top of an nREPL session.

First we need:

* to start from a clean environment
* to regenerate the unit tests for both CLJ and CLJS via the `cljx`
  plugin
* to recompile all the CLJS builds, and
* to launch the ring server.

```bash
# clean up
lein clean 
# unit test generation for CLJ and CLJS via cljx
lein cljx
# CLJS compilations
lein compile
# start the ring server
lein ring server-headless
```

> NOTE 5: At the moment don't worry about the `*WARNING*` messages you
> receive during the CLJS compilation.

Next launch the `lein repl` task from a new terminal to run a new nREPL session.

```bash
# open a new terminal command
# cd into the modern-cljs main directory
# launch the nREPL
lein repl
nREPL server started on port 52891
REPL-y 0.2.0
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)

user=>
```

Now we have to load the bREPL on top of it by following the
[Piggieback][12] instruction:

```clj
user=> (require 'cljs.repl.browser)
nil
user=> (cemerick.piggieback/cljs-repl
          :repl-env (cljs.repl.browser/repl-env :port 9000))
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=>
```

The above CLJ code creates, configures and runs a browser-based REPL,
named bREPL, which acts as the client side component of the connection
with the JS engine hosted by your browser, which acts as the
corresponding server-side component.

The final step, as we already explained in the [Tutorial 2][2],
consists of activating the bREPL session by visiting one of the pages
(e.g. [shopping-dbg.html][13]) of the development or the
pre-production builds.

> NOTE 6: In the [Tutorial 7][4] we explained how to exclude a bREPL
> connection from a production CLJS build.

Wait few moments to allow the client and the server components to
establish the connection (on port 9000) and then you can start REPLing
with the browser again as you did in the past tutorials.

To return the control to the underlying nREPL session, just exit the
bREPL session.

```cljs
cljs.user=> :cljs/quit
:cljs/quit
user=>
```

To exit the nREPL just run `user=>(quit)` as usual.

### A little bit of bREPL automation

Even if we have been able to create a bREPL session on top of an nREPL
session, the need to programmatically create a new bREPL session each
time we need to launch it is very boring.

One possible solution for automating such boring activity is to use
the `:injections` option of [Leiningen][5] as follows:

```clj
(defproject
  ...
  ...
  :injections [(require '[cljs.repl.browser :as brepl]
                        '[cemerick.piggieback :as pb])
               (defn browser-repl []
                 (pb/cljs-repl :repl-env (brepl/repl-env :port 9000)))]
  ...
  ...)
```

The value of the `:injections` option is a vector of CLJ forms that
are evaluated sequencially in the default CLJ namespace (i.e. `user`)
immediately after the start of the nREPL session.

After having required the `cljs.repl.browser` and the
`cemerick.piggieback` namespaces, we defined the `browser-repl`
function which wraps the already discussed creation of a bREPL client
session ready to connect with the waiting server counterpart living in
the browser.

If you did not quit the previous bREPL session, do so now, relaunch it
and call the `browser-repl` function we defined above in the
`:injections` option of [Leiningen][5]

```bash
lein repl
nREPL server started on port 50299
REPL-y 0.2.0
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)

user=> (browser-repl)
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=>
```

As usual, you activate the bREPL connection by visiting one of
the pages from the `:dev` or the `:pre-prod` builds.

## The need of a more comfortable project structure

As we walked through the tutorials of the series we started adding
plugins, dependencies and other esoteric options to the `project.clj`
file, which ended up being long and convoluted.

At the same time, even if we added the `:hooks [leiningen.cljsbuild]`
option to our `project.clj` file, we still need to issue more lein
tasks to be able to clean, compile and run the project or even its
tests.

### Cljx hooks

As the [lein-cljsbuild][6] does, the [cljx][20] lein plugin offers a
way to hook some of its subtasks to leiningen tasks
(e.g. `compile`). You just need to add it in the `:hooks` session of
your `project.clj` as follows:

```clj
(defproject ...
  ...
  :hooks [cljx.hooks leiningen.cljsbuild]
  ...)
```

Now you can more quickly clean, compile and start the project or its
unit tests as follows.

```bash
lein do clean, compile, test # unit testing from a clean env
```

```bash
lein do clean, compile, ring server-headless # run application from a clean env
```

Unfortunally, both the above commands show a double CLJS unit testing
code genration which, in turn, are compiled two times! The iteraction
between the `cljsbuild` and the `cljx` hooks seems to have a bug to be
fixed.

### Aliases option

Luckly, `lein` offers an `:aliases` option which, if combined with the
`do` task for chaining lein tasks, allows us to obtain even a better
level of automation without incurring in the double code
generation/compilation shown above by the bugged interaction of
`cljsbuild` and `cljx` hooks.

First, remove the `cljx` hooks and add the following `:aliases` option
to our `project.clj`.

```clj
(defproject ...
  ...
  :hooks [leiningen.cljsbuild]
  :aliases {"clean-test!" ["do" "clean," "cljx" "once," "compile," "test"]
	        "clean-start!" ["do" "clean," "cljx" "once," "compile," "ring" "server-headless"]}
  ...)
```

The value of the `:aliases` option is a map where each key is a string
and each associated value is a vector of the stringified tasks to be
chained by the `do` task.

> NOTE 7: Each command but the last has to contain the comma `,`
> inside the stringified task name (e.g. `"clean,"`), otherwise the
> next string will be interpreted as an argument of the previous task
> (e.g. "ring" "server-headless").

To list all the available aliases in a project, just call `lein help`
command.

```bash
lein help
...
These aliases are available:
clean-test!, expands to ["do" "clean," "cljx" "once," "compile," "test"]
clean-start!, expands to ["do" "clean," "cljx" "once," "compile," "ring" "server-headless"]
...
```

You can now safely call the above aliases as follows.

```bash
lein clean-test!
```

```bash
lein clean-start!!
```

Even if have been able to chain few tasks to obtain a little bit of
automation, the `project.clj` file is becaming more a more dense and
convoluted. It contains the dependencies pertaining the CLJ codebase,
the ones relative to the CLJS codebase and even the ones regarding the
unit testing and the enabling of a bREPL session based on nREPL.

The `:plugins` section is affected by the same kind of roles' mix in
the project, and so do the few configuration options for the plugins
themselves. Don't you think we need more separation of concerns?

### Leining profiles

Starting from the `"2.0.0"` release, [leiningen][5] introduced the
[profiles][14] feature, which allows to obtain, if not a shorter
`project.clj`, at least a superior separation of concerns in its
continuosly growing sections.

#### User profiles

Say you want a set of plugins to be available in all your local
projects managed by `lein`. I always want to have at least the
following plugins:

* [lein-try][15]: for REPling with new libs without declaring them in
  the projects' dependencies
* [lein-pprint][16]: to pretty print the entire map representation of
  the projects
* [lein-ancient][17]: to check if there are available upgrades for the
  components used in the projects
* [lein-bikeshed][18]: to check the adherence of the project code to a
  small set of established and extendable CLJ idioms.

All we have to do is to create a `profiles.clj` file inside the
`~/.lein` directory and write the following declarations:

```clj
{:user {:plugins [[lein-try "0.3.1"]
	              [lein-pprint "1.1.1"]
                  [lein-ancient "0.4.4"]
                  [lein-bikeshed "0.1.3"]]}}
```

If you now run the `lein pprint` command from a project home
directory, you'll get the entire map of the project itself and you can
verify that the above user level plugins have been merged with the
ones declared in your `project.clj` file.

> NOTE 8: Remember that any eventual local profile defined inside a
> the `project.clj` takes precedence over the one declared in the
> `~/.lein/profiles.cljs`.

```bash
lein pprint
{...
 ...
 :plugins
 ([lein-ring/lein-ring "0.8.7"]
  [lein-cljsbuild/lein-cljsbuild "0.3.3"]
  [com.keminglabs/cljx "0.3.0"]
  [lein-try/lein-try "0.3.1"]
  [lein-pprint/lein-pprint "1.1.1"]
  [lein-ancient/lein-ancient "0.4.4"]
  [lein-bikeshed/lein-bikeshed "0.1.3"]),
 ...}
```

This is just the beginning of the profiles story. 

#### Dev profile

As we said above, our `project.clj` mixed up dependencies, plugins and
project configurations pertaining to different activities onto the
project itself.

[Leinningen][14] predefines few profiles, being the already seen
`:user` profile one of them. A second predefined profile is the `:dev`
one, which is very useful in approaching the mentioned separation of
concerns in the `project.clj`

For example, the `piggieback` dependency and the corresponding
`:repl-options` and `:injections` configurations they all have to do
with the development activities by enabling the bREPL on top of an
nREPL.

By adding a `:profiles` section to the `project.clj` file we can start
separating those stuff pertaining the development activities.  Just
move them into a `:dev` profile as follows:

```clj
(defproject
  ...
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.0"]]}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :injections [(require '[cljs.repl.browser :as brepl]
                                         '[cemerick.piggieback :as pb])
                                (defn browser-repl []
                                  (pb/cljs-repl :repl-env
                                                (brepl/repl-env :port 9000)))]}
```

Bu using the `lein with-profiles` task you can easly verify the
differences between the `:user` and the `:dev` project maps after the
above changes.

```bash
lein with-profiles user pprint
```

```bash
lein with-profiles dev pprint
```

That said, there are more project's sections and configurations that
can be moved under the `:dev` profile to improve the separation of
concerns of the modern-cljs `project.clj` file:

* `com.cemerick/clojurescript.test` dependency
* `com.keminglabs/cljx` plugin and configuration
* `lein-cljsbuild` plugin and configuration
* `:hooks` section
* `:aliases` section

Here is the resulting `:profiles` section of `project.clj` obtained by
moving all those stuff under the `:dev` profile.

```bash
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ... 
  :profiles {:dev {:test-paths ["target/test/clj"]
                   :clean-targets ["out"]

                   :dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                                  [com.cemerick/piggieback "0.1.0"]]
                   
                   :hooks [leiningen.cljsbuild]
                   
                   :plugins [[lein-cljsbuild "0.3.3"]
                             [com.keminglabs/cljx "0.3.0"]]
                   
                   :cljx {:builds [{:source-paths ["test/cljx"]
                                    :output-path "target/test/clj"
                                    :rules :clj}
                                   
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test/cljs"
                                    :rules :cljs}]}

                   :cljsbuild {:crossovers [valip.core
                                            valip.predicates
                                            modern-cljs.login.validators
                                            modern-cljs.shopping.validators]
                              
                               :test-commands {"phantomjs-whitespace"
                                               ["runners/phantomjs.js" "target/test/js/testable_dbg.js"]
                              
                                               "phantomjs-simple"
                                               ["runners/phantomjs.js" "target/test/js/testable_pre.js"]
                              
                                               "phantomjs-advanced"
                                               ["runners/phantomjs.js" "target/test/js/testable.js"]}
                               :builds
                               {:ws-unit-tests
                                {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable_dbg.js"
                                            :optimizations :whitespace
                                            :pretty-print true}}
               
                                :simple-unit-tests
                                { :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable_pre.js"
                                            :optimizations :simple
                                            :pretty-print false}}
               
                                :advanced-unit-tests
                                {:source-paths ["src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable.js"
                                            :optimizations :advanced
                                            :pretty-print false}}
               
                                :dev
                                {:source-paths ["src/brepl" "src/cljs"]
                                 :compiler {:output-to "resources/public/js/modern_dbg.js"
                                            :optimizations :whitespace
                                            :pretty-print true}}
                                
                                :pre-prod
                                {:source-paths ["src/brepl" "src/cljs"]
                                 :compiler {:output-to "resources/public/js/modern_pre.js"
                                            :optimizations :simple
                                            :pretty-print false}}
                                
                                :prod
                                {:source-paths ["src/cljs"]

                                 :compiler {:output-to "resources/public/js/modern.js"
                                            :optimizations :advanced
                                            :pretty-print false}}}}
  
                   :aliases {"clean-test!" ["do" "clean," "cljx" "once," "compile," "test"]
                             "clean-start!" ["do" "clean," "cljx" "once," "compile," "ring" "server-headless"]}            
                   
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :injections [(require '[cljs.repl.browser :as brepl]
                                         '[cemerick.piggieback :as pb])
                                (defn browser-repl []
                                  (pb/cljs-repl :repl-env
                                                (brepl/repl-env :port 9000)))]}})
```

> NOTE 9: To clean the `out` directory generated by the calling the
> `(browser-brepl)` call from a `lein repl` session, we added to the
> `:dev` profile the `:clean-targets ["out"]` option.

The following *nix command (sorry for MS Windows users, but I don't
know that OS) allows you to verify the differences between the `:user`
and the `:dev` profiles.

```bash
diff <(lein with-profiles user pprint) <(lein with-profiles dev pprint)
1c1
< Performing task 'pprint' with profile(s): 'user'
---
> Performing task 'pprint' with profile(s): 'dev'
7a8,9
>  :repl-options
>  {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]},
17c19,21
<   [enlive/enlive "1.1.4"]),
---
>   [enlive/enlive "1.1.4"]
>   [com.cemerick/clojurescript.test "0.0.4"]
>   [com.cemerick/piggieback "0.1.0"]),
31a36,85
>  :cljsbuild
>  {:builds
>   ...
>   :crossovers
>   ...
>   :test-commands
>   ...},
32a87
>  :hooks [leiningen.cljsbuild],
124a180,187
>  :cljx
>  {:builds
>   ...},
127,130c190,199
<   [lein-try/lein-try "0.3.1"]
<   [lein-pprint/lein-pprint "1.1.1"]
<   [lein-ancient/lein-ancient "0.4.4"]
<   [lein-bikeshed/lein-bikeshed "0.1.3"]),
---
>   [lein-cljsbuild/lein-cljsbuild "0.3.3"]
>   [com.keminglabs/cljx "0.3.0"]),
>  :injections
>  [...
>   ...],
133c202,204
<  :test-paths ("/Users/mimmo/tmp/modern-cljs/test"),
---
>  :test-paths
>  ("/Users/mimmo/tmp/modern-cljs/target/test/clj"
>   "/Users/mimmo/tmp/modern-cljs/test"),
135c206,209
<  :aliases nil}
---
>  :aliases
>  {"clean-start!"
>   ["do" "clean," "cljx" "once," "compile," "ring" "server-headless"],
>   "clean-test!" ["do" "clean," "cljx" "once," "compile," "test"]}}
```

The description contained in this tutorial about the project map
generated by leiningen and its interaction with profiles is just a
starting point of the life clycle management of a CLJ/CLJS project. As
we'll see in subsequent tutorials, when we'll afford the packaging and
the deployment of a CLJ/CLJS project, the things are going to be even
more elaborated.

### Light the fire

Let's verify that everything is still working as expected by
restarting the project from a clean environment.

```bash
lein clean-start!
```

Now open a new terminal command, `cd` into the main project directory,
launch the the nREPL and finally call the `browser-repl` function.

```bash
lein repl
nREPL server started on port 50670
REPL-y 0.2.0
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)

user=> (browser-repl)
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=>
```

To activate the bREPL visit a page containing the JS script emitted by
the `:dev` or the `:pre-prod` cljsbuild (e.g. [shopping-dbg.html][21])

As a very last step, I suggest you to commit the changes as follows:

```bash
git add .
git commit -m "housekeeping"
```

That's all. Stay tuned for the next tutorial of the series.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md
[3]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[5]: https://github.com/technomancy/leiningen
[6]: https://github.com/emezeske/lein-cljsbuild
[7]: https://github.com/cemerick/piggieback/blob/master/README.md
[8]: https://github.com/clojure/tools.nrepl
[9]: https://github.com/eslick
[10]: http://ianeslick.com/2013/05/17/clojure-debugging-13-emacs-nrepl-and-ritz/
[11]: https://github.com/cemerick
[12]: https://github.com/cemerick/piggieback
[13]: http://localhost:3000/shopping-dbg.html
[14]: https://github.com/technomancy/leiningen/blob/stable/doc/PROFILES.md
[15]: https://github.com/rkneufeld/lein-try
[16]: https://github.com/technomancy/leiningen/tree/master/lein-pprint
[17]: https://github.com/xsc/lein-ancient
[18]: https://github.com/dakrone/lein-bikeshed
[19]: https://github.com/clojure/tools.nrepl#middleware
[20]: https://github.com/lynaghk/cljx
[21]: http://localhost:3000/shopping-dbg.html
