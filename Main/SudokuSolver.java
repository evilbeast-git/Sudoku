import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SudokuSolver extends JFrame {
    private static final int N = 9;
    private JTextField[][] cells = new JTextField[N][N];
    private JButton startButton;
    private JButton submitButton;
    private JButton pausePlayButton;
    private JButton solutionButton;
    private JButton checkButton;
    private JLabel timerLabel;
    private Timer timer;
    private int secondsElapsed;
    private int[][] initialBoard = new int[N][N];
    private int[][] currentPuzzle = new int[N][N];
    private boolean timerRunning = false;
    private Random random = new Random();
    private int vacantCells;
    private int score;
    private JLabel scoreLabel;


    public SudokuSolver() {
        setTitle("Sudoku");
        setSize(700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());

        timerLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
        timerLabel.setVisible(false);
        topPanel.add(timerLabel, BorderLayout.CENTER);

        pausePlayButton = new JButton("Pause");
        pausePlayButton.setVisible(false);
        topPanel.add(pausePlayButton, BorderLayout.WEST);

        scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);
        // topPanel.add(scoreLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(N, N));
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 30));
                setBorders(row, col, cells[row][col]);
                boardPanel.add(cells[row][col]);
            }
        }
        add(boardPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Controls: "));
        submitButton = new JButton("Submit");
        solutionButton = new JButton("Solution");
        checkButton = new JButton("Check");
        controlPanel.add(submitButton);
        controlPanel.add(solutionButton);
        controlPanel.add(checkButton);
        submitButton.setVisible(false);
        solutionButton.setVisible(false);
        checkButton.setVisible(false);
        add(controlPanel, BorderLayout.SOUTH);

        setupActionListeners();
        showInitialDialog();
    }

    private void setupActionListeners() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitSolution();
            }
        });

        solutionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displaySolution();
            }
        });

        pausePlayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePausePlay();
            }
        });

        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSolution();
            }
        });
    }

    private void setBorders(int row, int col, JTextField cell) {
        Border border = BorderFactory.createMatteBorder(
                row % 3 == 0 ? 3 : 1,
                col % 3 == 0 ? 3 : 1,
                row % 3 == 2 ? 3 : 1,
                col % 3 == 2 ? 3 : 1,
                Color.DARK_GRAY
        );
        cell.setBorder(border);
    }

    private void resetBoard() {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                cells[row][col].setText("");
                cells[row][col].setEditable(true);
                cells[row][col].setBackground(Color.WHITE);
            }
        }
        if (timer != null) {
            timer.cancel();
        }
        timerLabel.setText("Time: 0s");
        secondsElapsed = 0;
        timerRunning = false;
    }

    private void resetToInitialBoard() {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (currentPuzzle[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(currentPuzzle[row][col]));
                    cells[row][col].setEditable(false);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                }
                cells[row][col].setBackground(Color.WHITE);
            }
        }
        if (timer != null) {
            timer.cancel();
        }
        timerLabel.setText("Time: 0s");
        secondsElapsed = 0;
        timerRunning = true;
        startTimer();
    }

    private void generateSudoku() {
        resetBoard();
        score = 0; // Reset score
        scoreLabel.setText("Score: " + score);

        initialBoard = new int[N][N];
        fillDiagonal(initialBoard);
        solveSudoku(initialBoard);

        currentPuzzle = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(initialBoard[i], 0, currentPuzzle[i], 0, N);
        }

        removeNumbers(currentPuzzle);
        updateUIFromBoard(currentPuzzle);

        startTimer();
        timerLabel.setVisible(true);
        pausePlayButton.setVisible(true);

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (currentPuzzle[row][col] == 0) {
                    cells[row][col].setBackground(Color.BLACK);
                    cells[row][col].setForeground(Color.WHITE);
                } else {
                    cells[row][col].setBackground(Color.LIGHT_GRAY);
                    cells[row][col].setForeground(Color.BLACK);
                }
            }
        }

        submitButton.setVisible(true);
        solutionButton.setVisible(true);
        checkButton.setVisible(true);
    }

    private void startTimer() {
        secondsElapsed = 0;
        if (timer != null) {
            timer.cancel();
        }
        timerRunning = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timerRunning) {
                    timerLabel.setText("Time: " + (++secondsElapsed) + "s");
                }
            }
        }, 1000, 1000);
    }

    private void togglePausePlay() {
        if (timerRunning) {
            timerRunning = false;
            pausePlayButton.setText("Resume");

            JDialog pauseDialog = new JDialog(this, "Game Paused", true);
            pauseDialog.setSize(300, 100);
            pauseDialog.setLayout(new BorderLayout());

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton playButton = new JButton("Play");
            playButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    timerRunning = true;
                    pausePlayButton.setText("Pause");
                    pauseDialog.dispose();
                }
            });
            buttonPanel.add(playButton);

            JButton restartButton = new JButton("Restart");
            restartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int choice = JOptionPane.showConfirmDialog(
                            pauseDialog,
                            "Do you want to restart the game?",
                            "Confirm Restart",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        generateSudoku();
                        score = 0; // Reset score
                        scoreLabel.setText("Score: " + score);
                        timerRunning = true;
                        pausePlayButton.setText("Pause");
                        pauseDialog.dispose();
                    }
                }
            });
            buttonPanel.add(restartButton);

            JButton quitButton = new JButton("Quit");
            quitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int choice = JOptionPane.showConfirmDialog(
                            pauseDialog,
                            "Do you really want to exit?",
                            "Confirm Exit",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        showInitialDialog();
                        pauseDialog.dispose();
                        pausePlayButton.setText("Pause");
                    }
                    
                }
            });
            buttonPanel.add(quitButton);

            pauseDialog.add(buttonPanel, BorderLayout.CENTER);
            pauseDialog.setVisible(true);

        } else {
            timerRunning = true;
            pausePlayButton.setText("Pause");
        }
    }

    private void submitSolution() {
        checkSolution();
        int[][] userBoard = new int[N][N];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                String text = cells[row][col].getText();
                if (!text.equals("")) {
                    userBoard[row][col] = Integer.parseInt(text);
                    cells[row][col].setEditable(false);
                } else {
                    userBoard[row][col] = 0;
                }
            }
        }

        if (isBoardCorrect(userBoard)) {
            timer.cancel();
            displayEndGameOptions(true);
        } else {
            displayEndGameOptions(false);
        }
    }

    private void displayEndGameOptions(boolean isCorrect) {
        Object[] options;
        String message;
        if (isCorrect) {
            options = new Object[]{"New Game", "Quit"};
            message = "Congrats! You solved the puzzle.\nScore: " + score;
        } else {
            options = new Object[]{"Retry", "Quit"};
            message = "The solution is incorrect. Please try again.\nScore: " + score;
        }
    
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                isCorrect ? "Puzzle Solved" : "Incorrect Solution",
                JOptionPane.YES_NO_OPTION,
                isCorrect ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]
        );
    
        if (isCorrect) {
            if (choice == JOptionPane.YES_OPTION) {
                generateSudoku();
            } else if (choice == JOptionPane.NO_OPTION) {
                showInitialDialog();
            }
        } else {
            if (choice == JOptionPane.YES_OPTION) {
                retryIncorrectSolution();
            } else if (choice == JOptionPane.NO_OPTION) {
                showInitialDialog();
            }
        }
    }
    
    
    private void retryIncorrectSolution() {
        Object[] options = {"Yes", "No"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Retry with the same puzzle?",
                "Retry",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.YES_OPTION) {
            updateUIFromBoard(currentPuzzle);
            startTimer();
        } else if (choice == JOptionPane.NO_OPTION) {
            generateSudoku();
        }
    }

    private void showExitDialog() {
        Object[] options = {"Continue", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Do you really want to exit?",
                "Confirm Exit",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    private boolean isBoardCorrect(int[][] board) {
        for (int i = 0; i < N; i++) {
            if (!isValidRow(board, i) || !isValidColumn(board, i) || !isValidSubBox(board, i)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidRow(int[][] board, int row) {
        boolean[] seen = new boolean[10];
        for (int col = 0; col < N; col++) {
            int num = board[row][col];
            if (num != 0) {
                if (seen[num]) {
                    return false;
                }
                seen[num] = true;
            }
        }
        return true;
    }

    private boolean isValidColumn(int[][] board, int col) {
        boolean[] seen = new boolean[10];
        for (int row = 0; row < N; row++) {
            int num = board[row][col];
            if (num != 0) {
                if (seen[num]) {
                    return false;
                }
                seen[num] = true;
            }
        }
        return true;
    }

    private boolean isValidSubBox(int[][] board, int box) {
        int startRow = (box / 3) * 3;
        int startCol = (box % 3) * 3;
        boolean[] seen = new boolean[10];
        for (int row = startRow; row < startRow + 3; row++) {
            for (int col = startCol; col < startCol + 3; col++) {
                int num = board[row][col];
                if (num != 0) {
                    if (seen[num]) {
                        return false;
                    }
                    seen[num] = true;
                }
            }
        }
        return true;
    }

    private void updateUIFromBoard(int[][] board) {
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (board[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(board[row][col]));
                    cells[row][col].setEditable(false);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                }
            }
        }
    }

    private boolean isSafe(int[][] grid, int row, int col, int num) {
        // Check the row
        for (int x = 0; x < N; x++) {
            if (grid[row][x] == num) {
                return false;
            }
        }

        // Check the column
        for (int x = 0; x < N; x++) {
            if (grid[x][col] == num) {
                return false;
            }
        }

        // Check the 3x3 subgrid
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i + startRow][j + startCol] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean solveSudoku(int[][] grid) {
        int row = -1, col = -1;
        boolean isEmpty = true;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (grid[i][j] == 0) {
                    row = i;
                    col = j;
                    isEmpty = false;
                    break;
                }
            }
            if (!isEmpty) {
                break;
            }
        }

        // No empty space left
        if (isEmpty) {
            return true;
        }

        for (int num = 1; num <= 9; num++) {
            if (isSafe(grid, row, col, num)) {
                grid[row][col] = num;
                if (solveSudoku(grid)) {
                    return true;
                }
                grid[row][col] = 0; // Backtrack
            }
        }

        return false; // Triggers backtracking
    }

    private void fillDiagonal(int[][] grid) {
        for (int i = 0; i < N; i += 3) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    int num;
                    do {
                        num = random.nextInt(9) + 1;
                    } while (!isSafe(grid, i + j, i + k, num));
                    grid[i + j][i + k] = num;
                }
            }
        }
    }

    private void removeNumbers(int[][] board) {
        int cellsToRemove = vacantCells;
        while (cellsToRemove > 0) {
            int row = random.nextInt(N);
            int col = random.nextInt(N);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                cellsToRemove--;
            }
        }
    }

    private void displaySolution() {
        int[][] solution = new int[N][N];

        for (int i = 0; i < N; i++) {
            System.arraycopy(currentPuzzle[i], 0, solution[i], 0, N);
        }

        solveSudoku(solution);
        updateUIFromBoard(solution);
    }

    public static int getRandomNumber(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Max must be greater than or equal to min");
        }
        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;
    }


    private void selectDifficultyLevel() {
        Object[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Difficulty Level",
                "Difficulty Level",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { // Easy
            vacantCells = getRandomNumber(10, 17);
        } else if (choice == 1) { // Medium
            vacantCells = getRandomNumber(30, 40);
        } else if (choice == 2) { // Hard
            vacantCells = getRandomNumber(55, 60);
        } else {
            return;
        }

        generateSudoku();
    }
    private void checkAllCells() {
        boolean isAllCorrect = true;
        timerRunning = false;
    
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (currentPuzzle[row][col] == 0) { // Check only vacant cells
                    String userInput = cells[row][col].getText().trim();
                    if (!userInput.isEmpty()) {
                        try {
                            int userNumber = Integer.parseInt(userInput);
                            boolean isValid = isValidNumber(currentPuzzle, row, col, userNumber);
    
                            if (!isValid) {
                                cells[row][col].setBackground(Color.RED);
                                isAllCorrect = false;
                            } else {
                                cells[row][col].setBackground(Color.GREEN);
                            }
                        } catch (NumberFormatException ex) {
                            cells[row][col].setBackground(Color.RED);
                            isAllCorrect = false;
                        }
                    }
                }
            }
        }
    
        if (isAllCorrect) {
            JOptionPane.showMessageDialog(this, "Every number is correct!", "All Correct", JOptionPane.INFORMATION_MESSAGE);
            for(int row = 0; row < N; row++){
                for(int col = 0; col < N; col++){
                    if (currentPuzzle[row][col]==0){
                        String userInput = cells[row][col].getText().trim();
                        cells[row][col].setBackground(Color.BLACK);
                    }
                }
            }
            timerRunning = true;
        } else {
            JOptionPane.showMessageDialog(this, "Some Number are Incorrect. Please check again.", "Incorrect Cells", JOptionPane.WARNING_MESSAGE);
            for(int row = 0; row < N; row++){
                for(int col = 0; col < N; col++){
                    if (currentPuzzle[row][col]==0){
                        String userInput = cells[row][col].getText().trim();
                        cells[row][col].setBackground(Color.BLACK);
                    }
                }
            }
            timerRunning = true;
        }
    }

    private void checkSolution() {
        checkAllCells();
        boolean isCorrect = true;

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                String text = cells[row][col].getText();
                if (currentPuzzle[row][col] == 0) { // Only check vacant cells
                    if (!text.isEmpty() && Integer.parseInt(text) == initialBoard[row][col]) {
                        updateScore(10); // Increase score for each correct cell
                    } else {
                        isCorrect = false;
                        updateScore(-3); // Decrease score for each incorrect cell
                    }
                }
            }
        }

        scoreLabel.setText("Score: " + score);

    }
    
    private void updateScore(int points) {
        score += points;
        scoreLabel.setText("Score: " + score);
    }

    private boolean isValidNumber(int[][] board, int row, int col, int num) {
        // Check if the number is already in the row
        for (int c = 0; c < N; c++) {
            if (board[row][c] == num && c != col) {
                return false;
            }
        }

        // Check if the number is already in the column
        for (int r = 0; r < N; r++) {
            if (board[r][col] == num && r != row) {
                return false;
            }
        }

        // Check if the number is already in the 3x3 sub-box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (board[r][c] == num && (r != row || c != col)) {
                    return false;
                }
            }
        }

        return true;
    }
    
    private void showInitialDialog() {
        resetBoard(); // Reset the board and state
        score = 0; // Reset score
        scoreLabel.setText("Score: " + score); 
    
        // Create a modal dialog
        JDialog initialDialog = new JDialog(this, "Welcome", true);
        initialDialog.setSize(300, 100);
        initialDialog.setLayout(new BorderLayout());
    
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDifficultyLevel();
                initialDialog.dispose();
            }
        });
        buttonPanel.add(startButton);
    
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonPanel.add(exitButton);
    
        initialDialog.add(new JLabel("Welcome to Sudoku Solver!"), BorderLayout.CENTER);
        initialDialog.add(buttonPanel, BorderLayout.SOUTH);
        initialDialog.setLocationRelativeTo(null);
        initialDialog.setVisible(true);
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SudokuSolver().setVisible(true);
            }
        });
    }
}
