# Tutorial 19 - Livin' on the edge

In the [previous tutorial][1] we completed the job of integrating the
client-side validators for the Shopping Calculator input fields into
the corresponding WUI (Web User Interface) in such a way that the user
will be notified with the corresponding help messages when she enters
invalid values in the form itself.

## Introduction

As you remember, the injected validators have been implemented by
using a forked version of the
[valip](https://github.com/cemerick/valip) validation library done by
[Chas Emerick](https://github.com/cemerick). That library was in turn
a forked version of the [valip](https://github.com/weavejester/valip)
library originally created by
[James Reeves](https://github.com/weavejester) and it was aimed at
making the original library as portable as possible between Clojure
and ClojureScript. When Chas Emerick forked the `valip` original
library in 2012,
[Readers Conditional](http://clojure.org/reader#The%20Reader--Reader%20Conditionals)
did not exist. At those time you had two alternatives to make you code
portable from CLJ to CLJS, namely:

* the
[`crossover`](https://github.com/emezeske/lein-cljsbuild/blob/d5c08124ade0bfd6c548d14a66989f3fdb98a47f/doc/CROSSOVERS.md)
features by the
[`cljsbuild`](https://github.com/emezeske/lein-cljsbuild) plugin;
* the [cljx](https://github.com/lynaghk/cljx) plugin by
  [Kevin Lynagh](https://github.com/lynaghk).

The problem with those alternatives is that they are now both
deprecated in favor of the much easier, more powerful and more
flexible
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
Reader Conditionals is not as hard as you may think.

## Livin' on the edge

When you start using a library implemented by others, you can easily
end up with few misunderstandings of its use or even with some
unexpected issues. In these cases, the first thing you should do is to
browse and read its documentation. As you know, one problem with open
source software regards the corresponding documentation which is
frequently minimal, if not absent, outdated or requiring a level of
comprehension of details which as newbie we still have to grasp.

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
library, the very first thing I do, aside from reading the
corresponding `README.md` file when available, it's to view its
`project.clj` build file, or its `build.boot` build file, in the still
rare case in which the library has been based on `boot`:

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.4.0"]])
```

A pretty minimal build file in such a case. As you see, `valip`
depends on CLJ `1.4.0` release. To be able to use the Reader
Conditionals extension, the very first thing to be updated is the CLJ
dependency: from `1.4.0` to `1.7.0` (in a very short time the
clojure-dev team will deliver the `1.8.0` release):

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.7.0"]])
```

> NOTE 1: In this tutorial we're going to use the
> [`leiningen`](http://leiningen.org/) build tool and you needs to
> install it by following
> [this very easy instruction](http://leiningen.org/#install).


[`leiningen`](http://leiningen.org/) has a lot of sane defaults for
its build directives. For example, if you do not specify a
`:source-paths` directive, it assumes `src` as default. If you do not
specify a `:target-path` directive, it assume `target` as default and
if you do not specify a `:test-paths` it will assume the `test`
directory as default and so on. That's why the `project.clj` is so
noise free in such a case.

## Directory layout

The second thing I generally look into, is the directory layout of the
project directory:

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
the `test` source files are confined in the `test` directory,
mimicking the layout of the `src` directory. Nothing new, even coming
from a `boot` background. As you'll later discover, you can use
exactly the same arrangement of the directory layout regardless of the
build tool you are going to use for the project itself. If you want to
be a very good clojurian citizen, I would even suggest to set your
projects to be built with both `leiningen` and `boot`, by defining the
corresponding `project.clj` and `build.boot` build files.

## Migration preparation

To introduce the Reader Conditionals extension into a library
originally implemented for CLJ only, there few steps you should
follow:

* update the CLJ dependency to the `1.7.0` release, because the Reader
  Conditional extension has been introduced from that release on (we
  already did);
* identify all the functions that are specific for the
  JVM and that can't be evaluate in a CLJS context;
* identify all the macros defined in the library, because CLJS's
  macros must be defined in a different compilation stage from the one
  where they are consumed.

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
* all the functions specific to the JSVM (JavaScript Virtual Machine)
  are confined in the `predicates.cljs` source file hosted in the
  `src/valip/js` directory;
* all the functions which are agnostic from the hosting platform are
  confined in the `core.clj` and `predicates.clj` source files living
  in the `src/valip` directory;
* all the macros are confined in the `def.clj` source file hosted in
  the `src/valip/predicates` directory.

So far, so good. But what does it stand for that `reader.clj` source
file hosted in the `src/cljs` directory?

## A smart trick by a smart guy

As said, the `:source-paths` directive of the `project.clj` file is
set by default to `src`. By knowing the way CLJ maps namespace
segments to file pathnames, there is a smell of something strange
going on in that `src/cljs/reader.clj` source file. Let's take a look
at the it:

```clj
(ns cljs.reader
  "A dummy namespace, allowing valip.predicates to :require cljs.reader even in Clojure,
so as to allow portable usage of `read-string`."
  (:refer-clojure :exclude (read-string)))

(def read-string clojure.core/read-string)
```

Ahah! This is were the smart trick lurks. Chas Emerick defined a dummy
`cljs.reader` namespace mimicking the predefined CLJS `cljs.reader`
namespace. This dummy namespace aliases the `read-string` from the
`clojure.core` namespace in such a way that the `cljs.reader` can be
required even when `valip` runs on a JVM.

To see how this dummy namespace is used, let's take a look at the
`valip.predicates` namespace declaration from the `predicates.clj`
hosted in the `src/valip` directory:

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

* The CLJS `read` and `read-string` symbols are defined in the
  `cljs.reader` namespace, while the corresponding CLJ symbols are
  defined in the `clojure.core` namespace.

This incidental complexity was needed because the Reader Conditionals
were not available at the time of writing the `valip` library. As
we'll see later, even if the dummy namespace seems to be a very smart
trick, it is also affected by a subtle and serious security issue.

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

1. get rid of the above smart and dangerous trick, by deleting the
   `src/cljs` directory and the `reader.clj` file as well;
2. change the file extension of the `predicates.clj` and `core.clj`
   source files hosted in the `src/valip` directory from to `.cljc`;
3. move the specific JVM symbols' definitions from the
   `predicates.clj` source file hosted in the `src/valip/java`
   directory to the above `predicates.cljc` source file;
4. move the specific JSVM symbols' definitions from the
   `predicates.cljs` source file to the above `predicates.cljc` source
   file;
5. use the `#?` reader macro to differentiate the namespaces'
   requirements in the `valip.core` and `valip.predicates` namespaces
   declaration, depending on the feature made available by the hosting
   platform at runtime;
6. use the `#?` reader macro to differentiate the symbols' definitions
   in the `valip.predicates` namespace as well;
7. use the `#?` reader macro to differentiate the namespaces'
   requirements and the symbols' definitions for the unit tests as
   well;
8. delete the original `src/valip/java/predicates.clj` and
   `src/valip/js/predicates.cljs` source files, because we absorbed
   their content in the `predicates.cljc` source file.

Not such a big deal, right? **Wrong!**

The `project.clj` build file is for CLJ only. If we want to able to
compile and test the migrated `valip` library on CLJS, we need to add
both the `lein-cljsbuild` and the
[`lein-doo`](https://github.com/bensu/doo) plugins.

Moreover, unless you got the chance to read the
[first edition](https://github.com/magomimmo/modern-cljs/tree/master/doc/first-edition)
of this series or you do not already know about the `leiningen` build
tool, all those stuff are new to you and the noise/signal ratio of the
final `project.clj` build file is going to get worst when compared
with the `project.clj` we started from.

But don't worry! We're not going to use the `lein`/`cljsbuild` pair of
build tools to make `valip` compliant with Reader Conditionals
extension. We'll continue to use `boot` as we already did for any
previous tutorial of the series.

## Execution (Part I)

In the following paragraphs we're going to execute step by step the
above plan.

### Step 1

As said, we first have to delete the `src/cljs` directory and the
contained `reader.clj` file as well, because we do not need the Chas
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
namespace declarations as well, but we'll take care of this later.

### Step 3 

Next, we have to move the definitions specific for the JVM into the
above `predicates.cljc` source file. Open the `predicates.clj` source
file hosted in the `src/valip/java` directory, copy all the symbols'
definitions and append them in the `predicates.cljc` source file.

While you are at it, introduce the `#?` reader macro to inform the
compiler that these definition are specific for the JVM platform. This
way you're partially anticipating the Step 6:

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

> NOTE 2: note that we removed the `valip.predicates` alias (i.e.,
> `preds`) in the `valid-email-domain` definition because we moved the
> definition itself in that namespace.

### Step 4

You should now do the same thing with the `predicates.cljs` currently
living in the `valip.js` directory. While you're there, take advantage
of the opportunity of adding the CLJS `url?` definition inside the
`#?` reader macro already used for the CLJ `url?` definition as
follows:

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

First we have to make a short digression about the security issue we
talked about above:

[**Never use** `clojure.core/read` and `clojure.core/read-string`](https://groups.google.com/forum/#!topic/clojure/YBkUaIaRaow)
functions when dealing with untrusted sources in CLJ.

That said, both `cljs.reader/read` and `cljs.reader/read-string` are
not affected from the same security issues, because they adopted the
same approach used to implement `clojure.edn/read` and
`clojure.edn/read-string` which can read data structure, but are not
able to evaluate them.

All that to say that in the `valip.predicates` namespace declaration
we have to differentiate the reader namespace requirement between CLJ
and CLJS.

Following is the `valip.predicates` namespace declaration absorbing
the solution for the cited security issue and any other specific
namespace declaration specific for the two considered platforms (i.e.,
JVM an JSVM):

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

# Step 7

We now have to deal with the `valip` unit tests confined in the
`src/test` directory. *Mutatis mutandis*, we have to replicate the
above Step 2, Step 4 and Step 5 to the content of the
`test/valip/test` directory:

Let's start by updating the files extensions:

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

As in the previous cases, you could make the `:require` more compact
by using the same cited
[trick](https://github.com/polytypic/poc.cml/blob/6c80c55aa16ab1253ad3e85705b5360fbed73f7e/src/poc/cml/sem.cljc).

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
                                               between
                                               url?]]
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

> NOTE 3: In CLJS, when you want to `:use` a namespace instead of
> `:require` it, you must use the `:only` form of `:use`. In those
> cases I still prefer to use the `:refer` form of `:require`.

> NOTE 4: the `valid-email-domain?` predicate can only be evaluated on
> JVM and so it is the corresponding test.

## Step 8

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

Those steps represent the easiest part of making the `valip`
validation library compliant with the Reader Conditionals
extension. We should now have to afford the difficult ones:

* the addition of the
  [`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild)
  plugin to the `project.clj`;
* the execution of the tests on the JSVM platform.

Before digging in that new sea, take the opportunity to see if we're
able to run the newly updated `valip` library in CLJ.

## Run and test in CLJ

The only thing we need to do for running and test the updated CLJ
version of the `valip` library is to update its `project.clj` build
file by upgrading the pinned CLJ release in the `:dependencies`
section, but we already did it at the beginning of the tutorial:

```clj
(defproject com.cemerick/valip "0.3.2"
  :description "Functional validation library for Clojure and ClojureScript, forked from https://github.com/weavejester/valip"
  :url "http://github.com/cemerick/valip"
  :dependencies [[org.clojure/clojure "1.7.0"]])
```

You're now ready to run the CLJ tests by launching the `lein test`
task as follows:

```bash
cd /path/to/valip
lein test

lein test valip.test.core

lein test valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
```

Nice work. All the unit tests assertions passed. Let's now see if we
can use the `valip` library from the `lein` based `repl` task:

```bash
cd /path/to/valip
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
```

```clj
user=> (v/present? nil)
false
```

```clj
user=> (v/present? "")
false
```

```clj
user=> (v/present? " ")
false
```

```clj
user=> (v/present? "foo")
true
```

Good. Are not you curious about the predicates that have been
defined within the `#?` reader macro? Let's evaluate them at the REPL:

```clj
user=> (v/url? "http://www.google.com")
true
```

```clj
user=> (v/valid-email-domain? "me@me.com")
true
```

```clj
user=> (v/valid-email-domain? "me@googlenospam.com")
false
```

```clj
user=> (v/url? "www.google.com")
nil
```

As you know, when evaluated in boolean context, in CLJ/CLJS `nil` is
like `false`. I would prefer that the last expression would returned
`false`, but I can live with it.

## Digression about corner cases

One of the few tests I like to write are the ones testing corner
cases. If we want to be serious about functional programming, we
should never forget that a
[function](https://en.wikipedia.org/wiki/Function_(mathematics) is a
relation between a set of inputs and a set of permissible outputs with
the property that each input is related to exactly one output. The
corner cases are where the things became interesting.

Just to make an example, evaluate the following expression at the
REPL:

```clj
(+ 1)
1
```

But what about the following?

```clj
(+)
0
```

This corner case is interesting. In mathematics, the element `0` is
the *identity element* (or *neutral element*) for the `+` operation.

Let's try with the multiplication:

```clj
user=> (* 10)
10
```

```clj
user=> (*)
1
```

As you see the *identity element* for the multiplication is the
element `1`.  On the contrary, subtraction and division do not have an
identity element, as you can verify by yourself:

```clj
user=> (-)

ArityException Wrong number of args (0) passed to: core/-  clojure.lang.AFn.throwArity (AFn.java:429)
```

```clj
user=> (/)

ArityException Wrong number of args (0) passed to: core//  clojure.lang.AFn.throwArity (AFn.java:429)
```

Do you see how important is to test the corner cases? Let's now see
how `valip` behaves on corner cases. We already met two of them:

```clj
(v/present? nil)
false
```
and

```clj
user=> (v/url? "www.google.com")
nil
```

These corner cases does'not regards the arities of the functions. They
regard their domains/co-domains.

Let's now test other corner cases of the functions/predicates defined
by the `valip` library.

```clj
user=> ((v/matches #"...") "foo")
true
user=> ((v/matches #"...") "")
false
user=> ((v/matches #"...") nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
user=>
```

That's bad. The `present?` and the `url?` predicates are defined for
the `nil` element, while the function returned by the `matches` HOF
function is not, as you can verify from it's source code:

```clj
(source v/matches)
(defn matches
  "Creates a predicate that returns true if the supplied regular expression
  matches its argument."
  [re]
  (fn [s] (boolean (re-matches re s))))
nil
```

Let's go one with few other functions/predicates corner cases:

```clj
user=> (v/email-address? nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
```

```clj
user=> (v/integer-string? nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
```

```clj
user=> (v/decimal-string? nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
```

```clj
user=> (v/digits? nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
```

```clj
user=> (v/alphanumeric? nil)

NullPointerException   java.util.regex.Matcher.getTextLength (Matcher.java:1283)
```

Oh my God. They all raised a **null pointer exception** with the `nil`
corner cases. I'm not saying they should not. I'm saying that once you
decided your library behaves in some way, for example not considering
that `nil` element as member of a function, you should uniformly stay
with this decision, and `valip` does not. 

Do you want to see other misalignment? Evaluate a couple of `valip`
HOF functions returning predicates?

```clj
user=> ((v/min-length 5) nil)
false
user=> ((v/max-length 5) nil)
true
```

I don't know about you, but I can't accept such a misaligned
behaviors, because I'd be sure they're going to generate bugs very
difficult to be caught.

Now exit the REPL, because it's time to enter in a TDD session to fix
`valip` original library.

## Enter boot

Even if we could update the `project.clj` buy adding to it the
[`lein-auto`](https://github.com/weavejester/lein-auto) plugin, we
prefer to switch to `boot`, because we already know everything about
creating a `build.boot` build file to support TDD.

First, as you learned in the
[first tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-01.md#get-rid-of-warnings)
we want to pin the current `boot` release to `2.5.5` and get rid of
the warning about the deprecated implicit target directory emission:

```bash
cd /path/to/valip
boot -V > boot.properties
```
Now edit the `boot.properties` to remove the deprecation warning as follows:

```bash
#http://boot-clj.com
#Fri Jan 08 12:16:18 CET 2016
BOOT_CLOJURE_NAME=org.clojure/clojure
BOOT_CLOJURE_VERSION=1.7.0
BOOT_VERSION=2.5.5
BOOT_EMIT_TARGET=no
```

> NOTE 5: The latest available stable `boot` release at time of writing
> was `2.5.5`.

Now create the `build.file` for the `valip` project with the following content:

```clj
(set-env!
 :source-paths #{"src"}

 :dependencies '[[org.clojure/clojure "1.7.0"]
                 [adzerk/boot-test "1.0.7"]])

(require '[adzerk.boot-test :refer [test]])

(deftask testing
  []
  (merge-env! :source-paths #{"test"})
  identity)

(deftask clj-tdd
  "Launch a CLJ TDD Environment"
  []
  (comp
   (testing)
   (watch)
   (test :namespaces #{'valip.test.core 'valip.test.predicates})))
```

Here we set the `:source-paths` environment variable to the `src`
directory. Then we set the need dependencies and made the `test`
symbol visible. Next we define two new tasks:

* `testing` task: to add the `test` directory to the `:source-paths`
  environment variable;
* `clj-tdd`: to launch a CLJ based TDD session.

## TDD session

We're now ready to go. Launch the `clj-tdd` environment:

```bash
cd /path/to/valip
boot clj-tdd

Starting file watcher (CTRL-C to quit)...


Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 75 assertions.
0 failures, 0 errors.
Elapsed time: 4.832 sec
```

All the assertions of all tests succeeded. This is not a surprise,
because we already knew form the previous `lein test` session.

### Add corner cases assertions

Now edit the `test/valip/test/predicates.cljc` file to start adding
the assertions covering the domain corner cases we did not like in the
previous `lein repl` session:

```clj
(deftest test-matches
  (is ((matches #"...") "foo"))
  (is (not ((matches #"...") "foobar")))
  (is (not ((matches #"...") nil)))) ; corner case
```

As soon as you save the file you'll receive the expected error:

```bash
Testing valip.test.core

Testing valip.test.predicates

ERROR in (test-matches) (Matcher.java:1283)
expected: (not ((matches #"...") nil))
  actual: java.lang.NullPointerException: null
 at ...
 
Ran 20 tests containing 76 assertions.
0 failures, 1 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 20, :pass 75, :fail 0, :error 1, :type :summary}
    ...
Elapsed time: 0.647 sec
```

Open the `src/valip/predicates.cljs` to take a look at the `matches`
function definition:

```clj
(defn matches
  "Creates a predicate that returns true if the supplied regular expression
  matches its argument."
  [re]
  (fn [s] (boolean (re-matches re s))))
```

The `java.lang.NullPointerException: null` has been raised because we
passed the `nil` value to the anonymous function returned by `matches`
and `re-matches` does not like it, because it expects the argument to
be a string.

Now launch the CLJ REPL as usual

```bash
# from a new terminal
cd /path/to/valip
boot repl
nREPL server started on port 50035 on host 127.0.0.1 - nrepl://127.0.0.1:50035
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

because we want to play a little bit with the `re-matches` function:

```clj
boot.user=> (re-matches #"..." nil)

java.lang.NullPointerException:
```

Ok. Let's see if we pass it a void string `""` instead of the `nil` value.

```clj
boot.user=> (re-matches #"..." "")
nil
```

Could we transform `nil` into `""`? Sure, with the `str` function:

```cl
boot.user=> (str nil)
""
```

We can now go back to the `predicates.cljc` file to fix the bug:

```clj
(defn matches
  "Creates a predicate that returns true if the supplied regular expression
  matches its argument."
  [re]
  (fn [s] (boolean (re-matches re (str s))))) ;; wrap s within str
```

As soon as you save the file `clj-tdd` re-execute the tests and
returns success:

```bash
Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 76 assertions.
0 failures, 0 errors.
Elapsed time: 0.656 sec
```

While we are there, let's add the `((matches #"...") "")` corner
case. We already know it will succeed as well.

```clj
(deftest test-matches
  (is (not ((matches #"...") "")))   ; corner case
  (is (not ((matches #"...") nil)))  ; corner case
  (is ((matches #"...") "foo"))
  (is (not ((matches #"...") "foobar"))))
```

```bash
Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 77 assertions.
0 failures, 0 errors.
Elapsed time: 0.561 sec
```

## Extend the coverage

If you consider that all `valip` functions/predicates receive strings
as arguments to be evaluates, we already have the hint to extend the
current `valip` test assertions to cover and fix the `nil` corners
cases in the corresponding source code: just wrap any string argument
within a `str` function.

```clj
(deftest test-max-length
  (is ((max-length 5) ""))       ; corner case
  (is ((max-length 5) nil))
  (is ((max-length 5) "hello"))  ; corner case
  (is ((max-length 5) "hi"))
  (is (not ((max-length 5) "hello world"))))
```

While extending the tests assertions, you'll note that the
`test-max-length` has bee defined two times with a different
body. Something that the CLJ compiler was not able to catch. The
second occurrence had to be renamed has `test-min-length`:

```clj
(deftest test-min-length
  (is (not ((min-length 5) "")))    ; corner case
  (is (not ((min-length 5) nil)))   ; corner case
  (is ((min-length 5) "hello"))
  (is ((min-length 5) "hello world"))
  (is (not ((min-length 5) "hi"))))
```

The next failure you'll met will be with the `test-email-address?`
predicate:

```clj
(deftest test-email-address?
  (is (not (email-address? "")))    ; corner case
  (is (not (email-address? nil)))   ; corner case
  (is (email-address? "foo@example.com"))
  (is (email-address? "foo+bar@example.com"))
  (is (email-address? "foo-bar@example.com"))
  (is (email-address? "foo.bar@example.com"))
  (is (email-address? "foo@example.co.uk"))
  (is (not (email-address? "foo")))
  (is (not (email-address? "foo@bar")))
  (is (not (email-address? "foo bar@example.com")))
  (is (not (email-address? "foo@foo_bar.com"))))
```

Here us the assertion failure report:

```bash
Testing valip.test.core

Testing valip.test.predicates

ERROR in (test-email-address?) (Matcher.java:1283)
expected: (not (email-address? nil))
  actual: java.lang.NullPointerException: null
...
Ran 21 tests containing 86 assertions.
0 failures, 1 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 21, :pass 85, :fail 0, :error 1, :type :summary}
...
Elapsed time: 0.609 sec
```

As said above, to fix the bug we just to need to wrap the passed
argument within a `str` call:

```clj
(defn email-address?
  "Returns true if the email address is valid, based on RFC 2822. Email
  addresses containing quotation marks or square brackets are considered
  invalid, as this syntax is not commonly supported in practise. The domain of
  the email address is not checked for validity."
  [email]
  (let [re (str "(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+"
                "(?:\\.[a-z0-9!#$%&'*+/=?" "^_`{|}~-]+)*"
                "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+"
                "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")]
    (boolean (re-matches (re-pattern re) (str email))))) ; wrap within str
```

Some thing with the `test-url?` corner cases assertion about `nil`
argument:

```clj
(deftest test-url?
  (is (not (url? "")))
  (is (not (url? nil)))
  (is (url? "http://google.com"))
  (is (url? "http://foo"))
  (is (not (url? "foobar"))))
```

```bash
Testing valip.test.core

Testing valip.test.predicates

ERROR in (test-url?) (URI.java:3042)
expected: (not (url? nil))
  actual: java.lang.NullPointerException: null
 at java.net.URI$Parser.parse (URI.java:3042)
    ...
Ran 21 tests containing 89 assertions.
0 failures, 1 errors.
clojure.lang.ExceptionInfo: Some tests failed or errored
    data: {:test 21, :pass 88, :fail 0, :error 1, :type :summary}
...
Elapsed time: 0.572 sec
```

Again, just wrap the passed string argument within an `str` call:

```clj
#?(:clj (defn url?
          "Returns true if the string is a valid URL."
          [s]
          (try
            (let [uri (URI. (str s))]      ; wrap within str
              (and (seq (.getScheme uri))
                   (seq (.getSchemeSpecificPart uri))
                   (re-find #"//" (str s)) ; wrap within str
                   true))
            (catch URISyntaxException _ false)))
   :cljs (defn url?
           [s]
           (let [uri (-> s goog.Uri/parse)]
             (and (seq (.getScheme uri))
                  (seq (.getSchemeSpecificPart uri))
                  (re-find #"//" s)))))
```

> NOTE 6: at the moment we do not care about the CLJS definition of
> `url?` predicate.

Keep going on with the corner cases coverage. Next stop is `test-digit?`.

```clj
(deftest test-digits?
  (is (not (digits? "")))
  (is (not (digits? nil)))
  (is (digits? "01234"))
  (is (not (digits? "04xa"))))
```

Same failure, same solution:

```clj
(defn decimal-string?
  "Returns true if the string represents a decimal number."
  [s]
  (boolean (re-matches #"\s*[+-]?\d+(\.\d+(M|M|N)?)?\s*" s)))
```

I know, it's going to be boring, but the happy paths assertion are
much more boring than those corner cases.

Next stop is `test-integer-string?`. Some story as above

```clj
(deftest test-integer-string?
  (is (not (integer-string? "")))
  (is (not (integer-string? nil)))
  (is (integer-string? "10"))
  (is (integer-string? "-9"))
  (is (integer-string? "0"))
  (is (integer-string? "  8  "))
  (is (not (integer-string? "10,000")))
  (is (not (integer-string? "foo")))
  (is (not (integer-string? "10x")))
  (is (not (integer-string? "1.1"))))
```

and same fix too:

```cljs
(defn integer-string?
  "Returns true if the string represents an integer."
  [s]
  (boolean (re-matches #"\s*[+-]?\d+\s*" (str s))))
```

You'll have to keep going in the same way up to the end of the
`predicates.cljc` testing file and you're done. Your final test report
should be the following:

```bash
Testing valip.test.core

Testing valip.test.predicates

Ran 21 tests containing 97 assertions.
0 failures, 0 errors.
Elapsed time: 0.619 sec
```

You can now stop the `boot` process to proceed with the next step.

## Choosing among alternatives

The fact the migrated `valip` library has been successful tested on
CLJ does not mean that it will work on CLJS as well. We should now
afford another boring part: tooling.

As you saw above, it was very easy to run and test the CLJ version of
`valip` library by using a couple of default
[`leiningen`](http://leiningen.org/) tasks (e.g., `repl` and `test`).

On the contrary, we switched to `boot` building tool to easily create
test automation (i.e., `clj-tdd` task).

We have two alternatives to go on with CLJS:

* we could stay with `Leiningen`, which is the standard build tool
  used by the *clojurians*, by adding to it the
  [`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild)
  plugin to compile the CLJS version of the `valip` library;
* we could switch to `boot`.

`Leiningen` has been created by
[Phil Hagelberg](https://github.com/technomancy) in 2009, in the early
days of CLJ itself, when CLJS was not even an idea.

`Leiningen` can be extended via
[plugins](https://github.com/technomancy/leiningen/blob/master/doc/PLUGINS.md)
and even offers a
[template facility](https://github.com/technomancy/leiningen/blob/master/doc/TEMPLATES.md)
which allows to recreate at will a new project based on a
*templatificated* project's structure, no matter how complicate it
could be. `Leiningen` template facility could be even used to create a
`boot` [based project](https://github.com/martinklepsch/tenzing).

[`lein-cljsbuild`](https://github.com/emezeske/lein-cljsbuild) is a
`leiningen` plugin created at the end of 2011 to manage any
compilation tasks required by CLJS an its plethora of compilation
options. In some way, you could consider `cljsbuild` as a build tool
specialized for CLJS.

[`cljx`](https://github.com/lynaghk/cljx) is a second `leiningen`
plugin that got a lot of attention when clojurians realized the
opportunity of writing code able to be ran on JVM and JSVM as well
with few platform differences.

Before the advent of the Reader Conditionals extension, these were the
two fundamentals tools used to make Clojure(Script) libraries portable
on JVM and JSVM. As said more times, `cljx` is now deprecated, but
`cljsbuild` is still the most used building tool for CLJS.

I personally used `cljsbuild` quite a lot in the past and it saved me
more times from headaches. That said, from when I recently started
using `boot` and few of the tasks implemented by they community, I
always prefer to stay with the `boot` building tool when I have to
deal with CLJS, as this new edition of the `modern-cljs` series
attests.

Enough words. Let's get started.

## Bootify CLJS valip 

To be able to quickly set up a CLJ TDD environment, in the previous
paragraph we already created the `boot.properties` and `build.boot`
files. It is now very easy to update the `build.boot` file to be able
to extend it for covering the CLJS version of `valip` as well. We
start very simple, by just adding the `boot-cljs` boot task and
requiring its main `cljs` task to run the CLJS compiler.


```clj
(set-env!
 ...
 :dependencies '[...
                 [org.clojure/clojurescript "1.7.170"]
                 [adzerk/boot-cljs "1.7.170-3"]])

(require '...
         '[adzerk.boot-cljs :refer [cljs]])
```

> NOTE 7: note that we also added the latest stable CLJS release to the
> `:dependencies` section.

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

Ops. We immediately get a couple of warning. The first says the CLJS
compiler was not able to find the
[`goog.Uri`](https://google.github.io/closure-library/api/class_goog_Uri.html)
namespace, which is both a class and a namespace defined in the Google
Closure Library. The second warning says that the `parse` symbol, that
should live in the `goog.Uri` namespace, it is undefined.

Uhm, pretty weird warnings.

## Getting rid of warnings

To me actual warnings are potential errors and I don't like at all to
let them survive to eventually wake me up at night or while I'm on
vacation. This to say that we're now going to get rid of them.

First, let's get a look at the `predicates.cljc` source file starting
from its namespace declaration:

```clj
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from HTML forms."
  ...
  #?(:clj (:import (java.net URI URISyntaxException)
                   java.util.Hashtable
                   javax.naming.NamingException
                   javax.naming.directory.InitialDirContext)
     :cljs (:import goog.Uri)))
```

As you see, in the `:cljs` condition of the `#?` reader macro, the
namespace declaration `:import` the Google Closure `goog.Uri` class as
suggested by the
[corresponding paragraph](https://github.com/clojure/clojurescript/wiki/Google-Closure-Library/8c86561dd33cae261c987cfe8e8a92f0ff5a9c7c#using-google-closure-directly)
in the CLJS wiki.

Now proceed to the `url?` definition:

``clj
#?(:clj  (...)
   :cljs (defn url?
           [s]
           (let [uri (-> s goog.Uri/parse)]
             (and (seq (.getScheme uri))
                  (seq (.getSchemeSpecificPart uri))
                  (re-find #"//" s)))))
```

In the thread first macro expression `(-> s goog.Uri/parse)`,
`goog.Uri` is used as a namespace while calling the `parse` static
function. Moreover, by taking a look at the
[`goog.Uri` documentation](https://google.github.io/closure-library/api/class_goog_Uri.html),
you'll note that the `getSchemeSpecificPart` getter does not exist and
it's only available on the
[Java counterpart](http://docs.oracle.com/html/E18812_01/html/fc830ed0-6054-3c49-4d9b-ec34f10e92fb.htm).

Considering that `url` validation is a very complicated topic and
would even necessitate of a URL parser, I would be inclined to remove
the current CLJ and CLJS definitions.

That said, just as a matter of explanation on how to remove the above
warnings, here it's the solution:

```clj
#?(:clj (...)
   :cljs (defn url?
           [s]
           (let [uri (.parse goog.Uri (str s))]
             (and (seq (.getScheme uri))
                  ;(seq (.getSchemeSpecificPart uri))
                  (re-find #"//" (str s))))))
```

and launch the CLJS compilation again:

```bash
boot cljs
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
```

Good shot. Note as in the above code we also covered the corner case
of `nil` argument passed to `url?` as we already did for the CLJ
definition of the same function.

## CLJS compiler optimizations

Note that in the above `boot cljs` command we did not specify any
option to the `cljs` task. As you already know from previous
tutorials, this is because `cljs` uses some defaults:

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

Let's now see if `valip` compiles with `whitespace`, `simple` and
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

So far, so good. Let's move on with the CLJS test task.

## cljs-tdd

We already ran the `clj-tdd` environment in a previous
paragraph. Let's now update the `build.boot` build file to be able to
launch a `cljs-tdd` environment as well. As usual, to use a new `boot`
task we have to add it to the `:dependencies` environment variable of
the `build.boot` boot file and require its main namespace to make the
task visible to `boot` itself.

```clj
(set-env!
 ...

 :dependencies '[...
                 [crisptrutski/boot-cljs-test "0.2.1"]])

(require '...
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])
...

(deftask cljs-tdd
  "Launch a CLJ TDD Environment"
  []
  (comp
   (testing)
   (watch)
   (test-cljs :namespaces #{'valip.test.core 'valip.test.predicates})))
```

You can now safely launch the `cljs-tdd` newly defined task as
follows.

```bash
boot cljs-tdd

Starting file watcher (CTRL-C to quit)...

Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 91 assertions.
0 failures, 0 errors.
Elapsed time: 15.061 sec
```

Nice shot. But considering that we are dealing with a portable
library, we'd like to run the CLJ and CLJS tests all together, as we
already did for the `modern-cljs` project. That's very easy
too. Substitute the previously defined `clj-tdd` and `cljs-tdd` with
the following `tdd` new task definition:

```clj
boot tdd

Starting file watcher (CTRL-C to quit)...

Writing clj_test/suite.cljs...
Writing output.cljs.edn...
Compiling ClojureScript...
• output.js
Running cljs tests...
Testing valip.test.core

Testing valip.test.predicates

Ran 20 tests containing 91 assertions.
0 failures, 0 errors.

Testing valip.test.core

Testing valip.test.predicates

Ran 21 tests containing 97 assertions.
0 failures, 0 errors.
Elapsed time: 18.135 sec
```

Ok. We're done

## What's next

We reached our first objective of making the `valip` library compliant
with the Reader Conditional extension. During the process, we were also
able to solve a couple of problems affecting the original `valip`
library:

* the potential security issue created by the use of the
  `clojure.core/read-string` function;
* the bug in the `url?` predicate defined in the context of CLJS.

That said, we still have work to do, namely:

* locally install the updated `valip` library;
* publish the `valip` library to [`clojars`](https://clojars.org/) to
  make it available to whoever will be interested to.

### Boot task 

If you take a look at any CLJ/CLJS project based on `boot` build tool,
you'll quickly discover that the standard way to build a jar and push
it to a repository is
[bootlaces](https://github.com/adzerk-oss/bootlaces).


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
