# Tutorial 13 - Validation abstraction

In the [latest tutorial][1] we implemented a very rude server-side
validator for the `loginForm` which exersices the same kind of
syntacic rules previously implemented on the client-side.
	
Our long term objective is to eliminate any code duplication. To reach
this goal we have first to satisfy few intermediate obiectives:

* select a good server-side validator library
* port the selected library on the client-side
* create a new general purpose validator library which generates the
  server-side and the client-side validation code from a single entry.

That's a lot of work to be done for a single tutorial. In this
tutorial we're going to limit ourself by first search for validator
library wich eventually matches the above requirements.

# Introduction

[Github][] contains a large number of CLJ validator libs. If you
restrict your search to CLJS validator libs only you currently get
just one lib: [valip][] which is a port on CLJS of the
[original CLJ valip][]. This is already a good result by itself,
becasue it demonstrates that we share our long term objective with
someone else. If you then take a look at the owners of those libs,
you'll discover that they are two of the most prolific and active
clojure-ists: [Chas Emerick][] and [James Reeves][]. I'd love that the
motto *Smart people think alike* were true!

# The server side

Let's start by adding in our project the server-side valip lib. 

```clojure
;;; project.clj code fragment

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [com.cemerick/shoreleave-remote-ring "0.0.2"]
                 [shoreleave/shoreleave-remote "0.2.2"]
                 [valip "0.2.0"]]
```

The usage of the server-side valip lib is very simple and well
documented in the [usage][] section of the readme file which I
encourage you to read.

First you have a `validate` function from the `valip.core`
namespace. It accepts a map (i.e key/value pairs) and one or more
vectors. Each vector consists of a key, a predicate function and an
error string, like so:

```clojure
(validate map-of-values
  [key1 predicate1 error1]
  [key2 predicate2 error2]
  ...
  [keyn predicaten errorn])
```

If no predicate fails, `nil` is returned. If at least one predicate
fails a map of keys to error values is returned.

```clojure
{key1 [error1]
 key2 [error2]
 ...
 keyn [errorn]}
```

As you can see the value of each key of an error map is a vector,
because a value can fail for more than one predicate. The valip
server-side lib is already provides more pre-defined predicates
(e.g. `ppresent?`) and functions returning predicates (e.g. `matches`)
in the `valip.predicates` namespace, but it's very easy add it new
ones defined by you. A predicate accepts a single argument and returns
`true` or `false` and a function returning a predicate accepts a
single argument and returns a function which accept a single argument
and returns `true` or `false`.

```clojure
;;; from valip.predictes namespace

;;; present? predicate
(defn present?
  "Returns false if x is nil or blank, true otherwise."
  [x]
  (not (string/blank? x)))
  
;;; matches function 
(defn matches
  "Creates a predicate that returns true if the supplied regular expression
  matches its argument."
  [re]
  (fn [s] (boolean (re-matches re s))))
```

If there is a thing that it does not intrigue me about valip lib is
its dependencies from a lot of java packages in the `valip.predicates`
namespace. 

```clojure
(ns valip.predicates
  "Predicates useful for validating input strings, such as ones from a HTML
  form."
  (:require [clojure.string :as string]
            [clj-time.format :as time-format])
  (:import
    [java.net URL MalformedURLException]
    java.util.Hashtable
    javax.naming.NamingException
    javax.naming.directory.InitialDirContext
    [org.apache.commons.validator.routines IntegerValidator
                                           DoubleValidator]))
```

That's not surprising if you take into account that the server-side
`valip` lib has been create more that two years ago, when CLJS was
still floating around eventually in Rich head. What's is surprising is
that Chas Emerick has choosen it just five months ago, when CLJS was
already been digitalized from an idea into a programming language. So,
if `valip` is so dependent from the JVM, why Chas choose it to be
ported on CLJS instead of starting from a CLJ validator less
compromised with the underlaying JVM? I don't know.

Before to commit ourself with `valip` lib, let's see at least one
alternative: [metis][].

The metis validation lib is very young and is inspired to the ruby
Active Records Validation approach. Metis defines the macro
`defvalidator` which expands in a validator function. We know about
macros limitation in JS, but we also know how they can be easly
managed by isolating them and then by excplicitly requiring their
namespace when needed. So, it should not be a PITA to port metis on
CLJS. 

Anyway, let's first take a look at the metis usage before talking
about its porting on CLJS just to verify if satisfy our requirement of
semplicity and completeness by writing down some usage samples:

```closure
;;; email value is not nil
(defvalidator email-validator
	[:email :presence])
	
;;; email values is not nil and add an optional error message
(defvalidator email-validator
	[:email :presence {:message "Please enter your email address"}])

;;; email and password are not nil with a common error message
(defvalidator user-validator
	[[:email :password] {:message "This field is required"}])

;;; email presence and pattern
(defvalidator email-validator
	[:email [:presence :formatted {:pattern #"a pattern"}]])

;;; email and password togheter


	

```
# Next step - TBD

TBD

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[]: https://github.com/cemerick
[]: https://github.com/weavejester
[]: https://github.com/weavejester/valip#usage
[]: https://github.com/mylesmegyesi/metis
