package com.zstu.javalesson.wuziqi;

import java.awt.*;

/**
 * 棋盘配色主题
 */
public enum Theme {
    // 淡木色棋盘
    LIGHT_WOOD(new Color(235, 210, 165), new Color(120, 80, 40)),
    // 深木色棋盘（当前默认）
    DARK_WOOD(new Color(80, 30, 0), new Color(0, 0, 0)),
    // 绿色棋盘（类似围棋绿毯）
    GREEN_BOARD(new Color(60, 110, 60), new Color(20, 40, 20)),
    // 冷灰棋盘
    GRAY_BOARD(new Color(200, 200, 210), new Color(80, 80, 90));

    private final Color boardColor; // 棋盘背景色
    private final Color lineColor;  // 网格线颜色

    Theme(Color boardColor, Color lineColor) {
        this.boardColor = boardColor;
        this.lineColor = lineColor;
    }

    public Color getBoardColor() {
        return boardColor;
    }

    public Color getLineColor() {
        return lineColor;
    }
}



