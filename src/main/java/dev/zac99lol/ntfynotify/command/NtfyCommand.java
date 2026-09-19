package dev.zac99lol.ntfynotify.command;

import dev.zac99lol.ntfynotify.NtfyNotifyClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.network.chat.Component;

public abstract class NtfyCommand {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("ntfynotify")
                .then(ClientCommands.literal("enable")
                    .executes(context -> {
                        if (NtfyNotifyClient.ACTIVE) {
                            context.getSource().sendError(Component.literal("Notifications are already enabled."));
                            return 0;
                        }
                        NtfyNotifyClient.ACTIVE = true;
                        context.getSource().sendFeedback(Component.literal("Enabled notifications."));
                        return 1;
                    }))
                .then(ClientCommands.literal("disable")
                    .executes(context -> {
                        if (!NtfyNotifyClient.ACTIVE) {
                            context.getSource().sendError(Component.literal("Notifications are already disabled."));
                            return 0;
                        }
                        NtfyNotifyClient.ACTIVE = false;
                        context.getSource().sendFeedback(Component.literal("Disabled notifications."));
                        return 1;
                    })));
        });
    }
}
