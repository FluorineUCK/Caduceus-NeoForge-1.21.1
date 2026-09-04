; goto is a reserved Java identifier
(ns gay.object.caduceus.casting.actions.go-to
  (:require [gay.object.caduceus.casting.eval.vm.frames :as frames]
            [gay.object.caduceus.utils.casting :as casting]
            [gay.object.caduceus.utils.continuation :as continuation]
            [gay.object.caduceus.casting.mishaps.no-goto :as no-goto])
  (:import (at.petrak.hexcasting.api.casting OperatorUtils)
           (at.petrak.hexcasting.api.casting.castables Action)
           (at.petrak.hexcasting.api.casting.eval OperationResult)
           (at.petrak.hexcasting.api.casting.eval.vm FrameEvaluate)
           (at.petrak.hexcasting.api.casting.mishaps MishapNotEnoughArgs)
           (at.petrak.hexcasting.common.lib.hex HexEvalSounds)
           (gay.object.caduceus.casting.eval.vm.frames GotoFrame)))

(deftype OpSetupGoto []
  Action
  (operate [_this _env image cont]
    (let [stack (.getStack image)]
      (if (.isEmpty stack)
        (throw (MishapNotEnoughArgs/new 1 0)))
      (let [stack-size (.size stack)
            code (OperatorUtils/getList stack (dec stack-size) stack-size)]
        (OperationResult/new
          (casting/copy-image
            (.withUsedOp image)
            :stack (.init stack))
          []
          (continuation/push-all
            cont
            [(frames/->GotoFrame code)
             (FrameEvaluate/new code true)])
          (.get HexEvalSounds/HERMES))))))

(defn get-goto [cont]
  (if (continuation/done? cont)
    (throw (no-goto/->MishapNoGoto)))
  (if (instance? GotoFrame (.getFrame cont))
    cont
    (recur (.getNext cont))))

(defn goto [index code]
  (if (neg? index)
    (.takeRight code (- index))
    (.drop code index)))

(deftype OpGoto []
  Action
  (operate [_this _env image cont]
    (let [stack (.getStack image)]
      (if (.isEmpty stack)
        (throw (MishapNotEnoughArgs/new 1 0)))
      (let [stack-size (.size stack)
            new-cont (get-goto cont)
            code (-> new-cont .getFrame .getCode)
            code-size (.size code)
            index (OperatorUtils/getIntBetween
                    stack
                    (dec stack-size)
                    (- code-size)
                    code-size
                    stack-size)]
        (OperationResult/new
          (casting/copy-image
            (.withUsedOp image)
            :stack (.init stack))
          []
          (if (= index code-size)
            (.getNext new-cont)
            (->> code
                 (goto index)
                 (#(FrameEvaluate/new % true))
                 (.pushFrame new-cont)))
          (.get HexEvalSounds/NORMAL_EXECUTE))))))
