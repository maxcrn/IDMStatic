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
			
			LDPManipulation manip = new LDPManipulation();
			Processus p = manip.getProcessusInModel(filename);
			
			p.setActiviteCourante(p.getDebut().getReference());
			
			do {
				ArrayList<Object> params = new ArrayList<>();
				ArrayList<Object> paramsName = new ArrayList<>();
				
				for(int j = 0; j < p.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
					params.add(tags.get(p.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
				}
				
				
				//tags.put(params.toArray(), dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray()));
				
				System.out.println(p.getActiviteCourante().getDescription());
				
				
				// dynamicInvoke renvoie le resultat de l'opération effectuée
				Object result = dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
				String returnTag = p.getActiviteCourante().getAction().getReturnTag();
				tags.put(returnTag, result);
				
				System.out.println(tags);
				
				// On passe à l'activité suivante
				p.setActiviteCourante(p.getActiviteCourante().getSuivante());
			} while (p.getActiviteCourante().getSuivante() != null);
			
			ArrayList<Object> params = new ArrayList<>();
			ArrayList<Object> paramsName = new ArrayList<>();
			
			for(int j = 0; j < p.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
				params.add(tags.get(p.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
			}
			
			
			//tags.put(params.toArray(), dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray()));
			
			System.out.println(p.getActiviteCourante().getDescription());
			
			
			// dynamicInvoke renvoie le resultat de l'opération effectuée
			Object result = dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
			String returnTag = p.getActiviteCourante().getAction().getReturnTag();
			tags.put(returnTag, result);
			
			System.out.println(tags);
			
				
		}
		
		public static void main(String argv[]) throws Exception {
			
			LDPManipulation engine = new LDPManipulation();
			HashMap<String, Object> tags = new HashMap<String, Object>();
			tags.put("n",6);
			tags.put("puiss",3);
			tags.put("x", 100);
			Calcul calc = new Calcul();
			engine.execute("model/Calcul.xmi", calc, tags);
		}
}