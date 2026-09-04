package gay.object.caduceus.utils.continuation;

import at.petrak.hexcasting.api.casting.iota.Iota;

public interface ContinuationMarkHolder {
    Iota caduceus$getMark();

    void caduceus$setMark(Iota mark);
}
