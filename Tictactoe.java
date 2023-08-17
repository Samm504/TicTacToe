import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.lang.Math;

class TreeNode {
    Board board;
    boolean isTerminal;
    boolean isFullyExpanded;
    TreeNode parent;
    int visits;
    double score;
    Map<String, TreeNode> children;

    public TreeNode(Board board, TreeNode parent) {
        this.board = board;
        if (this.board.isWin() || this.board.isDraw()) {
            this.isTerminal = true;
        } else {
            this.isTerminal = false;
        }
        this.isFullyExpanded = this.isTerminal;
        this.parent = parent;
        this.visits = 0;
        this.score = 0;
        this.children = new HashMap<>();
    }
}

class MCTS {
    TreeNode root;
    final int iterations = 1000;
    final double explorationConstant = 2.0;

    public Board search(Board initialBoard) {
        root = new TreeNode(initialBoard, null);

        for (int iteration = 0; iteration < iterations; iteration++) {
            TreeNode node = select(root);
            double score = rollout(node.board);
            backpropagate(node, score);
        }

        return getBestMove(root, 0).board;
    }

    private TreeNode select(TreeNode node) {
        while (!node.isTerminal) {
            if (node.isFullyExpanded) {
                node = getBestMove(node, explorationConstant);
            } else {
                return expand(node);
            }
        }
        return node;
    }

    private TreeNode expand(TreeNode node) {
        List<Board> states = node.board.generateStates();
        for (Board state : states) {
            if (!node.children.containsKey(state.getPosition())) {
                TreeNode newNode = new TreeNode(state, node);
                node.children.put(state.getPosition(), newNode);
                if (states.size() == node.children.size()) {
                    node.isFullyExpanded = true;
                }
                return newNode;
            }
        }
        System.out.println("Should not get here!!!");
        return null;
    }

    private double rollout(Board board) {
        while (!board.isWin()) {
            try {
                List<Board> states = board.generateStates();
                board = states.get(new Random().nextInt(states.size()));
            } catch (Exception e) {
                return 0;
            }
        }
        return (board.getPlayer2() == 'x') ? 1 : -1;
    }

    private void backpropagate(TreeNode node, double score) {
        while (node != null) {
            node.visits++;
            node.score += score;
            node = node.parent;
        }
    }

    private TreeNode getBestMove(TreeNode node, double explorationConstant) {
        double bestScore = Double.NEGATIVE_INFINITY;
        List<TreeNode> bestMoves = new ArrayList<>();

        for (TreeNode childNode : node.children.values()) {
            int currentPlayer = (childNode.board.getPlayer2() == 'x') ? 1 : -1;
            double moveScore = currentPlayer * childNode.score / childNode.visits
                    + explorationConstant * Math.sqrt(Math.log(node.visits) / childNode.visits);

            if (moveScore > bestScore) {
                bestScore = moveScore;
                bestMoves.clear();
                bestMoves.add(childNode);
            } else if (moveScore == bestScore) {
                bestMoves.add(childNode);
            }
        }

        return bestMoves.get(new Random().nextInt(bestMoves.size()));
    }
}

class Board {
    char player1;
    char player2;
    char emptySquare;
    Map<String, Character> position;

    public Board() {
        player1 = 'x';
        player2 = 'o';
        emptySquare = '.';
        position = new HashMap<>();
        initBoard();
    }

    public char getPlayer1() {
        return player1;
    }

    public char getPlayer2() {
        return player2;
    }

    public Board(Board board) {
        player1 = board.player1;
        player2 = board.player2;
        emptySquare = board.emptySquare;
        position = new HashMap<>(board.position);
    }

    private void initBoard() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                position.put(row + "," + col, emptySquare);
            }
        }
    }

    public Board makeMove(int row, int col) {
        Board newBoard = new Board(this);
        newBoard.position.put(row + "," + col, player1);
        char temp = newBoard.player1;
        newBoard.player1 = newBoard.player2;
        newBoard.player2 = temp;
        return newBoard;
    }

    public boolean isDraw() {
        for (String key : position.keySet()) {
            if (position.get(key) == emptySquare) {
                return false;
            }
        }
        return true;
    }

    public boolean isWin() {
        // Vertical sequence detection
        for (int col = 0; col < 3; col++) {
            char lastSymbol = position.get("0," + col);
            int count = (lastSymbol == emptySquare) ? 0 : 1;
            for (int row = 1; row < 3; row++) {
                char currentSymbol = position.get(row + "," + col);
                if (currentSymbol == lastSymbol) {
                    count++;
                }
                if (count == 3) {
                    return true;
                }
                lastSymbol = currentSymbol;
            }
        }

        // Horizontal sequence detection
        for (int row = 0; row < 3; row++) {
            char lastSymbol = position.get(row + ",0");
            int count = (lastSymbol == emptySquare) ? 0 : 1;
            for (int col = 1; col < 3; col++) {
                char currentSymbol = position.get(row + "," + col);
                if (currentSymbol == lastSymbol) {
                    count++;
                }
                if (count == 3) {
                    return true;
                }
                lastSymbol = currentSymbol;
            }
        }

        // Diagonal sequence detection
        char centerSymbol = position.get("1,1");
        if (centerSymbol != emptySquare) {
            if ((position.get("0,0") == centerSymbol && position.get("2,2") == centerSymbol) ||
                (position.get("0,2") == centerSymbol && position.get("2,0") == centerSymbol)) {
                return true;
            }
        }

        return false;
    }

    public List<Board> generateStates() {
        List<Board> actions = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (position.get(row + "," + col) == emptySquare) {
                    actions.add(makeMove(row, col));
                }
            }
        }
        return actions;
    }

    public String getPosition() {
        StringBuilder pos = new StringBuilder();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                pos.append(position.get(row + "," + col));
            }
        }
        return pos.toString();
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                boardString.append(" ").append(position.get(row + "," + col));
            }
            boardString.append("\n");
        }

        if (player1 == 'x') {
            boardString.insert(0, "\n--------------\n \"x\" to move:\n--------------\n\n");
        } else if (player1 == 'o') {
            boardString.insert(0, "\n--------------\n \"o\" to move:\n--------------\n\n");
        }

        return boardString.toString();
    }
}

public class TicTacToe {
    public static void main(String[] args) {
        Board board = new Board();
        MCTS mcts = new MCTS();

        System.out.println("\n  Tic Tac Toe by Code Monkey King\n");
        System.out.println("  Type \"exit\" to quit the game");
        System.out.println("  Move format [x,y]: 1,2 where 1 is column and 2 is row");
        System.out.println(board);

        while (true) {
            String userInput = System.console().readLine("> ");

            if (userInput.equals("exit")) {
                break;
            }

            if (userInput.isEmpty()) {
                continue;
            }

            try {
                int row = Integer.parseInt(userInput.split(",")[1]) - 1;
                int col = Integer.parseInt(userInput.split(",")[0]) - 1;

                if (board.position.get(row + "," + col) != board.emptySquare) {
                    System.out.println(" Illegal move!");
                    continue;
                }

                board = board.makeMove(row, col);
                System.out.println(board);

                Board bestMove = mcts.search(board);

                try {
                    board = bestMove;
                } catch (Exception ignored) {
                }

                System.out.println(board);

                if (board.isWin()) {
                    System.out.printf("player \"%s\" has won the game!\n", board.player2);
                    break;
                } else if (board.isDraw()) {
                    System.out.println("Game is drawn!\n");
                    break;
                }
            } catch (Exception e) {
                System.out.println("  Error: " + e);
                System.out.println("  Illegal command!");
                System.out.println("  Move format [x,y]: 1,2 where 1 is column and 2 is row");
            }
        }
    }
}
