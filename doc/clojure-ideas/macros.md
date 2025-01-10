My "system" functions need 2 things:

- Receive and return the state, in order to be easily chainable on the state for debugging, and to be easily usable in the game
- (in a highly optimized world) only access what they need from the world, to allow for parallelization and whatnot

I think both of these can be achived with a def-system macro; If I'm thinking of it like in python, it will create a function that can either be called directly with a state and returns a state, for usage in debugging, or with a set of queries/components/entities/w/e and return the updated set of what it needs to return - to be used in a parallelized system execution place. It will probably be 2 different functions under the hood.

So yeah this is a use for macros
