package com.snow.deathlog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class DeathLogWebhook {

    private static final HttpClient CLIENT =
            HttpClient.newHttpClient();

    private DeathLogWebhook() {
    }

    public static void send(
            MinecraftServer server,
            String name,
            String uuid,
            String dimension,
            double x,
            double y,
            double z,
            String cause,
            int level,
            int totalExperience,
            float health,
            int food,
            float saturation,
            String gameMode,
            String inventory
    ) {
        DeathLogConfig.Webhook config =
                DeathLogConfig.current.webhook;

        if (!config.enabled || config.url.isBlank()) {
            return;
        }

        JsonObject root = new JsonObject();

        if (!config.username.isBlank()) {
            root.addProperty(
                    "username",
                    config.username
            );
        }

        if (!config.avatarUrl.isBlank()) {
            root.addProperty(
                    "avatar_url",
                    config.avatarUrl
            );
        }

        JsonArray embeds = new JsonArray();

        JsonObject embed = new JsonObject();

        embed.addProperty(
                "title",
                "☠️ Player Death"
        );

        embed.addProperty(
                "description",
                "**" + escape(name)
                        + "** died."
        );

        JsonArray fields = new JsonArray();

        addField(
                fields,
                "Player",
                name + "\n`" + uuid + "`",
                true
        );

        addField(
                fields,
                "Dimension",
                "`" + dimension + "`",
                true
        );

        addField(
                fields,
                "Location",
                String.format(
                        "`%.1f %.1f %.1f`",
                        x, y, z
                ),
                true
        );

        addField(
                fields,
                "Cause",
                cause,
                false
        );

        addField(
                fields,
                "Stats",
                "Level: `" + level + "`\n"
                        + "XP: `" + totalExperience + "`\n"
                        + "Health: `"
                        + String.format("%.1f", health)
                        + "`\n"
                        + "Food: `" + food + "`\n"
                        + "Saturation: `"
                        + String.format(
                                "%.1f",
                                saturation
                        )
                        + "`\n"
                        + "GameMode: `" + gameMode + "`",
                false
        );

        addField(
                fields,
                "Inventory",
                "```text\n"
                        + limitInventory(inventory)
                        + "\n```",
                false
        );

        embed.add("fields", fields);

        embeds.add(embed);
        root.add("embeds", embeds);

        sendJson(config.url, root.toString());
    }

    public static void sendTest(MinecraftServer server) {
        DeathLogConfig.Webhook config =
                DeathLogConfig.current.webhook;

        if (!config.enabled || config.url.isBlank()) {
            return;
        }

        JsonObject root = new JsonObject();

        root.addProperty(
                "username",
                config.username
        );

        JsonArray embeds = new JsonArray();

        JsonObject embed = new JsonObject();

        embed.addProperty(
                "title",
                "DeathLog Test"
        );

        embed.addProperty(
                "description",
                "Webhook connection is working."
        );

        embeds.add(embed);

        root.add("embeds", embeds);

        sendJson(config.url, root.toString());
    }

    private static void addField(
            JsonArray fields,
            String name,
            String value,
            boolean inline
    ) {
        JsonObject field = new JsonObject();

        field.addProperty("name", name);
        field.addProperty("value", value);
        field.addProperty("inline", inline);

        fields.add(field);
    }

    private static void sendJson(
            String url,
            String json
    ) {
        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(json)
                            )
                            .build();

            CLIENT.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            ).exceptionally(error -> {
                System.err.println(
                        "[DeathLog] Webhook failed: "
                                + error.getMessage()
                );

                return null;
            });

        } catch (Exception e) {
            System.err.println(
                    "[DeathLog] Invalid webhook URL: "
                            + e.getMessage()
            );
        }
    }

    private static String limitInventory(
            String inventory
    ) {
        if (inventory.length() <= 3900) {
            return inventory;
        }

        return inventory.substring(0, 3900)
                + "\n... inventory truncated";
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
    }
}
