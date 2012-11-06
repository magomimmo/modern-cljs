# Tutorial 4 - Modern ClojureScript

In this tutorial we're going to start the porting of few JavaScript (JS)
samples from the book [Modern JavaScript: Development and Desing][1] by
[Larry Ullman][2]. You can download the code of the book from [here][3].

The reason I choose [it][3] as a reference is because it starts
smoothly, but keeps a roboust approach to JS coding. I think that
bringing [Larry][2] approach from JS into ClojureScript (CLJS) could be
helpful to anyone not yet fluent in CLJS.

## Introduction

As anybody knows, in the 1990s JS has been prevalently used for
improving and validating HTML forms. Then, in the second half of the
2000s, JS started to be used to make asynchronous requests to a server
side resource and a couple of words, Ajax and Web 2.0, got a lot of
glory in few years.

As I said, I'm going to follow the already cited [Modern JavaScript][1]
book to try to transpose [Larry Ullman][2] approach as a kind of Modern
ClojureScript.

So I'll start by migrating to CLJS it's first sample, a registration
form, because it's very instructive both in explaning the evolution of
the use of JS in the last decade, and in starting CLJS programming
without knowing to much about Clojure and/or ClojureScript themselves.

> I'm persuading myself that CLJS should be easy to use not only to us,
> as server-side guys, but to smart client-side guys too. Application
> logic is moving fast from server-side to client-side and all of us, as
> server-side guys, never loved to much that C dressed bad LISP running
> in the browser. We now have the opportunity to see the best LISP ever
> running in the browser and we should try to bring client programmers
> with us. Otherwise we risk to see the same C dressed bad LISP running
> on the server-side too, just because we were not able to show them our
> supposed Clojure(Script) superiority.

## Registration form

If you donwloaded [Modern JS][3] code samples, you'll can find
`login.html`, `css/styles.css` and `js/login.js` files in `ch02`
directory.

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
### Progressive enhancement

`login.php` script is associated to the `form action`. And `login.js` is
linked with the html page. Aside from `login.js` beeing linked inside
`login.html` there is no direct connection between the `form` and the
JS script. This choice has to do with the so called *progressive
enhancement strategy* that Larry Ullman clearly explains in his book.

The following [sequence diagrams][4] show `progressive enhancement` in
action:

#### Server-side only validation

![Login Form Seq DIA 1][7]

If the browser do not support JS, the form submitted by the user will be
valideted by the server-side `php script` named `login.php`. If the
validation check passes, the server will register the new user, `else`
the server will return the error to the user to allow him to try again.

Thanks to JS and Ajax, this user experience can be improved. A better
solution is to perform a client-side validation using JS, which bring us
to the second sequence diagram.

#### Client-side validation

![Login Form Seq DIA 2][8]

If the client-side (i.e. JS) validation passes, we still have to ask the
server-side validation for security reasons. But if the client-side
validation does not pass, we need not to make a round-trip with the
server and we can immediatly return the errors to the user.

But we still have some problems. The client-side validation cannot
verify if the username (i.e. email address) is already taken. Which
bring us to Ajax and to the third sequence diagram.

#### Ajax in action

![login Form Seq DIA 3][9]

The user experience has been now much more enhanced. The Ajax call
communicates with the server (e.g. to verify if the email address is
already taken) resulting in a more efficient and responsive process.


# License

Copyright © Mimmo Cosenza, 2012. Released under the Eclipse Public
License, the same as Clojure.

[1]: http://www.larryullman.com/books/modern-javascript-develop-and-design/
[2]: http://www.larryullman.com/
[3]: http://www.larryullman.com/downloads/modern_javascript_scripts.zip
[4]: http://en.wikipedia.org/wiki/Sequence_diagram
[5]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/ch02-tree.png
[6]: https://raw.github.com/magomimmo/modern-cljs/master/doc/images/login-form.png
