package io.dimitris.emf.binary;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.emc.emf.EmfUtil;
import org.eclipse.epsilon.profiling.Stopwatch;

public class BinaryResource extends ResourceImpl {
	
	protected HashMap<EObject, Integer> ids = new HashMap<EObject, Integer>();
	protected Map<URI, Resource> resources = new AutoCreateMap<URI, Resource>(Resource.class);
	
	public static void main(String[] args) throws Exception {
		
		ResourceSet resourceSet = new ResourceSetImpl();
		EmfUtil.register(URI.createFileURI("/Users/dkolovos/Projects/Eclipse/eclipse-modeling-luna/workspace/org.eclipse.epsilon.emf.binary/JDTAST.ecore"), resourceSet.getPackageRegistry());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		org.eclipse.emf.ecore.resource.Resource set0 = resourceSet.createResource(URI.createFileURI("/Users/dkolovos/Projects/Eclipse/eclipse-modeling-luna/workspace/org.eclipse.epsilon.emf.binary/set0.xmi"));
		set0.load(null);
		
		BinaryResourceImpl competition = new BinaryResourceImpl(URI.createFileURI("/Users/dkolovos/Projects/Eclipse/eclipse-modeling-luna/workspace/org.eclipse.epsilon.emf.binary/set0.bin"));
		competition.getContents().add(set0.getContents().get(0));
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();
		competition.save(null);
		stopwatch.pause();
		System.out.println(stopwatch.getElapsed());
		
		BinaryResource resource = new BinaryResource(URI.createFileURI("/Users/dkolovos/Projects/Eclipse/eclipse-modeling-luna/workspace/org.eclipse.epsilon.emf.binary/set0.ebin"));
		resource.getContents().add(competition.getContents().get(0));
		stopwatch.resume();
		resource.save(null);
		System.out.println(stopwatch.getElapsed());
	}
	
	public BinaryResource(URI uri) {
		super(uri);
	}
	
	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options)
			throws IOException {
		
		outputStream = new BufferedOutputStream(outputStream, 2048);
		
		// Populate ID map
		TreeIterator<EObject> iterator = this.getAllContents();
		int id = 1;
		while (iterator.hasNext()) {
			ids.put(iterator.next(), id++);
		}
		
		// Reset iterator
		iterator = this.getAllContents();
		
		while (iterator.hasNext()) {
			EObject eObject = iterator.next();
			EClass eClass = eObject.eClass();
			resources.get(this.getURI()).
				getNsUris().get(eClass.getEPackage().getNsURI()).
				getEClasses().get(eClass).add(eObject);
		}
		
		boolean firstResource = false;
		for (URI resourceUri : resources.keySet()) {
			Resource resource = resources.get(resourceUri);
			if (!firstResource) {
				writeString(outputStream, resourceUri.toString());
			}
			
			for (String uri : resource.getNsUris().keySet()) {
				NsURI nsUri = resource.getNsUris().get(uri);
				writeString(outputStream, uri);
				
				for (EClass eClass : nsUri.getEClasses().keySet()) {
					
					// Keep only the structural features worth persisting
					List<EStructuralFeature> eAllStructuralFeatures = new ArrayList<EStructuralFeature>();
					for (EStructuralFeature sf : eClass.getEAllStructuralFeatures()) {
						if (sf.isChangeable() && !sf.isTransient() && !sf.isVolatile()) {
							eAllStructuralFeatures.add(sf);
						}
					}
					
					List<EObject> eObjects = nsUri.getEClasses().get(eClass);
					outputStream.write(eObjects.size());
					
					for (EObject eObject : eObjects) {
						int featureId = 0;
						
						//System.out.println(EcoreUtil.getID(eObject));
						//writeString(outputStream, getURIFragment(eObject));
						for (EStructuralFeature sf: eAllStructuralFeatures) {
							featureId ++;
							if (!eObject.eIsSet(sf)) continue;
							
							writeInt(outputStream, featureId);
							Object value = eObject.eGet(sf);
							if (sf instanceof EAttribute) {
								if (sf.isMany()) {
									Collection<?> collection = (Collection<?>) value;
									writeInt(outputStream, collection.size());
									for (Object v : collection) {
										writeValue(outputStream, v);
									}
								}
								else {
									writeValue(outputStream, value);
								}
							}
							else {
								if (sf.isMany()) {
									Collection<?> collection = (Collection<?>) value;
									writeInt(outputStream, collection.size());
									for (Object o : collection) {
										EObject referencedEObject = (EObject) o;
										writeInt(outputStream, ids.get(referencedEObject));
									}
								}
								else {
									if (value != null) {
										writeInt(outputStream, ids.get((EObject) value));
									}
									else {
										writeInt(outputStream, 0);
									}
								}
							}
						}
					}
				}
				
			}
		}
		
		outputStream.flush();
		
		/*
		// Debug
		for (URI resourceUri : resources.keySet()) {
			Resource resource = resources.get(resourceUri);
			System.out.println(resourceUri.toString());
			System.out.println(resource);
			for (NsURI nsUri : resource.getNsUris().values()) {
				for (EClass eClass : nsUri.getEClasses().keySet()) {
					System.out.println(eClass.getName() + " " + nsUri.getEClasses().get(eClass).size());
				}
			}
		}*/
		
	}
	
	protected void writeValue(OutputStream outputStream, Object value) throws IOException {
		if (value instanceof String) {
			writeString(outputStream, value + "");
		}
		else if (value instanceof Integer) {
			writeInt(outputStream, (Integer) value); 
		}
		else if (value instanceof Boolean) {
			if ((Boolean) value) {
				writeInt(outputStream, Byte.parseByte("1"));
			}
			else {
				writeInt(outputStream, Byte.parseByte("0"));
			}
		}
	}
	
	protected void writeInt(OutputStream outputStream, int i) throws IOException {
		outputStream.write(i);
	}
	
	protected void writeString(OutputStream outputStream, String str) throws IOException {
		outputStream.write(str.length());
		outputStream.write(str.getBytes());
	}
	
}
