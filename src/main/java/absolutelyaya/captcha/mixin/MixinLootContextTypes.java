package absolutelyaya.captcha.mixin;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.registry.CaptchaLoot;
import com.google.common.collect.BiMap;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootContextTypes.class)
public class MixinLootContextTypes
{
	@Shadow @Final private static BiMap<Identifier, LootContextType> MAP;
	
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onClinit(CallbackInfo ci)
	{
		MAP.put(CAPTCHA.identifier("reward"), CaptchaLoot.CAPTCHA_LOOT_CONTEXT);
	}
}
