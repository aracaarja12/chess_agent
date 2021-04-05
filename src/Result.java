package edu.uky.ai.chess.ex;

import edu.uky.ai.chess.state.State;

// Result class stores a board state and its value. Used in my chess agent. 
public class Result {
	public State state;
	public double value;

	public Result(State state, double value) {
		this.state = state;
		this.value = value;
	}
}