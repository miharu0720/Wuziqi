package com.zstu.javalesson.wuziqi;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenuItem;

/**
 * 技能管理器 - 统一管理所有技能
 * 支持双方玩家各自的技能（黑棋和白棋）
 */
public class SkillManager {
    // 黑棋和白棋各自的技能
    private Map<SkillType, Skill> blackSkills;
    private Map<SkillType, Skill> whiteSkills;
    private Map<SkillType, JMenuItem> skillMenuItems;
    private WuziqiPanel panel;
    
    public SkillManager(WuziqiPanel panel) {
        this.panel = panel;
        this.blackSkills = new HashMap<>();
        this.whiteSkills = new HashMap<>();
        this.skillMenuItems = new HashMap<>();
        
        // 为双方注册所有技能
        registerSkill(SkillType.DOUBLE_MOVE, new DoubleMoveSkill());
        registerSkill(SkillType.PROVOKE, new ProvokeSkill());
        registerSkill(SkillType.DEVOUR, new DevourSkill());
        registerSkill(SkillType.ROTATE, new RotateSkill());
        // 后续添加新技能时，只需在这里注册
    }
    
    /**
     * 注册技能（为双方玩家各创建一个实例）
     */
    public void registerSkill(SkillType type, Skill skillTemplate) {
        // 为黑棋和白棋各创建一个技能实例
        try {
            Skill blackSkill = skillTemplate.getClass().getDeclaredConstructor().newInstance();
            Skill whiteSkill = skillTemplate.getClass().getDeclaredConstructor().newInstance();
            blackSkills.put(type, blackSkill);
            whiteSkills.put(type, whiteSkill);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前玩家的技能
     */
    public Skill getCurrentPlayerSkill(SkillType type) {
        if (panel.turns == Turns.BLACK) {
            return blackSkills.get(type);
        } else {
            return whiteSkills.get(type);
        }
    }
    
    /**
     * 获取技能（兼容旧代码）
     */
    public Skill getSkill(SkillType type) {
        return getCurrentPlayerSkill(type);
    }
    
    /**
     * 注册技能菜单项
     */
    public void registerMenuItem(SkillType type, JMenuItem menuItem) {
        skillMenuItems.put(type, menuItem);
        updateMenuItem(type);
    }
    
    /**
     * 激活技能（当前玩家）
     */
    public boolean activateSkill(SkillType type) {
        // 只在人人对弈模式下可用
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        Skill skill = getCurrentPlayerSkill(type);
        if (skill != null && skill.activate(panel)) {
            updateMenuItem(type);
            return true;
        }
        return false;
    }
    
    /**
     * 更新技能菜单项状态
     */
    public void updateMenuItem(SkillType type) {
        Skill skill = getCurrentPlayerSkill(type);
        JMenuItem menuItem = skillMenuItems.get(type);
        if (skill != null && menuItem != null) {
            skill.updateMenuItem(menuItem, panel);
        }
    }
    
    /**
     * 更新所有技能菜单项状态
     */
    public void updateAllMenuItems() {
        for (SkillType type : blackSkills.keySet()) {
            updateMenuItem(type);
        }
    }
    
    /**
     * 处理落子逻辑
     * @param row 行
     * @param col 列
     * @return 是否继续处理（true=继续正常流程，false=拦截）
     */
    public boolean onPiecePlaced(int row, int col) {
        // 只在人人对弈模式下处理技能
        if (panel.model != DyModel.P2P) {
            return true;
        }
        
        // 检查当前玩家的技能
        Map<SkillType, Skill> currentSkills = (panel.turns == Turns.BLACK) ? blackSkills : whiteSkills;
        for (Skill skill : currentSkills.values()) {
            if (skill.isActive() && !skill.onPiecePlaced(panel, row, col)) {
                // 被技能拦截，不继续正常流程
                updateAllMenuItems();
                return false;
            }
        }
        // 所有技能都允许继续，更新菜单项状态
        updateAllMenuItems();
        return true; // 继续正常流程
    }
    
    /**
     * 重置所有技能（双方玩家）
     */
    public void resetAll() {
        for (Skill skill : blackSkills.values()) {
            skill.reset();
        }
        for (Skill skill : whiteSkills.values()) {
            skill.reset();
        }
        updateAllMenuItems();
    }
    
    /**
     * 处理点击已有棋子的事件
     * @param row 行
     * @param col 列
     * @return 是否处理了该事件（true=已处理，false=未处理）
     */
    public boolean onPieceClicked(int row, int col) {
        // 只在人人对弈模式下处理技能
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        // 检查当前玩家的技能
        Map<SkillType, Skill> currentSkills = (panel.turns == Turns.BLACK) ? blackSkills : whiteSkills;
        for (Skill skill : currentSkills.values()) {
            if (skill.isActive() && skill.onPieceClicked(panel, row, col)) {
                // 技能已处理该事件
                updateAllMenuItems();
                return true;
            }
        }
        // 没有技能处理该事件
        return false;
    }
    
    /**
     * 处理点击空位置的事件（用于某些技能，如天罡肃野·吞阵、斗柄回寅·轮转）
     * @param row 行
     * @param col 列
     * @return 是否处理了该事件（true=已处理，false=未处理）
     */
    public boolean onEmptyClicked(int row, int col) {
        // 只在人人对弈模式下处理技能
        if (panel.model != DyModel.P2P) {
            return false;
        }
        
        // 检查当前玩家的技能
        Map<SkillType, Skill> currentSkills = (panel.turns == Turns.BLACK) ? blackSkills : whiteSkills;
        for (Skill skill : currentSkills.values()) {
            // 检查是否是 DevourSkill 且已激活
            if (skill instanceof DevourSkill && skill.isActive()) {
                DevourSkill devourSkill = (DevourSkill) skill;
                if (devourSkill.onEmptyClicked(panel, row, col)) {
                    // 技能已处理该事件
                    updateAllMenuItems();
                    return true;
                }
            }
            // 检查是否是 RotateSkill 且已激活
            if (skill instanceof RotateSkill && skill.isActive()) {
                RotateSkill rotateSkill = (RotateSkill) skill;
                if (rotateSkill.onEmptyClicked(panel, row, col)) {
                    // 技能已处理该事件
                    updateAllMenuItems();
                    return true;
                }
            }
        }
        // 没有技能处理该事件
        return false;
    }
    
    /**
     * 获取技能激活失败的原因
     */
    public String getActivationFailureReason(SkillType type) {
        if (panel.model != DyModel.P2P) {
            return "技能只在人人对弈模式下可用！";
        }
        
        Skill skill = getCurrentPlayerSkill(type);
        if (skill == null) {
            return "技能不存在";
        }
        if (skill.getRemainingUses() <= 0) {
            return skill.getName() + "技能已用完！";
        }
        if (skill.isActive()) {
            return skill.getName() + "技能已激活，请先完成！";
        }
        if (!skill.canActivate(panel)) {
            // 根据技能类型返回更具体的提示
            if (type == SkillType.PROVOKE) {
                // 检查是否有敌方棋子
                int enemyPiece = (panel.turns == Turns.BLACK) ? 2 : 1;
                boolean hasEnemyPieces = false;
                for (int i = 0; i < WuziqiModel.ROWS; i++) {
                    for (int j = 0; j < WuziqiModel.COLS; j++) {
                        if (WuziqiModel.array[i][j] == enemyPiece) {
                            hasEnemyPieces = true;
                            break;
                        }
                    }
                    if (hasEnemyPieces) break;
                }
                if (!hasEnemyPieces) {
                    return "棋盘上没有敌方棋子，无法使用" + skill.getName() + "技能！";
                }
                return "只能在轮到您下棋时激活" + skill.getName() + "技能！";
            }
            return "只能在轮到您下棋时激活" + skill.getName() + "技能！";
        }
        return "无法激活" + skill.getName() + "技能";
    }
    
    /**
     * 获取黑棋技能状态（用于保存）
     */
    public Map<SkillType, SkillState> getBlackSkillStates() {
        Map<SkillType, SkillState> states = new HashMap<>();
        for (SkillType type : blackSkills.keySet()) {
            Skill skill = blackSkills.get(type);
            if (skill != null) {
                states.put(type, new SkillState(skill.getRemainingUses(), skill.isActive()));
            }
        }
        return states;
    }
    
    /**
     * 获取白棋技能状态（用于保存）
     */
    public Map<SkillType, SkillState> getWhiteSkillStates() {
        Map<SkillType, SkillState> states = new HashMap<>();
        for (SkillType type : whiteSkills.keySet()) {
            Skill skill = whiteSkills.get(type);
            if (skill != null) {
                states.put(type, new SkillState(skill.getRemainingUses(), skill.isActive()));
            }
        }
        return states;
    }
    
    /**
     * 恢复技能状态（用于读取）
     */
    public void restoreSkillStates(Map<SkillType, SkillState> blackStates, Map<SkillType, SkillState> whiteStates) {
        // 恢复黑棋技能状态
        for (SkillType type : blackStates.keySet()) {
            Skill skill = blackSkills.get(type);
            if (skill != null) {
                SkillState state = blackStates.get(type);
                restoreSkillState(skill, state);
            }
        }
        
        // 恢复白棋技能状态
        for (SkillType type : whiteStates.keySet()) {
            Skill skill = whiteSkills.get(type);
            if (skill != null) {
                SkillState state = whiteStates.get(type);
                restoreSkillState(skill, state);
            }
        }
        
        updateAllMenuItems();
    }
    
    /**
     * 恢复单个技能状态
     */
    private void restoreSkillState(Skill skill, SkillState state) {
        // 使用反射或直接调用方法来恢复状态
        // 由于技能状态是私有的，我们需要在 Skill 接口中添加恢复方法
        if (skill instanceof RestorableSkill) {
            ((RestorableSkill) skill).restoreState(state.remainingUses, state.isActive);
        }
    }
    
    /**
     * 技能状态数据类
     */
    public static class SkillState {
        public int remainingUses;
        public boolean isActive;
        
        public SkillState(int remainingUses, boolean isActive) {
            this.remainingUses = remainingUses;
            this.isActive = isActive;
        }
    }
}
