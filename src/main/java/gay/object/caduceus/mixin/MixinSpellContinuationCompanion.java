package gay.object.caduceus.mixin;

import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import gay.object.caduceus.utils.continuation.CaduceusContinuationCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation$Companion", remap = false)
public abstract class MixinSpellContinuationCompanion {
    @ModifyReturnValue(method = "getCODEC", at = @At("RETURN"))
    private Codec<SpellContinuation> caduceus$preserveMarksInCodec(Codec<SpellContinuation> original) {
        return CaduceusContinuationCodecs.wrapCodec(original);
    }

    @ModifyReturnValue(method = "getSTREAM_CODEC", at = @At("RETURN"))
    private StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> caduceus$preserveMarksInStreamCodec(
        StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> original
    ) {
        return CaduceusContinuationCodecs.wrapStreamCodec(original);
    }
}
