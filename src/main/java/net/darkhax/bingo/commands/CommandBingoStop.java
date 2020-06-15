package net.darkhax.bingo.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.darkhax.bingo.ModdedBingo;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.darkhax.bookshelf.util.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandBingoStop {

	public static LiteralArgumentBuilder<CommandSource> register() {
		return CommandUtils.createCommand("stop", 2, ctx -> {
			if(!BingoAPI.GAME_STATE.isActive()) {
				throw new SimpleCommandExceptionType(new TranslationTextComponent("command.bingo.stop.nogame")).create();
			}
			BingoAPI.GAME_STATE.end();
			ServerPlayerEntity sender = ctx.getSource().asPlayer();
			sender.getServer().getPlayerList().sendMessage(new TranslationTextComponent("command.bingo.stop.stopped", sender.getDisplayName()));
			ModdedBingo.NETWORK.sendToAllPlayers(new PacketSyncGameState());
			return 1;
		});
	}
    
}
