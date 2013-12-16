package extra166y;

import extra166y.Ops.Generator;
import extra166y.Ops.IntToObject;

public class ParallelArray<T> {
	T e0;
	T e1;
	private Object[] innerArray;

	public ParallelArray() {
	}

	public static jsr166y.ForkJoinPool defaultExecutor() {
		return null;
	}

	public static <T> ParallelArray<T> createUsingHandoff(T[] source,
			jsr166y.ForkJoinPool executor) {
		ParallelArray<T> parallelArray = new ParallelArray<T>();
		parallelArray.innerArray = source;
		return parallelArray;
	}

	public void apply(Ops.Procedure<? super T> procedure) {
		procedure.op(e0);
		procedure.op(e1);
	}

	public void applySeq(Ops.Procedure<? super T> procedure) {
		procedure.op(e0);
		procedure.op(e1);
	}
	
	public void applyWithIndexSeq(Ops.ProcedureWithIndex<? super T> procedure) {
		procedure.op(0, e0);
		procedure.op(1, e1);
	}

	@SuppressWarnings("unchecked")
	public void replaceWithGeneratedValue(Ops.Generator<? super T> generator) {
		// TODO: Is this a good simulation of parallel execution?
		e0 = (T) generator.op();
		e1 = (T) generator.op();

		innerArray[0] = e0;
		innerArray[1] = e1;
	}

	@SuppressWarnings("unchecked")
	public void replaceWithGeneratedValueSeq(Ops.Generator<? super T> generator) {
		e0 = (T) generator.op();
		e1 = (T) generator.op();

		innerArray[0] = e0;
		innerArray[1] = e1;
	}

	@SuppressWarnings("unchecked")
	public T[] getArray() {
		return (T[]) innerArray;
	}

	@SuppressWarnings("unchecked")
	public void replaceWithMappedIndex(IntToObject<? super T> intToObject) {
		e0 = (T) intToObject.op(0);
		e1 = (T) intToObject.op(1);

		innerArray[0] = e0;
		innerArray[1] = e1;
	}

	@SuppressWarnings("unchecked")
	public void replaceWithMappedIndexSeq(IntToObject<? super T> intToObject) {
		e0 = (T) intToObject.op(0);
		e1 = (T) intToObject.op(1);

		innerArray[0] = e0;
		innerArray[1] = e1;
	}

	public <W, V> void replaceWithMapping(
			Ops.BinaryOp<? super T, ? super V, ? extends T> combiner,
			ParallelArrayWithMapping<W, V> other) {
		e0 = (T) combiner.op(e0, other.e0);
		e1 = (T) combiner.op(e1, other.e1);

		innerArray[0] = e0;
		innerArray[1] = e1;
	}

	public void generate(Generator<?> generator) {
		// does not do anything. it is here for the purpose of the example
	}

	public Object[] toArray() {
		return new Object[] { e0, e1 };
	}

	public int size() {
		return 2;
	}
}
