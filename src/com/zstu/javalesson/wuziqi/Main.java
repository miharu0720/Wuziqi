package com.zstu.javalesson.wuziqi;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        UIManager.put("Menu.font", new Font("宋体",Font.PLAIN,28));
        UIManager.put("MenuItem.font", new Font("宋体",Font.PLAIN,28));
        UIManager.put("RadioButtonMenuItem.font", new Font("宋体",Font.PLAIN,28));
        UIManager.put("Menu.margin", new Insets(8,8,8,8));     // 菜单边距
        UIManager.put("MenuItem.Margin", new Insets(6,6,6,6)); // 菜单项边距

        UIManager.put("OptionPane.messageFont",
                new FontUIResource(new Font("微软雅黑", Font.PLAIN, 24)));
        UIManager.put("OptionPane.buttonFont",
                new FontUIResource(new Font("微软雅黑", Font.PLAIN, 20)));

        new WuziqiForm();
    }
}