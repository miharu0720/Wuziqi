package com.zstu.javalesson.wuziqi;

import javax.swing.JMenuItem;

/**
 * 双落子技能实现
 */
public class DoubleMoveSkill implements Skill, RestorableSkill {
    private boolean active = false;
    private boolean inProgress = false;
    private int remainingUses = 1;
    private static final int MAX_USES = 1;
    
    @Override
    public String getName() {
        return "荧惑守心·双曜";
    }
    
    @Override
    public String getDescription() {
        return "连续落两个子";
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
        return remainingUses > 0 
            && !active 
            && !inProgress;
    }
    
    @Override
    public boolean activate(WuziqiPanel panel) {
        if (canActivate(panel)) {
            active = true;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isActive() {
        return active || inProgress;
    }
    
    @Override
    public void reset() {
        active = false;
        inProgress = false;
        remainingUses = MAX_USES;
    }
    
    @Override
    public void updateMenuItem(JMenuItem menuItem, WuziqiPanel panel) {
        if (menuItem == null) return;
        
        // 只在人人对弈模式下显示
        if (panel.model != DyModel.P2P) {
            menuItem.setEnabled(false);
            menuItem.setText("荧惑守心·双曜 (仅人人对弈)");
            return;
        }
        
        if (remainingUses > 0) {
            if (active || inProgress) {
                menuItem.setText("荧惑守心·双曜 (激活中)");
                menuItem.setEnabled(false);
            } else {
                menuItem.setText("荧惑守心·双曜 (" + remainingUses + "次)");
                menuItem.setEnabled(canActivate(panel));
            }
        } else {
            menuItem.setText("荧惑守心·双曜 (已用完)");
            menuItem.setEnabled(false);
        }
    }
    
    @Override
    public boolean onPiecePlaced(WuziqiPanel panel, int row, int col) {
        // 只在人人对弈模式下处理
        if (panel.model != DyModel.P2P) {
            return true;
        }
        
        if (active && !inProgress) {
            // 第一次落子，进入双落子模式
            inProgress = true;
            active = false; // 标记为已使用
            // 不切换回合，等待第二次落子
            return false; // 拦截，不切换回合
        } else if (inProgress) {
            // 第二次落子，完成双落子
            inProgress = false;
            remainingUses = 0;
            // 继续正常流程，切换回合
            return true;
        }
        // 技能未激活，继续正常流程
        return true;
    }
    
    @Override
    public void restoreState(int remainingUses, boolean isActive) {
        this.remainingUses = remainingUses;
        this.active = isActive;
        // 如果技能已激活但不在进行中，说明是刚激活但还没落子
        // 如果技能已激活且在进行中，说明已经落了第一个子
        // 这里我们只恢复基本状态，inProgress 会在下次落子时根据情况设置
        this.inProgress = false; // 读取时重置进行状态
    }
}
