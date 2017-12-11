sealed trait List[+A] {

  def map[B](f: A => B): List[B]
  def headOption: Option[A]
  def flatMap[B](f: A => List[B]): List[B]
  def ++(other: List[A]): List[A]
}

case object Empty extends List[Nothing] {

  override def map[B](f: Nothing => B): List[B] =
    Empty

  override def headOption: Option[Nothing] =
    None

  override def ++(other: List[A]): List[A] =
    other

  override def flatMap[B](f: Nothing => B): List[B] =
    Empty
}

case class Cons[A](head: A, tail: List[A]) extends List[A] {

  override def map[B](f: A => B): List[B] =
    Cons(f(head), tail.map(f))

  override def headOption: Option[A] =
    Some(head)

  override def  ++(other: List[A]): List[A] =
    Cons(head, tail ++ other)

  override def flatMap[B](f: A => List[B]): List[B] =
    f(head) ++ tail.flatMap(f)


}