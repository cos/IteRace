package kingdom;

public class Dog {
	
	public Dog father;
	public Human owner;
	
	public void doesNothing() {
		
	}
	int age = 0;
	public void age() {
		this.age += 1;
	}
	public void ageAlias() {
		this.age();
	}
	public void ageage() {
		this.age();
	}
	int danger = 0;
	public void bark() {
		danger--;
		if(danger > 0)
			bark();
	}
}