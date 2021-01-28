package LDPparallel;

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
import LDPparallel.*;
import LDPparallel.Debut;

public class LDPparallelManipulation {

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
			Resource resource = chargerModele(modelFile, LDPparallelPackage.eINSTANCE);
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
	
		
		public Object dynamicInvoke(String methodName, Object target, Object params[]) throws Exception {
			   Class cl = target.getClass();
			   Class[] paramsClass = new Class[params.length];
			   for (int i=0; i < params.length; i++)
			      paramsClass[i] = params[i].getClass();
			   Method met = cl.getMethod(methodName, paramsClass);
			   Object result = met.invoke(target, params);
			   return result;
		}
		
		/**
		 * Dans un processus, insère une nouvelle activité après une certaine activité que l'on 
		 * retrouvera à partir de son nom.
		 * @param proc le processus 
		 * @param activiteCherchee le nom de l'activité cherchée
		 * @param nouvelleActivite le nom de la nouvelle activité à rajouter
		 * @throws Exception 
		 */
		
		public void execute(String filename, Object target, HashMap tags) throws Exception{
			
			LDPparallelManipulation manip = new LDPparallelManipulation();
			Processus p = manip.getProcessusInModel(filename);
			
			
			// Début de la première activité
			Sequence premiereSequence = LDPparallelFactory.eINSTANCE.createSequence();
			premiereSequence = (Sequence) p.getDebut().getReference();
			
			premiereSequence.setActiviteCourante(premiereSequence.getPremiereActivite());
			
			while(premiereSequence.getActiviteCourante().getSuivante() != null) {
				
				// Création et remplissage d'un tableau contenant tous les paramètres
				ArrayList<Object> params = new ArrayList<>();	
				for(int j = 0; j < premiereSequence.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
					params.add(tags.get(premiereSequence.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
				}
				
				// Affichage de l'activité en cours d'exécution
				System.out.println(premiereSequence.getActiviteCourante().getDescription());
				
				
				// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
				Object result = dynamicInvoke(premiereSequence.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
				// On récupère le returnTag de l'activité en cours
				String returnTag = premiereSequence.getActiviteCourante().getAction().getReturnTag();
				// On place le résultat dans la hashmap tags
				tags.put(returnTag, result);
				
				// On affiche tous les résultats
				System.out.println(tags);
				
				premiereSequence.setActiviteCourante(premiereSequence.getActiviteCourante().getSuivante());
			}
			
			// Création et remplissage d'un tableau contenant tous les paramètres
			ArrayList<Object> params = new ArrayList<>();	
			for(int j = 0; j < premiereSequence.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
				params.add(tags.get(premiereSequence.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
			}
			
			// Affichage de l'activité en cours d'exécution
			System.out.println(premiereSequence.getActiviteCourante().getDescription());
			
			
			// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
			Object result = dynamicInvoke(premiereSequence.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
			// On récupère le returnTag de l'activité en cours
			String returnTag = premiereSequence.getActiviteCourante().getAction().getReturnTag();
			// On place le résultat dans la hashmap tags
			tags.put(returnTag, result);
			
			// On affiche tous les résultats
			System.out.println(tags);
			
			// Maintenant que la séquence de début est terminée, il faut parcourir les fourches pour identifier celle qui a la séquence actuelle comme prédécesseur
		
			p.getPortes();
			
			for(Porte porte : p.getPortes()) {
				if(porte.getClass() == Fourche.class) {
					Fourche fourche = (Fourche) porte;
					if(fourche.getPred() == premiereSequence) {
						for(ElementProcessus elementProcessus : fourche.getSucc() ) {
							Sequence sequence = (Sequence) elementProcessus;
							sequence.setActiviteCourante(sequence.getPremiereActivite());
						}
					}
				}
			}
			
			
			
		}
		
		
		public static void main(String argv[]) throws Exception {
			
			LDPparallelManipulation engine = new LDPparallelManipulation();
			HashMap<String, Object> tags = new HashMap<String, Object>();
			tags.put("n",6);
			tags.put("puiss",3);
			tags.put("x", 100);
			Calcul calc = new Calcul();
			engine.execute("model/Calcul.xmi", calc, tags);
			
			
		}
}