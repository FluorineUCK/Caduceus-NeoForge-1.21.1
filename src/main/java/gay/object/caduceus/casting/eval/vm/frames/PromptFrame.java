package gay.object.caduceus.casting.eval.vm.frames;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import com.mojang.serialization.MapCodec;
import gay.object.caduceus.utils.continuation.ContinuationMarkHolder;
import kotlin.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class PromptFrame implements ContinuationFrame, ContinuationMarkHolder {
    private Iota mark;

    public PromptFrame(Iota mark) {
        this.mark = mark;
    }

    public static PromptFrame empty() {
        return new PromptFrame(new NullIota());
    }

    @Override
    public Pair<Boolean, TreeList<Iota>> breakDownwards(TreeList<Iota> stack) {
        return new Pair<>(false, stack);
    }

    @Override
    public CastResult evaluate(SpellContinuation continuation, ServerLevel level, CastingVM harness) {
        return new CastResult(new NullIota(), continuation, null, List.of(),
            ResolvedPatternType.EVALUATED, HexEvalSounds.NOTHING.get());
    }

    @Override
    public int size() {
        return mark.size();
    }

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    @Override
    public Iota caduceus$getMark() {
        return mark;
    }

    @Override
    public void caduceus$setMark(Iota mark) {
        this.mark = mark;
    }

    public static final Type<PromptFrame> TYPE = new Type<>() {
        private final MapCodec<PromptFrame> codec = IotaType.TYPED_CODEC
            .fieldOf("mark").xmap(PromptFrame::new, PromptFrame::caduceus$getMark);
        private final StreamCodec<RegistryFriendlyByteBuf, PromptFrame> streamCodec =
            IotaType.TYPED_STREAM_CODEC.map(PromptFrame::new, PromptFrame::caduceus$getMark);

        @Override
        public MapCodec<PromptFrame> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PromptFrame> streamCodec() {
            return streamCodec;
        }
    };
}
