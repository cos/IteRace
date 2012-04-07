package particles;

import java.util.concurrent.locks.ReentrantLock;

import extra166y.Ops;
import extra166y.ParallelArray;

public class ParticleWithLocks {
	public double xyz, y, m;
	ParticleWithLocks origin, origin1;

	public void vacuouslyNoRace() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.apply(new Ops.Procedure<Particle>() {
			@Override
			public void op(Particle b) {
			}
		});
	}
	
	public void noLocks() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				shared.xyz = 10;
				return new ParticleWithLocks();
			}
		});
	}
	
	public void oneSimpleLock() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				synchronized(x) {
					shared.xyz = 10;
					return new ParticleWithLocks();
				}
			}
		});
	}
	
	public void oneSimpleSafeLock() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				synchronized(shared) {
					shared.xyz = 10;
					return new ParticleWithLocks();
				}
			}
		});
	}
	
	public void anotherDumbLock() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				shared.xyz = 10;
				synchronized(x) {
					return new ParticleWithLocks();
				}
			}
		});
	}
	
	public void imbricatedLocks() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				Object y = new Object();
				
				synchronized(x) {
					synchronized(y) { }
					shared.xyz = 10;
				}
				return new ParticleWithLocks();
			}
		});
	}
	
	public void imbricatedTwoLocks() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				Object y = new Object();
				
				synchronized(x) {
					synchronized(y) {
						shared.xyz = 10;
					}
				}
				return new ParticleWithLocks();
			}
		});
	}
	
	public void throughMethodCall() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				Object y = new Object();
				
				synchronized(x) {
					theMethod(shared, y);
				}
				return new ParticleWithLocks();
			}

			private void theMethod(final ParticleWithLocks shared, Object y) {
				synchronized(y) {
					shared.xyz = 10;
				}
			}
		});
	}
	
	public void checkMeetOverAllValidPathsPositive() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				
				synchronized(x) {
					theMethod(shared, y);
					shared.xyz = 10;
				}
				theMethod(shared,y);
				return new ParticleWithLocks();
			}

			private void theMethod(final ParticleWithLocks shared, Object y) {
			}
		});
	}
	
	public void checkMeetOverAllValidPathsNegative() {
		ParallelArray<ParticleWithLocks> particles = ParallelArray.createUsingHandoff(new ParticleWithLocks[10],
				ParallelArray.defaultExecutor());

		final ParticleWithLocks shared = new ParticleWithLocks();

		particles.replaceWithGeneratedValue(new Ops.Generator<ParticleWithLocks>() {
			@Override
			public ParticleWithLocks op() {
				Object x = new Object();
				synchronized(x) {
					theMethod();
				}
				shared.xyz = 10;				
				anotherMethod();
				return new ParticleWithLocks();
			}

			private void theMethod() {
			}
			private void anotherMethod() {
			}
		});
	}

	public void lockUsingSynchronizedBlock() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {
					shared.x = 10;
				}
				return new Particle();
			}
		});
	}
	
	/**
	 * this checks whether the locks are propagated well through methods
	 */
	public void lockUsingSynchronizedBlockInAnotherMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {
					someMethod(shared);
				}
				return new Particle();
			}

			private void someMethod(final Particle shared) {
				shared.x = 10;
			}
		});
	}

	/**
	 * this checks whether the locks take into account the situation where the
	 * method is called from both a synched and unsynched place
	 */
	public void lockFromBothSynchronizedAndUnsynchronized() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				synchronized (this) {
					someMethod(shared);
				}
				someMethod(shared);
				return new Particle();
			}

			private void someMethod(final Particle shared) {
				shared.x = 10;
			}
		});
	}

	public void synchronizedMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public synchronized Particle op() {
				shared.x = 10;
				return new Particle();
			}
		});
	}

	public void synchronizedStaticMethod() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				raceBabyRace(shared);
				return new Particle();
			}
		});
	}

	private static synchronized void raceBabyRace(final Particle shared) {
		shared.x = 10;
	}

	public void reenterantLock() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ReentrantLock l = new ReentrantLock();
				l.lock();
				shared.x = 10;
				l.unlock();
				return new Particle();
			}
		});
	}
}