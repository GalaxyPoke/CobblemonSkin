package com.example.cobblemon_skin.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Bukkit 事件监听器：玩家加入后延迟 3 秒，通过反射调用 Fabric 模组发送皮肤列表数据包。
 * 延迟发送避免在加入阶段阻塞连接。
 */
public class PlayerJoinListener implements Listener {

    private final CobblemonSkinPlugin plugin;
    private Method sendSkinListMethod;
    private boolean methodResolved = false;

    public PlayerJoinListener(CobblemonSkinPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        // 延迟 60 tick (3秒)，等玩家完全加载后再发送皮肤数据
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!event.getPlayer().isOnline()) return;

            try {
                if (!methodResolved) {
                    methodResolved = true;
                    Class<?> handlerClass = Class.forName(
                        "com.example.cobblemon_skin.server.ServerPacketHandler"
                    );
                    sendSkinListMethod = handlerClass.getMethod(
                        "sendSkinListToPlayer", UUID.class
                    );
                    plugin.getLogger().info("已找到 Fabric 模组的发包方法");
                }

                if (sendSkinListMethod != null) {
                    sendSkinListMethod.invoke(null, playerUuid);
                    plugin.getLogger().info("已触发向 " + playerName + " 发送皮肤列表");
                }
            } catch (ClassNotFoundException e) {
                plugin.getLogger().warning("Fabric 模组未加载，无法发送皮肤列表: " + e.getMessage());
            } catch (Exception e) {
                plugin.getLogger().warning("向 " + playerName + " 发送皮肤列表失败: " + e.getMessage());
            }
        }, 60L); // 60 ticks = 3 seconds
    }
}
