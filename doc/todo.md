### Implementation details of world still present in `core.clj`:

- `[:world :base-grid]` - some functions such as `try-move-by` still expect the world, and access the `:base-grid` in it
- Generation of the initial state
