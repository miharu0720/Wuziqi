package com.zstu.javalesson.wuziqi;

import java.util.ArrayList;
import java.util.List;

public class Agent implements WuziqiConsts {
    
    // AI是白棋（2），玩家是黑棋（1）
    private static final int AI_PIECE = 2;
    private static final int PLAYER_PIECE = 1;
    private static final int EMPTY = 0;
    
    // 搜索深度（困难难度使用）
    private static final int MAX_DEPTH = 3;
    
    // 棋型评分权重
    private static final int FIVE = 100000;      // 五连（胜利）
    private static final int LIVE_FOUR = 10000;  // 活四
    private static final int RUSH_FOUR = 1000;   // 冲四
    private static final int LIVE_THREE = 100;   // 活三
    private static final int LIVE_TWO = 10;      // 活二
    
    // 当前难度
    private Difficulty difficulty = Difficulty.HARD;
    
    /**
     * 设置AI难度
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * 获取AI的最佳落子位置
     */
    int[] getij(int[][] board, int count) throws QipanyimanException {
        if (count == WuziqiModel.ROWS * WuziqiModel.COLS - 1) {
            QipanyimanException e = new QipanyimanException();
            throw e;
        }
        
        // 开局策略：如果棋盘为空，选择中心位置
        if (count == 0) {
            int center = WuziqiModel.ROWS / 2;
            return new int[]{center, center};
        }
        
        // 根据难度选择不同的策略
        switch (difficulty) {
            case EASY:
                return getEasyMove(board);
            case MEDIUM:
                return getMediumMove(board);
            case HARD:
                return getHardMove(board);
            default:
                return getHardMove(board);
        }
    }
    
    /**
     * 简单难度：有一定策略，但比中等和困难简单
     */
    private int[] getEasyMove(int[][] board) {
        List<int[]> moves = getValidMoves(board);
        if (moves.isEmpty()) {
            return new int[]{WuziqiModel.ROWS / 2, WuziqiModel.COLS / 2};
        }
        
        // 1. 检查是否有自己的胜利机会（最高优先级）
        for (int[] move : moves) {
            int[][] testBoard = copyBoard(board);
            testBoard[move[0]][move[1]] = AI_PIECE;
            if (WuziqiModel.is5PieceConnected(testBoard, move[0], move[1])) {
                return move;
            }
        }
        
        // 2. 检查是否有必须防守的威胁（玩家能成五）
        for (int[] move : moves) {
            int[][] testBoard = copyBoard(board);
            testBoard[move[0]][move[1]] = PLAYER_PIECE;
            if (WuziqiModel.is5PieceConnected(testBoard, move[0], move[1])) {
                return move;
            }
        }
        
        // 3. 检查是否需要防守玩家的活四或冲四
        List<int[]> defensiveMoves = new ArrayList<>();
        for (int[] move : moves) {
            int playerValue = evaluatePositionForPiece(board, move[0], move[1], PLAYER_PIECE);
            if (playerValue >= RUSH_FOUR) { // 玩家的冲四或活四
                defensiveMoves.add(move);
            }
        }
        if (!defensiveMoves.isEmpty()) {
            // 从防守位置中随机选择一个
            int randomIndex = (int) (Math.random() * defensiveMoves.size());
            return defensiveMoves.get(randomIndex);
        }
        
        // 4. 检查是否有自己的活三或冲四可以形成
        List<int[]> offensiveMoves = new ArrayList<>();
        for (int[] move : moves) {
            int aiValue = evaluatePositionForPiece(board, move[0], move[1], AI_PIECE);
            if (aiValue >= LIVE_THREE) { // AI的活三、冲四或活四
                offensiveMoves.add(move);
            }
        }
        if (!offensiveMoves.isEmpty()) {
            // 从进攻位置中随机选择一个
            int randomIndex = (int) (Math.random() * offensiveMoves.size());
            return offensiveMoves.get(randomIndex);
        }
        
        // 5. 使用简单评分，选择得分较高的位置（但加入一些随机性）
        List<int[]> scoredMoves = new ArrayList<>();
        int maxScore = Integer.MIN_VALUE;
        for (int[] move : moves) {
            int aiValue = evaluatePositionForPiece(board, move[0], move[1], AI_PIECE);
            int playerValue = evaluatePositionForPiece(board, move[0], move[1], PLAYER_PIECE);
            int score = aiValue - playerValue / 2; // 考虑进攻和防守
            
            if (score > maxScore) {
                maxScore = score;
                scoredMoves.clear();
                scoredMoves.add(move);
            } else if (score == maxScore) {
                scoredMoves.add(move);
            }
        }
        
        // 从得分最高的位置中随机选择一个（增加一些随机性，保持简单模式的特性）
        if (!scoredMoves.isEmpty()) {
            int randomIndex = (int) (Math.random() * scoredMoves.size());
            return scoredMoves.get(randomIndex);
        }
        
        // 6. 如果都没有，随机选择
        int randomIndex = (int) (Math.random() * moves.size());
        return moves.get(randomIndex);
    }
    
    /**
     * 中等难度：使用评估函数，选择当前局面下得分最高的位置（不使用Minimax）
     */
    private int[] getMediumMove(int[][] board) {
        List<int[]> moves = getValidMoves(board);
        if (moves.isEmpty()) {
            return new int[]{WuziqiModel.ROWS / 2, WuziqiModel.COLS / 2};
        }
        
        int[] bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        
        for (int[] move : moves) {
            int[][] newBoard = copyBoard(board);
            newBoard[move[0]][move[1]] = AI_PIECE;
            
            // 检查是否直接胜利
            if (WuziqiModel.is5PieceConnected(newBoard, move[0], move[1])) {
                return move;
            }
            
            // 评估这个位置的得分（只评估当前局面，不递归）
            int score = evaluatePosition(newBoard);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 困难难度：使用Alpha-Beta剪枝的Minimax算法
     */
    private int[] getHardMove(int[][] board) {
        List<int[]> moves = getValidMoves(board);
        if (moves.isEmpty()) {
            return new int[]{WuziqiModel.ROWS / 2, WuziqiModel.COLS / 2};
        }
        
        // 1. 检查是否有自己的胜利机会（最高优先级）
        for (int[] move : moves) {
            int[][] newBoard = copyBoard(board);
            newBoard[move[0]][move[1]] = AI_PIECE;
            if (WuziqiModel.is5PieceConnected(newBoard, move[0], move[1])) {
                return move;
            }
        }
        
        // 2. 检查是否有必须防守的威胁（玩家能成五）
        for (int[] move : moves) {
            int[][] newBoard = copyBoard(board);
            newBoard[move[0]][move[1]] = PLAYER_PIECE;
            if (WuziqiModel.is5PieceConnected(newBoard, move[0], move[1])) {
                return move; // 必须防守
            }
        }
        
        // 3. 检查玩家的活四和冲四威胁，优先防守
        List<int[]> defensiveMoves = new ArrayList<>();
        for (int[] move : moves) {
            int playerValue = evaluatePositionForPiece(board, move[0], move[1], PLAYER_PIECE);
            if (playerValue >= RUSH_FOUR) { // 玩家的冲四或活四
                defensiveMoves.add(move);
            }
        }
        if (!defensiveMoves.isEmpty()) {
            // 从防守位置中选择评分最高的（考虑防守后的局面）
            int[] bestDefensiveMove = defensiveMoves.get(0);
            int bestDefensiveScore = Integer.MIN_VALUE;
            for (int[] move : defensiveMoves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move[0]][move[1]] = AI_PIECE;
                int score = evaluatePosition(newBoard);
                if (score > bestDefensiveScore) {
                    bestDefensiveScore = score;
                    bestDefensiveMove = move;
                }
            }
            return bestDefensiveMove;
        }
        
        // 4. 检查玩家的活三威胁，优先防守
        List<int[]> liveThreeDefensiveMoves = new ArrayList<>();
        for (int[] move : moves) {
            int playerValue = evaluatePositionForPiece(board, move[0], move[1], PLAYER_PIECE);
            if (playerValue >= LIVE_THREE) { // 玩家的活三
                liveThreeDefensiveMoves.add(move);
            }
        }
        if (!liveThreeDefensiveMoves.isEmpty()) {
            // 从防守活三的位置中选择评分最高的
            int[] bestDefensiveMove = liveThreeDefensiveMoves.get(0);
            int bestDefensiveScore = Integer.MIN_VALUE;
            for (int[] move : liveThreeDefensiveMoves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move[0]][move[1]] = AI_PIECE;
                int score = evaluatePosition(newBoard);
                if (score > bestDefensiveScore) {
                    bestDefensiveScore = score;
                    bestDefensiveMove = move;
                }
            }
            return bestDefensiveMove;
        }
        
        // 5. 使用Alpha-Beta剪枝的Minimax算法选择最佳移动
        int[] bestMove = moves.get(0);
        int bestScore = Integer.MIN_VALUE;
        
        for (int[] move : moves) {
            int[][] newBoard = copyBoard(board);
            newBoard[move[0]][move[1]] = AI_PIECE;
            
            // 使用Minimax评估这个移动
            int score = alphaBeta(newBoard, MAX_DEPTH - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * Alpha-Beta剪枝的Minimax算法
     * 返回局面的评估分数
     */
    private int alphaBeta(int[][] board, int depth, int alpha, int beta, boolean maximizing) {
        // 到达最大深度或没有合法移动，评估当前局面
        if (depth == 0) {
            return evaluatePosition(board);
        }
        
        List<int[]> moves = getValidMoves(board);
        if (moves.isEmpty()) {
            return evaluatePosition(board);
        }
        
        if (maximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int[] move : moves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move[0]][move[1]] = AI_PIECE;
                
                // 检查是否胜利
                if (WuziqiModel.is5PieceConnected(newBoard, move[0], move[1])) {
                    return FIVE; // AI胜利
                }
                
                int score = alphaBeta(newBoard, depth - 1, alpha, beta, false);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break; // Alpha-Beta剪枝
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int[] move : moves) {
                int[][] newBoard = copyBoard(board);
                newBoard[move[0]][move[1]] = PLAYER_PIECE;
                
                // 检查是否胜利
                if (WuziqiModel.is5PieceConnected(newBoard, move[0], move[1])) {
                    return -FIVE; // 玩家胜利
                }
                
                int score = alphaBeta(newBoard, depth - 1, alpha, beta, true);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break; // Alpha-Beta剪枝
                }
            }
            return minScore;
        }
    }
    
    /**
     * 评估棋盘局面的得分
     * 正分表示AI优势，负分表示玩家优势
     */
    private int evaluatePosition(int[][] board) {
        int aiScore = 0;
        int playerScore = 0;
        
        // 遍历所有已有棋子的位置，评估每个位置的威胁
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            for (int j = 0; j < WuziqiModel.COLS; j++) {
                if (board[i][j] == AI_PIECE) {
                    // 评估AI在这个位置的棋型
                    aiScore += evaluatePiecePosition(board, i, j, AI_PIECE);
                } else if (board[i][j] == PLAYER_PIECE) {
                    // 评估玩家在这个位置的棋型
                    playerScore += evaluatePiecePosition(board, i, j, PLAYER_PIECE);
                }
            }
        }
        
        // 同时评估空位置的潜在价值（更准确的评估）
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            for (int j = 0; j < WuziqiModel.COLS; j++) {
                if (board[i][j] == EMPTY) {
                    // 评估这个空位置对AI和玩家的潜在价值
                    int aiValue = evaluatePositionForPiece(board, i, j, AI_PIECE);
                    int playerValue = evaluatePositionForPiece(board, i, j, PLAYER_PIECE);
                    
                    aiScore += aiValue / 2; // 潜在价值权重降低
                    playerScore += playerValue / 2;
                }
            }
        }
        
        return aiScore - playerScore;
    }
    
    /**
     * 评估已有棋子位置的棋型价值
     */
    private int evaluatePiecePosition(int[][] board, int row, int col, int piece) {
        int score = 0;
        
        // 四个方向：横、竖、左上-右下、右上-左下
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            PatternInfo pattern = countPatternForExistingPiece(board, row, col, dir[0], dir[1], piece);
            score += evaluatePattern(pattern);
        }
        
        return score;
    }
    
    /**
     * 统计已有棋子在某个方向的连子模式
     */
    private PatternInfo countPatternForExistingPiece(int[][] board, int row, int col, int di, int dj, int piece) {
        PatternInfo info = new PatternInfo();
        
        int count = 1; // 当前位置算1
        int blocked1 = 0; // 正方向是否被堵
        int blocked2 = 0; // 负方向是否被堵
        
        // 正方向
        int r = row + di;
        int c = col + dj;
        while (r >= 0 && r < WuziqiModel.ROWS && c >= 0 && c < WuziqiModel.COLS) {
            if (board[r][c] == piece) {
                count++;
                r += di;
                c += dj;
            } else {
                if (board[r][c] != EMPTY) {
                    blocked1 = 1; // 被对方棋子堵住
                }
                break;
            }
        }
        
        // 负方向
        r = row - di;
        c = col - dj;
        while (r >= 0 && r < WuziqiModel.ROWS && c >= 0 && c < WuziqiModel.COLS) {
            if (board[r][c] == piece) {
                count++;
                r -= di;
                c -= dj;
            } else {
                if (board[r][c] != EMPTY) {
                    blocked2 = 1; // 被对方棋子堵住
                }
                break;
            }
        }
        
        info.count = count;
        info.blocked = blocked1 + blocked2; // 0=活，1=半活，2=死
        
        return info;
    }
    
    /**
     * 评估某个位置对指定棋子的价值
     */
    private int evaluatePositionForPiece(int[][] board, int row, int col, int piece) {
        int score = 0;
        
        // 四个方向：横、竖、左上-右下、右上-左下
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            PatternInfo pattern = countPattern(board, row, col, dir[0], dir[1], piece);
            score += evaluatePattern(pattern);
        }
        
        return score;
    }
    
    /**
     * 统计某个方向的连子模式
     */
    private PatternInfo countPattern(int[][] board, int row, int col, int di, int dj, int piece) {
        PatternInfo info = new PatternInfo();
        
        // 向正方向统计
        int count = 1; // 当前位置算1
        int blocked1 = 0; // 正方向是否被堵
        int blocked2 = 0; // 负方向是否被堵
        
        // 正方向
        int r = row + di;
        int c = col + dj;
        while (r >= 0 && r < WuziqiModel.ROWS && c >= 0 && c < WuziqiModel.COLS) {
            if (board[r][c] == piece) {
                count++;
                r += di;
                c += dj;
            } else {
                if (board[r][c] != EMPTY) {
                    blocked1 = 1; // 被对方棋子堵住
                }
                break;
            }
        }
        
        // 负方向
        r = row - di;
        c = col - dj;
        while (r >= 0 && r < WuziqiModel.ROWS && c >= 0 && c < WuziqiModel.COLS) {
            if (board[r][c] == piece) {
                count++;
                r -= di;
                c -= dj;
            } else {
                if (board[r][c] != EMPTY) {
                    blocked2 = 1; // 被对方棋子堵住
                }
                break;
            }
        }
        
        info.count = count;
        info.blocked = blocked1 + blocked2; // 0=活，1=半活，2=死
        
        return info;
    }
    
    /**
     * 根据模式信息评估得分
     */
    private int evaluatePattern(PatternInfo pattern) {
        int count = pattern.count;
        int blocked = pattern.blocked;
        
        if (count >= 5) {
            return FIVE;
        }
        
        if (count == 4) {
            if (blocked == 0) {
                return LIVE_FOUR; // 活四
            } else if (blocked == 1) {
                return RUSH_FOUR; // 冲四
            }
        }
        
        if (count == 3) {
            if (blocked == 0) {
                return LIVE_THREE; // 活三
            }
        }
        
        if (count == 2) {
            if (blocked == 0) {
                return LIVE_TWO; // 活二
            }
        }
        
        return 0;
    }
    
    /**
     * 获取所有合法的落子位置
     * 优化：只考虑已有棋子附近的区域
     */
    private List<int[]> getValidMoves(int[][] board) {
        List<int[]> moves = new ArrayList<>();
        boolean[][] considered = new boolean[WuziqiModel.ROWS][WuziqiModel.COLS];
        
        // 先找到所有已有棋子
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            for (int j = 0; j < WuziqiModel.COLS; j++) {
                if (board[i][j] != EMPTY) {
                    // 标记周围2格内的位置为候选
                    for (int di = -2; di <= 2; di++) {
                        for (int dj = -2; dj <= 2; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (ni >= 0 && ni < WuziqiModel.ROWS && 
                                nj >= 0 && nj < WuziqiModel.COLS &&
                                board[ni][nj] == EMPTY && !considered[ni][nj]) {
                                moves.add(new int[]{ni, nj});
                                considered[ni][nj] = true;
                            }
                        }
                    }
                }
            }
        }
        
        // 如果没有任何棋子，返回中心位置
        if (moves.isEmpty()) {
            int center = WuziqiModel.ROWS / 2;
            moves.add(new int[]{center, center});
        }
        
        return moves;
    }
    
    /**
     * 复制棋盘
     */
    private int[][] copyBoard(int[][] board) {
        int[][] newBoard = new int[WuziqiModel.ROWS][WuziqiModel.COLS];
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            System.arraycopy(board[i], 0, newBoard[i], 0, WuziqiModel.COLS);
        }
        return newBoard;
    }
    
    /**
     * 模式信息类
     */
    private static class PatternInfo {
        int count;    // 连子数量
        int blocked;  // 被堵住的方向数（0=活，1=半活，2=死）
    }
}
