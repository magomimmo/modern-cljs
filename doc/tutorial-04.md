# Tutorial 4 - Modern ClojureScript

In this tutorial we're going to start by porting a few JavaScript (JS)
samples from the book [Modern JavaScript: Develop and Design][1] by
[Larry Ullman][2]. You can download the code from the book [here][3].

The reason I choose it as a reference is because it starts smoothly, but
keeps a robust approach to JS coding. I think that bringing Larry's
approach from JS into ClojureScript (CLJS) could be helpful to anyone
not yet fluent in CLJS.

## Introduction

As everybody knows, in the 1990s JS was primarily used for
improving and validating HTML forms. Then, in the second half of the
2000s, JS started to be used to make asynchronous requests to server-side
resources and within a few months we had two new buzzwords, Ajax
and Web 2.0.

As I said, I'm going to follow the already cited [Modern JavaScript][1]
book to try to translate [Larry Ullman's][2] approach into a kind of
Modern ClojureScript.

So let's start by migrating his first example to CLJS, a login form,
because it's very instructive both in explaining the evolution of the use
of JS in the latest decade, and in starting CLJS programming without
knowing much about Clojure and/or ClojureScript themselves.

## Preamble

If you want to start working from the end of the [previous tutorial][16],
assuming you've [git][20] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-03
git checkout -b tutorial-04-step-1
```

## Registration form

If you downloaded the [Modern JS][3] code samples, you'll find `login.html`,
`css/styles.css` and `js/login.js` files in the `ch02` directory.

![Modern ch02 tree][5]

If you open `login.html` with your browser you should see something like
this:

![Login Form][6]

Now, let's take a look at the HTML.

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
### Progressive enhancement and unobtrusive JS

Note that each element has both a `name` attribute and an `id`
attribute. The `name` value will be used when the form data is submitted
to the server-side. The `id` value will be used by JS/CSS.

`login.php` script is associated with the `form action`. And
`login.js` is linked within the html page. Aside from `login.js`,
there is no any other direct connection between the `form` and the JS
script. This choice has to do with the so called *progressive
enhancement* and *unobtrusive JS* approaches that Larry Ullman clearly
explains in his book.

The following [sequence diagrams][4] show these approaches in action.

#### Server-side only validation

![Login Form Seq DIA 1][8]

The form submitted by the user will be validated by the server-side PHP
script named `login.php`. If the validation check passes, the server
will log in the user, otherwise the server will return the errors to
the user for correction.

Thanks to JS and Ajax, this user experience can be improved a lot. A
better solution is to perform a client-side validation using JS, which
bring us to the second sequence diagram.

#### Client-side validation

![Login Form Seq DIA 2][9]

If the client-side (i.e., JS) validation passes, we still have to ask
the server-side validation for security reasons. But if the
client-side validation does not pass, we do not need to make a
round-trip to the server and we can immediately return the errors to
the user.

But we still have some problems. The client-side validation cannot
check if the username is registered. This bring us to Ajax and the
third sequence diagram.

#### Ajax in action

![login Form Seq DIA 3][10]

The user experience has now been much more enhanced. The Ajax call
communicates with the server (e.g., to verify if the email address
exists) resulting in a more efficient and responsive process.

In this tutorial, we are going to limit ourselves to the client-side
validation scenario without implementing the server-side validation or
the Ajax call to the server. We will implement those in subsequent,
more advanced tutorials.

## JavaScript

That said, let's take a look at `login.js`:

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

It has not been a short trip, but we can now start to port the login
form validation from JS to CLJS. We're going to start off by directly
translating JS to CLJS using [CLJS interop][21] with the underlying
JavaScript Virtual Machine (JSVM).

### Start the Immediate Feedback Environment

Considering the efforts of the CLJ/CLJS communities in bringing to us
all the needed pieces to build an Immediate Feedback Developing
Environemnt (IFDE), we have to commit ourselves in using it from the
very beginning.

Start the IFDE as usual:

```bash
boot dev
...
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

...
nREPL server started on port 50695 on host 127.0.0.1 - nrepl://127.0.0.1:50695
...
Compiling ClojureScript...
• js/main.js
Elapsed time: 22.924 sec
```

And visit the `http://localhost:3000` URL. Remember to open the
Developer Tool of your browser to see the correponding JS console.

### Copy the CSS file

Open a new terminal and unzip the `Modern JavaScript: Develop and
Design` zip file downloded from [here][3] and copy the `styles.css`
CSS file from the `ch02/css` subdirectory into the `html/css`
directory of the `modern-cljs` project.


```bash
cd /path/to/modern-cljs
unzip ~/Downloads/modern_javascript_scripts.zip -d ~/Downloads/
mkdir html/css
cp ~/Downloads/modern_javascript_scripts/ch02/css/styles.css html/css/
```

As soon as you copy the CSS file you should see the console of the
browser printing the `Reload` notification.

### Copy the HTML file 

Now copy the `login.html` from the `ch02` directory into the `html`
directory of the project.

```bash
cp ~/Downloads/modern_javascript_scripts/ch02/login.html html/
```

Again, as the file is copied, you'll see another `Reload` notification
printed at the console.

### Link the HTML file to the JS file

Now edit the copied `login.html` file and modify its `<script>` tag
for pointing to the `js/main.js` generated by the CLJS compiler.

```html
<!doctype html>
<html lang="en">
...
<body>
...
    <script src="js/main.js"></script>
</body>
</html>
```

> NOTE 1: If you're using an HTML5 browser, instruct the form to
> deactivate input validation by adding the `novalidate` attribute to
> the form. Pay attention to the `novalidate` spelling, otherwise the
> HTML5 browser will be free to intercept the fields' `required`
> attribute and check for them before JS is involved and you would not
> see the alert window be opened by CLJS.

The `reload` task is triggered again. Visit the
`http://localhost:3000/login.html` URL. the browser will show the
`login` form abd the console will print the `Hello, World!` string.

### Sperimenting with the bREPL

It's now time to play a little bit with the bREPL. Open a new terminal
to launch the nrepl client and then the bREPL as we already did more
times.

```clj
boot repl -c
REPL-y 0.3.7, nREPL 0.2.11
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
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:51743 >>
<< waiting for client to connect ... Connection is ws://localhost:51743
Writing boot_cljs_repl.cljs...
```

After a while you'll see the `connected!` notification at the terminal
and the `Opened Websockect REPL connection` notification in the console
of the browser.

```bash
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

Let's see if we're able to inspect the DOM form the bREPL. Aside from
layout elements, the `login.html` page has 4 elements identified by
their `id` attribute:

* `loginForm`
* `email`
* `password`
* `submit`

By using the CLJS way to interoperate with the JS Virtual Machine
(JSVM), we can evalute the following expressions:

```clj
cljs.user=> (.getElementById js/document "loginForm")
#object[HTMLFormElement [object HTMLFormElement]]
cljs.user=> (.getElementById js/document "email")
#object[HTMLInputElement [object HTMLInputElement]]
cljs.user=> (.getElementById js/document "password")
#object[HTMLInputElement [object HTMLInputElement]]
cljs.user=> (.getElementById js/document "submit")
#object[HTMLInputElement [object HTMLInputElement]]
```

Now fill the `email` and the `password` fields and evalute the
following expressions at the bREPL:

```clj
cljs.user=> (.-value (.getElementById js/document "email"))
"yourname@yourdomain.com"
cljs.user=> (.-value (.getElementById js/document "password"))
"weakpassword"
```

So far so good.

### Validate the login form

As we previously saw, the JS `validateForm()` function gets `email`
and `password` ids from form input and verifies that both have a
value. The `validateForm()` function returns `true` if the validation
passes, `false` otherwise.

Let's create the file `login.cljs` in the `src/cljs/modern_cljs`
directory and write the following code:

```clojure
(ns modern-cljs.login)

;; define the function to be attached to form submission event
(defn validate-form []
  ;; get email and password element from their ids in the HTML form
  (let [email (.getElementById js/document "email")
        password (.getElementById js/document "password")]
    (if (and (> (count (.-value email)) 0)
             (> (count (.-value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))

;; define the function to attach validate-form to onsubmit event of
;; the form
(defn init []
  ;; verify that js/document exists and that it has a getElementById
  ;; property
  (if (and js/document
           (.-getElementById js/document))
    ;; get loginForm by element id and set its onsubmit property to
    ;; our validate-form function
    (let [login-form (.getElementById js/document "loginForm")]
      (set! (.-onsubmit login-form) validate-form))))

;; initialize the HTML page in unobtrusive way
(set! (.-onload js/window) init)
```

As you can see, this ported code defines two functions: `validate-form`
and `init`.

> NOTE 2: Note that in CLJ/CLJS the use of CamelCase to name things is not
> idiomatic. That's why we translated `validateForm` to `validate-form`.

The `let` form allows you to define a kind of local variable, like
`var` in the JS code above. As noted, we extensively used the "."
and ".-" JS interop to call JS native functions
(e.g., `.getElementById`) and to get/set JS object properties
(e.g., `.-value`) or functions we want as values (e.g., `.-getElementById`
and `.-onsubmit`), rather than execute. This is one of the
[differences between CLJS and CLJ][12] that depends on the underlying
host virtual machine (i.e., JSVM versus JVM).

## CLJS Compilation

Now we can compile our `login.cljs` as usual (cfr. [Tutorial 1][11])

```bash
lein cljsbuild once
Could not find metadata thneed:thneed:1.0.0-SNAPSHOT/maven-metadata.xml in central (http://repo1.maven.org/maven2)
Retrieving thneed/thneed/1.0.0-SNAPSHOT/maven-metadata.xml (1k)
    from https://clojars.org/repo/
Compiling ClojureScript.
Compiling "resources/public/js/modern.js" from "src/cljs"...
Successfully compiled "resources/public/js/modern.js" in 5.075248 seconds.
```
If you want to trigger automatic JS recompilation whenever you change
CLJS source code, just replace the command `once` with `auto` like so:

```bash
lein cljsbuild auto
```

## Run

Open `login.html` in your browser to verify that everything went
well.

![Login Form][13]

Note that the browser shows both the login form and the "Hello,
ClojureScript!" text from the [first tutorial][11]. The reason for that
will be explained in a [subsequent tutorial][22] on the [Google Closure
Compiler][17].

Now let's play with the form:

* if you click the login button before having filled both email and
password fields you should see the alert window popping up and asking
you to complete the form;
* if you click the login button after having filled both email and
  password fields you should see the usual browser error message
  saying that the page
  `/path/to/modern-cljs/resources/public/login.php` could not be
  found. That's because the action attribute of the html form
  still references `login.php` as the server-side validation
  script. The server-side validation will be implemented in CLJ in a
  [subsequent tutorial][23].

![Please, complete the form][14]

![File not found][15]

## The fun part

In this last paragraph of the tutorial you can start to have some fun
with the brepl and the CLJ http-server we introduced in [tutorial 3][16]

0. launch the compile task in auto mode: `lein cljsbuild auto`
1. launch the brepl from a new terminal window: `lein trampoline
   cljsbuild repl-listen`
2. launch the ring server from a new terminal window: `lein ring
   server-headless`
3. visit the login page: `http://localhost:3000/login.html`
4. evaluate `(require '[modern-cljs.login :as m])` in the brepl
5. evaluate `m/validate-form` in the brepl. You should see the JS function
   generated by CLJS compiler with the support of Google Closure Compiler;
6. evaluate `(m/validate-form)`. You should see the alert window asking
   you to complete the form.
7. go on interacting with the browser via the brepl.

If you fill both the email and password and click the login button,
you'll see the `Not found page` we set up with compojure in
[Tutorial 3][16]

> NOTE 4: I strongly suggest to install the readline wrapper and then
> interact with the brepl by running the `rlwrap lein trampoline
> cljsbuil repl-listen` command.

If you created a new git branch as suggested in the preamble of this
tutorial, I suggest you to commit the changes as follows

```bash
git commit -am "modern javascript"
```

# Next Step [Tutorial 5: Introducing Domina][18]

In the [next tutorial][18] we're going to use the [domina library][19] to
make our login form validation more Clojure-ish.

# License

Copyright © Mimmo Cosenza, 2012-14. Released under the Eclipse Public
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
[18]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md
[19]: https://github.com/levand/domina
[20]: https://help.github.com/articles/set-up-git
[21]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#host-interop
[22]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-07.md
[23]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-12.md
