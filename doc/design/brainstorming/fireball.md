# Brainstorming

## Collision

**Current state**
The fireballs get stuck when hitting a wall.
They are solid, and don't disappear, and don't deal damage.

**Desired state**

- The fireballs disappear when hitting anything.
- They damage enemies when hitting them.
  - But this shuold be extendible for w/e logic I want to run.
- Entities can step onto them, they get damaged (if hostile).

**Ideas**
The way I currently see it, there are 2 main options:

- system+event-listener: Create system-like functions (similar to handle-mining) that access `:collision-data` on `state`, and if they see a collision on the fireball at the end of the turn, they do something.
- one-shot system: Store inside the fireball a function that takes the state and the collided thing and acts on the state.

I think I prefer the first option, at least for now.

**Collision DB**
I Think all of the contention in my mind is about how to handle the events after we find that 2 objects collided.

However, I think the rest of the collision handling is the same regardless of what I pick.

What I need:

- When trying to move, being able to query whether there's a solid object where I'm trying to go
- For things I'm interesting in knowing about their collisions (e.g. fireballs), be able to query / call a callback when such collisions happen
  - Ideally, allow me to stop the events if a fireball hits multiple things on the same tile but I despawned it first (for situations where this is how I'd like it to work.)
