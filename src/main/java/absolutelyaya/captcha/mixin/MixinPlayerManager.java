package absolutelyaya.captcha.mixin;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public class MixinPlayerManager
{
	@Inject(method = "respawnPlayer", at = @At("TAIL"))
	void afterRespawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir)
	{
		CAPTCHA.openRandomCaptcha(player, "generic");
	}
}
