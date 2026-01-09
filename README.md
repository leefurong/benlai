# Benlai (本来)

> **One State, One Truth.**
> 
> UI as a pure projection of your server-side data.

## The Philosophy: "Benlai" (本来)

The name comes from the Chinese concept **本来 (Běn Lái)**, meaning *The Original* or *The Root*.

There is an ancient Eastern philosophy:
> **"When the root is established, the Way grows naturally."**
> (本立而道生)

In the context of **Benlai**:
*   **The Root (本)** is your Server-side State.
*   **The Way (道)** is your UI.

We believe that **UI is just a fleeting shadow of the server's data**. You should focus on the *Root* (the Data), and let the *Way* (the UI) generate itself automatically. 

Stop managing the "branches" (DOM, Client Store, API adapters) and return to the "root".

## The Magic (Demo)

Imagine writing a dynamic web app without writing a single line of JavaScript, API endpoints, or JSON serializers.

Here is a fully functional **Counter App** running on the JVM:

```clojure
(ns my-app.core
  (:require [benlai.core :as b]))

;; 1. State lives ONLY on the Server (JVM)
(defonce app-state (atom {:count 0}))

;; 2. Event Handlers are pure Clojure functions
(defn increment [db _event]
  (update db :count inc))

(defn decrement [db _event]
  (update db :count dec))

;; 3. View is a projection of that state
(b/defview root-component [db]
  [:div.container
   [:h1 "Server Count: " (:count db)]
   
   [:div.actions
    ;; The :on-click is automatically wired to the server
    [:button {:on-click decrement} "-"]
    [:button {:on-click increment} "+"]]])

;; 4. Start the Server
(defn -main []
  ;; Benlai handles the HTTP/WebSocket transport automatically
  (b/start-server! {:port 8080
                    :view root-component
                    :state app-state}))
```

### What just happened?
1.  User clicks `+`.
2.  **Benlai** sends the event to the server (via HTTP or WebSocket).
3.  Server runs `(increment @app-state)`.
4.  Server calculates the **Hiccup Tree Diff** (not a text diff, but a structural patch).
5.  Browser receives the patch and surgically updates the DOM.

**Zero latency logic. Zero API boilerplate.**

## Why Benlai?

Modern web development is too complex. We spend too much time moving data from DB -> Backend Model -> JSON -> API -> Frontend Store -> Component.

**Benlai cuts the middleman.**

*   ❌ No REST/GraphQL APIs
*   ❌ No Client-side State Management (Redux/Re-frame)
*   ❌ No Hydration/Serialization hell
*   ✅ **Just Clojure functions returning Hiccup**

## Architecture

Benlai is **transport agnostic**. It can work over WebSockets for real-time apps or standard HTTP for simpler use cases.

```mermaid
graph LR
    User[User Interaction] -->|Event| ClientRuntime
    
    subgraph "Transport Layer (HTTP / WebSocket)"
      ClientRuntime <-->|JSON Patch| Server
    end

    subgraph Server [Server (The Brain)]
        Server -->|Dispatch| Logic[Event Handlers]
        Logic -->|Update| DB[(Atom / Datomic)]
        DB -->|Trigger| Watcher
        Watcher -->|Render| View[Hiccup View]
        View -->|Tree Diff| Server
    end
    
    ClientRuntime -->|Patch DOM| Browser[Browser Screen]
```

## Getting Started

Add Benlai to your `deps.edn`:

```clojure
{:deps
 {io.github.yourusername/benlai {:git/tag "v0.0.1" :git/sha "HEAD"}}}
```

*(Note: This project is currently in the **Proof of Concept** phase.)*

### Roadmap

- [ ] **Core**: Tree Diffing Algorithm (Server-side Virtual DOM).
- [ ] **Transport**: Support both HTTP (Long-polling/AJAX) and WebSockets.
- [ ] **Runtime**: Minimal JS client (generic event trapper & DOM patcher).
- [ ] **Reactivity**: Datomic/Datahike integration.

## License

Copyright © 2026 [leefurong]

Distributed under the MIT License.
