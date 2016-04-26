# Tutorial 22 - A minimalist interface to React with Reagent (Part II)

In the [previous tutorial][1] we ported to [Reagent][2] the first
steps of the official [React Tutorial][3].

In this Part II of the tutorial on Reagent we're going to complete the
porting of the official [React Tutorial][3] to [Reagent][2], by
introducing components state management.

## Preamble

To start working, assuming you've `git` installed, do as follows to
restart from the end of the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md#save-your-interactive-experience)
for both React Tutorial and its porting on Reagent.

## React Tutorial

Open a terminal and submit the following commands

```bash
git clone https://github.com/magomimmo/react-tutorial.git
cd react-tutorial
git checkout reagent-tutorial
```

Then from the same terminal install the required `npm` modules and run
the React Tutorial server:

```bash
npm install
PORT=3001 node server.js
```

Finally visit the [localhost:3001](http://localhost:3001/) URL.

## Reagent port of React Tutorial

Open a new terminal and submit the following commands

```bash
git clone https://github.com/magomimmo/modern-cljs.git
cd modern-cljs
git checkout se-tutorial-21
git checkout -b reagent-tutorial-2
```

Then launch the development environment as usual

```bash
boot dev
```

Next visit the
[localhost:3000/reagent.html](http://localhost:3000/reagent.html)
URL. Event if you'll not see anything, because we still have to attach
the `comment-box` root component to the `"content"` `div` of the
`reagent.html` page, a websocket connection will be established
between your development environmnet and the JS engine of your
browser.

Now open a new terminal and launch the nREPL client followed by the
bREPL client on top of it as usual:

```bash
cd /path/to/modern-cljs
boot repl -c
...
boot.user=> 
```

```clj
boot.user> (start-repl)
...
cljs.user> 
```

We're almost done. To complete the setup for continuing the porting of
the React Tutorial to Reagent, we need to require few namespaces from
the bREPL:

```clj
cljs.user> (require '[reagent.core :as r :refer [render]])
nil
```

```clj
cljs.user> (require '[domina.core :as dom :refer [by-id]])
nil
```

```clj
cljs.user> (require '[modern-cljs.reagent :as tut :refer [comment-box data]])
nil
```

> NOTE 1: the latest requirement is needed to make the `comment-box` and
> `data` symbols visible in the bREPL, because they are now defined in
> the `modern-cljs.reagent` namespace. In the previous bREPL session
> they were defined at the bREPL in the `cljs.user` namespace.

Finally, to attach and render the components we need to call the
Reagent `render`function:

```clj
cljs.user> (render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

You should now see the content of the `reagent.html` page.

## Reactive state

The next steps in the React Tutorial are
[Fetching from the server](https://facebook.github.io/react/docs/tutorial.html#fetching-from-the-server)
and
[Reactive State](https://facebook.github.io/react/docs/tutorial.html#reactive-state). In
this port of the React Tutorial to Reagent we're going to restrict our
scope to the state management only, because it is where Reagent
standouts the most from React.

Let's start by reading the paragraph of the React Tutorial on state
management:

> So far, based on its props, each component has rendered itself
> once. props are immutable: they are passed from the parent and are
> "owned" by the parent. To implement interactions, we introduce mutable
> state to the component. this.state is private to the component and can
> be changed by calling this.setState(). When the state updates, the
> component re-renders itself.

In the subsequent couple of steps the React Tutorial substitutes the
hard-coded `data` with some dynamic from the server. Considering I'm not
going to port to Reagent this part of the React Tutorial, I'm just
coping the corresponding code to facilitate its pasting into the
`example.js` source file. I'll also limit my code comments to few
things.

```js
// delete the `data` variable definition.
// substitute the previous CommentBox component definition with
// the following one
var CommentBox = React.createClass({
  loadCommentsFromServer: function() {
    $.ajax({
      url: this.props.url,
      dataType: 'json',
      cache: false,
      success: function(data) {
        this.setState({data: data});
      }.bind(this),
      error: function(xhr, status, err) {
        console.error(this.props.url, status, err.toString());
      }.bind(this)
    });
  },
  getInitialState: function() {
    return {data: []};
  },
  componentDidMount: function() {
    this.loadCommentsFromServer();
    setInterval(this.loadCommentsFromServer, this.props.pollInterval);
  },
  render: function() {
    return (
      <div className="commentBox">
        <h1>Comments</h1>
        <CommentList data={this.state.data} />
        <CommentForm />
      </div>
    );
  }
});

// substitute the previous ReactDOM.render call with the following one
ReactDOM.render(
  <CommentBox url="/api/comments" pollInterval={2000} />,
  document.getElementById('content')
);
```

The most important variation from the previous version of `CommentBox`
React component is the substitution of the `this.props.data`
*property* with the `this.state.data` *state* in its `render`
function, initially set to an empty array of comments, to make the
component stateful.

Apparently, the only way to change the state of the component is via
the `setState()` function.

The `setState()` function is indirectly called the very first time,
via the `loadCommentsFromServer()` function, in the body of the
`componentDidMount()`. `componentDidMount()` function is called only
once, immediately after the component has been mounted in the DOM buy
the `ReactDOM.render` function. Then, `setState()` is indirectly
called every 2 seconds (i.e. pollInterval), by passing to it the array
of comments got by polling the server via ajax.

If you heard that React implements *one-way data flow* communication
model between data and User Interfaces, this is what they mean:

```
data -> CommentBox -> CommentList -> Comment
```

The value of the `this.state.data` got from the server is passed down
to the `CommentList` component. Each comment contained in
`this.state.data` is then passed to the `Comment` component of the
list. Only new or updated `Comment` components will be redraw.

This last observation is very important, because it illustrates that a
React component gets updated non only when its private state changes,
but even when they receive new `props` from its owner
(e.g. `CommentBox -> CommentList -> Comment`).

As you are going to see in a moment, Reagent exploits the above React
behavior to not use the component state management offered by React
and, instead, take care of it by itself.

Let's now verify if the above code refactoring works as
expected. First reload the [localhost:3001](http://localhost:3001/)
URL. Considering that we still have to implement the `CommentForm`
component to create new comments, to see the React state management at
work you need to manually change the `comment.json` array of comments
which is read every two second by the the `CommentBox` component.

Open the `comments.json` file, which is located in the
`react-tutorial` main folder, and add a new comment to it:

```json
[
  {
    "id": 1388534400000,
    "author": "Pete Hunt",
    "text": "Hey there!"
  },
  {
    "id": 1420070400000,
    "author": "Paul O’Shannessy",
    "text": "React is *great*!"
  },
  {
    "id": 3,
    "author": "Dan Holmsand",
    "text": "Reagent 0.6.0-alpha is here. Check it out! http://reagent-project.github.io/news/news060-alpha.html"
  }
]
```

When you save the file, after a moment you should see the page been
updated with the new comment by the
[author of Reagent](https://twitter.com/holmsand).

![Dan Holmsand Comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/newcomment.png)

You could even modify one of the comment in the `comments.json` file
to see a reaction in the page as you save the file:

```json
[
  ...,
  ,
  {
    "id": 3,
    "author": "Dan Holmsand",
    "text": "Reagent 0.4.3 is now, finally, a thing."
  }
]
```

![update a comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/updatecomment.png)

Before porting the component state management from React to Reagent,
we have to digress a little bit about Clojure(Script)
[`atom`](http://clojure.org/reference/atoms).

## On being mundane

Immutability is divine, but sometime you still need to modify the
world around you. ClojureScript chosen the immutability as the default
behavior of its data structure, but it also offers you a more mundane
`atom` function when you really want to manipulate the reality.

Let's get same help from the bREPL to learn about `atom` usage. Say
you want to maintain the number of times a button gets clicked

```clj
cljs.user> (def clicks (atom 0))
#'cljs.user/clicks
```

By evaluating `clicks`, you get an `Atom` object as its value.

```clj
cljs.user> clicks
#object [cljs.core.Atom {:val 0}]
```

To get the protected value living inside the `clicks` symbol, you need
to dereference it by using the `deref` function:

```clj
cljs.user> (deref clicks)
0
```

The `deref` function is used so frequently that it deserves a reader
macro to shorten its call:

```clj
cljs.user> @clicks
0
```

Every time the button gets clicked, you to want to change its internal
value by using a function (i.e. `inc`):

```clj
cljs.user> (swap! clicks inc)
1
```

```clj
cljs.user> @clicks
1
```

And Sometimes you want to reset the number of clicks to a specific
value without using a function:

```clj
cljs.user> (reset! clicks 0)
0
```

```clj
cljs.user> @clicks
0
```

Even if changes to atoms are always non blocking and free of race
conditions, this is not the main reason we are talking about them in
the Reagent contest.

Say you want to observe the state of `clicks`. You can easily add a
watcher to log its state in time at the `js/console`:

```clj
(add-watch clicks :log #(-> %4 clj->js js/console.log))
```

Be sure to have your browser console open on the
[localhost:3000/reagent.html](http://localhost:3000/reagent.html) page
and observe it while updating the state of the `clicks` atom:

```clj
cljs.user> (reset! clicks 0)
0
```

```clj
cljs.user> (swap! clicks + 5)
5
```

```clj
cljs.user> (swap! clicks dec)
4
```

```clj
cljs.user> (swap! clicks dec)
3
```

You should see the state of the `clicks` atom to be printed at the
browser console every time you change its internal value at the
bREPL. Here, the fundamental word is *time*.

![Watch clicks atom at console](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/atom-console.png)

You can think about the `data` containing comments as an `atom` and
the `comment-box` component as an observer that execute a reaction,
redrawing itself, anytime it observes a change in the state of the
comments.

## From atom to ratom

The
[re-frame tutorials on Reagent documentation](https://github.com/Day8/re-frame/wiki#reagent-tutorials)
do an excellent job in explaining this behavior.

bla bla bla

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md
[2]: https://github.com/cljsjs/packages
[3]: https://github.com/teropa/hiccups
[4]: https://github.com/cgrand/enlive
[5]: https://github.com/weavejester/hiccup/wiki/Syntax

