package com.snow.deathlog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class DeathLog implements ModInitializer {

    public static final String MOD_ID = "deathlog";

    @Override
    public void onInitialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }

            var server = player.getServer();
            if (server == null) {
                return;
            }

            String name = player.getName().getString();
            String uuid = player.getUuidAsString();
            String dimension = player.getWorld()
                    .getRegistryKey()
                    .getValue()
                    .toString();

            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            String cause = damageSource.getName();

            server.sendMessage(
                    Text.literal(
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
