package projet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;


import org.eclipse.emf.common.util.EList;

import LDPparallel.*;

public class EngineParallele {
	
	public void execute(String filename, Object target, HashMap tags) throws Exception{
		
		System.out.println("");
		System.out.println("");
		System.out.println("--- EXECUTION DU MODELE PARALLELE ---");
		System.out.println("");
		System.out.println("");
		
		LDPparallelManipulation manip = new LDPparallelManipulation();
		Processus p = manip.getProcessusInModel(filename);
		
		boolean premiereDone = false;
		ArrayList<Sequence> sequencesDejaExecutees = new ArrayList<Sequence>();
		
		
		do {
			boolean ok = false;
			// Cas de la première séquence
			Sequence premiereSequence = (Sequence) p.getDebut().getReference();
			if(!premiereDone) {
				p.getSequencesActives().add(premiereSequence);
				premiereSequence.setActiviteCourante(premiereSequence.getPremiereActivite());
				premiereDone = true;
			}
			
			// Cas des séquence suivantes
			// La FOURCHE :
			else {
				if(!ok) {
					for(Porte porte : p.getPortes()) {
						if(porte instanceof Fourche) {
							Fourche fourche = (Fourche) porte;
							if(p.getSequencesActives().contains(fourche.getPred())) {
								p.getSequencesActives().clear();
								for(ElementProcessus elementProcessus : fourche.getSucc() ) {
									Sequence sequence2 = (Sequence) elementProcessus;
									if(!sequencesDejaExecutees.contains(sequence2)) {
										sequence2.setActiviteCourante(sequence2.getPremiereActivite());
										p.getSequencesActives().add(sequence2);
										sequencesDejaExecutees.add(sequence2);
										ok = true;
									}
								}
							}
						}
					}
				}
				
				
				// La JONCTION : 
				if(!ok) {
					for(Porte porte : p.getPortes()) {
						if(porte instanceof Jonction) {
							Jonction jonction = (Jonction) porte;
							int i = 0;
							for(Sequence sequence2 : p.getSequencesActives()) {
								for(ElementProcessus elementProcessus : jonction.getPred()) {
									if(sequence2 == (Sequence) elementProcessus) {
										i++;
									}
								}
							}
							if(i == p.getSequencesActives().size()) {
								p.getSequencesActives().clear();
								Sequence sequence2 = (Sequence)jonction.getSucc();
								if(!sequencesDejaExecutees.contains(sequence2)){
									sequence2.setActiviteCourante(sequence2.getPremiereActivite());
									p.getSequencesActives().add(sequence2);
									sequencesDejaExecutees.add(sequence2);
									ok=true;
								}
							}
						}
					}
				}
			}
			
			
			
			for(Sequence sequence : p.getSequencesActives()) {
				for(Activite activite : sequence.getActivites()) {
					System.out.println(activite.getDescription());
				}
			}
			System.out.println(p.getSequencesActives());
			RunnablePara paraRunnable = null;
			Thread[] threads = new Thread[p.getSequencesActives().size()];
			
			int i = 0;
			for(Sequence sequence : p.getSequencesActives()) {
				paraRunnable = new RunnablePara(sequence, target, tags);
				threads[i] = new Thread(paraRunnable, "thread1");
				threads[i].start();
				i++;
			}
			
			for(int j = 0; j < i; j++) {
				threads[j].join();
			}
			
			tags.putAll(paraRunnable.getTags());
			
			System.out.println(tags);
			
		}while(!p.getSequencesActives().contains((Sequence)p.getFin().getReference()));
	}
		
		
}
