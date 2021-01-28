package projet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.EList;

import LDPparallel.Sequence;

public class RunnablePara implements Runnable{
	
	HashMap tags;
	EList<Sequence> sequences;
	Object target;
	
	public RunnablePara(EList<Sequence> sequences, Object target, HashMap tags) {
		this.tags = tags;
		this.sequences = sequences;
		this.target = target;
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
	
	@Override
	public void run() {
		
		for(Sequence sequence : sequences) {
			while(sequence.getActiviteCourante().getSuivante() != null) {
				
				// Création et remplissage d'un tableau contenant tous les paramètres
				ArrayList<Object> params = new ArrayList<>();	
				for(int j = 0; j < sequence.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
					params.add(tags.get(sequence.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
				}
				
				// Affichage de l'activité en cours d'exécution
				//System.out.println(sequence.getActiviteCourante().getDescription());
				
				
				// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
				Object result = null;
				try {
					result = dynamicInvoke(sequence.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// On récupère le returnTag de l'activité en cours
				String returnTag = sequence.getActiviteCourante().getAction().getReturnTag();
				// On place le résultat dans la hashmap tags
				tags.put(returnTag, result);
				
				sequence.setActiviteCourante(sequence.getActiviteCourante().getSuivante());
			}
			
			// Création et remplissage d'un tableau contenant tous les paramètres
			ArrayList<Object> params = new ArrayList<>();	
			for(int j = 0; j < sequence.getActiviteCourante().getAction().getParamsTag().toArray().length; j++) {
				params.add(tags.get(sequence.getActiviteCourante().getAction().getParamsTag().toArray()[j]));
			}
			
			// Affichage de l'activité en cours d'exécution
			//System.out.println(sequence.getActiviteCourante().getDescription());
			
			
			// dynamicInvoke renvoie le resultat de l'opération effectuée dans result
			Object result = null;
			try {
				result = dynamicInvoke(sequence.getActiviteCourante().getAction().getMethodName(), target, params.toArray());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// On récupère le returnTag de l'activité en cours
			String returnTag = sequence.getActiviteCourante().getAction().getReturnTag();
			// On place le résultat dans la hashmap tags
			tags.put(returnTag, result);
			//System.out.println(tags);
		}
	}
	
	public HashMap getTags() {
		return tags;
	}


}
