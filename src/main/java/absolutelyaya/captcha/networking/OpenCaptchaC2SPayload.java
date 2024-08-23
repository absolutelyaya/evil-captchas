package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record OpenCaptchaC2SPayload() implements CustomPayload
{
	public static final CustomPayload.Id<OpenCaptchaC2SPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("captcha_c2s"));
	public static final PacketCodec<RegistryByteBuf, OpenCaptchaC2SPayload> CODEC = PacketCodec.unit(new OpenCaptchaC2SPayload());
	
	@Override
	public CustomPayload.Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
