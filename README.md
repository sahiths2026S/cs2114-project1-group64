# cs2114-project1-group64
This is a roulette based decision game about predicting a sequence.

# CS-Roulette
This is the repository for the CS 2k September team.

Player Experience:
"Press 1 to view tutorial, press 2 to skip."
2
"There are X cyanide pills and y sugar pills."
"You have 5 health, and The Dealer has 5 health."
"Press 1 to take a pill, press 2 to give opponent pill, press 3 to use item."
1
"You took a cyanide pill! You have 4 health remaining."
"The dealer took a cyanide pill. He has 4 health remaining."
""
"You have 4 health, and The Dealer has 4 health."
"Press 1 to take a pill, press 2 to give opponent pill, press 3 to use item."
...
"There are no more pills left. Restarting."
"You have been given an item."
"There are X cyanide pills and y sugar pills."

Reach goals:
Tutorial
Items
Basic terminal graphics

Items:
Acid - Current pill deals 2 damage instead of 1.
Vial - Remove the current pill from the chamber.
Neutralizer - Heal 1 health.
Handcuffs - Skip The Dealer's next turn.
Burner phone - player learns of one pill in the sequence at random.


Logic:
Object class for game data:
    Int for player health
    Int for dealer health
    List of 1s and zeros (1s represent cyanide and 0s represent sugar)
    Object class for item:
        Item type
        Use item function
