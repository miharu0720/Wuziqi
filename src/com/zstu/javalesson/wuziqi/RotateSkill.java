package com.zstu.javalesson.wuziqi;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * 斗柄回寅·轮转技能实现
 * 功能：旋转目标位置上下左右四个棋子（顺时针）
 */
public class RotateSkill implements Skill, RestorableSkill {
    private boolean active = false;
    private boolean selecting = false;  // 是否正在选择位置
    private int remainingUses = 1;
    private static final int MAX_USES = 1;
    
    @Override
    public String getName() {
        return "斗柄回寅·轮转";
    }
    
    @Override
    public String getDescription() {
        return "旋转目标位置上下左右四个棋子（顺时针）";
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
                "斗柄回寅·轮转技能已激活！\n请点击棋盘上的一个位置，将逆时针旋转该位置上下左右四个棋子。", 
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
            menuItem.setText("斗柄回寅·轮转 (仅人人对弈)");
            return;
        }
        
        if (remainingUses > 0) {
            if (active || selecting) {
                menuItem.setText("斗柄回寅·轮转 (选择中)");
                menuItem.setEnabled(false);
            } else {
                menuItem.setText("斗柄回寅·轮转 (" + remainingUses + "次)");
                menuItem.setEnabled(canActivate(panel));
            }
        } else {
            menuItem.setText("斗柄回寅·轮转 (已用完)");
            menuItem.setEnabled(false);
        }
    }
    
    @Override
    public boolean onPiecePlaced(WuziqiPanel panel, int row, int col) {
        // 斗柄回寅·轮转技能不影响正常落子
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
        
        // 旋转上下左右四个棋子
        rotateFourPieces(panel, row, col);
        
        // 技能使用完毕
        active = false;
        selecting = false;
        remainingUses = 0;
        
        // 刷新界面
        panel.updateStatusText();
        panel.repaint();
        
        JOptionPane.showMessageDialog(null, 
            "斗柄回寅·轮转成功！已旋转该位置上下左右四个棋子。", 
            "技能使用", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // 使用技能后切换回合
        panel.turns = (panel.turns == Turns.BLACK) ? Turns.WHITE : Turns.BLACK;
        panel.updateStatusText();
        panel.repaint();
        
        return true; // 已处理
    }
    
    /**
     * 处理点击空位置的事件（用于斗柄回寅·轮转技能）
     */
    public boolean onEmptyClicked(WuziqiPanel panel, int row, int col) {
        // 只在人人对弈模式下处理
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        if (!selecting) {
            return false; // 未激活选择模式，不处理
        }
        
        // 旋转上下左右四个棋子
        rotateFourPieces(panel, row, col);
        
        // 技能使用完毕
        active = false;
        selecting = false;
        remainingUses = 0;
        
        // 刷新界面
        panel.updateStatusText();
        panel.repaint();
        
        JOptionPane.showMessageDialog(null, 
            "斗柄回寅·轮转成功！已旋转该位置上下左右四个棋子。", 
            "技能使用", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // 使用技能后切换回合
        panel.turns = (panel.turns == Turns.BLACK) ? Turns.WHITE : Turns.BLACK;
        panel.updateStatusText();
        panel.repaint();
        
        return true; // 已处理
    }
    
    /**
     * 旋转目标位置上下左右四个棋子（顺时针）
     * 旋转规则：
     * - 上 -> 右
     * - 右 -> 下
     * - 下 -> 左
     * - 左 -> 上
     * @param panel 游戏面板
     * @param centerRow 中心行
     * @param centerCol 中心列
     */
    private void rotateFourPieces(WuziqiPanel panel, int centerRow, int centerCol) {
        // 获取四个位置的棋子值
        int top = (centerRow > 0) ? WuziqiModel.array[centerRow - 1][centerCol] : 0;
        int right = (centerCol < WuziqiModel.COLS - 1) ? WuziqiModel.array[centerRow][centerCol + 1] : 0;
        int bottom = (centerRow < WuziqiModel.ROWS - 1) ? WuziqiModel.array[centerRow + 1][centerCol] : 0;
        int left = (centerCol > 0) ? WuziqiModel.array[centerRow][centerCol - 1] : 0;
        
        // 获取四个位置的手数
        int topStep = (centerRow > 0) ? WuziqiModel.steps[centerRow - 1][centerCol] : 0;
        int rightStep = (centerCol < WuziqiModel.COLS - 1) ? WuziqiModel.steps[centerRow][centerCol + 1] : 0;
        int bottomStep = (centerRow < WuziqiModel.ROWS - 1) ? WuziqiModel.steps[centerRow + 1][centerCol] : 0;
        int leftStep = (centerCol > 0) ? WuziqiModel.steps[centerRow][centerCol - 1] : 0;
        
        // 顺时针旋转：上 -> 右，右 -> 下，下 -> 左，左 -> 上
        if (centerRow > 0) {
            WuziqiModel.array[centerRow - 1][centerCol] = right;
            WuziqiModel.steps[centerRow - 1][centerCol] = rightStep;
        }
        if (centerCol < WuziqiModel.COLS - 1) {
            WuziqiModel.array[centerRow][centerCol + 1] = bottom;
            WuziqiModel.steps[centerRow][centerCol + 1] = bottomStep;
        }
        if (centerRow < WuziqiModel.ROWS - 1) {
            WuziqiModel.array[centerRow + 1][centerCol] = left;
            WuziqiModel.steps[centerRow + 1][centerCol] = leftStep;
        }
        if (centerCol > 0) {
            WuziqiModel.array[centerRow][centerCol - 1] = top;
            WuziqiModel.steps[centerRow][centerCol - 1] = topStep;
        }
        
        // 检查旋转后是否导致游戏结束（检查四个位置）
        if (centerRow > 0 && WuziqiModel.array[centerRow - 1][centerCol] != 0) {
            if (panel.dealGameOver(centerRow - 1, centerCol)) {
                return;
            }
        }
        if (centerCol < WuziqiModel.COLS - 1 && WuziqiModel.array[centerRow][centerCol + 1] != 0) {
            if (panel.dealGameOver(centerRow, centerCol + 1)) {
                return;
            }
        }
        if (centerRow < WuziqiModel.ROWS - 1 && WuziqiModel.array[centerRow + 1][centerCol] != 0) {
            if (panel.dealGameOver(centerRow + 1, centerCol)) {
                return;
            }
        }
        if (centerCol > 0 && WuziqiModel.array[centerRow][centerCol - 1] != 0) {
            if (panel.dealGameOver(centerRow, centerCol - 1)) {
                return;
            }
        }
    }
    
    @Override
    public void restoreState(int remainingUses, boolean isActive) {
        this.remainingUses = remainingUses;
        this.active = isActive;
        this.selecting = false; // 读取时重置选择状态
    }
}

