package io.dimitris.emf.binary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class NsURI {
		protected String value;
		protected Map<EClass, List<EObject>> eClasses = new AutoCreateMap<EClass, List<EObject>>(ArrayList.class);
	
		public Map<EClass, List<EObject>> getEClasses() {
			return eClasses;
		}
		
	}