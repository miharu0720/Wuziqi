package com.zstu.javalesson.wuziqi;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 棋盘大小菜单监听器：9x9 / 13x13 / 19x19
 */
public class BoardSizeMenuActionListener implements ActionListener {

    private final WuziqiForm form;
    private final JRadioButtonMenuItem size9;
    private final JRadioButtonMenuItem size13;
    private final JRadioButtonMenuItem size19;

    public BoardSizeMenuActionListener(WuziqiForm form,
                                       JRadioButtonMenuItem size9,
                                       JRadioButtonMenuItem size13,
                                       JRadioButtonMenuItem size19) {
        this.form = form;
        this.size9 = size9;
        this.size13 = size13;
        this.size19 = size19;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int n = 13; // 默认
        if (source.equals(size9)) {
            n = 9;
        } else if (source.equals(size13)) {
            n = 13;
        } else if (source.equals(size19)) {
            n = 17;
        }

        // 修改棋盘大小并重置棋局
        WuziqiModel.setBoardSize(n, n);
        form.panel.turns = Turns.BLACK;
        form.panel.updateStatusText();
        form.panel.resetSize();
        form.panel.resetClockAndRestart();
        // 重置所有技能状态
        form.panel.getSkillManager().resetAll();

        // 重新布局窗口
        form.pack();
        form.setLocationRelativeTo(null);
    }
}


