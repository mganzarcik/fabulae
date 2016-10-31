package mg.fishchicken.gamelogic.effects;

public class MissingParameterException extends Exception {

	private static final long serialVersionUID = 2916869058361602701L;
	private EffectParameter param;
	
	public MissingParameterException(EffectParameter param) {
		super("Parameter "+param.getName()+" must be specified.");
		this.param = param;
	}
	
	public EffectParameter getEffectParameter() {
		return param;
	}
}
