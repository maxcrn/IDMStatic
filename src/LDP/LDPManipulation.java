package LDP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import java.lang.reflect.Method;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLMapImpl;

import projet.Calcul;
import LDP.*;
import LDPparallel.*;
import LDPparallel.Debut;

public class LDPManipulation {

	public void sauverModele(String uri, EObject root) {
		   Resource resource = null;
		   try {
		      URI uriUri = URI.createURI(uri);
		      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		      resource = (new ResourceSetImpl()).createResource(uriUri);
		      resource.getContents().add(root);
		      resource.save(null);
		   } catch (Exception e) {
		      System.err.println("ERREUR sauvegarde du modèle : "+e);
		      e.printStackTrace();
		   }
		}

		public Resource chargerModele(String uri, EPackage pack) {
		   Resource resource = null;
		   try {
		      URI uriUri = URI.createURI(uri);
		      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		      resource = (new ResourceSetImpl()).createResource(uriUri);
		      XMLResource.XMLMap xmlMap = new XMLMapImpl();
		      xmlMap.setNoNamespacePackage(pack);
		      java.util.Map options = new java.util.HashMap();
		      options.put(XMLResource.OPTION_XML_MAP, xmlMap);
		      resource.load(options);
		   }
		   catch(Exception e) {
		      System.err.println("ERREUR chargement du modèle : "+e);
		      e.printStackTrace();
		   }
		   return resource;
		}
		
		public Processus getProcessusInModel(String modelFile) {
			Resource resource = chargerModele(modelFile, LDPPackage.eINSTANCE);
			if (resource == null) {
				System.err.println(" Erreur de chargement du modèle");
				return null;
			}

			TreeIterator it = resource.getAllContents();

			Processus proc = null;
			while(it.hasNext()) {
			   EObject obj = (EObject) it.next();
			   if (obj instanceof Processus) {
			      proc = (Processus) obj;
			      break;
			   }
			}
			return proc;
		}
	
		
		/**
		 *  Affiche à l'écran la séquence ordonnée des activités d'un processus
		 *  @param proc le processus à afficher
		 */
		public void printProcessus(Processus proc) {
			
			System.out.println(proc.getActivites().get(0).getDescription());
			
			Activite a = (Activite) proc.getDebut().getReference();
			while(a.getSuivante() != null){
				System.out.print(a.getDescription() + " -> ");
				a = a.getSuivante();
			}
			System.out.println(a.getDescription());
	     
		}
}