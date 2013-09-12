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
		         [org.clojure/clojurescript "0.0-1847"]
                 [com.cemerick/piggieback "0.1.0"]
                 ...
                 ...]
  ...
  ...)
```

Note that we needed to add a version of CLJS which is equal or
newer than `"0.0-1835"`. This is because, starting from the
`"0.0.5"` version, [Piggieback][12] is not compatible with the
CLJS version implicitely used by the current stable version of the
[Lein-cljsbuild][6] plugin.

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
file which ended up being long and confusing. Take a look at the
latest `project.clj` if you don't believe me.

[Leiningen Profiles][14] offer a very handy and articulated approach
for simplifying the writing and mostly the reading of a project
declaration without losing any expressive power of the map
representing the full project.

### Global profiles

> ATTENTION: This part of the tutorial is no more valid. It will be
> updated in the near future because it is based on a
> misinterpretation of the featerues of [lein profiles][14]
> features. Sorry about that.

Say you want a set of plugins to be available in all your local
projects managed by `lein`. I always want to have few plugins.

For example:

* [lein-try][15]: for REPling with new libs without declaring them in
  the projects' dependencies
* [lein-pprint][16]: to pretty print the entire map representation of
  the projects
* [lein-ancient][17]: to check if there are available upgrades for the
  components used in the projects
* [lein-bikeshed][18]: to check the adherence of the project code to a
  small set of established CLJ idioms that you can extend.

All we have to do is to create a `profiles.clj` file inside the
`~/.lein` directory and write the following declarations:

```clj
{:user {:plugins [[lein-try "0.3.0"]
	              [lein-pprint "1.1.1"]
                  [lein-ancient "0.4.4"]
                  [lein-bikeshed "0.1.3"]]}}
```

If you now run the `lein pprint` command from a project home
directory, you'll get the entire map of the project itself.

You can then verify that the above plugins have been merged with the
declaration you set in the `project.clj` file. Just remember that the
eventual local profiles (e.g. inside a `project.clj`) take precedence
over the global ones.

## Local profiles

I always try to keep in the `project.clj` file only what is absolutely
needed to be easily read, but mostly what remains after having removed
everthing that has to do with the needed development libs/tools.

Here is a version of the `project.clj` file corresponding to the
latest state we reached in the first part ot this tutorial and cleaned
from anything that has to do with libs/tools supporting the
development phase.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.2.0"
  
  :source-paths ["src/clj"]
  :test-paths ["target/test/clj"]
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [domina "1.0.2-SNAPSHOT"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]]
  
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler modern-cljs.core/app})
```

Judge by yourself how much readble is this cleaned version of the
`project.clj` file if compared with the previous one.

That said, if you want to add a local `:profiles` section in the
`project.clj` file you can do it, but you're going to dirty the
readability of its content.

## Project separated profiles

Ok, we removed any lib/tool supporting the development phase, but
where we have to put them?

In a project specific `profiles.clj` file hosted in the main directory
of the project itself.

Following is the content of the project specific `profiles.clj` file
containing all the components we removed from the initial version of
the `project.clj` descriptor.

```clj
{:dev {:hooks [leiningen.cljsbuild]

       :dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                      [com.cemerick/piggieback "0.1.0"]]
       
       :plugins [[lein-cljsbuild "0.3.2"]
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
                   ;; for unit testing with phantomjs
                   :test-commands {"phantomjs-whitespace"
                                   ["runners/phantomjs.js" "target/test/js/testable_dbg.js"]

                                   "phantomjs-simple"
                                   ["runners/phantomjs.js" "target/test/js/testable_pre.js"]

                                   "phantomjs-advanced"
                                   ["runners/phantomjs.js" "target/test/js/testable.js"]}
                   :builds
                   {:ws-unit-tests
                    { ;; clojurescript source code path
                     :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]

                     ;; Google Closure Compiler options
                     :compiler { ;; the name of emitted JS script file
                                :output-to "target/test/js/testable_dbg.js"

                                ;; minimum optimization
                                :optimizations :whitespace
                                ;; prettyfying emitted JS
                                :pretty-print true}}
               
                    :simple-unit-tests
                    { ;; same path as above
                     :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]

                     :compiler { ;; different JS output name
                                :output-to "target/test/js/testable_pre.js"

                                ;; simple optimization
                                :optimizations :simple

                                ;; no need prettification
                                :pretty-print false}}
               
                    :advanced-unit-tests
                    { ;; same path as above
                     :source-paths ["src/cljs" "target/test/cljs"]

                     :compiler { ;; different JS output name
                                :output-to "target/test/js/testable.js"

                                ;; advanced optimization
                                :optimizations :advanced

                                ;; no need prettification
                                :pretty-print false}}
               
                    :dev
                    { ;; clojurescript source code path
                     :source-paths ["src/brepl" "src/cljs"]

                     ;; Google Closure Compiler options
                     :compiler { ;; the name of emitted JS script file
                                :output-to "resources/public/js/modern_dbg.js"

                                ;; minimum optimization
                                :optimizations :whitespace
                                ;; prettyfying emitted JS
                                :pretty-print true}}
                    :pre-prod
                    { ;; same path as above
                     :source-paths ["src/brepl" "src/cljs"]

                     :compiler { ;; different JS output name
                                :output-to "resources/public/js/modern_pre.js"

                                ;; simple optimization
                                :optimizations :simple

                                ;; no need prettification
                                :pretty-print false}}
                    :prod
                    { ;; same path as above
                     :source-paths ["src/cljs"]

                     :compiler { ;; different JS output name
                                :output-to "resources/public/js/modern.js"

                                ;; advanced optimization
                                :optimizations :advanced

                                ;; no need prettification
                                :pretty-print false}}}}
       
       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
       
       :injections [(require '[cljs.repl.browser :as brepl]
                             '[cemerick.piggieback :as pb])
                    (defn browser-repl []
                      (pb/cljs-repl :repl-env
                                    (brepl/repl-env :port 9000)))]}}
```

Now even the needed libs/tools and configurations for supporting the
development activities are more readable than before. Do you see how
many Lines Of Code (LOC) you need to establish a development
enviroment (i.e. `profiles.clj`) as compared to the LOC needed for
describing the project structure itself (i.e. `project.clj`)? This
is a kind of incidental complexity that I'll happily give up if
I can. I'm pretty sure that sooner or later someone will reduce it.

Note also that in producing the final project description map the
project specific `profiles.clj` file takes precedence over the
eventual `:profiles` declarations within the `project.clj` file, which
in turn take precedence over the global profiles in the
`~/.lein/profiles.clj` file.

Last but not least, as reported in the
[leingen profiles documentation][14]

> The `:user` profile is separate from `:dev`; the latter is intended
> to be specified in the project itself. In order to avoid collisions,
> the project should never define a :user profile, nor should a global
> :dev profile be defined. Use the show-profiles task to see what's
> available.

You can check the content of the project map associated with a profile
by issuing the following `lein with-profile` command:

```bash
lein with-profile user pprint
Performing task 'pprint' with profile(s): 'user'
{...
 :dependencies
 ([org.clojure/clojure "1.5.1"]
  [org.clojure/clojurescript "0.0-1847"]
  [compojure/compojure "1.1.5"]
  [hiccups/hiccups "0.2.0"]
  [domina/domina "1.0.2-SNAPSHOT"]
  [shoreleave/shoreleave-remote-ring "0.3.0"]
  [shoreleave/shoreleave-remote "0.3.0"]
  [com.cemerick/valip "0.3.2"]
  [enlive/enlive "1.1.4"]),
 ...
 :eval-in :subprocess,
 :plugins
 ([lein-ring/lein-ring "0.8.7"]
  [lein-pprint/lein-pprint "1.1.1"]
  [lein-ancient/lein-ancient "0.4.4"]
  [lein-bikeshed/lein-bikeshed "0.1.3"]
  [lein-try/lein-try "0.3.0"]),
  ...
  ...}
```

and

```bash
lein with-profile dev pprint
Performing task 'pprint' with profile(s): 'dev'
{...
 :repl-options
 {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]},
 :dependencies
 ([org.clojure/clojure "1.5.1"]
  [org.clojure/clojurescript "0.0-1847"]
  [compojure/compojure "1.1.5"]
  [hiccups/hiccups "0.2.0"]
  [domina/domina "1.0.2-SNAPSHOT"]
  [shoreleave/shoreleave-remote-ring "0.3.0"]
  [shoreleave/shoreleave-remote "0.3.0"]
  [com.cemerick/valip "0.3.2"]
  [enlive/enlive "1.1.4"]
  [com.cemerick/clojurescript.test "0.0.4"]
  [com.cemerick/piggieback "0.1.0"]),
  ...
 :cljsbuild
 {:crossovers
  [valip.core
   valip.predicates
   modern-cljs.login.validators
   modern-cljs.shopping.validators],
  :test-commands
  {"phantomjs-whitespace"
   ["runners/phantomjs.js" "target/test/js/testable_dbg.js"],
   "phantomjs-simple"
   ["runners/phantomjs.js" "target/test/js/testable_pre.js"],
   "phantomjs-advanced"
   ["runners/phantomjs.js" "target/test/js/testable.js"]},
  :builds
  {:ws-unit-tests
   {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"],
    :compiler
    {:output-to "target/test/js/testable_dbg.js",
     :optimizations :whitespace,
     :pretty-print true}},
   :simple-unit-tests
   {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"],
    :compiler
    {:output-to "target/test/js/testable_pre.js",
     :optimizations :simple,
     :pretty-print false}},
   :advanced-unit-tests
   {:source-paths ["src/cljs" "target/test/cljs"],
    :compiler
    {:output-to "target/test/js/testable.js",
     :optimizations :advanced,
     :pretty-print false}},
   :dev
   {:source-paths ["src/brepl" "src/cljs"],
    :compiler
    {:output-to "resources/public/js/modern_dbg.js",
     :optimizations :whitespace,
     :pretty-print true}},
   :pre-prod
   {:source-paths ["src/brepl" "src/cljs"],
    :compiler
    {:output-to "resources/public/js/modern_pre.js",
     :optimizations :simple,
     :pretty-print false}},
   :prod
   {:source-paths ["src/cljs"],
    :compiler
    {:output-to "resources/public/js/modern.js",
     :optimizations :advanced,
     :pretty-print false}}}},
 :ring {:handler modern-cljs.core/app},
 :hooks [leiningen.cljsbuild],
 ...
 :cljx
 {:builds
  [{:source-paths ["test/cljx"],
    :output-path "target/test/clj",
    :rules :clj}
   {:source-paths ["test/cljx"],
    :output-path "target/test/cljs",
    :rules :cljs}]},
 :plugins
 ([lein-ring/lein-ring "0.8.7"]
  [lein-cljsbuild/lein-cljsbuild "0.3.2"]
  [com.keminglabs/cljx "0.3.0"]),
 :injections
 [(require
   '[cljs.repl.browser :as brepl]
   '[cemerick.piggieback :as pb])
  (defn
   browser-repl
   []
   (pb/cljs-repl :repl-env (brepl/repl-env :port 9000)))],
 ...
 :test-paths ("/Users/mimmo/Developer/modern-cljs/target/test/clj"),
 ...}
```

As you can see, while the project map for the `user` profile merged
the declaration contained in the global `profiles.clj` file with the
declaration contained in the `project.clj` file, the project map for
the `dev` profile merged also the content of the declarations
contained in the project-specific `profiles.clj` file.

## Light the fire

Ok, enough words. Let's verify that everything is still working as
expected by restarting the project from a clean environment.

```bash
lein do clean, cljx, compile, test, lein ring server-headless 
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
the `:dev` or the `:pre-prod` cljsbuild.

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

