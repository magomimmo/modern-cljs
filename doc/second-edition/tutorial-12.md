# Tutorial 12 - Don't Repeat Yourself

In the [latest tutorial][1] we implemented a very crude server-side
validator for the Login Form which exercises the same kind of
syntactic rules previously implemented on the client-side.

One of our long term objectives is to eliminate any code duplication
from our web applications.  That's like saying that we want to stay as
compliant as possible with the Don't Repeat Yourself (DRY) principle.

## Preamble

If you want to start working from the end of the [previous tutorial][1],
assuming you've [git][21] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-11
```

# Introduction

In this tutorial of the *modern-cljs* series we're going to respect the
DRY principle while adhering to the progressive enhancement
strategy. Specifically, we'll try to exercise both the DRY principle and
the progressive enhancement strategy in one of the most relevant contexts
in developing a web application: the form fields validation.

Start by writing down few technical intermediate requirements to solve
this problem space. We need to:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and client code.

That's a lot of work to be done for a single tutorial. Take your time
to follow it step by step.

# The selection process

If you search GitHub for a CLJ validator library you'll find quite a
large number of results, but if you restrict the search to CLJS
library only, you currently get just one result: [Valip][2].

> NOTE 1: The above assertion is no longer true. In winter 2012, when I
> wrote the first edition of this series of tutorials, there were
> almost no CLJS form validators. Today, thanks to the awesome efforts
> of the CLJ/CLJS community, the problem has became the opposite:
> there are many of them to choose from.  That said, I decided to keep
> using the `valip` library in the series because it still has
> something to be learnt from.

[Valip][2] has been forked from the [original CLJ Valip][3] to make it
portable to the CLJS platform. This is already a good result by
itself, because it demonstrates that we share our long term objective
with someone else. If you then take a look at the owners of those two
Github repos, you'll discover that they are two of the most prolific
and active clojure-ists: [Chas Emerick][4] and [James Reeves][5]. I'm
happy that the motto *Smart people think alike* was true.

You will eventually search for other CLJ validator libraries. For the
moment, by following the Keep It Simple, Stupid
([KISS](https://en.wikipedia.org/wiki/KISS_principle)) pragmatic
approach, stay with the [Valip][2] library which already seems to
satisfy the first three intermediate requirements we just listed in
the introduction: its quality should be guaranteed by the quality of
its owners and it already runs on both CLJ and CLJS.

# The server side validation

Let's start by using `valip` on the server-side first. `valip` usage is
dead simple and well documented in the [readme][6] file which I
encourage you to read.

First, `valip` provides you a `validate` function from the `valip.core`
namespace. It accepts a map and one or more vectors. Each vector
consists of a key, a predicate function and an error string, like so:

```clojure
(validate {:key-1 value-1 :key-2 value-2 ... :key-n value-n}
  [key-1 predicate-1 error-1]
  [key-2 predicate-2 error-2]
  ...
  [key-n predicate-n error-n])
```

To keep things simple, we are going to apply the `valip` lib to our
old `Login Form` sample. Here is a possible `validate` usage for the
`loginForm` input elements:

```clj
(validate {:email "you@yourdomain.com" :password "weak1"}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid password format"])
```

As you can see, you can attach one or more predicates/functions to the
same key.  If no predicate fails, `nil` is returned. That's important
to remember when you'll exercise the `validate` function because it
could become misleading. If at least one predicate fails, a map of
keys to error values is returned. Again, to make things easier to
understand, suppose for a moment that the email value passed to
`validate` was not well formed and that the password was empty. You
would get a result like the following:

```clj
;;; a sample call
(validate {:email "zzzz" :password nil}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid password format"])

;;; should returns

{:email ["Invalid email format"]
 :password ["Password can't be empty" "Invalid password format"]}
```

The value of each key of an error map is a vector, because the
`validate` function can catch more than one predicate failure for
each key. That's a very nice feature to have.

## User defined predicates and functions

Even if `valip` library already provides, via the `valip.predicates`
namespace, a relatively wide range of portable and pre-defined
predicates and functions returning predicates, at some point you'll need
to define new predicates by yourself. `valip` lets you do this very easily
too.

A predicate accepts a single argument and returns `true` or
`false`. Here is a sample from the `valip.predicates` namespace:

```clj
(defn present?
  [x]
  (not (string/blank? x)))
```

You can also write a function returning a predicate accepting a single
argument and returning a boolean value as well. Again, here is a
sample from the `valip.predicates` namespace:

```clj
(defn matches
  [re]
  (fn [s] (boolean (re-matches re s))))
```

> NOTE 2: I personally consider the above `matches` definition as
> bugged. As you'll see in a subsequent tutorial specifically dedicated
> to `unit tests`, I always like to start testing functions from border
> cases. What happens when the passed argument `s` to the
> above anonymous function returned from `matches` is `nil`?
> 
> You'll get a `NullPointerException` on the JVM and an almost
> incomprehensible error on the JSVM.
> 
> A defensive approach is to wrap the above `s` argument inside a `str`
> call:
> 
> ```clj
> (defn matches
>   [re]
>   (fn [s] (boolean (re-matches re (str s)))))
> ```

Nothing new for any clojure-ist who knows about HOF (Higher Order
Functions), but another nice feature to have in your hands.

`valip` even offers the `defpredicate` macro, which allows you to
easily compose new predicates returning a boolean value. Here is a
sample got from the lib itself.

```clj
(defpredicate valid-email-domain?
  "Returns true if the domain of the supplied email address has a MX DNS entry."
  [email]
  [email-address?]
  (if-let [domain (second (re-matches #".*@(.*)" email))]
    (boolean (dns-lookup domain "MX"))))
```

If you need to take some inspiration when defining your own predicates
and functions, use those samples as references.

## A doubt to be cleared

If there was one thing that did not excite me about the original
[valip][3] library, it was its dependency on a lot of java packages in
the `valip.predicates` namespace.

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

This is not a suprise if you take into account that it was made more
than five years ago, when CLJS was floating around in just a few
clojure-ist minds.

The surprise is that Chas Emerick chose it a couple of years later,
when CLJS had already been reified from a very smart idea into a
programming language. So, if the original [valip][3] library was so
dependent on the JVM, why would Chas Emerick chose it over other,
less-compromised CLJ validation libraries? Maybe the answer is just
that the predefined predicates and functions were confined to the
`valip.predicates` namespace and most of them were easily redefinable
in portable terms.

## Something new to take into account

Even if Chas Emerick made a great work by rewriting the `valip` lib in
such a way that you could use it from CLJ or from CLJS, at those time
the so called
[`Features Expression Problem`](http://dev.clojure.org/display/design/Feature+Expressions)
was still to be solved in CLJ/CLJS.

You had two workarounds to use a portable (or almost
portable) CLJ/CLJS lib:

* use the
  [`:crossover`](https://github.com/emezeske/lein-cljsbuild/blob/a627aeaf797f77bea7aebd6fb8c594852b3c156a/doc/CROSSOVERS.md#sharing-code-between-clojure-and-clojurescript-deprecated)
  option of the the
  [`lein-cljsbuild` plugin](https://github.com/emezeske/lein-cljsbuild)
  for [`leiningen`][23], which is now deprecated;
* use the [lein-cljx](https://github.com/lynaghk/cljx) leiningen
  plugin, which added other complexity to the already complex enough
  `project.clj` declaration and it's now deprecated as well.

> NOTE 3: in this second edition of the `modern-cljs` series of
> tutorials I made the choice of using the `boot` building tools instead
> of the more standard `leiningen` one. There are two main reasons for
> that:
> 
> * the `build.boot` is shorter/simpler than the corresponding
>   `project.clj` when adding `CLJS` stuff;
> * you can run everything in a single JVM instance.

Starting with the `1.7.0` release, Clojure offers a new way to solve
the above Feature Expression Problem. I'm not going to explain it
right now. I'm anticipating you about it only because I rewrote the
[`valip`](https://github.com/magomimmo/valip/tree/0.4.0-SNAPSHOT) lib
by using that new feature in such a way that we can easily use it
inside our series of tutorials without having to do with the above
complexities.

## Add valip dependency

As usual when using a new lib, the first thing to be done is to add it
to the `dependencies` section of the project contained in the
`build.boot` build file.

```clj
(set-env!
 ...
 :dependencies '[...
                 [org.clojars.magomimmo/valip "0.4.0-SNAPSHOT"]
                 ])
...
```

## Start the IFDE

You already know. I always like to work in a live environment. So
start the IFDE

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 19.288 sec
```

and visit the [Login Form](http://localhost:3000/index.html).

Start the `REPL` as well, but refrain yourself from starting the bREPL
on top of it, because at the moment we're going to work on the
server-side only (i.e. CLJ).

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

## REPLing with valip

Require both `valip.core` and `valip.predicates` namespaces from
`valip`:

```clj
(use 'valip.core 'valip.predicates)
```

Test the `validate` form we cited above:

```clj
boot.user> (validate {:email "you@yourdomain.com" :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
nil
```

As you see it correctly returns `nil` because both the passed `email`
and `password` strings satisfy the validation tests.

Let's see few others failing cases:

```clj
boot.user> (validate {:email nil :password nil}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Email can't be empty" "Invalid email format"], :password ["Password can't be empty" "Invalid password format"]}
```

There we passed a `nil` value for both the `email` and `password`
arguments and the `validate` function returns a `map` containing the
error massages for both the `:email` and `:password` keys.

```clj
boot.user> (validate {:email "you@yourdomain.com" :password nil}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:password ["Password can't be empty" "Invalid password format"]}
```

There we passed a well formed `email` and a `nil` `password`
arguments. The returned `map` contains the `:password` key/value pair
only, because the `email` satisfies the validation predicates.

```clj
boot.user> (validate {:email nil :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Email can't be empty" "Invalid email format"]}
```

There we passed a void `mail` and and a `password` that matches the
used regex. So the return `map` contains the `:email` key only.

```clj
boot.user> (validate {:email "bademail@baddomain" :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Invalid email format"]}
```

There we passed a bad `mail` and a valid `password` arguments. The
return `map` contains one message only for the bad `email` address.

```clj
boot.user> (validate {:email "you@yourdomain.com" :password "badpasswd"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:password ["Invalid password format"]}
```

There we finally passed a nice `email` and an invalid `password`
arguments. The return `map` contains the message for the bad
`password` only.

OK, we familiarized enough with the `validate` function. We can now
start coding in a source file.

## validators.clj

To follow at our best the principle of separation of concerns, let's
create a new namespace specifically dedicated to the Login Form fields
validations.

To do that, create the `login` subdirectory under the
`src/clj/modern_cljs/` directory and then create a new file named
`validators.clj`.

```bash
# from a new terminal
cd /path/to/modern-cljs
mkdir src/clj/modern_cljs/login
touch src/clj/modern_cljs/login/validators.clj
```

Open the newly create source file and declare the new
`modern-cljs.login.validators` namespace by requiring the needed
namespaces from the `valip` library we just added to the `build.boot`
build file.

```clj
(ns modern-cljs.login.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? matches email-address?]]))
```

In the same file you now have to define the validators for the user
credential (i.e `email` and `password`). 

```clj
(def ^:dynamic *re-password* #"^(?=.*\d).{4,8}$")

(defn user-credential-errors [email password]
  (validate {:email email :password password}
            [:email present? "Email can't be empty."]
            [:email email-address? "The provided email is invalid."]
            [:password present? "Password can't be empty."]
            [:password (matches *re-password*) "The provided password is invalid"]))
```

As you see, we wrapped the above `validate` call inside a function
definition.

> NOTE 4: Again, to follow the separation of concerns principle, we
> moved here the *re-password* regular expression previously defined
> in the `login.clj` source file. Later you'll delete it from there.

> NOTE 5: `valip` provides the `email-address?` built-in predicate
> which matches the passed email value against an embedded regular
> expression. This regular expression is based on RFC 2822 and it is
> defined in the `valip.predicates` namespace. This is why we're not
> defining here the `*re-email*` regular expression as we did above
> with `*re-password*`.  Later, you'll delete the one previously
> defined in the `login.clj` source file.

So far, so good.

## Update login.clj

Open the `login.clj` file from the `src/clj/modern_cljs/` directory
and modify it as follows:

```clj
(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn authenticate-user [email password]
  (if (boolean (user-credential-errors email password))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
```

Remember to delete anything else. As soon as you save the file,
everything get recompiled.

> NOTE 6: we could have returned more detailed messages from the
> validator result to the user. To maintain the same behaviour of the
> previous server-side login version we only return the **Please
> complete the form.** message when the user typed something wrong in
> the form fields.

I don't know about you, but even for such a small and stupid case, the
use of a validator library seems to be effective, at least in terms of
code clarity and readability.

Let's now interactively verify if the just-added validator is working
as expected.

Visit [Login page](http://localhost:3000/index.html) and repeat all
the interaction tests we executed in the
[latest tutorial][14]. Remember to first disable the JS of the
browser and eventually reload the page too.

When you submit the Login Form you should receive a `Please complete
the form` message anytime you do not provide the `email` and/or the
`password` values and anytime the provided values do not pass the
corresponding validation predicates.

When the provided values for the email and password input elements
pass the validator rules, you should receive a message looking like
the following: **me@me.com and weak1 passed the formal
validation, but we still have to authenticate you**.

So far, so good.

It's now time to see if we're able to use the `valip` portable library
on the client-side as well.

# Crossing the border

Before crossing the border between the server and the client sides,
let's take into account a very important new features added to the
Clojure `1.7.0` release: [`Reader Conditionals`][22].

> Reader Conditionals are a new capability to support portable code that
> can run on multiple Clojure platforms with only small changes. In
> particular, this feature aims to support the increasingly common case
> of libraries targeting both Clojure and ClojureScript.
> 
> Code intended to be common across multiple platforms should use a new
> supported file extension: ".cljc". When requested to load a namespace,
> the platform-specific file extension (.clj, .cljs) will be checked
> prior to .cljc.

The patched `valip` lib has been rewrote by using the above `Reader
Conditionals` new capability, which is the named CLJ/CLJS gave to the
solution of the `Feature Expression` problem.

The `modern-cljs.login.validators` namespace we just wrote is
currently hosted in the `src/clj` source directory of the project. It
required portable namespace from the `valip` patched lib and did not
used any expression available for the JVM platform only. 

What does that means? It means you can safely rename the file with the
`cljc` extension and move it under a new `src/cljc` source path
directory in the `build.boot` building file.

## src/cljc

Even if `boot` is so nice that allows you to modify the `environment`
from the REPL with the `set-env!` function, the updating of the
`build.boot` file is one of the rare cases where I prefer to stop the
IFDE, make the changes and then restart it.

After having stopped IFDE:

* create the `src/cljc/modern_cljs/login` directory structure;
* move there the `validators.clj` source file by renaming it as
  `validators.cljc`;
* update the `:source-paths` section of the `build.boot` file by
  adding to its set the `src/cljc` path;
* start the IFDE again

The above steps are executed as follows:

```bash
cd /path/to/modern-cljs
mkdir -p src/cljc/modern_cljs/login
mv src/clj/modern_cljs/login/validators.clj src/cljc/modern_cljs/login/validators.cljc
rm -rf src/clj/modern_cljs/login
```

Now open the `build.boot` building file to update the `:source-paths`
environment variable:

```clj
(set-env!
 :source-paths #{"src/clj" "src/cljs" "src/cljc"}
 ...
 )
```

and finally restart IFDE and the REPL as well:

```bash
cd /path/to/modern-cljs
boot dev
...
Elapsed time: 23.658 sec
```

```bash
# from a new terminal
cd /path/to/modern-cljs
boot repl -c
...
boot.user=>
```

You're now ready to repeat yourself.

## A call for test automation?

Visit [Login page](http://localhost:3000/index.html) and repeat all
the interaction tests we executed in the
[latest tutorial][14]. Remember to first disable the JS of the
browser.

When you submit the Login Form you should receive a `Please complete
the form` message anytime you do not provide the `email` and/or the
`password` values and anytime the provided values do not pass the
corresponding validation predicates.

When the provided values for the email and password input elements
pass the validator rules, you should receive a message looking like
the following: **me@yme.com and weak1 passed the formal validation,
but we still have to authenticate you**.

Aside from the annoyance of having to repeat the interactive tests to
verify that everything is still working after the above refactoring,
the result is very good. But the magic has still to happen.

## The magic

Here we are. We reached the point. Let's see if the magic works.

You can now start the bREPL on top of the REPL we previously started:

```clj
boot.user=> (start-repl)
...
cljs.user=>
```

Do you want to see if the `valip` namespaces are available from the
CLJS as well? Require them an interact with some of the predicates.

```clj
cljs.user> (require '[valip.core :refer [validate]]
                    '[valip.predicates :refer [present? matches email-address?]])
nil
cljs.user> (present? nil)
false
cljs.user> (present? "")
false
cljs.user> (present? "weak1")
true
cljs.user> (email-address? "me@me.com")
true
```

WOW, that's impressive. Let's go on by trying the `validate`
expressions we previously tested the server side:

```clj
cljs.user> (validate {:email "you@yourdomain.com" :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
nil
```

```clj
cljs.user> (validate {:email nil :password nil}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Email can't be empty" "Invalid email format"], :password ["Password can't be empty" "Invalid password format"]}
```

```clj
cljs.user> (validate {:email "you@yourdomain.com" :password nil}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:password ["Password can't be empty" "Invalid password format"]}
```

```clj
cljs.user> (validate {:email nil :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Email can't be empty" "Invalid email format"]}
```

```clj
cljs.user> (validate {:email "bademail@baddomain" :password "weak1"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:email ["Invalid email format"]}
```

```clj
cljs.user> (validate {:email "you@yourdomain.com" :password "badpasswd"}
                     [:email present? "Email can't be empty"]
                     [:email email-address? "Invalid email format"]
                     [:password present? "Password can't be empty"]
                     [:password (matches #"^(?=.*\d).{4,8}$") "Invalid password format"])
{:password ["Invalid password format"]}
```

Let's see if the `user-credential-errors` function we
defined in the `modern-cljs.login.validators` namespace is working
from the bREPL as well.

Require the namespace and then call the `user-credential-errors`
function:

```clj
cljs.user> (require '[modern-cljs.login.validators :refer [user-credential-errors]])
nil
```

```clj
cljs.user> (user-credential-errors nil nil)
{:email ["Email can't be empty." "The provided email is invalid."], :password ["Password can't be empty." "The provided password is invalid"]}
```

```clj
cljs.user> (user-credential-errors "bademail" nil)
{:email ["The provided email is invalid."], :password ["Password can't be empty." "The provided password is invalid"]}
```

```clj
cljs.user> (user-credential-errors "me@me.com" nil)
{:password ["Password can't be empty." "The provided password is invalid"]}
```

```clj
cljs.user> (user-credential-errors "me@me.com" "weak")
{:password ["The provided password is invalid"]}
cljs.user> (user-credential-errors "me@me.com" "weak1")
nil
```

WOW, I don't know about you, but anytime I see this magic at work, I
would kiss any CLJ/CLJS contributor one by one. No way that anybody
could convince me to that there is something better than CLJ/CLJS on
the web planet.

## Back on Earth

OK, we had enough magic for now. Let's came back on Earth to update
the CLJS `modern-cljs.login` namespace by using those magics.

Open the `login.cljs` source file living in the `src/cljs/modern_cljs`
directory to start updating it while the IFDE is running:

```clj
(ns modern-cljs.login
  (:require [domina.core :refer [append!
                                 by-class
                                 by-id
                                 destroy!
                                 prepend!
                                 value
                                 attr]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime]
            [modern-cljs.login.validators :refer [user-credential-errors]])
  (:require-macros [hiccups.core :refer [html]]))
```

There we updated the namespace declaration by adding the
`modern-cljs.login.validators` requirement containing the
`user-credential-errors` validator.

Then we have to update all the already defined functions by
substituting any previous validation with the new one.

```clj
(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [errors (:email (user-credential-errors (value email) nil))]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    true))
```

The modification is limited, but very representative of the objective we
have reached. Now we are just returning any email validation error and
passing the first of them to the `prepend!` function for manipulating
the DOM.

> NOTE 7: Here we used the `if-let` form. If you don't understand it,
> I strongly suggest you to search the web for their usage. Generally
> speaking, [ClojureDocs][17] and [Grimoire](http://conj.io/) are
> [good][13]
> [stop](http://conj.io/store/v1/org.clojure/clojure/1.7.0/clj/clojure.core/if-let/).

The next function to be reviewed is `validate-password`. As you can see,
the changes are almost identical.

```clj
(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [errors (:password (user-credential-errors nil (value password)))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))
```

> NOTE 8: As an exercise, you can define a new function, named
> `validate-dom-element`, which extracts an abstraction from the
> `validate-email` and `validate-password` structure definition. It's
> just another application of the DRY principle. This could be the
> starting point of a CLJS validation library based on `defprotocol` and
> incorporating the CLJ/CLJS shared part of the validation: the data
> validation. A validation process could be seen as two parts process:
> the data validation part and the user interface rendering part. By
> separating the concerns you can even end up with something practical
> for the clojure-ist community.

Finally, we have to review the `validate-form` and the `init` functions as well.

```clj
(defn validate-form [evt email password]
  (if-let [{e-errs :email p-errs :password} (user-credential-errors (value email) (value password))]
    (if (or e-errs p-errs)
      (do
        (destroy! (by-class "help"))
        (prevent-default evt)
        (append! (by-id "loginForm") (html [:div.help "Please complete the form."])))
      (prevent-default evt))
    true))

(defn ^:export init []
  (if (and js/document
           (aget js/document "getElementById"))
    (let [email (by-id "email")
          password (by-id "password")]
      (listen! (by-id "submit") :click (fn [evt] (validate-form evt email password)))
      (listen! email :blur (fn [evt] (validate-email email)))
      (listen! password :blur (fn [evt] (validate-password password))))))
```

> NOTE 9: To maintain the same behaviour as before, we did not
> refactor the `validate-form` too much and just added `email` and
> `password` DOM elements to `validate-form` itself.

Aren't you curious like me to see if everything is still working? As
soon as you save the `login.cljs` everything gets recompiled and
reloaded but one thing: we modified the `init` function which is
exported to JS an directly called from the script tag inside the
`index.html` page. This is one of the rare cases where you need to
manually reload the page to see the result. So just reload the
[`Login Form`](http://localhost:3000/index.html) and you'll be
launched back to the sky again.

> NOTE 10: if you did not reactivated your browser JS Engine, do it
> before reload the Login Form page

Repeat all the interactive tests you did. I know, it's boring, but at
least you will be proud of the CLJ/CLJS community you're now part of.

We satisfied all the five requirements we started from:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and client code.

Best of all, we were able to follow the DRY and the separation of
concerns principles while adhering to the progressive enhancement
strategy.

The only residual violation of the DRY principle regards the HTML5
validations which are still duplicated in the `index.html` page. This
will be solved in successive tutorials where we will introduce the so
called *pure HTML template system*.

# Let's dance on the crossing border again

As a last paragraph of this tutorial we're going to extend what we've
already done by introducing a server-side only validator which will be
called via ajax by the client code.

As we said, most valip predicates are portable between CLJ and
CLJS. But not all of them. Just to make an example, `valip.predicates`
includes the `valid-email-domain?` predicate which verifies the
existence of the domain of the email address passed by the
user. Because it's implemented in terms of java native code,
`valid-email-domain?` is not available on a JS platform.

It often happens that some validations are not executable directly on
the client-side. However, thanks to ajax machinery, you can bring the
result of a server-side only validation on the client-side as
well. The `valid-email-domain?` predicate is one of such a case. Let's
see how.

## REPLing with email domain

Stop the bREPL (i.e., `:cljs/quit`). You're now back at the CLJ
REPL. Use the `valip.predicates` namespace a familiarize yourself with
the `valid-email-domain?` predicate:

```clj
boot.user> (use 'valip.predicates)
nil
```

```clj
boot.user> (valid-email-domain? "me@me.com")
true

```

```clj
boot.user> (valid-email-domain? "me@yourdomain.com")
true
```

```clj
boot.user> (valid-email-domain? "me@google-nospam.com")
false
```

By residing on the server-side, the `valid-email-domain?` predicate
has no cross site limitations as the browser counterpart.

We now want to verify if the `validate` function living in the
`valip.core` namespace works in tandem with the above
`valip-email-domain?` predicate:

```clj
boot.user> (use 'valip.core)
nil
```

```clj
boot.user> (validate {:email "me@me.com"}
                     [:email valid-email-domain? "The domain does not exist!"])
nil
```

```clj
boot.user> (validate {:email "me@google-nospam.com"}
                     [:email valid-email-domain? "The domain does not exist!"])
{:email ["The domain does not exist!"]}
```

So far, so good. 

We have now to decide in which namespace to define a new validator
verifying the domain of the email passed by the user.

## Reader Conditionals

The most obvious choice is to define such a validator in the same
namespace where we already defined the `user-credential-errors`
validator: the `modern-cljs.login.validators` namespace.

The problem is that such new validator is definable for the JVM only
and the `modern-cljs.logi.validators` namespace is a portable
namespace code living in the `validators.cljc` file (note the `.cljc`
extension).

Fortunatelly, as already said, starting from `1.7.0` release CLJ
offers a pretty handy way to conditionally evaluate a form/expression
depending on the features offered by the environment at compile-time:
the Reader Conditionals.

Currently there are three available platform features identifying the
environments at compile-time:

* `:clj`: to identify JVM
* `:cljs`: to identify JSVM
* `:clr`: identify the MS Common Language Runtime (i.e., MS `.net`)

There are two new reader literal forms: `#?` and `#?@`. At the moment
we're only interested in the first one. The `#?` form is interpreted
similarly to a `cond`: a feature condition is tested until a match is
found, then the corresponding single expression is returned.

Take into account that those platform features and the corresponding
reader literals are only available in source files with `.cljc`
extension and you can't use them in `clj` and `cljs` source files.

That's enough to start using the not portable `valid-email-domain?`
predicate in the portable `modern-cljs.login.validators` namespace to
define a new non portable validator by using the `#?` reader literal.

It's seems complex. Instead is very easy.

### validators.cljc

Open the `validators.cljc` to modify its namespace declaration and to
add a new `email-domain-errors` validator which use the JVM based
`valid-email-domain?` predicate.


```clj
(ns modern-cljs.login.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :as pred :refer [present? 
                                               matches 
                                               email-address?]]))

(def ^:dynamic *re-password* #"^(?=.*\d).{4,8}$")

(defn user-credential-errors [email password]
  (validate {:email email :password password}
            [:email present? "Email can't be empty."]
            [:email email-address? "The provided email is invalid."]
            [:password present? "Password can't be empty."]
            [:password (matches *re-password*) "The provided password is invalid"]))


#? (:clj (defn email-domain-errors [email]
           (validate 
            {:email email}
            [:email pred/valid-email-domain? "The domain of the email doesn't exist."])))
```

There are few important notable aspects in the above code:

1. the `valip.predicates` requirement form is now using both the `:as`
   and the `:refer` options. In this case the namespace declaration is
   shared between CLJ and CLJS and you can't refer the
   `valid-email-domain?` symbol on a JSVM platform. For this reason we
   added the `:as` option;
1. the `email-domain-errors` validator in now wrapped into the `#?`
   reader literal and it's going to be defined only for the JVM
   platform that's identified at compile by the `:clj` feature;
1. to refer to the `valid-email-domain?` predicate we're now using the
   `pred` alias;
1. there is no a `:cljs` condition/expression pair, meaning that the
   newly defined `email-domain-errors` validator is only available on
   the JVM platform.

We now have to call the new server-side-only validator in the
`authenticate-user` function which resides in the `login.clj` file.

```clj
(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors
                                                  email-domain-errors]]))

(defn authenticate-user [email password]
  (if (or (boolean (user-credential-errors email password))
          (boolean (email-domain-errors email)))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
```

In the namespace declaration we only added the `email-domain-errors`
to the `:refer` section of the namespace requirement.

We also changes the `authenticate-user` definition by adding the new
validator inside an `or` form with the old one. 

> NOTE 11: To maintain the previous behaviour of the server-side validation
> we're using the validators as if they were predicates which return just
> `true` or `false`.

If you now visit the Login Form you will see that if you provide a
well formed email address whose domain doesn't exist (e.g.,
`me@googlenospam.com`), you'll pass the client-side validator, but
you'll fail the server-side-only validator. So far, so good. The new
server side validation works as expected. We now need to remotize the
newly defined validator for using its results on the client-side as
well.

## Second step - Remotize the server-side-only validator

As we already know, we can easly remotize a function by using the
[shoreleave][18] machinery. Open the `remotes.clj` file and update the
namespace declaration by requiring the `modern-cljs.login.validators`
namespace, where we defined the `email-domain-errors` server-side-only
validator. Next define the new remote function as you already did in
the [9th Tutorial][24] with the `calculate` function. Here is the
updated content of `remotes.clj`

```clj
(ns modern-cljs.remotes 
  (:require [modern-cljs.core :refer [handler]]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]
            [modern-cljs.login.validators :as v]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(defremote email-domain-errors [email]
  (v/email-domain-errors email))

(def app
  (-> (var handler)
      (wrap-rpc)
      (site)))
```

> NOTE 12: We now require the namespace by using the `:as` option
> instead of the `:refer` option because we want to use the same name
> for the server-side validator and its remotization.

## Third step - Call the remotized validator

The last step consists in calling the newly remotized function from
the client-side code (i.e. CLJS).

Open the `login.cljs` file from `src/cljs/modern-cljs/` directory and
update its content as follows:

```clj
(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as shore-macros])
  (:require [domina.core :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [modern-cljs.login.validators :refer [user-credential-errors]]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]))

(defn validate-email-domain [email]
  (remote-callback :email-domain-errors
                   [email]
                   #(if %
                      (do
                        (prepend! (by-id "loginForm")
                                  (html [:div.help.email "The email domain doesn't exist."]))
                        false)
                      true)))

(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [errors (:email (user-credential-errors (value email) nil))]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    (validate-email-domain (value email))))
```

After having required the `shoreleave.remotes.http-rpc` namespace in the
namespace declaration, we added a new `validate-email-domain` function
which wraps the remotized `email-domain-errors` function via the
shoreleave `remote-callback` function and manipulates the DOM of the
`loginForm` with a new error/help message to the user.

We then updated the previously-defined `validate-email` function by
adding the call to the newly defined `validate-email-domain` function.

## Last step - Test the magic again

You should now check your work by interactively test the Login Form
again.  As usual visit the [index.html][7] page and see what
happens when you provide a well-formed email address whose domain
doesn't exist (e.g. `me@google-nospam.com`).

![ajax validator][19]

We're done. You can now stop any `boot` related process and reset your
git repository.

```bash
git reset --hard
```

# Next step - [Tutorial 13: It's better to be safe than sorry (Part 1)][20]

In the [next Tutorial][20] we are going to prepare the stage for
affording the unit testing topic. We'll also introduce the `Enlive`
template system to implement a server-side only version of the
Shopping Calculator aimed at adhering to the progressive enhancement
implementation strategy. We'll even see how to exercise code
refactoring to satisfy the DRY principle and to resolve a cyclic
namespaces dependency problem we'll meet on the way.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-11.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/weavejester/valip
[4]: https://github.com/cemerick
[5]: https://github.com/weavejester
[6]: https://github.com/cemerick/valip/blob/master/README.md
[7]: http:/localhost:3000/index.html
[8]: https://github.com/cemerick/valip/blob/master/README.md
[9]: http://dev.clojure.org/display/design/Feature+Expressions
[10]: https://github.com/emezeske/lein-cljsbuild
[11]: https://github.com/emezeske
[12]: https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/doc/CROSSOVERS.md
[13]: http://clojuredocs.org/clojure_core/clojure.core/if-let
[14]:https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-11.md#first-try
[15]: https://github.com/emezeske/lein-cljsbuild
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
[17]: http://clojuredocs.org/clojure_core/clojure.core/let#example_878
[18]: https://github.com/shoreleave
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/remote-validator.png
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-13.md
[21]: https://help.github.com/articles/set-up-git
[22]: https://github.com/clojure/clojure/blob/master/changes.md#22-reader-conditionals
[23]: http://leiningen.org/
[24]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-09.md
