package com.badlogic.gdx.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.RefCountedContainer;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * An asset manager that holds all of its assets in an OrderedMap instead
 * of a simple ObjectMap.
 * 
 * This means calling {@link #getAll(Class, com.badlogic.gdx.utils.Array)} will always return
 * loaded assets in the same order.
 * 
 * Located in the badlogic package due to package visibility in the parent class.
 * 
 * @author ANNUN
 *
 */
public class OrderedAssetManager extends AssetManager {
	
	public OrderedAssetManager(FileHandleResolver resolver) {
		super(resolver);
	}
	
	@Override
	protected <T> void addAsset(String fileName, Class<T> type, T asset) {
		super.addAsset(fileName, type, asset);
		
		// this is VERY ugly, but a bit more future proof in case of changes in the parent
		// than just copying the parent method
		ObjectMap<String, RefCountedContainer> typeToAssets = assets.get(type);
		
		if (!(typeToAssets instanceof OrderedMap)) {
			RefCountedContainer value = typeToAssets.get(fileName);
			typeToAssets = new OrderedMap<String, RefCountedContainer>();
			typeToAssets.put(fileName, value);
			assets.put(type, typeToAssets);
		}
	}
	
}
