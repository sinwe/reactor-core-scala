package reactor.core.scala.publisher

import java.time.Duration
import java.util.function.{Consumer, Function, Supplier}

import org.reactivestreams.{Publisher, Subscriber}
import reactor.core.publisher.{MonoSink, Mono => JMono}
import java.lang.{Long => JLong}

import reactor.core.scheduler.TimedScheduler


/**
  * Created by winarto on 12/26/16.
  */
class Mono[T](val jMono: JMono[T]) extends Publisher[T] {
  override def subscribe(s: Subscriber[_ >: T]): Unit = jMono.subscribe(s)
}

object Mono {
  def create[T](callback: MonoSink[T] => Unit): Mono[T] = {
    new Mono[T](
      JMono.create(new Consumer[MonoSink[T]] {
        override def accept(t: MonoSink[T]): Unit = callback(t)
      })
    )
  }

  def defer[T](supplier: () => Mono[T]): Mono[T] = {
    new Mono[T](
      JMono.defer(new Supplier[JMono[T]] {
        override def get(): JMono[T] = supplier().jMono
      })
    )
  }

  def delay(duration: scala.concurrent.duration.Duration): Mono[Long] = {
    new Mono[Long](
      JMono.delay(Duration.ofNanos(duration.toNanos))
        .map(new Function[JLong, Long] {
          override def apply(t: JLong): Long = t
        })
    )
  }

  def delayMillis(duration: Long): Mono[Long] = {
    new Mono[Long](
      JMono.delayMillis(Long2long(duration))
        .map(new Function[JLong, Long] {
          override def apply(t: JLong): Long = t
        })
    )
  }

  def delayMillis(duration: Long, timedScheduler: TimedScheduler): Mono[Long] = {
    new Mono[Long](
      JMono.delayMillis(Long2long(duration), timedScheduler)
        .map(new Function[JLong, Long] {
          override def apply(t: JLong): Long = t
        })
    )
  }

  def empty[T]: Mono[T] = {
    new Mono[T](
      JMono.empty()
    )
  }

  def empty[T](source: Publisher[T]): Mono[Unit] = {
    new Mono[Unit](
      JMono.empty(source)
        .map(new Function[Void, Unit] {
          override def apply(t: Void): Unit = ()
        })
    )
  }

  def error[T](throwable: Throwable): Mono[T] = {
    new Mono[T](
      JMono.error(throwable)
    )
  }
}
