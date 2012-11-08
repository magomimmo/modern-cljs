# Tutorial 4 - Modern ClojureScript

In this tutorial we're going to start the porting of few JavaScript (JS)
samples from the book [Modern JavaScript: Development and Desing][1] by
[Larry Ullman][2]. You can download the code of the book from [here][3].

The reason I choose it as a reference is because it starts smoothly, but
keeps a roboust approach to JS coding. I think that bringing Larry
approach from JS into ClojureScript (CLJS) could be helpful to anyone
not yet fluent in CLJS.

## Introduction

As anybody knows, in the 1990s JS has been prevalently used for
improving and validating HTML forms. Then, in the second half of the
2000s, JS started to be used to make asynchronous requests to a server
side resource and a couple of words, Ajax and Web 2.0, got a lot of
glory in few months.

As I said, I'm going to follow the already cited [Modern JavaScript][1]
book to try to transpose [Larry Ullman][2] approach as a kind of Modern
ClojureScript.

So I'll start by migrating to CLJS his first sample, a login form,
because it's very instructive both in explaning the evolution of the use
of JS in the last decade, and in starting CLJS programming without
knowing to much about Clojure and/or ClojureScript themselves.

> I'm persuading myself that CLJS should be easier to set up not only to
> us, as server-side guys, but for smart client-side guys
> too. Application logic is moving fast from server-side to client-side
> and all of us, as server-side guys, never loved to much that bad LISP
> in C dressed running in the browser. We now have the opportunity to
> see the best LISP ever *running in the browser* and we should try to
> bring client-side programmers with us. Otherwise, we risk to see that
> bad LISP in C dressed *running on the server-side* too, just because
> we were not able to show to those programmers the pretended
> Clojure(Script) superiority.

## Registration form

If you donwloaded [Modern JS][3] code samples, you'll find `login.html`,
`css/styles.css` and `js/login.js` files in `ch02` directory.

![Modern ch02 tree][5]

If you open `login.html` with your browser you should see something like
this:

![Login Form][6]

Now, take a loook at the HTML code.

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Login</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <!-- login.html -->
    <form action="login.php" method="post" id="loginForm">
        <fieldset>
            <legend>Login</legend>

            <div>
              <label for="email">Email Address</label>
              <input type="email" name="email" id="email" required>

            </div>

            <div>
              <label for="password">Password</label>
              <input type="password" name="password" id="password"
              required>
            </div>

            <div>
              <label for="submit"></label>
              <input type="submit" value="Login &rarr;" id="submit">
            </div>

        </fieldset>
    </form>

    <script src="js/login.js"></script>

</body>
</html>
```
### Progressive enhancement and unobstrusive JS

`login.php` script is associated to the `form action`. And `login.js` is
linked within the html page. Aside from `login.js` beeing linked within
`login.html`, there is no direct connection between the `form` and the
JS script. This choice has to do with the so called *progressive
enhancement* and *unobstrusive JS* that Larry Ullman clearly explains in
his book.

The following [sequence diagrams][4] show his approach in action.

#### Server-side only validation

![Login Form Seq DIA 1][8]

The form submitted by the user will be valideted by the server-side `php
script` named `login.php`. If the validation check passes, the server
will login the user, `else` the server will return the error to
the user to allow him to try again.

Thanks to JS and Ajax, this user experience can be improved a lot. A
better solution is to perform a client-side validation using JS, which
bring us to the second sequence diagram.

#### Client-side validation

![Login Form Seq DIA 2][9]

If the client-side (i.e. JS) validation passes, we still have to ask the
server-side validation for security reasons. But if the client-side
validation does not pass, we need not to make a round-trip with the
server and we can immediatly return the errors to the user.

But we still have some problems. The client-side validation cannot
verify if the username is a registered one. Which bring us to Ajax and
to the third sequence diagram.

#### Ajax in action

![login Form Seq DIA 3][10]

The user experience has been now much more enhanced. The Ajax call
communicates with the server (e.g. to verify if the email address does
exist) resulting in a more efficient and responsive process.

In this tutorial we are going to limit ourselves to the client-side
validation scenario without implementing the server-side validation
and the ajax call to server-side services, which will be implemented
in subsequent more advanced tutorials.

## JavaScript

That said, let's take a look at `login.js` code:

```JavaScript
// Script 2.3 - login.js

// Function called when the form is submitted.
// Function validates the form data and returns a Boolean value.
function validateForm() {
    'use strict';

    // Get references to the form elements:
    var email = document.getElementById('email');
    var password = document.getElementById('password');

    // Validate!
    if ( (email.value.length > 0) && (password.value.length > 0) ) {
        return true;
    } else {
        alert('Please complete the form!');
        return false;
    }

} // End of validateForm() function.

// Function called when the window has been loaded.
// Function needs to add an event listener to the form.
function init() {
    'use strict';

    // Confirm that document.getElementById() can be used:
    if (document && document.getElementById) {
        var loginForm = document.getElementById('loginForm');
        loginForm.onsubmit = validateForm;
    }

} // End of init() function.

// Assign an event listener to the window's load event:
window.onload = init;
```

## Porting to ClojureScript

It has been not a short trip, but we can now start to port the login
form validation from JS to CLJS. We're going to start pedantic by
directly translating JS to CLJS using [CLJS interop][12] with the underlying
JavaScript Virtual Machine (JSVM).

The JS `validateForm()` function gets `email` and `password` ids from
form input and just verity that both of them have a
value. `validateForm()` function returns `true` if the validation
passes, `false` otherwise.

Now let's write some CLJS code. Create the file `login.cljs` in the
`src/cljs/modern_cljs` directory and write the following code:

```clojure
(ns modern-cljs.login)

;; define the function to be attached to form submission event
(defn validate-form []
  ;; get email and password element by their ids in the HTML form
  (let [email (.getElementById js/document "email")
        password (.getElementById js/document "password")]
    (if (and (> (count (.-value email)) 0)
             (> (count (.-value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))

;; define the function to attach validate-form to onsubmit event of
;;the form
(defn init []
  ;; verity that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; validate-form function
    (let [login-form (.getElementById js/document "loginForm")]
      (set! (.-onsubmit login-form) validate-form))))

;; initialize the HTML page in unobstrusive way
(set! (.-onload js/window) init)
```

As you can see, this pedantic code directly translated from JS code,
defines two functions: `validate-form` and `init`. 

> Note that in CLJ/CLJS the use of CamelCase to name things is not
> idiomatic. That's why we translated `validateForm` to
> `validate-form`.

The `let` form allows you to define a kind of local variables, like
`var` in the above JS code. As we said, we exstensively used the "."
and ".-" JS interop to call JS native functions
(e.g. `.getElementById`) and to get/set JS object properties
(i.e. `.-value`) or funcions we want as value (e.g. `.-getElementById`
and `.-onsubmit`), rather than execute. This is one of the
[differences between CLJS from CLJ][12] that dipends on the underlying
hosting virtual machine (i.e. JSVM versus JVM).

Copy `login.html` file from [ch02 of Modern JS Code][3] to
`resources/public` directory.

> If you're using an HTML5 browser, instruct the form to deactivate
> input validation by adding `novalidate` as last attribute of the
> form.

Finally, set the `src` script tag attribute value to
`js/modern.js`. Here is the final `login.html`

```html
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Login</title>
    <!--[if lt IE 9]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <link rel="stylesheet" href="css/styles.css">
</head>
<body>
    <!-- login.html -->
    <form action="login.php" method="post" id="loginForm" novalidate>
        <fieldset>
            <legend>Login</legend>

            <div>
              <label for="email">Email Address</label>
              <input type="email" name="email" id="email" required>
            </div>

            <div>
              <label for="password">Password</label>
              <input type="password" name="password" id="password" required>
            </div>

            <div>
              <label for="submit"></label>
              <input type="submit" value="Login &rarr;" id="submit">
            </div>

        </fieldset>
    </form>
    <script src="js/modern.js"></script>
</body>
</html>
```

We're almost done.  Copy `styless.css` file from
[ch02/css of Modern JS Code][3] to `resources/public/css` directory.

## CLJS Compilation

Now we can compile our `login.cljs` as usual (cfr. [Tutorial 1][11])

```bash
$ lein cljsbuild once
Could not find metadata thneed:thneed:1.0.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
Retrieving thneed/thneed/1.0.0-SNAPSHOT/maven-metadata.xml (1k)
    from https://clojars.org/repo/
Compiling ClojureScript.
Compiling "resources/public/js/modern.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern.js" in 5.075248 seconds.
$
```
If you want trigger CLJS automatic recompilation whenever you change
CLJS source code, just substutute the task `auto` to `once` like so:

```bash
$ lein cljsbuild auto
```

## Run

Open `login.html` in your browser to verify that everything went
well.

![Login Form][13]

Note that the browser shows both the login form and the "Hello,
ClojureScript!" text from the [first tutorial][11]. The reason of that
will be explained in a subsequent tutorial on [Google Closure
Compiler][17]. 

Now play with the form:

* if you click the login button before having valorized both email and
password fields you should see the alert window popping up and asking
you to complete the form;
* if you click the login button after having valorized both email and
  passoword fields you should see the usual browser error message
  saying that the page
  `/path/to/modern-cljs/resources/public/login.php` could not be
  found. That's because the action attribute of the html form
  still references `login.php` as the server-side validation
  script. The server-side validation will be implemented in CLJ in a
  subsequent more advanced tutorial.
  
![Please, complete the form][14]

![File not found][15]

## The fun part

In this last paragraph of the tutorial you can start to have some fun
with the brepl and the CLJ http-server we introduced in [tutorial 3][16]

0. launch the compile task in auto mode: `$ lein cljsbuild auto`
1. launch the brepl: `$ lein trampoline cljsbuild repl-listen`
2. launch the ring server: `$ lein ring server`
3. visit the login page: `http://localhost:3000/login.html`
4. evaluate `(in-ns 'modern-cljs.login)` in the brepl
5. evaluate `validate-form` in the brepl. You should see the JS function
generated by CLJS compiler with the support of Google Closure Compiler;
6. evaluate `(validate-form)`. You should see the Alert Window asking
you to complete the form.
7. go on interacting with the browser via the brepl.

If you fill both the email and password and click the login button (or
evalute `(validate-form)` in the brepl, you'll see the `Not found page`
we set up with compojure in [Tutorial 3][16]

# Tutorial 5 - Introducing Domina

TO BE DONE

# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[2]: http://www.larryullman.com/
[3]: http://www.larryullman.com/downloads/modern_javascript_scripts.zip
[4]: http://en.wikipedia.org/wiki/Sequence_diagram
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ch02-tree.png
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-form.png
[8]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-01-dia.png
[9]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-02-dia.png
[10]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-03-dia.png
[11]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[12]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
[13]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-cljs-01.png
[14]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/please-complete.png
[15]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/file-not-found.png
[16]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[17]: https://developers.google.com/closure/compiler/
