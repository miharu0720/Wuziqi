package com.zstu.javalesson.wuziqi;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * 星孛袭野·夺子技能实现
 * 功能：指定敌方的一颗棋子变成我方棋子
 */
public class ProvokeSkill implements Skill, RestorableSkill {
    private boolean active = false;
    private boolean selecting = false;  // 是否正在选择敌方棋子
    private int remainingUses = 1;
    private static final int MAX_USES = 1;
    
    @Override
    public String getName() {
        return "星孛袭野·夺子";
    }
    
    @Override
    public String getDescription() {
        return "指定敌方的一颗棋子变成我方棋子";
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
        if (remainingUses <= 0 || active || selecting) {
            return false;
        }
        // 检查是否有敌方棋子
        return hasEnemyPieces(panel);
    }
    
    @Override
    public boolean activate(WuziqiPanel panel) {
        if (canActivate(panel)) {
            active = true;
            selecting = true;
            JOptionPane.showMessageDialog(null, 
                "星孛袭野·夺子技能已激活！\n请点击一个敌方棋子将其转换为己方棋子。", 
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
            menuItem.setText("星孛袭野·夺子 (仅人人对弈)");
            return;
        }
        
        if (remainingUses > 0) {
            if (active || selecting) {
                menuItem.setText("星孛袭野·夺子 (选择中)");
                menuItem.setEnabled(false);
            } else {
                menuItem.setText("星孛袭野·夺子 (" + remainingUses + "次)");
                menuItem.setEnabled(canActivate(panel));
            }
        } else {
            menuItem.setText("星孛袭野·夺子 (已用完)");
            menuItem.setEnabled(false);
        }
    }
    
    @Override
    public boolean onPiecePlaced(WuziqiPanel panel, int row, int col) {
        // 星孛袭野·夺子技能不影响正常落子
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
        
        // 确定己方和敌方棋子值
        int myPiece = (panel.turns == Turns.BLACK) ? 1 : 2;
        int enemyPiece = (panel.turns == Turns.BLACK) ? 2 : 1;
        
        // 检查点击的是否是敌方棋子
        if (WuziqiModel.array[row][col] == enemyPiece) {
            // 将敌方棋子转换为己方棋子
            WuziqiModel.array[row][col] = myPiece;
            
            // 更新手数记录（保持原有手数，但颜色改变）
            // 注意：这里不改变 stepCount，因为只是转换棋子颜色
            // 但是需要更新 steps 数组，因为棋子颜色改变了
            
            // 检查转换后是否导致游戏结束
            boolean isOver = panel.dealGameOver(row, col);
            if (isOver) {
                // 游戏结束，重置技能
                active = false;
                selecting = false;
                remainingUses = 0;
                return true;
            }
            
            // 技能使用完毕
            active = false;
            selecting = false;
            remainingUses = 0;
            
            // 刷新界面
            panel.updateStatusText();
            panel.repaint();
            
            JOptionPane.showMessageDialog(null, 
                "星孛袭野·夺子成功！敌方棋子已转换为己方棋子。", 
                "技能使用", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // 转换后切换回合
            panel.turns = (panel.turns == Turns.BLACK) ? Turns.WHITE : Turns.BLACK;
            panel.updateStatusText();
            panel.repaint();
            
            return true; // 已处理
        } else if (WuziqiModel.array[row][col] == myPiece) {
            // 点击的是己方棋子，取消技能
            JOptionPane.showMessageDialog(null, 
                "请点击敌方棋子！\n技能已取消。", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            active = false;
            selecting = false;
            panel.updateStatusText();
            return true; // 已处理（取消技能）
        } else {
            // 点击的是空位置，取消技能
            JOptionPane.showMessageDialog(null, 
                "请点击敌方棋子！\n技能已取消。", 
                "提示", 
                JOptionPane.WARNING_MESSAGE);
            active = false;
            selecting = false;
            panel.updateStatusText();
            return true; // 已处理（取消技能）
        }
    }
    
    /**
     * 检查是否有敌方棋子
     */
    private boolean hasEnemyPieces(WuziqiPanel panel) {
        int enemyPiece = (panel.turns == Turns.BLACK) ? 2 : 1;
        for (int i = 0; i < WuziqiModel.ROWS; i++) {
            for (int j = 0; j < WuziqiModel.COLS; j++) {
                if (WuziqiModel.array[i][j] == enemyPiece) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void restoreState(int remainingUses, boolean isActive) {
        this.remainingUses = remainingUses;
        this.active = isActive;
        this.selecting = false; // 读取时重置选择状态
    }
    
}

