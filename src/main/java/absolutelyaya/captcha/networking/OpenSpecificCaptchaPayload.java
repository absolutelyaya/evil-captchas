package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OpenSpecificCaptchaPayload(String type, String reason, float difficulty) implements CustomPayload
{
	public static final CustomPayload.Id<OpenSpecificCaptchaPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("specific_captcha"));
	public static final PacketCodec<RegistryByteBuf, OpenSpecificCaptchaPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, OpenSpecificCaptchaPayload::type, PacketCodecs.STRING, OpenSpecificCaptchaPayload::reason, PacketCodecs.FLOAT, OpenSpecificCaptchaPayload::difficulty, OpenSpecificCaptchaPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
