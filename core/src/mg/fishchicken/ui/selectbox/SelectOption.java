package mg.fishchicken.ui.selectbox;

import mg.fishchicken.core.util.CoreUtil;

import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;

/**
 * Simple option class to be used in combination with the {@link SelectBox}
 * 
 * Provides a name - value pair.
 * 
 * @author ANNUN
 *
 */
public class SelectOption<T> {
	
	public String name;
	public T value;
	
	public SelectOption(String name, T value) {
		this.name = name;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SelectOption<?>) {
			return CoreUtil.equals(name, ((SelectOption<?>) obj).name)
					&& CoreUtil.equals(value, ((SelectOption<?>) obj).value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (name+value).hashCode();
	}

}
