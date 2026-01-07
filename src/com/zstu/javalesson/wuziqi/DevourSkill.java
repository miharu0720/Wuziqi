package com.zstu.javalesson.wuziqi;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * 天罡肃野·吞阵技能实现
 * 功能：清空点击位置周围的棋子（十字形范围）
 */
public class DevourSkill implements Skill, RestorableSkill {
    private boolean active = false;
    private boolean selecting = false;  // 是否正在选择位置
    private int remainingUses = 1;
    private static final int MAX_USES = 1;
    
    @Override
    public String getName() {
        return "天罡肃野·吞阵";
    }
    
    @Override
    public String getDescription() {
        return "清空点击位置周围的棋子（十字形范围）";
    }
    
    @Override
    public int getRemainingUses() {
        return remainingUses;
    }
    
    @Override
    public int getMaxUses() {
        return MAX_USES;
    }
    
    @Override
    public boolean canActivate(WuziqiPanel panel) {
        // 只在人人对弈模式下可用
        if (panel.model != DyModel.P2P) {
            return false;
        }
        return remainingUses > 0 && !active && !selecting;
    }
    
    @Override
    public boolean activate(WuziqiPanel panel) {
        if (canActivate(panel)) {
            active = true;
            selecting = true;
            JOptionPane.showMessageDialog(null, 
                "天罡肃野·吞阵技能已激活！\n请点击棋盘上的一个位置，将清空该位置周围菱形范围内的棋子（共13个位置）。", 
                "技能激活", 
                JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isActive() {
        return active || selecting;
    }
    
    @Override
    public void reset() {
        active = false;
        selecting = false;
        remainingUses = MAX_USES;
    }
    
    @Override
    public void updateMenuItem(JMenuItem menuItem, WuziqiPanel panel) {
        if (menuItem == null) return;
        
        // 只在人人对弈模式下显示
        if (panel.model != DyModel.P2P) {
            menuItem.setEnabled(false);
            menuItem.setText("天罡肃野·吞阵 (仅人人对弈)");
            return;
        }
        
        if (remainingUses > 0) {
            if (active || selecting) {
                menuItem.setText("天罡肃野·吞阵 (选择中)");
                menuItem.setEnabled(false);
            } else {
                menuItem.setText("天罡肃野·吞阵 (" + remainingUses + "次)");
                menuItem.setEnabled(canActivate(panel));
            }
        } else {
            menuItem.setText("天罡肃野·吞阵 (已用完)");
            menuItem.setEnabled(false);
        }
    }
    
    @Override
    public boolean onPiecePlaced(WuziqiPanel panel, int row, int col) {
        // 天罡肃野·吞阵技能不影响正常落子
        return true;
    }
    
    @Override
    public boolean onPieceClicked(WuziqiPanel panel, int row, int col) {
        // 只在人人对弈模式下处理
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        if (!selecting) {
            return false; // 未激活选择模式，不处理
        }
        
        // 清空十字形范围的棋子（可以点击任何位置，包括空位置）
        int clearedCount = clearCrossArea(panel, row, col);
        
        // 技能使用完毕
        active = false;
        selecting = false;
        remainingUses = 0;
        
        // 刷新界面
        panel.updateStatusText();
        panel.repaint();
        
        JOptionPane.showMessageDialog(null, 
            "天罡肃野·吞阵成功！已清空 " + clearedCount + " 个位置的棋子。", 
            "技能使用", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // 使用技能后切换回合
        panel.turns = (panel.turns == Turns.BLACK) ? Turns.WHITE : Turns.BLACK;
        panel.updateStatusText();
        panel.repaint();
        
        return true; // 已处理
    }
    
    /**
     * 处理点击空位置的事件（用于天罡肃野·吞阵技能）
     */
    public boolean onEmptyClicked(WuziqiPanel panel, int row, int col) {
        // 只在人人对弈模式下处理
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        if (!selecting) {
            return false; // 未激活选择模式，不处理
        }
        
        // 清空十字形范围的棋子（可以点击任何位置，包括空位置）
        int clearedCount = clearCrossArea(panel, row, col);
        
        // 技能使用完毕
        active = false;
        selecting = false;
        remainingUses = 0;
        
        // 刷新界面
        panel.updateStatusText();
        panel.repaint();
        
        JOptionPane.showMessageDialog(null, 
            "天罡肃野·吞阵成功！已清空 " + clearedCount + " 个位置的棋子。", 
            "技能使用", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // 使用技能后切换回合
        panel.turns = (panel.turns == Turns.BLACK) ? Turns.WHITE : Turns.BLACK;
        panel.updateStatusText();
        panel.repaint();
        
        return true; // 已处理
    }
    
    /**
     * 清空菱形范围的棋子
     * 范围（菱形，共13个位置）：
     *      *
     *    * * *
     *  * * * * *
     * * * * * * * *
     *  * * * * *
     *    * * *
     *      *
     * 包括：上下左右各2个，四个斜向各1个，中心1个
     * @param panel 游戏面板
     * @param centerRow 中心行
     * @param centerCol 中心列
     * @return 清空的棋子数量
     */
    private int clearCrossArea(WuziqiPanel panel, int centerRow, int centerCol) {
        int clearedCount = 0;
        
        // 清空中心位置
        if (WuziqiModel.array[centerRow][centerCol] != 0) {
            WuziqiModel.array[centerRow][centerCol] = 0;
            WuziqiModel.steps[centerRow][centerCol] = 0;
            clearedCount++;
        }
        
        // 清空上方（共2个）
        for (int offset = 1; offset <= 2; offset++) {
            int row = centerRow - offset;
            if (row >= 0) {
                if (WuziqiModel.array[row][centerCol] != 0) {
                    WuziqiModel.array[row][centerCol] = 0;
                    WuziqiModel.steps[row][centerCol] = 0;
                    clearedCount++;
                }
            }
        }
        
        // 清空下方（共2个）
        for (int offset = 1; offset <= 2; offset++) {
            int row = centerRow + offset;
            if (row < WuziqiModel.ROWS) {
                if (WuziqiModel.array[row][centerCol] != 0) {
                    WuziqiModel.array[row][centerCol] = 0;
                    WuziqiModel.steps[row][centerCol] = 0;
                    clearedCount++;
                }
            }
        }
        
        // 清空左侧（共2个）
        for (int offset = 1; offset <= 2; offset++) {
            int col = centerCol - offset;
            if (col >= 0) {
                if (WuziqiModel.array[centerRow][col] != 0) {
                    WuziqiModel.array[centerRow][col] = 0;
                    WuziqiModel.steps[centerRow][col] = 0;
                    clearedCount++;
                }
            }
        }
        
        // 清空右侧（共2个）
        for (int offset = 1; offset <= 2; offset++) {
            int col = centerCol + offset;
            if (col < WuziqiModel.COLS) {
                if (WuziqiModel.array[centerRow][col] != 0) {
                    WuziqiModel.array[centerRow][col] = 0;
                    WuziqiModel.steps[centerRow][col] = 0;
                    clearedCount++;
                }
            }
        }
        
        // 清空四个斜向（各1个）
        // 左上斜向
        if (centerRow > 0 && centerCol > 0) {
            if (WuziqiModel.array[centerRow - 1][centerCol - 1] != 0) {
                WuziqiModel.array[centerRow - 1][centerCol - 1] = 0;
                WuziqiModel.steps[centerRow - 1][centerCol - 1] = 0;
                clearedCount++;
            }
        }
        
        // 右上斜向
        if (centerRow > 0 && centerCol < WuziqiModel.COLS - 1) {
            if (WuziqiModel.array[centerRow - 1][centerCol + 1] != 0) {
                WuziqiModel.array[centerRow - 1][centerCol + 1] = 0;
                WuziqiModel.steps[centerRow - 1][centerCol + 1] = 0;
                clearedCount++;
            }
        }
        
        // 左下斜向
        if (centerRow < WuziqiModel.ROWS - 1 && centerCol > 0) {
            if (WuziqiModel.array[centerRow + 1][centerCol - 1] != 0) {
                WuziqiModel.array[centerRow + 1][centerCol - 1] = 0;
                WuziqiModel.steps[centerRow + 1][centerCol - 1] = 0;
                clearedCount++;
            }
        }
        
        // 右下斜向
        if (centerRow < WuziqiModel.ROWS - 1 && centerCol < WuziqiModel.COLS - 1) {
            if (WuziqiModel.array[centerRow + 1][centerCol + 1] != 0) {
                WuziqiModel.array[centerRow + 1][centerCol + 1] = 0;
                WuziqiModel.steps[centerRow + 1][centerCol + 1] = 0;
                clearedCount++;
            }
        }
        
        // 重新计算手数（因为清空了棋子，需要重新分配手数）
        WuziqiModel.stepCount = 0;
        for (int r = 0; r < WuziqiModel.ROWS; r++) {
            for (int c = 0; c < WuziqiModel.COLS; c++) {
                if (WuziqiModel.array[r][c] != 0) {
                    WuziqiModel.steps[r][c] = ++WuziqiModel.stepCount;
                } else {
                    WuziqiModel.steps[r][c] = 0;
                }
            }
        }
        
        return clearedCount;
    }
    
    @Override
    public void restoreState(int remainingUses, boolean isActive) {
        this.remainingUses = remainingUses;
        this.active = isActive;
        this.selecting = false; // 读取时重置选择状态
    }
}

