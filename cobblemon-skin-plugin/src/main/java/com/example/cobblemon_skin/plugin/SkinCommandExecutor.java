package com.example.cobblemon_skin.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * /skin 命令执行器。
 * 通过 Bukkit 权限系统检查权限后，委托给 Fabric 模组的 /pokemonskin Brigadier 命令。
 * 在 Arclight 上，Fabric 模组注册的 Brigadier 命令会自动桥接为服务端命令。
 */
public class SkinCommandExecutor implements CommandExecutor, TabCompleter {

    private final CobblemonSkinPlugin plugin;

    public SkinCommandExecutor(CobblemonSkinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c此命令只能由玩家使用！");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list" -> handleList(player);
            case "set" -> handleSet(player, args);
            case "clear" -> handleClear(player, args);
            case "admin" -> handleAdmin(player, args);
            case "help" -> sendHelp(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleList(Player player) {
        if (!player.hasPermission("cobblemon.skin.list")) {
            player.sendMessage("§c你没有权限查看皮肤列表！");
            return;
        }
        // 委托给 Fabric 模组的 Brigadier 命令
        dispatchAsConsole("pokemonskin list " + player.getName());
    }

    private void handleSet(Player player, String[] args) {
        if (!player.hasPermission("cobblemon.skin.use")) {
            player.sendMessage("§c你没有权限使用皮肤！");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("§c用法: /skin set <皮肤ID> [槽位]");
            return;
        }
        String skinId = args[1];
        String slot = args.length > 2 ? args[2] : "1";
        // 以玩家身份执行命令
        player.performCommand("pokemonskin set " + skinId + " " + slot);
    }

    private void handleClear(Player player, String[] args) {
        if (!player.hasPermission("cobblemon.skin.use")) {
            player.sendMessage("§c你没有权限使用皮肤！");
            return;
        }
        String slot = args.length > 1 ? args[1] : "1";
        player.performCommand("pokemonskin clear " + slot);
    }

    private void handleAdmin(Player player, String[] args) {
        if (!player.hasPermission("cobblemon.skin.admin")) {
            player.sendMessage("§c你没有管理员权限！");
            return;
        }
        if (args.length < 3) {
            player.sendMessage("§c用法: /skin admin set <玩家> <皮肤ID> [槽位]");
            player.sendMessage("§c用法: /skin admin clear <玩家> [槽位]");
            return;
        }
        String adminSub = args[1].toLowerCase();
        switch (adminSub) {
            case "set" -> {
                if (args.length < 4) {
                    player.sendMessage("§c用法: /skin admin set <玩家> <皮肤ID> [槽位]");
                    return;
                }
                String target = args[2];
                String skinId = args[3];
                String slot = args.length > 4 ? args[4] : "1";
                dispatchAsConsole("pokemonskin admin set " + target + " " + skinId + " " + slot);
            }
            case "clear" -> {
                if (args.length < 3) {
                    player.sendMessage("§c用法: /skin admin clear <玩家> [槽位]");
                    return;
                }
                String target = args[2];
                String slot = args.length > 3 ? args[3] : "1";
                dispatchAsConsole("pokemonskin admin clear " + target + " " + slot);
            }
            default -> {
                player.sendMessage("§c未知子命令: " + adminSub);
                player.sendMessage("§c用法: /skin admin <set|clear> ...");
            }
        }
    }

    /**
     * 以控制台身份派发命令（用于管理员操作）
     */
    private void dispatchAsConsole(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6========== CobblemonSkin 帮助 ==========");
        player.sendMessage("§e/skin list §7- 查看可用皮肤列表");
        player.sendMessage("§e/skin set <皮肤ID> [槽位] §7- 应用皮肤到宝可梦");
        player.sendMessage("§e/skin clear [槽位] §7- 清除宝可梦皮肤");
        if (player.hasPermission("cobblemon.skin.admin")) {
            player.sendMessage("§e/skin admin set <玩家> <皮肤ID> [槽位] §7- 管理员设置皮肤");
            player.sendMessage("§e/skin admin clear <玩家> [槽位] §7- 管理员清除皮肤");
        }
        player.sendMessage("§6=======================================");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> subs = new java.util.ArrayList<>(Arrays.asList("list", "set", "clear", "help"));
            if (sender.hasPermission("cobblemon.skin.admin")) {
                subs.add("admin");
            }
            return filterStartsWith(subs, args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("admin")) {
                return filterStartsWith(Arrays.asList("set", "clear"), args[1]);
            }
            if (args[0].equalsIgnoreCase("set")) {
                // 皮肤 ID 补全 — 尝试从 Fabric 模组读取
                List<String> skinIds = getSkinIdsFromMod();
                if (!skinIds.isEmpty()) {
                    return filterStartsWith(skinIds, args[1]);
                }
            }
            if (args[0].equalsIgnoreCase("clear")) {
                return filterStartsWith(Arrays.asList("1", "2", "3", "4", "5", "6"), args[1]);
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                return filterStartsWith(Arrays.asList("1", "2", "3", "4", "5", "6"), args[2]);
            }
            if (args[0].equalsIgnoreCase("admin")) {
                // 玩家名补全
                return null; // null = Bukkit 自动补全在线玩家
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("set")) {
            List<String> skinIds = getSkinIdsFromMod();
            if (!skinIds.isEmpty()) {
                return filterStartsWith(skinIds, args[3]);
            }
        }
        return Collections.emptyList();
    }

    /**
     * 尝试通过反射从 Fabric 模组的 SkinManager 获取皮肤 ID 列表。
     * 在 Arclight 上，Fabric 模组的类可以通过共享类加载器访问。
     */
    private List<String> getSkinIdsFromMod() {
        try {
            Class<?> skinManagerClass = Class.forName("com.example.cobblemon_skin.server.SkinManager");
            Object skinInfoList = skinManagerClass.getMethod("getSkinInfoList").invoke(null);
            if (skinInfoList instanceof List<?> list) {
                return list.stream()
                        .map(info -> {
                            try {
                                return (String) info.getClass().getMethod("getSkinId").invoke(info);
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(java.util.Objects::nonNull)
                        .toList();
            }
        } catch (Exception ignored) {
            // Fabric 模组未加载或类不可访问
        }
        return Collections.emptyList();
    }

    private List<String> filterStartsWith(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        return options.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .toList();
    }
}
