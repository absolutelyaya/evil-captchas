package absolutelyaya.captcha.registry;

import absolutelyaya.captcha.CAPTCHA;
import absolutelyaya.captcha.component.CaptchaComponents;
import absolutelyaya.captcha.component.IConfigComponent;
import absolutelyaya.captcha.component.IPlayerComponent;
import absolutelyaya.captcha.config.ServerConfig;
import absolutelyaya.captcha.data.InvoluntaryAddon;
import absolutelyaya.captcha.networking.openCaptcha;
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
									.then(literal("force-addon").then(argument("target", player()).then(argument("type", string()).suggests(Commands::addonProvider).then(argument("duration", floatArg()).executes(Commands::executeForceAddon)))))
									.then(literal("config").then(argument("rule", string()).suggests(Commands::ruleProvider).then(argument("value", string()).suggests(Commands::ruleValueProvider).executes(Commands::executeSetConfig)).executes(Commands::executeCheckConfig)).executes(Commands::executeOpenConfigScreen))
									.then(literal("difficulty")
												  .then(literal("reset")
																.then(literal("global").executes(Commands::executeResetGlobalDifficulty))
																.then(literal("player").then(argument("target", player()).executes(Commands::executeResetLocalDifficulty))))
												  .then(literal("set")
																.then(literal("global").then(argument("difficulty", floatArg()).executes(Commands::executeSetGlobalDifficulty)))
																.then(literal("player").then(argument("target", player()).then(argument("difficulty", floatArg()).executes(Commands::executeSetLocalDifficulty)))))
												  .then(literal("check")
																.then(literal("global").executes(Commands::executeCheckGlobalDifficulty))
																.then(literal("player").then(argument("target", player()).executes(Commands::executeCheckLocalDifficulty))))));
	}
	
	private static CompletableFuture<Suggestions> typeProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("single-boxes").suggest("multi-boxes").suggest("puzzle-slide").suggest("wonky-text")
					   .suggest("simple-comprehension").suggest("advanced-comprehension").suggest("math").suggest("image-search")
					   .suggest("wimmelbild").suggest("rorschach").suggest("gambling").suggest("amongus").suggest("wizard")
					   .suggest("butterflies").suggest("slimer").suggest("sponsor").buildFuture();
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
		
		CAPTCHA.openCaptcha(target, "generic", type, difficulty);
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
	
	private static int executeResetGlobalDifficulty(CommandContext<ServerCommandSource> context)
	{
		IConfigComponent global = CaptchaComponents.CONFIG.get(context.getSource().getWorld().getScoreboard());
		global.setCurDifficulty(5f);
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.reset.global"), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeResetLocalDifficulty(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
		IPlayerComponent component = CaptchaComponents.PLAYER.get(player);
		component.resetLocalDifficulty();
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.reset.local", player.getDisplayName()), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeSetGlobalDifficulty(CommandContext<ServerCommandSource> context)
	{
		float val = context.getArgument("difficulty", Float.class);
		IConfigComponent global = CaptchaComponents.CONFIG.get(context.getSource().getWorld().getScoreboard());
		global.setCurDifficulty(val);
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.set.global", val), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeSetLocalDifficulty(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
		float val = context.getArgument("difficulty", Float.class);
		IPlayerComponent component = CaptchaComponents.PLAYER.get(player);
		component.setLocalDifficulty(val);
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.set.local", player.getDisplayName(), val), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeCheckGlobalDifficulty(CommandContext<ServerCommandSource> context)
	{
		IConfigComponent global = CaptchaComponents.CONFIG.get(context.getSource().getWorld().getScoreboard());
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.check.global", global.getCurDifficulty()), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static int executeCheckLocalDifficulty(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
		IPlayerComponent component = CaptchaComponents.PLAYER.get(player);
		float val = component.getLocalDifficulty();
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.difficulty.check.local", player.getDisplayName(), val), true);
		return Command.SINGLE_SUCCESS;
	}
	
	private static CompletableFuture<Suggestions> addonProvider(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)
	{
		return builder.suggest("spinning-pig").suggest("winter-wonderland").suggest("custom-cursor").suggest("live-reaction")
					   .suggest("screen-saver").buildFuture();
	}
	
	private static int executeForceAddon(CommandContext<ServerCommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		String type = context.getArgument("type", String.class);
		float difficulty = context.getArgument("duration", Float.class);
		CaptchaComponents.PLAYER.get(target).addInvoluntaryAddon(new InvoluntaryAddon(type, System.currentTimeMillis() + (long)(difficulty * 1000),
				target.getRandom().nextFloat(), target.getRandom().nextFloat()));
		context.getSource().sendFeedback(() -> Text.translatable("captcha.command.force-addon", type, target.getDisplayName()), false);
		return Command.SINGLE_SUCCESS;
	}
}
