package projet;

import java.util.ArrayList;

import LDP.Activite;
import LDP.LDPManipulation;
import LDP.Processus;
import LDPparallel.Fourche;
import LDPparallel.LDPparallelFactory;
import LDPparallel.Porte;
import LDPparallel.Sequence;

public class Transformation {
	
	public void transfo(String filename) {
		// RESTE A AJOUTER DEBUT ET FIN EN PARCOURANT ET COMPARANT NOMS ACTIVITES
		
		System.out.println("");
		System.out.println("");
		System.out.println("--- TRANSFORMATION DU MODELE STATIQUE EN PARALLELE ---");
		System.out.println("");
		System.out.println("");
		
		LDPManipulation manip = new LDPManipulation();
		Processus p = manip.getProcessusInModel(filename);
		LDPparallel.Processus processusPara = LDPparallelFactory.eINSTANCE.createProcessus();
		LDPparallel.Debut debutPara = LDPparallelFactory.eINSTANCE.createDebut();
		LDPparallel.Fin finPara = LDPparallelFactory.eINSTANCE.createFin();
		
		String nomDebut = null;
		String nomFin = null;
		
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
			
			
			
			
			
			// Si elle n'a qu'un paramètre provenant d'opérations
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
			
			
			// Si une activité a son return utilisé dans plus d'une activité en tant que paramètre 
			// Alors on recupere dans un tableau toutes les activités qui ont en paramètre, le returnTag de notre activité
			// Puis on parcourt ce tableau et si une activité a son returnTag utilisé en paramètre par au moins une autre du tableau créé, on incrémente un compteur
			// Si le compteur est inférieur à la taille du tableau alors il y a une fourche après cette activité
			if(nbReturnInParam > 1) {
				ArrayList<Activite> activitesUtiliseReturn = new ArrayList<Activite>();
				for(Activite activite2 : p.getActivites()) {
					for(String param : activite2.getAction().getParamsTag()) {
						if(activite.getAction().getReturnTag().equals(param)) {
							activitesUtiliseReturn.add(activite2);
						}
					}
				}
				int i = 1;
				for(Activite activite2 : activitesUtiliseReturn) {
					for(String param : activite2.getAction().getParamsTag()) {
						for(Activite activite3 : activitesUtiliseReturn) {
							if(activite3.getAction().getReturnTag().equals(param)) {
								i ++;
								break;
							}
						}
					}
				}
				
				if(i < activitesUtiliseReturn.size()) {
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
			}
			
			if(p.getDebut().getReference() == activite) {
				nomDebut = activite.getDescription();
			}
			
			if(p.getFin().getReference() == activite) {
				nomFin = activite.getDescription();
			}
		}
		
		for(Sequence sequence : processusPara.getSequences()) {
			for(LDPparallel.Activite activite : sequence.getActivites()) {
				if(activite.getDescription().equals(nomDebut)) {
					debutPara.setReference(sequence);
					processusPara.setDebut(debutPara);
				}
				if(activite.getDescription().equals(nomFin)) {
					finPara.setReference(sequence);
					processusPara.setFin(finPara);
				}
			}
		}
		manip.sauverModele("model/CalculPara.xmi", processusPara);
	}
}
