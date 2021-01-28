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
			
			
			// Vérification : les paramètres de l'opération sont présents dans tags
			if(checkDependances(p.getDebut().getReference(), tags)) {
				// Début de la première activité
				p.setActiviteCourante(p.getDebut().getReference());
			}
			else {
				return;
			}
			
			// Boucle pour dérouler toutes les activités jusqu'à la dernière
			do {
				
				
				// Création et remplissage d'un tableau contenant tous les paramètres
				ArrayList<Object> params = new ArrayList<>();	
				for(int j = 0; j < p.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
					params.add(tags.get(p.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
				}
				
				// Affichage de l'activité en cours d'exécution
				System.out.println(p.getActiviteCourante().getDescription());
				
				
				// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
				Object result = dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
				// On récupère le returnTag de l'activité en cours
				String returnTag = p.getActiviteCourante().getAction().getReturnTag();
				// On place le résultat dans la hashmap tags
				tags.put(returnTag, result);
				
				// On affiche tous les résultats
				System.out.println(tags);
				
				
				// Vérification : les paramètres de l'opération sont présents dans tags
				if(checkDependances(p.getActiviteCourante().getSuivante(), tags)) {
					// On passe à l'activité suivante
					p.setActiviteCourante(p.getActiviteCourante().getSuivante());
				}
				else {
					return;
				}
			} while (p.getActiviteCourante().getSuivante() != null);
			
			// On refait la même chose que dans le while pour traiter le dernier élément
			
			// Création et remplissage d'un tableau contenant tous les paramètres
			ArrayList<Object> params = new ArrayList<>();	
			for(int j = 0; j < p.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
				params.add(tags.get(p.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
			}
			
			// Affichage de l'activité en cours d'exécution
			System.out.println(p.getActiviteCourante().getDescription());
			
			
			// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
			Object result = dynamicInvoke(p.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
			// On récupère le returnTag de l'activité en cours
			String returnTag = p.getActiviteCourante().getAction().getReturnTag();
			// On place le résultat dans la hashmap tags
			tags.put(returnTag, result);
			
			// On affiche tous les résultats
			System.out.println(tags);
		}
		
		
		// Paramètres : 
			// activite : Activité à traiter
			// tags : HashMap de tous les tags issues des opérations déjà réalisées
		// Retourne un booléen :
			// true : si tous les paramètres de l'activité à effectuer sont présents et non nuls dans tags (donc si les opérations précédentes nécessaires à l'exécution ont été effectuées)
			// false : sinon
		public boolean checkDependances(Activite activite, HashMap<String, Object> tags) {
			int i=0;
			int nbParam = activite.getAction().getParamsTag().size();
			for(String param : activite.getAction().getParamsTag()) {
				if(tags.get(param) != null) {
					i++;
				}
			}
			return i==nbParam;
		}
		
		public void transfo(String filename) {
			// RESTE A AJOUTER DEBUT ET FIN EN PARCOURANT ET COMPARANT NOMS ACTIVITES
			
			LDPManipulation manip = new LDPManipulation();
			Processus p = manip.getProcessusInModel(filename);
			LDPparallel.Processus processusPara = LDPparallelFactory.eINSTANCE.createProcessus();
			LDPparallel.Debut debutPara = LDPparallelFactory.eINSTANCE.createDebut();
			LDPparallel.Fin finPara = LDPparallelFactory.eINSTANCE.createFin();
			
			for(Activite activite : p.getActivites()) {
				
				// On recrée l'activité en LDPparallel
				LDPparallel.Activite activitePara = LDPparallelFactory.eINSTANCE.createActivite();
				
				// On recrée l'opération de l'activité
				LDPparallel.Operation opePara = LDPparallelFactory.eINSTANCE.createOperation();
				opePara.setMethodName(activite.getAction().getMethodName());
				opePara.setReturnTag(activite.getAction().getReturnTag());
				for(String param : activite.getAction().getParamsTag()) {
					opePara.getParamsTag().add(param);
				}
				
				// On ajoute l'opération à l'activité créee
				activitePara.setAction(opePara);
				// On ajoute la description à l'activité créee
				activitePara.setDescription(activite.getDescription());
				

				
				// Calcul du nombre de paramètres provenant d'opérations pour une activité
				int nbParamFromReturn = 0;
				
				System.out.println(activite.getDescription());
				
				for(String param : activite.getAction().getParamsTag()) {
					for(Activite activite2 : p.getActivites()) {
						if(param.equals(activite2.getAction().getReturnTag())) {
							nbParamFromReturn ++;
						}
					}
				}
				System.out.println("nbParamFromReturn : " + nbParamFromReturn);
				
				// Calcul du nombre de paramètres contenant le return
				int nbReturnInParam = 0;
				for(Activite activite2 : p.getActivites()) {
					for(String param : activite2.getAction().getParamsTag()) {
						if(activite.getAction().getReturnTag().equals(param)) {
							nbReturnInParam ++;
						}
					}
				}
				System.out.println("nbReturnInParam : " + nbReturnInParam);
				
				// Calcul du nombre de fois où les paramètres sont aussi paramètres d'autres activités
				int nbParamsInParams = 0;
				for(Activite activite2 : p.getActivites()) {
					for(String param : activite.getAction().getParamsTag()) {
						for(String param2 : activite2.getAction().getParamsTag()) {
							if(param.equals(param2) && activite.getDescription() != activite2.getDescription()) {
								nbParamsInParams ++;
							}
						}
					}
				}
				System.out.println("nbParamsInParams : " + nbParamsInParams);
				
				// Calcul du nombre de fois où les paramètres sont aussi paramètres d'autres activités dans les activités déjà créées dans le nouveau processus
				int nbParamsInParamsNewProc = 0;
				for(Sequence sequence : processusPara.getSequences()) {
					for(LDPparallel.Activite activite2 : sequence.getActivites()) {
						for(String param : activite.getAction().getParamsTag()) {
							for(String param2 : activite2.getAction().getParamsTag()) {
								if(param.equals(param2)) {
									nbParamsInParamsNewProc ++;
								}
							}
						}
					}
				}
				System.out.println("nbParamsInParamsNewProc : " + nbParamsInParamsNewProc);
				System.out.println("");
				
				
				// Si une activité a plus d'un paramètre provenant d'opérations :
				// Alors : 
					// Si elle a déjà au moins un paramètre qui est aussi parametre dans d'autres activités déjà créées dans le nouveau processus
						// Alors : On ajoute notre activité à la suite d'une activité qui a comme returnTag un des paramTags de notre activité
					// Sinon on commence une nouvelle section précédée d'une jonction dans laquelle on met notre activité
				if(nbParamFromReturn > 1) {
					if(nbParamsInParamsNewProc >= 1) {
						Sequence sequenceTemp = null;
						for(Sequence sequence : processusPara.getSequences()) {
							for(LDPparallel.Activite activite2 : sequence.getActivites()) {
								for(String paramTag : activitePara.getAction().getParamsTag()) {
									if(paramTag.equals(activite2.getAction().getReturnTag())) {
										sequenceTemp = sequence;
										activitePara.setPrecedente(activite2);
										activite2.setSuivante(activitePara);
									}
								}
							}
						}
						sequenceTemp.getActivites().add(activitePara);
					}
					else {
						// Création de la nouvelle séquence
						LDPparallel.Sequence sequencePara = LDPparallelFactory.eINSTANCE.createSequence();
						sequencePara.getActivites().add(activitePara);
						
						// On ajoute la première activité dans la séquence
						sequencePara.setPremiereActivite(activitePara);
						
						// Création de la jonction
						LDPparallel.Jonction jonctionPara = LDPparallelFactory.eINSTANCE.createJonction();
						
						// On ajoute la séquence créée en successeur à la jonction créée
						jonctionPara.setSucc(sequencePara);
						
						// On parcourt toutes les activités ainsi que tous les paramètres de notre activité
						for(Activite activite2 : p.getActivites()) {
							for(String param : activitePara.getAction().getParamsTag()) {
								
								// Si une activité possède en returnTag, un paramTag de notre activité alors on entre dans le if
								if(activite2.getAction().getReturnTag().equals(param)) {
									
									// On parcourt toutes les séquences et toutes les activités du nouveau processus afin de chercher l'activité correspondante
									for(Sequence sequence : processusPara.getSequences()) {
										for(LDPparallel.Activite activitePara2 : sequence.getActivites()) {
											
											// Si une activité a la même description que celle que celle que l'on a identifiée comme ayant le paramTag en returnTag
											// Alors on ajoute sa séquence en tant que prédécesseur de la jonction
											if(activitePara2.getDescription().equals(activite2.getDescription())) {
												jonctionPara.getPred().add(sequence);
											}
										}
									}
								}
							}
						}
						// On ajoute la séquence et la jonction au processus
						processusPara.getSequences().add(sequencePara);
						processusPara.getPortes().add(jonctionPara);
					}
					
				}
				
				
				
				
				
				// Si elle n'a qu'un paramètre provenant d'opérations mais plusieurs paramètres
				// Alors plusieurs cas sont possibles :
				// Si l'activité a : 
					// au moins un paramètre qui est aussi paramètre d'autres activités dans les activités déjà créées dans le nouveau processus ET que le nombre de fois où ces paramètres sont aussi paramètres d'autres activités est égal au nombre de ses paramètres qui sont aussi des return
					// OU aucun paramètre étant paramètres d'autres activités
				// Alors :  
					// On ne crée pas de nouvelle séquence mais on la met à la suite d'une activité déjà créée dans une séquence déjà créée
					//  Sauf si cette séquence est succédée d'une fourche auquel cas on crée une nouvelle séquence dans laquelle on la place 
					//   ET qu'on ajoute comme successeur de la fourche si cette fourche a, dans la séquence qui la précède, une activité ayant un returnTag correspondant à un des paramètres de notre activité
					
				// Sinon : on crée une nouvelle séquence dans laquelle on la place 
				//  ET qu'on ajoute comme successeur de la fourche si cette fourche a, dans la séquence qui la précède, une activité ayant un returnTag correspondant à un des paramètres de notre activité
				else if(nbParamFromReturn == 1){
					if((nbParamsInParamsNewProc >= 1 && nbParamsInParams == nbParamFromReturn) || nbParamsInParams == 0) {
						Sequence sequenceTemp = null;
						for(Sequence sequence : processusPara.getSequences()) {
							for(LDPparallel.Activite activite2 : sequence.getActivites()) {
								for(String paramTag : activitePara.getAction().getParamsTag()) {
									if(paramTag.equals(activite2.getAction().getReturnTag())) {
										sequenceTemp = sequence;
										activitePara.setPrecedente(activite2);
										activite2.setSuivante(activitePara);
									}
								}
							}
						}
						boolean ok = false;
						if(processusPara.getPortes().isEmpty()) {
							sequenceTemp.getActivites().add(activitePara);
							ok = true;
						}
						for(Porte porte : processusPara.getPortes()) {
							if(porte instanceof Fourche) {
								Fourche fourche = (Fourche) porte;
								if(fourche.getPred() != sequenceTemp) {
									sequenceTemp.getActivites().add(activitePara);
									ok = true;
								}
							}
						}
						if(!ok) {
							Sequence sequence = LDPparallelFactory.eINSTANCE.createSequence();
							sequence.setPremiereActivite(activitePara);
							sequence.getActivites().add(activitePara);
							processusPara.getSequences().add(sequence);
							for(Porte porte : processusPara.getPortes()) {
								if(porte instanceof Fourche) {
									Fourche fourche = (Fourche) porte;
									if(fourche.getPred() instanceof Sequence) {
										Sequence sequenceParaTemp = (Sequence) fourche.getPred();
										for(LDPparallel.Activite activiteTemp : sequenceParaTemp.getActivites()) {
											for(String param : activitePara.getAction().getParamsTag()){
												if(activiteTemp.getAction().getReturnTag().equals(param)) {
													fourche.getSucc().add(sequence);
												}
											}
										}
									}
								}
							}
						}
					}
					else {
						Sequence sequence = LDPparallelFactory.eINSTANCE.createSequence();
						sequence.setPremiereActivite(activitePara);
						sequence.getActivites().add(activitePara);
						processusPara.getSequences().add(sequence);
						if(nbParamsInParams >= 1) {
							for(Porte porte : processusPara.getPortes()) {
								if(porte instanceof Fourche) {
									Fourche fourche = (Fourche) porte;
									if(fourche.getPred() instanceof Sequence) {
										Sequence sequenceParaTemp = (Sequence) fourche.getPred();
										for(LDPparallel.Activite activiteTemp : sequenceParaTemp.getActivites()) {
											for(String param : activitePara.getAction().getParamsTag()){
												if(activiteTemp.getAction().getReturnTag().equals(param)) {
													fourche.getSucc().add(sequence);
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
				// Si elle n'a pas de paramètre provenant d'opération, c'est que c'est une activité qui commence une séquence
				else if(nbParamFromReturn == 0) {
					LDPparallel.Sequence sequencePara = LDPparallelFactory.eINSTANCE.createSequence();
					sequencePara.getActivites().add(activitePara);
					sequencePara.setPremiereActivite(activitePara);
					processusPara.getSequences().add(sequencePara);
				}
				
				
				// Si une activité a son return utilisé dans plus d'une activité en tant que paramètre alors il y a une fourche après cette activité
				if(nbReturnInParam > 1) {
					for(Sequence sequence : processusPara.getSequences()) {
						for(LDPparallel.Activite activitePara2 : sequence.getActivites()) {
							if(activitePara == activitePara2) {	
								LDPparallel.Fourche fourche = LDPparallelFactory.eINSTANCE.createFourche();
								fourche.setPred(sequence);
								processusPara.getPortes().add(fourche);
							}
						}
					}
				}
				manip.sauverModele("model/CalculPara.xmi", processusPara);
			}
		}
		
		
		public static void main(String argv[]) throws Exception {
			
			LDPManipulation engine = new LDPManipulation();
			HashMap<String, Object> tags = new HashMap<String, Object>();
			tags.put("n",5);
			tags.put("n2",5);
			tags.put("n3", 4);
			tags.put("n4", 2);
			tags.put("puiss",3);
			tags.put("x", 100);
			Calcul calc = new Calcul();
			//engine.execute("model/newCalculSeqComplique.xmi", calc, tags);
			//engine.transfo("model/newCalculSeqComplique.xmi");
			//engine.transfo("model/newCalculSeq.xmi");
			engine.transfo("model/Calcul.xmi");
			
		}
}