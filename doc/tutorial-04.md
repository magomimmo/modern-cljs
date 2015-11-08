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

If you want to start working from the end of the [previous tutorial][4],
assuming you've [git][5] installed, do as follows.

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-03
git checkout -b tutorial-04-step-1
```

## Registration form

If you downloaded the [Modern JS][3] code samples, you'll find `login.html`,
`css/styles.css` and `js/login.js` files in the `ch02` directory.

![Modern ch02 tree][6]

If you open `login.html` with your browser you should see something like
this:

![Login Form][7]

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

The following [sequence diagrams][8] show these approaches in action.

#### Server-side only validation

![Login Form Seq DIA 1][9]

The form submitted by the user will be validated by the server-side PHP
script named `login.php`. If the validation check passes, the server
will log in the user, otherwise the server will return the errors to
the user for correction.

Thanks to JS and Ajax, this user experience can be improved a lot. A
better solution is to perform a client-side validation using JS, which
bring us to the second sequence diagram.

#### Client-side validation

![Login Form Seq DIA 2][10]

If the client-side (i.e., JS) validation passes, we still have to ask
the server-side validation for security reasons. But if the
client-side validation does not pass, we do not need to make a
round-trip to the server and we can immediately return the errors to
the user.

But we still have some problems. The client-side validation cannot
check if the username is registered. This bring us to Ajax and the
third sequence diagram.

#### Ajax in action

![login Form Seq DIA 3][11]

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
translating JS to CLJS using [CLJS interop][13] with the underlying
JavaScript Virtual Machine (JSVM).

The JS `validateForm()` function gets `email` and `password` ids from
form input and verifies that both have a value. The `validateForm()`
function returns `true` if the validation passes, `false` otherwise.

Now let's write some CLJS code. Create the file `login.cljs` in the
`src/cljs/modern_cljs` directory and write the following code:


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

> NOTE 1: Note that in CLJ/CLJS the use of CamelCase to name things is
> not idiomatic. That's why we translated `validateForm` to
> `validate-form`.

The `let` form allows you to define a kind of local variable, like
`var` in the JS code above. We extensively used the "."  and ".-" JS
interop to call JS native functions (e.g., `.getElementById`) and to
get/set JS object properties (e.g., `.-value`) or functions we want as
values (e.g., `.-getElementById` and `.-onsubmit`), rather than
execute. This is one of the [differences between CLJS and CLJ][14]
that depends on the underlying host virtual machine (i.e., JSVM versus
JVM).

## Launch the IFDE envirnoment

Before launching our IFDE environment there is still a very small
changes to be done: we have to instruct the CLJS compiler about the
newly created `modern-cljs.login` namespace.

Open the `html/js/main.cljs.edn` file and add the `modern-cljs.login`
namespace to the `:require` section:

```clj
{:require [modern-cljs.core modern-cljs.login]
 :compiler-options {:asset-path "js/main.out"}}
```

We can now launch the IFDE enviroment as usual:

```bash
boot dev
Starting reload server on ws://localhost:62490
Writing boot_reload.cljs...
Writing boot_cljs_repl.cljs...
2015-11-08 09:49:35.871:INFO::clojure-agent-send-off-pool-0: Logging initialized @9124ms
2015-11-08 09:49:35.928:INFO:oejs.Server:clojure-agent-send-off-pool-0: jetty-9.2.10.v20150310
2015-11-08 09:49:35.949:INFO:oejs.ServerConnector:clojure-agent-send-off-pool-0: Started ServerConnector@31c5f202{HTTP/1.1}{0.0.0.0:3000}
2015-11-08 09:49:35.950:INFO:oejs.Server:clojure-agent-send-off-pool-0: Started @9203ms
Started Jetty on http://localhost:3000

Starting file watcher (CTRL-C to quit)...

Adding :require adzerk.boot-reload to main.cljs.edn...
nREPL server started on port 62491 on host 127.0.0.1 - nrepl://127.0.0.1:62491
Adding :require adzerk.boot-cljs-repl to main.cljs.edn...
Compiling ClojureScript...
• js/main.js
Elapsed time: 17.379 sec
```

## Check the form validation

Visit the `http://localhost:3000/login.html` URL and play with it:

* if you click the `Login` button before having filled both `email`
and `password` fields you should see the alert window popping up and
asking you to complete the form;
* if you click the login button after having filled both `email` and
`password` fields you should see the usual browser error message
saying that the page `localhost:3000/login.php` not found. That's
because the action attribute of the html form still references
`login.php` as the server-side validation script. The server-side
validation will be implemented in CLJ in a subsequent tutorial.

## The fun part

In this last paragraph of the tutorial you can start to have some fun
with the bREPL:

Visit the `http://localhost:3000/login.html` again and then launch the
bREPL:

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
<< started Weasel server on ws://127.0.0.1:62661 >>
<< waiting for client to connect ... Connection is ws://localhost:62661
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
```

Now require the `modern-cljs.login` namespace and then start
interacting with the bREPL:

```clj
cljs.user=> (require '[modern-cljs.login :as l])
ni
cljs.user=> l/validate-form
#object[modern_cljs$login$validate_form "function modern_cljs$login$validate_form(){
var email = document.getElementById("email");
var password = document.getElementById("password");
if(((cljs.core.count.call(null,email.value) > (0))) && ((cljs.core.count.call(null,password.value) > (0)))){
return true;
} else {
alert("Please, complete the form!");

return false;
}
}"]
cljs.user=> l/init
#object[modern_cljs$login$init "function modern_cljs$login$init(){
if(cljs.core.truth_((function (){var and__4974__auto__ = document;
if(cljs.core.truth_(and__4974__auto__)){
return document.getElementById;
} else {
return and__4974__auto__;
}
})())){
var login_form = document.getElementById("loginForm");
return login_form.onsubmit = modern_cljs.login.validate_form;
} else {
return null;
}
}"]
```

As you see, by evaluating the symbols of the `validate-form` and
`init` functions the bREPL returns the JS functions generated by the
CLJS compiler.

Now select few DOM elements:

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

If you call `validate-form` function from the bREPL before having
filled the `email` or the `password` field, the browser will show the
alert dialog. As soon as you dismiss the dialog, the `validate-form`
function will return `false` at the bREPL.

```clj
cljs.user=> (l/validate-form)
false
```

Now fill the `email` and `password` fields, access their values and
call the `validate-form` again:

```clj
cljs.user=> (.-value (.getElementById js/document "email"))
"name@domain.com"
cljs.user=> (.-value (.getElementById js/document "password"))
"weakpasswd"
cljs.user=> (l/validate-form)
true
```

As you see, this time the `validate-form` returns `true` because the
`email` and `password` fields passed the validation tests.

If you created a new git branch, as suggested in the preamble of the
tutorial, quit everything and commit the changes.

```bash
git commit -am "modern javascript"
```

# Next Step Tutorial 5: TBD

# License

Copyright © Mimmo Cosenza, 2012-15. Released under the Eclipse Public
License, the same as Clojure.

[1]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[2]: http://www.larryullman.com/
[3]: http://www.larryullman.com/downloads/modern_javascript_scripts.zip
[4]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-03.md
[5]: https://help.github.com/articles/set-up-git
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ch02-tree.png
[7]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-form.png
[8]: http://en.wikipedia.org/wiki/Sequence_diagram
[9]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-01-dia.png
[10]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-02-dia.png
[11]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-03-dia.png
[12]: https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-01.md
[13]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#host-interop
[14]: https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure
