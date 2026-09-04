(ns gay.object.caduceus.casting.eval.vm.frames
  (:import (gay.object.caduceus.casting.eval.vm.frames GotoFrame PromptFrame)))

(defn ->PromptFrame [mark] (PromptFrame/new mark))
(defn empty-prompt-frame [] (PromptFrame/empty))
(defn prompt-frame-type [] PromptFrame/TYPE)

(defn ->GotoFrame [code] (GotoFrame/new code))
(defn goto-frame-type [] GotoFrame/TYPE)
