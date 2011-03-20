package siena.core.async;


public class SienaFutureMock<T> implements SienaFuture<T>{
	T value;
	
	public SienaFutureMock(T value){
		this.value = value;
	}
	
	public T get() {
		return value;
	}

}
