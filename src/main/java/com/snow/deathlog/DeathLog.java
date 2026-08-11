package com.snow.deathlog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DeathLog implements ModInitializer {

    public static final String MOD_ID = "deathlog";

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            var server = player.level().getServer();
            if (server == null) {
                return;
            }

            String name = player.getName().getString();
            String uuid = player.getUUID().toString();

            String dimension = player.level()
                    .dimension().identifier()
                    .toString();

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            String cause = player.getCombatTracker()
                    .getDeathMessage()
                    .getString();

            server.sendSystemMessage(
                    Component.literal(
                            "[DeathLog] " + name
                                    + " died at "
                                    + dimension
                                    + " "
                                    + String.format("%.1f %.1f %.1f", x, y, z)
                                    + " | Cause: " + cause
                    )
            );

            System.out.println(
                    "[DeathLog] Player=" + name
                            + " UUID=" + uuid
                            + " Dimension=" + dimension
                            + " X=" + x
                            + " Y=" + y
                            + " Z=" + z
                            + " Cause=" + cause
            );
        });
    }
}
