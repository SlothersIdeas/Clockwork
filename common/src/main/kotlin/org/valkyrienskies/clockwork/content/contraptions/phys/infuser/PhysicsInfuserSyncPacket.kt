package org.valkyrienskies.clockwork.content.contraptions.phys.infuser

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import org.valkyrienskies.clockwork.platform.api.network.ClientNetworkContext
import org.valkyrienskies.clockwork.platform.api.network.S2CCWPacket

class PhysicsInfuserSyncPacket : S2CCWPacket {
    override var player: Player? = null
    private var inventoryPacket: NonNullList<ItemStack>
    private val pos: BlockPos

    constructor(buf: FriendlyByteBuf) {
        // FIXED: Read pos first to match write()
        pos = buf.readBlockPos()

        val nbt = buf.readNbt()
        inventoryPacket = NonNullList.withSize(1, ItemStack.EMPTY)
        if (nbt != null) {
            ContainerHelper.loadAllItems(nbt, inventoryPacket)
        }
    }

    constructor(tile: PhysicsInfuserBlockEntity) {
        inventoryPacket = tile.inventory
        pos = tile.blockPos
    }

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeBlockPos(pos)
        val nbt = CompoundTag()
        ContainerHelper.saveAllItems(nbt, inventoryPacket)
        buffer.writeNbt(nbt)
    }

    override fun handle(context: ClientNetworkContext) {
        context.enqueueWork {
            val level = Minecraft.getInstance().level ?: return@enqueueWork
            val blockEntity = level.getBlockEntity(pos) as? PhysicsInfuserBlockEntity ?: return@enqueueWork
            blockEntity.inventory = inventoryPacket
        }
        context.setPacketHandled(true)
    }
}