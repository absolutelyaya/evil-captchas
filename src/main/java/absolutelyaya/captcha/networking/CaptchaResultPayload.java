package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CaptchaResultPayload(boolean result, String type, float difficulty) implements CustomPayload
{
	public static final CustomPayload.Id<CaptchaResultPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("captcha_result"));
	public static final PacketCodec<RegistryByteBuf, CaptchaResultPayload> CODEC =
			PacketCodec.tuple(PacketCodecs.BOOL, CaptchaResultPayload::result,
					PacketCodecs.STRING, CaptchaResultPayload::type,
					PacketCodecs.FLOAT, CaptchaResultPayload::difficulty, CaptchaResultPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
