package com.zstu.javalesson.wuziqi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GameMenuActionListener implements ActionListener {
    WuziqiForm f;
    public GameMenuActionListener(WuziqiForm f){
        this.f = f;
    }
    @Override
    public void actionPerformed(ActionEvent e){
        if (e.getSource().equals(f.newItem)){
            WuziqiModel.clear();
            f.panel.repaint();
            f.panel.turns = Turns.BLACK;
            f.panel.updateStatusText();
            f.panel.resetClockAndRestart();
            // 重置所有技能状态
            f.panel.getSkillManager().resetAll();
        }
        if(e.getSource().equals(f.saveItem)){
            WuziqiModel.saveAs(new File("data/game.dat"), f.panel.getSkillManager(), f.panel.turns, f.panel.model);
        }
        if(e.getSource().equals(f.readItem)){
            WuziqiModel.readForm(new File("data/game.dat"), f.panel.getSkillManager(), f.panel);
            f.panel.repaint();
            
            // 根据恢复的模式更新菜单选择
            if (f.panel.model == DyModel.P2P) {
                f.p2pMenuItem.setSelected(true);
            } else {
                f.p2mMenuItem.setSelected(true);
            }
            
            // 更新状态栏和技能菜单项
            f.panel.updateStatusText();
        }
    }
}
