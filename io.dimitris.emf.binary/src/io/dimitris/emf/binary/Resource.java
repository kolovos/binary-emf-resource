package io.dimitris.emf.binary;

import java.util.Map;

public class Resource {
	protected Map<String, NsURI> nsUris = new AutoCreateMap<String, NsURI>(NsURI.class);
	
	public Map<String, NsURI> getNsUris() {
		return nsUris;
	}
}
