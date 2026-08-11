package com.snow.deathlog;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public final class DeathLogEvent {

    private DeathLogEvent() {
    }

    public static void log(ServerPlayer player) {
        try {
            String name = player.getName().getString();
            String uuid = player.getUUID().toString();

            String dimension = player.level()
                    .dimension()
                    .identifier()
                    .toString();

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            String cause = player.getCombatTracker()
                    .getDeathMessage()
                    .getString();

            int level = player.experienceLevel;
            int totalExperience = player.totalExperience;

            float health = player.getHealth();
            int food = player.getFoodData().getFoodLevel();
            float saturation = player.getFoodData().getSaturationLevel();

            String gameMode = player.gameMode
                    .getGameModeForPlayer()
                    .getName();

            String inventory = inventory(player);

            String message =
                    "[DeathLog] " + name
                            + " (" + uuid + ")"
                            + "\nDimension: " + dimension
                            + "\nLocation: "
                            + String.format("%.1f %.1f %.1f", x, y, z)
                            + "\nCause: " + cause
                            + "\nLevel: " + level
                            + " | XP: " + totalExperience
                            + "\nHealth: "
                            + String.format("%.1f", health)
                            + " | Food: " + food
                            + " | Saturation: "
                            + String.format("%.1f", saturation)
                            + "\nGameMode: " + gameMode
                            + "\n\nInventory:\n"
                            + inventory;

            var server = player.level().getServer();

            if (server == null) {
                return;
            }

            if (DeathLogConfig.current.chat.enabled) {
                server.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(message)
                );
            }

            if (DeathLogConfig.current.console.enabled) {
                System.out.println(message);
            }

            if (DeathLogConfig.current.webhook.enabled) {
                DeathLogWebhook.send(
                        server,
                        name,
                        uuid,
                        dimension,
                        x,
                        y,
                        z,
                        cause,
                        level,
                        totalExperience,
                        health,
                        food,
                        saturation,
                        gameMode,
                        inventory
                );
            }

        } catch (Exception e) {
            System.err.println(
                    "[DeathLog] Failed to process death: "
                            + e.getMessage()
            );
        }
    }

    private static String inventory(ServerPlayer player) {
        Inventory inv = player.getInventory();

        StringBuilder out = new StringBuilder();

        out.append("Hotbar:\n");

        for (int slot = 0; slot < 9; slot++) {
            appendItem(
                    out,
                    "Hotbar " + slot,
                    inv.getItem(slot)
            );
        }

        out.append("\nInventory:\n");

        for (int slot = 9; slot < 36; slot++) {
            appendItem(
                    out,
                    "Slot " + slot,
                    inv.getItem(slot)
            );
        }

        out.append("\nArmor:\n");

        appendItem(
                out,
                "Helmet",
                player.getItemBySlot(EquipmentSlot.HEAD)
        );

        appendItem(
                out,
                "Chestplate",
                player.getItemBySlot(EquipmentSlot.CHEST)
        );

        appendItem(
                out,
                "Leggings",
                player.getItemBySlot(EquipmentSlot.LEGS)
        );

        appendItem(
                out,
                "Boots",
                player.getItemBySlot(EquipmentSlot.FEET)
        );

        out.append("\nOffhand:\n");

        appendItem(
                out,
                "Offhand",
                player.getItemBySlot(EquipmentSlot.OFFHAND)
        );

        return out.toString();
    }

    private static void appendItem(
            StringBuilder out,
            String slot,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemId = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .toString();

        out.append("- ")
                .append(slot)
                .append(": ")
                .append(itemId)
                .append(" x")
                .append(stack.getCount());

        if (stack.isEnchanted()) {
            out.append(" [Enchants: ");

            boolean first = true;

            for (var entry : stack.getEnchantments().entrySet()) {
                var enchantmentHolder = entry.getKey();
                int level = entry.getIntValue();

                if (!first) {
                    out.append(", ");
                }

                first = false;

                String enchantmentId = enchantmentHolder.unwrapKey().map(key -> key.identifier().toString()).orElseGet(() -> enchantmentHolder.getRegisteredName());

                out.append(enchantmentId)
                        .append(" ")
                        .append(level);
            }

            out.append("]");
        }

        out.append("\n");
    }
}
