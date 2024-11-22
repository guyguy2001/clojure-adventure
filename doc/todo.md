### Implementation details of world still present in `core.clj`:

- `[:world :base-grid]` - some functions such as `try-move-by` still expect the world, and access the `:base-grid` in it
- Generation of the initial state

### Ideas

Grid rewrite thoughts:

- There's no reason to embed stuff directly into the ascii (like how I did with #), it's kinda silly
  Also the whole concept of layers which are just 2d arrays are kinda silly
- I also think I'll do away with layers, I'll just have objects, maybe sorted by :layer

Concerete plans:

- Remove grid2d layers - the inputs are only 1d lists of objects.
- The result is a 2d grid of vecs of items on that square - maybe not a vec at the start, but it will be a vec in the future.
- I can easily query for object at pos, and normal objects behave just like #
- Objects will have :collision false if they are ghosts, and another property which I forgot

Open questions:

- Do I update the pos->entity representation whenever I move an object? Or only at the end of turns?
  - Do I give up on that idea for the sake of simplicity and being able to just change :pos?
    - I do need to pass the world already for bounds checking, so I guess this isn't that bad
- Semi-related but very important - I'm passing state everywhere, and I now finally see that it makes testing / playing with stuff more difficult.
  How do I stop?
  - I guess just like in bevy, I'll just ask for the right parameters; If I need the world and a specific notification queue, I'll ask for them.
    - This also means that the world stuff needs to take the world and not the state.
  - Thinking about it in terms of bevy - it makes total sense that the get_neighboring_objects(pos) function is a method of some World/Grid, not some nebulous state thingy.
    In my case, the world seems to contain both the entity list (a pretty hardcode ecs thing), but also a mapping between 2d coordinate space and these objects.
    Right now I'm starting to see this separation more clearly - as I'm (finally) getting the 2d grid to be the source of truth for world-ly stuff, instead of the :objects dict.

Unrelated: the ID of objects should:
a) be called ID.
b) be inside of the object as well.

"So let's say I think of modifying world/state like I would of a side effect -
that means that the functions which do it should be further out, maybe in their own module
Also reading from world could be considered a side effect just like writing to world would
It could be stated that a function should either receive [world, entity_id] or [entity1, entity2] -
never have both a world and an entity (not sure if that's a good convention or not)"

### Actual Tasks

- DONE - Get the world/ functions to accept `world` instead of `state`

- DONE Implement populations for the new grid
- Implement the new grid into the main code

  - DONE - Fix target selection (broken because now non-intractable objects are also in there)
  - DONE - Fix collision
  - DONE - Update new grid when moving
  - Remove all instances of :base-grid
  - Rename :new-grid
  - See where :new-grid is used directly, if there's a good alternative

- Put the IDs of objects into the objects themselves
- Retire the functions that return both of them (unless they're also needed for some reason)
