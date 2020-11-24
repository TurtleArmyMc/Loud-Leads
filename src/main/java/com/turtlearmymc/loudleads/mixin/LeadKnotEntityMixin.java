package com.turtlearmymc.loudleads.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.decoration.LeadKnotEntity;

@Mixin(LeadKnotEntity.class)
public abstract class LeadKnotEntityMixin {
    // Mutes default lead knot breaking sound when punched/fence is destroyed
    // Only works on the integrated server

    private Map<Integer, Boolean> leadsSilence = new HashMap<Integer, Boolean>();

    @Inject(method="onBreak", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/decoration/LeadKnotEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"
        )
    )
    private void silenceBeforeBreakSound(CallbackInfo ci) {
        LeadKnotEntity lead = ((LeadKnotEntity) (Object) this);
        leadsSilence.put(lead.getEntityId(), lead.isSilent());
        lead.setSilent(true);
    }

    @Inject(method="onBreak", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/decoration/LeadKnotEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V",
        shift = At.Shift.AFTER
        )
    )
    private void resetSilenceAfterBreakSound(CallbackInfo ci) {
        LeadKnotEntity lead = ((LeadKnotEntity) (Object) this);
        lead.setSilent(leadsSilence.get(lead.getEntityId()));
        leadsSilence.remove(lead.getEntityId());
    }
}