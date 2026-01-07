package com.zstu.javalesson.wuziqi;

import javax.swing.JButton;
import javax.swing.JMenuItem;

/**
 * 技能接口 - 所有技能必须实现此接口
 */
public interface Skill {
    /**
     * 获取技能名称
     */
    String getName();
    
    /**
     * 获取技能描述
     */
    String getDescription();
    
    /**
     * 获取剩余使用次数
     */
    int getRemainingUses();
    
    /**
     * 获取最大使用次数（每局）
     */
    int getMaxUses();
    
    /**
     * 检查技能是否可以激活
     * @param panel 游戏面板
     * @return 是否可以激活
     */
    boolean canActivate(WuziqiPanel panel);
    
    /**
     * 激活技能
     * @param panel 游戏面板
     * @return 是否激活成功
     */
    boolean activate(WuziqiPanel panel);
    
    /**
     * 检查技能是否已激活
     */
    boolean isActive();
    
    /**
     * 重置技能状态（新局开始时调用）
     */
    void reset();
    
    /**
     * 更新技能按钮状态（兼容旧代码）
     * @param button 技能按钮
     * @param panel 游戏面板
     */
    default void updateButton(JButton button, WuziqiPanel panel) {
        // 默认实现，可以忽略
    }
    
    /**
     * 更新技能菜单项状态
     * @param menuItem 技能菜单项
     * @param panel 游戏面板
     */
    void updateMenuItem(JMenuItem menuItem, WuziqiPanel panel);
    
    /**
     * 处理落子逻辑（如果需要）
     * @param panel 游戏面板
     * @param row 行
     * @param col 列
     * @return 是否继续处理（true=继续正常流程，false=拦截）
     */
    boolean onPiecePlaced(WuziqiPanel panel, int row, int col);
    
    /**
     * 处理点击已有棋子的事件（用于特殊技能，如星孛袭野·夺子）
     * @param panel 游戏面板
     * @param row 行
     * @param col 列
     * @return 是否处理了该事件（true=已处理，false=未处理，继续正常流程）
     */
    default boolean onPieceClicked(WuziqiPanel panel, int row, int col) {
        return false; // 默认不处理
    }
}

