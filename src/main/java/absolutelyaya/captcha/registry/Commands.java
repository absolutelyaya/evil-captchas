package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.networking.OpenSpecificCaptchaPayload;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands
{
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("captcha").then(argument("target", player()).then(argument("type", string()).suggests(Commands::typeProvider).then(argument("difficulty", floatArg()).executes(Commands::executeOpenCaptcha)))));
	}
	
	private static CompletableFuture<Suggestions> typeProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("single-boxes").suggest("multi-boxes").suggest("puzzle-slide").suggest("wonky-text")
					   .suggest("simple-comprehension").suggest("advanced-comprehension").suggest("math").suggest("image-search")
					   .suggest("wimmelbild").suggest("rorschach").suggest("gambling").suggest("amongus").suggest("wizard")
					   .suggest("butterflies").buildFuture();
	}
	
	private static int executeOpenCaptcha(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("type", String.class);
		float difficulty = context.getArgument("difficulty", Float.class);
		
		ServerPlayNetworking.send(target, new OpenSpecificCaptchaPayload(type, "generic", difficulty));
		return Command.SINGLE_SUCCESS;
	}
}
