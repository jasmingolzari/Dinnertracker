# Sustainability in DinnerTracker

## Introduction
DinnerTracker is a Java application where users log in, register the
groceries in their fridge and browse recipes. Recipes can be sorted by how
many of the ingredients the user already has, so the best-matching recipe
appears first. We assume the app is used regularly by a few thousand
households, and we use the Sustainability Awareness Framework (SusAF) to
reflect on its impacts across dimensions and levels. The numbers in
parentheses in the text below refer to the impacts shown in our
sustainability awareness diagram (SusAD). Green boxes are positive effects
and red boxes are negative effects. Not all impacts in the text are in the
diagram.

![SusAD diagram for DinnerTracker](susAD.jpg)

## Environmental aspects
The main environmental benefit is recduced food waste (2) (enabling level, environmental dimension). A clear overview of what is in the fridge (1) helps users avoid buying items they already own, and sorting recipes by available ingredients ecourages them to use food before it spoils. Fewer unnecessary purchases also means less packaging and fewer transport emissions. At the immediate level, the app has a small footprint since it is lighweight and stores litte data. At the systemic level, if many households change their habits, total household food waste could fall (5). 

## Social and individual aspects
On the individual dimension, knowing what can be cooked with the ingredients at hand reduces the daily stress of deciding what to eat and may encourage cooking at home. Privacy is a concern, since login details and a fridge inventory reveal something about a person´s habits and diet (6). We limit this risk by keeping each user´s fridge private: only the logged-in users can see their own groceries. 

On the social dimension, recipes are public and visible to all users, which makes the app a small shared resource where knowledge about cooking is available to everyone. Recipes do not show who created them. This protects the privacy of contributors and makes it easy to share without exposing personal information. Because each fridge is individual, the app does not support shared household planning, which limits its social benefits. Inclusion is also a challenge: users need a device, and it is only available in Norwegian (7). 

## Economic and technical aspects
Economically, buying only what is needed lowers grocery costs (4). This is a chain of effects: a clear fridge overview (1) leads to fewer duplicate purchaes, which leads to less food waste (2), which leads to lower costs(4). Technically, we built the app in Java with a modular structure, which makes it easier to maintain and to extend with new features (3). 

## Negative effects and trade-offs
Not all effects are positive. Entering groceries manually takes effort (8), and if the inventory is not kept up to date, the overview (1) becomes wrong and the benefit disappers (2). Sorting by the most matching ingredients does not consider health, season or how much of an ingredient is used, so it may favor certain recipes over others. A rebound effect is also possible: if cooking becomes easier, some users may end up buying more, not less. Finally, storing user data always carries privacy risks (6). 

## Future work
If we developed DinnerTracker further with sustainabiliy in mind, we would prioritixe: expiry dates with warnings for items about to spoil, which would directly target food waste, a shopping list based on missing ingredients, a shared household fridge, and filters for vegetarian meals and allergies. 