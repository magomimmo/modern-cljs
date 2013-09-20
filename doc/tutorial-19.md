# Tutorial 19 - Survival guide for livin' on the edge 

In the [previous tutorial][1] we afforded two topics:

* the adption of the [pieggieback][2] lib to improve the bREPLing
  experience with CLJS;
* the introduction of the [lein profiles][3] features in the
  `project.clj` file to support a better separation of concerns during
  the lifecycle of a CLS/CLJS mixed project.

## Introduction

When in your project you start using a lib implemented by others, you
can easily end up with few misunderstandings of its use or even with
some unexpected issues. In these cases the first thing you should do
is to browse and read its documentation. As you now, one of the most
frequent problem with most open source software regards the
corresponding documentation which is frequently minimal, if not
absent, out-of-date respeting the codebase or requiring a level of
comprehension of the details which you still have to grasp.

Likely, most of the CLJ/CLJS open source libs are hosted on github
which offers an amazing support to social coding. Even if few CLJ/CLJS
libs have an extensive documentation and/or an associated mailing-list
for submitting dubts and questions, every lib hosted on github is
supported by an articulated, although easy, issue and version control
management systems. Those two systems help a lot in managing almost
any kind of distributed and remote collaboration required by an open
source software.

This tutorial is composed of two parts:

* Livin' on the edge (Part 1). In this part we're going to update the
  depenendencies of a set of libs and we'll also fix an issue of a lib
  on which the `modern-cljs` depends on;
* Surviving guide (Part 2). In this part we're going to see what to do
  when the responsiveness of the owner of a lib for which we have
  submitted a push request is not compatible with our project
  schedule.

## Livin' on the edge

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-18
> git checkout -b tutorial-19-step-1
> ```

As you remember, in the [Tutorial 10 - Introducing Ajax][4] we added
to the project's dependencies the [shoreleave-remote][5] and the
[shoreleave-remote-ring][6] libs.

Even if those set of [shoreleave][7] libs did not create any issue in
our project, I always like to use libs which are up-to-date with the
latest available release of the external libs they use.

If you take a look at the `project.clj` file of the
[shoreleave-remote][5] and the [shoreleave-remote-ring][6], you'll
discover that they are both based on the obsolete [lein 1][8] release
and on the `1.4` release of Clojure.

Here is the [shoreleave-remote][8] `project.clj`:

```clj
(defproject shoreleave/shoreleave-remote "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [shoreleave/shoreleave-core "0.3.0"]
                 [shoreleave/shoreleave-browser "0.3.0"]]
  :dev-dependencies [;[cdt "1.2.6.2-SNAPSHOT"]
                     ;[lein-cdt "1.0.0"] ; use lein cdt to attach
                     ;[lein-autodoc "0.9.0"]
                     [lein-marginalia "0.7.1"]])
```

And here is the [shoreleave-remote-ring][9] `project.clj`:

```clj
(defproject shoreleave/shoreleave-remote-ring "0.3.0"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/shoreleave/shoreleave-remote-ring"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.reader "0.7.0"]]
  :dev-dependencies [;[cdt "1.2.6.2-SNAPSHOT"]
                     ;[lein-cdt "1.0.0"] ; use lein cdt to attach
                     ;[lein-autodoc "0.9.0"]
                     [lein-marginalia "0.7.1"]])
```

You detect the fact that they both depend on `lein 1` from two
symptoms:

* the presence of the `:dev-dependencies` section which, in `lein 2`,
  has been deprecated by the introduction of [lein profiles][10];
* the presence of the `lein-cdt` plugin, which has been integrated
  into [swank-clojure][11] a lot of time ago and it now deprecated
  too.

You should also note that the `shoreleave-remote` lib depends on the
[shoreleave-core][12] and the [shoreleave-browser][13] libs as well,
while the `shoreleave-remote-ring` lib further depends on the
[org.clojure.tool-reader][14].

Nothing to worry about too much but, as I said, I prefer to use libs
that are frequently updated and maintained and this is a good
opportunity to be cooperative with the open source community.
Generally speaking, the updating of the dependencies of a project
requires little efforts which even a Clojure beginner can afford.

### Fork and clone

I always fork and clone the libs I used in my projects. And I strongly
suggest to you to do the same.

Start by [forking][15] the [shoreleave-core][12] repo and locally
cloning the forked repo.

Then edit its `project.clj` as follow:

```clj
(defproject shoreleave/shoreleave-core "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's core auxiliary functions"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

> NOTE 2: I added the `:min-lein-version "2.0.0"` option because we are
> now using the `:profiles` features which is available starting from
> the `lein 2.0.0` release.

> NOTE 3: I kept the [lein-marginalia][16] plugin in the `:dev` profile,
> even if I would have preferred to move it in the `:user` profile
> inside the `~/.lein/profiles.clj`.

> NOTE 4: the `shoreleave` set of libs does not follow the usual habit
> of creating a tag for each release. The master branch should have been
> named `0.3.1-SNAPSHOT` instead.

*Mutatis mutandis*, repeat the same changes in the
 `shoreleave-browser`, `shoreleave-remote` and
 `shoreleave-remote-ring`.

```clj
;;; shoreleave-browser
(defproject shoreleave/shoreleave-browser "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's enhanced browser utilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [shoreleave/shoreleave-core "0.3.0"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

```clj
;;; shoreleave-remote
(defproject shoreleave/shoreleave-remote "0.3.0"
  :description "A smarter client-side with ClojureScript : Shoreleave's rpc/xhr/jsonp facilities"
  :url "http://github.com/shoreleave"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [shoreleave/shoreleave-core "0.3.0"]
                 [shoreleave/shoreleave-browser "0.3.0"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

```clj
(defproject shoreleave/shoreleave-remote-ring "0.3.0"
  :description "A smarter client-side with ClojureScript : Ring- (and Compojure-) server-side Remotes support"
  :url "https://github.com/shoreleave/shoreleave-remote-ring"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "See the notice in README.mkd or details in LICENSE_epl.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.reader "0.7.7"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.7.1"]]}})
```

> NOTE 5: I updated the `tools.reded` lib to the latest available
> release which is the `0.7.7`.



### Cljx hooks

As the [lein-cljsbuild][6] does, the [cljx][20] lein plugin offers a
way to hook some of its subtasks (e.g. `cljx once`) to leiningen tasks
(e.g. `clean` and `compile`). You just need to add it in the `:hooks`
session of your `project.clj` as follows:

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

Unfortunally, both the above commands show a double CLJS unit testing
code generation which, in turn, are compiled two times! The iteraction
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
command. Near the end of the command output you should see the defined
aliases and the correponding expansions.

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

Even if we have been able to chain few tasks to obtain a little bit of
automation, the `project.clj` file is becoming larger and
convoluted. It contains the dependencies pertaining the CLJ codebase,
the ones relative to the CLJS codebase and even the ones regarding the
unit testing and the enabling of a bREPL session.

The `:plugins` section is affected by the same kind of mixed roles in
the project, and so do the few configuration options for the plugins
themselves. Don't you think we need more separation of concerns?

### Leining profiles

Starting from the `"2.0.0"` release, [leiningen][5] introduced the
[profiles][14] feature, which allows to obtain, if not a shorter
`project.clj`, at least a superior separation of concerns during its
life cycle.

#### User profile

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
one, which could be useful in approaching the mentioned separation of
concerns in the `project.clj`

For example, the `piggieback` dependency and the corresponding
`:repl-options` and `:injections` configurations they all have to do
with the development activities by enabling the bREPL on top of an
nREPL.

By adding a `:profiles` section to the `project.clj` file we can start
separating those stuff pertaining the development activities. Just
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

By using the `lein with-profiles` task you can easly verify the
differences between the `:user` and the `:dev` project maps after the
above changes.

> NOTE 8: run the `lein help with-profiles` to see the features of this tasks.
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

That said, there are more project's sections and configurations that
can be moved under the `:dev` profile to improve the separation of
concerns of the modern-cljs `project.clj` file.

The choice about what to keep in the `:default` profile and what to
move in other predefined profiles or even in user-defined profiles, is
project dependend, subjective and even dependent on phases of the
project lifecycle we still have to afford (e.g. project packaging and
deployment).

At the moment, a possible choice could be to move into the `:dev`
profile everything has specifically to do with the CLJS unit testing
activities. As said, this choice is subject to be changed during the
project lifecycle and you should consider it just as a staring point.

* the `com.keminglabs/cljx` plugin and configurations: because at the
  moment we're using them only to generate unit testing codebase for
  both CLJ and CLJS;
* the `com.cemerick/clojurescript.test` dependency: becasue it is only
  used in unit testing codebase;
* the `lein-cljsbuild` build configurations for CLJS unit testing:
  because they are only used only to emit testable JS codebase;
* the `:aliases` section, because they both chain the `cljx` task,
  which we're going to move into the `:dev` profile.

Finally I think that we could move to the `:dev` profile the
`lein-cljsbuild` builds configurations declared to support the
`:whitespace` and `:simple` compiler optimizations as well, because we
probably don't want to deploy in production any code that has not been
optimized to its maximum level.

Here is the resulting `project.clj` file obtained by moving all those
stuff under the `:dev` profile.

```bash
(defproject modern-cljs "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.2.0"
  
  :source-paths ["src/clj"]
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [domina "1.0.2-SNAPSHOT"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [enlive "1.1.4"]]
  
  :plugins [[lein-ring "0.8.7"]
            [lein-cljsbuild "0.3.3"]]
  
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
  
  :profiles {:dev {:test-paths ["target/test/clj"]
                   :clean-targets ["out"]
                   
                   :dependencies [[com.cemerick/clojurescript.test "0.0.4"]
                                  [com.cemerick/piggieback "0.1.0"]]
                   
                   :plugins [[com.keminglabs/cljx "0.3.0"]]
                   
                   :cljx {:builds [{:source-paths ["test/cljx"]
                                    :output-path "target/test/clj"
                                    :rules :clj}
                                   
                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test/cljs"
                                    :rules :cljs}]}
                   
                   :cljsbuild {:test-commands {"phantomjs-whitespace"
                                               ["runners/phantomjs.js" "target/test/js/testable_dbg.js"]
                                               
                                               "phantomjs-simple"
                                               ["runners/phantomjs.js" "target/test/js/testable_pre.js"]
                                               
                                               "phantomjs-advanced"
                                               ["runners/phantomjs.js" "target/test/js/testable.js"]}
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
                                { :source-paths ["src/brepl" "src/cljs" "target/test/cljs"]
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

> NOTE 9: To delete the `out` directory generated by calling the
> `(browser-brepl)` function from a `lein repl` session, we added to
> the `:dev` profile the `:clean-targets ["out"]` option.

The following *nix command (sorry for any MS Windows users, but I
don't know that OS) allows you to verify the differences between the
`:user` and the `:dev` profiles.

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
33,34c37,75
<  {:builds
<   {:prod
---
>  {:test-commands
>   {"phantomjs-simple"
>    ["runners/phantomjs.js" "target/test/js/testable_pre.js"],
>    "phantomjs-whitespace"
>    ["runners/phantomjs.js" "target/test/js/testable_dbg.js"],
>    "phantomjs-advanced"
>    ["runners/phantomjs.js" "target/test/js/testable.js"]},
>   :builds
>   {:advanced-unit-tests
>    {:source-paths ["src/cljs" "target/test/cljs"],
>     :compiler
>     {:pretty-print false,
>      :output-to "target/test/js/testable.js",
>      :optimizations :advanced}},
>    :simple-unit-tests
>    {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"],
>     :compiler
>     {:pretty-print false,
>      :output-to "target/test/js/testable_pre.js",
>      :optimizations :simple}},
>    :dev
>    {:source-paths ["src/brepl" "src/cljs"],
>     :compiler
>     {:pretty-print true,
>      :output-to "resources/public/js/modern_dbg.js",
>      :optimizations :whitespace}},
>    :ws-unit-tests
>    {:source-paths ["src/brepl" "src/cljs" "target/test/cljs"],
>     :compiler
>     {:pretty-print true,
>      :output-to "target/test/js/testable_dbg.js",
>      :optimizations :whitespace}},
>    :pre-prod
>    {:source-paths ["src/brepl" "src/cljs"],
>     :compiler
>     {:pretty-print false,
>      :output-to "resources/public/js/modern_pre.js",
>      :optimizations :simple}},
>    :prod
127a169,176
>  :cljx
>  {:builds
>   [{:source-paths ["test/cljx"],
>     :rules :clj,
>     :output-path "target/test/clj"}
>    {:source-paths ["test/cljx"],
>     :rules :cljs,
>     :output-path "target/test/cljs"}]},
131,134c180,188
<   [lein-pprint/lein-pprint "1.1.1"]
<   [lein-ancient/lein-ancient "0.4.4"]
<   [lein-bikeshed/lein-bikeshed "0.1.3"]
<   [lein-try/lein-try "0.3.1"]),
---
>   [com.keminglabs/cljx "0.3.0"]),
>  :injections
>  [(require
>    '[cljs.repl.browser :as brepl]
>    '[cemerick.piggieback :as pb])
>   (defn
>    browser-repl
>    []
>    (pb/cljs-repl :repl-env (brepl/repl-env :port 9000)))],
137,139c191,198
<  :test-paths ("/Users/mimmo/devel/modern-cljs/test"),
<  :clean-targets [:target-path],
<  :aliases nil}
---
>  :test-paths
>  ("/Users/mimmo/devel/modern-cljs/target/test/clj"
>   "/Users/mimmo/devel/modern-cljs/test"),
>  :clean-targets (:target-path "out"),
>  :aliases
>  {"clean-start!"
>   ["do" "clean," "cljx" "once," "compile," "ring" "server-headless"],
>   "clean-test!" ["do" "clean," "cljx" "once," "compile," "test"]}}
```

Don't forget that the content of this part of the tutorial is just a
first attempt to introduce into the `project.clj` a little bit of
separation of concerns. My personal understanding of all the nuances
of the interactions between the lein profiles and tasks is still under
construction. That said, I personally think that we should improve a
lot the usability of the `lein-cljsbuild` plugin to considerably
reduce the incidental complexity of any mixed CLJ/CLJS project.

As we'll see in subsequent tutorials, when we'll afford the packaging
and the deployment of a CLJ/CLJS project, the things are going to be
even more elaborated.

### Light the fire

Let's verify that everything is working as expected. First we want try
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

Well, it worked. Now I expect that by running the `test` task with the
`:user` profile we get in trouble, because all the dependensies,
plugins and configurations supporting the unit testing task of the
project are confined in the `:dev` profile.

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

Nice, it works as well. On the contrary, I expect that by running the
`lein with-profiles user ring server-headless`, which is not confined
in the `:dev` profile, the application starts.

```bash
lein with-profiles user ring server-headless
Performing task 'ring' with profile(s): 'user'
Compiling ClojureScript.
2013-09-17 18:12:21.169:INFO:oejs.Server:jetty-7.6.8.v20121106
2013-09-17 18:12:21.232:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
Started server on port 3000
```

Good is works again as expected. You can verify that the application
behaves as before by visiting the [shopping-dbg.html][21].

Now stop the ring server and check that the defined aliases
(i.e. `clean-test!` and `clean-start!`) are not available when issued
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

If you do not specify a profile while calling a task, `lein` activates
the `:default` profile which includes
`[:base :system :user :provided :dev]`. That's why you can still use
all the descripted project tasks/subtaks and aliases without
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
the :dev or the :pre-prod cljsbuild (e.g. [shopping-dbg.html][21]).

As a very last step, I suggest you to commit the changes as follows:

```bash
git add .
git commit -m "housekeeping"
```

> NOTE 10: The ammount of works executed by the lein tasks in this
> project is impressive. If you don't believe me, try to issue the
> `tree` command from the terminal before and after having built
> everything.

That's all. Stay tuned for the next tutorial of the series.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md


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
