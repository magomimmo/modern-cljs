# Tutorial 20 - Enfocus your view

In the [previous tutorial][1] we described a couple of approaches to
survive while livin' on the edge of a continuosly changing CLJ/CLJS
libs used as dependencies in our project. We ended up by publishing to
[clojars][2] a set of four [shoreleave][3] libs which the
`modern-cljs` project directly or indirectly depends on. This way you
can become collaborative with the CLJ/CLJS communities and, at the
same time, more indipendent from someone else's decision to merge or
refuse your pull requests.

In this tutorial we're getting back to CLJS and to my obsession with
the application of the DRY principle.

## Introduction

In the [Tutorial 14 - Its better to be safe than sorry (Part 1)][4] we
introduce the [enlive][5] templating system for Clojure. Then, in the
[Tutorial 17 - Enlive by REPLing][6] we encapsuleted into the `enlive`
templating system the form's input validators for the `Shopping
Calculator`.

As you rembember, we were able to share the same codebase of the
validators and their corresponding unit tests between the server side
CLJ code and the client side CLJS code.

In this tutorial we are going to investigate the feasibility of doing
something even more dramatic than that: the sharing of as much code as
possibile between the server side HTML transformation of the of the
`Shopping Calculator` page and the corresponding client side DOM
manipulation of the same page.

> NOTE 1: I suggest you to keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-19
> git checkout -b tutorial-20-step-1
> ```

## Short review

In the [Tutorial 9 - DOM Manipulation][7] we used the events'
management of the [domina][8] lib to implement the client side DOM
manipulation of the `Shopping Calculator` as a consequence of the
events triggered by the user interation with the browser.

Then, in the subsequent tutorials, even if we augumented the `Shopping
Calculator` sample by adding a bit of Ajax and by preparing both the
input validators and the corresponding unit tests, we missed to merge
those validators in the CLJS code as we did in the server side
implementation.

## Mission impossible

That said, if you take a look at the server side code which uses the
[enlive][5] lib and the client side code which uses the [domina][8]
lib, you hardly find any common code.

> Server side code for HTML transformation

```clj
(ns modern-cljs.templates.shopping
  (:require [net.cgrand.enlive-html :refer [deftemplate content do-> add-class set-attr attr=]]
            [modern-cljs.remotes :refer [calculate]]
            [modern-cljs.shopping.validators :refer [validate-shopping-form]]))

(defmacro maybe-error [expr] 
  `(if-let [x# ~expr] 
     (do-> (add-class "error")
           (content x#))
     identity))

(deftemplate update-shopping-form "public/shopping.html"
  [q p t d errors]

  ;; select and transform input labels

  [[:label (attr= :for "quantity")]] (maybe-error (first (:quantity errors)))
  [[:label (attr= :for "price")]] (maybe-error (first (:price errors)))
  [[:label (attr= :for "tax")]] (maybe-error (first (:tax errors)))
  [[:label (attr= :for "discount")]] (maybe-error (first (:discount errors)))

  ;; select and transform input values

  [:#quantity] (set-attr :value q)
  [:#price] (set-attr :value p)
  [:#tax] (set-attr :value t)
  [:#discount] (set-attr :value d)

  ;; select and transform total

  [:#total] (if errors
              (set-attr :value "0.00")
              (set-attr :value (format "%.2f" (calculate q p t d)))))

(defn shopping [q p t d]
  (update-shopping-form q p t d (validate-shopping-form q p t d)))
```

> Client side code for DOM manipulation

```clj
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))

(defn calculate [evt]
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))
    (prevent-default evt)))

(defn add-help! []
  (append! (by-id "shoppingForm")
               (html [:div.help "Click to calculate"])))

(defn remove-help![]
  ;;(destroy! (by-class "help")))
  (destroy! (.getElementsByClassName js/document "help")))

(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
```

This time my obsession about the DRY principle seems to be a *mission
impossible* to be satisfied. But do not despair, because we still have
some hope.

## Enter Enfocus

[Enfocus][] is a DOM manipulation and templating library for
ClojureScript inspired by Enlive. This statement is intriguing enough
to make our best efforts for learning it. Then we will try to fullfill
our obsession.

We are going to start very easy, which means trying to learn how to
use [Enlive][] in the context of the `Shopping Calculator` by
substituting it to the [domina][] lib.

In the [Tutorial 17 - Enlive by REPLing][] we introduced the usage of
the [lein-try][] plugin to experiment at the CLJ REPL with both the
[Hiccup][] and the [Enlive][] libs. In the second part of the
[Tutorial 18 - Housekeeping][] we stated that by using the
[Piggieback][] lib we could have a better bREPL experience if compared
with the [*standard* bREPL one][]. In the
[Tutorial 19 - A survival guide for livin' on the edge][] we described
how to fork/clone/update and publish to [clojars][] a snapshot lib.
It's time to demonstrate how to put all these pieces together.

### Living on the edge with enfocus

First clone and branch the [Enfocus][] repo:

```bash
cd ~/dev
git clone https://github.com/ckirkendall/enfocus.git
cd enfocus
git checkout -b clojars # will be publish to clojars
```

Why are we doing that? Take a look at its `project.clj` and you'll
understand.

> NOTE 1: The Enfocus project structure diverges a little bit from the
> regular ones. It uses a project directory, where you'll find the
> `project.clj` and a `testing` directory which we do not care about.

```clj
(defproject enfocus "2.0.0-SNAPSHOT"
  :description "DOM manipulation tool for clojurescript inspired by Enlive"
  :source-paths ["cljs-src" ".generated/cljs" ".generated/clj"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [domina "1.0.1" :exclusions [org.clojure/clojurescript]]
                 [org.jsoup/jsoup "1.7.2"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [com.keminglabs/cljx "0.2.2"]]
  :cljx {:builds [{:source-paths ["cljx-src"]
                   :output-path ".generated/clj"
                   :rules cljx.rules/clj-rules}

                  {:source-paths ["cljx-src"]
                   :output-path ".generated/cljs"
                   :extension "cljs"
                   :rules cljx.rules/cljs-rules}]}
  :cljsbuild
  {:builds
   [{:builds nil,
     :source-paths ["cljs-src" ".generated/cljs"]
     :compiler
     {:output-dir "../testing/resources/public/cljs",
      :output-to "../testing/resources/public/cljs/enfocus.js",
      :optimizations :whitespace,
      :pretty-print true}}]}
  :hooks [cljx.hooks])
```

As you can see `enfocus` depends on few outdated libs and plugins. But
it depends on `domina "1.0.1"` as well, which has a very
[annoying bug][] and we want to update the `domina` lib to the
`1.0.2-SNAPSHOT` release which solves it. While we are changing the
`domina` releases, we take the opportunity to update the rest of the
dependencies and plugins.

```clj
(defproject org.clojars.magomimmo/enfocus "2.0.0-SNAPSHOT"
  ...
  :url "https://github.com/magomimmo/enfocus/tree/clojars"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1847"]
                 [domina "1.0.2-SNAPSHOT"]
				 [org.jsoup/jsoup "1.7.2"]]
  :plugins [[lein-cljsbuild "0.3.3"]
            [com.keminglabs/cljx "0.3.0"]]
  :cljx {:builds [{:source-paths ["cljx-src"]
                   :output-path ".generated/clj"
                   :rules :clj}

                  {:source-paths ["cljx-src"]
                   :output-path ".generated/cljs"
                   :rules :cljs}]}
  ...)
```

> NOTE 2: There are few things that I still have to grasp about
> `enfocus` project structure, but this is not the place to afford them.

> NOTE 3: As you see I added the group-id `org.clojars.magomimmo` in the
> name of the project because I'm going to publish it to [clojars][]. I
> also added the optional `:url` option which links to the `clojars`
> branch in my `enfocus` repo.


You can finally commit the changes by issuing the usual `git` commands
as follows:

```bash
git add .
git commit -m "Step 2"
```

That's all. Stay tuned for the next tutorial of the series.

# Next Step - TO BE DONE

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-19.md
[2]: https://clojars.org/
[3]: https://github.com/shoreleave
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[5]: https://github.com/cgrand/enlive
[6]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-17.md


[2]: https://github.com/cemerick/piggieback
[3]: https://github.com/technomancy/leiningen/blob/stable/doc/PROFILES.md
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[5]: https://github.com/shoreleave/shoreleave-remote
[6]: https://github.com/shoreleave/shoreleave-remote-ring

[8]: https://github.com/shoreleave/shoreleave-core
[9]: https://github.com/shoreleave/shoreleave-browser
[10]: https://github.com/clojure/tools.reader
[11]: https://help.github.com/articles/fork-a-repo
[12]: http://git-scm.com/book/en/Git-Basics-Tagging
[13]: https://github.com/gdeer81/lein-marginalia
[14]: http://en.wikipedia.org/wiki/Literate_programming
[15]: http://semver.org/
[16]: https://github.com/cemerick
[17]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-16.md 
[18]: https://github.com/ohpauleez
[19]: https://help.github.com/articles/be-social#pull-requests
[20]: https://help.github.com/articles/syncing-a-fork
[21]: https://clojars.org/
[22]: https://github.com/technomancy/leiningen/blob/stable/doc/TUTORIAL.md#publishing-libraries
[23]: https://clojars.org/register
[24]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#authentication
[25]: http://clojars.org/repo/
[26]: http://releases.clojars.org/repo/
[27]: https://github.com/
