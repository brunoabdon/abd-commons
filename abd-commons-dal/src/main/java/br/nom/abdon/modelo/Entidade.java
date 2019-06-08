package br.nom.abdon.modelo;

import java.io.Serializable;

import br.nom.abdon.util.Identifiable;


/**
 * Um {@link Identifiable} cuja chave pode ser setada.
 * 
 * @param <Key> O tipo da chave do identifiable.
 * 
 * @author bruno abdon
 * 
 */
public interface Entidade<Key> extends Identifiable<Key>, Serializable {

	/**
	 * Seta a chave do elemento.
	 * 
	 * @param id A chave a ser setda.
	 */
    public void setId(Key id);
}