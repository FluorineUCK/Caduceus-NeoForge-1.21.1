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
import kotlin.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class GotoFrame implements ContinuationFrame {
    private final TreeList<Iota> code;

    public GotoFrame(TreeList<Iota> code) {
        this.code = code;
    }

    public TreeList<Iota> getCode() {
        return code;
    }

    @Override
    public Pair<Boolean, TreeList<Iota>> breakDownwards(TreeList<Iota> stack) {
        return new Pair<>(true, stack);
    }

    @Override
    public CastResult evaluate(SpellContinuation continuation, ServerLevel level, CastingVM harness) {
        return new CastResult(new NullIota(), continuation, null, List.of(),
            ResolvedPatternType.EVALUATED, HexEvalSounds.NOTHING.get());
    }

    @Override
    public int size() {
        return 1 + code.size();
    }

    @Override
    public Type<?> getType() {
        return TYPE;
    }

    public static final Type<GotoFrame> TYPE = new Type<>() {
        private final MapCodec<GotoFrame> codec = TreeList.codecOf(IotaType.TYPED_CODEC)
            .fieldOf("code").xmap(GotoFrame::new, GotoFrame::getCode);
        private final StreamCodec<RegistryFriendlyByteBuf, GotoFrame> streamCodec =
            IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()).map(GotoFrame::new, GotoFrame::getCode);

        @Override
        public MapCodec<GotoFrame> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GotoFrame> streamCodec() {
            return streamCodec;
        }
    };
}
