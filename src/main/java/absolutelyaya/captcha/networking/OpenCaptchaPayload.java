package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenCaptchaPayload(String reason) implements CustomPayload
{
	public static final CustomPayload.Id<OpenCaptchaPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("open_captcha"));
	public static final PacketCodec<RegistryByteBuf, OpenCaptchaPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, OpenCaptchaPayload::reason, OpenCaptchaPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
