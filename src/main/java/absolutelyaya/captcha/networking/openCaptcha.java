package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record openCaptcha(String type, String reason, float difficulty) implements CustomPayload
{
	public static final CustomPayload.Id<openCaptcha> ID = new CustomPayload.Id<>(CAPTCHA.identifier("open"));
	public static final PacketCodec<RegistryByteBuf, openCaptcha> CODEC = PacketCodec.tuple(PacketCodecs.STRING, openCaptcha::type, PacketCodecs.STRING, openCaptcha::reason, PacketCodecs.FLOAT, openCaptcha::difficulty, openCaptcha::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
