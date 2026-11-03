# Challenges 

During the project we faced several challenges which we solved by finding suitable solutions together. This file reflects on the two that had the greatest impact on our work. 

## Challenge 1: Blocking dependencies 

### The challenge 
Early in the project, many of our issues depended on each other. Group members therefore had to wait for others to finish before they could continue with their own issues. For example, development of the GUI could not proceed until `RecipeFileHandler` was completed. This was before we started using issue relationships, so the dependencies were not visible to the group. In Release 1, this caused many essential issues to be closed near the milestone deadline, which we aimed to avoid. 

### Measures 
After recognizing the problem, we introduced several measures to prevent it in later releases. We began marking blocking relationships between issues in GitHub, and we identified dependencies both when issues were created and before work on them started. Large tasks were split into smaller, independent parts, so that fewer tasks relied on one another. In addition, we held regular meetings where blockages could be uncovered early if someone was stuck. 

### Lessons learned 
A key lesson was that extra planning in advance saves considerable time and restructuring later. Making dependencies visible turns a hidden risk into something the group can plan around. Breaking large issues into smaller independent parts also made the progress more smooth, and it made it easier for another member to take over a task if someone got stuck. 

## Challenge 2: Introducing amounts and units 

### The challenge 
In Release 2, we found a problem in our domain model. The GUI let users enter a quantity for each ingredient, but `ingredients` in `Recipe` and `foodItems` in `Fridge` were lists. A list cannot connect an amount to each item, so `Frigdge`could not track how much of each item was left. Since the GUI was already built on top of this model, fixing it was a large change affecting the domain, persistence and the user interface.

### Measures 
We replaced each lists with a `Map<FoodItem, Amount>`. We created a new record called `Amount` that holds a quantity and a `Unit`, and a new enum called `Unit` with valid units such as liters and grams. Using an enum prevents misspelled units. Persistence now stores the unit together with each item. The group members working on the GUI extended the recipe page with unit selection.

### Lessons learned 
The main lesson was that the domain model should be designed around what users and GUI need, before other layers are built on top of it. Every layer that depends on the model makes a later change more demanding, as this challenge showed. If we were to start over, we would check the domain model against the GUI's needs earlier.