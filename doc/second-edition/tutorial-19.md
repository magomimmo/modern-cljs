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
easier, more powerful and more flexible
[Reader Conditionals](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
extension we cited above.

When I recently decide to switch this series of tutorials on CLJS from
the [leiningen](http://leiningen.org/) build tool to the
[boot](https://github.com/boot-clj/boot) one, I realized that I could
exploit that decision to accommodate the `valip` portable library to
the new CLJ/CLJS Reader Conditional extension as well.

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

## The Valip Library

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

## Directory layout

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
2. change the extension of the `predicates.clj` and `core.clj` files
   hosted in the `src/valip` directory from `.clj` to `.cljc`;
3. move the specific JVM symbols' definitions from the
   `predicates.clj` source file hosted in the `src/valip/java`
   directory to the above `predicates.cljc` source file;
4. move the specific JSVM symbols' definitions from the
   `predicates.cljs` source file to the above `predicates.cljc` source
   file;
5. use the `#?` reader macro to differentiate the namespaces'
   requirements in the `valip.core` and `valip.predicates` namespaces
   declaration depending on the feature made available by the hosting
   platform at runtime;
6. use the `#?` reader macro to differentiate the symbols' definitions
   in the `valip.predicates` namespace as well;
7. eventually move the `def.clj` file containing the macros
   definitions to the `src/valip` directory and rename it as
   `macros.clj`;
8. use the `#?` reader macro to differentiate the namespaces'
   requirements and the symbols' definitions for the unit tests as
   well;
9. delete the original `src/valip/java/predicates.clj` and
   `src/valip/js/predicates.cljs` source files, because we absorbed
   their content in the `predicates.cljc` source file.

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

But don't worry! We're not going to use `lein` and `cljsbuild` build
tools to create a new release of `valip` that is compliant with Reader
Conditionals extension. We're going to use `boot`.

## Execution (Part I)

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

We now need to rename the `predicates.clj` and `core.clj` source files
hosted in the `src/valip` directory respectively to `predicates.cljc`
and `core.cljc`:

```bash
mv src/valip/predicates.clj src/valip/predicates.cljc
mv src/valip/core.clj src/valip/core.cljc
```

These files contains all the symbols' definitions that are already
portable between CLJ and CLJS. Obviously we have to update their
namespace declarations, but we'll take care of this later.

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
          [email-address?]
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

The next step is the most tedious one. You have to update the
`valip.core` and `valip.predicates` namespaces' declarations to
accommodate the different CLJ and CLJS namespaces' requirements. Lets
first review all those namespaces' declarations.

```clj
(ns valip.core
  "Functional validations.")
```

Here there is really nothing to be done. Go on by reviewing and
comparing the original JVM and JSVM specific namespaces' declarations
with the `valip.predicates` one:

```clj
(ns valip.java.predicates
  "Useful validation predicates implemented for JVM Clojure."
  (:require [valip.predicates :as preds]
            [valip.predicates.def :refer (defpredicate)])
  (:import
    (java.net URI URISyntaxException)
    java.util.Hashtable
    javax.naming.NamingException
    javax.naming.directory.InitialDirContext))
```

```clj
(ns valip.js.predicates
  "Useful validation predicates implemented for ClojureScript using the Google Closure libraries
where necessary."
  (:import goog.Uri))
```

```clj
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms.
All predicates in this namespace are considered portable between different
Clojure implementations."
  (:require [clojure.string :as str]
            [cljs.reader :refer [read-string]])
  (:refer-clojure :exclude [read-string]))
```

#### Never read from untrusted source

First we have to make a short digression about security writing a
strong statement:

[**Never use `clojure.core/read` and `clojure.core/read-string`]
functions when dealing with untrusted sources in
CLJ**(https://groups.google.com/forum/#!topic/clojure/YBkUaIaRaow)

That said, both `cljs.reader/read` and `cljs.reader/read-string` are
not affected from the same security issues, because they adopted the
same approach used to implement `clojure.edn/read` and
`clojure.edn/read-string` which can read data structure, but are not
able to evaluate them.

All that to say that in the `valip.predicates` namespace declaration
we have to differentiate the reader namespace requirement between CLJ
and CLJS.

Following is the temporary `valip.predicates` namespace declaration
absorbing the above security issues and any specific namespace
declaration for the two considered platforms (i.e., JVM an JSVM):

```clj
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms."
  #?(:clj (:require [clojure.string :as str]
                    [clojure.edn :refer [read-string]]
                    [valip.predicates.def :refer [defpredicate]])
     :cljs (:require [clojure.string :as str]
                     [cljs.reader :refer [read-string]]))
  #?(:clj (:refer-clojure :exclude [read-string])
     :cljs (:require-macros [valip.predicates.def :refer [defpredicate]]))
  #?(:clj (:import (java.net URI URISyntaxException)
                   java.util.Hashtable
                   javax.naming.NamingException
                   javax.naming.directory.InitialDirContext)
     :cljs (:import goog.Uri)))
```

To make the code more readable, we adopted an expanded declaration
form. If you want to make the declaration more compact you could use
[this smart trick](https://github.com/polytypic/poc.cml/blob/6c80c55aa16ab1253ad3e85705b5360fbed73f7e/src/poc/cml/sem.cljc).

## STEP 6

We already differentiated any platform specific symbol definition in
the `valip.predicates` namespace while we pasted those symbols during
the execution of the Step 3. So there is nothing else left to be done
in this Step 6.

## STEP 7

At the moment we do not need to move/rename the
`src/valip/predicates/def.clj` source file containing any `valip`
macros. We keep the eventual execution of this Step 7 for a later
time.

# Step 8

We now have to deal with the `valip` unit tests confined in the
`src/test` directory. All we have to do here it is to reapply,
*mutatis mutandis*, the above Step 2, Step 4 and Step 5 to the content
of the `test/valip/test` directory:

Start by updating the files extensions:

```bash
mv test/valip/test/core.clj test/valip/test/core.cljc
mv test/valip/test/predicates.clj test/valip/test/predicates.cljc
```

Then open the two files and update their namespaces declarations and
symbols definitions as follows:

```clj
;;; core.cljc
(ns valip.test.core
  #?(:clj (:require [valip.core :refer [validation-on validate]]
                    [clojure.test :refer [deftest is]])
     :cljs (:require [valip.core :refer [validation-on validate]]
                     [cljs.test :refer-macros [deftest is]])))
```

As in the previous cases, you could compact the `:require` expression
by using the trick cited above.

```clj
(ns valip.test.predicates
  #?(:cljs (:require [valip.predicates :refer [present?
                                               matches
                                               max-length
                                               min-length
                                               email-address?
                                               digits?
                                               integer-string?
                                               decimal-string?
                                               gt
                                               gte
                                               lt
                                               lte
                                               over
                                               under
                                               at-most
                                               at-least
                                               between]]
                     [cljs.test :refer-macros [deftest is]])
     :clj (:use valip.predicates clojure.test)))

;;; some tests following...

#?(:clj (deftest test-valid-email-domain?
          (is (valid-email-domain? "example@google.com"))
          (is (not (valid-email-domain? "foo@example.com")))
          (is (not (valid-email-domain? "foo@google.com.nospam")))
          (is (not (valid-email-domain? "foo")))))

;;; some tests following
```

> NOTE 2: In CLJS, when you want to `:use` a namespace instead of
> `:require` it, you must use the `:only` form of `:use`. In those
> cases I still prefer to use the `:refer` form of `:require`.

> NOTE 3: the `valid-email-domain?` predicate can only be evaluated on
> JVM and so it is the corresponding test.

## Step 9

We can now get rid of the original source files containing platforms
specific predicates, because they have been completely absorbed in the
`predicates.cljc` file:

```bash
rm -rf src/valip/java
rm -rf src/valip/js
```

We end up with the following directory layout:

```bash
tree
.
├── README.md
├── project.clj
├── src
│   └── valip
│       ├── core.cljc
│       ├── predicates
│       │   └── def.clj
│       └── predicates.cljc
└── test
    └── valip
        └── test
            ├── core.cljc
            └── predicates.cljc

6 directories, 7 files
```

As you see the only survived pure CLJ file is `def.clj` which is the
one containing the macros definitions.

All those 9 Steps represent the easiest parts of making the `valip`
validation library compliant with the Reader Conditionals
extension. We now have to afford the difficult ones:

* the addition of the
  [`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild)
  plugin to the `project.clj`;
* the tests execution on CLJS.

But before digging in that new sea, we have the opportunity to see if
we're able to run the newly updated `valip` library in CLJ.

## Run and test in CLJ

The only thing we need to do for running and test the updated version
of the `valip` library is to update its `project.clj` build file by
upgrading the pinned CLJ release in the `:dependencies` section:

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.7.0"]])
```

To run the CLJ tests all you have to do is to launch the `lein test`
task as follows:

```bash
cd /path/to/valip
lein test

lein test valip.test.core

lein test valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
```

WOW! All the unit tests assertions passed. Let's now see if we can use
the `valip` library from the CLJ REPL:

```bash
lein repl
nREPL server started on port 49762 on host 127.0.0.1 - nrepl://127.0.0.1:49762
REPL-y 0.3.7, nREPL 0.2.10
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_66-b17
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=>
```

```clj
user=> (require '[valip.predicates :as v])
nil
user=> (v/present? nil)
false
user=> (v/present? "")
false
user=> (v/present? " ")
false
user=> (v/present? "foo")
true
user=>
```

Not so bad. Are not you curious about the predicates that have been
defined within the `#?` reader macro? Let's evaluate them at the REPL:

```clj
user=> (v/url? "http://www.google.com")
true
user=> (v/valid-email-domain? "me@me.com")
true
user=> (v/valid-email-domain? "me@googlenospam.com")
false
```

So far, so good. But the fact that the migrated `valip` library has
been successful tested on CLJ does not mean that it works on CLJS as
well. We now need to afford the boring part. 

## Choosing among alternatives

We now have two alternatives to go on with CLJS:

* we could stay with [`Leiningen`](http://leiningen.org/), which is
  the standard build tool used by the *clojurians*, by adding to it the
  [`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild)
  plugin to compile the CLJS version of the `valip` library;
* we could switch to `boot`.

[`Leiningen`](http://leiningen.org/) has been created by
[Phil Hagelberg](https://github.com/technomancy) in 2009, in the early
days of CLJ itself, when CLJS was not even an idea.

As you saw above, it has been very easy to run and test the CLJ
version of `valip` library by using the default `lein` tasks (e.g.,
`repl` and `test`).

In reality `Leiningen` is much more than a CLJ build tool and its
default list of tasks can be extended via
[plugins](https://github.com/technomancy/leiningen/blob/master/doc/PLUGINS.md). Leiningen
even offers a
[template facility](https://github.com/technomancy/leiningen/blob/master/doc/TEMPLATES.md)
which allows to recreate at will a new project based on a *templatificated* 
project's structure, no matter how complicate it could be. `Leiningen`
template facility could be even used to create a
[`boot` based project](https://github.com/martinklepsch/tenzing).

[`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild) is a
`leiningen` plugin created at the end of 2011 to manage any
compilation tasks required by CLJS an its plethora of compilation
options. In some way, you could consider `cljsbuild` as a build tool
specialized for CLJS.

[`cljx`](https://github.com/lynaghk/cljx) is a second `leiningen`
plugin that got a lot of attention when clojurians realized the
opportunity of writing code able to be ran on JVM and JSVM with few
platform differences.

Before the advent of the Reader Conditionals extension, these were the
two fundamentals tools used to make Clojure(Script) libraries portable
on JVM and JSVM. As said more times, `cljx` is now deprecated, but
`cljsbuild` is still the most used building tool for CLJS.

I personally used `cljsbuild` quite a lot in the past and it saved me
more times from headaches. That said, from when I recently started
using `boot` and few of the tasks implemented by they community, I
always prefer stay with the `boot` building tool when I have to deal
with CLJS, as this new edition of the `modern-cljs` series attests.

Enough words. Let's get started.

## Bootify valip 

First we want to create the `boot.properties` file to pin the `boot`
version to the latest available stable release:

```bash
cd /path/to/valip
boot -V > boot.properties
```

Then, to get rid from the deprecated implicit target directory
emission, as we already did in the very first tutorial while creating
the `modern-cljs` project, we add the `BOOT_EMIT_TARGET=no` statement
in the newly generated `boot.properties` file:

```bash
#http://boot-clj.com
#Wed Jan 06 09:43:32 CET 2016
BOOT_CLOJURE_NAME=org.clojure/clojure
BOOT_CLOJURE_VERSION=1.7.0
BOOT_VERSION=2.5.5
BOOT_EMIT_TARGET=no
```

Next we have to create the `build.boot` file in the main project
directory as well. *Mutatis mutandis* it corresponds to the
`leiningen` `project.clj` build file.

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [adzerk/boot-cljs "1.7.170-3"]])

(require '[adzerk.boot-cljs :refer [cljs]])
```

As you see we started very simple, by setting the `:source-paths` and
the `:dependencies` of the project and finally making the `cljs`
compilation tasks visible with the `require` form.

## Shoot the gun

We are now immediately able to launch the CLJS compilation:

```bash
cd /path/to/valip
boot cljs
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
WARNING: No such namespace: goog.Uri, could not locate goog/Uri.cljs, goog/Uri.cljc, or Closure namespace "" at line 127 src/valip/predicates.cljc
WARNING: Use of undeclared Var goog.Uri/parse at line 127 src/valip/predicates.cljc
```

Ops. As you see the fact that we previously succeeded with CLJ it does
not mean that the CLJS compilation would succeeded as well.

> NOTE 4: we did not use the `target -d target` task to generate the
> output of the CLJS compiler in the `target` directory, because at the
> moment we're only interested in verifying if the CLJS compilation
> succeeds.

At the moment I prefer to move on quickly and I postpone the solution of this bad warning for a later time. To get rid of the above warning:

* open the `predicates.cljc` file;
* remove the CLJS import form from the namespace declaration;
* remove the CLJS `url?` definition.

```clj
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms."
  #?(:clj (:require [clojure.string :as str]
                    [clojure.edn :refer [read-string]]
                    [valip.predicates.def :refer [defpredicate]])
     :cljs (:require [clojure.string :as str]
                     [cljs.reader :refer [read-string]]))
  #?(:clj (:refer-clojure :exclude [read-string])
     :cljs (:require-macros [valip.predicates.def :refer [defpredicate]]))
  #?(:clj (:import (java.net URI URISyntaxException)
                   java.util.Hashtable
                   javax.naming.NamingException
                   javax.naming.directory.InitialDirContext)))
```

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
            (catch URISyntaxException _ false))))
```

Now launch the CLJS compilation again:

```bash
boot cljs
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

As you remember, when you do not specify any option to the `cljs`
task, it uses some defaults:

```bash
cljs -h
Compile ClojureScript applications.

Multiple builds can be compiled parallel. To define builds use .cljs.edn
files. ID of build is the name of .cljs.edn file without the extension.
To compile only specific builds, use ids option to select .cljs.edn files
by name. Output files of build will be put below id.out folder in fileset.

If no .cljs.edn files exists, default one is created. It will depend on
all .cljs files in fileset.

Available --optimization levels (default 'none'):

* none         No optimizations. Bypass the Closure compiler completely.
* whitespace   Remove comments, unnecessary whitespace, and punctuation.
* simple       Whitespace + local variable and function parameter renaming.
* advanced     Simple + aggressive renaming, inlining, dead code elimination.

Source maps can be enabled via the --source-map flag. This provides what the
browser needs to map locations in the compiled JavaScript to the corresponding
locations in the original ClojureScript source files.

The --compiler-options option can be used to set any other options that should
be passed to the Clojurescript compiler. A full list of options can be found
here: https://github.com/clojure/clojurescript/wiki/Compiler-Options.

Options:
  -h, --help                   Print this help info.
  -i, --ids IDS                Conj IDS onto the ids option.
  -O, --optimizations LEVEL    Set the optimization level to LEVEL.
  -s, --source-map             Create source maps for compiled JS.
  -c, --compiler-options OPTS  Set options to pass to the Clojurescript compiler to OPTS.
```

Let's see if `valip` compiles with `whitespace`, `simple` and
`advanced` options as well:

```bash
boot cljs -O whitespace
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

```bash
boot cljs -O simple
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

```bash
boot cljs -O advanced
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

So far, so good. Let's move on with the test task.

## CLJ test

As we learned in a
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-14.md#light-the-fire-on-the-server-side)
`boot` does not come with a predefined CLJ test as `lein` does, but we
can run the CLJ test from within the CLJ REPL:

```bash
boot repl
nREPL server started on port 50242 on host 127.0.0.1 - nrepl://127.0.0.1:50242
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.7.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_66-b17
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

First we have to add dynamically add the `test` directory to
`:source-paths` environment variable of `boot`:

```clj
boot.user=> (merge-env! :source-paths #{"test"})
nil
```

Then we have to require the `clojure.test` namespace and the test
namespaces we defined in the `test` directory as well:

```clj
boot.user> (require '[clojure.test :as t]
                    '[valip.test.core :as tc]
                    '[valip.test.predicates :as tp])
nil
```

We can know launch the tests from the CLJ REPL

```clj
boot.user> (t/run-tests 'valip.test.core 'valip.test.predicates)

Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
{:test 20, :pass 75, :fail 0, :error 0, :type :summary}
```

Nothing new, because we already knew from the previous `lein test` run
that the CLJ tests would passed. But at least we have been able to
obtain the same results. But `boot` want to be serious about covering
what it's already offered to clojurians by the `lein` build tool.

Enter `boot-test` task.

## boot-test task

Now kill the active CLJ REPL and update the `build.boot` build file as follows:

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [adzerk/boot-cljs "1.7.170-3"]
                 [adzerk/boot-test "1.1.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-test :refer [test]])

(deftask testing
  []
  (merge-env! :source-paths #{"test"})
  identity)
```

Here we added the `boot-test` dependency, required its main namespace
to make the `test` task visible and finally defined a `testing` task
to add the `test` directory to the `:source-paths` environment
variable of `boot`.

We can now launch the `test` task as follows:

```clj
boot testing test

Testing valip.core

Testing valip.predicates

Testing valip.predicates.def

Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
```

By default the `test` task runs all the project namespaces, but you
can restrict the namespaces to run `test` into by simply passing the
namespaces you're interested in as follows:

```clj
boot testing test -n valip.test.core -n valip.test.predicates

Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
```

So far, so good. Let's move on with CLJS tests.

# boot-cljs-test

As usual, to use a new task we have to add it to the `:dependencies`
environment variable of the `build.boot` boot file and require its
main namespace to make the task visible to `boot` itself.

```clj
(set-env!
 ...

 :dependencies '[...
                 [crisptrutski/boot-cljs-test "0.2.1"]])

(require '...
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

```

Even if we already used the `test-cljs` task in previous tutorials, it
does not hurt to refresh how it works by asking for its docstring:

```clj
boot test-cljs -h
Run cljs.test tests via the engine of your choice.

 The --namespaces option specifies the namespaces to test. The default is to
 run tests in all namespaces found in the project.

Options:
  -h, --help                 Print this help info.
  -e, --js-env VAL           Set the environment to run tests within, eg. slimer, phantom, node,
                                  or rhino to VAL.
  -n, --namespaces NS        Conj NS onto namespaces whose tests will be run. All tests will be run if
                                  ommitted.
  -s, --suite-ns NS          Set test entry point. If this is not provided, a namespace will be
                                  generated to NS.
  -O, --optimizations LEVEL  Set the optimization level to LEVEL.
  -o, --out-file VAL         Set output file for test script to VAL.
  -c, --cljs-opts VAL        Set compiler options for CLJS to VAL.
  -u, --update-fs?           Only if this is set does the next task's filset include
                                  and generated or compiled cljs from the tests.
  -x, --exit?                Exit immediately with reporter's exit code.
```

As you see, there are a couple of interesting option to be used:

* the `-n` option, to specify the namespaces whose tests will be run;
* the `-O` option, to specify the desired optimization level.

You can now safely launch the `test-cljs` as follows.

```bash
boot testing test-cljs -n valip.test.core -n valip.test.predicates
Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
WARNING: test-max-length at line 32 is being replaced at line 37 test/valip/test/predicates.cljc
WARNING: Use of undeclared Var valip.test.predicates/url? at line 60 test/valip/test/predicates.cljc
WARNING: Use of undeclared Var valip.test.predicates/url? at line 61 test/valip/test/predicates.cljc
WARNING: Use of undeclared Var valip.test.predicates/url? at line 62 test/valip/test/predicates.cljc
WARNING: Use of undeclared Var valip.test.predicates/url? at line 63 test/valip/test/predicates.cljc
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

ERROR in (test-url?) (TypeError:NaN:NaN)
expected: (url? "http://google.com")
  actual: #object[TypeError TypeError: 'undefined' is not an object (evaluating 'valip.test.predicates.url_QMARK_.call')]

ERROR in (test-url?) (TypeError:NaN:NaN)
expected: (url? "http://foo")
  actual: #object[TypeError TypeError: 'undefined' is not an object (evaluating 'valip.test.predicates.url_QMARK_.call')]

ERROR in (test-url?) (TypeError:NaN:NaN)
expected: (not (url? "foobar"))
  actual: #object[TypeError TypeError: 'undefined' is not an object (evaluating 'valip.test.predicates.url_QMARK_.call')]

ERROR in (test-url?) (TypeError:NaN:NaN)
expected: (not (url? ""))
  actual: #object[TypeError TypeError: 'undefined' is not an object (evaluating 'valip.test.predicates.url_QMARK_.call')]

Ran 19 tests containing 71 assertions.
0 failures, 4 errors.
```

Ops, we got some *WARNINGs* and some *ERRORs* too. The first *WARNING*
regards the `text-max-lenght` symbol be defined at line 32 and the
redefined at line 37 of the `predicates.cljc` test file, something
that CLJ compiler was not able to detect. All the other *WARNINGs*
regard the use of the undefined `url?` symbol. This in not a surprise,
because we previously remove it from the CLJS version of the `valip`
library. Moreover, those *WARNINGs* became *ERRORs* during the tests
execution.

Let's fix all these issues. Open the `predicates.cljc` living in the
`test/valip/test` directory. You'll see that, due to some cut&paste
command, Chas Emerick defined the `test-max-length` two times. Just
remove one of them.

To fix the *WARNINGs* and the *ERRORs* about the `url?` not been
defined, just wrap it in the `#?` reader macro as follows:

```clj
#?(:clj (deftest test-url?
          (is (url? "http://google.com"))
          (is (url? "http://foo"))
          (is (not (url? "foobar")))
          (is (not (url? "")))))
```

and re-run the above `boot` command:

```bash
boot testing test-cljs -n valip.test.core -n valip.test.predicates
Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

Ran 18 tests containing 67 assertions.
0 failures, 0 errors.
```

Now we talk. You can even run the CLJ and CLJS test all together as follows:

```bash
boot testing test -n valip.test.core -n valip.test.predicates test-cljs -n valip.test.core -n valip.test.predicates

Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

Ran 18 tests containing 67 assertions.
0 failures, 0 errors.
```

Not so bad, but it's now time to get rid of *WARNING* we got about
`goog.Uri` namespace the very first time we try to launched the `cljs`
task and that we temporally solved by removing the CLJS version of the
`url?` predicate.

## Fix the bug

Let's get a look at the original CLJ and CLJS code:

```clj
;; CLJ
(ns valip.java.predicates
  "Useful validation predicates implemented for JVM Clojure."
  (:require [valip.predicates :as preds]
            [valip.predicates.def :refer (defpredicate)])
  (:import
    (java.net URI URISyntaxException)
    java.util.Hashtable
    javax.naming.NamingException
    javax.naming.directory.InitialDirContext))

(defn url?
  "Returns true if the string is a valid URL."
  [s]
  (try
    (let [uri (URI. s)]
      (and (seq (.getScheme uri))
           (seq (.getSchemeSpecificPart uri))
           (re-find #"//" s)
           true))
    (catch URISyntaxException _ false)))
```

```clj
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

First note that in the CLJS version, the namespace declaration used
the `:import` form for the needed Google Closure Library
`goog.Uri`. But after that `goog.Uri` is used as a namespace in the
`goog.Uri/parse` expression. In such a case you should use the
`:require` form, as documented in the
[CLJS Wiki](https://github.com/clojure/clojurescript/wiki/Google-Closure-Library/8c86561dd33cae261c987cfe8e8a92f0ff5a9c7c#using-google-closure-directly).

<uptohere>

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
