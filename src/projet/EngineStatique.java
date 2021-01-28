package projet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import LDP.Activite;
import LDP.LDPManipulation;
import LDP.Processus;

public class EngineStatique {

	public void execute(String filename, Object target, HashMap tags) throws Exception{
		
		System.out.println("");
		System.out.println("");
		System.out.println("--- EXECUTION DU MODELE STATIQUE ---");
		System.out.println("");
		System.out.println("");
		
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
	
	
	public Object dynamicInvoke(String methodName, Object target, Object params[]) throws Exception {
		   Class cl = target.getClass();
		   Class[] paramsClass = new Class[params.length];
		   for (int i=0; i < params.length; i++)
		      paramsClass[i] = params[i].getClass();
		   Method met = cl.getMethod(methodName, paramsClass);
		   Object result = met.invoke(target, params);
		   return result;
	}
}
