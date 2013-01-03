package particles;

import extra166y.Ops;
import extra166y.ParallelArray;

public class TestRacePiggybackingFix {

	public ParallelArray<Particle> createParticleArray() {
		return ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
	}

	public void testCross() {
		final Particle shared = new Particle();

		createParticleArray().replaceWithGeneratedValue(
				new Ops.Generator<Particle>() {
					@Override
					public Particle op() {
						shared.x = 7;
						bar(shared);
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