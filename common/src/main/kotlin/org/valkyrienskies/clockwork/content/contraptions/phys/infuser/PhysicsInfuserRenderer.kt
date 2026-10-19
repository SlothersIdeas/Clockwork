package org.valkyrienskies.clockwork.content.contraptions.phys.infuser


import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer
import net.createmod.catnip.animation.AnimationTickHolder
import net.createmod.catnip.render.CachedBuffers
import net.createmod.catnip.render.SuperByteBuffer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import org.valkyrienskies.clockwork.ClockworkPartials
import org.valkyrienskies.clockwork.util.ClockworkAnimationUtils
import org.valkyrienskies.clockwork.util.render.RenderUtil
import org.valkyrienskies.clockwork.util.render.TransformData
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.world.inventory.InventoryMenu

class PhysicsInfuserRenderer(context: BlockEntityRendererProvider.Context?) :
    SmartBlockEntityRenderer<PhysicsInfuserBlockEntity>(context) {
    private var teForScanner: PhysicsInfuserBlockEntity? = null
    override fun renderSafe(be: PhysicsInfuserBlockEntity, partialTicks: Float, ms: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int) {
        if (be !is PhysicsInfuserBlockEntity) return
        this.teForScanner = be
        val infuser = be
        val blockState = be.blockState
        val vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS))


        val infuser_frame = CachedBuffers.partial(ClockworkPartials.INFUSER_FRAME, blockState).light<SuperByteBuffer>(light)
        val infuser_vortex = CachedBuffers.partial(ClockworkPartials.INFUSER_VORTEX, blockState).light<SuperByteBuffer>(light)

        ms.pushPose()
       val interpolatedAngle = infuser.getInterpolatedCoreAngle(AnimationTickHolder.getPartialTicks() - 1)
        val t = be.getAnimationProgress()

        if (infuser.animationType != null) {
            if (infuser.animationType === PhysicsInfuserBlockEntity.Animation.ASSEMBLY) {
                ClockworkAnimationUtils().applyAnimationToPartial(infuser_frame, PhysicsInfuserAnimations.assemble!!, "frame", Vec3(0.0 / 16, 0.0 / 16, 0.0 / 16), t)
                ClockworkAnimationUtils().applyAnimationToPartial(infuser_vortex, PhysicsInfuserAnimations.assemble!!, "vortex", Vec3(0.0 / 16, 8.0/16, 0.0 / 16), t)
                infuser_vortex.color<SuperByteBuffer>(255,255,255,Mth.clampedLerp(255.0,0.0, t-8.8).toInt())
}
            else if (infuser.animationType === PhysicsInfuserBlockEntity.Animation.IDLE) {
                infuser_vortex.scale(0f)
                infuser_vortex.scale(0f)
                infuser_frame.translate(Vector3f(0.5f,0.5f,0.5f))
                infuser_frame.rotateYDegrees(-45f)
                infuser_frame.translateBack(Vector3f(0.5f,0.5f,0.5f))
            }
        }
        infuser_frame.renderInto(ms, vertexConsumer)
        infuser_vortex.renderInto(ms, vertexConsumer)


        if (!be.inventory[0].isEmpty) {
            val innerData = TransformData(Vector3f(0f, 0f, 0f), Vector3f(interpolatedAngle, interpolatedAngle, 0f))
            val data = TransformData(Vector3f(0f, 0f, 0f), Vector3f(0f, interpolatedAngle, 0f))
            val outerData = TransformData(Vector3f(0f, 0f, 0f), Vector3f(0f, interpolatedAngle, 0f))

                RenderUtil.renderCubeMatrix(ms, buffer, blockState, innerData, data, outerData,1.5f, light,overlay)
            }
        ms.popPose()
        }

    companion object {
        // The number of ticks over which to compute scan results. Which is at the
        // same time the use time of the scanner item.
        const val SCAN_COMPUTE_DURATION = 40

        // Initial radius of the scan wave.
        const val SCAN_INITIAL_RADIUS = 10

        // Scan wave growth time offset to avoid super slow start speed.
        const val SCAN_TIME_OFFSET = 200

        // How long the ping takes to reach the end of the visible area.
        const val SCAN_GROWTH_DURATION = 2000

        // Reference render distance the above constants are relative to.
        private const val REFERENCE_RENDER_DISTANCE = 12

        // --------------------------------------------------------------------- //
        var scanningTicks = -1

        // --------------------------------------------------------------------- //
        // List of providers currently used to scan.
        var currentStart: Long = -1
        var lastScanCenter: Vec3? = null
        var viewModelStack: PoseStack? = null
        var projectionMatrix: Matrix4f? = null

        // --------------------------------------------------------------------- //
        fun cancelScan() {
            scanningTicks = 0
        }

        private fun clear() {
            lastScanCenter = null
            currentStart = -1
        }
    }
}
