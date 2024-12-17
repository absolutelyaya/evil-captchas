package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record RequestAddonAdditionPayload(String type, float difficulty) implements CustomPayload
{
	public static final CustomPayload.Id<RequestAddonAdditionPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("addon"));
	public static final PacketCodec<RegistryByteBuf, RequestAddonAdditionPayload> CODEC = PacketCodec.tuple(
			PacketCodecs.STRING, RequestAddonAdditionPayload::type,
			PacketCodecs.FLOAT, RequestAddonAdditionPayload::difficulty,
			RequestAddonAdditionPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
