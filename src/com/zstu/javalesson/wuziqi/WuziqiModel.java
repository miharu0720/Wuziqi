package com.zstu.javalesson.wuziqi;

import java.io.*;

public class WuziqiModel implements WuziqiConsts{
    // 当前棋盘大小（行数、列数），默认 13x13
    public static int ROWS = 13, COLS = 13;

    // 棋盘落子信息：0=空，1=黑子，2=白子
    static int[][] array = new int[ROWS][COLS];
    // 每个位置对应的手数（第几手），0 表示未落子
    static int[][] steps = new int[ROWS][COLS];
    // 当前总手数
    static int stepCount = 0;

    // 修改棋盘大小，并重置棋盘与手数
    public static void setBoardSize(int rows, int cols) {
        ROWS = rows;
        COLS = cols;
        array = new int[ROWS][COLS];
        steps = new int[ROWS][COLS];
        clear();
    }

    static int[] pixels2ij(int px, int py, int start_x, int start_y){
        int [] ij = new int[2];
        int i = Math.round((float)(py-start_y) / CELL_WIDTH);
        int j = Math.round((float)(px-start_x) / CELL_WIDTH);
        ij[0] = i;
        ij[1] = j;
        return ij;
    }
    static int[] ij2pixels(int start_x, int start_y, int i, int j){
        int[] pixels = new int[2];
        int x = start_x + j*CELL_WIDTH;
        int y = start_y + i*CELL_WIDTH;
        pixels[0] = x;
        pixels[1] = y;
        return pixels;
    }
    // 把棋盘清空为未下子状态
    public static void clear() {
        stepCount = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                array[i][j] = 0;
                steps[i][j] = 0;
            }
        }
    }
    // 保存游戏
    static void saveAs(File file, SkillManager skillManager, Turns currentTurn, DyModel gameModel){
        FileWriter fw = null;
        try {
            // 确保文件所在的目录存在
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            fw = new FileWriter(file);
            // 保存棋盘状态
            for (int i = 0; i < array.length; i++) {
                String line = new String();
                for (int j = 0; j < array[i].length; j++) {
                    line = line+array[i][j];
                }
                line = line + "\n";
                char[] chars = line.toCharArray();
                fw.write(chars, 0, chars.length);
            }
            // 保存游戏状态（轮到谁下棋和游戏模式）
            fw.write("GAME_STATE\n");
            fw.write("TURN:" + currentTurn.name() + "\n");
            fw.write("MODEL:" + gameModel.name() + "\n");
            
            // 保存技能状态（如果存在技能管理器且是人人对弈模式）
            if (skillManager != null) {
                fw.write("SKILLS\n");
                // 保存黑棋技能状态
                java.util.Map<SkillType, SkillManager.SkillState> blackStates = skillManager.getBlackSkillStates();
                for (SkillType type : blackStates.keySet()) {
                    SkillManager.SkillState state = blackStates.get(type);
                    fw.write("BLACK:" + type.name() + ":" + state.remainingUses + ":" + state.isActive + "\n");
                }
                // 保存白棋技能状态
                java.util.Map<SkillType, SkillManager.SkillState> whiteStates = skillManager.getWhiteSkillStates();
                for (SkillType type : whiteStates.keySet()) {
                    SkillManager.SkillState state = whiteStates.get(type);
                    fw.write("WHITE:" + type.name() + ":" + state.remainingUses + ":" + state.isActive + "\n");
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 保存游戏（兼容旧代码，不保存技能状态和游戏状态）
    static void saveAs(File file, SkillManager skillManager){
        saveAs(file, skillManager, null, null);
    }
    
    // 保存游戏（兼容旧代码，不保存技能状态）
    static void saveAs(File file){
        saveAs(file, null);
    }
    // 读取游戏
    static void readForm(File file, SkillManager skillManager, WuziqiPanel panel){
        FileReader fr = null;
        BufferedReader br = null;

        try{
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String line = null;
            int i = 0;
            boolean readingGameState = false;
            boolean readingSkills = false;
            Turns savedTurn = null;
            DyModel savedModel = null;
            java.util.Map<SkillType, SkillManager.SkillState> blackStates = new java.util.HashMap<>();
            java.util.Map<SkillType, SkillManager.SkillState> whiteStates = new java.util.HashMap<>();
            
            // 读取前先清空棋盘和手数
            clear();

            while((line=br.readLine())!=null){
                if (line.equals("GAME_STATE")) {
                    readingGameState = true;
                    readingSkills = false;
                    continue;
                }
                
                if (line.equals("SKILLS")) {
                    readingSkills = true;
                    readingGameState = false;
                    continue;
                }
                
                if (readingGameState) {
                    // 解析游戏状态
                    if (line.startsWith("TURN:")) {
                        String turnName = line.substring(5);
                        try {
                            savedTurn = Turns.valueOf(turnName);
                        } catch (IllegalArgumentException e) {
                            // 忽略无效的回合值
                        }
                    } else if (line.startsWith("MODEL:")) {
                        String modelName = line.substring(6);
                        try {
                            savedModel = DyModel.valueOf(modelName);
                        } catch (IllegalArgumentException e) {
                            // 忽略无效的模式值
                        }
                    }
                } else if (readingSkills) {
                    // 解析技能状态
                    String[] parts = line.split(":");
                    if (parts.length == 4) {
                        String player = parts[0];
                        String skillTypeName = parts[1];
                        int remainingUses = Integer.parseInt(parts[2]);
                        boolean isActive = Boolean.parseBoolean(parts[3]);
                        
                        try {
                            SkillType type = SkillType.valueOf(skillTypeName);
                            SkillManager.SkillState state = new SkillManager.SkillState(remainingUses, isActive);
                            if (player.equals("BLACK")) {
                                blackStates.put(type, state);
                            } else if (player.equals("WHITE")) {
                                whiteStates.put(type, state);
                            }
                        } catch (IllegalArgumentException e) {
                            // 忽略未知的技能类型
                        }
                    }
                } else {
                    // 读取棋盘状态
                    char[] chars = line.toCharArray();
                    for (int j = 0; j < chars.length; j++) {
                        if(j != chars.length-1){
                            String str = ""+chars[j];
                            array[i][j] = Integer.parseInt(str);
                        }
                    }
                    ++i;
                }
            }
            
            // 读取完成后，根据棋盘重新为已有棋子分配一个"顺序手数"
            stepCount = 0;
            for (int r = 0; r < array.length; r++) {
                for (int c = 0; c < array[r].length; c++) {
                    if (array[r][c] != 0) {
                        steps[r][c] = ++stepCount;
                    }
                }
            }
            
            // 恢复游戏状态
            if (panel != null) {
                if (savedTurn != null) {
                    panel.turns = savedTurn;
                } else {
                    // 向后兼容：如果没有保存的回合信息，根据落子数判断
                    int count = notZero();
                    panel.turns = (count % 2 == 0) ? Turns.BLACK : Turns.WHITE;
                }
                
                if (savedModel != null) {
                    panel.model = savedModel;
                } else {
                    // 向后兼容：如果没有保存的模式信息，根据落子数判断
                    int count = notZero();
                    if (count % 2 != 0) {
                        panel.model = DyModel.P2P;
                    }
                }
            }
            
            // 恢复技能状态
            if (skillManager != null && (!blackStates.isEmpty() || !whiteStates.isEmpty())) {
                skillManager.restoreSkillStates(blackStates, whiteStates);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // 读取游戏（兼容旧代码，不恢复游戏状态）
    static void readForm(File file, SkillManager skillManager){
        readForm(file, skillManager, null);
    }
    
    // 读取游戏（兼容旧代码，不恢复技能状态）
    static void readForm(File file){
        readForm(file, null);
    }
    // 统计落子个数
    static int notZero(){
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if(array[i][j] != 0)
                    count++;
            }
        }
        return count;
    }
    // 判别胜负
    public static boolean is5PieceConnected(int[][] a, int i, int j){
        if(a[i][j]!=0){
            int i0 = i,j0 = j;
            int count = 1;
            // 东西方向
            while(true){// 向东搜索
                ++j;
                if (j >= COLS) break;
                if (a[i][j]!=a[i0][j0]) break;
                ++count;
            }
            j = j0;
            while(true){// 向西搜索
                --j;
                if (j < 0) break;
                if (a[i][j]!=a[i0][j0]) break;
                ++count;
            }
            // 判断count是否为5
            if (count >= 5) return true;
            count = 1;
            // 重置坐标，准备检查南北方向
            i = i0;
            j = j0;

            // 南北方向
            while(true){// 向南搜索
                ++i;
                if (i >= ROWS) break;
                if (a[i][j]!=a[i0][j0]) break;
                ++count;
            }
            i = i0;
            while(true){// 向北搜索
                --i;
                if (i < 0) break;
                if (a[i][j]!=a[i0][j0]) break;
                ++count;
            }
            // 判断count是否为5
            if (count >= 5) return true;
            count = 1;
            i = i0;

            // 西北-东南方向（左上到右下）
            while(true){ // 向东南搜索（右下）
                ++i; ++j;
                if (i >= ROWS || j >= COLS) break;
                if (a[i][j] != a[i0][j0]) break;
                ++count;
            }
            i = i0; j = j0;  // 重置坐标
            while(true){ // 向西北搜索（左上）
                --i; --j;
                if (i < 0 || j < 0) break;
                if (a[i][j] != a[i0][j0]) break;
                ++count;
            }
            if (count >= 5) return true;
            count = 1;
            i = i0;
            j = j0;

            // 西南-东北方向（左下到右上）
            while(true){ // 向东北搜索（右上）
                --i; ++j;  // 行减少，列增加
                if (i < 0 || j >= COLS) break;
                if (a[i][j] != a[i0][j0]) break;
                ++count;
            }
            i = i0; j = j0;  // 重置坐标
            while(true){ // 向西南搜索（左下）
                ++i; --j;  // 行增加，列减少
                if (i >= ROWS || j < 0) break;
                if (a[i][j] != a[i0][j0]) break;
                ++count;
            }

            if (count >= 5) return true;
        }
        return false;
    }
}
