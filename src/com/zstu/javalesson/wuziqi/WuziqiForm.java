package com.zstu.javalesson.wuziqi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WuziqiForm extends JFrame {

    WuziqiPanel panel;

    // 游戏菜单
    JMenu gameMenu;
    JMenuItem newItem;
    JMenuItem saveItem;
    JMenuItem readItem;

    JRadioButtonMenuItem p2pMenuItem;
    JRadioButtonMenuItem p2mMenuItem;

    public WuziqiForm(){
        setTitle("五子棋小游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 使用边界布局，上方棋盘，下方按钮
        setLayout(new BorderLayout());

        panel = new WuziqiPanel();

        initMenu();  // 初始化菜单栏

        // 棋盘放在中间
        add(panel, BorderLayout.CENTER);

        // 底部按钮栏 + 状态栏 + 棋钟
        JPanel bottomPanel = new JPanel();
        // 中间对齐，间距适中
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // 悔棋按钮
        JButton undoButton = new JButton("悔棋");
        // 调整按钮字体大小
        undoButton.setFont(new Font("微软雅黑", Font.BOLD, 24));
        // 调整按钮内边距，让按钮更大
        undoButton.setMargin(new Insets(8, 30, 8, 30));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.undo();
            }
        });
        bottomPanel.add(undoButton);

        // 状态栏：显示当前模式和轮到谁下
        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        bottomPanel.add(statusLabel);
        // 让棋盘面板可以更新状态栏文字
        panel.setStatusLabel(statusLabel);

        // 棋钟：黑棋 / 白棋用时
        JLabel blackTimeLabel = new JLabel();
        JLabel whiteTimeLabel = new JLabel();
        blackTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        whiteTimeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        bottomPanel.add(blackTimeLabel);
        bottomPanel.add(whiteTimeLabel);
        panel.setClockLabels(blackTimeLabel, whiteTimeLabel);

        add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void initMenu(){
        // 创建菜单条目：游戏，选项，关于
        // 游戏菜单
        gameMenu = new JMenu("游戏");
        newItem = new JMenuItem("新建游戏");
        saveItem = new JMenuItem("保存游戏");
        readItem = new JMenuItem("读取游戏");
        JMenuItem exitItem = new JMenuItem("退出游戏");

        GameMenuActionListener gmal = new GameMenuActionListener(this);
        newItem.addActionListener(gmal);
        saveItem.addActionListener(gmal);
        readItem.addActionListener(gmal);

        // 结束菜单简单消息处理，使用匿名对象
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });

        // 组装菜单
        gameMenu.add(newItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(readItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // 选项菜单
        JMenu optionMenu = new JMenu("游戏选项");
        p2pMenuItem = new JRadioButtonMenuItem("人人对弈");
        p2mMenuItem = new JRadioButtonMenuItem("人机对弈");

        ButtonGroup group = new ButtonGroup();
        group.add(p2mMenuItem);
        group.add(p2pMenuItem);

        p2mMenuItem.setSelected(true);

        optionMenu.add(p2pMenuItem);
        optionMenu.add(p2mMenuItem);
        OptionMenuActionListener omal = new OptionMenuActionListener(this,
                p2pMenuItem,
                p2mMenuItem);
        p2pMenuItem.addActionListener(omal);
        p2mMenuItem.addActionListener(omal);

        // 棋盘大小子菜单
        JMenu sizeMenu = new JMenu("棋盘大小");
        JRadioButtonMenuItem size9Item = new JRadioButtonMenuItem("9 路棋盘");
        JRadioButtonMenuItem size13Item = new JRadioButtonMenuItem("13 路棋盘");
        JRadioButtonMenuItem size19Item = new JRadioButtonMenuItem("17 路棋盘");
        ButtonGroup sizeGroup = new ButtonGroup();
        sizeGroup.add(size9Item);
        sizeGroup.add(size13Item);
        sizeGroup.add(size19Item);
        // 默认选中 13 路棋盘
        size13Item.setSelected(true);

        BoardSizeMenuActionListener bsmal =
                new BoardSizeMenuActionListener(this, size9Item, size13Item, size19Item);
        size9Item.addActionListener(bsmal);
        size13Item.addActionListener(bsmal);
        size19Item.addActionListener(bsmal);

        sizeMenu.add(size9Item);
        sizeMenu.add(size13Item);
        sizeMenu.add(size19Item);

        optionMenu.addSeparator();
        optionMenu.add(sizeMenu);

        // 主题子菜单
        JMenu themeMenu = new JMenu("主题");
        JRadioButtonMenuItem lightThemeItem = new JRadioButtonMenuItem("淡木色");
        JRadioButtonMenuItem darkThemeItem = new JRadioButtonMenuItem("深木色");
        JRadioButtonMenuItem greenThemeItem = new JRadioButtonMenuItem("绿色棋盘");
        JRadioButtonMenuItem grayThemeItem = new JRadioButtonMenuItem("灰色棋盘");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(lightThemeItem);
        themeGroup.add(darkThemeItem);
        themeGroup.add(greenThemeItem);
        themeGroup.add(grayThemeItem);
        // 默认选中深木色
        darkThemeItem.setSelected(true);

        lightThemeItem.addActionListener(e -> {
            panel.setTheme(Theme.LIGHT_WOOD);
        });
        darkThemeItem.addActionListener(e -> {
            panel.setTheme(Theme.DARK_WOOD);
        });
        greenThemeItem.addActionListener(e -> {
            panel.setTheme(Theme.GREEN_BOARD);
        });
        grayThemeItem.addActionListener(e -> {
            panel.setTheme(Theme.GRAY_BOARD);
        });

        themeMenu.add(lightThemeItem);
        themeMenu.add(darkThemeItem);
        themeMenu.add(greenThemeItem);
        themeMenu.add(grayThemeItem);

        optionMenu.addSeparator();
        optionMenu.add(themeMenu);
        
        // AI难度子菜单
        JMenu difficultyMenu = new JMenu("AI难度");
        JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("简单");
        JRadioButtonMenuItem mediumItem = new JRadioButtonMenuItem("中等");
        JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("困难");
        ButtonGroup difficultyGroup = new ButtonGroup();
        difficultyGroup.add(easyItem);
        difficultyGroup.add(mediumItem);
        difficultyGroup.add(hardItem);
        // 默认选中困难
        hardItem.setSelected(true);
        
        easyItem.addActionListener(e -> {
            panel.setDifficulty(Difficulty.EASY);
        });
        mediumItem.addActionListener(e -> {
            panel.setDifficulty(Difficulty.MEDIUM);
        });
        hardItem.addActionListener(e -> {
            panel.setDifficulty(Difficulty.HARD);
        });
        
        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);
        
        optionMenu.addSeparator();
        optionMenu.add(difficultyMenu);

        // 技能菜单（只在人人对弈模式下可用）
        JMenu skillMenu = new JMenu("技能");
        JMenuItem doubleMoveMenuItem = new JMenuItem("荧惑守心·双曜");
        JMenuItem provokeMenuItem = new JMenuItem("星孛袭野·夺子");
        JMenuItem devourMenuItem = new JMenuItem("天罡肃野·吞阵");
        JMenuItem rotateMenuItem = new JMenuItem("斗柄回寅·轮转");
        
        doubleMoveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SkillManager skillManager = panel.getSkillManager();
                if (skillManager.activateSkill(SkillType.DOUBLE_MOVE)) {
                    JOptionPane.showMessageDialog(null, 
                        "荧惑守心·双曜技能已激活！\n下一次轮到您时，可以连续落两个子。", 
                        "技能激活", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String message = skillManager.getActivationFailureReason(SkillType.DOUBLE_MOVE);
                    JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        provokeMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SkillManager skillManager = panel.getSkillManager();
                if (skillManager.activateSkill(SkillType.PROVOKE)) {
                    // 技能激活成功，提示信息已在技能内部显示
                } else {
                    String message = skillManager.getActivationFailureReason(SkillType.PROVOKE);
                    JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        devourMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SkillManager skillManager = panel.getSkillManager();
                if (skillManager.activateSkill(SkillType.DEVOUR)) {
                    // 技能激活成功，提示信息已在技能内部显示
                } else {
                    String message = skillManager.getActivationFailureReason(SkillType.DEVOUR);
                    JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        rotateMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SkillManager skillManager = panel.getSkillManager();
                if (skillManager.activateSkill(SkillType.ROTATE)) {
                    // 技能激活成功，提示信息已在技能内部显示
                } else {
                    String message = skillManager.getActivationFailureReason(SkillType.ROTATE);
                    JOptionPane.showMessageDialog(null, message, "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        skillMenu.add(doubleMoveMenuItem);
        skillMenu.add(provokeMenuItem);
        skillMenu.add(devourMenuItem);
        skillMenu.add(rotateMenuItem);
        
        // 注册技能菜单项到技能管理器
        panel.getSkillManager().registerMenuItem(SkillType.DOUBLE_MOVE, doubleMoveMenuItem);
        panel.getSkillManager().registerMenuItem(SkillType.PROVOKE, provokeMenuItem);
        panel.getSkillManager().registerMenuItem(SkillType.DEVOUR, devourMenuItem);
        panel.getSkillManager().registerMenuItem(SkillType.ROTATE, rotateMenuItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutMenuItem = new JMenuItem("关于本游戏");

        // 关于本游戏消息单独处理
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "破界连星，此乃神之一手定虚空！",
                        "关于五子棋游戏",
                        JOptionPane.INFORMATION_MESSAGE,
                        null); // 最后一个参数和图标相关
            }
        });
        helpMenu.add(aboutMenuItem);
        
        // 添加分隔符
        helpMenu.addSeparator();
        
        // 作者信息菜单项
        JMenuItem authorMenuItem = new JMenuItem("作者信息");
        authorMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "https://github.com/miharu0720",
                        "作者信息",
                        JOptionPane.INFORMATION_MESSAGE,
                        null);
            }
        });
        helpMenu.add(authorMenuItem);

        // 创建菜单条，加载菜单
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(gameMenu);
        menuBar.add(optionMenu);
        menuBar.add(skillMenu);
        menuBar.add(helpMenu);
    }
}
