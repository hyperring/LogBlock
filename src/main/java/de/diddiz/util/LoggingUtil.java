package de.diddiz.util;

import static de.diddiz.LogBlock.config.Config.getWorldConfig;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Button;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Ladder;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.material.Torch;
import org.bukkit.material.TrapDoor;
import org.bukkit.material.TripwireHook;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.Logging;
import de.diddiz.LogBlock.config.WorldConfig;

public class LoggingUtil {

	public static void smartLogFallables(Consumer consumer, String playerName, Block origin) {

		WorldConfig wcfg = getWorldConfig(origin.getWorld());
		if (wcfg == null) return;

		//Handle falling blocks
		Block checkBlock = origin.getRelative(BlockFace.UP);
		int up = 0;
		final int highestBlock = checkBlock.getWorld().getHighestBlockYAt(checkBlock.getLocation());
		while (BukkitUtils.getRelativeTopFallables().contains(checkBlock.getTypeId())) {

			// Record this block as falling
			consumer.queueBlockBreak(playerName, checkBlock.getState());

			// Guess where the block is going (This could be thrown of by explosions, but it is better than nothing)
			Location loc = origin.getLocation();
			int x = loc.getBlockX();
			int y = loc.getBlockY();
			int z = loc.getBlockZ();
			while (y > 0 && BukkitUtils.canFall(loc.getWorld(), x, (y - 1), z)) {
				y--;
			}
			// If y is 0 then the sand block fell out of the world :(
			if (y != 0) {
				Location finalLoc = new Location(loc.getWorld(), x, y, z);
				// Run this check to avoid false positives
				if (!BukkitUtils.getFallingEntityKillers().contains(finalLoc.getBlock().getTypeId())) {
					finalLoc.add(0, up, 0); // Add this here after checking for block breakers
					if (finalLoc.getBlock().getType() == Material.AIR || BukkitUtils.getRelativeTopFallables().contains(finalLoc.getBlock().getTypeId())) {
						consumer.queueBlockPlace(playerName, finalLoc, checkBlock.getTypeId(), checkBlock.getData());
					} else {
						consumer.queueBlockReplace(playerName, finalLoc, finalLoc.getBlock().getTypeId(), finalLoc.getBlock().getData(), checkBlock.getTypeId(), checkBlock.getData());
					}
					up++;
				}
			}
			if (checkBlock.getY() >= highestBlock) break;
			checkBlock = checkBlock.getRelative(BlockFace.UP);
		}
	}

	public static void smartLogBlockBreak(Consumer consumer, String playerName, Block origin) {

		WorldConfig wcfg = getWorldConfig(origin.getWorld());
		if (wcfg == null) return;

		Block checkBlock = origin.getRelative(BlockFace.UP);
		if (BukkitUtils.getRelativeTopBreakabls().contains(checkBlock.getTypeId())) {
			if (wcfg.isLogging(Logging.SIGNTEXT) && checkBlock.getType() == Material.SIGN_POST) {
				consumer.queueSignBreak(playerName, (Sign) checkBlock.getState());
			} else {
				consumer.queueBlockBreak(playerName, checkBlock.getState());
			}
		}

		List<Location> relativeBreakables = BukkitUtils.getBlocksNearby(origin, BukkitUtils.getRelativeBreakables());
		if (relativeBreakables.size() != 0) {
			for (Location location : relativeBreakables) {
				final Material blockType = location.getBlock().getType();
				final BlockState blockState = location.getBlock().getState();
				final MaterialData data = blockState.getData();
				switch (blockType) {
					case REDSTONE_TORCH_ON:
					case REDSTONE_TORCH_OFF:
						if (blockState.getBlock().getRelative(((RedstoneTorch) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case TORCH:
						if (blockState.getBlock().getRelative(((Torch) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case COCOA:
						if (blockState.getBlock().getRelative(((CocoaPlant) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case LADDER:
						if (blockState.getBlock().getRelative(((Ladder) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case LEVER:
						if (blockState.getBlock().getRelative(((Lever) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case TRIPWIRE_HOOK:
						if (blockState.getBlock().getRelative(((TripwireHook) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case WOOD_BUTTON:
					case STONE_BUTTON:
						if (blockState.getBlock().getRelative(((Button) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					case WALL_SIGN:
						if (blockState.getBlock().getRelative(((org.bukkit.material.Sign) data).getAttachedFace()).equals(origin)) {
							if (wcfg.isLogging(Logging.SIGNTEXT)) {
								consumer.queueSignBreak(playerName, (Sign) blockState);
							} else {
								consumer.queueBlockBreak(playerName, blockState);
							}
						}
						break;
					case TRAP_DOOR:
						if (blockState.getBlock().getRelative(((TrapDoor) data).getAttachedFace()).equals(origin)) {
							consumer.queueBlockBreak(playerName, blockState);
						}
						break;
					default:
						consumer.queueBlockBreak(playerName, blockState);
						break;
				}
			}
		}
		// Do this down here so that the block is added after blocks sitting on it
		consumer.queueBlockBreak(playerName, origin.getState());
	}
}
