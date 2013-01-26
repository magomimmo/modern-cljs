# Tutorial 13 - Validation abstraction

In the [latest tutorial][1] we implemented a very rude server-side
validator for the `loginForm` which exercises the same kind of
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
restrict your search to CLJS validator libs only, you currently get just
one lib: [valip][] which is a port on CLJS of the
[original CLJ valip][]. This is already a good result by itself, becasue
it demonstrates that we share our long term objective with someone
else. If you then take a look at the owners of those libs, you'll
discover that they are two of the most prolific and active clojure-ists:
[Chas Emerick][] and [James Reeves][]. I'd love that the motto *Smart
people think alike* was true!

# The server side valip lib

The usage of the server-side [valip][] lib is very simple and well
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

## Multiple predicates, single keys

To make things more clear, try to apply this usage to our login sample
like so:

```clojure
(validate {:email email :password password}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid email format"])

```

As you can see you can attach more one or more predicate/function to the
same key.  If no predicate fails, `nil` is returned. If at least one
predicate fails, a map of keys to error values is returned. Again, to
make things easier to be understood, suppose for a moment that the
`email` value passed to `validate` was not well formed and that the
`password` was empty. You would get a result like the following:

```clojure
{:email ["Invalid email format"]
 :password ["Password can't be empty" "Invalid password format"]}
```

As you can see the value of each key of an error map is a vector,
because a value can fail for more than one predicate. That's a nice
feature to have.

## User defined predicates and functions

Even if ehe valip lib already provides a relatively wide range of
pre-defined predicates and functions returning predicates in the
`valip.predicates` namespace, at some point you'll need to define them
by yourself. Valip lib lets you do this very easily.

A predicate accepts a single argument and returns `true` or `false`. A
function returning a predicate accepts a single argument and returns a
function which accept a single argument and returns `true` or
`false`.

Nothing new for any clojure-ist and this is another very nice feature to
have for a validator.

If you need to take some inspiration, here are the definitions of the built-in
`presence?` predicate and `matches` function returning a predicate.

```clojure
;;; from valip.predictes namespace

;;; present? predicate
(defn present?
  [x]
  (not (string/blank? x)))

;;; matches function
(defn matches
  [re]
  (fn [s] (boolean (re-matches re s))))
```

## A doubt to be cleared

If there is a thing that it does not intrigue me about valip server-side
lib is its dependencies from a lot of java packages in the
`valip.predicates` namespace.

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

That is not a suprise, if you take into account that the server-side
`valip` lib has been created more that two years ago, when CLJS was
eventually floating just in few clojure-ist minds.

The surprise is that Chas Emerick has choosen it just five months ago,
when CLJS was already been reified from a very smart idea into a
programming language. So, if the valip lib is so dependent from the JVM,
why Chas Cemerick choose it to port a validator from CLJ to CLJS instead
of choofing a CLJ validator less compromised with the underlaying JVM? I
don't know. Maybe the answer is just that the predefined predicates and
functions were confined in the `valip.predicates` namespace and easily
redefinable in terms of JS interop features of CLJS.

## First try

We already talked to much. Let's see the `valip` lib at work on by
validating our old `loginForm` friend.

Open the `login.clj` file the `src/clj/modern_cljs/` directory. Now
update the `modern-cljs.login` namespace by requiring `valip.core` and
`valip.predicates`. Then update the screwed validator as follows:

```clojure
(ns modern-cljs.login
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? email-address? matches]]))

(def ^:dynamic *re-password*
  #"^(?=.*\d).{4,8}$")

(defn validate-email [email]
  (validate {:email email}
            [:email present? "Email can't be empty"]
            [:email email-address? "The provided email is invalid"]))

(defn validate-password [password]
  (validate {:password password}
            [:password present? "Password can't be empty"]
            [:password (matches *re-password*) "The provided password is invalid"]))

(defn authenticate-user [email password]
  (let [email-errors (validate-email email)
        passwd-errors (validate-password password)]
    (println email-errors passwd-errors)
    (if (and (empty? email-errors)
             (empty? passwd-errors))
      (str email " and " password
           " passed the formal validation, but we still have to authenticate you")
      (str "Please complete the form"))))
```

> NOTE 1: Valip provides the `email-address?` built-in predicate which
> matches the passed email value against a regular expression. This
> regular expression is based on RFC 2822 and it is defined in the
> `valip.predictes` namespace. That way previoulsy defined dynamic
> vairable `*re-email*` has been deleted. Valip also provides the built-in
> `valid-domain-email?` predicate which, by running a DNS lookup, verify
> even the validity of the email domain.

> NOTE 2: As said, `validate` returns a `nil` value when every validation
> passes, and a map of key/vector when any vector does not passes. This is
> something I don't like to much, because it doesn't allow to destruct the
> result and then requires a more complex result management.

> NOTE 3: The `authenticate-user` doesn't individually handle the error
> messages returned by `validate-email` and `validate-password`. This is
> something we're going to correct later.

I don't know about you, but for this simple validation case I can't see
so much code improvement or readability. It is just more
declarative. But I'm pretty sure that for more complicated validation
scenarios a declarative approach is going to give you back more than
that.

It's now time to take seriusly Chas Emerick and try to use the
client-side `valip` lib.

# The client-side valip lib

As you can read from the [readme][] file of the Chas Cemerick's
[valip fork], he tried to make the fork as portable as possibile between
CLJ and CLJS and, as we were supposing at the beginning of this
tutorial, most of the differences reside in the `valip.predicates` namespace
which, as we said, required a lot of java packages.

You can find portable predicates in the `valip.predicates`
namespace. Platform-specifc predicates can be found in
`valip.java.predicates` and `valip.js.predicates`. The `validate`
function from the `valip.core` namespace and the usage of the library is
exactly the same.

## Update the project dependencies

Instead of `[valip "0.2.0"]` lib, we have to add
`[com.cemerick/valip "0.3.2"]` to the dependencies of the project.

```clojure
;;; code fragment

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [com.cemerick/shoreleave-remote-ring "0.0.2"]
                 [shoreleave/shoreleave-remote "0.2.2"]
                 [com.cemerick/valip "0.3.2"]]
```


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
[]: https://github.com/cemerick/valip/blob/master/README.md
