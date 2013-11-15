# Tutorial XX - sync or swim

One of the defining characteristics of the Lisp-family of languages is the
ability to take concepts from other languages and implement them at a library
level.  One of the recent examples of this is the [core.async][1] library, which
adds asynchronous communication ala-[golang][2].

## Introduction

In this tutorial, we're going to integrate `core.async` and use it to add
client-side validations to our shopping form.  In the process, we will get to
experience a new way of thinking about and reacting to events.

> NOTE 1: I suggest you keep track of your work by issuing the
> following commands at the terminal:
>
> ```bash
> git clone https://github.com/magomimmo/modern-cljs.git
> cd modern-cljs
> git checkout tutorial-22
> git checkout -b tutorial-XX-step-1
> ```

## Dependencies

Since `core.async` is a fairly bleeding-edging library, we need to bump up the
versions of clojurescript and cljsbuild to make everything work.

```clj
(defproject modern-cljs "0.1.0-SNAPSHOT"
  ...
  ...
  :dependencies [,,,
    [org.clojure/clojurescript "0.0-2014"]
    ,,,
    [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
    ; you may need this as well to avoid dependency problems
    [org.clojure/tools.reader "0.7.10"]]
  ,,,
  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            ,,,]
```

## Adding Validation

The first thing we'll do with `core.async` is perform client-side validation of
the shopping cart form whenever a field changes.  Since we'll want to be able to
validate a single form field (as opposed to the entire form), we first add a
simple method to the end of `shopping/validatiors.clj`:

```clojure
(defn validate-field [field val]
  (field (validate-shopping-form val val val val)))
```

A slight hack, this function validates a single field using the
`validate-shopping-form` function we wrote previously by passing in the same
value for each of the fields, then just getting the errors for the field we care
about.

Next, we will add the new requirements to `shopping.cljs`:

```clojure
(ns modern-cljs.shopping
  (:require-macros [hiccups.core :refer [html]]
                   [cljs.core.async.macros :refer [go-loop]])
  (:require [domina :refer [by-id value by-class set-value! append! destroy!
                            add-class! remove-class! text set-text!]]
            [domina.events :refer [listen! prevent-default]]
            [domina.xpath :refer [xpath]]
            [hiccups.runtime :as hiccupsrt]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [modern-cljs.shopping.validators :refer [validate-field]]
            [cljs.core.async :refer [put! chan >! <! map<]]))
```

We will be using the `go-loop` macro from `core.async`, as well as the various
channel manipulation functions.  We also require the `validate-field`
function, some more domina dom-manipulation functions, and the domina `xpath`
function.

Next, we define a helper function to create a channel that will transmit events
from an element.

```clojure
(defn listen [el type]
  (let [out (chan)]
    (listen! el type (partial put! out))
    out))
```

The function simply creates a channel called out, uses the domina `listen!`
function to register a callback which will put the recieved event into the
channel, then returns the new channel.

Next, we'll define a helper function which will create a channel of the
validation errors for a given field.

```clojure
(defn errors-for [field]
  (map< (fn [evt]
          (or (validate-field (keyword field) (.-value (:target evt)))
              []))
        (listen (by-id field) :change)))
```

The `map<` function takes events recieved on the channel created by `listen`
and uses the `validate-field` function to create a channel of error vectors
(note that we replace nils with empty lists to avoid inadvertently indicating
that the channel is exhausted).

With these functions in place, we can define our function to asynchronously check
a given field.

```clojure
(defn check-field [field errs-chan]
  (let [label (xpath (str "//label[@for='" field "']"))
        title (text label)]
    (go-loop
      []
      (let [errs (<! errs-chan)]
        (if (empty? errs)
          (-> label (remove-class! "error") (set-text! title))
          (-> label (add-class! "error") (set-text! (first errs))))
        (recur)))))
```

This function recieves the name of the field and a channel of errors (which
we'll create with `(errors-for field)`).  We get the corresponding label for the
input field and keep track of what it's original title should be, then spawn our
go-routine with `go-loop`.

Like in the Go language, this creates an asynchronous process which can wait for
inputs on a channel without blocking anything else.  Instead of using the basic
`go` macro, we use `go-loop`, equivalent to `(go (loop ...`, since we want to
keep processing events forever.  The routine will wait for an error to come in
on the `errs-chan` channel, then either display an error message in the label or
restore the original state of the label.

Now, we can create one of these goroutines for each field in the form in `init`
with a simple loop over the ids of the fields.

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [fields '("quantity" "price" "tax" "discount")]
      (loop [fields fields]
       (when (not (empty? fields))
        (check-field (first fields) (errors-for (first fields)))
        (recur (rest fields)))))
    (listen! (by-id "calc") :click (fn [evt] (calculate evt)))
    (listen! (by-id "calc") :mouseover add-help!)
    (listen! (by-id "calc") :mouseout remove-help!)))
```

Now, we can do our usual incantation after making changes:

```bash
lein clean-start!
```

After rebuild, go to the [shopping url][3] (making sure Javascript is enabled
now) and try putting some values in the form.  You'll see that if you put an
invalid value in one of the fields, a message will appear as soon as you leave
the input and go away once corrected.

## Automatic updates

Now that we have our error messages appearing right away, it's starting to feel
clunky to have to click the "Calculate" button every time we make a change.
Let's leverage the techniques we've just learned to automatically recalculate
the total when the values change.  The one complication, however, is that we
don't want to try to calculate a new total if any of the fields are invalid.

First, we'll need to add the `merge` function from `core.async` to our list of
requires:

```clojure
(ns modern-cljs.shopping
  ...
  (:require ,,,
            [cljs.core.async :refer [put! chan >! <! map< merge]]))
```

Next, we'll need to make some slight modifications to the `calculate` function.
As it currently exists, the function takes in the domina event so it can prevent
the form from submitting.  However, if we're going to make the button completely
unnecessary, so let's just remove it.  We remove the `evt` paramater and the
call to `prevent-default` from `calculate` so it looks like this:

```clojure
(defn calculate []
  (let [quantity (value (by-id "quantity"))
        price (value (by-id "price"))
        tax (value (by-id "tax"))
        discount (value (by-id "discount"))]
    (remote-callback :calculate
                     [quantity price tax discount]
                     #(set-value! (by-id "total") (.toFixed % 2)))))
```

Next, we remove the `listen!` calls in `init` and replace them with a call to
`destroy!`:

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [fields '("quantity" "price" "tax" "discount")]
      (loop [fields fields]
        (when (not (empty? fields))
          (check-field (first fields) (errors-for (first fields)))
          (recur (rest fields)))))
    (destroy! (by-id "calc"))))
```

Now, we're ready to add our function to recalculate the total automatically.

```clojure
(defn recalc-total [errs-chan]
  (go-loop
    [err-fields #{}]
    (let [[field errs] (<! errs-chan)]
      (when (not (empty? errs))
        (recur (conj err-fields field)))
      (let [rest-errs (disj err-fields field)]
        (when (empty? rest-errs)
          (calculate))
        (recur rest-errs)))))
```

This function will also receive a channel which will get form errors.  Unlike
`check-field` though, the channel for `recalc-total` will contain values which
are vectors like `[field-name error-vector]`.  This way it can keep track of
which fields are in an invalid state and only update when all the fields are
copacetic.

Lastly, we'll need to create a channel to feed this function, which will do
using `map<` and `cljs.core.async/merge` in `init` as follows:

```clojure
(defn ^:export init []
  (when (and js/document
             (aget js/document "getElementById"))
    (let [fields '("quantity" "price" "tax" "discount")
          all-field-errs (map (fn [field] (map< #(vector field %) (errors-for field))) fields)]
      (recalc-total (merge all-field-errs))
      (loop [fields fields]
        (when (not (empty? fields))
          (check-field (first fields) (errors-for (first fields)))
          (recur (rest fields)))))
    (destroy! (by-id "calc"))))
```

Now, run `lein compile` and refresh and you will see the shopping form
automatically updating the total whenever a valid change is made.

# Next Step

TO BE DONE

# License

By James Cash, for Mimmo Cosenza to do whatever he wishes with.

[1]: http://clojure.github.io/core.async/
[2]: http://golang.org/
[3]: http://localhost:3000/shopping.html
