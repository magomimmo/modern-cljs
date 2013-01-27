# Tutorial 13 - Don't Repeat Yourself while crossing the border

In the [latest tutorial][1] we implemented a very rude server-side
validator for the `loginForm` which exercises the same kind of
syntactic rules previously implemented on the client-side.

One of our long term objectives is to eliminate any code duplication
from our web applications.  That's like to say we want firmly stay as
compliant as possible with the Don't Repeat Yourself (DRY) principle.

# Introduction

In this tutorial of the *modern-cljs* series we're going to respect the
DRY principle while adhering to the progressive enhancement
strategy. Specifically, we'll try to exercise both the DRY principle and
the progressive enhancement strategy in one of the most relevant context
in developing a web application: the form fields validation.

Let's start by writing down few technical intermediate requirements to
solve this problem space. We need to:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and cliente code.

That's a lot of work to be done for a single tutorial. Take your time to
follow it step by step.

# The selection process

If you search GitHub for a CLJ validator library you'll find quite a
large number of results, but if you restrict the search to CLJS library
only, you currently get just one result: [valip][2]. [Valip][2] is a
fork of the [original CLJ valip][3] to make it portable on CLJS
platform. This is already a good result by itself, becasue it
demonstrates that we share our long term objective with someone else. If
you then take a look at the owners of those two Github repos, you'll
discover that they are two of the most prolific and active clojure-ists:
[Chas Emerick][4] and [James Reeves][5]. I'd love that the motto *Smart
people think alike* was true.

We will eventually search for others CLJ validator libraries later. For
the moment, by following the Keep It Small and Stupid (KISS) pragmatic
approach, we stay with the [Valip][2] library which already seems to
satify the first three intermediate requirements we just listed in the
introduction: its goodness should be guaranteed by the quality of its
owners and it already runs on both CLJ and CLJS.

# The server side validation

Let's start by using Valip on the server-side first. [Valip][2] usage is
damn simple and well documented in the [readme][6] file which I
encourage you to read.

First, Valip provides you a `validate` function from the `valip.core`
namespace. It accepts a map and one or more vectors. Each vector
consists of a key, a predicate function and an error string, like so:

```clojure
(validate {:key-1 value-1 :key-2 value-2 ... :key-n value-n
  [key-1 predicate-1 error-1]
  [key-2 predicate-2 error-2]
  ...
  [key-n predicate-n error-n])
```

To keep things simple, we are going to apply the Valip lib to our old
`Login` tutorial sample. Here is a possible `validate` usage for the
`loginForm` input elements:

```clojure
(validate {:email "xxx@zzz.com" :password "zzzz1"}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid password format"])
```

As you can see you can attach more one or more predicate/function to the
same key.  If no predicate fails, `nil` is returned. That's important to
be remember when you'll exercise the `validate` function. If at least
one predicate fails, a map of keys to error values is returned. Again,
to make things easier to be understood, suppose for a moment that the
email value passed to `validate` was not well formed and that the
password was empty. You would get a result like the following:

```clojure
;;; the sample call
(validate {:email "zzzz" :password nil})

;;; will returns

{:email ["Invalid email format"]
 :password ["Password can't be empty" "Invalid password format"]}
```

The value of each key of an error map is a vector, because the
`validate` function can catch for more than one predicate failure for
each key. That's a very nice feature to have.

## User defined predicates and functions

Even if Valip library already provides, via the `valip.predicates`
namespace, a relatively wide range of portable and pre-defined
predicates and functions returning predicates, at some point you'll need
to define new predicates by yourself. Valip lets you do this very easily
too.

A predicate accepts a single argument and returns `true` or `false`. A
function returning a predicate accepts a single argument and returns a
function which accept a single argument and returns `true` or
`false`.

Nothing new for any clojure-ist which knows about HOF (Higher Order
Function), but another nice feature to have in your hands.

If you need to take some inspiration when defining your own predicates
and functions, here are the definitions of the built-in `presence?`
predicate and the `matches` function which returns a predicate.

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

If there is a thing that it does not intrigue me about the original
original [Valip][3] library is its dependency from a lot of java
packages in the `valip.predicates` namespace.

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

This is not a suprise if you take into account that it has been created
more that two years ago, when CLJS was eventually floating just in few
clojure-ist minds.

The surprise is that Chas Emerick has choosen it just five months ago,
when CLJS was already been reified from a very smart idea into a
programming language. So, if the original [Valip][3] library was so
dependent on the JVM, why Chas Cemerick choose it when he decided to
port a validator from CLJ to CLJS instead of choofing a CLJ validator
less compromised with the underlaying JVM? I don't know. Maybe the
answer is just that the predefined predicates and functions were
confined in the `valip.predicates` namespace and most of them were
easily redefinable in portable terms.

## First try

We already talked to much. Let's see the [Valip][2] lib at work by
validating our old `loginForm` friend.

First you have to add the [forked Valip][2] library to the project
dependencies.

```clojure
;;; code fragment from project.clj
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [hiccups "0.2.0"]
                 [com.cemerick/shoreleave-remote-ring "0.0.2"]
                 [shoreleave/shoreleave-remote "0.2.2"]
                 [com.cemerick/valip "0.3.2"]]
```

To follow at our best the principle of separation of concerns, let's
create a new namespace specifically dedicated to the `loginForm` fields
validations. Create the `login` subdirectory under the
`src/clj/modern_cljs/` directory and then create a new file named
`validators.clj`. Open it and declare the new
`modern-cljs.login.validators` namespace by requiring the portable namespaces
from the [Valip][2] library.

```clojure
(ns modern-cljs.login.validators
  (:require [valip.core :refer [validate]]
            [valip.predicates :refer [present? matches email-address?]]))
```

In the same file you now have to define the validators for the user
credential (i.e `email` and `password`).

```clojure
(def ^:dynamic *re-password* #"^(?=.*\d).{4,8}$")

(defn user-credential-errors [email password]
  (validate {:email email :password password}
            [:email present? "Email can't be empty."]
            [:email email-address? "The provided email is invalid."]
            [:password present? "Password can't be empty."]
            [:password (matches *re-password*) "The provided password is invalid"]))
```

> NOTE 1: Again, to follow the separation of concerns principle, we
> moved here the *re-password* regular expression previously defined in
> the `login.clj` source file.

> NOTE 2: Valip provides the `email-address?` built-in
> predicate which matches the passed email value against an embedded
> regular expression. This regular expression is based on RFC 2822 and
> it is defined in the `valip.predicates` namespace. This is why the
> `*re-email*` regular expression is not needed anymore.

> NOTE 3: The original [Valip][3] also provides the built-in
> `valid-domain-email?` predicate which, by running a DNS lookup, verify
> even the validity of the email domain. This is not a portable
> predicates and as such it leaves in the `valip.java.predicates`
> namespace og the [forked by Chas Emerick Valip library][2].

We know need to update the `login.clj` by calling the just defined
`user-credential-errors` function.  Open the `login.clj` file from the
`src/clj/modern_cljs/` directory and modify it as follows:

```clojure
(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn authenticate-user [email password]
  (let [{email-errors :email
         password-errors :password} (user-credential-errors email password)]
    (println email-errors)
    (println password-errors)
    (if (and (empty? email-errors)
             (empty? password-errors))
      (str email " and " password
           " passed the formal validation, but we still have to authenticate you")
      (str "Please complete the form."))))
```

> NOTE 4: Even if we could have returned back to the user more detailed
> messages from the validator result, to maintain the same behaviour of
> the previous server-side login version we only return the *Please
> complete the form.* message when the user typed something wrong in the
> form fields.

I don't know about you, but even for such a small and stupid case, the
use of a validator library seems to be effective, at least in terms of
code clarity and readability.

Anyway, let's run the application as usual to verify that the
interaction with the just added validator is working as expected,
that means as at the end of the [latest tutorial][14].

```bash
$ rm -rf out # it's better to be safe than sorry
$ lein clean # it's better to be safe than sorry
$ lein cljsbuild clean # it's better to be safe than sorry
$ lein cljsbuild auto dev
$ lein ring server-headless # in a new terminal
```

Visit [login-dbg.html][7] page and repeat all the interactiion tests we
executed in the [latest tutorial][1]. Remeber to first disabled the JS
of the browser.

When you submit the [login-dbg.html][7] page you should receive a `Please
complete the form` message anytime you do not provide the `email` and/or
the `password` values and anytime the provided values do not pass the
corresponding validation predicates.

When the provided values for the email and password input elements pass
the validator rules, you should receive the following message:
*xxx.yyy@gmail.com and zzzx1 passed the formal validation, but we still
have to authenticate you*. So far, so good.

It's now time to take seriusly Chas Emerick and try to use the [Valip][2]
library on the client-side.

# Cross the border

As you can read from the [readme][8] file of the Chas Cemerick's
[Valip fork][2], he tried to make its code as portable as possibile
between CLJ and CLJS and, as we were supposing at the beginning of this
tutorial, most of the differences reside in the `valip.predicates`
namespace which, as we said, originally required a lot of java packages.

You can find the portable predicates in the `valip.predicates` namespace
we already used. The platform-specifc predicates can be found in
`valip.java.predicates` and `valip.js.predicates`.

## Hard time

It's now time to dedicate our attention to the client-side validation to
verify that we can use the [valip fork][2] from Chas Emerick.

Before doing any code modification let's face one big problem: the so
called [Feature Expression Problem][9].

> Writing programs that target Clojure and ClojureScript involves a lot of
> copy and pasting. The usual approach is to copy the whole code of one
> implementation to a source file of the other implementation and to
> modify the platform dependent forms until they work on the other
> platform. Depending on the kind of program the platform specific code is
> often a fraction of the code that works on both platforms. A change to
> platform independent code requires a modification of two source files
> that have to be kept in sync. To solve this problem branching by target
> platform on a form level would help a lot.

The current workaround for this really big problem is to ask the help of
the [lein-cljsbuild][10] plugin we have already been using from the very
beginning of this series of short tutorials on ClojureScript.

## Thanks to Evan Mezeske

[Evan Mezaske][11] made a great job by donating [lein-cljsbuild][15] to the
clojurescript community. I suspect that without it I would never been
able even to launch the CLJS compiler. Big, really big thanks to him and
to all the others who helps him in keeping it updated with the frequent
CLJS and Google Closure Compiler releases.

[Lein-cljsbuild][15] plugin has a `:crossovers` option which allows to
CLJ and CLJS to share any code that is not specific to the CLJ/CLJS
underlying virtual machines (i.e. JVM and JSVM).

The trick is pretty simple. Any namespace you want to share between CLJ
and CLJS has to be put in a vector which is attached to the
`:crossovers` keyword option of the `:cljsbuild` section of your
project. It's quicker to do than it's to say. Here is the interested
`project.clj` code fragment.

```clojure
  ;; code fragment from project.clj

  ;; cljsbuild tasks configuration
  :cljsbuild {:crossovers [valip.core valip.predicates modern-cljs.login.validators]
              :builds
              {:dev
               {;; clojurescript source code path
                :source-path "src/cljs"

                ;; Google Closure Compiler options
                :compiler {;; the name of emitted JS script file
                           :output-to "resources/public/js/modern_dbg.js"

                           ;; minimum optimization
                           :optimizations :whitespace
                           ;; prettyfying emitted JS
                           :pretty-print true}}
  ;; other code follows
```

As you can seed we added three namespaces to the `:crossovers` option:

* `valip.core` namespace which includes the portable CLJ/CLJS core code
  of the library;
* `valip.predicates` namespace which includes the portable CLJ/CLJS code
  where the portable `predicates` are defined;
* `modern-cljs.login.validators` namespace which includes the validator
  we defined and that we want to share between the server-side and
  client-side of our web application.

To have a much better understanding of the `:crossovers` option I
strongly recommend you to read the [original documentation][12].

## The magic of the Don't Repeat Yourself principle

Here we are. We reached the point. Let's see it the magic works.

Open the `login.cljs` file from the `src/cljs/modern-cljs/` directory
and start by first adding the `modern-cljs.login.validators` namespace
where we defined the `user-credential-errors` validator.

```clojure
(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
            [domina.events :refer [listen! prevent-default]]
            [hiccups.runtime :as hiccupsrt]
            [modern-cljs.login.validators :refer [user-credential-errors]]))
```

Then we have just to review the code by injecting the validators shared
with the server-side code. We start by reviewing the `validate-mail`
definition.

```clojure
(defn validate-email [email]
  (destroy! (by-class "email"))
  (if-let [{errors :email} (user-credential-errors (value email) nil)]
    (do
      (prepend! (by-id "loginForm") (html [:div.help.email (first errors)]))
      false)
    true))
```

The modification is limited, but very representative of the objective we
have reached. Now we are just returning any email validation error and
passing the first of them to the `prepend!` function for manipulating
the DOM.

> NOTE 5: Here we used two clojure specific expressions: the map
> destructoring idiom and the `if-let` form. If you don't understand
> them, I strongly suggest you to search the web for their
> usage. Generally speaking, [ClojureDocs][17] is a [good stop][13].

The next function to be reviewed is `validate-password`. As you can see,
the modifications are almost identical.

```clojure
(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [{errors :password} (user-credential-errors nil (value password))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))
```

> NOTE 6: As an exercise, you can define a new function, named
> `validate-dom-element`, which bubbles up an abstraction from
> `validate-email` and `validate-password` structure definition. It's just
> another application of the DRY principle. Then, this could be the starting
> point of a CLJS validation library based on `defprotocol` and
> incorporating the CLJ/CLJS shared part of the validation: the data
> validation. A validation process could be seen as two parts: the data
> validation part and the user interface rendering part. By separating
> che concerns you can even end up with something practical for the
> clojure-ist community.

Finally, we have to review the `validate-form` and the `init` function.

```clojure
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

> NOTE 7: To maintain the same behaviour as before, we did not refactor
> the `validate-form` to much.

Aren't you curious like me to see if everything is still working? Let's
run the application as usual:

```bash
$ rm -rf out # it's better to be safe than sorry
$ lein clean # it's better to be safe than sorry
$ lein cljsbuild clean # it's better to be safe than sorry
$ lein cljsbuild auto dev
$ lein ring server-headless # in a new terminal
```

Now visit as usual the [login-dbg.html][7] page and exercise all the
interaction test we already did in this and in the [previous tutorial][14].

Great, really great! We satisfied all the four requirements we started
from:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and cliente code.

Best of all were able to follow the DRY and the separation of concernes
principles while adhering to the progressive enhancement strategy. I
can't be more happy.

The only residual violation of the DRY principle regards the HTML5
validations which are still duplicated in the `login-dbg.html`
page. This will be solved in successive tutorial when we will introduce
the so called *pure HTML template system*.

# Suggested Homework

If you want to extend the work that we've done here you could try to
introduce the `valid-email-domain?` predicate in the server validation
code. Then, by using the approach we already explained in the
[ajax tutorial][16], you could try to attach the email domain validation
to the `blur` event of the `email` element of the `loginForm`. In this
way you'll reach a very good user interaction for a login form, by
validating the email domain as soon as possible via an ajax call which
bypasses the browser cross domain limitation.

# Next step - TBD

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012-13. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
[2]: https://github.com/cemerick/valip
[3]: https://github.com/weavejester/valip
[4]: https://github.com/cemerick
[5]: https://github.com/weavejester
[6]: https://github.com/cemerick/valip/blob/master/README.md
[7]: http:/localhost:3000/login-dbg.html
[8]: https://github.com/cemerick/valip/blob/master/README.md
[9]: http://dev.clojure.org/display/design/Feature+Expressions
[10]: https://github.com/emezeske/lein-cljsbuild
[11]: https://github.com/emezeske
[12]: https://github.com/emezeske/lein-cljsbuild/blob/0.3.0/doc/CROSSOVERS.md
[13]: http://clojuredocs.org/clojure_core/clojure.core/if-let
[14]:https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md#first-try
[15]: https://github.com/emezeske/lein-cljsbuild
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-10.md
[17]: http://clojuredocs.org/clojure_core/clojure.core/let#example_878
