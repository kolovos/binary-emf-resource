package io.dimitris.emf.binary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;

@SuppressWarnings("serial")
public class AutoCreateMap<K, V> extends LinkedHashMap<K, V>{

	public static void main(String[] args) {
		HashMap<EClass, List<EObject>> typeMap = new AutoCreateMap<EClass, List<EObject>>(ArrayList.class);
		System.out.println((List<EObject>)typeMap.get(EcoreFactory.eINSTANCE.createEClass()));
	}
	
	protected Class<?> valueClass = null;
	protected LinkedHashMap<K, V> cache = new LinkedHashMap<K, V>();
	
	public AutoCreateMap(Class<?> valueClass) {
		this.valueClass = valueClass;
	}
	
	@Override
	public V get(Object key) {
		V v = super.get(key);
		if (v == null) {
			try {
				v = (V) valueClass.newInstance();
				put((K) key, v);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return v;
	}
	
}
