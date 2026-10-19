package com.simibubi.create.content.redstone.link.controller;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.utility.AdventureUtil;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.createmod.catnip.data.Couple;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1271;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_3715;
import net.minecraft.class_3908;
import io.github.fabricators_of_create.porting_lib.item.UseFirstBehaviorItem;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;

public class LinkedControllerItem extends class_1792 implements class_3908, UseFirstBehaviorItem {

	public LinkedControllerItem(class_1793 properties) {
		super(properties);
	}

	@Override
	public class_1269 onItemUseFirst(class_1799 stack, class_1838 ctx) {
		class_1657 player = ctx.method_8036();
		if (player == null)
			return class_1269.field_5811;
		class_1937 world = ctx.method_8045();
		class_2338 pos = ctx.method_8037();
		class_2680 hitState = world.method_8320(pos);

		if (player.method_7294()) {
			if (player.method_5715()) {
				if (AllBlocks.LECTERN_CONTROLLER.has(hitState)) {
					if (!world.field_9236)
						AllBlocks.LECTERN_CONTROLLER.get().withBlockEntityDo(world, pos, be ->
							be.swapControllers(stack, player, ctx.method_20287(), hitState));
					return class_1269.field_5812;
				}
			} else {
				if (AllBlocks.REDSTONE_LINK.has(hitState)) {
					if (world.field_9236)
						EnvExecutor.runWhenOn(EnvType.CLIENT, () -> () -> this.toggleBindMode(ctx.method_8037()));
					player.method_7357()
						.method_7906(this, 2);
					return class_1269.field_5812;
				}

				if (hitState.method_27852(class_2246.field_16330) && !hitState.method_11654(class_3715.field_17366)) {
					if (!world.field_9236) {
						class_1799 lecternStack = player.method_7337() ? stack.method_7972() : stack.method_7971(1);
						AllBlocks.LECTERN_CONTROLLER.get().replaceLectern(hitState, world, pos, lecternStack);
					}
					return class_1269.field_5812;
				}

				if (AllBlocks.LECTERN_CONTROLLER.has(hitState))
					return class_1269.field_5811;
			}
		}

		return method_7836(world, player, ctx.method_20287()).method_5467();
	}

	@Override
	public class_1271<class_1799> method_7836(class_1937 world, class_1657 player, class_1268 hand) {
		class_1799 heldItem = player.method_5998(hand);

		if (player.method_5715() && hand == class_1268.field_5808) {
			if (!world.field_9236 && player instanceof class_3222 && player.method_7294())
				NetworkHooks.openScreen((class_3222) player, this, buf -> {
					buf.method_10793(heldItem);
				});
			return class_1271.method_22427(heldItem);
		}

		if (!player.method_5715()) {
			if (world.field_9236)
				EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::toggleActive);
			player.method_7357()
				.method_7906(this, 2);
		}

		return class_1271.method_22430(heldItem);
	}

	@Environment(EnvType.CLIENT)
	private void toggleBindMode(class_2338 pos) {
		LinkedControllerClientHandler.toggleBindMode(pos);
	}

	@Environment(EnvType.CLIENT)
	private void toggleActive() {
		LinkedControllerClientHandler.toggle();
	}

	public static ItemStackHandler getFrequencyItems(class_1799 stack) {
		if (AllItems.LINKED_CONTROLLER.get() != stack.method_7909())
			throw new IllegalArgumentException("Cannot get frequency items from non-controller: " + stack);
		return getFrequencyItems(stack.method_7911("Items"));
	}

	public static ItemStackHandler getFrequencyItems(class_2487 invNBT) {
		ItemStackHandler newInv = new ItemStackHandler(12);
		if (!invNBT.method_33133())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static Couple<Frequency> toFrequency(class_1799 controller, int slot) {
		ItemStackHandler frequencyItems = getFrequencyItems(controller);
		return Couple.create(Frequency.of(frequencyItems.getStackInSlot(slot * 2)),
			Frequency.of(frequencyItems.getStackInSlot(slot * 2 + 1)));
	}

	@Override
	public class_1703 createMenu(int id, class_1661 inv, class_1657 player) {
		if (AdventureUtil.isAdventure(player))
			return null;
		class_1799 heldItem = player.method_6047();
		return LinkedControllerMenu.create(id, inv, heldItem);
	}

	@Override
	public class_2561 method_5476() {
		return method_7848();
	}

//	@Override
//	@Environment(EnvType.CLIENT)
//	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
//		consumer.accept(SimpleCustomRenderer.create(this, new LinkedControllerItemRenderer()));
//	}

}
