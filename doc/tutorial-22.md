# Tutorial 22 - Learn by Contributing (Part 3)

In the [previous tutorial][1] we postponed the unit tests
implementation for the revised [Enfocus][2] lib to give more attention
at the generation of its `jar` package. We altered both the
directories layout and some `project.clj` setting until we reached a
full control of it. Then we implemented `hello-enfocus`, the simplest
`enfocus`-based project you can think about. Finally we prepared the
project to be interactively REPLed with the support of the
[piggieback][3] lib.

In this tutorial we're going to move forward in improving the
`Enfocus` lib by applying again the separation of concerns principle
to the `project.clj` file and then by starting implementing few unit
tests based on the [clojurescript.test][4] lib.

## Preamble

If you did not type by yourself all the changes we made in the cloned
`Enfocus` during the [previous tutorial][1], I suggest you to start
working by cloning and branching the following repo.

```bash
cd ~/dev
git clone https://github.com/magomimmo/enfocus.git
cd enfocus
git checkout tutorial-21
git checkout -b tutorial-22
```

## Introduction

As you have already experimented, the ammount of information to be
managed in the `project.clj` to control every aspect of a CLJ/CLJS
project tends to grow with the time spent adding features to the
project itself.

In this tutorial, before starting the unit tests implementation for
the [enfocus][2] lib, we want to find a way to make the `project.clj`
file more readable by applying few of the things we learnt in the
previous *housekeeping* tutorials.

## Divide et Impera

Take a look of the final `project.clj` we ended up in the
[previous tutorial][1].

```clj
(defproject org.clojars.magomimmo/enfocus "2.0.1-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :url "http://ckirkendall.github.io/enfocus-site"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.2.0"

  :source-paths ["src/clj"]
  :test-paths ["test/clj"]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [domina "1.0.2"]
                 [org.jsoup/jsoup "1.7.2"]]

  :plugins [[lein-cljsbuild "0.3.4"]]
  
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:crossovers [enfocus.enlive.syntax]
   :crossover-jar true

   :builds {:deploy
             {:source-paths ["src/cljs"]
              :jar true
              :compiler
              {:output-to "dev-resources/public/js/deploy.js"
               :optimizations :whitespace
               :pretty-print true}}}}

  :profiles {:dev {:resources-paths ["dev-resources"]

                   :dependencies [[com.cemerick/piggieback "0.1.0"]
                                  [ring "1.2.0"]
                                  [compojure "1.1.5"]]
                   :plugins [[com.cemerick/clojurescript.test "0.1.0"]]
                   
                   :cljsbuild 
                   {:builds {:whitespace
                             {:source-paths ["src/cljs" "test/cljs" "src/brepl"]
                              :compiler
                              {:output-to "dev-resources/public/js/whitespace.js"
                               :optimizations :whitespace
                               :pretty-print true}}

                             :simple
                             {:source-paths ["src/cljs" "test/cljs"]
                              :compiler
                              {:output-to "dev-resources/public/js/simple.js"
                               :optimizations :simple
                               :pretty-print false}}

                             :advanced
                             {:source-paths ["src/cljs" "test/cljs"]
                              :compiler
                              {:output-to "dev-resources/public/js/advanced.js"
                               :optimizations :advanced
                               :pretty-print false}}}
                    :test-commands {"whitespace"
                                    ["phantomjs" :runner "dev-resources/public/js/whitespace.js"]
                                    
                                    "simple"
                                    ["phantomjs" :runner "dev-resources/public/js/simple.js"]
                                    
                                    "advanced"
                                    ["phantomjs" :runner "dev-resources/public/js/advanced.js"]}}
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :injections [(require '[cljs.repl.browser :as brepl]
                                         '[cemerick.piggieback :as pb])
                                (defn browser-repl []
                                  (pb/cljs-repl :repl-env
                                                (brepl/repl-env :port 9000)))]}})
```

As you see the project's settings we need to set as an *enfocus
developer* overwhelm our capacity to take full control of them as an
*enfocus user*. Luckly, the `profiles` feature of `lein` is going to
be very helpful in keeping separating the view of the project as an
*enfocus developer* from its view as an *enfocus user*.

You'll be amazed by discovering how easy is to keep separated the
*enfocus user view* from the *enfocus developer view*.

### The enfocus user view

Create a new file named `profiles.clj` in the main project
directory. Open the `project.clj`, cut the whole `:profiles {:dev
{...}}` section and paste it into the `profiles.clj` newly created
file. Finally delete the `:profiles` keyword option which is already
implicitly set by the `profiles.clj` filename itself.

There is only a little minutiae to be adjusted as well. Move the
`:test-paths ["test/clj"]` from the `project.clj` to the newly created
`profiles.clj`.

Here is the resulting `project.clj`

```clj
(defproject org.clojars.magomimmo/enfocus "2.0.1-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :url "http://ckirkendall.github.io/enfocus-site"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}

  :min-lein-version "2.2.0"

  :source-paths ["src/clj"]
  
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [domina "1.0.2"]
                 [org.jsoup/jsoup "1.7.2"]]

  :plugins [[lein-cljsbuild "0.3.4"]]
  
  :hooks [leiningen.cljsbuild]

  :cljsbuild
  {:crossovers [enfocus.enlive.syntax]
   :crossover-jar true

   :builds {:deploy
             {:source-paths ["src/cljs"]
              :jar true
              :compiler
              {:output-to "dev-resources/public/js/deploy.js"
               :optimizations :whitespace
               :pretty-print true}}}})
```

Much simpler eh!.

### The efocus developer view

An here is the `profiles.clj` content:

```clj
{:dev {:test-paths ["test/clj"]
       :resources-paths ["dev-resources"]

       :dependencies [[com.cemerick/piggieback "0.1.0"]
                      [ring "1.2.0"]
                      [compojure "1.1.5"]]
       :plugins [[com.cemerick/clojurescript.test "0.1.0"]]
                   
       :cljsbuild 
       {:builds {:whitespace
                 {:source-paths ["src/cljs" "test/cljs" "src/brepl"]
                  :compiler
                  {:output-to "dev-resources/public/js/whitespace.js"
                   :optimizations :whitespace
                   :pretty-print true}}

                 :simple
                 {:source-paths ["src/cljs" "test/cljs"]
                  :compiler
                  {:output-to "dev-resources/public/js/simple.js"
                   :optimizations :simple
                   :pretty-print false}}

                 :advanced
                 {:source-paths ["src/cljs" "test/cljs"]
                  :compiler
                  {:output-to "dev-resources/public/js/advanced.js"
                   :optimizations :advanced
                   :pretty-print false}}}
        :test-commands {"whitespace"
                        ["phantomjs" :runner "dev-resources/public/js/whitespace.js"]
                                    
                        "simple"
                        ["phantomjs" :runner "dev-resources/public/js/simple.js"]
                                    
                        "advanced"
                        ["phantomjs" :runner "dev-resources/public/js/advanced.js"]}}
       :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
       :injections [(require '[cljs.repl.browser :as brepl]
                             '[cemerick.piggieback :as pb])
                    (defn browser-repl []
                      (pb/cljs-repl :repl-env
                                    (brepl/repl-env :port 9000)))]}}
```

The world in which lives the *enfocus developer* is much more complex
if compared with the world in which lives the *enfocus user* and we
don't want that an *enfocus user* has to even visually perceive that
complexity.

Take into account that even if it appears that the change has been
very easy to be done, this is only because we already kept all the
`:dev` setting, but the `:test-paths` option, separated in the
original `project.clj` and the efford we did in applying the
separation of concerns principle is now paying us back.

Don't forget that you should never set any `:user` option in the
`profiles.clj` file. Also remember that `lein` merges together
three sources of project setting:

* the `profiles.clj` file from the `~/.lein` directory: this file
  should contain `:user` setting only;
* the `profiles.clj` local to the project: this file should never
  contains any `:user` setting;
* the `project.clj` file.

Finally remember that in case of setting conflicts, the content of the
local `profiles.clj` takes precedence over the `project.clj` content
which in turn takes precedence over the `~/.lein/profiles.clj`
content.

### Light the fire

As usual after a main step ahead we like to verify that everthing is
still working as expected.

#### Clean, compile and test

```bash
lein do clean, compile, test
Deleting files generated by lein-cljsbuild.
Compiling ClojureScript.
Compiling "dev-resources/public/js/advanced.js" from ["src/cljs" "test/cljs"]...
...
Successfully compiled "dev-resources/public/js/advanced.js" in 16.323194 seconds.
Compiling "dev-resources/public/js/simple.js" from ["src/cljs" "test/cljs"]...
...
Successfully compiled "dev-resources/public/js/simple.js" in 7.01869 seconds.
Compiling "dev-resources/public/js/whitespace.js" from ["src/cljs" "test/cljs" "src/brepl"]...
...
Successfully compiled "dev-resources/public/js/whitespace.js" in 3.713015 seconds.
Compiling "dev-resources/public/js/deploy.js" from ["src/cljs"]...
Successfully compiled "dev-resources/public/js/deploy.js" in 3.191533 seconds.
Compiling ClojureScript.

lein test enfocus.server

Ran 0 tests containing 0 assertions.
0 failures, 0 errors.
Running all ClojureScript tests.

Testing enfocus.core-test

....
Ran 1 tests containing 1 assertions.
1 failures, 0 errors.
{:test 1, :pass 0, :fail 1, :error 0, :type :summary}
Subprocess failed
```

Good. The only unit test currently included with the project is the
one that we wanted to fail to remember us that we still have to define
unit tests.

#### Package

Let's now verify if by moving all that stuff from the
`project.clj` file to the `profiles.clj` file we are still in controll
of the generated `jar` package.

```clj
lein jar
Compiling ClojureScript.
Created /Users/mimmo/Developer/enfocus/target/enfocus-2.0.1-SNAPSHOT.jar
jar tvf target/enfocus-2.0.1-SNAPSHOT.jar
    92 Fri Oct 25 19:49:38 CEST 2013 META-INF/MANIFEST.MF
  4713 Fri Oct 25 19:49:38 CEST 2013 META-INF/maven/org.clojars.magomimmo/enfocus/pom.xml
   166 Fri Oct 25 19:49:38 CEST 2013 META-INF/maven/org.clojars.magomimmo/enfocus/pom.properties
   976 Fri Oct 25 19:49:38 CEST 2013 META-INF/leiningen/org.clojars.magomimmo/enfocus/project.clj
   976 Fri Oct 25 19:49:38 CEST 2013 project.clj
 11519 Fri Oct 25 19:49:38 CEST 2013 META-INF/leiningen/org.clojars.magomimmo/enfocus/README.textile
     0 Fri Oct 25 11:23:04 CEST 2013 enfocus/
     0 Fri Oct 25 11:23:04 CEST 2013 enfocus/enlive/
  1928 Fri Oct 25 11:23:04 CEST 2013 enfocus/enlive/syntax.clj
  4386 Fri Oct 25 11:23:04 CEST 2013 enfocus/macros.clj
  2109 Fri Oct 25 19:49:38 CEST 2013 enfocus/enlive/syntax.cljs
 23336 Fri Oct 25 19:49:38 CEST 2013 enfocus/core.cljs
  4247 Fri Oct 25 19:49:38 CEST 2013 enfocus/events.cljs
  6851 Fri Oct 25 19:49:38 CEST 2013 enfocus/effects.cljs
```

Good. Everthing is still resting in its own place. The last thing to
be verified is the bREPL activation.

#### bREPLing with Enfocus

Issue the following commands at the terminal window from the main project direttory:

```clj
lein repl
Compiling ClojureScript.
nREPL server started on port 54609 on host 127.0.0.1
REPL-y 0.2.1
Clojure 1.5.1
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=> (require '[enfocus.server :as http])
nil
user=> (http/run)
2013-10-25 20:07:50.503:INFO:oejs.Server:jetty-7.6.8.v20121106
2013-10-25 20:07:50.576:INFO:oejs.AbstractConnector:Started SelectChannelConnector@0.0.0.0:3000
#<Server org.eclipse.jetty.server.Server@2747ebcb>
user=> (browser-repl)
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=>
```

and then visit the [localhost:3000][5] URL to activate the bREPL
connection for interacting with the JS Engine of your browser.

```clj
cljs.user=> (+ 1 41)
42
cljs.user=> (js/alert "Hello, Enfocus")
nil
cljs.user=> :cljs/quit
:cljs/quit
user=> exit
Bye for now!
```

Not bad so far. Before making any progress towards unit testing, make
you a favor: commit the above changes.

```bash
lein clean
git add .
git commit -m "keep the user view of enfocus separated from the developer view"
```

## Unit testing Enfocus

Here we are. After a journey long two tutorials and half we finally
reached our destination from which to start again by implementing unit
tests for the `Enfocus` lib.

I discussed a lot with [Creighton Kirkendall][6] about unit testing
its lib and at the beginning we had two completly different views of
this topic.

Being `Enfocus` a live DOM manipulation lib, he worries a lot about
the need to have a visual confirmation of its correctness on any
browser. My obsession with the application of the *separation of
concerns principle* drove me instead in finding a way to keep the unit
tests which do not need a visual confirmation separated from the unit
tests which eventually need to be visualized in any browser to be
confirmed.

### Unit testing strategy

My personal unit testing strategy is very simple to be explained:

1. start unit testing from the most indipendent namespace of a lib;
2. take a look at the namespaces depending on it and annotate any
   symbol it uses of it;
3. start unit testing those symbols at the edge cases;
4. extend the unit testing of edge cases to all the public symbols of
   the choosen namespace;
5. extend the unit testing by covering the regular use cases of the
   symbols;
6. move to the next namespace with the same approach.

The steps `4.`, `5.`, and `6.` can be interleaved, depending on your
patience in filling up the regular use cases for each puplic symbol of
a namespace. My patience with unit testing is negletable and most of
the times I unit test the edge cases and very few regular uses case
only.

### The most indipendent namespace

The `Enfocus` lib defines five namespaces:

* `enfocus.core`
* `enfocus.effects`
* `enfocus.events`
* `enfocus.enlive.syntax`
* `enfocus.macros`

By looking at the namespaces' declarations the most indipendent one is
`enfocus.enlive.syntax`.

```clj
(ns enfocus.enlive.syntax)
```

The `enfocus.enlive.syntax` namespace is used by the `enfocus.macros`
and by the `enfocus.core` namespace.

```clj
(ns enfocus.macros
  (:refer-clojure :exclude [filter delay])
  (:require [clojure.java.io :as io]
            [enfocus.enlive.syntax :as syn])
  (:import [org.jsoup.Jsoup]))
```

```clj
(ns enfocus.core
  (:refer-clojure :exclude [filter delay])
  (:require [enfocus.enlive.syntax :as en]
            [goog.net.XhrIo :as xhr]
            [goog.dom.query :as query]
            [goog.style :as style]
            [goog.events :as events]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.dom.ViewportSizeMonitor :as vsmonitor]
            [goog.async.Delay :as gdelay]
            [goog.Timer :as timer]
            [clojure.string :as string]
            [domina :as domina]
            [domina.css :as dcss]
            [domina.xpath :as xpath])
  (:require-macros [enfocus.macros :as em]
                   [domina.macros :as dm]))
```

### The used public symbols of the most indipendent namespace

If you take a look at the `macros.clj` file, which defines the
`enfocus.macros` namespace, and search for the used symbol from the
`enfocus.enlive.syntax` namespace, you'll discover that it use the
`convert` symbol only: annotate it and go on searching for used public
symbol from the `enfocus.enlive.syntax` namespace in the next
namespace which is the `enfocus.core`.

Surprise! Even the `enfocus.core` namespace only use the `convert`
symbol from the the `enfocus.enlive.syntax` namespace. That's very
good for my lazyness regarding unit testing, because by following my
unit testing strategy we have to unit test the `enfocus.enlive.syntax`
namespace for the `convert` symbol only.

But wait a minute. Remember that `enfocus.enlive.syntax` is a
*portable* namespace and we should then unit test it both in the CLJ
environment (i.e. against a Java VM) and in the CLJS environment
(i.e. against a JS VM). As explained in the
[Tutorial 16 - It's better to be safe than sorry (Part 3)][7] to apply
the DRY principle in the context of unit testing CLJ/CLJS code we need
to add the `cljx` plugin. You should now already know that we're going
to add it in the `:dev` profile which has been kept separated from the
`project.clj` in the `profiles.clj`.

```clj
{:dev {:test-paths ["test/clj" "target/test/clj"]
       ...
       :plugins [...
                 [com.keminglabs/cljx "0.3.0"]]
        
       :cljx {:builds [{:source-paths ["test/cljx"]
                                    :output-path "target/test/clj"
                                    :rules :clj}

                                   {:source-paths ["test/cljx"]
                                    :output-path "target/test/cljs"
                                    :rules :cljs}]}
           
       :cljsbuild 
       {:builds {:whitespace
                 {:source-paths ["src/cljs" "test/cljs" "src/brepl" "target/test/cljs"]
                  ...}

                 :simple
                 {:source-paths ["src/cljs" "test/cljs" "target/test/cljs"]
                  ...}

                 :advanced
                 {:source-paths ["src/cljs" "test/cljs" "target/test/cljs"]
                  ...}}
        ...}
       ...}}
```

Let's start from unit testing the `convert` symbol in the JVM.


`enfocus.macros` namespace has to be tested in the CLJ enviroment (i.e. against the JVM), while 

Stay tuned for the next tutorial.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-21.md
[2]: https://github.com/ckirkendall/enfocus
[3]: https://github.com/cemerick/piggieback
[4]: https://github.com/cemerick/clojurescript.test
[5]: http://localhost:3000/
[6]: https://github.com/ckirkendall
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md


[6]: https://clojars.org/
[7]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[8]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-18.md
[9]: http://en.wikipedia.org/wiki/Same_origin_policy
[10]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-02.md

