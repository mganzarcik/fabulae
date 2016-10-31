package mg.fishchicken.core.util;


public class Pair<L, R> {
	
	private L left;
	private R right;
	
	public Pair() {
		this(null, null);
	}
	
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public R getRight() {
		return right;
	}
	
	public L getLeft() {
		return left;
	}
	
	public void setLeft(L left) {
		this.left = left;
	}
	
	public void setRight(R right) {
		this.right = right;
	}
	
	@Override
	public int hashCode() {
		return (getRight() == null ? 0 : getRight().hashCode())
				^ (getLeft() == null ? 0 : getLeft().hashCode());
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Pair<?, ?>) {
			return equals((Pair<?,?>)obj);
		}
		return false;
	}
	
	public boolean equals(Pair<?, ?> tuple) {
		return CoreUtil.equals(this.right, tuple.getRight()) && CoreUtil.equals(this.left, tuple.getLeft());
	}

}
