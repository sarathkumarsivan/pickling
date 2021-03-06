package scala.pickling
package pickler

import scala.collection.generic.CanBuildFrom
import scala.collection.{IndexedSeq, LinearSeq}

// TODO(jvican) - Register runtime pickler generators

trait SeqPicklers {
  implicit def seqPickler[T : FastTypeTag](
      implicit elemPickler: Pickler[T],
      elemUnpickler: Unpickler[T],
      collTag: FastTypeTag[Seq[T]],
      cbf: CanBuildFrom[Seq[T], T, Seq[T]]
  ): AbstractPicklerUnpickler[Seq[T]] = SeqSetPickler[T, Seq]
}

trait IndexedSeqPicklers {
  implicit def indexedSeqPickler[T : FastTypeTag](
      implicit elemPickler: Pickler[T],
      elemUnpickler: Unpickler[T],
      collTag: FastTypeTag[IndexedSeq[T]],
      cbf: CanBuildFrom[IndexedSeq[T], T, IndexedSeq[T]]
  ): AbstractPicklerUnpickler[IndexedSeq[T]] = SeqSetPickler[T, IndexedSeq]
}

trait LinearSeqPicklers {
  implicit def linearSeqPickler[T : FastTypeTag](
      implicit elemPickler: Pickler[T],
      elemUnpickler: Unpickler[T],
      collTag: FastTypeTag[LinearSeq[T]],
      cbf: CanBuildFrom[LinearSeq[T], T, LinearSeq[T]]
  ): AbstractPicklerUnpickler[LinearSeq[T]] = SeqSetPickler[T, LinearSeq]
}
