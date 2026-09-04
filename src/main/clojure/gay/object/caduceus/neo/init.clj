(ns gay.object.caduceus.neo.init
  (:require [gay.object.caduceus.core :as caduceus]
            [gay.object.caduceus.registry :as registry]
            [gay.object.caduceus.init :as caduceus.init])
  (:import (net.minecraft.resources ResourceLocation)
           (net.neoforged.bus.api IEventBus)
           (net.neoforged.neoforge.registries RegisterEvent RegisterEvent$RegisterHelper)))

(defn- init-registry [^IEventBus bus]
  (fn [registrar]
    (.addListener
      bus
      RegisterEvent
      (reify java.util.function.Consumer
        (accept [_ event]
          (.register
            ^RegisterEvent event
            (registry/get-registry-key registrar)
            (reify java.util.function.Consumer
              (accept [_ helper]
                (registry/init
                  registrar
                  (fn [^ResourceLocation id value]
                    (.register ^RegisterEvent$RegisterHelper helper id value)))))))))))

(defn init [^IEventBus bus]
  (caduceus.init/init (init-registry bus)))
