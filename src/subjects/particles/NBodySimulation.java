package particles;

import java.util.ArrayList;

import extra166y.Ops.Generator;
import extra166y.Ops.Procedure;
import extra166y.ParallelArray;

public class NBodySimulation {
	Particle centerOfMass;
	double dT = 1.0;
	protected Object lock = new Object();
	ArrayList<Particle> history = new ArrayList<Particle>();

	void compute() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(
				new Particle[10], ParallelArray.defaultExecutor());
		particles.replaceWithGeneratedValue(new Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = new Particle();
				return p;
			}
		});

		particles.apply(new Procedure<Particle>() {

			@Override
			public void op(Particle p) {
				p.vX += p.fX / p.m * dT;
				p.vY += p.fY / p.m * dT;
				p.x += p.vX * dT;
				p.y += p.vY * dT;

				Particle oldCOM = centerOfMass;
				centerOfMass = new Particle();
				synchronized (lock) {
					centerOfMass.m = oldCOM.m + p.m;
				}
				centerOfMass.x = (oldCOM.x * 100);
				centerOfMass.y = (oldCOM.y * 100);

				System.out.println(centerOfMass);
				history.add(centerOfMass);
			}
		});
	}
}
