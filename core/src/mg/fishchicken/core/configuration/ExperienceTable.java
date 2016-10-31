package mg.fishchicken.core.configuration;

import java.io.IOException;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StreamUtils;
import com.opencsv.CSVReader;

public class ExperienceTable {
	
	private ObjectMap<Integer, Integer> requiredExpGain;
	private ObjectMap<Integer, Integer> requiredExpTotal;
	
	public ExperienceTable(FileHandle file) {
		requiredExpGain = new ObjectMap<Integer, Integer>();
		requiredExpTotal = new ObjectMap<Integer, Integer>();
		CSVReader reader = new CSVReader(file.reader());
		try {
			String[] line = reader.readNext();
			int total = 0;
			for (int i = 0; i < line.length; ++i) {
				int currGain = Integer.parseInt(line[i]);
				requiredExpGain.put(i+2, currGain);
				total += currGain;
				requiredExpTotal.put(i+2, total);
			}
		} catch (IOException e) {
			throw new GdxRuntimeException(e);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}
	
	/**
	 * Returns the experience a character must gain in order to reach
	 * the supplied level.
	 *
	 * Will return -1 if the supplied level is not in the table. This means
	 * the level is beyond the level cap.
	 * 
	 * @param level
	 * @return
	 */
	public int getRequiredExperienceGainForLevel(int level) {
		if (!requiredExpGain.containsKey(level)) {
			return -1;
		}
		return requiredExpGain.get(level);
	}
	
	
	/**
	 * Returns the experience a character must have in total in order
	 * to reach the supplied level.
	 * 
	 * Will return -1 if the supplied level is not in the table. This means
	 * the level is beyond the level cap.
	 * 
	 * @param level
	 * @return
	 */
	public int getRequiredExperienceTotalForLevel(int level) {
		if (!requiredExpTotal.containsKey(level)) {
			return -1;
		}
		return requiredExpTotal.get(level);
	}
}
