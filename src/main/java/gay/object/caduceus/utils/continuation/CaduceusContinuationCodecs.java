package gay.object.caduceus.utils.continuation;

import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CaduceusContinuationCodecs {
    private CaduceusContinuationCodecs() {}

    private record MarkedFrame(ContinuationFrame frame, Optional<Iota> mark) {
        private static final Codec<MarkedFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContinuationFrame.Type.Companion.getTYPED_CODEC().fieldOf("frame").forGetter(MarkedFrame::frame),
            IotaType.TYPED_CODEC.optionalFieldOf("mark").forGetter(MarkedFrame::mark)
        ).apply(instance, MarkedFrame::new));
    }

    private static Codec<SpellContinuation> originalCodec;
    private static Codec<SpellContinuation> wrappedCodec;
    private static StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> originalStreamCodec;
    private static StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> wrappedStreamCodec;

    public static synchronized Codec<SpellContinuation> wrapCodec(Codec<SpellContinuation> original) {
        if (original == wrappedCodec) return wrappedCodec;
        if (original == originalCodec && wrappedCodec != null) return wrappedCodec;
        originalCodec = original;

        Codec<SpellContinuation> marked = MarkedFrame.CODEC.listOf().xmap(
            CaduceusContinuationCodecs::fromMarkedFrames,
            CaduceusContinuationCodecs::toMarkedFrames
        );
        wrappedCodec = Codec.either(marked, original).xmap(
            either -> either.map(value -> value, value -> value),
            Either::left
        );
        return wrappedCodec;
    }

    public static synchronized StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> wrapStreamCodec(
        StreamCodec<RegistryFriendlyByteBuf, SpellContinuation> original
    ) {
        if (original == wrappedStreamCodec) return wrappedStreamCodec;
        if (original == originalStreamCodec && wrappedStreamCodec != null) return wrappedStreamCodec;
        originalStreamCodec = original;
        wrappedStreamCodec = new StreamCodec<>() {
            @Override
            public SpellContinuation decode(RegistryFriendlyByteBuf buffer) {
                int size = buffer.readVarInt();
                var frames = new ArrayList<ContinuationFrame>(size);
                for (int i = 0; i < size; i++) {
                    ContinuationFrame frame = ContinuationFrame.Type.Companion.getTYPED_STREAM_CODEC().decode(buffer);
                    if (buffer.readBoolean()) {
                        ContinuationUtils.setFrameMark(frame, IotaType.TYPED_STREAM_CODEC.decode(buffer));
                    }
                    frames.add(frame);
                }
                return ContinuationUtils.fromFrames(frames);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, SpellContinuation continuation) {
                var frames = ContinuationUtils.frames(continuation);
                buffer.writeVarInt(frames.size());
                for (var frame : frames) {
                    ContinuationFrame.Type.Companion.getTYPED_STREAM_CODEC().encode(buffer, frame);
                    Iota mark = ContinuationUtils.getFrameMark(frame);
                    boolean present = mark != null && !(mark instanceof NullIota);
                    buffer.writeBoolean(present);
                    if (present) IotaType.TYPED_STREAM_CODEC.encode(buffer, mark);
                }
            }
        };
        return wrappedStreamCodec;
    }

    private static SpellContinuation fromMarkedFrames(List<MarkedFrame> entries) {
        var frames = new ArrayList<ContinuationFrame>(entries.size());
        for (var entry : entries) {
            entry.mark().ifPresent(mark -> ContinuationUtils.setFrameMark(entry.frame(), mark));
            frames.add(entry.frame());
        }
        return ContinuationUtils.fromFrames(frames);
    }

    private static List<MarkedFrame> toMarkedFrames(SpellContinuation continuation) {
        var result = new ArrayList<MarkedFrame>();
        for (var frame : ContinuationUtils.frames(continuation)) {
            Iota mark = ContinuationUtils.getFrameMark(frame);
            result.add(new MarkedFrame(frame,
                mark == null || mark instanceof NullIota ? Optional.empty() : Optional.of(mark)));
        }
        return result;
    }
}
