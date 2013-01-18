(ns domina.events
  (:require [domina :as domina]
            [goog.object :as gobj]
            [goog.events :as events]))

;; Restatement of the the GClosure Event API.
(defprotocol Event
  (prevent-default [evt] "Prevents the default action, for example a link redirecting to a URL")
  (stop-propagation [evt] "Stops event propagation")
  (target [evt] "Returns the target of the event")
  (current-target [evt] "Returns the object that had the listener attached")
  (event-type [evt] "Returns the type of the the event")
  (raw-event [evt] "Returns the original GClosure event"))

(def builtin-events (set (map keyword (gobj/getValues events/EventType))))

(def root-element (.. js/window -document -documentElement))

(defn- find-builtin-type
  [evt-type]
  (if (contains? builtin-events evt-type)
    (name evt-type)
    evt-type))

;; The listener function will always return true: we ignore the return value
;; of the user-provided function since we don't want consumers to have to worry
;; about that (user-provided functions are usually just a bunch of side effects)
;; User functions can explicitly take control by calling prevent-default or
;; stop-propegation
(defn- create-listener-function
  [f]
  (fn [evt]
    (f (reify
         Event
         (prevent-default [_] (.preventDefault evt))
         (stop-propagation [_] (.stopPropagation evt))
         (target [_] (.-target evt))
         (current-target [_] (.-currentTarget evt))
         (event-type [_] (.-type evt))
         (raw-event [_] evt)
         ILookup
         (-lookup [o k]
           (if-let [val (aget evt k)]
             val
             (aget evt (name k))))
         (-lookup [o k not-found] (or (-lookup o k)
                                      not-found))))
    true))

(defn- listen-internal!
  [content type listener capture once]
  (let [f (create-listener-function listener)
        t (find-builtin-type type)]
    (doall (for [node (domina/nodes content)]
             (if once
               (events/listenOnce node t f capture)
               (events/listen node t f capture))))))

(defn listen!
  "Add an event listener to each node in a DomContent. Listens for events during the bubble phase. Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element."
  ([type listener] (listen! root-element type listener))
  ([content type listener]
     (listen-internal! content type listener false false)))

(defn capture!
  "Add an event listener to each node in a DomContent. Listens for events during the capture phase.  Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element."
  ([type listener] (capture! root-element type listener))
  ([content type listener]
     (listen-internal! content type listener true false)))

(defn listen-once!
  "Add an event listener to each node in a DomContent. Listens for events during the bubble phase. De-registers the listener after the first time it is invoked.  Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element."
  ([type listener] (listen-once! root-element type listener))
  ([content type listener]
     (listen-internal! content type listener false true)))

(defn capture-once!
  "Add an event listener to each node in a DomContent. Listens for events during the capture phase. De-registers the listener after the first time it is invoked.  Returns a sequence of listener keys (one for each item in the content). If content is omitted, binds a listener to the document's root element."
  ([type listener] (capture-once! root-element type listener))
  ([content type listener]
     (listen-internal! content type listener true true)))

(defn unlisten!
  "Removes event listeners from each node in the content. If a listener type is supplied, removes only listeners of that type. If content is omitted, it will remove listeners from the document's root element."
  ([] (unlisten! root-element))
  ([content]
     (doseq [node (domina/nodes content)]
       (events/removeAll node)))
  ([content type]
     (let [type (find-builtin-type type)]
       (doseq [node (domina/nodes content)]
         (events/removeAll node type)))))

(defn- ancestor-nodes
  "Returns a seq of a node and its ancestors, starting with the document root."
  ([n] (ancestor-nodes n [n]))
  ([n so-far]
     (if-let [parent (.-parentNode n)]
       (recur parent (cons parent so-far))
       so-far)))

;; See closure.goog.testing.events.fireBrowserEvent. This function will give us the same
;; bubbling/capturing functionality as W3C DOM level 2 events, on native browser nodes,
;; so we can dispatch against things that do not inherit from goog.event.EventTarget.
(defn dispatch-browser!
  "Intended for internal/testing use only. Clients should prefer dispatch!. Dispatches an event as a simulated browser event from the given source node. Emulates capture/bubble behavior. Returns false if any handlers called prevent-default, otherwise true."
  [source evt]
  (let [ancestors (ancestor-nodes (domina/single-node source))]
    ;; Capture phase
    (doseq [n ancestors]
      (when-not (.-propagationStopped n)
        (set! (.-currentTarget evt) n)
        (events/fireListeners n (.-type evt) true evt)))
    ;; Bubble phase
    (doseq [n (reverse ancestors)]
      (when-not (.-propagationStopped n)
        (set! (.-currentTarget evt) n)
        (events/fireListeners n (.-type evt) false evt)))
    (.-returnValue_ evt)))

(defn dispatch-event-target!
  "Intended for internal/testing use only. Clients should prefer dispatch!. Dispatches an event using GClosure's event handling. The event source must extend goog.event.EventTarget"
  [source evt]
  (events/dispatchEvent source evt))

(defn- is-event-target?
  "Tests whether the object is a goog.event.EventTarget"
  [o]
  (and (.-getParentEventTarget o)
       (.-dispatchEvent o)))

(defn dispatch!
  "Dispatches an event of the given type, adding the values in event map to the event object. Optionally takes an event source. If none is provided, dispatches on the document's root element. Returns false if any handlers called prevent-default, otherwise true."
  ([type evt-map] (dispatch! root-element type evt-map))
  ([source type evt-map]
     (let [evt (events/Event. (find-builtin-type type))]
       (doseq [[k v] evt-map] (aset evt k v))
       (if (is-event-target? source)
         (dispatch-event-target! source evt)
         (dispatch-browser! source evt)))))

(defn unlisten-by-key!
  "Given a listener key, removes the listener."
  [key]
  (events/unlistenByKey key))

(defn get-listeners
  "Returns a sequence of event listeners for all the nodes in the
content of a given type."
  [content type]
  (let [type (find-builtin-type type)]
    (mapcat #(events/getListeners % type false) (domina/nodes content))))
