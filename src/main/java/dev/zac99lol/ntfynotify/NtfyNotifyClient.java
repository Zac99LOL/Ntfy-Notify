package dev.zac99lol.ntfynotify;

import dev.zac99lol.ntfynotify.command.NtfyCommand;
import dev.zac99lol.ntfynotify.util.NtfyNotification;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class NtfyNotifyClient implements ClientModInitializer {
    public static final String MOD_ID = "ntfy_notify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean ACTIVE = false;
    public static int DEATH_NOTIFICATION_COOLDOWN = 0;
    public static int LEAVE_GAME_COOLDOWN = 100;
    private static float PREVIOUS_HEALTH;

    public static NtfyNotification LEFT_NOTIFICATION = new NtfyNotification("Automatically left the game due to taking too much damage.", "Left the game!");
    public static NtfyNotification FAILED_TO_LEAVE_NOTIFICATION = new NtfyNotification("Failed to leave the game because you had already left in the last 5 seconds.");

    public static NtfyNotification constructDeathNotification(MutableComponent literal) {
        return new NtfyNotification(
            NtfyNotifyConfig.INSTANCE.deathUseDeathMessage ? literal.getString() : NtfyNotifyConfig.INSTANCE.deathMessage,
            NtfyNotifyConfig.INSTANCE.deathTitle,
            NtfyNotifyConfig.INSTANCE.deathPriority
        );
    }

    public static NtfyNotification constructDamageNotification(float amount, float health) {
        return new NtfyNotification(
            NtfyNotifyConfig.INSTANCE.damageMessage.replace("${amount}", String.valueOf(amount)).replace("${health}", String.valueOf(health)),
            NtfyNotifyConfig.INSTANCE.damageTitle,
            NtfyNotifyConfig.INSTANCE.damagePriority
        );
    }

    @Override
    public void onInitializeClient() {
        NtfyCommand.init();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (!ACTIVE) return;
            if (client.player.isDeadOrDying()) return;

            float health = client.player.getHealth();

            if (health < PREVIOUS_HEALTH && PREVIOUS_HEALTH - health > NtfyNotifyConfig.INSTANCE.damageThreshold && health < NtfyNotifyConfig.INSTANCE.damageHealthThreshold && NtfyNotifyConfig.INSTANCE.damageEnabled) {
                constructDamageNotification(PREVIOUS_HEALTH - health, health).send();
                if (NtfyNotifyConfig.INSTANCE.damageLeaveGame && PREVIOUS_HEALTH - health > NtfyNotifyConfig.INSTANCE.damageLeaveGameDamageThreshold && health < NtfyNotifyConfig.INSTANCE.damageLeaveGameHealthThreshold) {
                    if (LEAVE_GAME_COOLDOWN == 0) {
                        LEAVE_GAME_COOLDOWN = 100;
                        client.disconnectFromWorld(Component.translatable("multiplayer.disconnect.ntfynotify.damaged"));
                        LEFT_NOTIFICATION.send();
                    } else {
                        FAILED_TO_LEAVE_NOTIFICATION.send();
                    }
                }
            }
            PREVIOUS_HEALTH = health;

            if (DEATH_NOTIFICATION_COOLDOWN > 0) DEATH_NOTIFICATION_COOLDOWN--;
            if (LEAVE_GAME_COOLDOWN > 0) LEAVE_GAME_COOLDOWN--;
        });
    }
}
