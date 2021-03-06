/**
 */
package LDPparallel;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Activite</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link LDPparallel.Activite#getDescription <em>Description</em>}</li>
 *   <li>{@link LDPparallel.Activite#getSuivante <em>Suivante</em>}</li>
 *   <li>{@link LDPparallel.Activite#getPrecedente <em>Precedente</em>}</li>
 *   <li>{@link LDPparallel.Activite#getAction <em>Action</em>}</li>
 * </ul>
 *
 * @see LDPparallel.LDPparallelPackage#getActivite()
 * @model annotation="http://www.eclipse.org/emf/2002/Ecore constraints='pasDeCycle'"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore/OCL/Pivot pasDeCycle='self.pasDansSuivant(self)'"
 * @generated
 */
public interface Activite extends EObject {
	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see LDPparallel.LDPparallelPackage#getActivite_Description()
	 * @model required="true"
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link LDPparallel.Activite#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Suivante</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link LDPparallel.Activite#getPrecedente <em>Precedente</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Suivante</em>' reference.
	 * @see #setSuivante(Activite)
	 * @see LDPparallel.LDPparallelPackage#getActivite_Suivante()
	 * @see LDPparallel.Activite#getPrecedente
	 * @model opposite="precedente"
	 * @generated
	 */
	Activite getSuivante();

	/**
	 * Sets the value of the '{@link LDPparallel.Activite#getSuivante <em>Suivante</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Suivante</em>' reference.
	 * @see #getSuivante()
	 * @generated
	 */
	void setSuivante(Activite value);

	/**
	 * Returns the value of the '<em><b>Precedente</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link LDPparallel.Activite#getSuivante <em>Suivante</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Precedente</em>' reference.
	 * @see #setPrecedente(Activite)
	 * @see LDPparallel.LDPparallelPackage#getActivite_Precedente()
	 * @see LDPparallel.Activite#getSuivante
	 * @model opposite="suivante"
	 * @generated
	 */
	Activite getPrecedente();

	/**
	 * Sets the value of the '{@link LDPparallel.Activite#getPrecedente <em>Precedente</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precedente</em>' reference.
	 * @see #getPrecedente()
	 * @generated
	 */
	void setPrecedente(Activite value);

	/**
	 * Returns the value of the '<em><b>Action</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Action</em>' containment reference.
	 * @see #setAction(Operation)
	 * @see LDPparallel.LDPparallelPackage#getActivite_Action()
	 * @model containment="true"
	 * @generated
	 */
	Operation getAction();

	/**
	 * Sets the value of the '{@link LDPparallel.Activite#getAction <em>Action</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Action</em>' containment reference.
	 * @see #getAction()
	 * @generated
	 */
	void setAction(Operation value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model required="true" actRequired="true"
	 *        annotation="http://www.eclipse.org/emf/2002/Ecore/OCL/Pivot body='if self.suivante.oclIsUndefined() then true \n\t\t\t      else not(self.suivante = act) and self.suivante.pasDansSuivant(act)\n\t\t\t      endif'"
	 * @generated
	 */
	boolean pasDansSuivant(Activite act);

} // Activite
