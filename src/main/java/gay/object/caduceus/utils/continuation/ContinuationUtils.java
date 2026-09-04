package gay.object.caduceus.utils.continuation;

import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.utils.TreeList;
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContinuationUtils {
    private ContinuationUtils() {}

    public static List<ContinuationFrame> frames(SpellContinuation continuation) {
        var result = new ArrayList<ContinuationFrame>();
        var cursor = continuation;
        while (cursor instanceof SpellContinuation.NotDone notDone) {
            result.add(notDone.getFrame());
            cursor = notDone.getNext();
        }
        return result;
    }

    public static SpellContinuation fromFrames(List<? extends ContinuationFrame> frames) {
        SpellContinuation result = SpellContinuation.Done.INSTANCE;
        for (int i = frames.size() - 1; i >= 0; i--) {
            result = result.pushFrame(frames.get(i));
        }
        return result;
    }

    public static SpellContinuation add(SpellContinuation inner, SpellContinuation outer) {
        var frames = frames(inner);
        Collections.reverse(frames);
        var result = outer;
        for (var frame : frames) {
            result = result.pushFrame(frame);
        }
        return result;
    }

    public static SpellContinuation cleanThothFrames(SpellContinuation continuation) {
        var cleaned = new ArrayList<ContinuationFrame>();
        for (var frame : frames(continuation)) {
            if (frame instanceof FrameForEach each) {
                cleaned.add(new FrameForEach(
                    each.getData(), each.getCode(), each.getContextStack(), each.getStashedStack(),
                    TreeList.from(each.getAcc())
                ));
            } else {
                cleaned.add(frame);
            }
        }
        return fromFrames(cleaned);
    }

    public static @Nullable Iota getFrameMark(ContinuationFrame frame) {
        return frame instanceof ContinuationMarkHolder holder ? holder.caduceus$getMark() : null;
    }

    public static void setFrameMark(ContinuationFrame frame, Iota mark) {
        if (frame instanceof ContinuationMarkHolder holder) {
            holder.caduceus$setMark(mark);
        }
    }

    public static Component display(SpellContinuation continuation) {
        return display(continuation, "caduceus.tooltip.continuation");
    }

    public static Component display(SpellContinuation continuation, String translationKey) {
        MutableComponent contents = Component.empty();
        boolean first = true;
        for (var frame : frames(continuation)) {
            if (!first) contents.append(", ");
            first = false;
            ResourceLocation id = HexContinuationTypes.REGISTRY.getKey(frame.getType());
            String type = id == null ? frame.getClass().getName() : id.toString();
            MutableComponent name = Component.translatableWithFallback(
                "caduceus.tooltip.continuation.frame." + type, type
            );
            Iota mark = getFrameMark(frame);
            if (mark != null && !(mark instanceof NullIota)) {
                name = Component.translatable("caduceus.tooltip.continuation.frame.mark.inline", name);
            }
            contents.append(name);
        }
        return Component.translatable(translationKey, contents).withStyle(ChatFormatting.RED);
    }
}
