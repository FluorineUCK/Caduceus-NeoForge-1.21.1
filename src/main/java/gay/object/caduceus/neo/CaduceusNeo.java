package gay.object.caduceus.neo;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(CaduceusNeo.MOD_ID)
public final class CaduceusNeo {
    public static final String MOD_ID = "caduceus";

    public CaduceusNeo(IEventBus modBus) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("gay.object.caduceus.neo.init"));
        Clojure.var("gay.object.caduceus.neo.init", "init").invoke(modBus);
    }
}
