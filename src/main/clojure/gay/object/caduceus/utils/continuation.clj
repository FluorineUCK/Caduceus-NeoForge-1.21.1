(ns gay.object.caduceus.utils.continuation
  (:import (at.petrak.hexcasting.api.casting.eval.vm SpellContinuation SpellContinuation$Done SpellContinuation$NotDone)
           (at.petrak.hexcasting.api.casting.iota NullIota)
           (at.petrak.hexcasting.api.casting.mishaps MishapInvalidIota MishapOthersName)
           (gay.object.caduceus.utils.continuation ContinuationMarkHolder ContinuationUtils)))

(defn done? [cont] (instance? SpellContinuation$Done cont))
(defn not-done? [cont] (instance? SpellContinuation$NotDone cont))
(defn frame [cont] (when (not-done? cont) (.getFrame cont)))

(defn make
  ([frame] (.pushFrame SpellContinuation$Done/INSTANCE frame))
  ([frame next] (SpellContinuation$NotDone/new frame next)))

(defn frames [cont] (ContinuationUtils/frames cont))

(defn push-all
  "Takes a sequence of frames from bottom to top, and pushes them to a continuation."
  ([coll] (push-all SpellContinuation$Done/INSTANCE coll))
  ([cont coll] (reduce #(.pushFrame %1 %2) cont coll)))

(defn add [i j] (ContinuationUtils/add i j))
(defn clean-thoth-frames [cont] (ContinuationUtils/cleanThothFrames cont))

(defn get-frame-mark
  ([frame] (get-frame-mark frame nil))
  ([frame not-found]
   (if (instance? ContinuationMarkHolder frame)
     (.caduceus$getMark ^ContinuationMarkHolder frame)
     not-found)))

(defn get-mark [cont]
  (if-let [top (frame cont)]
    (get-frame-mark top (NullIota/new))
    (NullIota/new)))

(defn set-frame-mark [frame iota _world]
  (when (instance? ContinuationMarkHolder frame)
    (.caduceus$setMark ^ContinuationMarkHolder frame iota)))

(defn set-mark [cont mark world]
  (when-let [top (frame cont)]
    (set-frame-mark top mark world)))

(defn assert-valid-mark [mark reverse-idx world]
  (when (> (.size mark) 1)
    (throw (MishapInvalidIota/ofType mark reverse-idx "continuation_mark")))
  (when-let [mishap (MishapOthersName/getTrueNameMishapFromDatum world mark nil)]
    (throw mishap)))

(defn display
  ([cont] (ContinuationUtils/display cont))
  ([cont i18n-key] (ContinuationUtils/display cont i18n-key)))
