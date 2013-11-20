package particles;

import extra166y.Ops;
import extra166y.ParallelArray;

/*
 * space
 * 
 * 
 * 
 * 
 */

public class ParticleWithKnownThreadSafe {

	public void simpleKnownThreadSafe() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ThreadSafe.raceInThreadSafe();
				return new Particle();
			}
		});
	}

	public void racePastKnownThreadSafe() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ThreadSafe.moveParticle(shared);
				return new Particle();
			}
		});
	}

	public void noRaceOnTransitiveClosureVerySimple() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ThreadSafeOnClosure.raceInThreadSafe();
				return new Particle();
			}
		});
	}
	
	public void noRaceOnTransitiveClosure() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		final Particle shared = new Particle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				ThreadSafeOnClosure.moveParticle(shared);
				return new Particle();
			}
		});
	}
	
	public void noRaceOnSafeObject() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		final Particle shared = ThreadSafeParticleGenerator.getSafeParticle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.moveTo(18, 21);
				return new Particle();
			}
		});
	}
	
	public void raceOnSafeObjectAccessedDirectly() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());

		final Particle shared = ThreadSafeParticleGenerator.getSafeParticle();

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				shared.x++;
				return new Particle();
			}
		});
	}
	
	
	static class ThreadSafeParticleGenerator {
		static public Particle getSafeParticle() {
			return new Particle();
		}
	}
	
	static class ThreadSafeOnClosure {
		static int noRaceOnMe = 10;

		synchronized static public void raceInThreadSafe() {
			noRaceOnMe = 11;
		}
		
		synchronized static public void moveParticle(Particle p) {
			noRaceOnMe = 13;
			p.moveTo(13, 17);
		}
	}

	static class ThreadSafe {
		static int noRaceOnMe = 10;

		synchronized static public void raceInThreadSafe() {
			noRaceOnMe = 11;
		}

		static public void moveParticle(Particle p) {
			synchronized (ThreadSafe.class) {
				noRaceOnMe = 13;
			}
			p.moveTo(13, 17);
		}
	}
}