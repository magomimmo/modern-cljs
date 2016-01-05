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

As you remember, the injected validators have been implemented by
using a forked version of the
[valip](https://github.com/cemerick/valip) validation library by
[Chas Emerick](https://github.com/cemerick). That library was in turn
a forked version of the [valip](https://github.com/weavejester/valip)
library by [James Reeves](https://github.com/weavejester) and it was
aimed at making the original library as portable as possible between
Clojure and ClojureScript. When Chas Emerick forked the `valip`
original library in 2012,
[Readers Conditional](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
did not exist. At those time you had two alternatives to make you code
portable from CLJ to CLJS, namely:

* the
[`crossover`](https://github.com/emezeske/lein-cljsbuild/blob/d5c08124ade0bfd6c548d14a66989f3fdb98a47f/doc/CROSSOVERS.md)
features by the
[`cljsbuild`](https://github.com/emezeske/lein-cljsbuild) plugin;
* the [cljx](https://github.com/lynaghk/cljx) plugin by
  [Kevin Lynagh](https://github.com/lynaghk).

The problem with those alternatives to make your code portable between
CLJ and CLJS is that they are now both deprecated in favor of the much
more powerful and flexible
[Reader Conditionals](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
extension we cited above.

When I recently decide to switch this series of tutorials on CLJS from
the [leiningen](http://leiningen.org/) build tool to the
[boot](https://github.com/boot-clj/boot) one, I realized that I could
exploit that decision to migrate `valip` portable to the new CLJ/CLJS
Reader Conditional extension as well.

This tutorial is a kind of a walk-through of that migration
experience. The reason why I decided to put that experience in words
is that I believe that one of the main advantages of CLJ/CLJS pair
over other programming languages is that it allows to unify two
completely different worlds: the front-end and the back-end. If you're
thinking that, thanks to [`nodejs`](https://nodejs.org/en/),
JavaScript has the same bipolar insanity, you're right. But even
without starting a never ending discussion about JS's pitfalls, the
CLJ/CLJS pair has still some advantages to carry on with itself,
because it allows you to decide on which back-end platform to run your
services, being it `JVM`, `nodejs` or even `.net`.

My personal hope is to see in the near future an increasing number of
CLJ/CLJS libraries able to be used indifferently on JS or JVM
platforms and eventually dynamically moved at runtime from one side to
the other and *vice versa*.

This tutorial of the series has to be intended as an effort to
stimulate CLJ/CLJS newbies to start learning that pair of programming
languages while doing, namely while being collaborative with the
maintainers of CLJ/CLJS libraries, because most of the time the
migration path needed to make a CLJ library portable on CLJS via
Reader Conditionals is not hard at all.

## Livin' on the edge

When you start using a library implemented by others, you can easily
end up with few misunderstandings of its use or even with some
unexpected issues. In these cases, the first thing you should do is to
browse and read its documentation. As you know, one problem with open
source software regards the corresponding documentation which is
frequently minimal, if not absent, outdated or requiring a level of
comprehension of the details which as newbie we still have to grasp.

Likely, most of the CLJ/CLJS open source libraries are hosted on
[github](https://github.com/) which offers an amazing support for
collaboration and social coding. Even if few CLJ/CLJS libraries have
an extensive documentation and/or an associated mailing-list for
submitting doubts and questions, every CLJ/CLJS library hosted on
github is supported by an articulated, although easy, issue and
version control management systems. Those two systems help a lot in
managing almost any distributed and remote collaboration requirements.

## Valip Library

As said, the main purpose of Chas Emerick while forking the original
`valip` library was to make it portable from CLJ to
CLJS. Consequently, it should be very easy to adopt the Reader
Conditionals extension, because most of the porting works should
already be done by Chas Emerick himself.

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

> NOTE 1: in github parlance the original repository you fork is named
> `upstream`.

Finally, make a new branch to keep your work separated from the master
branch

```bash
git checkout -b reader-conditionals
Switched to a new branch 'reader-conditionals'
```

and you're ready to start collaborating.

## Look at the `project.clj` file

When I want to grasp an initial understanding of a new CLJ/CLJS
library, the very first thing that I do, aside reading the
corresponding `README.md` file when available, it's to view its
`project.clj` build file (or its `build.boot` build file, for the rare
case in which the library has been based on `boot`):

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.4.0"]])
```

A pretty minimal build file in such a case. As you see, `valip`
depends on CLJS `1.4.0` release. To be able to use the Reader
Conditionals extension, the very first thing to be updated is the CLJ
dependency: from `1.4.0` to `1.7.0` (in a very short time the
clojure-dev team will deliver the `1.8.0` release).

[`leiningen`](http://leiningen.org/) has a lot of sane defaults for
its build directives. For example, if you do not specify a
`:source-paths` directive, it assumes `src` as default. If you do not
specify a `:target-path` directive, it assume `target` as default and
if you do not specify a `:test-paths` it will assume the `test`
directory as default and so on. That's why the `project.clj` is so
noise free in such a case.

## Look at the directory layout

The very second thing take I generally look into, is the directory
layout of the project directory:

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
the layout of the `src` directory. Nothing new even coming from a
`boot` background.

## Migration preparation

To introduce the Reader Conditionals extension into a library
originally implemented for CLJ only there few steps you should follow:

* update the CLJ dependency to the `1.7.0` release, because the Reader
  Conditional extension has been introduced from that release;
* identify all the functions that are specific for the
  JVM and that can't be evaluate in a CLJS context;
* identify all the macros defined in the library, because CLJS's
  macros must be defined in a different compilation stage than the one
  from where they are consumed.

## Valip current state

With `valip` we're in good position. Indeed, the above directory
layout of the `valip` validation library already partially fits with
the above preparation, because Chas Emerick's goal was to make `valip`
portable on JSVM (JavaScript Virtual Machine) either via the
`:crossover` feature of the
[lein-cljsbuild](https://github.com/emezeske/lein-cljsbuild) plugin or
via the [`lein-cljx`](https://github.com/lynaghk/cljx) plugin.

Let's comment a little bit the current directory layout of the `valip`
library:

* all the functions specific to the JVM are confined in the
  `predicates.clj` source file hosted in the `src/valip/java`
  directory;
* all the functions specific to the JSVM are confined in the
  `predicates.cljs` source file hosted in the `src/valip/js`
  directory;
* all the functions which are agnostic from the platform are confined
  in the `core.clj` and `predicates.clj` source files hosted in the
  `src/valip` directory;
* all the macros are confined in the `def.clj` source file hosted in
  the `src/valip/predicates` directory.

So far, so good. But what does it stand for that `reader.clj` source
file hosted in the `src/cljs` directory?

## A smart trick by a smart guy

Consider that the `:source-paths` directive of the `project.clj` file
is set by default to `src`. Now, by knowing the way CLJ maps namespace
segments to file pathnames, there is a smell of something strange
going on there. Let's take a look at the `reader.clj` source file:

```clj
(ns cljs.reader
  "A dummy namespace, allowing valip.predicates to :require cljs.reader even in Clojure,
so as to allow portable usage of `read-string`."
  (:refer-clojure :exclude (read-string)))

(def read-string clojure.core/read-string)
```

Ahah! This is were the smart trick lurks. Chas Emerick defined a dummy
`cljs.reader` namespace mimicking the CLJS `cljs.reader` namespace. It
only aliases the `read-string` from the `clojure.core` namespace in
such a way that the `cljs.reader` can be required even when `valip`
runs on a JVM.

Let's take a look at the `valip.predicates` namespace declaration from
the `predicates.clj` hosted in the `src/valip` directory:

```clj
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms.
All predicates in this namespace are considered portable between different
Clojure implementations."
  (:require [clojure.string :as str]
            [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))
```

That smart guy first refereed the `read-string` function from the
`cljs.reader` namespace we just saw above. Then, to prevent a symbol
collision with the `read-string` symbol from the `clojure.core`
namespace, he excluded it from been interned in the `valip.predicates`
namespace. But they have exactly the same definition. This has to do
with one of the
[differences between CLJ and CLJS](https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#the-reader),
namely:

* The `read` and `read-string` symbols are defined in the
  `cljs.reader` namespace, while the corresponding CLJ symbols are
  defined in the `clojure.core` namespace.

All this incidental complexity was needed because the Reader
Conditionals were not available at the time of writing the `valip`
library.

## What's next

As we learned few tutorials ago, the Reader Conditionals extension
offers the `#?` reader macro to dynamically differentiate at runtime
the forms to be evaluated depending on the features of the hosting
platform. At the moment we are interested in two features that can
only be used inside`.cljc` source files:

* `:clj` is available when CLJ runs on a JVM;
* `:cljs` is available when CLJ runs on a JSVM.

With the Reader Conditionals machinery in our hands we can now proceed
with the next steps, namely:

1. delete the `src/cljs` directory and the `reader.clj` file as well;
2. change the extension of the `predicates.clj` file hosted in the
  `src/valip` directory from `.clj` to `.cljc`;
3. move the specific JVM symbols' definitions from the `predicates.clj`
  file hosted in the `src/valip/java` directory to the above
  `predicates.cljc` file;
4. move the specific JSVM symbols' definitions from the
  `predicates.cljs` file to the above `predicates.cljc` file;
5. use the `#?` reader macro to differentiate the namespaces'
  requirements in the `valip.predicates` namespace declaration
  depending on the feature made available by the hosting platform at
  runtime;
6. use the `#?` reader macro to differentiate the symbols' definitions
  in the `valip.predicates` namespace as well;
7. eventually move the `def.clj` file containing the macros definitions
  to the `src/valip` directory and rename it as `macros.clj`;
8. use the `#?` reader macro to differentiate the namespaces'
  requirements and the symbols' definitions for the unit tests as well.

Not such a big deal, right? **Wrong!**

The `project.clj` build file is for the `leiningen` build tool. If we
want to able to compile and test the migrated `valip` library on CLJS,
we need to add both the `lein-cljsbuild` and the
[`lein-doo`](https://github.com/bensu/doo) plugins.

Moreover, unless you got the chance to read the
[first edition](https://github.com/magomimmo/modern-cljs/tree/master/doc/first-edition)
of this series or you do not already know about the `leiningen` build
tool, all those stuff are new to you and the noise/signal ratio of the
final `project.clj` build file is going to get worst when compared
with the `project.clj` we started from.

Don't worry, I'll try to escort you while doing this new things.

## Execution

In the following paragraphs we're going to execute step by step the
above plan leaving the update of the `project.clj` build file as a
last milestone to be reached.

### Step 1

As said, we first have to delete the `src/cljs` directory and the
contained `reader.clj` file as well because we do not need the Chas
Emerick trick anymore:

```bash
cd /path/to/valip
rm -rf src/cljs
```

### Step 2

We now need to rename the `predicates.clj` source file hosted in the
`src/valip` directory to `predicates.cljc`:

```bash
mv src/valip/predicates.clj src/valip/predicates.cljc
```

This file already contains all the symbols' definitions that are
already portable between CLJ and CLJS. Obviously we have to update its
namespace declaration, but we'll take care of this later, after we'd
imported any platform specific definition as well.

### Step 3 

Next, we have to move the definitions specific for the JVM into the
above `predicates.cljc` source file. Open the `predicates.clj` source
file hosted in the `src/valip/java` directory, copy all the symbols'
definitions and append them in the `predicates.cljc` source file.

While you are there, introduce the `#?` reader macro to inform the
compiler that these definition are specific for the JVM platform as
follows:

```clj
;;; above the rest of the file
#?(:clj (defn url?
          "Returns true if the string is a valid URL."
          [s]
          (try
            (let [uri (URI. s)]
              (and (seq (.getScheme uri))
                   (seq (.getSchemeSpecificPart uri))
                   (re-find #"//" s)
                   true))
            (catch URISyntaxException _ false))))

#?(:clj (defn- dns-lookup [^String hostname ^String type]
          (let [params {"java.naming.factory.initial"
                        "com.sun.jndi.dns.DnsContextFactory"}]
            (try
              (.. (InitialDirContext. (Hashtable. params))
                  (getAttributes hostname (into-array [type]))
                  (get type))
              (catch NamingException _
                nil)))))

#?(:clj (defpredicate valid-email-domain?
          "Returns true if the domain of the supplied email address has a MX DNS entry."
          [email]
          [preds/email-address?]
          (if-let [domain (second (re-matches #".*@(.*)" email))]
            (boolean (dns-lookup domain "MX")))))
```

### Step 4

You should now do the same thing with the `predicates.cljs` currently
living in the `valip.js` directory. While you're here, take advantage
of the opportunity and add the CLJS `url?` definition inside the `#?`
reader macro already used for the CLJ `url?` definition as follows:

```clj
#?(:clj (defn url?
          "Returns true if the string is a valid URL."
          [s]
          (try
            (let [uri (URI. s)]
              (and (seq (.getScheme uri))
                   (seq (.getSchemeSpecificPart uri))
                   (re-find #"//" s)
                   true))
            (catch URISyntaxException _ false)))
   :cljs (defn url?
           [s]
           (let [uri (-> s goog.Uri/parse)]
             (and (seq (.getScheme uri))
                  (seq (.getSchemeSpecificPart uri))
                  (re-find #"//" s)))))
```

As you see, you're conditionally defining the `url?` symbol depending
on the feature of the platform at runtime, being it JVM or JSVM.

### Step 5

The next step is the most tedious. You have to update the
`valip.predicates` namespace declaration to accommodate the different
CLJ and CLJS namespace requirements. Let's start by accommodating the
namespaces requirements that are common between CLJ and CLJS:


<up-to-here>


```clj
(defproject com.cemerick/valip
"1.0.0-SNAPSHOT" :description "Functional validation library for
Clojure and ClojureScript, forked from
https://github.com/weavejester/valip" :url
"http://github.com/cemerick/valip" :dependencies
[[org.clojure/clojure "1.7.0"]]) ```

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
