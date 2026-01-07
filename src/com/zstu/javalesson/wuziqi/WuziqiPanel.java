package com.zstu.javalesson.wuziqi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WuziqiPanel extends JPanel implements WuziqiConsts, MouseListener {

    int start_x;
    int start_y;

    Turns turns = Turns.BLACK;
    int counts = 0;
    Agent agent = new Agent();

    DyModel model = DyModel.P2M;
    
    // AI难度
    Difficulty aiDifficulty = Difficulty.HARD;

    // 状态栏标签（显示当前模式和轮到谁下）
    JLabel statusLabel;
    
    // 技能管理器
    private SkillManager skillManager;

    // 棋钟标签（黑棋 / 白棋用时）
    JLabel blackTimeLabel;
    JLabel whiteTimeLabel;
    
    // 当前棋盘主题
    Theme theme = Theme.DARK_WOOD;

    // 棋钟：黑白双方累计用时（秒）
    int blackSeconds = 0;
    int whiteSeconds = 0;
    javax.swing.Timer clockTimer;

    public WuziqiPanel(){
        Dimension dm = calculateSize();
        setPreferredSize(dm);
        addMouseListener(this);
        setFocusable(true);

        // 初始化技能管理器
        skillManager = new SkillManager(this);

        // 初始化棋钟
        clockTimer = new javax.swing.Timer(1000, e -> {
            if (turns == Turns.BLACK) {
                blackSeconds++;
            } else {
                whiteSeconds++;
            }
            updateClockLabels();
        });
        clockTimer.start();
    }
    
    /**
     * 获取技能管理器
     */
    public SkillManager getSkillManager() {
        return skillManager;
    }
    
    /**
     * 触发AI下棋（用于技能使用后）
     */
    public void triggerAIMoveAfterSkill() {
        if (turns == Turns.WHITE && model == DyModel.P2M) {
            // 在后台线程中执行AI计算
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int[] ij_w;
                    try {
                        // AI思考过程（在后台线程）
                        ij_w = agent.getij(WuziqiModel.array, WuziqiModel.stepCount);
                        
                        // 回到UI线程更新界面
                        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                // 智能体落子并记录手数
                                WuziqiModel.array[ij_w[0]][ij_w[1]] = 2;
                                WuziqiModel.stepCount++;
                                WuziqiModel.steps[ij_w[0]][ij_w[1]] = WuziqiModel.stepCount;
                                // 判断胜负
                                turns = Turns.BLACK;
                                updateStatusText();
                                repaint();
                                if (dealGameOver(ij_w[0], ij_w[1]))
                                    return;
                                counts += 2;
                            }
                        });
                    } catch (QipanyimanException e) {
                        // 棋盘已满，忽略
                    }
                }
            }).start();
        }
    }

    // 由窗口 Form 传入状态栏标签
    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
        updateStatusText();
    }

    // 由窗口 Form 传入棋钟标签
    public void setClockLabels(JLabel blackLabel, JLabel whiteLabel) {
        this.blackTimeLabel = blackLabel;
        this.whiteTimeLabel = whiteLabel;
        updateClockLabels();
    }

    // 设置棋盘主题
    public void setTheme(Theme theme) {
        this.theme = theme;
        repaint();
    }
    
    // 设置AI难度
    public void setDifficulty(Difficulty difficulty) {
        this.aiDifficulty = difficulty;
        agent.setDifficulty(difficulty);
    }
    

    // 重置棋钟并重新开始计时
    public void resetClockAndRestart() {
        blackSeconds = 0;
        whiteSeconds = 0;
        updateClockLabels();
        if (clockTimer != null) {
            // 如果计时器正在运行，先停止
            if (clockTimer.isRunning()) {
                clockTimer.stop();
            }
            // 重新启动计时器
            clockTimer.start();
        }
    }
    

    @Override
    protected void paintComponent(Graphics arg0){
        super.paintComponent(arg0);
        drawQipan(arg0);
        drawPieces(arg0);
    }

    public Dimension calculateSize() {
        int qipan_w = CELL_WIDTH * (WuziqiModel.COLS - 1);
        int qipan_h = qipan_w;

        // 面板宽度 = 棋盘宽度的 2 倍（左右留空）
        int pw = 2 * qipan_w;
        // 面板高度 = 棋盘高度 + 上下各一行左右的留白
        int ph = qipan_h + CELL_WIDTH * 2;

        // 计算start_x, start_y（棋盘左上角偏移）
        start_x = (pw - qipan_w) / 2;
        // 让棋盘稍微靠上，预留一行格子的空白
        start_y = CELL_WIDTH;

        return new Dimension(pw, ph);
    }

    // 根据当前棋盘大小，重新计算并应用面板尺寸
    public void resetSize() {
        Dimension dm = calculateSize();
        setPreferredSize(dm);
        revalidate();
        repaint();
    }

    protected void drawQipan(Graphics arg0){
        // 背景
        int x0 = start_x - CELL_WIDTH / 2;
        int y0 = start_y - CELL_WIDTH / 2;
        // 棋盘背景颜色根据主题切换
        arg0.setColor(theme.getBoardColor());
        arg0.fillRect(x0, y0, WuziqiModel.COLS * CELL_WIDTH, WuziqiModel.ROWS * CELL_WIDTH);
        // 直线
        arg0.setColor(theme.getLineColor());
        for (int i = 0; i < WuziqiModel.ROWS; i++){
            arg0.drawLine(start_x,
                    start_y+i*CELL_WIDTH,
                    start_x+(WuziqiModel.COLS-1)*CELL_WIDTH,
                    start_y+i*CELL_WIDTH);
        }

        for (int i = 0; i < WuziqiModel.COLS; i++){
            arg0.drawLine(start_x+i*CELL_WIDTH,
                    start_y,
                    start_x+i*CELL_WIDTH,
                    start_y+(WuziqiModel.ROWS-1)*CELL_WIDTH);
        }

        int center = WuziqiModel.ROWS / 2;
        int[] dots = {3, center, WuziqiModel.ROWS - 4}; // 星位
        int dotSize = CELL_WIDTH / 5;

        for (int i : dots) {
            for (int j : dots) {
                if ((i == center && j == center) ||
                        (i == 3 && j == 3) || (i == 3 && j == WuziqiModel.ROWS - 4) ||
                        (i == WuziqiModel.ROWS - 4 && j == 3) || (i == WuziqiModel.ROWS - 4 && j == WuziqiModel.ROWS - 4)) {
                    arg0.setColor(Color.black);
                    arg0.fillOval(start_x + j * CELL_WIDTH - dotSize/2,
                            start_y + i * CELL_WIDTH - dotSize/2,
                            dotSize, dotSize);
                }
            }
        }
    }

    protected void drawPieces(Graphics arg0){
        // 画棋子
        for (int i = 0; i < WuziqiModel.ROWS; i++){
            for (int j = 0; j < WuziqiModel.COLS; j++){
                if (WuziqiModel.array[i][j] == 1){
                    int[] pixels = WuziqiModel.ij2pixels(start_x, start_y, i, j);
                    // 黑子在不同主题下仍为黑色（如需改为棕色等，可在这里切换）
                    arg0.setColor(Color.black);
                    arg0.fillOval(pixels[0] - CELL_WIDTH / 2,
                            pixels[1] - CELL_WIDTH / 2,
                            CELL_WIDTH,
                            CELL_WIDTH);
                    // 在黑子上绘制手数
                    int step = WuziqiModel.steps[i][j];
                    if (step > 0) {
                        arg0.setColor(Color.white);
                        Font oldFont = arg0.getFont();
                        Font font = oldFont.deriveFont(Font.BOLD, CELL_WIDTH * 0.5f);
                        arg0.setFont(font);
                        String text = String.valueOf(step);
                        FontMetrics fm = arg0.getFontMetrics();
                        int textWidth = fm.stringWidth(text);
                        int textHeight = fm.getAscent();
                        int tx = pixels[0] - textWidth / 2;
                        int ty = pixels[1] + textHeight / 2 - 2;
                        arg0.drawString(text, tx, ty);
                        arg0.setFont(oldFont);
                    }
                    // 高亮最后一手（当前最大手数的棋子）
                    if (step == WuziqiModel.stepCount && step > 0) {
                        arg0.setColor(Color.RED);
                        int size = CELL_WIDTH + 4; // 比棋子略大一点
                        arg0.drawRect(pixels[0] - size / 2,
                                      pixels[1] - size / 2,
                                      size,
                                      size);
                    }
                }
                if (WuziqiModel.array[i][j] == 2){
                    int[] pixels = WuziqiModel.ij2pixels(start_x, start_y, i, j);
                    // 白子保持白色
                    arg0.setColor(Color.white);
                    arg0.fillOval(pixels[0] - CELL_WIDTH / 2,
                            pixels[1] - CELL_WIDTH / 2,
                            CELL_WIDTH,
                            CELL_WIDTH);
                    // 在白子上绘制手数
                    int step = WuziqiModel.steps[i][j];
                    if (step > 0) {
                        arg0.setColor(Color.black);
                        Font oldFont = arg0.getFont();
                        Font font = oldFont.deriveFont(Font.BOLD, CELL_WIDTH * 0.5f);
                        arg0.setFont(font);
                        String text = String.valueOf(step);
                        FontMetrics fm = arg0.getFontMetrics();
                        int textWidth = fm.stringWidth(text);
                        int textHeight = fm.getAscent();
                        int tx = pixels[0] - textWidth / 2;
                        int ty = pixels[1] + textHeight / 2 - 2;
                        arg0.drawString(text, tx, ty);
                        arg0.setFont(oldFont);
                    }
                    // 高亮最后一手（当前最大手数的棋子）
                    if (step == WuziqiModel.stepCount && step > 0) {
                        arg0.setColor(Color.RED);
                        int size = CELL_WIDTH + 4; // 比棋子略大一点
                        arg0.drawRect(pixels[0] - size / 2,
                                      pixels[1] - size / 2,
                                      size,
                                      size);
                    }
                }
            }
        }
    }

    // 根据当前模式和轮次更新状态栏文字
    public void updateStatusText() {
        if (statusLabel == null) return;
        String modeText = (model == DyModel.P2M) ? "人机对弈" : "人人对弈";
        String turnText = (turns == Turns.BLACK) ? "黑棋" : "白棋";
        // 检查是否有技能正在执行
        if (skillManager != null) {
            Skill doubleMoveSkill = skillManager.getSkill(SkillType.DOUBLE_MOVE);
            if (doubleMoveSkill != null && doubleMoveSkill.isActive()) {
                turnText += " (荧惑守心·双曜: 请落第二个子)";
            }
        }
        statusLabel.setText("模式：" + modeText + "    当前轮到：" + turnText);
        // 同时更新所有技能菜单项状态
        if (skillManager != null) {
            skillManager.updateAllMenuItems();
        }
    }

    // 更新棋钟标签文本
    private void updateClockLabels() {
        if (blackTimeLabel != null) {
            blackTimeLabel.setText("黑: " + formatSeconds(blackSeconds));
        }
        if (whiteTimeLabel != null) {
            whiteTimeLabel.setText("白: " + formatSeconds(whiteSeconds));
        }
    }

    // 秒数格式化为 mm:ss
    private String formatSeconds(int totalSeconds) {
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    /**
     * 撤销一步或两步棋：
     * - 人机模式：一次撤销玩家和AI各一步（共两手），可多次点击连续悔棋；
     * - 人人模式：一次撤销最近的一步。
     */
    public void undo() {
        if (WuziqiModel.stepCount <= 0) {
            return;
        }

        if (model == DyModel.P2M) {
            // 人机模式：尽量撤销两手（玩家+AI）
            int undoTimes = Math.min(2, WuziqiModel.stepCount);
            for (int k = 0; k < undoTimes; k++) {
                undoOneStep();
            }
            // 回到玩家(黑棋)行棋
            turns = Turns.BLACK;
            // AI 使用的计数相应回退
            if (counts >= 2) {
                counts -= 2;
            } else {
                counts = 0;
            }
            // 如果技能正在执行，重置相关技能状态
            if (skillManager != null) {
                Skill doubleMoveSkill = skillManager.getSkill(SkillType.DOUBLE_MOVE);
                if (doubleMoveSkill != null && doubleMoveSkill.isActive()) {
                    // 双落子技能会被重置，但保留使用次数
                    doubleMoveSkill.reset();
                    // 恢复使用次数（因为悔棋了）
                    if (doubleMoveSkill.getRemainingUses() == 0) {
                        // 如果技能已用完，恢复一次使用机会
                        // 这里需要访问DoubleMoveSkill的内部状态，暂时先重置
                    }
                }
            }
            updateStatusText();
            this.repaint();
        } else if (model == DyModel.P2P) {
            // 人人模式：仅撤销最后一步
            int color = undoOneStep();
            if (color == 1) {
                // 撤销黑子，轮到黑棋
                turns = Turns.BLACK;
            } else if (color == 2) {
                // 撤销白子，轮到白棋
                turns = Turns.WHITE;
            }
            updateStatusText();
            this.repaint();
        }
    }

    /**
     * 撤销最近的一手棋，返回被撤销棋子的颜色（1=黑，2=白，0=无）
     */
    private int undoOneStep() {
        if (WuziqiModel.stepCount <= 0) {
            return 0;
        }
        int targetStep = WuziqiModel.stepCount;
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            for (int j = 0; j < WuziqiModel.COLS; j++) {
                if (WuziqiModel.steps[i][j] == targetStep) {
                    int color = WuziqiModel.array[i][j];
                    WuziqiModel.array[i][j] = 0;
                    WuziqiModel.steps[i][j] = 0;
                    WuziqiModel.stepCount--;
                    return color;
                }
            }
        }
        // 理论上不会走到这里，如果走到这里就简单地递减 stepCount
        WuziqiModel.stepCount--;
        return 0;
    }

    protected boolean dealGameOver(int i, int j){
        // 根据刚刚落子的位置判断棋子颜色
        int pieceColor = WuziqiModel.array[i][j];
        String result = (pieceColor == 1 ? "黑" : "白");
        boolean isOver = WuziqiModel.is5PieceConnected(WuziqiModel.array, i, j);
        if(isOver){
            // 棋局结束，停止棋钟
            if (clockTimer != null) {
                clockTimer.stop();
            }
            int selection = JOptionPane.showConfirmDialog(
                    null,
                    "棋局结束！"+result+"赢了，是否重开？",
                    "判断胜负："+result+"胜",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (selection==JOptionPane.CANCEL_OPTION){
                System.exit(0);
            }
            if (selection==JOptionPane.OK_OPTION){
                // 清空棋盘与手数
                WuziqiModel.clear();
                turns = Turns.BLACK;
                counts = 0;
                // 重置双落子状态
                // 重置所有技能状态
                if (skillManager != null) {
                    skillManager.resetAll();
                }
                // 重置并重新启动计时器
                resetClockAndRestart();
            }
            updateStatusText();
            this.repaint();
        }
        return isOver;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        if(model == DyModel.P2M) {
            int px = arg0.getX();
            int py = arg0.getY();
            int[] ij = WuziqiModel.pixels2ij(px, py, start_x, start_y);
            if (ij[0] < 0 || ij[0] >= WuziqiModel.ROWS || ij[1] < 0 || ij[1] >= WuziqiModel.COLS)
                return;
            
            // 检查是否有技能需要处理点击已有棋子的事件（如星孛袭野·夺子）
            // 只在人人对弈模式下处理
            if (WuziqiModel.array[ij[0]][ij[1]] != 0) {
                if (model == DyModel.P2P && skillManager != null && skillManager.onPieceClicked(ij[0], ij[1])) {
                    // 技能已处理该事件
                    return;
                }
                // 如果没有技能处理，且点击的是已有棋子，则忽略
                return;
            }
            
            // 轮到智能体下棋时，鼠标点击无效
            if (turns == Turns.WHITE)
                return;

            // 玩家落子并记录手数
            WuziqiModel.array[ij[0]][ij[1]] = turns == Turns.BLACK ? 1 : 2;
            WuziqiModel.stepCount++;
            WuziqiModel.steps[ij[0]][ij[1]] = WuziqiModel.stepCount;

            // 检查游戏是否结束
            boolean isOver = this.dealGameOver(ij[0], ij[1]);
            if (isOver) {
                // 游戏结束，重置所有技能
                if (skillManager != null) {
                    skillManager.resetAll();
                }
                return;
            }
            
            // 让技能管理器处理落子逻辑
            boolean shouldContinue = skillManager == null || skillManager.onPiecePlaced(ij[0], ij[1]);
            
            if (!shouldContinue) {
                // 被技能拦截（如双落子的第一次落子），不切换回合
                updateStatusText();
                this.repaint();
                return;
            }
            
            // 正常切换回合
            turns = turns == Turns.BLACK ? Turns.WHITE : Turns.BLACK;
            updateStatusText();
            this.repaint();
            
            // 检查第二次落子后是否游戏结束（双落子情况）
            // 如果技能在第二次落子时返回true，说明完成了双落子，需要再次检查游戏结束
            if (skillManager != null) {
                Skill doubleMoveSkill = skillManager.getSkill(SkillType.DOUBLE_MOVE);
                if (doubleMoveSkill != null && doubleMoveSkill.getRemainingUses() == 0) {
                    // 双落子刚完成，检查第二次落子是否导致游戏结束
                    isOver = this.dealGameOver(ij[0], ij[1]);
                    if (isOver) {
                        skillManager.resetAll();
                        return;
                    }
                }
            }
            
            // 检查第二次落子后是否游戏结束（双落子情况）
            if (skillManager != null) {
                Skill doubleMoveSkill = skillManager.getSkill(SkillType.DOUBLE_MOVE);
                if (doubleMoveSkill != null && doubleMoveSkill.isActive()) {
                    isOver = this.dealGameOver(ij[0], ij[1]);
                    if (isOver) {
                        skillManager.resetAll();
                        return;
                    }
                }
            }

            // 轮到智能体下棋 - 在后台线程中执行，避免阻塞UI
            if (turns == Turns.WHITE) {
                // 在后台线程中执行AI计算
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                int[] ij_w;
                try {
                            // AI思考过程（在后台线程）
                            ij_w = agent.getij(WuziqiModel.array, WuziqiModel.stepCount);
                            
                            // 回到UI线程更新界面
                            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    // 智能体落子并记录手数
                    WuziqiModel.array[ij_w[0]][ij_w[1]] = 2;
                                    WuziqiModel.stepCount++;
                                    WuziqiModel.steps[ij_w[0]][ij_w[1]] = WuziqiModel.stepCount;
                    // 判断胜负
                    turns = Turns.BLACK;
                                    updateStatusText();
                                    repaint();
                    if (dealGameOver(ij_w[0], ij_w[1]))
                        return;
                    counts += 2;
                                }
                            });
                } catch (QipanyimanException e) {
                    // e.printStackTrace();
                            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                    int selection = JOptionPane.showConfirmDialog(
                            null,
                            "棋盘已满，和棋！是否重开？",
                            "Title",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (selection == JOptionPane.CANCEL_OPTION) {
                        System.exit(0);
                    }
                    if (selection == JOptionPane.OK_CANCEL_OPTION) {
                                        for (int i = 0; i < WuziqiModel.ROWS; i++) {
                                            for (int j = 0; j < WuziqiModel.COLS; j++) {
                                WuziqiModel.array[i][j] = 0;
                            }
                        }
                        turns = Turns.BLACK;
                        counts = 0;
                                        updateStatusText();
                                        repaint();
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        }
        if (model==DyModel.P2P){
            int px = arg0.getX(); int py = arg0.getY();
            int[] ij = WuziqiModel.pixels2ij(px,py,start_x,start_y);
            if(ij[0]<0||ij[0]>=WuziqiModel.ROWS||ij[1]<0||ij[1]>=WuziqiModel.COLS)
                return;
            
            // 检查是否有技能需要处理点击已有棋子的事件（如星孛袭野·夺子）
            if(WuziqiModel.array[ij[0]][ij[1]]!=0) {
                if (skillManager != null && skillManager.onPieceClicked(ij[0], ij[1])) {
                    // 技能已处理该事件
                    return;
                }
                // 如果没有技能处理，且点击的是已有棋子，则忽略
                return;
            }
            
            // 检查是否有技能需要处理点击空位置的事件（如天罡肃野·吞阵）
            if (skillManager != null && skillManager.onEmptyClicked(ij[0], ij[1])) {
                // 技能已处理该事件
                return;
            }
            
            // 双人对弈落子并记录手数
            WuziqiModel.array[ij[0]][ij[1]] = (turns==Turns.BLACK?1:2);
            WuziqiModel.stepCount++;
            WuziqiModel.steps[ij[0]][ij[1]] = WuziqiModel.stepCount;

            // 检查游戏是否结束
            boolean isOver = this.dealGameOver(ij[0], ij[1]);
            if (isOver) {
                // 游戏结束，重置所有技能
                if (skillManager != null) {
                    skillManager.resetAll();
                }
                return;
            }
            
            // 让技能管理器处理落子逻辑
            boolean shouldContinue = skillManager == null || skillManager.onPiecePlaced(ij[0], ij[1]);
            
            if (!shouldContinue) {
                // 被技能拦截（如双落子的第一次落子），不切换回合
                updateStatusText();
                this.repaint();
                return;
            }
            
            // 正常切换回合
            turns = (turns==Turns.BLACK?Turns.WHITE:Turns.BLACK);
            updateStatusText();
            this.repaint();
            // 判断胜负（第二次落子后，双落子情况）
            if (dealGameOver(ij[0], ij[1])){
                if (skillManager != null) {
                    skillManager.resetAll();
                }
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
