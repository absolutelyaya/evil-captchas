package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenCaptcha(String type, String reason, float difficulty) implements CustomPayload
{
	public static final CustomPayload.Id<OpenCaptcha> ID = new CustomPayload.Id<>(CAPTCHA.identifier("open"));
	public static final PacketCodec<RegistryByteBuf, OpenCaptcha> CODEC = PacketCodec.tuple(PacketCodecs.STRING, OpenCaptcha::type, PacketCodecs.STRING, OpenCaptcha::reason, PacketCodecs.FLOAT, OpenCaptcha::difficulty, OpenCaptcha::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
