package mg.fishchicken.ui.dialog;


public abstract class OkCancelCallback<T> {

	public void onOk(T result) {
		
	}
	
	public void onCancel() {
		
	}
	
	public void onError(String errorMessage) {
		
	};
}
