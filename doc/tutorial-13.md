# Tutorial 13 - Don't Repeat Yourself

In the [latest tutorial][1] we implemented a very crude server-side
validator for the `loginForm` which exercises the same kind of
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
git checkout tutorial-12
git checkout -b tutorial-13-step-1
```

# Introduction

In this tutorial of the *modern-cljs* series we're going to respect the
DRY principle while adhering to the progressive enhancement
strategy. Specifically, we'll try to exercise both the DRY principle and
the progressive enhancement strategy in one of the most relevant contexts
in developing a web application: the form fields validation.

Let's start by writing down few technical intermediate requirements to
solve this problem space. We need to:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and client code.

That's a lot of work to be done for a single tutorial. Take your time to
follow it step by step.

# The selection process

If you search GitHub for a CLJ validator library you'll find quite a
large number of results, but if you restrict the search to CLJS
library only, you currently get just one result: [Valip][2]. Valip has
been forked from the [original CLJ Valip][3] to make it portable to the
CLJS platform. This is already a good result by itself, because it
demonstrates that we share our long term objective with someone
else. If you then take a look at the owners of those two Github repos,
you'll discover that they are two of the most prolific and active
clojure-ists: [Chas Emerick][4] and [James Reeves][5]. I'm happy that
the motto *Smart people think alike* was true.

We will eventually search for others CLJ validator libraries. For the
moment, by following the Keep It Simple, Stupid
([KISS](https://en.wikipedia.org/wiki/KISS_principle)) pragmatic
approach, we will stay with the [Valip][2] library which already seems
to satisfy the first three intermediate requirements we just listed in
the introduction: its quality should be guaranteed by the quality of
its owners and it already runs on both CLJ and CLJS.

# The server side validation

Let's start by using Valip on the server-side first. Valip usage is
dead simple and well documented in the [readme][6] file which I
encourage you to read.

First, Valip provides you a `validate` function from the `valip.core`
namespace. It accepts a map and one or more vectors. Each vector
consists of a key, a predicate function and an error string, like so:

```clojure
(validate {:key-1 value-1 :key-2 value-2 ... :key-n value-n}
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

As you can see, you can attach one or more predicates/functions to the
same key.  If no predicate fails, `nil` is returned. That's important
to remember when you'll exercise the `validate` function because it
could become misleading. If at least one predicate fails, a map of
keys to error values is returned. Again, to make things easier to
understand, suppose for a moment that the email value passed to
`validate` was not well formed and that the password was empty. You
would get a result like the following:

```clojure
;;; the sample call
(validate {:email "zzzz" :password nil}
  [:email present? "Email can't be empty"]
  [:email email-address? "Invalid email format"]
  [:password present? "Password can't be empty"]
  [:password (matches *re-password*) "Invalid password format"])

;;; will returns

{:email ["Invalid email format"]
 :password ["Password can't be empty" "Invalid password format"]}
```

The value of each key of an error map is a vector, because the
`validate` function can catch more than one predicate failure for
each key. That's a very nice feature to have.

## User defined predicates and functions

Even if Valip library already provides, via the `valip.predicates`
namespace, a relatively wide range of portable and pre-defined
predicates and functions returning predicates, at some point you'll need
to define new predicates by yourself. Valip lets you do this very easily
too.

A predicate accepts a single argument and returns `true` or `false`. A
function returning a predicate accepts a single argument and returns a
function which accepts a single argument and returns `true` or
`false`.

Nothing new for any clojure-ist who knows about HOF (Higher Order
Functions), but another nice feature to have in your hands.

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

If there is one thing that it does not excite me about the
original [Valip][3] library, it is its dependency on a lot of java
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

This is not a suprise if you take into account that it was made
more than two years ago, when CLJS was floating aroun in just a few
clojure-ist minds.

The surprise is that Chas Emerick chose it just five months ago,
when CLJS was already been reified from a very smart idea into a
programming language. So, if the original [Valip][3] library was so
dependent on the JVM, why would Chas Cemerick choose it over other,
less-compromised CLJ validation libraries? I don't know. Maybe the
answer is just that the predefined predicates and functions were
confined to the `valip.predicates` namespace and most of them were
easily redefinable in portable terms.

## First try

We already talked too much. Let's see the [Valip][2] lib at work by
validating our old `loginForm` friend.

First you have to add the [forked Valip][2] library to the project
dependencies.

```clj
(defproject ...
   ...
  :dependencies [...
                 [com.cemerick/valip "0.3.2"]]
  ...)
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
> `valid-domain-email?` predicate which, by running a DNS lookup,
> verify even the validity of the email domain. This is not a portable
> predicate and as such it now leaves in the `valip.java.predicates`
> namespace of the [forked by Chas Emerick Valip library][2].

We now need to update the `login.clj` by calling the just defined
`user-credential-errors` function.  Open the `login.clj` file from the
`src/clj/modern_cljs/` directory and modify it as follows:

```clojure
(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]))

(defn authenticate-user [email password]
  (if (boolean (user-credential-errors email password))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
```

> NOTE 4: Even if we could have returned a more detailed messages from
> the validator result to the user, to maintain the same behaviour of
> the previous server-side login version we only return the *Please
> complete the form.* message when the user typed something wrong in the
> form fields.

I don't know about you, but even for such a small and stupid case, the
use of a validator library seems to be effective, at least in terms of
code clarity and readability.

Anyway, let's run the application as usual to verify that the
interaction with the just-added validator is working as expected.

```bash
rm -rf out # it's better to be safe than sorry
lein do clean, cljsbuild clean, cljsbuild auto dev # it's better to be safe than sorry
lein ring server-headless # in a new terminal
```

Visit [login-dbg.html][7] page and repeat all the interaction tests we
executed in the [latest tutorial][14]. Remember to first disable the JS
of the browser.

When you submit the [login-dbg.html][7] page you should receive a `Please
complete the form` message anytime you do not provide the `email` and/or
the `password` values and anytime the provided values do not pass the
corresponding validation predicates.

When the provided values for the email and password input elements pass
the validator rules, you should receive the following message:
*xxx.yyy@gmail.com and zzzx1 passed the formal validation, but we still
have to authenticate you*. So far, so good.

It's now time to take Chas Emerick seriously, and try to use the [Valip][2]
library on the client-side.

# Cross the border

As you can read from the [readme][8] file of the Chas Cemerick's
[Valip fork][2], he tried to make its code as portable as possibile
between CLJ and CLJS and, as we learned at the beginning of this
tutorial, most of the differences reside in the `valip.predicates`
namespace which, as we said, originally required a lot of java packages.

You can find the portable predicates in the `valip.predicates` namespace
we already used. The platform-specific predicates can be found in
`valip.java.predicates` and `valip.js.predicates`.

## Hard time

It's now time to dedicate our attention to the client-side validation to
verify that we can use the [valip fork][2] from Chas Emerick.

Before doing any code modification let's face one big problem: the so
called [Feature Expression Problem][9].

> Writing programs that target Clojure and ClojureScript involves a lot of
> copy and pasting. The usual approach is to copy the entire code of one
> implementation to a source file of the other implementation and to
> modify the platform dependent forms until they work on the other
> platform. Depending on the kind of program the platform specific code is
> often a fraction of the code that works on both platforms. A change to
> platform independent code requires a modification of two source files
> that have to be kept in sync. To solve this problem, branching by target
> platform on a form level would help a lot.

The current workaround for this really big problem is to ask the help of
the [lein-cljsbuild][10] plugin we have already been using from the very
beginning of this series of short tutorials on ClojureScript.

## Thanks to Evan Mezeske

[Evan Mezeske][11] made a great job by donating [lein-cljsbuild][15] to the
clojurescript community. I suspect that without it I would never been
able even to launch the CLJS compiler. Big, really big, thanks to him and
to all the others who help him in keeping it updated with the frequent
CLJS and Google Closure Compiler releases.

The [lein-cljsbuild][15] plugin has a `:crossovers` option which allows
CLJ and CLJS to share any code that is not specific to the CLJ/CLJS
underlying virtual machines (i.e. JVM and JSVM).

The trick is pretty simple. Any namespace you want to share between CLJ
and CLJS has to be put in a vector which is attached to the
`:crossovers` keyword option of the `:cljsbuild` section of your
project. It's quicker to do than it's to say. Here is the interested
`project.clj` code fragment.

```clj
(defproject ...
  ...
  :cljsbuild {:crossovers [valip.core valip.predicates modern-cljs.login.validators]
              ...}
  ...)
```

As you can see, we added three namespaces to the `:crossovers` option:

* `valip.core` namespace which includes the portable CLJ/CLJS core code
  of the library;
* `valip.predicates` namespace which includes the portable CLJ/CLJS code
  where the portable `predicates` are defined;
* `modern-cljs.login.validators` namespace which includes the validator
  we defined and that we want to share between the server-side and
  client-side of our web application.

To have a much better understanding of the `:crossovers` option I
strongly recommend you read the [original documentation][12].

> NOTE 5: When you use the `:crossovers` option, you do not need to
> add anything to the main Leiningen `:source-paths` from the
> `:source-paths` pertaining the CLJS builds because the corresponding
> CLJ codebase is already there.

## The magic of the Don't Repeat Yourself principle

Here we are. We reached this point. Let's see if the magic works.

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

Then we just have to review the code by injecting the validators shared
with the server-side code. We start by reviewing the `validate-email`
definition.

```clojure
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

> NOTE 6: Here we used the `if-let` form. If you don't understand
> it, I strongly suggest you to search the web for their
> usage. Generally speaking, [ClojureDocs][17] is a [good stop][13].

The next function to be reviewed is `validate-password`. As you can see,
the modifications are almost identical.

```clojure
(defn validate-password [password]
  (destroy! (by-class "password"))
  (if-let [errors (:password (user-credential-errors nil (value password)))]
    (do
      (append! (by-id "loginForm") (html [:div.help.password (first errors)]))
      false)
    true))
```

> NOTE 7: As an exercise, you can define a new function, named
> `validate-dom-element`, which extracts an abstraction from the
> `validate-email` and `validate-password` structure definition. It's
> just another application of the DRY principle. This could be the
> starting point of a CLJS validation library based on `defprotocol` and
> incorporating the CLJ/CLJS shared part of the validation: the data
> validation. A validation process could be seen as two parts process:
> the data validation part and the user interface rendering part. By
> separating the concerns you can even end up with something practical
> for the clojure-ist community.

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

> NOTE 8: To maintain the same behaviour as before, we did not
> refactor the `validate-form` too much and just added `email` and
> `password` DOM elements to `validate-form` itself.

Aren't you curious like me to see if everything is still working? Let's
run the application as usual:

```bash
rm -rf out # it's better to be safe than sorry
lein do clean, cljsbuild clean, cljsbuild auto dev # it's better to be safe than sorry
lein ring server-headless # in a new terminal
```

Now, as usual, visit the [login-dbg.html][7] page and exercise all the
interaction tests we already did in this and in the [previous tutorial][14].

Great, really great! We satisfied all the five requirements we started
from:

* select a good server-side validator library
* verify its portability from CLJ to CLJS
* port the library from CLJ to CLJS
* define a set of validators to be shared between the server and the
  client code
* exercise the defined validators on the server and cliente code.

Best of all, we were able to follow the DRY and the separation of concerns
principles while adhering to the progressive enhancement strategy.

The only residual violation of the DRY principle regards the HTML5
validations which are still duplicated in the `login-dbg.html`
page. This will be solved in successive tutorial when we will introduce
the so called *pure HTML template system*.

# Let's dance on the crossing border

As a last paragraph of this tutorial we're going to extend what we've
already done by introducing a server-side only validator which will be
called also via ajax from the client code.

As we said, most valip predicates are portable between CLJ and CLJS. But
not all of them. Just to make an example, the `valip.java.predicates`
includes a `valid-email-domain?` which verify the existence of the
domain of an email address passed by the user. Because it's implemented
in terms of java native code, `valid-email-domain?` is not available on
the CLJS platform.

Often it happens that some validations are not executable directly on
the client-side. However, thanks to ajax machinery, you can bring the
result of a server-side only validation on the the client-side. The
`valid-email-domain` is one of such a case. Let's see how.

## First step - Create a server only validator

First, you have to define a new validator which will not be shared with
CLJS. Remembering how `lein-cljsbuild` crossovers magic works, you
have to define a new namespace that will not be added to the
`:crossover` option in the `project.clj`.

Create a new `java` directory under the `src/clj/modern_cljs/login`
directory. Then create a new `validators.clj` file in it and next define
the new server-side validator only as follows:

```clojure
(ns modern-cljs.login.java.validators
  (:require [valip.core :refer [validate]]
            [valip.java.predicates :refer [valid-email-domain?]]))

(defn email-domain-errors [email]
  (validate {:email email}
            [:email valid-email-domain? "The domain of the email doesn't exist."]))
```

Have you noticed that we required the `valip.java.predicates` which is
only available to CLJ code? The definition of the new server-side only
validator is very simple. It uses the valip predefined
`valid-email-domain?` we talked about few lines above.

Now we have to call the new server-side-only validator in the
`authenticate-user` function which resides in the `login.clj` file.

```clojure
(ns modern-cljs.login
  (:require [modern-cljs.login.validators :refer [user-credential-errors]]
            [modern-cljs.login.java.validators :refer [email-domain-errors]]))

(defn authenticate-user [email password]
  (if (or (boolean (user-credential-errors email password)) (boolean (email-domain-errors email)))
    (str "Please complete the form.")
    (str email " and " password
           " passed the formal validation, but we still have to authenticate you")))
```

As usual, we had to add the newly created namespace in the namespace
declaration for using the `email-domain-errors` validator inside the
`authenticate-user` function.

> NOTE 9: To maintain the previous behaviour of the server-side validation
> we're using the validators as if they were predicates which return just
> `true` or `false`.

If you now run the application and test it as usual by visiting the
[login-dbg.html][7] you will see that if you provide a well formed email
address whose domain doesn't exist, you'll pass the client-side
validator, but you'll fail the server-side-only validator. So far, so
good.

## Second step - Remotize the server-side-only validator

As we already know from the [10th Tutorial][16], we can easly remotize a
function by using the [shoreleave][18] machinery. Open the `remotes.clj`
file and update the namespace declaration by requiring the
`modern-cljs.login.java.validators` namespace, where we newly defined the
`email-domain-errors` server-side-only validator. Next define the new
remote function as you already did in the [10th Tutorial][16] with the
`calculate` function. Here is the updated content of `remotes.clj`

```clojure
(ns modern-cljs.remotes
  (:require [modern-cljs.core :refer [handler]]
            [modern-cljs.login.java.validators :as v]
            [compojure.handler :refer [site]]
            [shoreleave.middleware.rpc :refer [defremote wrap-rpc]]))

(defremote calculate [quantity price tax discount]
  (-> (* quantity price)
      (* (+ 1 (/ tax 100)))
      (- discount)))

(defremote email-domain-errors [email]
  (v/email-domain-errors email))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))
```

> NOTE 10: Note as we now `:require` the namespce by using the `:as` option instead
> of the `:refer` option because we want to use the same name for the
> server-side-only validator and its remotization.

## Third step - Call the remotized validator

The last step consists in calling from the client-side code (i.e. CLJS)
the newly-remotized function.

Open the `login.cljs` file from `src/cljs/modern-cljs/` directory and
update its content as follows:

```clojure
(ns modern-cljs.login
  (:require-macros [hiccups.core :refer [html]]
                   [shoreleave.remotes.macros :as shore-macros])
  (:require [domina :refer [by-id by-class value append! prepend! destroy! attr log]]
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

We can now compile, run and test again the magic provided to CLJ and
CLJS by the lein-cljsbuild `:crossovers` option and the shoreleave
library.

```bash
rm -rf out # it's better to be safe than sorry
lein clean # it's better to be safe than sorry
lein cljsbuild clean # it's better to be safe than sorry
lein cljsbuild auto dev
lein ring server-headless # in a new terminal
```

As usual visit the [login-dbg.html][7] page and see what happens when
you provide a well-formed email address whose domain doesn't exist.

![ajax validator][19]

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "DRY while crossing the border"
```

Stay tuned for the next tutorial.

# Next step - [Tutorial 14: It's better to be safe than sorry (Part 1)][20]

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
[18]: https://github.com/shoreleave
[19]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/remote-validator.png
[20]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-14.md
[21]: https://help.github.com/articles/set-up-git
