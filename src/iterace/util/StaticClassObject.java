package iterace.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.types.TypeReference;

public class StaticClassObject implements InstanceKey {
	
	private final TypeReference klass;

	public StaticClassObject(TypeReference klass) {
		this.klass = klass;
	}

	@Override
	public IClass getConcreteType() {
		return null;
	}

	public TypeReference getKlass() {
		return klass;
	}
	
	@Override
	public String toString() {
		return "Class: "+klass;
	}
}
