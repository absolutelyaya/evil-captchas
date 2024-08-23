package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CaptchaResultPayload(boolean result) implements CustomPayload
{
	public static final CustomPayload.Id<CaptchaResultPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("captcha_result"));
	public static final PacketCodec<RegistryByteBuf, CaptchaResultPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOL, CaptchaResultPayload::result, CaptchaResultPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
