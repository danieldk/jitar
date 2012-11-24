package eu.danieldk.nlp.jitar.data

class UniGram(t1c: Int) {
  var t1: Int = t1c

  override def equals(other: Any): Boolean = other match {
    case that: UniGram => this.t1 == that.t1
    case _             => false
  }

  override def hashCode = t1;

  override def toString(): String = t1.toString;
}
