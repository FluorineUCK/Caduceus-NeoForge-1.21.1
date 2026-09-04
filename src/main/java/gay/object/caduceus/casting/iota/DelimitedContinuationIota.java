package gay.object.caduceus.casting.iota;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import com.mojang.serialization.MapCodec;
import gay.object.caduceus.utils.continuation.ContinuationUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class DelimitedContinuationIota extends Iota {
    private final SpellContinuation continuation;

    public DelimitedContinuationIota(SpellContinuation continuation) {
        super(() -> TYPE);
        this.continuation = ContinuationUtils.cleanThothFrames(continuation);
    }

    public SpellContinuation getContinuation() {
        return continuation;
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    protected boolean toleratesOther(Iota that) {
        return Iota.typesMatch(this, that)
            && that instanceof DelimitedContinuationIota other
            && continuation.equals(other.continuation);
    }

    @Override
    public @NotNull CastResult execute(CastingVM vm, ServerLevel world, SpellContinuation outer) {
        return new CastResult(this, ContinuationUtils.add(continuation, outer), vm.getImage(), List.of(),
            ResolvedPatternType.EVALUATED, HexEvalSounds.HERMES.get());
    }

    @Override
    public boolean executable() {
        return true;
    }

    @Override
    public int size() {
        int size = 0;
        for (var frame : ContinuationUtils.frames(continuation)) size += 1 + frame.size();
        return size;
    }

    @Override
    public Component display() {
        return ContinuationUtils.display(continuation, "caduceus.tooltip.continuation.delimited");
    }

    @Override
    public int hashCode() {
        return continuation.hashCode();
    }

    public static final IotaType<DelimitedContinuationIota> TYPE = new IotaType<>() {
        private final MapCodec<DelimitedContinuationIota> codec = SpellContinuation.getCODEC()
            .fieldOf("value").xmap(DelimitedContinuationIota::new, DelimitedContinuationIota::getContinuation);
        private final StreamCodec<RegistryFriendlyByteBuf, DelimitedContinuationIota> streamCodec =
            SpellContinuation.getSTREAM_CODEC().map(DelimitedContinuationIota::new,
                DelimitedContinuationIota::getContinuation);

        @Override
        public MapCodec<DelimitedContinuationIota> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DelimitedContinuationIota> streamCodec() {
            return streamCodec;
        }

        @Override
        public int color() {
            return 0xffaa0000;
        }
    };
}
