# Tutorial 19 - Livin' on the edge

In the [previous tutorial][1] we completed the job of integrating the
client-side validators for the Shopping Calculator input fields into
the corresponding WUI (Web User Interface) in such a way that the user
will be notified with the corresponding help messages when she enters
invalid values in the form itself.

## Preamble

To start working from the end of the [previous tutorial][1], assuming
you've git installed, do as follows

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
```

## Introduction

As you remember, those validators have been implemented by using a
forked version of the [valip](https://github.com/cemerick/valip)
validation library by
[Chas Emerick](https://github.com/cemerick). That library was in turn
a forked version of the [valip](https://github.com/weavejester/valip)
library by [James Reeves](https://github.com/weavejester) aimed at
make that library as portable as possible between Clojure and
ClojureScript. When Chas Emerick forked the `valip` library in 2012,
[Readers Conditional](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
did not exist and you had two alternative ways to make you code
portable to CLJ and CLJS, namely:

* the
[`crossover`](https://github.com/emezeske/lein-cljsbuild/blob/d5c08124ade0bfd6c548d14a66989f3fdb98a47f/doc/CROSSOVERS.md)
features by the
[`cljsbuild`](https://github.com/emezeske/lein-cljsbuild) plugin;
* the [cljx](https://github.com/lynaghk/cljx) plugin by
  [Kevin Lynagh](https://github.com/lynaghk).

The problem with those alternatives to make your code portable is that
they are now both deprecated in favor of the much more powerful and
flexible Reader Conditionals extension we cited above.

When I recently decide to switch this series of tutorials on CLJS from
the [leiningen](http://leiningen.org/) build tool to the
[boot](https://github.com/boot-clj/boot) one, I realized that I could
exploit that decision to migrate the `valip` portable fork by Chas
Emerick to the new CLJ/CLJS Reader Conditional extension as well.

This tutorial is a kind of a walk-through of that migration
experience. The reason why I decided to put that experience in words
is that I believe that one of the main advantages of CLJ/CLJS pair
over other programming languages is that it allows to unify two
completely different worlds: the front-end and the back-end. If you're
thinking that, thanks to [`nodejs`](https://nodejs.org/en/),
JavaScript has the same bipolar insanity, you're right. But even
without starting a never ending discussion about JS's pitfalls, the
CLJ/CLJS pair has still some advantages to carry with itself, because
it lets you decide on which back-end platform to run your services,
being a `JVM`, `nodejs` or even `.net`.

My personal hope is to see in the near future a growing number of
CLJ/CLJS libraries able to be used indifferently on JS and JVM
platforms and eventually dynamically moved at run-time from one side
to the other and *vice versa*.

This tutorial of the series has to be intended as an effort to
stimulate CLJ/CLJS newbies to start learning that pair of programming
languages while doing, namely while being collaborative with the
maintainers of CLJ/CLJS libraries, because most of the time the
migration path is not so hard to be taken.

## Livin' on the edge

When you start using a library implemented by others, you can easily
end up with few misunderstandings of its use or even with some
unexpected issues. In these cases the first thing you should do is to
browse and read its documentation. As you know, one problem with open
source software regards the corresponding documentation which is
frequently minimal, if not absent, outdated or requiring a level of
comprehension of the details which as newbie you still have to grasp.

Likely, most of the CLJ/CLJS open source libraries are hosted on
[github](https://github.com/) which offers an amazing support for
collaboration and social coding. Even if few CLJ/CLJS libraries have
an extensive documentation and/or an associated mailing-list for
submitting dubts and questions, every CLJ/CLJS library hosted on
github is supported by an articulated, although easy, issue and
version control management systems. Those two systems help a lot in
managing almost any distributed and remote collaboration requirements.

## Valip fork by Chas Emerick

As said, the main purpose of Chas Emerick while forking the original
`valip` library was to make it portable from CLJ to
CLJS. Consequently, it should be relatively easy to make it supporting
the new Reader Conditionals extension of the `1.7.0` CLJ/CLJS release,
because most of the porting works should already be done by Chas
Emerick himself.

## Fork, clone and branch

The first step is to
[fork the library from github](https://help.github.com/articles/fork-a-repo/)
and then create a local clone of the fork in your computer.

```bash
git clone https://github.com/<your_github_name>/valip.git
Cloning into 'valip'...
remote: Counting objects: 309, done.
remote: Total 309 (delta 0), reused 0 (delta 0), pack-reused 309
Receiving objects: 100% (309/309), 36.37 KiB | 0 bytes/s, done.
Resolving deltas: 100% (101/101), done.
Checking connectivity... done.
```

Now add to your cloned repository the remote address of the original
`valip` library

```bash
cd valip
git remote add upstream https://github.com/cemerick/valip.git
git remote -v
origin	https://github.com/magomimmo/valip.git (fetch)
origin	https://github.com/magomimmo/valip.git (push)
upstream	https://github.com/cemerick/valip.git (fetch)
upstream	https://github.com/cemerick/valip.git (push)
```

Finally, make a new branch to keep your work separated from the master
branch

```bash
git checkout -b reader-conditionals
Switched to a new branch 'reader-conditionals'
```

and we're ready to start collaborating.

## Look at the `project.clj` file

When I want to reach a better understanding of a new CLJ/CLJS library,
the very first thing that I do is to view its `project.clj` (or it's
`build.boot` file if the library has been based on `boot`):

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.4.0"]])
```

As you see, `valip` depends on CLJS `1.4.0` release. To be able to use
Reader Conditionals, the very first thing to be update will be the CLJ
dependency CLJ from `1.4.0` to `1.7.0`.

Also note that `leiningen` has a lot of sane defaults for its build
directives. For example, if you do not specify a `:source-paths`
directive, it assumes `src`. If you do not specify a `:target-path`
directive, it assume `target` as the default and if you do not specify
a `:test-paths` it will assume the `test` directory as default and so
on. That's why the `project.clj` is so concise.

## Look at the directory layout

The very second thing take I look into, it's the directory layout of the project:

```bash
tree
.
├── README.md
├── project.clj
├── src
│   ├── cljs
│   │   └── reader.clj
│   └── valip
│       ├── core.clj
│       ├── java
│       │   └── predicates.clj
│       ├── js
│       │   └── predicates.cljs
│       ├── predicates
│       │   └── def.clj
│       └── predicates.clj
└── test
    └── valip
        └── test
            ├── core.clj
            └── predicates.clj

9 directories, 10 files
```

As you see the source files are confined in the `src` directory, while
the `test` source files are confined in the `test` directory mimicking
the layout of the `src` directory.

## The core

The `valip` lib has a `valip.core` namespace living in the
`src/valip/core.clj` source file:

```clj
(ns valip.core
  "Functional validations.")

(defn validation-on
  "Performs a validation on a key in a map using the supplied predicate
  function. A {key [error]} map is returned if the predicate returns false;
  nil is returned if the predicate returns true."
  [key pred? error]
  (fn [value-map]
    (let [value (value-map key)]
      (if-not (pred? value)
        {key [error]}))))

(defn merge-errors
  "Merge error maps returned by from the validation-on function."
  [& error-maps]
  (apply merge-with into error-maps))

(defn validate
  "Validate a map of values using the supplied validations. Each validation
  is represented as a vector containing [key predicate? error] values. A map
  is returned for all the keys that failed their predicates, in the form:
  {key [errors]}. If no predicates return false, nil is returned."
  [value-map & validations]
  (->> validations
       (map (partial apply validation-on))
       (map (fn [f] (f value-map)))
       (apply merge-errors)))
```

Here there is nothing specific for the JVM or the JS platforms. This
file is going to stay the same while migrating the `valip` library on
the Reader Conditionals.

## Code specific for the JVM

Under the `src/valip` directory you see two interesting directories:
`java` and `js` containing the same file with different extensions:
`predicates.clj` and `predicates.cljs`. Let's take a look at them:

```clj
;;; src/valip/java/predicates.clj
(ns valip.java.predicates
  "Useful validation predicates implemented for JVM Clojure."
  (:require [valip.predicates :as preds]
            [valip.predicates.def :refer (defpredicate)])
  (:import
    (java.net URI URISyntaxException)
    java.util.Hashtable
    javax.naming.NamingException
    javax.naming.directory.InitialDirContext))

;;; follow few definitions
```

As the namespace's docstring says, this is where `valip` defines
things specific for the JVM (i.e., `url?` and `valid-email-domain?`
predicates). We already know that by using the `#?` reader macro from
the Reader Conditional Extension we should be able to include those
JVM specific predicates within the same `cljc` source file including
the JSVM specific predicates and the ones that are common between CLJ
and CLJS.

## Code specific for the JSVM

Let's now take a look at the corresponding `predicates.cljs` in the
`js` directory:

```clj
;;; src/valip/js/predicates.cljs
(ns valip.js.predicates
  "Useful validation predicates implemented for ClojureScript using the Google Closure libraries
where necessary."
  (:import goog.Uri))

(defn url?
  [s]
  (let [uri (-> s goog.Uri/parse)]
    (and (seq (.getScheme uri))
         (seq (.getSchemeSpecificPart uri))
         (re-find #"//" s))))
```

Well, as you see, here we only have the `url?` predicate defined using
a
[`google closure`](https://google.github.io/closure-library/api/class_goog_Uri.html)
library. Indeed, because of the
[same-origin policy](https://en.wikipedia.org/wiki/Same-origin_policy)
you can't directly access from JS a different URI, it does not make
sense to define a JS `valid-email-domain?` predicates, unless you
circumvent this limitation using one of the allowed ways.

As above, we know how to manage JSVM specific code with the `#?`
reader macro in a `cljc` source file.

## Code common to JVM and JSVM

The above directory layout shows a third `predicates.clj` source file
(note the `.clj` extension) which is locate directly under the
`src/valip` directory. Let's take a look at it:

```clj
;;; src/valip/predicates.clj

(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms.
All predicates in this namespace are considered portable between different
Clojure implementations."
  (:require [clojure.string :as str]
            [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))
  
;;; follow all predicates...
```

Well, this is were all the predicates common between CLJ and CLJS
live. Concentrate your attention at the above `valip.predicates` namespace
declaration.

There Chas Emerick first refereed the `read-string` function from the
`cljs.reader` namespace. Then, to prevent namespace conflicts with the
`read-string` function from the `clojure.core` namespace, he excluded
it from been interned with the same name in the newly defined
namespace. This has to do with one of the
[differences between CLJ and CLJS](https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader),
namely:

* The `read` and `read-string` functions are located in the
  `cljs.reader` namespace

### A smart trick by a smart guy

But wait a minute, what it's going on there? If you review the
directory layout of the project, you'll see a `reader.clj` source file
under the `src/cljs` directory. Considering that the `:source-paths`
directive of the `project.clj` file is set by default to `src` and by
knowing the way CLJ maps namespace segments to file pathnames, there
is a smell of something strange going on. Let's take a look at the
`reader.clj` source file:

```clj
(ns cljs.reader
  "A dummy namespace, allowing valip.predicate to :require cljs.reader even in Clojure,
so as to allow portable usage of `read-string`."
  (:refer-clojure :exclude (read-string)))

(def read-string clojure.core/read-string)
```

Ahah! This is were the smart trick leaves. Chas Emerick defined a
dummy `cljs.reader` namespace mimicking the CLJS `cljs.reader`
namespace. It only aliases the `read-string` from the `clojure.core`
namespace in such a way that the `cljs.reader` can be required even
when `valip` runs on a JVM.

All this just because the Reader Conditionals were not available at
the time of writing the `valip` library, but it is now very easy to be
solved without any trick. Again, we only need to use the `#?` reader
macro to differentiate the requirement in the namespace declaration.

The last source file we still have to take a look at, a side from the
unit tests relegated in the `test` directory, is the `def.clj` living
in the `src/valip/predicates` directory:

```clj
(ns valip.predicates.def)

(defmacro pfn
  [& [name? & fbody]]
  (let [[name fbody] (if (symbol? name?)
                       [name? fbody]
                       [nil (cons name? fbody)])
        prologue (if name (list 'fn name) (list 'fn))
        [args pre-preds & body] (if (and (-> fbody first vector?)
                                         (-> fbody second vector?))
                                  fbody
                                  (list* (first fbody) nil fbody))]
    (when (> (count args) 1)
      (throw (IllegalArgumentException.
               (str "Validation predicate functions should take only one argument, not " args))))
    `(~@prologue
       ~args
       (and (every? #(% ~@args) ~pre-preds)
            ~@body))))

(defmacro defpredicate
  [name & fdecl]
  (let [[doc & fdecl] (if (string? (first fdecl))
                      fdecl
                      [nil fdecl])
        name (if doc
               (vary-meta name merge {:doc doc})
               name)]
    `(def ~name (pfn ~name ~@fdecl))))
```

Forget for a moment the complication of a macro calling another
macro. Here is where Chas Emerick added some value to the original
`valip` library by defining the `defpredicate` macro that allows
anybody to add new predicates used for validation.

There is nothing specific for CLJ or CLJS in this file, aside from the
need to keep the macros definitions separated from their use, as Chas
Emerick already did. Indeed, CLJS's macros must be defined in a
different compilation stage than the one from where they are
consumed. Again, thanks to the `#?` reader macro we know how to
differentiate in a `.cljc` source file a requirement of a namespace
containing and it's not a big deal.

### One more thing: unit tests

Chas Emerick has been so nice to have defined even some unit tests for
its library. The only thing we should take care of here regards the
fact CLJ uses the `clojure.test` namespaces while CLJS uses
`cljs.test` namespace. This is something we already learned how to
manage, right?

### Summary

Let's summarize our discoveries to be prepared for migrating the
`valip` validation library to the new Reader Conditionals extension:

* we have to update the CLJ dependency from the `1.4.0` release to the
  `1.7.0` release, because the Reader Conditional extension has been
  introduced starting from that release;
* we have to aggregate all the predicates that are specific for the
  JVM or for the JSVM platform in the same file where the common
  predicates live, provided that:
  * we modify the extension of the file form `.clj` to `.cljc`;
  * we differentiate any specific namespace requirement in the
    namespace declaration by using the `#?` reader macro;
  * we differentiate any specific definition by using the `#?` reader
    macro;
* we have to differentiate the namespace requirement in the unit tests
  namespace declaration by using again the `#?` reader macro.
  
Now a so hard work to be done, right? Wrong! The `valip` library build
file is based on `leiningen` and if we want to able to compile and
test it with CLJS, we need to add to the `project.clj` build file the
`lein-cljsbuild` and the [`lein-doo`](https://github.com/bensu/doo)
plugins as well.

Moreover, unless you got the chance to read the
[first edition](https://github.com/magomimmo/modern-cljs/tree/master/doc/first-edition)
of this series or you do not already know about the `leiningen` build
tool, all those stuff are new to you.

Don't worry, I'll try to escort you while doing this new things.

## 


```clj
(defproject com.cemerick/valip "1.0.0-SNAPSHOT"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.7.0"]])
```

Note as we updated `valip` release from the original `0.3.2` to
`1.0.0-SNAPSHOT` as well. This is because we want to adhere to the
['semantic versioning'](http://semver.org/) rules:

> Given a version number MAJOR.MINOR.PATCH, increment the:
> 
> * MAJOR version when you make incompatible API changes,
> * MINOR version when you add functionality in a backwards-compatible manner, and
> * PATCH version when you make backwards-compatible bug fixes.
> 
> Additional labels for pre-release and build metadata are available as
> extensions to the MAJOR.MINOR.PATCH format.

## Start TDD

Start TDD environment:

```bash
cd /path/to/modern-cljs
boot tdd
...
Elapsed time: 26.573 sec
```

### Start CLJ REPL

Now launch the client REPL as usual
 
```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

Remember to reactivate the JS engine from the Developer Tools of your
browser and then visit the
[Shopping Calculator](http://localhost:3000/shopping.html) URI to
activate the websocket connection used by `tdd` to reload pages when
you save some changes.

### Start CLJS bREPL

Finally, launch the CLJS bREPL from the CLJ REPL

```bash
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:49974 >>
<< waiting for client to connect ... Connection is ws://localhost:49974
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=> 
```

and you're ready to go.



You can now stop the CLJ REPL and the boot process, and then reset the
branch as usual:

```bash
git reset --hard
```

Stay tuned for the next tutorial

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-18.md
[2]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-16.md
