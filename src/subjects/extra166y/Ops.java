package extra166y;

public class Ops {
	
	public interface ProcedureWithIndex<T> {
		void op(int i, T b);
	}
	
	public interface Procedure<T> {
		void op(T b);
	}

	public interface Generator<T> {
		T op();
	}

	public interface IntToObject<T> {
		T op(int i);
	}

	public interface BinaryOp<A, B, R> {
		R op(A a, B b);
	}
}
