# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Tetris game implementation in ClojureScript using Reagent (React wrapper) and Shadow-CLJS for compilation and hot-reloading.

## Documentation

Additional documentation can be found in the `doc/` directory:
- `doc/state-management.md` - Guide to using Reagent atoms vs. regular Clojure atoms

## Development Workflow

**Start development server:**

```bash
npx shadow-cljs watch frontend
```

This starts the development server at <http://localhost:8080> with hot-reloading enabled.

**Build for production:**

```bash
npx shadow-cljs release frontend
```

**REPL connection:**
Shadow-CLJS provides nREPL support. Connect to the REPL after starting the watch command. The `.nrepl-port` file contains the port number.

# Clojure REPL Evaluation

The command `clj-nrepl-eval` is installed on your path for evaluating Clojure code via nREPL.

**Discover nREPL servers:**

`clj-nrepl-eval --discover-ports`

**Evaluate code:**

`clj-nrepl-eval -p <port> "<clojure-code>"`

With timeout (milliseconds)

`clj-nrepl-eval -p <port> --timeout 5000 "<clojure-code>"`

The REPL session persists between evaluations - namespaces and state are maintained.
Always use `:reload` when requiring namespaces to pick up changes.

## Architecture

### Module Structure

**Tetris Game Logic** (`src/main/acme/frontend/tetris/`):

- `point.cljs` - Core point manipulation (translate, left, right, down)
- `points.cljs` - Collection-level point operations
- `block.cljs` - Tetromino (game piece) logic including shapes, movement, and rotation

**Application Layer** (`src/main/acme/frontend/`):

- `app.cljs` - Main Reagent application with game loop and rendering

### Data Model

**Tetromino (Block) Structure:**

```clojure
{:shape :t          ; Keyword from #{:t :o :l :i :s :z :j}
 :rotation 0        ; Degrees: 0, 90, 180, 270
 :location [2 0]}   ; Grid coordinates as [x y] vector (not pixels)
```

**Shape Definitions:**
Shapes are defined as vectors of `[x y]` coordinate pairs in `block.cljs`. Each tetromino has 4 coordinate pairs in a **1-4 coordinate grid** (X: 1-3, Y: varies by shape), with Y-coordinates designed to center around point 2.5 for proper rotation.

**⚠️ IMPORTANT:** Shape Y-coordinates, rotation center in `point.cljs`, and starting location in `create` function are **tightly coupled**. See the "Rotation" section under "Key Patterns" for the critical relationship between these values. The O-block at `[[2 2] [3 2] [2 3] [3 3]]` serves as the reference - it must stay in place when rotated.

**Color Definitions:**
Each shape has an associated color defined in the `colors` map. Colors are attached to points during rendering via the `points/add-color` transformation.

### Coordinate Systems

**Dual Coordinate Systems:**

*Shape Space (1-4 grid):*

- Tetromino shapes defined in a 1-4 coordinate grid
- Center point at (2.5, 2.5) for rotation calculations
- X range: 1-3, Y range: 1-4 (varies by piece)

*Board Space (0-indexed):*

- Game board uses 0-indexed coordinates (rows 0-19, columns 0-9)
- Grid coordinates are vectors: `[x y]` where x is column, y is row
- Valid board positions: x ∈ [0, 9], y ∈ [0, 19]
- Starting location `[2 -2]` compensates for shape space offset (see "Rotation" section for why -2)

**Grid vs Pixel Coordinates:**

- Game logic uses grid coordinates (integer row/column positions)
- SVG rendering requires pixel coordinates
- Conversion: `pixel = grid * cell-size` (currently 20px per cell)
- Direct mapping with no offset: grid coordinate 0 → pixel 0, grid coordinate 1 → pixel 20, etc.

**SVG Origin:**

- (0, 0) is at top-left corner
- Y-axis increases downward (row 0 at top, row 19 at bottom)
- Board is rendered as 200×400px SVG (10 columns × 20 rows with 20px cells)

### State Management

**Global State:**

```clojure
app-state (r/atom {:current-index nil    ; For message shuffling demo
                   :current-block nil})  ; Active falling tetromino
```

**Game Loop:**
Managed via `js/setInterval` with cleanup hooks:

- `start-tick!` - Initializes game loop (800ms interval), calls `stop-tick!` first to prevent stacking
- `stop-tick!` - Cleanup function to clear interval
- `tick-game!` - Called each tick to move block down or spawn new one when bottom is reached
- Bottom detection: Checks the maximum y-coordinate of all rendered points (`>= 19` for a 20-row board)
- Hot-reload hooks: `^:dev/before-load stop` and `^:dev/after-load init`

### Key Patterns

**Point Transformation:**
The `point` namespace provides functional transformations using **vector-based coordinates**. All movement functions return new points (immutable):

```clojure
(point/translate [5 1] [-1 0])  ; Move point left: [5 1] + [-1 0] = [4 1]
(point/left [5 1])              ; Convenience function: [4 1]
(point/down [5 1])              ; Move down: [5 2]
(point/right [5 1])             ; Move right: [6 1]
```

**Rotation:**
Rotation uses matrix transformations centered at (2.5, 2.5):

- `mirror [[x y]]` → `[(- 5 x) y]` - horizontal flip
- `flip [[x y]]` → `[x (- 5 y)]` - vertical flip
- `transpose [[x y]]` → `[y x]` - swap x and y
- 90° = flip + transpose
- 180° = mirror + flip
- 270° = mirror + transpose

**⚠️ CRITICAL: Rotation Center, Shape Coordinates, and Starting Location are Interconnected**

These three variables MUST be changed together. Changing one without adjusting the others will break rotation behavior (e.g., O-block will move when rotated).

**The relationship:**

1. **Rotation Center: (2.5, 2.5)**
   - Defined by formulas: `(- 5 x)` and `(- 5 y)` in `point.cljs`
   - The number 5 = 2 × center coordinate 2.5
   - Change this by modifying the constants in `mirror` and `flip` functions

2. **Shape Y-Coordinates: Must center around 2.5**
   - Current: All shapes use Y coordinates where the center ≈ 2.5
   - T, O, S, Z: Y range 2-3 (center: 2.5) ✓
   - I, L, J: Y range varies but centers near 2.5
   - O-block MUST be `[[2 2] [3 2] [2 3] [3 3]]` to stay in place when rotated

3. **Starting Location Y-offset: Compensates for shape coordinates**
   - Current: `[2 -2]`
   - Formula: `min_shape_y + location_y = 0` (to render at top row)
   - With shapes starting at y=2: `2 + (-2) = 0` ✓

**Example: If you change rotation center to (1.5, 1.5):**

- Change `mirror` to `[(- 3 x) y]` (because 2 × 1.5 = 3)
- Change `flip` to `[x (- 3 y)]`
- Shift ALL shapes so minimum y = 0 (center at 1.5)
- Change starting location to `[2 0]` (because 0 + 0 = 0)

**Color Attachment:**
Points are transformed into `{[x y] color}` maps via `points/add-color`:

```clojure
(points/add-color [[2 3] [3 3]] "red")  ; → ([[2 3] "red"] [[3 3] "red"])
(into {} (points/add-color [[2 3]] "red"))  ; → {[2 3] "red"}
```

**Block Movement:**
Block movement delegates to point functions via `update`:

```clojure
(update tetro :location point/right)  ; location is a vector [x y]
```

**Points Collection Transformation:**
The `points` namespace handles transforming collections of points:

```clojure
(points/move [[1 0] [2 0] [3 0]] [5 1])  ; Translate all points by [5 1]
```

**Rendering Pattern:**

1. `game/show` merges junkyard with current tetro into `:points` map
2. `block/show` converts tetro to `{[x y] color}` map:
   - Get shape coordinates from shape definition (1-4 space)
   - Apply rotation transformations (centered at 2.5, 2.5)
   - Translate to board position by adding location vector
   - Attach color to each point → `{[x y] color}`
3. `render-points` iterates the map to create SVG rect elements
4. Destructure each entry as `[[x y] color]`
5. Scale grid coordinates to pixels: `(* x 20)` and `(* y 20)` for 20px cells
6. Points are rendered within an SVG `:g` (group) element

## Shadow-CLJS Configuration Notes

- Entry point: `acme.frontend.app/init`
- Development HTTP server on port 8080 serves the `public/` directory
- Uses simple optimizations for release builds
- Source paths include `src/dev`, `src/main`, and `src/test`
- CIDER nREPL support configured for Emacs integration

## ClojureScript Best Practices & Gotchas

### Test Naming Conventions

- Avoid `?` in test function names (e.g., use `in-bounds-test` not `in-bounds?-test`)

### Avoid Special Form Names

**CRITICAL:** Never name functions after ClojureScript/Clojure special forms or JavaScript constructors. These create ambiguity and cause confusing errors.

**Problematic names to avoid:**
- `new` - Conflicts with the JavaScript object constructor special form
- `def`, `if`, `let`, `loop`, `recur`, `do`, `quote`, `var`, `fn`, `set!`
- Other special forms listed in the ClojureScript documentation

**Example of the problem:**

```clojure
;; ❌ BAD - Conflicts with special form
(defn new []
  (-> (init)
      (new-tetro)))

;; When called from within the same namespace:
(new)  ; Error: "null is not a constructor"
       ; Compiler confuses function with JavaScript constructor

;; ✅ GOOD - Use descriptive, non-conflicting names
(defn new-game []
  (-> (init)
      (new-tetro)))

(new-game)  ; Works correctly
```

**Error symptoms:**
- "null is not a constructor"
- "X is not a function"
- Unexpected behavior when calling functions without namespace qualification

**Solution:**
Use descriptive, domain-specific names like `new-game`, `create-game`, `init-state`, etc. instead of generic names that might conflict with language primitives.
