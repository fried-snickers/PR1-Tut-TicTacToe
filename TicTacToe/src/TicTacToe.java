import java.util.Random;
import java.util.Scanner;

//represents available players. If cpuMode is active, CPU will be PLAYER_A
enum Players {PLAYER_A, PLAYER_B};

//represents game results states
enum Results {PLAYER_A_WINS, PLAYER_B_WINS, DRAW, UNDETERMINED};

/*
 * Class that represents the entire game of TicTacToe.
 * Supports PvP and PvC
 */
public class TicTacToe {

    /*---class attributes---*/

    private char[] board;
    private boolean cpuMode;
    private Random rand;
    private Scanner sc;
    private char playerASymbol;
    private char playerBSymbol;
    private Results result;

    /*
     * Constructor 
     */
    public TicTacToe(final boolean cpuMode, final char playerASymbol, final char playerBSymbol) {
        /*---initialise board---*/

        this.board = new char[9];
        for(int fieldIndex = 0; fieldIndex < this.board.length; fieldIndex++)
            this.board[fieldIndex] = ' ';

        /*---initialise random number generator in case cpuMode is active---*/
        if(cpuMode)
            this.rand = new Random();

        /*---initialise the rest---*/

        this.cpuMode = cpuMode;
        this.sc = new Scanner(System.in);
        this.playerASymbol = playerASymbol;
        this.playerBSymbol = playerBSymbol;
        this.result = Results.UNDETERMINED;
    }

    /*
     * override inherited toString method for easy printing of board
     */
    @Override
    public String toString() {
        String boardLayout = "";
        for(int i = 1; i < this.board.length+1; i++) {
            char field = this.board[i-1];
            boardLayout += " [" + field + "] ";
            if(i % 3 == 0)
                boardLayout += "\n";
        }
        return boardLayout;
    }

    /*
     * main function of class: play game
     */
    public void playGame() {
        boolean continuePlaying = true;
        byte iterationCounter = 0;
        int playerAWinCounter = 0;
        int playerBWinCounter = 0;
        int drawCounter = 0;
        int roundCounter = 1;
        boolean cpuBegins = true;
        int latestPlayedIndex;
        String[] yesNoVariants = {"y", "Y", "n", "N", "yes", "no", "YES", "NO"};

        System.out.println("Welcome to TicTacToe!");
        System.out.println("Fields are enumerated like this:\n[1] [2] [3]\n[4] [5] [6]\n[7] [8] [9]\n");

        /*---determine if player begins in case cpuMode is active---*/

        if(this.cpuMode) {

            String query = "\nDo you want to begin? [y/n]";
            String input = getValidInput(query, yesNoVariants);
            if(input.equals("y"))
                cpuBegins = false;
        }
        
        //controls continuation of game after a round (play game - ask if user wants to play again)
        do {
            System.out.println("\n\nLet's play! This is round " + roundCounter);

            //if cpu begins, increment roundCounter because CPU is PLAYER_A
            if(this.cpuMode && !cpuBegins && (iterationCounter == 0))
                iterationCounter++;

            //controls continuation of a single round (player move - print board - evaluate result)
            do {
                /*---cpu or player move---*/

                if(iterationCounter % 2 == 0) 
                    latestPlayedIndex = playMove(Players.PLAYER_A);
                else 
                    latestPlayedIndex = playMove(Players.PLAYER_B);
                
                //alternatively: latestPlayerIndex = playMove((iterationCounter%2 == 0) ? Players.PLAYER_A : Players.PLAYER_B);
                
                iterationCounter++;

                //print board for intermediate result
                System.out.println(this);

                /*---evaluate board---*/

                this.result = checkResultStatus(latestPlayedIndex);
                switch(this.result) {
                    case PLAYER_A_WINS:
                        System.out.println("Congratulations, Player A, you won!");
                        playerAWinCounter++;
                        this.result = Results.PLAYER_A_WINS;
                        break;
                    case PLAYER_B_WINS:
                        System.out.println("Congratulations, Player B, you won!");
                        playerBWinCounter++;
                        this.result = Results.PLAYER_B_WINS;
                        break;
                    case DRAW:
                        System.out.println("Draw!");
                        drawCounter++;
                        this.result = Results.DRAW;
                        break;
                    case UNDETERMINED:
                        break;
                }
            } while(this.result == Results.UNDETERMINED);
            
            /*---reset game, but remember # of rounds played---*/
            
            iterationCounter = 0;
            roundCounter++;

            /*---reset field---*/

            for(int fieldIndex = 0; fieldIndex < this.board.length; fieldIndex++) {
                this.board[fieldIndex] = ' ';
            }

            /*---ask player if he wants to play another game---*/

            String query = "\n\nDo you want to play again? [y/n]";
            String input = getValidInput(query, yesNoVariants);
            if(input.equals("n"))
                continuePlaying = false;
            else {
                query = "Do you want to play against the computer(1) or against a friend(2)? [1/2]";
                input = getValidInput(query,"1", "2");
                this.cpuMode = Integer.parseInt(input) == 1 ? true : false;
            }

        } while(continuePlaying);

        /*---print statistics---*/

        System.out.println("\n\nHere's your statistics:");
        System.out.println("Rounds played: \t" + (roundCounter-1)); //-1 because roundCounter is incremented after each round, even if the game is discontinued
        System.out.println("Draw: \t\t" + drawCounter);
        System.out.println("Win Player A: \t" + playerAWinCounter);
        System.out.println("Win Player B: \t" + playerBWinCounter);
        System.out.println("\nGoodbye!");
    }

    private int playMove(final Players player) {
        //represents index of latest played field
        int fieldNumberIndex;

        /*---in case cpuMode is active---*/

        if(this.cpuMode && player == Players.PLAYER_A) {
            fieldNumberIndex = this.rand.nextInt(9);
            
            while (this.board[fieldNumberIndex] != ' ')
                fieldNumberIndex = this.rand.nextInt(9);
            
            this.board[fieldNumberIndex] = this.playerASymbol;
            return fieldNumberIndex;
        }

        /*---in case cpuMode is inactive---*/

        String query = "Enter field number: ";
        String[] numericalOptions = new String[9];
        for(int i = 1; i < 10; i++)
            numericalOptions[i-1] = ""+i;
        String input = getValidInput(query, numericalOptions);
        fieldNumberIndex = Integer.parseInt(input) - 1;
        this.board[fieldNumberIndex] = player == Players.PLAYER_A ? this.playerASymbol : this.playerBSymbol;
        System.out.println();
        return fieldNumberIndex;
    }

    private Results checkResultStatus(final int index) {
        //determine which player just placed their symbol
        Players player = (this.board[index] == playerASymbol) ? Players.PLAYER_A : Players.PLAYER_B;

        //if there's a win, return current player 
        if(checkHorizontally(index) || checkVertically(index) || checkDiagonally(index)) 
            return player == Players.PLAYER_A ? Results.PLAYER_A_WINS : Results.PLAYER_B_WINS;

        //if there is still empty fields, current result is draw, otherwise it's still undetermined 
        for(char field : this.board) 
            if(field == ' ')
                return Results.UNDETERMINED;
        return Results.DRAW;
    }

    private boolean checkHorizontally(final int fieldIndex) {
        //first row
        if(fieldIndex < 3) 
            return this.board[0] == this.board[1] && this.board[0] == this.board[2];
            //return checkTriple(0, 1, 2);
        
        //second row
        if(fieldIndex < 6)
            return this.board[3] == this.board[4] && this.board[3] == this.board[5];
            //return checkTriple(3, 4, 5);
        
        //third row
        else
            return this.board[6] == this.board[7] && this.board[6] == this.board[8];
            //return checkTriple(6, 7, 8);

    }

    private boolean checkVertically(final int fieldIndex) {
        //first column
        if(fieldIndex % 3 == 0)
            return this.board[0] == this.board[3] && this.board[0] == this.board[6];
            //return checkTriple(0, 3, 6);

        //second column
        if (fieldIndex % 3 == 1)
            return this.board[1] == this.board[4] && this.board[1] == this.board[7];
            //return checkTriple(1, 4, 7);

        //third column
        else
            return this.board[2] == this.board[5] && this.board[2] == this.board[8];
            //return checkTriple(2, 5, 8);
    }

    private boolean checkDiagonally(final int fieldIndex) {
        return checkTriple(fieldIndex, 2, 4, 6) || checkTriple(fieldIndex, 0, 4, 8);
    }

    private boolean checkTriple(final int fieldIndex, final int... triple) {
        char symbol = this.board[fieldIndex];
        for(int index : triple)
            if(this.board[index] != symbol)
                return false;
        return true;

        // alternatively: return this.board[triple[0]] == this.board[triple[1]] && this.board[triple[0]] == this.board[triple[2]];
    }

    private String getValidInput(final String query, final String ... availableOptions) {
        boolean validInput = false;
        String input;
        do {
            System.out.println(query);
            input = this.sc.nextLine();
            for(String option : availableOptions) {
                if(input.equals(option)) {
                    validInput = true;
                    break;
                }
            }
        } while(!validInput);

        //shortcut for yes-no-questions
        switch(input) {
            case "y":
            case "Y":
            case "yes":
            case "YES": 
                return "y";
            case "n":
            case "N":
            case "no":
            case "NO":
                return "n";
            default: 
                System.out.println();
                return input;
        }
    }

    public static void main(String[] args) {
        new TicTacToe(Boolean.parseBoolean(args[0]), 'x', 'o').playGame();
    }
}
