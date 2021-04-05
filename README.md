# To Play a Game or Run a Tournament

To play two games against my agent, one as white and one as black: 

$ java -Xms4g -Xmx4g -XX:NewSize=3g -jar chess.jar -g 1 -a my_agent/arap223.jar agents/human.jar

To run a round robin tournament with any number of agents: 

$ java -Xms4g -Xmx4g -XX:NewSize=3g -jar chess.jar -g 1 -a [agents to include]

The flags "-Xms4g -Xmx4g -XX:NewSize=3g" are optional. 
They specify that 4GB of memory be used, 3 of which are used for new objects. 

The flag "-g 1" specifies that each agent will play exactly two games against every other agent, 
one as white and one as black. 

# chess.jar

This is the implementation of the rules of chess and the GUI. 
It was provided by my professor, Dr. Stephen Ware of the University of Kentucky. 

# src/

Contains the source files that I wrote for my chess agent. 

## src/arap.java

This is the implementation of my chess agent, which uses iterative deepening minimax
search with alpha beta pruning. It inherits from the Agent class, which was provided by Dr. Ware. 
Its function chooseMove() overrides that of Agent. 

Documentation for the object-oriented API provided by Dr. Ware used is here: 
http://cs.uky.edu/~sgware/courses/cs463g/projects/chess/doc/

## src/Result.java

This is a data class that contains a board state and its value/evaluation. 
A positive value is good for white, while a negative evaluation is good for black. 

# my_agent/arap223.jar

This is the JAR file that contains my chess agent. 

# agents/

These are other chess agents provided by Dr. Ware. I received a grade based on my agent's 
performance in a round robin tournament with these agents. Because my agent won the tournament, 
I received extra credit on the assignment. 

## agents/human.jar

Use this agent to play as a human, and test your mettle. 

## agents/random.jar

This agent chooses moves at random. The random number generator always starts with the same seed, 
so all else being equal, Random will make the same moves every time.

## agents/greedy.jar

This agent always chooses the move which maximizes its total material score. 
If it has multiple moves that result in the same score, it chooses at random between them.

## agents/novice.jar

This agent uses MinMax search with Alpha Beta Pruning to look 1 turn ahead 
(i.e. 2 ply or 1 move for each player) and choose the move which will maximize its material score. 

## agents/beginner.jar

This agent uses iterative deepening MinMax search with Alpha Beta Pruning to look 2 turns 
(i.e. 4 ply) ahead and choose the move which will maximize its material score.

## agents/intermediate.jar

Intermediate uses iterative deepening MinMax search with Alpha Beta Pruning to look 3 turns ahead 
(when possible). Its search is also quiescent.