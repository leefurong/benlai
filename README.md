# Benlai (本来)

> **True full-stack means zero JavaScript.**
> 
> Stop the futile game of syncing partial state across the wire. Benlai keeps the Truth on the server and projects it directly to the browser.

## What is Benlai?

**Benlai is a full-stack web framework where you write zero JavaScript.**

Instead of managing state in the browser, syncing it with the server, and dealing with API endpoints, serialization, and hydration—**Benlai keeps everything on the server**. The browser is just a rendering engine.

### The Problem We're Solving

Traditional web development forces you to:

```
1. Design a database schema
2. Create backend models
3. Write API endpoints (REST/GraphQL)
4. Serialize to JSON
5. Create frontend stores (Redux/Recoil/Zustand)
6. Write API clients
7. Handle loading/error states
8. Sync state between client and server
9. Deal with hydration issues
10. Manage two codebases (backend + frontend)
```

**All of this just to move data from point A to point B.**

### The Benlai Way

With Benlai, you:

```
1. Define state (a Clojure atom)
2. Write event handlers (pure functions)
3. Write views (Hiccup)
4. Done.
```

**That's it.** No APIs. No serialization. No state syncing. No JavaScript.

## Why Should You Care?

### If you're tired of:
- ❌ Writing the same data transformations twice (server + client)
- ❌ Maintaining API contracts and versioning
- ❌ Debugging state sync issues ("Why isn't my UI updating?")
- ❌ Managing client-side state that should live on the server
- ❌ Writing JavaScript when you'd rather write Clojure

### Benlai gives you:
- ✅ **One codebase** - Everything in Clojure
- ✅ **One source of truth** - State lives on the server
- ✅ **Zero JavaScript** - The browser just renders
- ✅ **No API boilerplate** - No endpoints, no serialization
- ✅ **Instant updates** - State changes automatically reflect in UI

## See It in Action

Here's a complete, working counter app:

```clojure
(ns my-app.core
  (:require [benlai.core :as b]))

;; State lives on the server
(defonce app-state (atom {:count 0}))

;; Event handlers are pure Clojure functions
(defn increment [db _event]
  (update db :count inc))

(defn decrement [db _event]
  (update db :count dec))

;; Views are Hiccup
(b/defview root-component [db]
  [:div
   [:h1 "Count: " (:count db)]
   [:button {:on-click ["my-app.core/increment"]} "+"]
   [:button {:on-click ["my-app.core/decrement"]} "-"]])

;; Start the server
(defn -main []
  (b/start-server! {:port 8080
                    :view root-component
                    :state app-state}))
```

**That's the entire app.** No JavaScript. No API. No state management library.

When you click the button:
1. Browser sends event to server
2. Server updates state
3. Server re-renders view
4. Browser updates UI

**All automatically. Zero boilerplate.**

## Philosophy: "Benlai" (本来)

The name comes from the Chinese concept **本来 (Běn Lái)**, meaning *The Original* or *The Root*.

There is an ancient Eastern philosophy:
> **"The master is the root."**
> (君子务本)
> 
> **"When the root is established, the Way grows naturally."**
> (本立而道生)

In **Benlai**:
*   **The Root (本)** = Your Server-side State
*   **The Way (道)** = Your UI

**UI is just a projection of your server's state.** Focus on the root (the state), and let the UI generate itself.

## Quick Start

### Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure) installed
- A web browser

### Run a Demo

```bash
# Clone the repository
git clone <repository-url>
cd benlai

# Run the Counter demo
clj -M -m my-app.core

# Or run the Todo List demo
clj -M -m my-app.todo
```

Then open http://localhost:8080 in your browser.

## Demo Applications

### 1. Counter App (`my-app.core`)

A simple counter demonstrating basic state management and event handling.

**Features:**
- Increment/decrement counter
- Server-side state management
- Automatic UI updates

### 2. Todo List App (`my-app.todo`)

A full-featured todo list application.

**Features:**
- Add todos
- Mark todos as complete/incomplete
- Delete individual todos
- Mark all complete/incomplete
- Clear all completed todos

## How It Works

### The Flow

```
User clicks button
    ↓
Browser sends event to server (HTTP POST)
    ↓
Server dispatches to event handler
    ↓
Handler updates server state (atom)
    ↓
Server re-renders view (Hiccup)
    ↓
Server sends updated Hiccup tree (JSON)
    ↓
Browser converts Hiccup to HTML
    ↓
Browser updates DOM
```

**All in one round trip. No state syncing. No API layer.**

### Key Design Principles

#### 1. Server-Side State as Single Source of Truth

All application state lives on the server (JVM). The browser is a **stateless rendering engine** - it has no business logic, no state management, only UI rendering.

#### 2. Distinguish Browser-Side Temporary State

**Browser-side temporary state** (like input field values while typing) should **NOT** be sent to the server. Only **committed state** (like form submissions) should trigger server events.

```clojure
;; ❌ Wrong: Sending every keystroke to server
[:input {:on-input ["handler" "#input.value"]}]

;; ✅ Correct: Browser handles temporary state, server handles submission
[:form {:on-submit ["handler" "#input.value"]}
 [:input {:id "input"}]]  ; No server binding
```

#### 3. Standard Browser Event Names

Benlai uses standard browser event names (`:on-click`, `:on-submit`, `:on-change`) instead of custom attributes. This makes the code more familiar and compatible with standard HTML.

#### 4. Pure Clojure Functions

Event handlers are pure Clojure functions that take `(db event)` and return a new state. No side effects, no framework magic - just functions.

## Technical Documentation

### Core API

#### `b/defview name [params] & body`

Defines a view component that renders Hiccup from state.

```clojure
(b/defview root-component [db]
  [:div "Hello, " (:name db)])
```

#### `b/start-server! {:port port :view view-fn :state state-atom}`

Starts the HTTP server and serves the application.

```clojure
(b/start-server! {:port 8080
                  :view root-component
                  :state app-state})
```

### Event Handlers

Event handlers receive two arguments:
1. `db` - Current state (the atom's value)
2. `event` - Event data from client

Event data format:
- Single value: `{:value "text"}`
- Multiple values: `{:0 "first", :1 "second"}`
- Map: `{:key "value"}` (if single map param)

Example:
```clojure
(defn add-todo [db event]
  (let [text (get event :value)]
    (update db :todos conj {:id (generate-id) :text text})))
```

### Event Binding Syntax

**Server Event Handlers** (vector format):
```clojure
[:button {:on-click ["my-ns/handler" "#input.value"]} "Submit"]
[:form {:on-submit ["my-ns/handler" "#input.value"]} ...]
[:input {:on-change ["my-ns/handler" (:id item)]}]
```

**Client-Side Handlers** (string format - for temporary UI effects):
```clojure
[:input {:on-focus "this.style.borderColor='#4CAF50'"
         :on-blur "this.style.borderColor='#e0e0e0'"}]
```

### Parameter Resolution

The client runtime can resolve parameter expressions:

- `"#input.value"` - Get value of element with id "input"
- `"input[name].value"` - Get value of input with name attribute
- `"\"literal\""` - Literal string value
- Direct values (UUIDs, numbers, etc.) - Passed as-is

### State Management

State is stored in a Clojure `atom` on the server:

```clojure
(defonce app-state (atom {:todos []}))
```

Event handlers return new state, which replaces the atom's value:

```clojure
(defn update-state [db event]
  (assoc db :key (get event :value)))  ; Returns new state
```

### Client Runtime

The client runtime (`resources/public/index.html`) is a minimal JavaScript implementation that:

1. **Renders Hiccup**: Converts Hiccup tree (JSON) to HTML
2. **Attaches Event Listeners**: Finds `data-on-*` attributes and wires them to server
3. **Sends Events**: POSTs events to `/api/event` endpoint
4. **Updates DOM**: Replaces entire app HTML on each update

## Architecture

```
┌─────────┐         HTTP POST          ┌─────────┐
│ Browser │ ──────────────────────────> │ Server  │
│         │ <────────────────────────── │ (JVM)   │
│         │    Hiccup Tree (JSON)       │         │
└─────────┘                             └─────────┘
     │                                        │
     │ 1. User clicks button                  │
     │ 2. Send event                          │
     │                                        │ 3. Dispatch to handler
     │                                        │ 4. Update state
     │                                        │ 5. Render view
     │                                        │ 6. Return Hiccup
     │ 7. Update DOM                         │
```

Benlai uses HTTP for transport (WebSocket support planned).

## Status: Proof of Concept

⚠️ **This project is currently in the Proof of Concept (POC) phase.**

*   ✅ Core functionality is working
*   ✅ Two demo applications available (Counter & Todo List)
*   ⚠️ **API may change rapidly** - not recommended for production use
*   ⚠️ Limited documentation and error handling

## Current Limitations

As a POC, Benlai has several limitations:

1. **Single Session**: All users share the same state (no session isolation)
2. **Full DOM Replacement**: Updates replace entire app HTML (no incremental updates)
3. **HTTP Only**: No WebSocket support yet
4. **Basic Error Handling**: Limited error messages and recovery
5. **No Tree Diffing**: Full re-render on every update (planned)

## Roadmap

- [x] **Core**: Basic server-side state management
- [x] **Core**: Event handling system
- [x] **Core**: Hiccup view rendering
- [x] **Core**: Client runtime (minimal JS)
- [ ] **Core**: Tree diffing algorithm (incremental DOM updates)
- [ ] **Transport**: WebSocket support for real-time apps
- [ ] **Core**: Session management (multi-user support)
- [ ] **Core**: Better error handling and recovery
- [ ] **Reactivity**: Datomic/Datahike integration
- [ ] **Performance**: Optimize for large state trees

## Project Structure

```
benlai/
├── src/
│   ├── benlai/
│   │   └── core.clj          # Core library (defview, start-server!)
│   └── my_app/
│       ├── core.clj          # Counter demo app
│       └── todo.clj          # Todo List demo app
├── resources/
│   └── public/
│       └── index.html        # Client runtime (Hiccup → HTML, event handling)
├── deps.edn                  # Project dependencies
├── README.md                 # This file
└── GETTING_STARTED.md        # Quick start guide
```

## Contributing

This is a POC project. Contributions, feedback, and ideas are welcome!

## License

Copyright © 2026 [leefurong]

Distributed under the MIT License.
