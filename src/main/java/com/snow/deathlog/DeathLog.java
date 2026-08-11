package com.snow.deathlog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DeathLog implements ModInitializer {

    public static final String MOD_ID = "deathlog";

    @Override
    public void onInitialize() {
        DeathLogConfig.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("deathlog")
                            .requires(source ->
                                    source.permissions().hasPermission(2))
                            .then(Commands.literal("reload")
                                    .executes(context -> {
                                        DeathLogConfig.load();

                                        context.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "[DeathLog] Configuration reloaded."
                                                ),
                                                true
                                        );

                                        return 1;
                                    }))
                            .then(Commands.literal("status")
                                    .executes(context -> {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        DeathLogConfig.status()
                                                ),
                                                false
                                        );

                                        return 1;
                                    }))
                            .then(Commands.literal("test")
                                    .executes(context -> {
                                        DeathLogWebhook.sendTest(
                                                context.getSource().getServer()
                                        );

                                        context.getSource().sendSuccess(
                                                () -> Component.literal(
                                                        "[DeathLog] Test webhook sent."
                                                ),
                                                true
                                        );

                                        return 1;
                                    }))
            );
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }

            DeathLogEvent.log(player);
        });
    }
}
