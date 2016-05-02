# Tutorial 22 - A minimalist interface to React with Reagent (Part II)

In the [previous tutorial][1] we ported to [Reagent][2] the first
steps of the official [React Tutorial][3].

In this Part II of the tutorial on Reagent we're going to complete the
porting of the official [React Tutorial][3] to [Reagent][2], by
introducing component state management.

## Preamble

To start working, assuming you've `git` installed, do as follows to
restart from the end of the
[previous tutorial](https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md#save-your-interactive-experience)
for both React Tutorial and its porting on Reagent.

## React Tutorial

If you did not save the state of the React Tutorial at the end of the
[previous tutorial]((https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md#save-your-interactive-experience)),
open a terminal and submit the following commands

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

Finally visit the URL [localhost:3001](http://localhost:3001/).

## Reagent port of React Tutorial

Open a new terminal and enter the following commands

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

Next visit the URL
[localhost:3000/reagent.html](http://localhost:3000/reagent.html). 
Event if you don't see anything, because we still have to attach
the `comment-box` root component to the `"content"` `div` of the
`reagent.html` page, a websocket connection will be established
between your development environmnet and your browser's JS engine.
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

The value of `this.state.data` got from the server is passed down
to the `CommentList` component. Each comment contained in
`this.state.data` is then passed to the `Comment` component of the
list. Only new or updated `Comment` components will be redraw.

This last observation is very important, because it illustrates that a
React component gets updated not only when its private state changes,
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
  // ...,
  ,
  {
    "id": 3,
    "author": "Dan Holmsand",
    "text": "Reagent 0.4.3 is now, finally, a thing."
  }
]
```

![update a comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/updatecomment1.png)

Before porting the component state management from React to Reagent,
we have to digress a little bit about Clojure(Script)
[`atom`](http://clojure.org/reference/atoms).

## On being mundane

Immutability is divine, but sometime you still need to modify the
world around you. ClojureScript has immutability as the default
behavior of its data structure, but it also offers you a more mundane
`atom` function when you really want to manipulate the reality.

Let's get some help from the bREPL to learn about `atom` usage. Say
you want to maintain the number of times a button gets clicked

```clj
cljs.user> (def clicks (atom 0))
#'cljs.user/clicks
```

By evaluating `clicks`, you get the `Atom` object itself.

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

The `deref` function is used so frequently that it deserves a
[reader macro](https://yobriefca.se/blog/2014/05/19/the-weird-and-wonderful-characters-of-clojure/)
to shorten its call:

```clj
cljs.user> @clicks
0
```

Every time the button gets clicked, you want to increment its internal
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
value:

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
watcher to log its state in time at the `js/console`.

To add a `watcher` you use `add-watch`:

```clj
cljs.user> (doc add-watch)
-------------------------
cljs.core/add-watch
([iref key f])
  Adds a watch function to an atom reference. The watch fn must be a
  fn of 4 args: a key, the reference, its old-state, its
  new-state. Whenever the reference's state might have been changed,
  any registered watches will have their functions called. The watch
  fn will be called synchronously. Note that an atom's state
  may have changed again prior to the fn call, so use old/new-state
  rather than derefing the reference. Keys must be unique per
  reference, and can be used to remove the watch with remove-watch,
  but are otherwise considered opaque by the watch mechanism.  Bear in
  mind that regardless of the result or action of the watch fns the
  atom's value will change.  Example:

      (def a (atom 0))
      (add-watch a :inc (fn [k r o n] (assert (== 0 n))))
      (swap! a inc)
      ;; Assertion Error
      (deref a)
      ;=> 1
nil
```

In this context, we're only interested in logging the new value
(i.e. the fourth argument passed to the watcher function).

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

You should see the state of the `clicks` atom printed at the
browser console every time you change its internal value at the
bREPL. Here, the fundamental word is *time*.

![Watch clicks atom at console](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/atom-console.png)

You can think about the `data` containing comments as an `atom` and
the `comment-box` component as an observer that execute a reaction,
redrawing itself, anytime it observes a change in the state of the
comments itself.

> NOTE 2:  in reality, as we'll see in a moment, this statement is not
> true.

## From atom to ratom

The
[Reagent tutorials on the re-frame wiki](https://github.com/Day8/re-frame/wiki#reagent-tutorials)
does a terrific job explaining Reagent and I strongly reccomend you
read it several times, until you really grasp the entire content.

That said, Reagent offers its own version of the standard CLJS `atom`
definition, AKA `ratom`, supporting the same `atom` protocol
(e.g. `swap!`, `reset!`, etc.) plus something new, as you can read
from its documentation at the REPL:

```clj
cljs.user> (doc r/atom)
-------------------------
reagent.core/atom
([x] [x & rest])
  Like clojure.core/atom, except that it keeps track of derefs.
Reagent components that derefs one of these are automatically
re-rendered.
nil
```

This is very interesting: whenever a component derefs a `ratom`, it
automagically re-renders itself. This statement is not totally true,
because a Reagent component re-renders itself when it derefs a `ratom`
only if the internal value of the `ratom` is not
[identical](https://github.com/reagent-project/reagent/pull/143) to
the previous one.

## Ratom at work

We previously saw in the React Tutorial that by modifying the array of
comments in the `comments.json` file, periodically read by ajax, we're
creating the condition for a change in the `CommentBox`
`this.state.data` state and, consequently, in the `CommentList` and
`Comment` components as well via the one-way data flow communication
model.

Even without porting the `ajax` stuff from the React Tutorial to
Reagent, by now knowing that a Reagent component could eventually
re-render itself by derefing a `ratom`, we should be able to obtain
the same behavior.

First, open the `reagent.cljs` file and modify the `data` symbol by
wrapping its definition in a `ratom`:

```clj
(def data (r/atom [{:id 1
                    :author "Pete Hunt"
                    :text "This is one comment"}
                   {:id 2
                    :author "Jordan Walke"
                    :text "This is *another* comment"}]))
```

Then modify consequently the `comment-box` component definition by
simply adding the dereference of its comments parameter as follows:

```clj
(defn comment-box [comments]
  [:div 
   [:h1 "Comments"]
   [comment-list @comments]
   [comment-form]])
```

Next, re-render the `comment-box` in the `"content"` div as usual:

```clj
cljs.user> (render [comment-box data] (by-id "content"))
#object[Object [object Object]]
```

Finally, swap the content of the `ratom` by adding a new comment to
it:

```clj
cljs.user> (swap! data conj {:id 3 :author "Mimmo Cosenza" :text "Reagent is even better"})
[{:id 1, :author "Pete Hunt", :text "This is one comment"} {:id 2, :author "Jordan Walke", :text "This is *another* comment"} {:id 3, :author "Mimmo Cosenza", :text "Reagent is even better"}]
```

The `comment-box` *view* component should have immediately been updated
with the new comment just added to the `ratom`.

![view update](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/viewupdate.png)

Let's see if the `comment-box` component reacts to an updated comment as well:

```clj
cljs.user> (swap! data assoc-in [(rand-int (count @data)) :text] "This is a **randomly** assigned comment")
[{:id 1, :author "Pete Hunt", :text "This is one comment"} {:id 2, :author "Jordan Walke", :text "This is *another* comment"} {:id 3, :author "Mimmo Cosenza", :text "This is a **randomly** assigned comment"}]
```

> NOTE 3: the text of the comment to be updated is randomly chosen by
> the `(rand-int (count @data))` expression.

Again, a soon as the `swap!` form gets evaluated, you should see the
corresponding components *view* been updated.

![update comment](https://github.com/magomimmo/modern-cljs/blob/master/doc/images/updatecomment.png)

We just experimented with how Reagent abstracted away, in the `ratom`
structure, the React need to use `this.state.data` instead of
`this.props.data` to manage state management in React components.

Hopefully, you should have also appreciated how the Reagent way of
managing components state made the Reagent/CLJS code much more concise
than the corresponding React/JS code.

The next and last step in porting the React Tutorial to Reagent has to
do with the `CommentForm` component.

## Adding new comments

bla bla bla

## Next Step - TBD

# License

Copyright © Mimmo Cosenza, 2012-16. Released under the Eclipse Public
License, the same as Clojure.

[1]: https://github.com/magomimmo/modern-cljs/blob/master/doc/second-edition/tutorial-21.md
[2]: http://reagent-project.github.io/
[3]: https://facebook.github.io/react/docs/tutorial.html

