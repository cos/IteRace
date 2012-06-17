package particles;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extra166y.Ops;
import extra166y.ParallelArray;

public class ParticleUsingLibrary {
	
	public void noRaceWhenPrintln() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				System.out.println("bla");
				return new Particle();
			}
		});
	}
	
	public void noRaceOnPattern() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Pattern pattern = Pattern.compile(".*");
				pattern.matcher("testtest");
				return new Particle();
			}
		});
	}
	
	public void noRaceOnSafeMatcher() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Pattern pattern = Pattern.compile(".*");
				Matcher matcher = pattern.matcher("testtest");
				matcher.matches();
				return new Particle();
			}
		});
	}
	
	public void raceOnUnsafeMatcher() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		Pattern pattern = Pattern.compile(".*");
		final Matcher matcher = pattern.matcher("testtest");
		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				matcher.matches();
				return new Particle();
			}
		});
	}
	
	public void noRaceOnStringConcatenation() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());

		particles.replaceWithGeneratedValue(new Ops.Generator<Particle>() {
			@Override
			public Particle op() {
				Particle p = new Particle();
				@SuppressWarnings("unused")
				String bla = "tralala" + p;
				bla += "";
				return p;
			}
		});
	}
	
	
	public void raceOnArrayList() {
		ParallelArray<Particle> particles = ParallelArray.createUsingHandoff(new Particle[10],
				ParallelArray.defaultExecutor());
		
		final ArrayList<Integer> someList = new ArrayList<Integer>();

		particles.replaceWithMappedIndex(new Ops.IntToObject<Particle>() {
			@Override
			public Particle op(int i) {
				someList.add(i);
				return new Particle();
			}
		});
	}
}