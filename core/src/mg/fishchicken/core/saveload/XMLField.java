package mg.fishchicken.core.saveload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import mg.fishchicken.core.util.XMLUtil;

/**
 * Marks the class field as readable from XML.
 * 
 * Fields annotated with this annotation will be read correctly
 * from XML by the {@link XMLUtil#readPrimitiveMembers(Object, com.badlogic.gdx.utils.XmlReader.Element)}
 * method. 
 * @author ANNUN
 * @see XMLUtil#readPrimitiveMembers(Object, com.badlogic.gdx.utils.XmlReader.Element)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XMLField {
	String fieldPath() default "";
}
