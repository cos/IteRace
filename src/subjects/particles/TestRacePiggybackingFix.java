package particles;

import extra166y.Ops;
import extra166y.ParallelArray;

public class TestRacePiggybackingFix {

	public ParallelArray<Particle> createParticleArray() {
		return ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
	}

	public void testCrossWithMain() {
		final Particle shared = new Particle();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						bar(shared);
						Particle nonShared = bar(new Particle());
						nonShared.x = 8;
						return null;
					}
				});
	}

	public void testCrossWithTheOtherThread() {
		final Particle shared = new Particle();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.origin = new Particle();
						bar(shared.origin);
						Particle nonShared = bar(new Particle());
						nonShared.x = 8;
						return null;
					}
				});
	}

	public Particle bar(Particle x) {
		return x;
	}
}