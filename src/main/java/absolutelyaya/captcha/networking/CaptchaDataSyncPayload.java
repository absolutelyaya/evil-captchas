package absolutelyaya.captcha.networking;

import absolutelyaya.captcha.CAPTCHA;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record CaptchaDataSyncPayload(NbtCompound data) implements CustomPayload
{
	public static final CustomPayload.Id<CaptchaDataSyncPayload> ID = new CustomPayload.Id<>(CAPTCHA.identifier("data"));
	public static final PacketCodec<RegistryByteBuf, CaptchaDataSyncPayload> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, CaptchaDataSyncPayload::data, CaptchaDataSyncPayload::new);
	
	@Override
	public Id<? extends CustomPayload> getId()
	{
		return ID;
	}
}
