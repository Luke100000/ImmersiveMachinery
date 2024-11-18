package immersive_machinery.mixin.client;

import com.mojang.authlib.GameProfile;
import immersive_machinery.entity.Copperfin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(method = "getWaterVision", at = @At("RETURN"), cancellable = true)
    private void immersive_machinery$getWaterVision(CallbackInfoReturnable<Float> cir) {
        if (this.getRootVehicle() instanceof Copperfin copperfin) {
            cir.setReturnValue(copperfin.modifyWaterVision(cir.getReturnValueF()));
        }
    }
}
