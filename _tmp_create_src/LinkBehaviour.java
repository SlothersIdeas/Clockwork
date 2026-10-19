package com.simibubi.create.content.redstone.link;

import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.data.Couple;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2680;

public class LinkBehaviour extends BlockEntityBehaviour implements IRedstoneLinkable, ClipboardCloneable {

	public static final BehaviourType<LinkBehaviour> TYPE = new BehaviourType<>();

	enum Mode {
		TRANSMIT, RECEIVE
	}

	Frequency frequencyFirst;
	Frequency frequencyLast;
	ValueBoxTransform firstSlot;
	ValueBoxTransform secondSlot;
	class_243 textShift;

	public boolean newPosition;
	private Mode mode;
	private IntSupplier transmission;
	private IntConsumer signalCallback;

	protected LinkBehaviour(SmartBlockEntity be, Pair<ValueBoxTransform, ValueBoxTransform> slots) {
		super(be);
		frequencyFirst = Frequency.EMPTY;
		frequencyLast = Frequency.EMPTY;
		firstSlot = slots.getLeft();
		secondSlot = slots.getRight();
		textShift = class_243.field_1353;
		newPosition = true;
	}

	public static LinkBehaviour receiver(SmartBlockEntity be, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntConsumer signalCallback) {
		LinkBehaviour behaviour = new LinkBehaviour(be, slots);
		behaviour.signalCallback = signalCallback;
		behaviour.mode = Mode.RECEIVE;
		return behaviour;
	}

	public static LinkBehaviour transmitter(SmartBlockEntity be, Pair<ValueBoxTransform, ValueBoxTransform> slots,
		IntSupplier transmission) {
		LinkBehaviour behaviour = new LinkBehaviour(be, slots);
		behaviour.transmission = transmission;
		behaviour.mode = Mode.TRANSMIT;
		return behaviour;
	}

	public LinkBehaviour moveText(class_243 shift) {
		textShift = shift;
		return this;
	}

	public void copyItemsFrom(LinkBehaviour behaviour) {
		if (behaviour == null)
			return;
		frequencyFirst = behaviour.frequencyFirst;
		frequencyLast = behaviour.frequencyLast;
	}

	@Override
	public boolean isListening() {
		return mode == Mode.RECEIVE;
	}

	@Override
	public int getTransmittedStrength() {
		return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0;
	}

	@Override
	public void setReceivedStrength(int networkPower) {
		if (!newPosition)
			return;
		signalCallback.accept(networkPower);
	}

	public void notifySignalChange() {
		Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(getWorld(), this);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().field_9236)
			return;
		getHandler().addToNetwork(getWorld(), this);
		newPosition = true;
	}

	@Override
	public Couple<Frequency> getNetworkKey() {
		return Couple.create(frequencyFirst, frequencyLast);
	}

	@Override
	public void unload() {
		super.unload();
		if (getWorld().field_9236)
			return;
		getHandler().removeFromNetwork(getWorld(), this);
	}

	@Override
	public boolean isSafeNBT() {
		return true;
	}

	@Override
	public void write(class_2487 nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		nbt.method_10566("FrequencyFirst", frequencyFirst.getStack()
			.method_7953(new class_2487()));
		nbt.method_10566("FrequencyLast", frequencyLast.getStack()
			.method_7953(new class_2487()));
		nbt.method_10544("LastKnownPosition", blockEntity.method_11016()
			.method_10063());
	}

	@Override
	public void read(class_2487 nbt, boolean clientPacket) {
		long positionInTag = blockEntity.method_11016()
			.method_10063();
		long positionKey = nbt.method_10537("LastKnownPosition");
		newPosition = positionInTag != positionKey;

		super.read(nbt, clientPacket);
		frequencyFirst = Frequency.of(class_1799.method_7915(nbt.method_10562("FrequencyFirst")));
		frequencyLast = Frequency.of(class_1799.method_7915(nbt.method_10562("FrequencyLast")));
	}

	public void setFrequency(boolean first, class_1799 stack) {
		stack = stack.method_7972();
		stack.method_7939(1);
		class_1799 toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
		boolean changed = !class_1799.method_31577(stack, toCompare);

		if (changed)
			getHandler().removeFromNetwork(getWorld(), this);

		if (first)
			frequencyFirst = Frequency.of(stack);
		else
			frequencyLast = Frequency.of(stack);

		if (!changed)
			return;

		blockEntity.sendData();
		getHandler().addToNetwork(getWorld(), this);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	private RedstoneLinkNetworkHandler getHandler() {
		return Create.REDSTONE_LINK_NETWORK_HANDLER;
	}

	public static class SlotPositioning {
		Function<class_2680, Pair<class_243, class_243>> offsets;
		Function<class_2680, class_243> rotation;
		float scale;

		public SlotPositioning(Function<class_2680, Pair<class_243, class_243>> offsetsForState,
			Function<class_2680, class_243> rotationForState) {
			offsets = offsetsForState;
			rotation = rotationForState;
			scale = 1;
		}

		public SlotPositioning scale(float scale) {
			this.scale = scale;
			return this;
		}

	}

	public boolean testHit(Boolean first, class_243 hit) {
		class_2680 state = blockEntity.method_11010();
		class_243 localHit = hit.method_1020(class_243.method_24954(blockEntity.method_11016()));
		return (first ? firstSlot : secondSlot).testHit(getWorld(), getPos(), state, localHit);
	}

	@Override
	public boolean isAlive() {
		class_1937 level = getWorld();
		class_2338 pos = getPos();
		if (blockEntity.isChunkUnloaded())
			return false;
		if (blockEntity.method_11015())
			return false;
		if (!level.method_8477(pos))
			return false;
		return level.method_8321(pos) == blockEntity;
	}

	@Override
	public class_2338 getLocation() {
		return getPos();
	}

	@Override
	public String getClipboardKey() {
		return "Frequencies";
	}

	@Override
	public boolean writeToClipboard(class_2487 tag, class_2350 side) {
		tag.method_10566("First", frequencyFirst.getStack()
			.method_7953(new class_2487()));
		tag.method_10566("Last", frequencyLast.getStack()
			.method_7953(new class_2487()));
		return true;
	}

	@Override
	public boolean readFromClipboard(class_2487 tag, class_1657 player, class_2350 side, boolean simulate) {
		if (!tag.method_10545("First") || !tag.method_10545("Last"))
			return false;
		if (simulate)
			return true;
		setFrequency(true, class_1799.method_7915(tag.method_10562("First")));
		setFrequency(false, class_1799.method_7915(tag.method_10562("Last")));
		return true;
	}

}
