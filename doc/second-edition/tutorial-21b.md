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
[React.js](https://facebook.github.io/react/index.html), a JavaScript
library for building user interfaces that recently got a lot of
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

Even if I appreciate a lot React (and React Native too) I still think
that ClojureScript is a superior programming language when compared
with JS and with
[JSX](http://facebook.github.io/react/docs/jsx-in-depth.html) as well.

To explain my opinion on React and, at the same time, to introduce you
to [Reagent](http://reagent-project.github.io/), it can be very useful
to start this tutorial by porting to Reagent the
[official React.js introductory tutorial](https://facebook.github.io/react/docs/tutorial.html).

## Install React Tutorial

To get an idea of the final web application created by the React
Tutorial you need [Node.js](https://nodejs.org/en/) to be installed on
your computer.

### Install Node Version Manager (NVM)

I personally prefer to be able to install `node` by using
[nvm](https://github.com/creationix/nvm), a simple script to install
and manage different node versions.

Open your terminal and do the following:

```bash
curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.0/install.sh | bash
```

> NOTE 1: On OSX, if you get `nvm: command not found` after running
> the install script, your system may not have a `.bash_profile` file
> where the command is set up. Simple create one with `touch
> ~/.bash_profile` and run the install script again.

You can then install `node` by issuing the following command at the
terminal:

```bash
nvm install 5.0
```

Verify that both `node.js` and `npm` have been installed:

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

As you see, this is a very basic web application, but it contains
important concepts to be grasped for better understanding `React` and
how `Reagent` interface it.

Post a couple of new comments by using the web form. You'll note
the list of the comments to be updated without refreshing the full
page.

![React Tutorial new comments](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-02.png)

Nothing new under the sun. What is new, aside from the performance
that we can't appreciate with such a simple sample, it is well hidden
under the wood.

> NOTE 3: In the above image you'll note that the first newly added
> comment contains a link and the second one contains a word in
> bold. This is because the `<input>` element for the comment is even
> able to parse `markdown` text.

## Porting to Reagent. Step 1: create your first React Component

Now that you have an idea about the final behavior of the web
application, let's get started by following the official tutorial of
`React` to progressively port it to `Reagent`.

Stop the running web server and issue the following commands at the
terminal:

```bash
git reset --hard
HEAD is now at 2be1a2d Use 15.0.1
```

```bash
git checkout -b tutorial
Switched to a new branch 'tutorial'
```

```bash
rm public/scripts/example.js
touch public/scripts/example.js
```

Open the `example.js` source file with your preferred editor and copy
and paste into it the code from the beginning of the official react
tutorial:

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

Rerun the tutorial web app:

```bash
PORT=3001 node server.js
Server started: http://localhost:3001/
```

Reload the [localhost:3001](http://localhost:3001/) page. You should
see your very first and simple React Component saying `Hello, world! I
am a CommentBox.`

The above lines mix into JS code a kind of HTML code, JSX code in
React parlance. It first defines a new class, named `CommentBox`,
which is an UI Component containing one method only: `render()`. This
method uses JSX syntax to declare the structure of the component
itself. In this very simple case, it is just a `div` component (i.e. a
React component, not an HTML element) and the text `Hello, world! I am
a CommentBox`.

This newly created UI `ComponentBox` component is then instantiated by
the `ReactDOM.render()` method, which also attaches it to the
`content` element `id` from the HTML page.

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
   element to which the `CommentBox` component instance is to be
   attached;
1. the `<script type="text/babel" src="scripts/example.js"></script>`
   `script` tag loading the `example.js` file we just coded.

The code contained in the `example.js` file can't be interpreter as is
by all browsers on the market, because it uses `ECMAScript 6`, AKA
ECMAScript 2015 and ES6. But we don't care about it. Just note that it
needs to be passed through the
[`babel transpiler`](https://babeljs.io/) (i.e. a source to source
compiler) to be transpiled as ES5, which is supported by almost any
browser out there.

## Porting to Reagent. Step 2: prepare the field

Ad said, Reagent is a ClojureScript minimalist interface to React. To
understand its mechanics let's get started by interact with it via the
powerful CLJS bREPL.

Before starting the IFDE (Immediate Feedback Development Environment),
we first want to add the latest available Reagent library to the
`build.boot` of the `modern-cljs` project.

```clj
(set-env!
 ...
 :dependencies '[
                 ...
                 [reagent "0.6.0-alpha"]
                 [cljsjs/marked "0.3.5-0"]
                 ])
...
```

Note that we also added the `cljsjs/marked "0.3.5-0"` JS external
library. This is the same JS library used in the React Tutorial,
opportunely packaged to be used in a CLJS project. We'll see its use
later.

Now create the `reagent.html` file in the `html` directory of the `modern-cljs`

```bash
cd /path/to/modern-cljs
touch html/reagent.html
```

and copy into it the following very simple `html` code:

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
CLJS compilation as soon as we'll issue the `boot dev` command to
launch the IFDE:

There are only two remarkable differences from the `index.html` and
`shopping.html` files we coded in the previous tutorials:

1. in the `body` we now have a single `div` tag only. This is very
   typical of any Single Page Application (SPA) and it resembles the
   `index.html` file from the react tutorial;
1. we did not add the call of any `init()` function. This is because
   in the next few steps we're going to work at the bREPL and we'll
   add that call later, when we'll eventually persist our experiments
   with Reagent in a file.
   
Also copy the `base.css` file from the React Tutorial to the
`html/css` directory of the `modern-cljs` project to obtain the same
basic page style.

```bash
cp /path/to/react-tutorial/public/css/base.css html/css/
```

We are almost done. Before launching the IFDE with the `boot dev`
command, create the `reagent.cljs` file and require both the
`reagent.core` and the `cljsjs.marked` namespaces into it.

```bash
touch src/cljs/modern_cljs/reagent.cljs
```

```clj
(ns modern-cljs.reagent
  (:require [reagent.core :as r]
            [cljsjs.marked]))
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

> NOTE 5: you could have launched instead. But at the moment we're not
> interested in executing any test. We only want to learn about
> Reagent by interacting with it through the bREPL.

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

> NOTE 6: as usual with the IFDE, as soon as you visit the
> [localhost:3000/reagent.html](http://localhost:3000/reagent.html)
> URL the bREPL connects with the JS engine of the browser and it is
> ready to evaluate CLJS expressions.

Now require the `reagent.core` namespace at the bREPL and create your
very first Reagent Component, which, under the wood, will soon become
a React Component.

```clj
cljs.user> (require '[reagent.core :as r])
nil
```

```clj
cljs.user> (defn component-box []
             [:div "Hello, world! I'm a component-box"])
#'cljs.user/component-box
```

Believe it or not, such a simple pure function is enough to create a
Reagent Component corresponding to the `ComponentBox` Component we
previously created with the following `JSX` code:

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

Don't you believe it? Evaluate the following form at the bREPL:

```clj
cljs.user> (r/render [component-box] (.getElementById js/document "content"))
#object[Object [object Object]]
```

Do you see the `Hello, world! I'm a component-box` text in the page?
The above `render` call corresponds to the `ReactDOM.render` function
call used with React:

```js
ReactDOM.render(
  <CommentBox />,
  document.getElementById('content')
);
```

> NOTE 7: as you see, being clojurean, we used `kebab-case` names
> (i.e. `component-box`) instead of `CamelCase` names
> (i.e. `ComponentBox`).

Does the `[:div "some text]` form remembers you something? It uses the
same
[hiccup syntax](https://github.com/weavejester/hiccup/wiki/Syntax)
we're already accustomed with from the tutorials involving
[hiccups](https://github.com/teropa/hiccups) and
[Enlive](https://github.com/cgrand/enlive) libraries.

Evaluate the newly defined function at the bREPL

```clj
cljs.user> (comment-box)
[:div "Hello, world! I'm a comment-box"]
```

No surprises here. It returns exactly what you expect. The very first
magics of Reagent is encapsulated inside the `render` function.

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

Considering that we still have the `domina` library available in our
project, we can simplify the `r/render` call as follows:

```clj
cljs.user> (require '[domina.core :as dom])
nil
```

```clj
cljs.user> (r/render [comment-box] (dom/by-id "content"))
#object[Object [object Object]]
```

Let's move on.

## Porting to Reagent. Step 4: Composing Component

The next step in the React Tutorial is to define two new skeleton
Components. The first, named `CommentList`, to list current comments
and the second, named `CommentForm`, used to create new comments.

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

We can now compose these two newly declared components inside the
`CommentBox` one as follows:

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

Pretty logical. The `div` component now includes an `h1`, a
`CommentList` and a `CommentForm` sub components.

If you now reload the [localhost:3001](http://localhost:3001/) page
you should see the following content:

![Composing Component](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tutorial-03.png)

Let's do the same thing with Reagent.

```clj
cljs.user> (defn comment-list []
             [:div "Hello, world! I'm a comment-list"])
#'cljs.user/comment-list
```

```clj
cljs.user> (defn comment-form []
             [:div "Hello, world! I'm a comment-form"])
```

We can now redefine the `comment-box` component by incorporating into
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

Let's re-render the `comment-box` root component of our newly created
component hierarchy:

```clj
cljs.user> (r/render [comment-box] (dom/by-id "content")
#object[Object [object Object]]
```

The `reagent.html` page is immediately updated and you should see the following content

![Reagent Composing Components](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-tut-01.png)

> NOTE 8: remember that you always have to define a component with one
> `root` component only (e.g. the above `:div`).

So far, so good. We replicated in Reagent the same components
composition with much less incidental (i.e. plumbing) code.

Before going on with the next step, let's evaluate again the
`(comment-box)` function at the bREPL

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
of the Clojure(Script) evaluation rules for vectors: each item in a
vector is evaluated and  when it is a symbol, it evaluates to the
value of the symbol. It just happens that `comment-list` and
`comment-box` are functions, so they evaluate to the corresponding
function objects.

I strongly suggest to take your time to read
[one of the best documentation](https://github.com/Day8/re-frame/wiki/Using-%5B%5D-instead-of-%28%29)
available on Reagent, because it explains why you should never use
round parentheses when composing components, even if you eventually
could:

```clj
cljs.user> (defn comment-box []
             [:div
              [:h1 "Comments"]
              (comment-list)   ;; don't do this
              (comment-form)]) ;; don't do this
#'cljs.user/comment-box
```

```clj
cljs.user> (comment-box)
[:div [:h1 "Comments"] [:div "Hello, world! I'm a comment-list"] [:div "Hello, world! I'm a comment-form"]]
```

```clj
;; still working, but much less efficient in articulated UI scenarios, because
;; comment-list and comment-form are not transformed by `render` into React Components

cljs.user> (r/render [comment-box] (dom/by-id "content"))
#object[Object [object Object]]
```

The two scenario become clearer if you add the
[React Developer Tools extension](https://github.com/facebook/react-devtools)
to your Google Chrome Browser.

By composing the components with the round parentheses, the Reagent
`render` function produces the following component hierarchy

![Round Parentheses](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-round.png)

while, by composing them with the square brackets, the Reagent `render`
function produces the following component hierarchy:

![Square Brackets](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-square.png)

As very well explained in the cited documentation, when you compose
components by using the round parentheses instead of the square
brackets, the `r/render` function will not componentize them as React
component and you'll loose the performance improvement implemented by
the the VDOM differ algorithm.

Let' move on.

## Porting to Reagent. Step 5: Passing data to Components

Let's now create a `Comment` component, which will depend on
the data passed in from its parent (i.e. `ComponentList`).

```js
var Comment = React.createClass({
  render: function() {
    return (
      <div className="comment">
        <h2 className="commentAuthor">
          {this.props.author}
        </h2>
        {this.props.children}
      </div>
    );
  }
});
```

Even without going into details with the JSX syntax, you can see that
the `Comment` component will accepts two arguments from its parent:

1. the author of the comment (i.e. `this.props.author`), rendered as a `h2`;
1. the comment itself (i.e. `this.props.children`), rendered as
   regular text.

We can now redefine the `CommentList` component (i.e. the parent of
the `Comment` component) as follows:

```js
var CommentList = React.createClass({
  render: function() {
    return (
      <div className="commentList">
        <Comment author="Pete Hunt">This is one comment</Comment>
        <Comment author="Jordan Walke">This is *another* comment</Comment>
      </div>
    );
  }
});
```

Note that we have we passed the `author` of the `Comment` via an
attribute and the `text` of the `Comment` via an XML-like child node.

If you reload the [localhost:3001](http://localhost:3001/) page you
should see the following content:

![Pass data to components](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/react-tut-props.png)

Let's replicate the same thing with Reagent. We just need to define a
function with two parameters, `author` and `comment` as follows:

```clj
cljs.user> (defn comment-component [author comment]
             [:div
              [:h2 author]
              comment])
#'cljs.user/comment-component
```

> NOTE 9: the `comment` symbol is already defined in the `cljs.core`
> namespace. This is why we preferred to name the new component as
> `comment-component`.

In Reagent the input parameters of a function correspond to React
`props`.

```clj
cljs.user> (comment-component "Pete Hunt" "This is a comment")
[:div [:h2 "Pete Hunt"] "This is a comment"]
```

Again, the `comment-component` pure function can be composed for
redefining the `comment-list` component as follows:

```clj
cljs.user> (defn comment-list []
             [:div
              [comment-component "Paul Hunt" "This is a comment"]
              [comment-component "Jordan Walke" "This *another* component"]])
#'cljs.user/comment-list
```

Let's see the result by re-rendering the `comment-box` root component

```clj
cljs.user> (r/render [comment-box] (dom/by-id "content"))
#object[Object [object Object]]
```

As soon as you evaluate the `r/render` function, the `reagent.html`
page gets updated and you should see the same content you previously
saw in react

![Reagent props](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-tut-props.png)

## Porting to Reagent. Step 6: Adding Markdown

As we saw at the very beginning of this tutorial while playing with
the final web application implemented in React, we discovered that the
text input of the `CommentForm` component supports `markdown`
syntax. Is does this by using the
[`marked`](https://github.com/chjj/marked) JS library that has been
included in the `index.html` of the tutorial. The marked text will be
then rendered by the `Comment` component.

Here is the React based solution for parsing a `markdown` text and
generating the corresponding HTML markup to be rendered by the
browser.

```js
var Comment = React.createClass({
  rawMarkup: function() {
    var rawMarkup = marked(this.props.children.toString(), {sanitize: true});
    return { __html: rawMarkup };
  },

  render: function() {
    return (
      <div className="comment">
        <h2 className="commentAuthor">
          {this.props.author}
        </h2>
        <span dangerouslySetInnerHTML={this.rawMarkup()} />
      </div>
    );
  }
});
```

Here the React tutorial redefines the `Comment` component by adding
the `rawMarkup` function returning a JS object used to set the
`dengerouslySetInnerHTML` attribute of the `span` component. The
`dangerouslySetInnerHTML` attribute is not a predefined HTML element
attribute. It is internally used bay React itself (tbd: add link).

If you now reload the [localhost:3001](http://localhost:3001/) you
should see the word *another* displayed in *italics*.

Let's try to port this solution on Reagent. As you probably remember
from the lessons involving [hiccups]() and [enlive](), the [hiccup]()
syntax allows to set any attribute of any `html` element by using maps
like so:

```clj
cljs.user> [:div [:label {:for "price"} "Price"]]
[:div [:label {:for "price"} "Price"]]
```

Here we set `"price"` as the value of the `:for` attribute for the
`:label` element. The same thing is true for Reagent Component
definition which, as already said more times, uses the same `hiccup`
syntax. Let's see it at work.

First, we need to require the `cljsjs.marked` namespace defined in the
`cljsjs/marked` JS library that we added to the `:dependencies`
section of the `build.boot` at the beginning of the tutorial.

```clj
cljs.user> (require '[cljsjs.marked])
nil
```

> NOTE 10: when you require an external JS library prepackaged for
> being used by CLJS you can refer its symbols by using the `js`
> fictitious namespace.

We can verify the `marked` JS library works as expected as follows:

```clj
cljs.user> (js/marked "This is *another* comment.")
"<p>This is <em>another</em> comment.</p>\n"
```

```clj
cljs.user> (js/marked "Reagent 0.6.0-alpha is here. [Check it out](http://reagent-project.github.io/news/news060-alpha.html)")
"<p>Reagent 0.6.0-alpha is here. <a href=\"http://reagent-project.github.io/news/news060-alpha.html\">Check it out</a></p>\n"
```

OK, it works like a charm. We now need to replicate the same
`dangerouslySetInnerHTML` React trick inside our `comment-component`
reagent component

```clj
cljs.user> (defn comment-component [author comment]
             [:div 
              [:h2 author]
              [:span {:dangerouslySetInnerHTML 
                      #js {:__html (js/marked comment)}}]])
#'cljs.user/comment-component
```

> NOTE 11: the `:dangerouslySetInnerHTML` attribute expects a JS object
> as a value. The `#js` is a tagged literal transforming a CLJS
> structure (i.e. a CLJS map in this case) into a JS object.

Re-render the `comment-box` as usual

```clj
cljs.user> (r/render [comment-box] (dom/by-id "content"))
#object[Object [object Object]]
```

Again, you immediately see the *another* word shown in *italics*

![Italics](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/reagent-marked.png)


## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-20.md
