package edu.uky.ai.chess.ex;

import java.util.ArrayList;
import java.util.Iterator;

import edu.uky.ai.chess.Agent;
import edu.uky.ai.chess.state.Board;
import edu.uky.ai.chess.state.Player;
import edu.uky.ai.chess.state.State;
import edu.uky.ai.chess.state.Piece;
import edu.uky.ai.chess.state.Pawn;
import edu.uky.ai.chess.state.Knight;
import edu.uky.ai.chess.state.Bishop;
import edu.uky.ai.chess.state.Rook;
import edu.uky.ai.chess.state.Queen;

/**
 * This agent chooses its next move with iterative deepening Minmax search with Alpha Beta Pruning
 * to look 3 or more turns ahead. 
 * 
 * Note: this was written such that no more than half a million board states can be considered
 * for each move made. Additionally, the agent cannot consider a single move for more than five 
 * minutes, although this program tends to make a move in a matter of seconds. These are requirements 
 * from my AI class. 
 * 
 * @author Aaron Applegate
 */
public class arap223 extends Agent {
	public arap223() {
		super("arap223");
	}

	// Finds the best move with a variation of Minmax IDS and Alpha Beta Pruning
	@Override
	protected State chooseMove(State current) {
		Result best_result = IDS(current);
		// Given the best result on the horizon, dive to find the best move now
		while (best_result.state.previous != current)
			best_result.state = best_result.state.previous;
		return best_result.state;
	}
	
	/* 
	 * Handles the iterative deepening aspect of the search. Each iteration looks ahead max_depth moves, 
	 * or max_depth/2 turns. The search only ever goes as deep as max_max_depth, which is larger in the 
	 * endgame. 
	 */
	private static Result IDS(State state) {
		// Look ahead more aggressively in the endgame
		int max_depth = 2, max_max_depth = 6;
		if (state.board.countPieces() <= 10)
			max_max_depth = 12;
		
		// Look ahead with Iterative Deepening Minmax Adversarial Search
		Result result = null, best_result = null;
		while (max_depth <= max_max_depth && !state.budget.hasBeenExhausted()) {
			if (state.player == Player.WHITE) // if white
				result = find_max_ab(state, 0, max_depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			else // if black
				result = find_min_ab(state, 0, max_depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			if (!state.budget.hasBeenExhausted()) // if the search completed without exhausting the budget
				best_result = result;
			else // if the budget was exhausted
				break;
			// iterate by two to iteratively check an additional turn, rather than an additional move
			max_depth += 2;
		} // while it needs to look deeper and hasn't yet hit the budget
		return best_result;
	}
	
	/*
	 * By looking ahead to max_depth, this maximizes the minimum gain for white in terms of an evaluation 
	 * of the board state. This is the objective of white because board states are evaluated by subtracting 
	 * black's score from white's, where a higher score is considered better. Alpha Beta Pruning is used to 
	 * limit the number of board states that need to be considered. 
	 */
	private static Result find_max_ab(State state, int depth, int max_depth, double alpha, double beta) {
		// Base case where state is conceptually a leaf node
		Result best_result = new Result(state, eval_board(state));
		if (depth >= max_depth || state.over) // if the max depth has been reached or the game is over
			return best_result; 
		
		double best = Double.NEGATIVE_INFINITY; // set best to negative infinity
		
		// generate all child states and sort them such that moves involving a capture will be considered first
		// intuition: moves involving captures tend to be either very good or very bad, which helps w/ AB pruning
		State child = null;
		ArrayList<State> children = new ArrayList<>();
		ArrayList<State> without_capture = new ArrayList<>();
		Iterator<State> iterator = state.next().iterator();
		while(!state.budget.hasBeenExhausted() && iterator.hasNext()) {
			child = iterator.next();
			if (was_capture(child))
				children.add(child);
			else
				without_capture.add(child);
		}
		children.addAll(without_capture);
		
		// for every child of the current state
		iterator = children.iterator();
		while(iterator.hasNext()) {
			child = iterator.next(); // child = next child state
			// calculate the value of the child pseudo-recursively by considering subsequent moves
			Result child_result = find_min_ab(child, depth+1, max_depth, alpha, beta);
			if (child_result.value > best) { // if this child has the best value so far, update best
				best = child_result.value;
				best_result = child_result;
			}
			if (best >= beta) // prune if best is no less than beta
				return best_result;
			alpha = Math.max(alpha, best); // set alpha
		}
		return best_result; // return the best result
	}
	
	/*
	 * By looking ahead to max_depth, this minimizes the maximum loss for black in terms of an evaluation 
	 * of the board state. This is the objective of black because board states are evaluated by subtracting 
	 * black's score from white's, where a higher score is considered better. Alpha Beta Pruning is used to 
	 * limit the number of board states that need to be considered. 
	 */
	private static Result find_min_ab(State state, int depth, int max_depth, double alpha, double beta) {
		// Base case where state is conceptually a leaf node
		Result best_result = new Result(state, eval_board(state));
		if (depth >= max_depth || state.over) // if the max depth has been reached or the game is over
			return best_result;
		
		double best = Double.POSITIVE_INFINITY; // set best to positive infinity
		
		// generate all child states and sort them such that moves involving a capture will be considered first
		// intuition: moves involving captures tend to be either very good or very bad, which helps w/ AB pruning
		State child = null;
		ArrayList<State> children = new ArrayList<>();
		ArrayList<State> without_capture = new ArrayList<>();
		Iterator<State> iterator = state.next().iterator();
		while(!state.budget.hasBeenExhausted() && iterator.hasNext()) {
			child = iterator.next();
			if (was_capture(child))
				children.add(child);
			else
				without_capture.add(child);
		}
		children.addAll(without_capture);
		
		// for every child of the current state
		iterator = children.iterator();
		while(iterator.hasNext()) {
			child = iterator.next(); // child = next child state
			// calculate the value of the child pseudo-recursively by considering subsequent moves
			Result child_result = find_max_ab(child, depth+1, max_depth, alpha, beta);
			if (child_result.value < best) { // if this child has the best value so far, update best
				best = child_result.value;
				best_result = child_result;
			}
			if (best <= alpha) // prune if best is no greater than alpha
				return best_result;
			beta = Math.min(beta, best); // set beta
		}
		return best_result; // return the best result
	}
	
	// Checks if a move was a capture based on the state before and after the move
	private static boolean was_capture(State state) {
		return state.board.countPieces() < state.previous.board.countPieces();
	}
	
	/*
	 * Evaluates the board state. Positive values are good for white while negative values are good
	 * for black. As it's written, eval_player() isn't the most efficient way to evaluate the board, 
	 * but it's straightforward and readable with negligible performance loss from a practical 
	 * perspective. 
	 */
	private static double eval_board(State state) {
		if (state.over && state.movesUntilDraw == 0) // if the game is a draw
			return 0;
		else if (state.over && !state.check) // if the game is a stalemate
			return 0;
		else if (state.over && state.player == Player.BLACK && state.check) // if white wins
			return 1000;
		else if (state.over && state.player == Player.WHITE && state.check) // if black wins
			return -1000;
		else // otherwise, subtract black's score from white's
			return eval_player(state, Player.WHITE)-eval_player(state, Player.BLACK);
	}
	
	// Evaluates a player's position given the board state. Higher values are better. 
	private static double eval_player(State state, Player player) {
		double score = 0;
		// Sums up the material value of the player's pieces, with special handling for pawns
		for(Piece piece : state.board)
			if(piece.player == player) { // only consider the player's pieces
				score += value(piece);
				if (piece instanceof Pawn) // if the piece is a pawn, account for pawn structure
					score -= .5*eval_pawn(state.board, piece);
			}
		// return the score, accounting for the number of moves that can be made
		// intuition: having more potential moves is generally a good sign
		return score+(.1*state.countDescendants());
	}
	
	/*
	 * Evaluates the structure of a single pawn. Presently, it only looks for double pawns. 
	 * However, this is written such that it can be easily added to in the future. That said, 
	 * it's likely best to ditch the pawn-by-pawn approach and consider all pawns at once. 
	 * Nevertheless, I plan to expand and improve this in the future. 
	 */
	private static double eval_pawn(Board board, Piece pawn) {
		if (is_double_pawn(board, pawn)) // if it's a double pawn
			return 1;
		else
			return 0;
	}
	
	/*
	 * Determines if a pawn is a double pawn by checking if there is a pawn of like color 
	 * "in front of" it. So, if there are at least two pawns of like color in the same file, 
	 * this function will return true for all but the most advanced one. This is to prevent
	 * double counting. 
	 */
	private static boolean is_double_pawn(Board board, Piece pawn) {
		// calculate the rank directly "in front of" the pawn
		int rank;
		if (pawn.player == Player.WHITE) // white pawns advance to higher ranks
			rank = pawn.rank+1;
		else // black pawns advance to lower ranks
			rank = pawn.rank-1;
		while (rank < 9 && rank > 0) { // for each valid rank "in front of" the pawn
			Piece in_front = board.getPieceAt(pawn.file, rank);
			// if the current in_front piece being considered is also a pawn
			if (in_front instanceof Pawn)
				// return true if it's of the same color as the original pawn
				if (in_front.player == pawn.player)
					return true;
			
			if (pawn.player == Player.WHITE) // increment the rank to check for white pawns
				rank += 1;
			else // decrement the rank to check for black pawns
				rank -= 1;
		}
		return false; // return false if a double pawn is not detected
	}

	// Gets the value of a given piece
	private static double value(Piece piece) {
		if(piece instanceof Pawn)
			return 1;
		else if(piece instanceof Knight)
			return 3;
		else if(piece instanceof Bishop)
			return 3;
		else if(piece instanceof Rook)
			return 5;
		else if(piece instanceof Queen)
			return 9;
		else // if the piece is the King
			return 100;
	}
}
