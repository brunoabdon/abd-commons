package br.nom.abdon.modelo;

import java.io.Serializable;

import br.nom.abdon.util.Identifiable;


/**
 *
 * @author bruno
 * @param <Key>
 */
public interface Entidade<Key> extends Identifiable<Key>, Serializable {

    public void setId(Key id);
}