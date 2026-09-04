package gay.object.caduceus.mixin.iota;

import gay.object.caduceus.utils.continuation.ContinuationUtils;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.ContinuationIota;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ContinuationIota.class, remap = false)
public abstract class MixinContinuationIota {
    @Shadow public abstract SpellContinuation getContinuation();

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static SpellContinuation caduceus$copyThothAccumulatorsToStopThemFromCrashingTheGame(
        SpellContinuation continuation
    ) {
        return ContinuationUtils.cleanThothFrames(continuation);
    }

    @Redirect(
        method = "size",
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I")
    )
    private int caduceus$stopIgnoringTheEntireSizeCalculation(int a, int b) {
        return Math.max(a, b);
    }

    @Inject(method = "display", at = @At("HEAD"), cancellable = true)
    private void caduceus$betterDisplay(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(ContinuationUtils.display(getContinuation()));
    }
}
