package com.snow.deathlog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DeathLogConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path FILE =
            Path.of("config", "deathlog.json");

    public static Config current = new Config();

    private DeathLogConfig() {
    }

    public static void load() {
        try {
            Files.createDirectories(FILE.getParent());

            if (!Files.exists(FILE)) {
                current = new Config();
                save();
                return;
            }

            String json = Files.readString(FILE);
            Config loaded = GSON.fromJson(json, Config.class);

            current = loaded != null ? loaded : new Config();

        } catch (Exception e) {
            System.err.println(
                    "[DeathLog] Failed to load config: "
                            + e.getMessage()
            );

            current = new Config();
        }
    }

    private static void save() throws IOException {
        Files.writeString(
                FILE,
                GSON.toJson(current)
        );
    }

    public static String status() {
        return "[DeathLog] "
                + "Chat=" + current.chat.enabled
                + " | Console=" + current.console.enabled
                + " | Webhook=" + current.webhook.enabled;
    }

    public static class Config {

        public Output chat = new Output(false);
        public Output console = new Output(false);
        public Webhook webhook = new Webhook();

        public Config() {
        }
    }

    public static class Output {

        public boolean enabled;

        public Output() {
        }

        public Output(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Webhook {

        public boolean enabled = false;
        public String url = "";
        public String username = "DeathLog";
        public String avatarUrl = "";
    }
}
