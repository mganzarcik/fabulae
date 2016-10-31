package mg.fishchicken.ui.dialog;

public class ProgressDialogSettings {
	
	public float start, end;
	public boolean canCancel;
	public String header, text;
	
	public ProgressDialogSettings(String header, float end, boolean canCancel) {
		this(header, null, end, canCancel);
	}
	
	public ProgressDialogSettings(String header, String text, float end, boolean canCancel) {
		this.start = 0;
		this.end = end;
		this.canCancel = canCancel;
		this.header = header;
		this.text = text;
	}

}
