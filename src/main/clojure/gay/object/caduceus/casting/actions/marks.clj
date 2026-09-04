(ns gay.object.caduceus.casting.actions.marks
  (:require [gay.object.caduceus.utils.casting :as casting]
            [gay.object.caduceus.utils.continuation :as continuation])
  (:import (at.petrak.hexcasting.api.casting.castables Action ConstMediaAction ConstMediaAction$DefaultImpls)
           (at.petrak.hexcasting.api.casting.eval OperationResult)
           (at.petrak.hexcasting.api.casting.mishaps MishapNotEnoughArgs)
           (at.petrak.hexcasting.common.lib.hex HexEvalSounds)))

(deftype OpReadLocalMark []
  Action
  (operate [_this _env image cont]
    (let [mark (continuation/get-mark cont)]
      (OperationResult/new
        (casting/copy-image
          (.withUsedOp image)
          :stack (-> image .getStack (.appended mark)))
        []
        cont
        (.get HexEvalSounds/NORMAL_EXECUTE)))))

(deftype OpReadIotaMark []
  ConstMediaAction
  (getArgc [_this] 1)
  (getMediaCost [_this] 0)
  (execute [this args _env]
    (-> args
        (casting/get-continuation 0 (.getArgc this))
        continuation/get-mark
        vector))
  (executeWithOpCount [this args env]
    (ConstMediaAction$DefaultImpls/executeWithOpCount this args env))
  (operate [this env image cont]
    (ConstMediaAction$DefaultImpls/operate this env image cont)))

(deftype OpWriteLocalMark []
  Action
  (operate [_this env image cont]
    (let [stack (.getStack image)
          mark (when-not (.isEmpty stack) (.last stack))]
      (if (nil? mark)
        (throw (MishapNotEnoughArgs/new 1 0)))
      (continuation/assert-valid-mark mark 0 (.getWorld env))
      (continuation/set-mark cont mark (.getWorld env))
      (OperationResult/new
        (casting/copy-image
          (.withUsedOp image)
          :stack (.init stack))
        []
        cont
        (.get HexEvalSounds/NORMAL_EXECUTE)))))
