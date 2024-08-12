package absolutelyaya.captcha.mixin;

import absolutelyaya.captcha.networking.OpenRandomCaptchaPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack
{
	@Inject(method = "onCraftByPlayer", at = @At("HEAD"))
	void onCrafted(World world, PlayerEntity player, int amount, CallbackInfo ci)
	{
		if(player instanceof ServerPlayerEntity serverPlayer && world.random.nextFloat() < 0.001f)
			ServerPlayNetworking.send(serverPlayer, new OpenRandomCaptchaPayload("craft"));
	}
}
