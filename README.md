# Tetris

A classic Tetris game implementation in ClojureScript using Reagent (React) and Shadow-CLJS.

[![Tests](https://github.com/kevgathuku/tetris-cljs/actions/workflows/test.yml/badge.svg)](https://github.com/kevgathuku/tetris-cljs/actions/workflows/test.yml)

## Features

- Classic Tetris gameplay with 7 tetromino shapes (T, O, L, I, S, Z, J)
- Keyboard controls (arrow keys and space for rotation)
- Automatic piece falling with configurable speed
- Boundary validation and collision detection with settled pieces (junkyard)
- Wall kicks for rotation near edges
- Hot-reloading for development
- Pure functional game logic with immutable data structures

## Tech Stack

- **ClojureScript** - Functional programming on JavaScript
- **Reagent** - ClojureScript wrapper for React
- **Shadow-CLJS** - Build tool with hot-reload and REPL support
- **SVG** - Vector graphics rendering

## Prerequisites

- Node.js (v20 or higher recommended)
- Java 21 (required by Shadow-CLJS/Google Closure Compiler)
- npm or yarn

## Installation

```bash
# Clone the repository
git clone git@github.com:kevgathuku/tetris-cljs.git
cd tetris

# Install dependencies
npm install
```

## Development

### Start Development Server

```bash
npx shadow-cljs watch frontend
```

This starts the development server at <http://localhost:8080> with hot-reloading enabled.

### Connect to REPL

Shadow-CLJS provides nREPL support for interactive development:

```bash
# The .nrepl-port file contains the port number
# Default port is 3333 (configured in shadow-cljs.edn)
```

Using `clj-nrepl-eval` tool:

```bash
# Discover running REPL servers
clj-nrepl-eval --discover-ports

# Evaluate code
clj-nrepl-eval -p 3333 "(+ 1 2)"
```

### Run Tests

```bash
npx shadow-cljs compile test
```

## Production Build

```bash
npx shadow-cljs release frontend
```

Output will be in `public/js/` directory.

## Game Controls

- **Arrow Left** - Move piece left
- **Arrow Right** - Move piece right
- **Arrow Down** - Move piece down faster
- **Arrow Up** - Rotate piece 90° clockwise
- **Space** - Rotate piece 90° clockwise
- **Shuffle Button** - Generate new piece

## Architecture

### Coordinate Systems

The game uses two coordinate systems:

**Shape Space (1-4 grid):**

- Tetromino shapes defined in a 1-4 coordinate grid
- Center point at (2.5, 2.5) for rotation calculations
- Used for shape definitions and rotation transformations

**Board Space (0-indexed):**

- Game board uses 0-indexed coordinates (rows 0-19, columns 0-9)
- 10 columns × 20 rows
- Valid positions: x ∈ [0, 9], y ∈ [0, 19]

### Module Structure

```
src/main/acme/frontend/
├── tetris/
│   ├── point.cljs      # Core point manipulation (coords, bounds, collision)
│   ├── points.cljs     # Collection-level operations (move, rotate, valid)
│   ├── block.cljs      # Tetromino logic (shapes, movement, rotation)
│   └── game.cljs       # Game state (move, merge, score, wall kicks)
└── app.cljs            # Main application, UI, and game loop
```

### Data Model

**Tetromino Structure:**

```clojure
{:shape :t          ; Shape type (:t :o :l :i :s :z :j)
 :rotation 0        ; Rotation in degrees (0, 90, 180, 270)
 :location [2 -2]}  ; Grid coordinates [x y]
```

**Game State:**

```clojure
{:tetro {...}       ; Current falling tetromino
 :score 0           ; Player score
 :points [...]      ; Rendered points with colors [[[x y] color] ...]
 :junkyard {...}}   ; Settled pieces as {[x y] color} map
```

### Rotation System

Rotation uses matrix transformations centered at (2.5, 2.5):

- **90°** = flip + transpose
- **180°** = mirror + flip
- **270°** = mirror + transpose

The rotation center, shape coordinates, and starting location are tightly coupled. See `CLAUDE.md` for detailed documentation.

## Project Configuration

- **shadow-cljs.edn** - Build configuration
- **deps.edn** - Clojure dependencies
- **package.json** - Node dependencies
- **CLAUDE.md** - Development guidance and architecture documentation

## Testing

Tests are written using ClojureScript's built-in test framework:

```clojure
(deftest in-bounds-test
  (testing "x boundaries with plain points"
    (is (true? (point/in-bounds? [5 10])))))
```

Test files:

- `point_test.cljs` - Point operations (coords, in-bounds, collide, valid)
- `points_test.cljs` - Collection operations (move, add-color, rotate, valid)
- `game_test.cljs` - Game logic (init, movement, collision detection)

Run tests with:

```bash
npx shadow-cljs compile test
```

Tests run automatically on push/PR via GitHub Actions (`.github/workflows/test.yml`).

## Development Notes

- Hot-reload hooks prevent state loss during development
- Window-level keyboard listeners provide seamless game controls
- Functional architecture ensures predictable state transitions
- All game logic uses immutable data structures
