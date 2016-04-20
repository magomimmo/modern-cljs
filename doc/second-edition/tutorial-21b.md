# Tutorial 21 Reagent: a minimalist interface to React.js

In the [previous tutorial][1] we enhanced the `build.boot` building
file to be able to install a CLJ/CLJS library to the local `maven`
repository and finally publish it to
[`clojars`](https://clojars.org/).

As it has been said in previous tutorials aimed at creating a kind of
confidence with the ClojureStript programming language, by adopting
the [domina](https://github.com/levand/domina) library for DOM
manipulation, we have been almost prehistoric.

In this tutorial of the series we're going to introduce
[Reagent](http://reagent-project.github.io/), a very well known
minimalist ClojureScript interface to
[React.js](https://facebook.github.io/react/index.html) which is a
JavaScript library for building user interfaces that got a lot of
attention all over the places.

## Preamble

To start working, assuming you've git installed, do as follows:

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-18
```

## Introduction

Even if the most discussed facet of
[React.js](https://facebook.github.io/react/index.html) is the idea of
adopting a
[Virtual DOM](http://facebook.github.io/react/docs/working-with-the-browser.html#the-virtual-dom)
(VDOM) maintaining a fast and immutable in-memory representation of
the DOM, my personal opinion is that the VDOM and its differing
algorithm only represent implementation details. To me, the most
fundamental aspects of React.js are:

* [one-way data flow, AKA one-way binding](https://youtu.be/nYkdrAPrdcw?list=PLb0IAmt7-GS188xDYE-u1ShQmFFGbrk0v);
* [composability model](https://facebook.github.io/react/docs/multiple-components.html);
* [synthetic event system](http://facebook.github.io/react/docs/interactivity-and-dynamic-uis.html#event-handling-and-synthetic-events);

Even if I appreciate a lot React.js (and React Native too) I still
think that ClojureScript is a superior programming language when
compared with JS and with
[JSX](http://facebook.github.io/react/docs/jsx-in-depth.html) as well.

To explain my opinion on React.JS and, at the same time, to introduce
you to [Reagent](http://reagent-project.github.io/), it can be very
useful to start this tutorial by porting to Reagent the
[official React.js introductory tutorial](https://facebook.github.io/react/docs/tutorial.html).

## Install React Tutorial

The best way to appreciate Reagent is to use as a reference the
official React.js tutorial by downloading and locally running it on
your computer.

### Install node.js

First you need to install [node.js](https://nodejs.org/en/). I
personally prefer to be able to install it by using
[nvm](https://github.com/creationix/nvm), a simple script to install
and manage different node versions.

The easiest way to install `nvm` is by using its script as follow:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash
```

> NOTE 1: On OSX, if you get `nvm: command not found` after running
> the install script, your system may not have a `.bash_profile` file
> where the command is set up. Simple create one with `touch
> ~/.bash_profile` and run the install script again.

You can then install `node.js` by issuing the following command at the
terminal:

```bash
nvm install 5.0
```

You can now verify that both `node.js` and `npm` have been installed:

```bash
node -v
v5.10.0
```

```bash
npm -v
3.8.3
```

### Get and run the React Tutorial

Now that you have `node` and `npm` installed on your computer you can
clone and run the React Tutorial as follows:

```bash
git clone https://github.com/reactjs/react-tutorial.git
Cloning into 'react-tutorial'...
remote: Counting objects: 496, done.
remote: Total 496 (delta 0), reused 0 (delta 0), pack-reused 496
Receiving objects: 100% (496/496), 98.83 KiB | 0 bytes/s, done.
Resolving deltas: 100% (246/246), done.
Checking connectivity... done.
```

```bash
cd react-tutorial
npm install
react-tutorial@0.0.0 /Users/mimmo/tmp/react-tutorial
├─┬ body-parser@1.15.0
...
  └── vary@1.0.1

npm WARN react-tutorial@0.0.0 No license field.
```

```bash
PORT=3001 node server.js
Server started: http://localhost:3001/
```

> NOTE 2: I set the port of the node http server to `3001` to not
> collide the [express server](http://expressjs.com/) with the default
> `3000` port number of the clojure web server we're going to later
> launch with the `boot dev` command from within the `modern-cljs`
> project folder.

### Play with the React web application

Now visit the [localhost:3001](http://localhost:3001/) URL in your
browser and you should receive something like this:

![React Tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-01.png)

As you see this is a very basic web application, but it contains very
important concepts to be grasped for better understanding `React.js`
and, consequently, how `Reagent` eventually is eventually any better
to be used. Note that the page is composed of the following elements:

1. An `<h1>` `Comments` element;
1. A list (i.e. `<div>`) of `Comments`;
1. A `<form>` element to submit new `Comment`;

Each comment is composed of:

1. An `<h2>` element for the author of the `Comment`
1. A `<span>` element for the `Comment` itself. 

Now post a couple of new comments by using the web form. You'll note
the list of the comments to be updated without refreshing the full
page.

![React Tutorial new comments](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-02.png)

Nothing new under the sun. What is new, aside from the performance
that we can't appreciate with such a simple sample, it is well hidden
under the wood.

> NOTE 3: In the above image you'll note that the first added comment
> contains a link and the second one contains a word in bold. This is
> because the `<input>` element for the comment is even able to parse
> a `markdown` text.

## Porting to Reagent. Step 1: your first React Component

As said, my opinion is that to better appreciate `Reagent`, we should
known a little bit of `React.js` as well. So, let's get started by
following the official tutorial of `React.js` to progressively port it
to `Reagent`.

First rename the original `public/scripts/example.js` file to
`public/scripts/complete.js` and create a new
`public/scripts/example.js` file:

```bash
mv public/scripts/example.js public/scripts/old.js
```

```bash
touch public/scripts/example.js
```

Now open the `example.js` source file with your preferred editor and
copy and paste into it the code from the beginning of the official
react tutorial:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        Hello, world! I am a CommentBox.
      </div>
    );
  }
});

ReactDOM.render(
  <CommentBox />,
  document.getElementById('content')
);
```

Reload the [localhost:3001](http://localhost:3001/) page. You should
see your very first and simple React Component saying "Hello, world! I
am a CommentBox.

The above lines mix into JS code a kind of HTML code, JSX code in
React parlance. It first define a new class, named `CommentBox`, which
is an UI Component, containing one method only: `render()`. This
method uses JSX syntax to declare the structure of the component
itself which, in this very simple case, is just a `div` tag element
(it's a React component, not an HTML element) and the text `Hello,
world! I am a CommentBox`.

Then, this newly created UI `ComponentBox` is instantiated by the
`ReactDOM.render()` method, which also attaches it to the `content`
element `id` from the HTML page.

Let's take a look at the `index.html` file living in the `public`
directory of the `react-tutorial` project folder.

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>React Tutorial</title>
    <!-- Not present in the tutorial. Just for basic styling. -->
    <link rel="stylesheet" href="css/base.css" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.0.1/react.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/react/15.0.1/react-dom.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-core/5.6.16/browser.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.2/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/marked/0.3.5/marked.min.js"></script>
  </head>
  <body>
    <div id="content"></div>
    <script type="text/babel" src="scripts/example.js"></script>
    <script type="text/babel">
      // To get started with this tutorial running your own code, simply remove
      // the script tag loading scripts/example.js and start writing code here.
    </script>
  </body>
</html>
```

For the moment there are only two things to be noted here:

1. the `<div id="content"></div>`, which represent the `root` HTML
   element to which the `CommentBox` instance is to be attached;
1. the `<script type="text/babel" src="scripts/example.js"></script>`
   `script` tag loading the `example.js` file we just coded.

The JSX code contained in the `example.js` file can't be interpreter
as is by all browsers on the market, because it uses `ECMAScript 6`,
AKA ECMAScript 2015 or as ES6 as well. But we don't care about
it. Just note that it needs to be passed through the
[`babel transpiler`](https://babeljs.io/) (i.e. a source to source
compiler) to be transpiled as ES5.

## Porting to Reagent. Step 2: prepare the field

Ad said, Reagent is a ClojureScript minimalist interface to React,
which means that it uses React internally. We want to understand its
mechanics by first interact with it via the powerful CLJS bREPL as we
learned in previous tutorials.

Before starting the IFDE (Immediate Feedback Development Environment),
we first want add the latest available Reagent library to the
`build.boot` of the `modern-cljs` project.

```clojure
(set-env!
 ...
 :dependencies '[
                 ...
                 [reagent "0.6.0-alpha"]
                 ])
...
```

Now create the `reagent.html` file in the `html` directory of the `modern-cljs`:

```bash
cd /path/to/modern-cljs
touch html/reagent.html
```

and copy into it the following very simple `html` code which resemble
both the `index.html` file from the react tutorial and the
`index.html` file from the `modern-cljs` project.


```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>Reagent Tutorial</title>
    <link rel="stylesheet" href="css/base.css" />
  </head>
  <body>
    <div id="content"></div>
    <script src="main.js"></script>
  </body>
</html>
```

As you see, we linked the `main.js` file that will be created by the
CLJS compilation. 

There are only two remarkable differences from the `index.html` and
`shopping.html` files from the previous tutorials:

1. in the `body` we now have a single `div` tag only. This is very
   typical of any Single Page Application (SPA) and it resembles the
   `index.html` file from the react tutorial;
1. we have not added the call of the `init()` function to be exported
   from the `modern-cljs.reagent` CLJS namespace, as we already did in
   the previous tutorials with the `modern-cljs.login` and
   `modern-cljs.shopping` namespaces. This is because in the next few
   steps we're going to work at the bREPL and we'll add that call
   later, when we'll persist our experiment into the `reagent.cljs`
   source file.

Also copy the `base.css` file from the React Tutorial to the
`html/css` directory of the `modern-cljs` project to obtain the same
basic page style.

```bash
cp /path/to/react-tutorial/public/css/base.css html/css/
```

We are almost done. Before launching the IFDE with the `boot dev`
command, create the `reagent.cljs` file and require the `reagent.core`
namespace into it.

```bash
touch src/cljs/modern_cljs/reagent.cljs
```

```clj
(ns modern-cljs.reagent
  (:require [reagent.core :as r]))
```

> NOTE 4: As we saw in previous tutorials, before to be able to use a
> new namespace from a new library in the bREPL, we first need to
> require it in a CLJS file, otherwise the bREPL is not able to access
> it.

## Porting to Reagent. Step 3: your first Reagent component

Launch the IFDE as usual:

```bash
boot dev
Starting reload server on ws://localhost:54336
...
Writing main.cljs.edn...
Compiling ClojureScript...
• main.js
Writing target dir(s)...
Elapsed time: 32.980 sec
```

> NOTE 5: you could launch the TDD environment as well with the `boot
> tdd` command. But at the moment we're not interested in executing any
> test. We only want to learn about Reagent by interact with it through
> the bREPL.

So far, so good.

Now open a new terminal and launch the `boot` client and the `bREPL`
on top of it as usual.

```bash
boot repl -c
REPL-y 0.3.7, nREPL 0.2.12
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
boot.user=>
```

```clj
boot.user=> (start-repl)
<< started Weasel server on ws://127.0.0.1:54355 >>
<< waiting for client to connect ... Connection is ws://localhost:54355
Writing boot_cljs_repl.cljs...
 connected! >>
To quit, type: :cljs/quit
nil
cljs.user=>
```

> NOTE 6: as usual with IDFE, as soon as you visit the
> [localhost:3000/reagent.html](http://localhost:3000/reagent.html)
> URL the bREPL connects with the JS engine of the browser and it is
> ready to evaluate CLJS expressions.

Now require the `reagent.core` namespace at the bREPL and create your
very first Reagent Component, which, under the wood, is a React
Component.

```clj
cljs.user> (require '[reagent.core :as r])
nil
```

```clj
cljs.user> (defn component-box []
             [:div "Hello, world! I am a component-box"])
#'cljs.user/component-box
```

Believe it or not, such a simple and pure function it's enough to
create a Reagent Component corresponding to the `ComponentBox`
component we previously created with the following `JSX` code:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        Hello, world! I am a CommentBox.
      </div>
    );
  }
});
```

Don't you believe it. Evaluate the following form at the bREPL:

```clj
cljs.user> (r/render [component-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

Do you see the `Hello, world! I'm a component-box` text in the page?
The above `render` call correspond to the

```js
ReactDOM.render(
  <CommentBox />,
  document.getElementById('content')
);
```

> NOTE 7: as you see, being clojurean, we used `kebab-case` names
> (i.e. `component-box`) instead of `CamelCase` names
> (i.e. `ComponentBox`).

Does the `[:div "some text]` form remembers you something? It is the
same
`[hiccup syntax](https://github.com/weavejester/hiccup/wiki/Syntax)
we're already accustomed with from the tutorials involving
[hiccups](https://github.com/teropa/hiccups) and
[Enlive](https://github.com/cgrand/enlive) libraries.

Evaluate the newly defined function at the bREPL

```clj
cljs.user> (comment-box)
[:div "Hello, world! I'm a comment-box"]
```

No surprises here. It returns exactly what you expect. The very first magics of Reagent is encapsulated inside the `render` function.

```clj
cljs.user> (doc r/render)
-------------------------
reagent.core/render
([comp container] [comp container callback])
  Render a Reagent component into the DOM. The first argument may be 
either a vector (using Reagent's Hiccup syntax), or a React element. The second argument should be a DOM node.

Optionally takes a callback that is called when the component is in place.

Returns the mounted component instance.
nil
```

Let's move on.

## Porting to Reagent. Step 4: Composing Component

The next step in the React Tutorial is to define two new skeleton
`Component`. The first, named `CommentList`, will host comments and
the second, named `CommentForm`, will be used to create new comments.

Here is the corresponding code you have to add to the `example.js` JSX
file.

```js
var CommentList = React.createClass({
  render: function() {
    return (
      <div className="commentList">
        Hello, world! I am a CommentList.
      </div>
    );
  }
});

var CommentForm = React.createClass({
  render: function() {
    return (
      <div className="commentForm">
        Hello, world! I am a CommentForm.
      </div>
    );
  }
});
```

We can now compose these two newly define component inside the
`CommentBox` one, which has to be redefined as follows:

```js
var CommentBox = React.createClass({
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList />
        <CommentForm />
      </div>
    );
  }
});
```

Pretty logical. The `div` Component now includes an `h1` header, the
`CommentList` and the `CommentForm` sub components.

If you now reload the [local:3001](http://localhost:3001/) page you
should see the following content:

![Composing Component](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tutorial-03.png)

Let's do the same thing with Reagent:

```clj
cljs.user> (defn comment-list []
             [:div "Hello, world! I'm a comment-list"])
#'cljs.user/comment-list
```

```clj
cljs.user> (defn comment-form []
             [:div "Hello, world! I'm a comment-form"])
```

We can now redefine the `comment-box` Component by incorporating into
it the `h1`, `comment-list` and `comment-form` Components as we did in
React


```clj
cljs.user> (defn comment-box []
             [:div
              [:h1 "Comments"]
              [comment-list]
              [comment-form]])
#'cljs.user/comment-box
```

Let's re-render the *root component* `comment-box` of our newly created component hierarchy

```clj
cljs.user> (r/render [comment-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

The `reagent.html` page is immediately updated and you should see the following content

![Reagent Composing Components](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-tut-01.png)

So far, so good. We replicated in Reagent the same React composition
with much less incidental (i.e. plumbing) code. Anytime I hear someone
saying that ClojureScript, be being a LISP dialect, has to many
parentheses, I immediately switch my attention to a more educated
developer. They just don't know what they are talking about.

Before going on with the next step, let's evaluate the `(comment-box)`
function again at the bREPL

```clj
cljs.user> (comment-box)
[:div [:h1 "Comments"] [#object[cljs$user$comment_list "function cljs$user$comment_list(){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),"Hello, world! I'm a comment-list"], null);
}"]] [#object[cljs$user$comment_form "function cljs$user$comment_form(){
return new cljs.core.PersistentVector(null, 2, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"div","div",1057191632),"Hello, world! I'm a comment-form"], null);
}"]]]
```

OK, the output should have been pretty printed to be more readable,
but the concept is still evident. This is just a standard application
of the Clojure(Script) evaluation rules: each item in a vector is
evaluated. When an item is a symbol, it evaluates to the value of the
symbol. If just happens that `comment-list` and `comment-box` are
functions, so they evaluate to the corresponding functions objects.

I strongly suggest to take your time read
[this pretty nice wiki](https://github.com/Day8/re-frame/wiki/Using-%5B%5D-instead-of-%28%29)
on Reagent, because it explains why you should never use the round
parentheses when composing components, even if you eventually could.

```clj
;; don't do this
cljs.user> (defn comment-box []
             [:div
              [:h1 "Comments"]
              (comment-list)
              (comment-form)])
#'cljs.user/comment-box
```

```clj
cljs.user> (comment-box)
[:div [:h1 "Comments"] [:div "Hello, world! I'm a comment-list"] [:div "Hello, world! I'm a comment-form"]]
```

```clj
;; still working, but much less efficient in articulated UI scenarios, because
;; comment-list and comment-form are transformed as React Component

cljs.user> (r/render [comment-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
