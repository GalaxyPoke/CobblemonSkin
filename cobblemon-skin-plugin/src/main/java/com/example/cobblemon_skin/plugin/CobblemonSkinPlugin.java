package com.example.cobblemon_skin.plugin;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * CobblemonSkin Bukkit 插件 — 提供 Bukkit 命令和权限管理。
 * 实际的皮肤逻辑和网络通信由 Fabric 模组（mods/ 里的 JAR）处理。
 * 本插件通过 Arclight 的 Brigadier 桥接调用模组注册的命令。
 */
public class CobblemonSkinPlugin extends JavaPlugin {

    private static CobblemonSkinPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        SkinCommandExecutor executor = new SkinCommandExecutor(this);
        var cmd = getCommand("skin");
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // 注册玩家加入监听器 — 延迟后触发 Fabric 模组发送皮肤数据包
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("CobblemonSkin 插件已加载！（Bukkit 命令 + 发包接口）");
    }

    @Override
    public void onDisable() {
        getLogger().info("CobblemonSkin 插件已卸载！");
    }

    public static CobblemonSkinPlugin getInstance() {
        return instance;
    }
}
