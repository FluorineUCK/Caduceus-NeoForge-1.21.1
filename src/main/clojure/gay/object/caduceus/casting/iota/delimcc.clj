(ns gay.object.caduceus.casting.iota.delimcc
  (:import (gay.object.caduceus.casting.iota DelimitedContinuationIota)))

(defn ->DelimitedContinuationIota [cont]
  (DelimitedContinuationIota/new cont))

(defn iota-type [] DelimitedContinuationIota/TYPE)
