package dev.zac99lol.ntfynotify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.zac99lol.ntfynotify.util.NtfyNotification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class NtfyNotifyConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final NtfyNotifyConfig DEFAULT = new NtfyNotifyConfig();
    public static final Path CONFIG_FILE = Path.of("config").resolve("ntfy_notify_config.json");
    public static NtfyNotifyConfig INSTANCE = loadConfigFile(CONFIG_FILE.toFile());

    // config values
        // general category
            // Make validated, good regex is ^https?:\/\/(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)*[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z]{2,})?\/$
            public String server = "https://ntfy.sh/";

            // Make validated, letters, numbers, underscores, dashes, up to 64 characters
            public String topic = "SET_A_TOPIC";

        // death event category
            public boolean deathEnabled = true;
            public String deathTitle = "";
            public String deathMessage = "You just lost the game.";
            public boolean deathUseDeathMessage = true;
            public NtfyNotification.Priority deathPriority = NtfyNotification.Priority.MAX;

        // damage event category
            public boolean damageEnabled = true;
            // trigger conditions group
                public float damageThreshold = 0.0F;
                public float damageHealthThreshold = 20.0F;
            // action group
                public boolean damageLeaveGame = false;
                public float damageLeaveGameDamageThreshold = 0.0F;
                public float damageLeaveGameHealthThreshold = 8.0F;
            public String damageTitle = "";
            public String damageMessage = "You took ${amount} damage!";
            public NtfyNotification.Priority damagePriority = NtfyNotification.Priority.HIGH;
    // end config values

    public static Screen createScreen(Screen parent) {
        Option<String> deathMessageOption = Option.<String>createBuilder()
            .name(translate("death.message"))
            .description(description("death.message.description"))
            .binding(DEFAULT.deathMessage, () -> INSTANCE.deathMessage, value -> INSTANCE.deathMessage = value)
            .controller(StringControllerBuilder::create)
            .available(!INSTANCE.deathUseDeathMessage)
            .build();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
            .title(translate("title"))
            .category(ConfigCategory.createBuilder()
                .name(translate("category.general"))
                .tooltip(translate("category.general.tooltip"))
                .option(Option.<String>createBuilder()
                    .name(translate("general.server"))
                    .description(description("general.server.description"))
                    .binding(DEFAULT.server, () -> INSTANCE.server, value -> INSTANCE.server = value)
                    .controller(StringControllerBuilder::create)
                    .build())
                .option(Option.<String>createBuilder()
                    .name(translate("general.topic"))
                    .description(description("general.topic.description"))
                    .binding(DEFAULT.topic, () -> INSTANCE.topic, value -> INSTANCE.topic = value)
                    .controller(StringControllerBuilder::create)
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(translate("category.death"))
                .tooltip(translate("category.death.tooltip"))
                .option(Option.<Boolean>createBuilder()
                    .name(translate("death.enabled"))
                    .description(description("death.enabled.description"))
                    .binding(DEFAULT.deathEnabled, () -> INSTANCE.deathEnabled, value -> INSTANCE.deathEnabled = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .option(Option.<String>createBuilder()
                    .name(translate("death.title"))
                    .description(description("death.title.description"))
                    .binding(DEFAULT.deathTitle, () -> INSTANCE.deathTitle, value -> INSTANCE.deathTitle = value)
                    .controller(StringControllerBuilder::create)
                    .build())
                .option(deathMessageOption)
                .option(Option.<Boolean>createBuilder()
                    .name(translate("death.use_death_message"))
                    .description(description("death.use_death_message.description"))
                    .binding(DEFAULT.deathUseDeathMessage, () -> INSTANCE.deathUseDeathMessage, value -> INSTANCE.deathUseDeathMessage = value)
                    .controller(TickBoxControllerBuilder::create)
                    .addListener((opt, event) -> {
                        opt.applyValue();
                        deathMessageOption.setAvailable(!INSTANCE.deathUseDeathMessage);
                    })
                    .build())
                .option(Option.<NtfyNotification.Priority>createBuilder()
                    .name(translate("death.priority"))
                    .description(description("death.priority.description"))
                    .binding(DEFAULT.deathPriority, () -> INSTANCE.deathPriority, value -> INSTANCE.deathPriority = value)
                    .controller(opt -> EnumControllerBuilder.create(opt).enumClass(NtfyNotification.Priority.class))
                    .build())
                .option(ButtonOption.createBuilder()
                    .name(translate("death.test"))
                    .description(description("death.test.description"))
                    .action((screen, option) -> NtfyNotifyClient.constructDeathNotification(Component.literal("Example death message here")).send())
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(translate("category.damage"))
                .tooltip(translate("category.damage.tooltip"))
                .option(Option.<Boolean>createBuilder()
                    .name(translate("damage.enabled"))
                    .description(description("damage.enabled.description"))
                    .binding(DEFAULT.damageEnabled, () -> INSTANCE.damageEnabled, value -> INSTANCE.damageEnabled = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(translate("damage.group.trigger"))
                    .description(description("damage.group.trigger.description"))
                    .option(Option.<Float>createBuilder()
                        .name(translate("damage.trigger.damage_threshold"))
                        .description(description("damage.trigger.damage_threshold.description"))
                        .binding(DEFAULT.damageThreshold, () -> INSTANCE.damageThreshold, value -> INSTANCE.damageThreshold = value)
                        .controller(FloatFieldControllerBuilder::create)
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(translate("damage.trigger.health_threshold"))
                        .description(description("damage.trigger.health_threshold.description"))
                        .binding(DEFAULT.damageHealthThreshold, () -> INSTANCE.damageHealthThreshold, value -> INSTANCE.damageHealthThreshold = value)
                        .controller(FloatFieldControllerBuilder::create)
                        .build())
                    .build())
                .group(OptionGroup.createBuilder()
                    .name(translate("damage.group.action"))
                    .description(description("damage.group.action.description"))
                    .option(Option.<Boolean>createBuilder()
                        .name(translate("damage.action.leave_game"))
                        .description(description("damage.action.leave_game.description"))
                        .binding(DEFAULT.damageLeaveGame, () -> INSTANCE.damageLeaveGame, value -> INSTANCE.damageLeaveGame = value)
                        .controller(TickBoxControllerBuilder::create)
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(translate("damage.action.leave_game_damage_threshold"))
                        .description(description("damage.action.leave_game_damage_threshold.description"))
                        .binding(DEFAULT.damageLeaveGameDamageThreshold, () -> INSTANCE.damageLeaveGameDamageThreshold, value -> INSTANCE.damageLeaveGameDamageThreshold = value)
                        .controller(FloatFieldControllerBuilder::create)
                        .build())
                    .option(Option.<Float>createBuilder()
                        .name(translate("damage.action.leave_game_health_threshold"))
                        .description(description("damage.action.leave_game_health_threshold.description"))
                        .binding(DEFAULT.damageLeaveGameHealthThreshold, () -> INSTANCE.damageLeaveGameHealthThreshold, value -> INSTANCE.damageLeaveGameHealthThreshold = value)
                        .controller(FloatFieldControllerBuilder::create)
                        .build())
                    .build())
                .option(Option.<String>createBuilder()
                    .name(translate("damage.title"))
                    .description(description("damage.title.description"))
                    .binding(DEFAULT.damageTitle, () -> INSTANCE.damageTitle, value -> INSTANCE.damageTitle = value)
                    .controller(StringControllerBuilder::create)
                    .build())
                .option(Option.<String>createBuilder()
                    .name(translate("damage.message"))
                    .description(description("damage.message.description"))
                    .binding(DEFAULT.damageMessage, () -> INSTANCE.damageMessage, value -> INSTANCE.damageMessage = value)
                    .controller(StringControllerBuilder::create)
                    .build())
                .option(Option.<NtfyNotification.Priority>createBuilder()
                    .name(translate("damage.priority"))
                    .description(description("damage.priority.description"))
                    .binding(DEFAULT.damagePriority, () -> INSTANCE.damagePriority, value -> INSTANCE.damagePriority = value)
                    .controller(opt -> EnumControllerBuilder.create(opt).enumClass(NtfyNotification.Priority.class))
                    .build())
                .option(ButtonOption.createBuilder()
                    .name(translate("damage.test"))
                    .description(description("damage.test.description"))
                    .action((screen, option) -> {})
                    .available(false)
                    .build())
                .build())
            .save(() -> INSTANCE.save());

        return builder.build().generateScreen(parent);
    }

    private static MutableComponent translate(String key) {
        return Component.translatable("config.ntfynotify." + key);
    }

    private static OptionDescription description(String key) {
        return OptionDescription.of(translate(key));
    }

    public void save() {
        saveConfigFile(CONFIG_FILE.toFile());
    }

    private static NtfyNotifyConfig loadConfigFile(File file) {
        NtfyNotifyConfig config = null;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = GSON.fromJson(reader, NtfyNotifyConfig.class);
            } catch (IOException e) {
                throw new RuntimeException("Problem occured while trying to load config: ", e);
            }
        }

        if (config == null) {
            config = DEFAULT;
        }

        return config;
    }

    private void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Problem occured while trying to save config: ", e);
        }
    }
}
