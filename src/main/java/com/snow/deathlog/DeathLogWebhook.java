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
            root.addProperty("username", config.username);
        }

        if (!config.avatarUrl.isBlank()) {
            root.addProperty("avatar_url", config.avatarUrl);
        }

        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();

        embed.addProperty(
                "title",
                "☠️ Player Death"
        );

        embed.addProperty(
                "description",
                "**" + escape(name) + "** died."
        );

        JsonArray fields = new JsonArray();

        addField(
                fields,
                "👤 Player",
                "**" + escape(name) + "**\n"
                        + "UUID: `" + uuid + "`",
                true
        );

        addField(
                fields,
                "💀 Death",
                "**Cause:** " + escape(cause),
                false
        );

        addField(
                fields,
                "🌍 Location",
                "**Dimension:** `" + dimension + "`\n"
                        + "**XYZ:** `"
                        + String.format(
                                "%.1f %.1f %.1f",
                                x, y, z
                        )
                        + "`",
                true
        );

        addField(
                fields,
                "📊 Stats",
                "Level: `" + level + "`\n"
                        + "XP: `" + totalExperience + "`\n"
                        + "Health: `" + String.format("%.1f", health) + "`\n"
                        + "Food: `" + food + "`\n"
                        + "Saturation: `" + String.format("%.1f", saturation) + "`\n"
                        + "Gamemode: `" + gameMode + "`",
                true
        );

        addInventoryField(
                fields,
                "⚔️ Main Hand",
                section(inventory, "Main Hand:", "Offhand:")
        );

        addInventoryField(
                fields,
                "🖐️ Offhand",
                section(inventory, "Offhand:", null)
        );

        addInventoryField(
                fields,
                "🎒 Hotbar",
                section(inventory, "Hotbar:", "Inventory:")
        );

        addInventoryField(
                fields,
                "📦 Inventory",
                section(inventory, "Inventory:", "Armor:")
        );

        addInventoryField(
                fields,
                "🛡️ Armor",
                section(inventory, "Armor:", "Main Hand:")
        );

        embed.add("fields", fields);

        embed.addProperty(
                "footer",
                "DeathLog • v1.2.0"
        );

        embed.addProperty(
                "timestamp",
                java.time.Instant.now().toString()
        );

        embeds.add(embed);
        root.add("embeds", embeds);

        sendJson(
                config.url,
                root.toString()
        );
    }

    public static void sendTest(MinecraftServer server) {
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

        JsonArray embeds = new JsonArray();
        JsonObject embed = new JsonObject();

        embed.addProperty(
                "title",
                "✅ DeathLog Test"
        );

        embed.addProperty(
                "description",
                "Webhook connection is working."
        );

        embed.addProperty(
                "footer",
                "DeathLog • v1.2.0"
        );

        embeds.add(embed);
        root.add("embeds", embeds);

        sendJson(
                config.url,
                root.toString()
        );
    }

    private static void addField(
            JsonArray fields,
            String name,
            String value,
            boolean inline
    ) {
        JsonObject field = new JsonObject();

        field.addProperty("name", name);
        field.addProperty(
                "value",
                value.isBlank() ? "Empty" : value
        );
        field.addProperty("inline", inline);

        fields.add(field);
    }

    private static void addInventoryField(
            JsonArray fields,
            String name,
            String value
    ) {
        if (value == null || value.isBlank()) {
            value = "Empty";
        }

        if (value.length() > 1024) {
            value = value.substring(0, 1010)
                    + "\n... truncated";
        }

        addField(
                fields,
                name,
                "```text\n" + value + "\n```",
                false
        );
    }

    private static String section(
            String inventory,
            String startMarker,
            String endMarker
    ) {
        if (inventory == null || inventory.isBlank()) {
            return "";
        }

        int start = inventory.indexOf(startMarker);

        if (start < 0) {
            return "";
        }

        start += startMarker.length();

        int end = endMarker == null
                ? inventory.length()
                : inventory.indexOf(endMarker, start);

        if (end < 0) {
            end = inventory.length();
        }

        return inventory
                .substring(start, end)
                .trim();
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
                                    HttpRequest.BodyPublishers.ofString(json)
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

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("`", "\\`");
    }
}
