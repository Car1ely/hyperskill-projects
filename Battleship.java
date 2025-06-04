
import java.util.Scanner;

class Battleship {
    static final int boardSize = 10;
    static final String[][] player1Board = new String[boardSize][boardSize];
    static final String[][] player2Board = new String[boardSize][boardSize];
    static final String[][] player1Fog = new String[boardSize][boardSize];
    static final String[][] player2Fog = new String[boardSize][boardSize];
    //this is 4 boards for 2 players(1st with placed ships, 2nd with fog (player view))
    enum Ship {
        aircraftCarrier("Aircraft Carrier", 5),
        battleship("Battleship", 4),
        submarine("Submarine", 3),
        cruiser("Cruiser", 3),
        destroyer("Destroyer", 2);
        // Using enum is good practice because it stores related constants(name and size) together
        // makes it easy to loop through all ship types.

        private final String name;
        private final int size;

        Ship(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }
    }
    //standard getters and setters
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Player 1, place your ships on the game field");
        initializeBoards(player1Board, player1Fog); //filling board
        printBoard(player1Board); //printing
        placeAllShips(scanner, player1Board);
        promptNextPlayer(scanner); //changing turn

        System.out.println("Player 2, place your ships on the game field");
        initializeBoards(player2Board, player2Fog);
        printBoard(player2Board);
        placeAllShips(scanner, player2Board);
        promptNextPlayer(scanner);

        boolean player1Turn = true;

        while (true) {
            String[][] currentFog = player1Turn ? player1Fog : player2Fog;
            String[][] currentBoard = player1Turn ? player1Board : player2Board;
            String[][] opponentBoard = player1Turn ? player2Board : player1Board;
            //if current = 1st, so 1st board printed
            printBoard(currentFog);
            System.out.println("---------------------");
            printBoard(currentBoard);
            System.out.println("Player " + (player1Turn ? "1" : "2") + ", it's your turn:");

            while (true) {
                String shotInput = scanner.nextLine().trim();
                int[] shotCoord;
                try {
                    shotCoord = parseCoordinate(shotInput);
                } catch (Exception e) {
                    System.out.println("Error! You entered the wrong coordinates. Try again:");
                    continue;
                }
                //our shot input coord is divided by 2 parts (row and col)
                int shotRow = shotCoord[0];
                int shotCol = shotCoord[1];

                if (shotRow < 0 || shotRow >= boardSize || shotCol < 0 || shotCol >= boardSize) {
                    System.out.println("Error! You entered the wrong coordinates. Try again:");
                    continue;
                }

                if (opponentBoard[shotRow][shotCol].equals("O")) {
                    opponentBoard[shotRow][shotCol] = "X"; //changing O to X when ship getting a shot
                    currentFog[shotRow][shotCol] = "X"; //same logic but on fog board
                    if (isShipSunkAt(opponentBoard, shotRow, shotCol)) {
                        if (!shipsNotSunk(opponentBoard)) {
                            System.out.println("You sank the last ship. You won. Congratulations!"); //when it was the last ship
                            break;
                        } else {
                            System.out.println("You sank a ship!"); //when other ships are not sunk yet
                        }
                    } else {
                        System.out.println("You hit a ship!");
                    }
                } else {
                    if (!currentFog[shotRow][shotCol].equals("X") && !currentFog[shotRow][shotCol].equals("M")) {
                        currentFog[shotRow][shotCol] = "M"; //miss logic, changing ~ to M
                        opponentBoard[shotRow][shotCol] = "M";
                    }
                    System.out.println("You missed!");
                }
                break;
            }

            promptNextPlayer(scanner);
            player1Turn = !player1Turn;
        }
    }

    static void placeAllShips(Scanner scanner, String[][] board) {
        for (Ship ship : Ship.values()) {
            while (true) {
                System.out.println("Enter the coordinates of the " + ship.getName() + " (" + ship.getSize() + " cells):");
                String input = scanner.nextLine().trim();
                String[] coordinates = input.split(" ");

                //messages for exceptions and errors
                if (coordinates.length != 2) {
                    System.out.println("Error! Invalid input. Try again:");
                    continue;
                }

                int[] start = parseCoordinate(coordinates[0]);
                int[] end = parseCoordinate(coordinates[1]);

                if (!isStraightLine(start, end)) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    continue;
                }

                int actualLength = calculateLength(start, end);
                if (actualLength != ship.getSize()) {
                    System.out.println("Error! Wrong length of the " + ship.getName() + "! Try again:");
                    continue;
                }

                if (isTooCloseToOtherShips(start, end, board)) {
                    System.out.println("Error! You placed it too close to another one. Try again:");
                    continue;
                }

                placeShip(start, end, board);
                printBoard(board);
                break;
            }
        }
    }

    static void initializeBoards(String[][] board, String[][] fog) {
        for (int row = 0; row < boardSize; row++) { //filling board with ~
            for (int col = 0; col < boardSize; col++) {
                board[row][col] = "~";
                fog[row][col] = "~";
            }
        }
    }

    static void printBoard(String[][] board) {
        System.out.print("  ");
        for (int i = 1; i <= boardSize; i++) { //printing line of numbers
            System.out.print(i + " ");
        }
        System.out.println();
        for (int row = 0; row < boardSize; row++) { //printing line of letters
            char rowLabel = (char) ('A' + row);
            System.out.print(rowLabel + " ");
            for (int col = 0; col < boardSize; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
    }

    static int[] parseCoordinate(String input) { //here we are splitting coordinates by col and rows
        int row = input.charAt(0) - 'A';    //e.g   A2            
        int column = Integer.parseInt(input.substring(1)) - 1;
        return new int[]{row, column};
    }

    static boolean isStraightLine(int[] start, int[] end) {
        return start[0] == end[0] || start[1] == end[1];
    }

    static int calculateLength(int[] start, int[] end) {
        return Math.abs(start[0] - end[0]) + Math.abs(start[1] - end[1]) + 1;
    } //here we are getting length through subtracting min value from max value, +1 to count first cell

    static boolean isTooCloseToOtherShips(int[] start, int[] end, String[][] board) {
        int rowStart = Math.max(0, Math.min(start[0], end[0]) - 1);
        int rowEnd = Math.min(boardSize - 1, Math.max(start[0], end[0]) + 1);
        int colStart = Math.max(0, Math.min(start[1], end[1]) - 1);
        int colEnd = Math.min(boardSize - 1, Math.max(start[1], end[1]) + 1);
        // create a "frame" around the ship to check if there are other ships nearby
        for (int row = rowStart; row <= rowEnd; row++) {
            for (int col = colStart; col <= colEnd; col++) {
                if (board[row][col].equals("O")) {
                    return true; //if cell located near = O, you can not place
                }
            }
        }
        return false;
    }

    static void placeShip(int[] start, int[] end, String[][] board) {
        if (start[0] == end[0]) {
            for (int col = Math.min(start[1], end[1]); col <= Math.max(start[1], end[1]); col++) {
                board[start[0]][col] = "O"; //e.g board[2][1], board[2][2], board[2][3]
            }
        } else {
            for (int row = Math.min(start[0], end[0]); row <= Math.max(start[0], end[0]); row++) {
                board[row][start[1]] = "O"; // same but vertical!
            }
        }
    }

    static boolean shipsNotSunk(String[][] board) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (board[row][col].equals("O")) { //not all ships sunk if there are any O on the board
                    return true;                   //it means that game is not finished
                }
            }
        }
        return false;
    }

    static boolean isShipSunkAt(String[][] board, int hitRow, int hitCol) {
        int[][] directions = {{-1,0},{1,0},{0,-1},{0,1}}; // up, down, left, right
        for (int[] dir : directions) {
            int r = hitRow + dir[0]; //checking vertically
            int c = hitCol + dir[1]; //checking horizontally
            while (r >= 0 && r < boardSize && c >= 0 && c < boardSize) { //to avoid going out of bounds
                if (board[r][c].equals("O")) return false; //not sunk yet
                if (board[r][c].equals("~") || board[r][c].equals("M")) break;
                r += dir[0];
                c += dir[1];
            }
        }
        return true;
    }

    static void promptNextPlayer(Scanner scanner) { //just giving a message about changing turn
        System.out.println("Press Enter and pass the move to another player");
        scanner.nextLine();
    }
}
