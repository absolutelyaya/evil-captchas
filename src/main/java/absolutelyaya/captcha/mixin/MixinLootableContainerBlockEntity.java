package absolutelyaya.captcha.mixin;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootableContainerBlockEntity.class)
public abstract class MixinLootableContainerBlockEntity implements LootableInventory
{
	@Shadow @Nullable protected RegistryKey<LootTable> lootTable;
	
	@Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
	void onCreateMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity, CallbackInfoReturnable<ScreenHandler> cir)
	{
		if(!playerEntity.isSpectator() && lootTable != null && playerEntity instanceof ServerPlayerEntity player)
		{
			CAPTCHA.openRandomCaptcha(player, "chest");
			generateLoot(player);
			cir.setReturnValue(null);
		}
	}
}
