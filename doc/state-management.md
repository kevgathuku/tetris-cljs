# State Management in ClojureScript/Reagent

This document explains the state management patterns used in this Tetris implementation.

## Atom Types: Reagent vs. Regular Clojure Atoms

### Reagent Atoms (`r/atom`)

Reagent atoms are reactive and trigger React re-renders when their value changes.

**When to use:**
- State that is **displayed in the UI**
- Data that components need to **react to**
- Values that affect rendering

**Examples in this project:**
```clojure
(defonce app-state
  (r/atom {:current-index nil
           :current-block nil}))
```

**Why:**
- `:current-block` is rendered on screen (shape, position, color)
- When the block moves, the UI must update
- Components automatically re-render when `app-state` changes

**Characteristics:**
- ✓ Triggers React re-renders on change
- ✓ Components automatically subscribe
- ✓ Efficient React reconciliation
- ✗ Overhead for non-UI state

### Regular Clojure Atoms (`atom`)

Regular atoms do NOT trigger React re-renders. They're pure Clojure state containers.

**When to use:**
- Internal bookkeeping
- Values **NOT displayed in the UI**
- Performance-critical state that changes frequently

**Examples in this project:**
```clojure
(defonce tick-interval (atom nil))
```

**Why:**
- Stores `setInterval` ID (a number)
- Never displayed to the user
- Only used internally to clear the interval
- Would cause unnecessary re-renders if using `r/atom`

**Characteristics:**
- ✓ No React overhead
- ✓ Lightweight state container
- ✓ Perfect for non-UI state
- ✗ Components don't react to changes

## Common Use Cases

### Use Reagent Atom (`r/atom`)

| Use Case | Example |
|----------|---------|
| Game state | `{:score 100 :level 3}` |
| Current player data | `{:name "Alice" :position [5 10]}` |
| UI toggles | `{:modal-open? true}` |
| Form inputs | `{:username "" :email ""}` |
| Filtered/sorted lists | Derived from base data |

### Use Regular Atom (`atom`)

| Use Case | Example |
|----------|---------|
| Timer/interval IDs | `(atom nil)` |
| WebSocket connections | `(atom ws-conn)` |
| DOM element refs | `(atom dom-node)` |
| Caches | `(atom {:key-1 "cached-value"})` |
| Event listeners | `(atom listener-fn)` |

## Performance Implications

### Unnecessary Re-renders Example

**Bad: Using `r/atom` for interval ID**
```clojure
(defonce tick-interval (r/atom nil))  ; ❌ Wrong choice

(defn start-tick! []
  (reset! tick-interval              ; Triggers re-render
    (js/setInterval tick-game! 800)))
```

**Problem:**
- Every `reset!` triggers React reconciliation
- Entire component tree is checked for updates
- Zero UI benefit since ID is never displayed

**Good: Using regular `atom` for interval ID**
```clojure
(defonce tick-interval (atom nil))    ; ✓ Correct choice

(defn start-tick! []
  (reset! tick-interval              ; No re-render
    (js/setInterval tick-game! 800)))
```

**Benefit:**
- No React overhead
- State changes are instant
- Better performance

## Rule of Thumb

Ask yourself: **"Is this value displayed in the UI?"**

- **YES** → Use `r/atom`
- **NO** → Use regular `atom`

## Mixed State Example

From this project:

```clojure
;; UI state - displayed on screen
(defonce app-state
  (r/atom {:current-index nil    ; Shown in debug info
           :current-block nil}))  ; Rendered as SVG

;; Internal state - never displayed
(defonce tick-interval (atom nil))  ; Just for cleanup
```

## Deriving State

Sometimes you need to derive one atom from another:

```clojure
;; Source atom (reagent)
(defonce app-state (r/atom {:blocks [...]}))

;; Derived atom (also reagent, so components react)
(defonce visible-blocks
  (r/reaction
    (filter :visible @app-state)))
```

**Note:** `r/reaction` creates a reactive computation that updates automatically.

## Testing Considerations

Regular atoms are easier to test since they don't involve React lifecycle:

```clojure
;; Easy to test
(def interval-id (atom nil))
(reset! interval-id 123)
(is (= 123 @interval-id))

;; Requires Reagent test utilities
(def ui-state (r/atom {:count 0}))
;; Need to render components and check updates
```

## Common Pitfalls

### 1. Using `r/atom` for everything

**Problem:**
```clojure
(def websocket (r/atom nil))     ; ❌ Unnecessary
(def cache (r/atom {}))          ; ❌ Unnecessary
(def request-id (r/atom 0))     ; ❌ Unnecessary
```

**Solution:** Use regular `atom` for non-UI state.

### 2. Not using `r/atom` when needed

**Problem:**
```clojure
(def score (atom 0))  ; ❌ Won't trigger re-render

(defn score-display []
  [:div "Score: " @score])  ; Won't update when score changes!
```

**Solution:** Use `r/atom` for displayed values.

### 3. Mixing concerns

**Problem:**
```clojure
(def app-state (r/atom {:block {:x 5 :y 1}
                        :interval-id 123}))  ; ❌ Mixed UI and non-UI
```

**Solution:** Separate UI state from internal state.

## Further Reading

- [Reagent Documentation](https://reagent-project.github.io/)
- [ClojureScript Atoms](https://clojurescript.org/reference/atoms)
- [React State Management Patterns](https://react.dev/learn/managing-state)
