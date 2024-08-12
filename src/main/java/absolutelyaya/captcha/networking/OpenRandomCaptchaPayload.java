package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenRandomCaptchaPayload(String reason) implements CustomPayload
{
	public static final CustomPayload.Id<OpenRandomCaptchaPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("random_captcha"));
	public static final PacketCodec<RegistryByteBuf, OpenRandomCaptchaPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, OpenRandomCaptchaPayload::reason, OpenRandomCaptchaPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
