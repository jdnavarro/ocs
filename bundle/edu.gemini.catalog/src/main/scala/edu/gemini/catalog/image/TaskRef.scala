package edu.gemini.catalog.image

import scalaz.concurrent.Task

/** A concurrent mutable cell for type A. */
sealed trait TaskRef[A] {

  /** Atomic modification. */
  def mod(f: A => A): Task[Unit]

  /** Return the current value. */
  def get: Task[A]

  /** Replace the current value. */
  def put(a: A): Task[Unit] =
    mod(_ => a)

}

object TaskRef {

  /** Create a new TaskRef. */
  def newTaskRef[A](a: A): Task[TaskRef[A]] =
    Task.delay {
      @volatile var value = a
      new TaskRef[A] { ref =>
        def get: Task[A] = Task.delay(value)
        def mod(f: A => A): Task[Unit] = Task.delay(ref.synchronized(value = f(value)))
      }
    }

}
