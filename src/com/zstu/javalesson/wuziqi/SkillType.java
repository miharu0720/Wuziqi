package com.zstu.javalesson.wuziqi;

/**
 * 技能类型枚举
 */
public enum SkillType {
    DOUBLE_MOVE("荧惑守心·双曜"),
    PROVOKE("星孛袭野·夺子"),
    DEVOUR("天罡肃野·吞阵"),
    ROTATE("斗柄回寅·轮转");
    // 后续可以继续添加新技能类型
    
    private final String displayName;
    
    SkillType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

