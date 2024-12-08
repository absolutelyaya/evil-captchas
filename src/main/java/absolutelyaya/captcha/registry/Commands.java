package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.config.ServerConfig;
import absolutelyaya.captcha.networking.OpenCaptcha;
import absolutelyaya.yayconfig.networking.OpenConfigScreenPayload;
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
import net.minecraft.text.Text;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.FloatArgumentType.floatArg;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands
{
	static final List<String>
			boolConfigs = List.of("lethal", "explosive", "validation-expiration", "not-easy"),
			intConfigs = List.of("lives", "expiration-delay-min", "expiration-delay-range"),
			floatConfigs = List.of("constant-increase-rate");
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
	{
		dispatcher.register(literal("captcha").requires(source -> source.hasPermissionLevel(2))
									.then(literal("force").then(argument("target", player()).then(argument("type", string()).suggests(Commands::typeProvider).then(argument("difficulty", floatArg()).executes(Commands::executeOpenCaptcha)))))
									.then(literal("config").then(argument("rule", string()).suggests(Commands::ruleProvider).then(argument("value", string()).suggests(Commands::ruleValueProvider).executes(Commands::executeSetConfig)).executes(Commands::executeCheckConfig)).executes(Commands::executeOpenConfigScreen)));
	}
	
	private static CompletableFuture<Suggestions> typeProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("single-boxes").suggest("multi-boxes").suggest("puzzle-slide").suggest("wonky-text")
					   .suggest("simple-comprehension").suggest("advanced-comprehension").suggest("math").suggest("image-search")
					   .suggest("wimmelbild").suggest("rorschach").suggest("gambling").suggest("amongus").suggest("wizard")
					   .suggest("butterflies").buildFuture();
	}
	
	private static CompletableFuture<Suggestions> ruleProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		for (String s : boolConfigs)
			builder.suggest(s);
		for (String s : intConfigs)
			builder.suggest(s);
		for (String s : floatConfigs)
			builder.suggest(s);
		return builder.buildFuture();
	}
	
	private static CompletableFuture<Suggestions> ruleValueProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		String rule = context.getArgument("rule", String.class);
		if(boolConfigs.contains(rule))
			builder.suggest("true").suggest("false");
		else if(NumberUtils.isCreatable(builder.getInput()))
			builder.suggest(builder.getInput());
		else
			builder.suggest(0);
		return builder.buildFuture();
	}
	
	private static int executeOpenCaptcha(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("type", String.class);
		float difficulty = context.getArgument("difficulty", Float.class);
		
		ServerPlayNetworking.send(target, new OpenCaptcha(type, "generic", difficulty));
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.force", type, target.getDisplayName()), false);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeSetConfig(CommandContext<ServerCommandSource> context)
	{
		String rule = context.getArgument("rule", String.class);
		String val = context.getArgument("value", String.class);
		
		ServerConfig config = CAPTCHA.config;
		switch(rule)
		{
			case "lethal" -> config.lethal.setValue(Boolean.parseBoolean(val));
			case "explosive" -> config.explosive.setValue(Boolean.parseBoolean(val));
			case "validation-expiration" -> config.validationExpiration.setValue(Boolean.parseBoolean(val));
			case "not-easy" -> config.notEasy.setValue(Boolean.parseBoolean(val));
			
			case "lives" -> config.lives.setValue(Integer.parseInt(val));
			case "constant-increase-interval" -> config.constantIncreaseInterval.setValue(Integer.parseInt(val));
			case "expiration-delay-min" -> config.expirationDelayMin.setValue(Integer.parseInt(val));
			case "expiration-delay-range" -> config.expirationDelayMax.setValue(Integer.parseInt(val));
			
			case "constant-increase-rate" -> config.constantIncreaseRate.setValue(Float.parseFloat(val));
		}
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.config.set", rule, val), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeCheckConfig(CommandContext<ServerCommandSource> context)
	{
		String rule = context.getArgument("rule", String.class);
		
		ServerConfig config = CAPTCHA.config;
		Object val = switch(rule)
		{
			case "lethal" -> config.lethal.getValue();
			case "explosive" -> config.explosive.getValue();
			case "validation-expiration" -> config.validationExpiration.getValue();
			case "not-easy" -> config.notEasy.getValue();
			
			case "lives" -> config.lives.getValue();
			case "constant-increase-interval" -> config.constantIncreaseInterval.getValue();
			case "expiration-delay-min" -> config.expirationDelayMin.getValue();
			case "expiration-delay-range" -> config.expirationDelayMax.getValue();
			
			case "constant-increase-rate" -> config.constantIncreaseRate.getValue();
			
			default -> Text.translatable("captcha.command.config.check.nothing").toString();
		};
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.config.check", rule, String.valueOf(val)), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeOpenConfigScreen(CommandContext<ServerCommandSource> context)
	{
		ServerPlayerEntity player = context.getSource().getPlayer();
		if(player == null)
			return 0;
		ServerPlayNetworking.send(player, new OpenConfigScreenPayload(CAPTCHA.config.getId()));
		return Command.SINGLE_SUCCESS;
	}
}
