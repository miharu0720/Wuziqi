package com.zstu.javalesson.wuziqi;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionMenuActionListener implements ActionListener {

    WuziqiForm f;
    JRadioButtonMenuItem p2pMenuItem, p2mMenuItem;

    public OptionMenuActionListener(WuziqiForm f,
                                    JRadioButtonMenuItem p2pMenuItem,
                                    JRadioButtonMenuItem p2mMenuItem){
        this.f = f;
        this.p2pMenuItem = p2pMenuItem;
        this.p2mMenuItem = p2mMenuItem;
    }
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource().equals(p2pMenuItem)){
            f.panel.model = DyModel.P2P;
            System.out.println("对弈模式：人人");
        }
        if(e.getSource().equals(p2mMenuItem)){
            f.panel.model = DyModel.P2M;
            System.out.println("对弈模式：人机");
        }
        WuziqiModel.clear();
        f.panel.turns = Turns.BLACK;
        f.panel.updateStatusText();
        f.panel.resetClockAndRestart();
        // 重置所有技能状态
        f.panel.getSkillManager().resetAll();
        f.panel.repaint();
    }
}
