package dev.zac99lol.ntfynotify.mixin;

import dev.zac99lol.ntfynotify.NtfyNotifyClient;
import dev.zac99lol.ntfynotify.NtfyNotifyConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerCombatKill", at = @At("HEAD"))
    private void ntfyNotify$sendDeathNotification(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        if (!NtfyNotifyClient.ACTIVE) return;
        if (!NtfyNotifyConfig.INSTANCE.deathEnabled) return;
        if (NtfyNotifyClient.DEATH_NOTIFICATION_COOLDOWN == 0) {
            NtfyNotifyClient.constructDeathNotification(packet.message().copy()).send();
            NtfyNotifyClient.DEATH_NOTIFICATION_COOLDOWN = 20;
        } else {
            NtfyNotifyClient.LOGGER.warn("Tried sending a death notification while on cooldown!");
        }
    }
}
