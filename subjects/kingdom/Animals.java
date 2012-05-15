package kingdom;

public class Animals {
	
	private Human someOwner = new Human();
	public void live(){
		rexIsRex(null);
		rexIsNotLaica(null);
		rexsFatherIsAlwaysTheSame(new Dog());
		Dog bla = new Dog();
		bla.owner = new Human();
		sometimesDogsGetSold(bla);
		bla = new Dog();
		bla.owner = someOwner;
		sometimesDogsGetSoldToTheSameOwner(bla);
	}
	
	public void rexIsRex(Dog rex) {
	}
	public void rexIsNotLaica(Dog rex) {
		Dog laica = new Dog();
	}
	public void rexsFatherIsAlwaysTheSame(Dog rex) {
		Dog cine1 = rex.father;
		Dog cine2 = rex.father;
	}
	public void sometimesDogsGetSold(Dog rex) {
		Human cine1 = rex.owner;
		rex.owner = new Human();
		Human cine2 = rex.owner;
	}
	public void sometimesDogsGetSoldToTheSameOwner(Dog rex) {
		Human cine1 = rex.owner;
		Human bla = someOwner;
		rex.owner = bla;
		Human cine2 = rex.owner;
	}
}
