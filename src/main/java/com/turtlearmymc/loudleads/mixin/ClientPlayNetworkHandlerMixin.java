package com.turtlearmymc.loudleads.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeadKnotEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    private long lastTime = -1;
    private List<Integer> leadKnotsDetatchedFromThisTickIds = new ArrayList<Integer>();

    @Inject(method="onEntityAttach", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/mob/MobEntity;setHoldingEntityId(I)V"
        )
    )
    private void onEntityAttachSound(EntityAttachS2CPacket packet, CallbackInfo ci) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = ((ClientPlayNetworkHandler) (Object) this);

        World world = clientPlayNetworkHandler.getWorld();
        MobEntity attachedEntity = (MobEntity) world.getEntityById(packet.getAttachedEntityId());
        Entity newHoldingEntity = world.getEntityById(packet.getHoldingEntityId());
        PlayerEntity p = MinecraftClient.getInstance().player;

        if (newHoldingEntity == null) {
            // Detaching sounds
            Entity currentHoldingEntity = attachedEntity.getHoldingEntity();

            if (!(currentHoldingEntity instanceof LeadKnotEntity)) {
                // Detach from player sound
                double x = attachedEntity.x, y = attachedEntity.y, z = attachedEntity.z;
                world.playSound(p, x, y, z, SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            } else {
                if (world.getLevelProperties().getTime() != lastTime) {
                    lastTime = world.getLevelProperties().getTime();
                    leadKnotsDetatchedFromThisTickIds.clear();
                }
                if (!leadKnotsDetatchedFromThisTickIds.contains(currentHoldingEntity.getEntityId())) {
                    // Breaking fence lead sound
                    double x = currentHoldingEntity.x, y = currentHoldingEntity.y, z = currentHoldingEntity.z;
                    world.playSound(p, x, y, z, SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                    leadKnotsDetatchedFromThisTickIds.add(currentHoldingEntity.getEntityId());
                }
            }
        } else if (!(newHoldingEntity instanceof LeadKnotEntity)) {
            // Attach to player sound
            double x = attachedEntity.x, y = attachedEntity.y, z = attachedEntity.z;
            world.playSound(p, x, y, z, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        } else if (newHoldingEntity.age != 0) {
            // Attach to pre-existing fence lead sound
            double x = newHoldingEntity.x, y = newHoldingEntity.y, z = newHoldingEntity.z;
            world.playSound(p, x, y, z, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Inject(method="onGameJoin", at = @At("HEAD"))
    private void onGameJoin(CallbackInfo ci) {
        lastTime = 0;
        leadKnotsDetatchedFromThisTickIds.clear();
    }
}