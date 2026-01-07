package com.zstu.javalesson.wuziqi;

/**
 * 可恢复状态的技能接口（用于保存/读取游戏）
 */
public interface RestorableSkill {
    /**
     * 恢复技能状态
     * @param remainingUses 剩余使用次数
     * @param isActive 是否激活
     */
    void restoreState(int remainingUses, boolean isActive);
}

