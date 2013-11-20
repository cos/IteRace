package particles;

import extra166y.Ops;
import extra166y.ParallelArray;

public class TestThreadLocal {
	public ParallelArray<Particle> createParticleArray() {
		return ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
	}

	public void testNoRaceOnThreadLocalSet() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.set(new Particle());
						return null;
					}
				});
	}

	public void testNoRaceOnThreadLocalGet() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();
		shared.set(new Particle());

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.get();
						return null;
					}
				});
	}

	public void testNoRaceOnThreadLocalSetGet() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();
		shared.set(new Particle());

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.set(new Particle());
						shared.get();
						return null;
					}
				});
	}

	public void testNoRaceOnThreadLocalObject() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						Particle particle = new Particle();
						shared.set(particle);
						particle.x = 7;
						return null;
					}
				});
	}
	
	public void testNoRaceOnThreadLocalRetrivedObject() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.set(new Particle());
						shared.get().x = 7;
						return null;
					}
				});
	}
	
	public void testRaceOnThreadLocalRetrivedObject() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();
		final Particle sharedParticle = new Particle();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.set(sharedParticle);
						shared.get().x = 7;
						return null;
					}
				});
	}
	
	public void testNoRaceOnObjectSetOutsideTheLoop() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();
		shared.set(new Particle());

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.get().x = 7;
						return null;
					}
				});
	}
	
	public void testRaceOnThreadLocalReferredSharedObject() {
		final ThreadLocal<Particle> shared = new ThreadLocal<Particle>();
		final Particle sharedParticle = new Particle();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						Particle local = new Particle();
						local.origin = sharedParticle;
						shared.set(local);
						shared.get().origin.x = 7; // should race
						shared.get().x = 9; // should not race
						return null;
					}
				});
	}
}
