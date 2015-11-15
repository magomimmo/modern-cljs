# Tutorial 18 - Housekeeping!

In the [previous tutorial][1] we injected form validators into the
WUI (Web User Interface) in such a way that the user will be
notified with the corresponding error messages when she/he enters
invalid values. Continuing with our progressive enhancement
strategy, we started to inject the form validators into the
server-side-only code first.

## Introduction

Instead of immediately going back to the client-side to inject the
same validators into the CLJS code, in this tutorial we're going to
digress about two topics:

* setting up a more comfortable browser REPL, as compared to the
  one covered in previous tutorials (i.e., [Tutorial 2][2],
  [Tutorial 3][3] and [Tutorial 7][4])
* setting up a more comfortable project structure by using
  [Leiningen][5] `profiles`.

> NOTE 1: This tutorial requires a leiningen version `>=
> 2.2.0`, excluding version `2.5.0`. Version `2.5.1` has been confirmed to work.
<!-- See https://github.com/technomancy/leiningen/issues/1757 -->
> You can upgrade leiningen to the latest
> available version by issuing the following command at the terminal:
>
> ```bash
> lein upgrade
> ```

> To verify your current leiningen version, use the following
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


> NOTE 3: I also suggest that you quickly review the previous tutorials mentioned above if
> you did not read them, or you don't remember the covered topics.

## The need for a more comfortable bREPL experience

In [Tutorial 2][2], [Tutorial 3][3] and [Tutorial 7][4] we
explained how to set up and use a browser connected REPL (i.e., bREPL)
to enable an interactive, dynamic style of programming.

That said, the above bREPL configuration is subject to a few
limitations inherited from the underlying default CLJS REPL,
internally used by the [lein-cljsbuild][6] plugin.

> NOTE 4: See [Piggieback README][7] for a brief list of the
> limitations of the default CLJS REPL.

In this tutorial we're going to set up a more
capable bREPL to be run from a Clojure/JVM [nREPL][8] launched by [Leiningen][5]
via the `lein repl` task.

As pointed out by [Ian Eslick][9] in his very useful [tutorial][10] on
setting up a Clojure debugging environment,

> nREPL is a tooling framework for allowing editors (clients) to
> connect to running Clojure instances (servers) to utilize
> information in the environment to navigate code, complete symbols,
> and dynamically evaluate code...It also defines a middleware
> framework to add functionality on top of the basic definitions of
> transport and minimal methods to support a REPL.

Once again, we reference the work of [Chas Emerick][11] who
created [Piggieback][12], a bREPL implemented as an
[nREPL middleware][19], which allows you to launch a CLJS bREPL session on
top of an [nREPL][8] session to overcome the limitations of the
default CLJS REPL.

## bREPL setup with Piggieback

The setup of a bREPL based on [Piggieback][12] nREPL middleware is not
cumbersome at all, but does require us to scrupulously follow a few steps.

The first of them, the creation of a pair of CLJS/HTML files that
enable the connection between the JS engine of the browser and a REPL,
has been already described in [Tutorial 2][2] and updated in
[Tutorial 3][3] and [Tutorial 7][4] of this series. Be happy; it remains
the same for the nREPL-based bREPL too.

The second step consists of:

* adding the [Piggieback][12] nREPL middleware to the project
  dependencies, and
* configuring the `:nrepl-options` of the project.

The third and final step requires:

* launching the nREPL via the `lein repl` task, and
* running the bREPL from the active nREPL session.

### STEP 1

Already done in the cited tutorials (i.e., [Tutorial 2][2],
[Tutorial 3][3] and [Tutorial 7][4]).

### Step 2

Open `project.clj` and update project dependencies as follows:

```clj
(defproject
  ...
  ...
  :dependencies [...
                 ...
                 [com.cemerick/piggieback "0.1.2"]
                 ...
                 ...]
  ...
  ...)
```

Then we have to configure the project `:repl-options` as follows:

```clj
(defproject
  ...
  ...
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  ...
  ...)
```

Here we add the `cemerick.piggipack/wrap-cljs-repl` middleware to
the stack of nREPL middleware used by `lein repl` when starting an
nREPL session.

### Step 3 - Run the bREPL

We can now run a bREPL session on top of an nREPL session.

We will:

* start from a clean environment
* regenerate the unit tests for both CLJ and CLJS via the `cljx`
  plugin
* recompile all the CLJS builds, and
* launch the ring server.

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

> NOTE 5: Don't worry if you receive `*WARNING*` messages
> during CLJS compilation. Considering that older CLJS
> builds don't define the `cljs.core/set-print-fn!` function,
> the `clojurescript.test` lib defines it in the
> `cemerick.cljs.test` namespace instead, but when you're running a newer CLJS
> build that function is already defined.

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
[Piggieback][12] instructions:

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
called a bREPL, which acts as the client side component of the connection
with the JS engine hosted by your browser, which acts as the
corresponding server-side component.

The final step, as we already explained in [Tutorial 2][2],
consists of activating the bREPL session by visiting one of the pages
(e.g., [shopping-dbg.html][13]) of the development or
pre-production builds.

> NOTE 6: In [Tutorial 7][4] we explained how to exclude a bREPL
> connection from your production CLJS build.

Wait a few moments to allow the client and the server components to
establish the connection (on port 9000) and then you can start REPLing
with the browser again as you did in the past tutorials.

To return the control to the underlying nREPL session, just exit the
bREPL session.

```cljs
cljs.user=> :cljs/quit
:cljs/quit
user=>
```

To exit the nREPL just run `(quit)` as usual.

### A little bit of bREPL automation

Even if we have been able to create a bREPL session on top of an nREPL
session, the need to manually start a new bREPL session each
time we need to launch it is very boring.

One possible solution for automating such a tedious activity is to use
the `:injections` option of [Leiningen][5] as follows:

```clj
(defproject
  ...
  ...
  :injections [(require '[cljs.repl.browser :as brepl]
                        '[cemerick.piggieback :as pb])
               (defn browser-repl []
                 (pb/cljs-repl :repl-env
                               (brepl/repl-env :port 9000)))]
  ...
  ...)
```

The value of the `:injections` option is a vector of CLJ forms that
are evaluated sequentially in the default CLJ namespace (i.e., `user`)
immediately after the start of each nREPL session.

After having required the `cljs.repl.browser` and
`cemerick.piggieback` namespaces, we define the `browser-repl`
function to wrap the already discussed creation of a bREPL client
session ready to connect with the waiting server counterpart in
the browser.

If you did not quit the previous bREPL session, do so now, relaunch it
and then call the `browser-repl` function we defined above in the
`:injections` option of [Leiningen][5]:

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
the pages from the `dev` or the `pre-prod` builds.

## The need for a more comfortable project structure

As we walked through the tutorials of the series we started adding
plugins, dependencies and other esoteric options to the `project.clj`
file, which ended up being long and convoluted.

At the same time, even if we added the `:hooks [leiningen.cljsbuild]`
option to our `project.clj` file, we still need to issue more lein
tasks to be able to clean, compile and run the project and its
tests.

### Cljx hooks

As the [lein-cljsbuild][6] does, the [cljx][20] lein plugin offers a
way to hook some of its subtasks (e.g., `cljx once`) to built-in leiningen tasks
(e.g., `clean` and `compile`). You just need to add it in the `:hooks`
option in `project.clj` as follows:

```clj
(defproject ...
  ...
  :hooks [cljx.hooks leiningen.cljsbuild]
  ...)
```

Now you can more quickly clean, compile and start the project or its
unit tests as follows. Take your time when submitting the following
chain of tasks, because they are going to execute a lot of jobs.

```bash
lein do clean, compile, test # unit testing from a clean env
```

```bash
lein do clean, compile, ring server-headless # run application from a clean env
```

Unfortunately, both of the commands above compile the CLJS unit tests twice!
The interaction between the `cljsbuild` and `cljx` hooks seems to have a bug to be
fixed.

### Aliases option

Luckily, `lein` offers an `:aliases` option which, when combined with the
`do` task for chaining lein tasks, allows us to obtain an even better
level of automation without incurring the double code
generation/compilation shown above by the buggy interaction of
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

> NOTE 7: Each command but the last has to contain a comma `,`
> inside the stringified task name (e.g., `"clean,"`), otherwise the
> next string will be interpreted as an argument of the previous task
> (e.g., "ring" "server-headless").

To list all available aliases in a project, just call `lein help`.
Near the end of the command output you should see the defined
aliases and correponding expansions.

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
lein clean-start!
```

Even if we have been able to chain a few tasks to obtain a bit of
automation, the `project.clj` file is becoming larger and more
convoluted. It contains the dependencies pertaining to the CLJ codebase,
the ones for the CLJS codebase and even those for
unit testing and supporting bREPL sessions.

The `:plugins` section is affected by the same kind of mixed role in
the project, as do a few configuration options for the plugins
themselves. Don't you think we need more separation of concerns?

### Leiningen profiles

Starting from the `"2.0.0"` release, [leiningen][5] introduced the
[profiles][14] feature, which allows you to obtain, if not a shorter
`project.clj`, at least better separation of concerns during its
life cycle.

#### User profile

Say you want a set of plugins to be available in all your local
projects managed by `lein`. I always want to have at least the
following plugins:

* [lein-try][15]: for REPLing with new libs without declaring them in
  the project's dependencies
* [lein-pprint][16]: to pretty print the entire map representation of
  `project.clj`
* [lein-ancient][17]: to check if there are available upgrades for the
  components used in the projects
* [lein-bikeshed][18]: to check the adherence of the project code to a
  small set of established and extendable CLJ idioms.

All we have to do is to create a `profiles.clj` file inside the
`~/.lein` directory and write the following declarations:

```clj
{:user {:plugins [[lein-try "0.3.2"]
	              [lein-pprint "1.1.1"]
                  [lein-ancient "0.4.4"]
                  [lein-bikeshed "0.1.3"]]}}
```

If you now run the `lein pprint` command from a project home
directory, you'll get the entire map of the project itself and you can
verify that the above user level plugins have been merged with the
ones declared in your `project.clj` file.

```bash
lein pprint
{...
 ...
 :plugins
 ([lein-ring/lein-ring "0.8.8"]
  [lein-cljsbuild/lein-cljsbuild "0.3.4"]
  [com.cemerick/clojurescript.test "0.2.1"])
  [com.keminglabs/cljx "0.3.0"]
  [lein-try/lein-try "0.3.2"]
  [lein-pprint/lein-pprint "1.1.1"]
  [lein-ancient/lein-ancient "0.4.4"]
  [lein-bikeshed/lein-bikeshed "0.1.3"]),
 ...}
```

This is just the beginning of the profiles story.

#### Dev profile

As we said above, our `project.clj` mixes dependencies, plugins and
project configurations pertaining to different aspects of the project.

[Leiningen][14] predefines a few profiles, including the `:user`
profile (specified in `~/.lein/profiles.clj` above).
A second predefined profile is `:dev`,
which could be useful in our efforts to achieve separation of
concerns in `project.clj`

For example, the `piggieback` dependency and the corresponding
`:repl-options` and `:injections` configurations all have to do
with the development activities by enabling the bREPL on top of an
nREPL.

By adding a `:profiles` section to `project.clj` we can start
separating those options pertaining to development activities. Just
move them into a `:dev` profile as follows:

```clj
(defproject
  ...
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :injections [(require '[cljs.repl.browser :as brepl]
                                         '[cemerick.piggieback :as pb])
                                (defn browser-repl []
                                  (pb/cljs-repl :repl-env
                                                (brepl/repl-env :port 9000)))]}}
```

By using the `lein with-profiles` task you can easly verify the
differences between the `:user` and the `:dev` project maps after the
changes above.

> NOTE 8: Run `lein help with-profiles` to see the features of this task.
>
> ```bash
> lein help with-profiles
> Apply the given task with the profile(s) specified.
>
> Comma-separated profiles may be given to merge profiles and perform the task.
> Colon-separated profiles may be given for sequential profile task application.
>
> A profile list may either be a list of profiles to use, or may specify the
> profiles to add or remove from the active profile list using + or - prefixes.
>
> For example:
>
>      lein with-profile user,dev test
>      lein with-profile -dev test
>      lein with-profile +1.4:+1.4,-dev:base,user test
>
> To list all profiles or show a single one, see the show-profiles task.
> For a detailed description of profiles, see `lein help profiles`.
>
> Arguments: ([profiles task-name & args])
> ```

```bash
lein with-profiles user pprint
```

```bash
lein with-profiles dev pprint
```

That said, there are more sections and configurations that
can be moved under the `:dev` profile to improve the separation of
concerns of the modern-cljs `project.clj` file.

The choice about what to keep in the `:default` profile and what to
move to other predefined profiles or even to user-defined profiles, is
project dependent, subjective and even dependent on phases of the
project lifecycle we have not yet considered (e.g., project packaging and
deployment).

At the moment, a possible choice could be to move into the `:dev`
profile everything specifically to do with the CLJS unit testing
activities. As said, this choice is subject to change during the
project lifecycle and you should consider it just as a staring point.

* `com.keminglabs/cljx` plugin and configuration: at the
  moment we're using them only to generate unit testing codebase for
  both CLJ and CLJS;
* `com.cemerick/clojurescript.test` plugin: it is only
  used in unit tests;
* `lein-cljsbuild` build configurations for CLJS unit testing:
  they are only used only to emit testable JS codebase;
* `:aliases` section: both aliases chain the `cljx` task,
  which we're going to move into the `:dev` profile.

Finally we could move to the `:dev` profile the `lein-cljsbuild`
builds configurations declared to support the `:whitespace` and
`:simple` optimization levels as well. As we'll see in a subsequent
tutorial, `cljsbuild` offers `:crossover-jar` and `:jar` options to
specify which `:source-paths` CLJS pathnames to include when building a `jar`
package to be published or distributed to third parties. At the moment
we don't care about this.

Here is the resulting `project.clj` file obtained by moving all this
stuff to the `:dev` profile.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "A series of tutorials on ClojureScript"
  :url "https://github.com/magomimmo/modern-cljs"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :pom-addition [:developers [:developer
                              [:id "magomimmo"]
                              [:name "Mimmo Cosenza"]
                              [:url "https://github.com/magomimmo"]
                              [:email "mimmo.cosenza@gmail.com"]
                              [:timezone "+2"]]]

  :test-paths ["target/test/clj"]

  :min-lein-version "2.2.0"

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2069"]
                 [compojure "1.1.6"]
                 [hiccups "0.2.0"]
                 [domina "1.0.3"]
                 [org.clojars.magomimmo/shoreleave-remote-ring "0.3.1-SNAPSHOT"]
                 [org.clojars.magomimmo/shoreleave-remote "0.3.1-SNAPSHOT"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]]

  :plugins [[lein-ring "0.8.8"]
            [lein-cljsbuild "1.0.0"]]

  :hooks [leiningen.cljsbuild]

  :ring {:handler modern-cljs.core/app}

  :cljsbuild {:crossovers [valip.core
                           valip.predicates
                           modern-cljs.login.validators
                           modern-cljs.shopping.validators]

              :builds {:prod
                       {:source-paths ["src/cljs"]

                        :compiler {:output-to "resources/public/js/modern.js"
                                   :optimizations :advanced
                                   :pretty-print false}}}}

  :profiles {:dev {:source-paths ["src/brepl"]
                   :test-paths ["target/test/cljs"]
                   :clean-targets ["out"]

                   :dependencies [[com.cemerick/piggieback "0.1.2"]]

                   :plugins [[com.keminglabs/cljx "0.3.0"]
                             [com.cemerick/clojurescript.test "0.2.1"]]

                   :cljx {:builds [{:source-paths ["test/cljx"]
                                    :output-path "target/test/clj"
                                    :rules :clj}

                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test/cljs"
                                    :rules :cljs}]}

                   :cljsbuild {:test-commands {"phantomjs-whitespace"
                                               ["phantomjs" :runner "target/test/js/testable_dbg.js"]

                                               "phantomjs-simple"
                                               ["phantomjs" :runner "target/test/js/testable_pre.js"]

                                               "phantomjs-advanced"
                                               ["phantomjs" :runner "target/test/js/testable.js"]}
                               :builds
                               {:dev
                                {:source-paths ["src/brepl" "src/cljs"]
                                 :compiler {:output-to "resources/public/js/modern_dbg.js"
                                            :optimizations :whitespace
                                            :pretty-print true}}

                                :pre-prod
                                {:source-paths ["src/brepl" "src/cljs"]
                                 :compiler {:output-to "resources/public/js/modern_pre.js"
                                            :optimizations :simple
                                            :pretty-print false}}

                                :ws-unit-tests
                                {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable_dbg.js"
                                            :optimizations :whitespace
                                            :pretty-print true}}

                                :simple-unit-tests
                                {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable_pre.js"
                                            :optimizations :simple
                                            :pretty-print false}}

                                :advanced-unit-tests
                                {:source-paths ["src/cljs" "target/test/cljs"]
                                 :compiler {:output-to "target/test/js/testable.js"
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

> NOTE 9: To remove the `out` directory generated by calling the
> `(browser-brepl)` function from a `lein repl` session, we added to
> the `:dev` profile the `:clean-targets ["out"]` option.


> NOTE 10: We moved the `"src/brepl"` from the main Leiningen
> `:source-paths` to the corresponding `:source-path` within the
> `:dev` profile. Again, this is because it pertains to the development
> phase (probably you don't want an active bREPL in production).


> NOTE 11: We moved the `"target/test/cljs"` pathname from the main
> Leiningen `:test-paths` to the corresponding `:test-paths` in the
> `:dev` profile as well. Again, the reason why is the fact the it
> pertains to the development/testing phases of the project.

If you're curious and patient in reading output from the `diff`, the
following *nix command (sorry for any MS Windows users, but I don't
know that OS) allows you to verify the differences between the `:user`
and the `:dev` profiles.

```bash
diff -u <(lein with-profiles user pprint) <(lein with-profiles dev pprint)
```

Don't forget that the content of this part of the tutorial is just a
first attempt to introduce a little bit of separation of concerns into `project.clj`.
My personal understanding of all the nuances
of the interactions between lein profiles and tasks is still under
construction. That said, I personally think that we should significantly improve
the usability of the `lein-cljsbuild` plugin to considerably
reduce the incidental complexity of any mixed CLJ/CLJS project.

As we'll see in subsequent tutorials, when we afford the packaging
and the deployment of a CLJ/CLJS project, things are going to be
even more elaborate.

### Light the fire

Let's verify that everything is working as expected. First we want to try
the `clean-test!` alias with the `:dev` profile.

```bash
lein with-profile dev clean-test!
Performing task 'clean-test!' with profile(s): 'dev'
Deleting files generated by lein-cljsbuild.
Rewriting test/cljx to target/test/clj (clj) with features #{clj} and 0 transformations.
Rewriting test/cljx to target/test/cljs (cljs) with features #{cljs} and 1 transformations.
Compiling ClojureScript.
...
...
{:test 1, :pass 13, :fail 0, :error 0, :type :summary}
```

Well, it worked. Now I expect that running the `test` task with the
`:user` profile will cause trouble, because all the dependencies,
plugins and configuration supporting unit testing of the
project are confined to the `:dev` profile.

```bash
lein with-profiles user test
Performing task 'test' with profile(s): 'user'
Compiling ClojureScript.

lein test user

Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
Running all ClojureScript tests.
Could not locate test command .
Error encountered performing task 'test' with profile(s): 'user'
Suppressed exit
```

Nice, it works as well. On the contrary, I expect that running the
`lein with-profiles user ring server-headless`, which is not confined
to the `:dev` profile, will start the application.

```bash
lein with-profiles user ring server-headless
Performing task 'ring' with profile(s): 'user'
Compiling ClojureScript.
2013-09-17 18:12:21.169:INFO:oejs.Server:jetty-7.6.8.v20121106
2013-09-17 18:12:21.232:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
Started server on port 3000
```

Good, it works again as expected. You can verify that the application
behaves as before by visiting [shopping-dbg.html][21].

Now stop the ring server and check that the defined aliases
(i.e., `clean-test!` and `clean-start!`) are not available when issued
from the `:user` profile.

```bash
lein with-profiles user clean-test!
Performing task 'clean-test!' with profile(s): 'user'
'clean-test!' is not a task. See 'lein help'.
Error encountered performing task 'clean-test!' with profile(s): 'user'
Task not found
```

```bash
lein with-profiles user clean-start!
Performing task 'clean-start!' with profile(s): 'user'
'clean-start!' is not a task. See 'lein help'.
Error encountered performing task 'clean-start!' with profile(s): 'user'
Task not found
```

Great, it worked again as expected.

If you do not specify a profile when calling a task, `lein` activates
the `:default` profile which includes
`[:base :system :user :provided :dev]`. That's why you can still use
all the described project tasks/subtaks and aliases without
specifying the `:dev` profile.

```bash
lein ring server-headless
Compiling ClojureScript.
2013-09-16 12:36:38.746:INFO:oejs.Server:jetty-7.6.8.v20121106
2013-09-16 12:36:38.810:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
Started server on port 3000
```

```bash
lein repl #in a new terminal within the main project directory
Compiling ClojureScript.
nREPL server started on port 51254 on host 127.0.0.1
REPL-y 0.2.1
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
the `:dev` or the `:pre-prod` cljsbuild (e.g., [shopping-dbg.html][21]).

As a final step, I suggest you to commit the changes as follows:

```bash
git add .
git commit -m "housekeeping"
```

> NOTE 10: The amount of work executed by the lein tasks in this
> project is impressive. If you don't believe me, try to issue the
> `tree` command from the terminal before and after having built
> everything.

That's all. Stay tuned for the next tutorial of the series.

## Next Step - [Tutorial 19 - Livin' On the Edge][22]

In the [next tutorial][22] we are going to explain how to contribute
to someone else's repositories and how to publish snapshot releases on
clojars for use without your own projects.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
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
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
